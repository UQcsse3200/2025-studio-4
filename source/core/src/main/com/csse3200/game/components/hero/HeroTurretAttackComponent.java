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

/**
 * Hero turret-style attack component:
 * - Does not handle movement.
 * - Fires bullets towards the mouse cursor when cooldown expires.
 * - Rotates the hero sprite to face the mouse direction.
 * - Bullet damage is computed from CombatStatsComponent (baseAttack) at fire time.
 * - Sound is played directly via ResourceService (Sound).play(volume).
 */
public class HeroTurretAttackComponent extends Component {
    private float cooldown;
    private float bulletSpeed;
    private float bulletLife;
    private String bulletTexture;
    private Camera camera;
    private String overrideShootSfxKey = null;
    private Float  overrideShootSfxVol = null;
    /** 音效资源键（通常为 assets 内路径或你们的别名键） */
    private String shootSfxKey = null; // 由配置注入
    private float shootSfxVolume = 1.0f; // 合理默认，范围[0,1]

    /** 限频：避免极短时间内叠加过多 */
    private float shootSfxMinInterval = 0.06f;
    private float sfxCooldown = 0f;

    private float cdTimer = 0f;
    private final Vector3 tmp3 = new Vector3();
    private final Vector2 mouseWorld = new Vector2();
    private final Vector2 dir = new Vector2(); // Reused to avoid frequent allocation

    /** Optional: flat bonus damage and scaling multiplier for buffs/passives (default = no bonus). */
    private int flatBonusDamage = 0;     // +X damage
    private float attackScale = 1f;      // ×Y multiplier

    /** Adjust based on default sprite orientation: 0 if facing right, -90 if facing up. */
    private static final float SPRITE_FACING_OFFSET_DEG = -90f;

    public HeroTurretAttackComponent(float cooldown, float bulletSpeed, float bulletLife,
                                     String bulletTexture, Camera camera) {
        this.cooldown = cooldown;
        this.bulletSpeed = bulletSpeed;
        this.bulletLife = bulletLife;
        this.bulletTexture = bulletTexture;
        this.camera = camera;
    }

    public void setBulletTexture(String bulletTexture) {
        this.bulletTexture = bulletTexture;
    }

    public HeroTurretAttackComponent setCooldown(float s) {
        this.cooldown = s;
        return this;
    }

    public HeroTurretAttackComponent setBulletParams(float speed, float life) {
        this.bulletSpeed = speed;
        this.bulletLife = life;
        return this;
    }

    /** （可选）更换音效键；可传资源路径或你们的别名键 */
    public HeroTurretAttackComponent setShootSfxKey(String key) {
        this.shootSfxKey = key;
        return this;
    }

    /** （可选）设置音量 */
    public HeroTurretAttackComponent setShootSfxVolume(float volume) {
        this.shootSfxVolume = Math.max(0f, Math.min(1f, volume));
        return this;
    }

    @Override
    public void create() {
        // Listen for ultimate ability multipliers (HeroUltimateComponent triggers "attack.multiplier")
        entity.getEvents().addListener("attack.multiplier", (Float mul) -> {
            if (mul == null || mul <= 0f) mul = 1f;
            this.attackScale = mul; // Multiplier directly applied in computeDamageFromStats()
        });
        // 声音资源请在关卡/区域加载阶段预加载（见 ForestGameArea.loadAssets）
        entity.getEvents().addListener("attack.sfx.override", (String key, Float vol) -> {
            this.overrideShootSfxKey = (key != null && !key.isBlank()) ? key : null;
            this.overrideShootSfxVol = (vol != null) ? Math.max(0f, Math.min(1f, vol)) : null;
        });

        // ★ 监听：清除覆盖
        entity.getEvents().addListener("attack.sfx.clear", () -> {
            this.overrideShootSfxKey = null;
            this.overrideShootSfxVol = null;
        });
    }

    @Override
    public void update() {
        if (entity == null) return;

        float dt = (Gdx.graphics != null) ? Gdx.graphics.getDeltaTime() : (1f / 60f);
        if (cdTimer > 0f) cdTimer -= dt;
        if (sfxCooldown > 0f) sfxCooldown -= dt;

        // Compute aiming direction
        Vector2 firePos = getEntityCenter(entity);
        if (!computeAimDirection(firePos, dir)) return;

        // Rotate sprite to face aim direction
        RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
        if (rot != null) {
            float angleDeg = dir.angleDeg() + SPRITE_FACING_OFFSET_DEG;
            rot.setRotation(angleDeg);
        }

        // Fire when cooldown expires
        if (cdTimer <= 0f) {
            float vx = dir.x * bulletSpeed;
            float vy = dir.y * bulletSpeed;

            // ⭐ Damage is always read from CombatStatsComponent at fire time
            int dmg = computeDamageFromStats();

            final Entity bullet = ProjectileFactory.createBullet(
                    bulletTexture, firePos, vx, vy, bulletLife, dmg
            );

            var es = ServiceLocator.getEntityService();
            if (es != null) {
                Gdx.app.postRunnable(() -> es.register(bullet));
                entity.getEvents().trigger("hero:shoot", firePos.x, firePos.y, null);
            } else {
                Gdx.app.error("HeroTurret", "EntityService is null; skip bullet spawn this frame");
            }

            // 直接播放音效
            playShootSfxDirect();

            cdTimer = cooldown;
        }
    }

