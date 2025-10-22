package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.GameTime;

/**
 * Hero turret-style attack component:
 * - Fires bullets toward the mouse cursor on cooldown.
 * - Rotates the hero sprite to face aim direction.
 * - Damage is computed from CombatStatsComponent (baseAttack) * attackScale + flatBonusDamage.
 * - Respects GameTime time scale (paused => no fire).
 * - Supports shoot SFX with override and rate limiting.
 */
public class HeroTurretAttackComponent extends Component {
    private float cooldown;
    private float bulletSpeed;
    private float bulletLife;
    private String bulletTexture;
    private Camera camera;

    // SFX
    private String shootSfxKey = null;         // default key from config
    private float shootSfxVolume = 1.0f;       // [0,1]
    private String overrideShootSfxKey = null; // temporary override via events
    private Float  overrideShootSfxVol = null; // temporary volume override
    private float shootSfxMinInterval = 0.06f; // avoid spam
    private float sfxCooldown = 0f;

    // Attack modifiers
    private int   flatBonusDamage = 0;
    private float attackScale     = 1f;

    private float cdTimer = 0f;

    private final Vector3 tmp3       = new Vector3();
    private final Vector2 mouseWorld = new Vector2();
    private final Vector2 dir        = new Vector2();

    /** Adjust based on default sprite orientation: 0 if facing right, -90 if facing up. */
    private static final float SPRITE_FACING_OFFSET_DEG = -90f;

    /**
     * @param cooldown      seconds between shots
     * @param bulletSpeed   bullet speed in world units per second
     * @param bulletLife    bullet lifetime in seconds
     * @param bulletTexture bullet sprite texture path
     * @param camera        world camera used to unproject mouse coordinates
     */
    public HeroTurretAttackComponent(float cooldown, float bulletSpeed, float bulletLife,
                                     String bulletTexture, Camera camera) {
        this.cooldown = cooldown;
        this.bulletSpeed = bulletSpeed;
        this.bulletLife = bulletLife;
        this.bulletTexture = bulletTexture;
        this.camera = camera;
    }

    /** Change the bullet texture at runtime. */
    public void setBulletTexture(String bulletTexture) {
        this.bulletTexture = bulletTexture;
    }

    /** Set the firing cooldown (seconds). */
    public HeroTurretAttackComponent setCooldown(float s) {
        this.cooldown = s;
        return this;
    }

    /** Set bullet speed and lifetime. */
    public HeroTurretAttackComponent setBulletParams(float speed, float life) {
        this.bulletSpeed = speed;
        this.bulletLife = life;
        return this;
    }

    /** (Optional) set default shoot SFX key. */
    public HeroTurretAttackComponent setShootSfxKey(String key) {
        this.shootSfxKey = key;
        return this;
    }

    /** (Optional) set default SFX volume [0,1]. */
    public HeroTurretAttackComponent setShootSfxVolume(float volume) {
        this.shootSfxVolume = Math.max(0f, Math.min(1f, volume));
        return this;
    }

    /** Set flat bonus damage (+X). */
    public HeroTurretAttackComponent setFlatBonusDamage(int flatBonusDamage) {
        this.flatBonusDamage = flatBonusDamage;
        return this;
    }

    /** Set multiplicative damage scale (×Y). */
    public HeroTurretAttackComponent setAttackScale(float attackScale) {
        this.attackScale = attackScale;
        return this;
    }

    /** Subscribe to multipliers and SFX override events. */
    @Override
    public void create() {
        // Multiplier from ult/buffs.
        entity.getEvents().addListener("attack.multiplier", (Float mul) -> {
            if (mul == null || mul <= 0f) mul = 1f;
            this.attackScale = mul;
        });

        // Temporary SFX override (e.g., during ult).
        entity.getEvents().addListener("attack.sfx.override", (String key, Float vol) -> {
            this.overrideShootSfxKey = (key != null && !key.isBlank()) ? key : null;
            this.overrideShootSfxVol = (vol != null) ? Math.max(0f, Math.min(1f, vol)) : null;
        });

        // Clear SFX override.
        entity.getEvents().addListener("attack.sfx.clear", () -> {
            this.overrideShootSfxKey = null;
            this.overrideShootSfxVol = null;
        });
    }