    /**
     * Compute damage based on combat stats:
     * baseAttack (from CombatStatsComponent) * attackScale + flatBonusDamage.
     * Rounded to nearest integer, minimum value = 1.
     */
    private int computeDamageFromStats() {
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        int base = (stats != null) ? stats.getBaseAttack() : 1;
        float scaled = base * attackScale + flatBonusDamage;
        return Math.max(1, Math.round(scaled));
    }

    /**
     * Compute normalized direction vector from fire position to mouse world coordinates.
     */
    private boolean computeAimDirection(Vector2 firePos, Vector2 outDir) {
        if (camera == null) return false;
        Vector3 tmp3 = this.tmp3.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(tmp3);
        mouseWorld.set(tmp3.x, tmp3.y);

        outDir.set(mouseWorld).sub(firePos);
        if (outDir.isZero(0.0001f)) return false;
        outDir.nor();
        return true;
    }

    /** 直接通过 ResourceService 播放射击音效（与 CurrencyManagerComponent 的写法一致） */
    // 直接通过 ResourceService 播放射击音效（带回退 + 日志）
    /** 直接播放射击音效：带存在性检查 + 设备检查 + 回退 + 清晰日志，不再抛异常 */
    private void playShootSfxDirect() {
        // 先合并覆盖值（例如大招/状态切换期间想临时换音效）
        final String key = (overrideShootSfxKey != null && !overrideShootSfxKey.isBlank())
                ? overrideShootSfxKey
                : shootSfxKey;
        final float baseVol = (overrideShootSfxVol != null) ? overrideShootSfxVol : shootSfxVolume;

        // 基本校验 & 限频
        if (key == null || key.isBlank()) return;
        if (sfxCooldown > 0f) return;

        // 归一化音量 0~1
        final float vol = Math.max(0f, Math.min(1f, baseVol));

        try {
            // 优先从 ResourceService 里拿（要求你在关卡/区域里预加载了该 sound）
            var rs = ServiceLocator.getResourceService();
            if (rs != null) {
                Sound s = null;
                try {
                    s = rs.getAsset(key, Sound.class); // 未加载好可能为 null 或抛异常
                } catch (Throwable ignored) {
                    // 忽略异常，继续尝试回退加载
                }
                if (s != null) {
                    long id = s.play(vol);
                    Gdx.app.log("HeroTurretSFX",
                            "Played via ResourceService: key=" + key + ", id=" + id + ", vol=" + vol
                                    + (overrideShootSfxKey != null ? " (override)" : ""));
                    sfxCooldown = shootSfxMinInterval;
                    return;
                } else {
                    Gdx.app.error("HeroTurretSFX",
                            "Sound not in ResourceService (or null): " + key + " -> try fallback newSound()");
                }
            } else {
                Gdx.app.error("HeroTurretSFX",
                        "ResourceService is null -> try fallback newSound()");
            }

            // 回退：直接用 LibGDX newSound（确保文件存在且音频后端可用）
            if (!Gdx.files.internal(key).exists()) {
                Gdx.app.error("HeroTurretSFX",
                        "Asset file NOT FOUND: " + key + " (check path/case and that it’s in assets)");
                return;
            }
            if (Gdx.audio == null) {
                Gdx.app.error("HeroTurretSFX",
                        "Gdx.audio is NULL (headless test? audio backend not initialized)");
                return;
            }

            Sound s2 = Gdx.audio.newSound(Gdx.files.internal(key));
            long id2 = s2.play(vol);
            Gdx.app.log("HeroTurretSFX",
                    "Played via fallback newSound: key=" + key + ", id=" + id2 + ", vol=" + vol
                            + (overrideShootSfxKey != null ? " (override)" : ""));
            sfxCooldown = shootSfxMinInterval;

            // 说明：这里没有立即 dispose()，否则会中断播放。若担心泄漏，可做一个缓存复用。

        } catch (Throwable t) {
            // 不让异常冒泡，避免打断射击流程
            Gdx.app.error("HeroTurretSFX",
                    "Play failed for key=" + key + ", vol=" + vol, t);
        }
    }




    /**
     * Get entity center:
     * - Prefer using getCenterPosition() if available.
     * - Otherwise, fall back to position + half-scale.
     */
    private static Vector2 getEntityCenter(Entity e) {
        try {
            Vector2 center = e.getCenterPosition();
            if (center != null) return center;
        } catch (Throwable ignored) { }
        Vector2 pos = e.getPosition();
        Vector2 scale = e.getScale();
        float cx = pos.x + (scale != null ? scale.x * 0.5f : 0.5f);
        float cy = pos.y + (scale != null ? scale.y * 0.5f : 0.5f);
        return new Vector2(cx, cy);
    }

    // ===== Optional: chainable API for setting damage modifiers (useful for passives, items, buffs) =====

    /** Set flat bonus damage (+X). */
    public HeroTurretAttackComponent setFlatBonusDamage(int flatBonusDamage) {
        this.flatBonusDamage = flatBonusDamage;
        return this;
    }

    /** Set damage multiplier (×Y). */
    public HeroTurretAttackComponent setAttackScale(float attackScale) {
        this.attackScale = attackScale;
        return this;
    }
}