    /**
     * Rotate to face the mouse, and when off cooldown spawn a bullet with computed damage.
     * Respects GameTime time scale (no shooting when paused).
     */
    @Override
    public void update() {
        if (entity == null) return;

        final GameTime gameTime = ServiceLocator.getTimeSource();
        final float dt = (gameTime != null) ? gameTime.getDeltaTime()
                : ((Gdx.graphics != null) ? Gdx.graphics.getDeltaTime() : 0f);
        if (cdTimer > 0f)      cdTimer      -= dt;
        if (sfxCooldown > 0f)  sfxCooldown  -= dt;

        // Compute aim direction
        final Vector2 firePos = getEntityCenter(entity);
        if (!computeAimDirection(firePos, dir)) return;

        // Rotate sprite to face aim direction (ok to rotate while paused)
        RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
        if (rot != null) {
            float angleDeg = dir.angleDeg() + SPRITE_FACING_OFFSET_DEG;
            rot.setRotation(angleDeg);
        }

        // Only fire if not paused and cooldown expired
        final boolean canFireNow = (gameTime == null || gameTime.getTimeScale() > 0f) && cdTimer <= 0f;
        if (!canFireNow) return;

        float vx = dir.x * bulletSpeed;
        float vy = dir.y * bulletSpeed;

        int dmg = computeDamageFromStats();

      final Entity bullet = ProjectileFactory.createBullet(
              bulletTexture, firePos, vx, vy, bulletLife, dmg, true
      );

        var es = ServiceLocator.getEntityService();
        if (es != null) {
            Gdx.app.postRunnable(() -> es.register(bullet));
            // notify UI/FX
            entity.getEvents().trigger("hero:shoot", firePos.x, firePos.y, null);
        } else {
            Gdx.app.error("HeroTurret", "EntityService is null; skip bullet spawn this frame");
        }

        // Play SFX (rate-limited and with overrides)
        playShootSfxDirect();

        cdTimer = cooldown;
    }

    /** Compute damage = max(1, round(base * attackScale + flatBonusDamage)). */
    private int computeDamageFromStats() {
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        int base = (stats != null) ? stats.getBaseAttack() : 1;
        float scaled = base * attackScale + flatBonusDamage;
        return Math.max(1, Math.round(scaled));
    }

    /** Compute normalized direction vector from fire position to mouse world coordinates. */
    private boolean computeAimDirection(Vector2 firePos, Vector2 outDir) {
        if (camera == null) return false;
        tmp3.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(tmp3);
        mouseWorld.set(tmp3.x, tmp3.y);

        outDir.set(mouseWorld).sub(firePos);
        if (outDir.isZero(0.0001f)) return false;
        outDir.nor();
        return true;
    }

    /** SFX playback with override, existence checks, rate limit and clear logs. */
    private void playShootSfxDirect() {
        final String key = (overrideShootSfxKey != null && !overrideShootSfxKey.isBlank())
                ? overrideShootSfxKey
                : shootSfxKey;
        final float baseVol = (overrideShootSfxVol != null) ? overrideShootSfxVol : shootSfxVolume;

        if (key == null || key.isBlank()) return;
        if (sfxCooldown > 0f) return;

        final float vol = Math.max(0f, Math.min(1f, baseVol));

        try {
            var rs = ServiceLocator.getResourceService();
            if (rs != null) {
                Sound s = null;
                try {
                    s = rs.getAsset(key, Sound.class);
                } catch (Throwable ignored) {}
                if (s != null) {
                    long id = s.play(vol);
                    // Gdx.app.log("HeroTurretSFX",
                    //         "Played via ResourceService: key=" + key + ", id=" + id + ", vol=" + vol
                    //                 + (overrideShootSfxKey != null ? " (override)" : ""));
                    sfxCooldown = shootSfxMinInterval;
                    return;
                } else {
                    Gdx.app.error("HeroTurretSFX",
                            "Sound not in ResourceService (or null): " + key + " -> try fallback newSound()");
                }
            } else {
                Gdx.app.error("HeroTurretSFX", "ResourceService is null -> try fallback newSound()");
            }

            if (!Gdx.files.internal(key).exists()) {
                Gdx.app.error("HeroTurretSFX", "Asset file NOT FOUND: " + key);
                return;
            }
            if (Gdx.audio == null) {
                Gdx.app.error("HeroTurretSFX", "Gdx.audio is NULL (headless test?)");
                return;
            }

            Sound s2 = Gdx.audio.newSound(Gdx.files.internal(key));
            long id2 = s2.play(vol);
            Gdx.app.log("HeroTurretSFX",
                    "Played via fallback newSound: key=" + key + ", id=" + id2 + ", vol=" + vol
                            + (overrideShootSfxKey != null ? " (override)" : ""));
            sfxCooldown = shootSfxMinInterval;

            // 注意：不立即 dispose() 以避免中断播放；如需优化可做缓存复用。
        } catch (Throwable t) {
            Gdx.app.error("HeroTurretSFX", "Play failed for key=" + key + ", vol=" + vol, t);
        }
    }

    /** Utility to get entity center if available; otherwise approximate from position/scale. */
    private static Vector2 getEntityCenter(Entity e) {
        try {
            Vector2 center = e.getCenterPosition();
            if (center != null) return center;
        } catch (Throwable ignored) {}
        Vector2 pos = e.getPosition();
        Vector2 scale = e.getScale();
        float cx = pos.x + (scale != null ? scale.x * 0.5f : 0.5f);
        float cy = pos.y + (scale != null ? scale.y * 0.5f : 0.5f);
        return new Vector2(cx, cy);
    }
}
