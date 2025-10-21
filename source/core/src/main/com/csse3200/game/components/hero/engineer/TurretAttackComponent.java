package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.services.ServiceLocator;

/**
 * {@code TurretAttackComponent}
 * <p>
 * Used by automated robots or turrets that periodically fire bullets
 * in a fixed direction. The attack damage is read from the entity’s
 * {@link CombatStatsComponent}.
 * </p>
 *
 * <ul>
 *   <li>Fires at regular intervals based on cooldown.</li>
 *   <li>Bullets travel in a constant direction and speed.</li>
 *   <li>Damage value depends on the turret's combat stats.</li>
 * </ul>
 */
public class TurretAttackComponent extends Component {
    /**
     * The normalized direction in which the turret fires bullets.
     */
    private Vector2 direction;

    /**
     * Time (in seconds) between consecutive shots.
     */
    private final float cooldown;

    /**
     * Bullet speed in world units per second.
     */
    private final float bulletSpeed;

    /**
     * Bullet lifetime (in seconds) before despawning.
     */
    private final float bulletLife;

    /**
     * Path to the bullet's texture.
     */
    private final String bulletTexture;

    /**
     * Internal cooldown timer.
     */
    private float cdTimer = 0f;
    // --- SFX ---
    private String shootSfxKey = null;   // 比如 "sounds/turret_shoot.ogg"
    private float shootSfxVolume = 1.0f; // 0~1
    private float sfxMinInterval = 0.05f;
    private float sfxCooldown = 0f;



    /**
     * Constructs a new {@code TurretAttackComponent}.
     *
     * @param direction     The initial firing direction.
     * @param cooldown      Time interval between shots.
     * @param bulletSpeed   The speed of fired bullets.
     * @param bulletLife    How long each bullet lasts before disappearing.
     * @param bulletTexture Texture path for the bullet sprite.
     */

    public TurretAttackComponent(Vector2 direction, float cooldown,
                                 float bulletSpeed, float bulletLife, String bulletTexture) {
        this.direction = direction.nor();
        this.cooldown = cooldown;
        this.bulletSpeed = bulletSpeed;
        this.bulletLife = bulletLife;
        this.bulletTexture = bulletTexture;
    }

    /**
     * Updates the turret’s firing direction dynamically.
     *
     * @param newDir The new direction vector (will be normalized).
     */
    public void setDirection(Vector2 newDir) {
        if (newDir != null) {
            this.direction = new Vector2(newDir).nor();
        }
    }

    @Override
    public void update() {
        if (entity == null) return;

        // Get delta time (safe fallback if Gdx.graphics is null)
        float dt = (Gdx.graphics != null) ? Gdx.graphics.getDeltaTime() : (1f / 60f);
        if (sfxCooldown > 0f) sfxCooldown -= dt;
        // Countdown the cooldown timer
        if (cdTimer > 0f) {

            cdTimer -= dt;
            return;
        }

        // Compute the position to spawn the bullet (entity’s center)
        Vector2 firePos = getEntityCenter(entity);

        // Compute velocity components based on direction and speed
        float vx = direction.x * bulletSpeed;
        float vy = direction.y * bulletSpeed;

        // Retrieve attack damage from the entity’s combat stats
        int dmg = computeDamageFromStats();

        // Create a new bullet entity
        final Entity bullet = ProjectileFactory.createBullet(
                bulletTexture, firePos, vx, vy, bulletLife, dmg, true
        );

        // Register the bullet entity safely on the next frame
        var es = ServiceLocator.getEntityService();
        if (es != null) {
            Gdx.app.postRunnable(() -> es.register(bullet));
        }

        // Reset cooldown timer
        playShootSfx();
        cdTimer = cooldown;
    }
    public TurretAttackComponent setShootSfxKey(String key) {
        this.shootSfxKey = key;
        return this;
    }
    public TurretAttackComponent setShootSfxVolume(float v) {
        this.shootSfxVolume = Math.max(0f, Math.min(1f, v));
        return this;
    }
    public TurretAttackComponent setShootSfxMinInterval(float sec) {
        this.sfxMinInterval = Math.max(0f, sec);
        return this;
    }


    /**
     * Retrieves the attack damage from the {@link CombatStatsComponent}.
     * If unavailable, defaults to 1.
     *
     * @return The calculated base damage.
     */
    private int computeDamageFromStats() {
        CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
        int base = (stats != null) ? stats.getBaseAttack() : 1;
        return Math.max(1, base);
    }

    /**
     * Gets the approximate center position of the entity.
     * Falls back to using position and scale if {@code getCenterPosition()} is unavailable.
     *
     * @param e The entity to calculate the center for.
     * @return A {@link Vector2} representing the entity's center position.
     */
    private static Vector2 getEntityCenter(Entity e) {
        try {
            Vector2 center = e.getCenterPosition();
            if (center != null) return center;
        } catch (Throwable ignored) {
        }

        Vector2 pos = e.getPosition();
        Vector2 scale = e.getScale();

        float cx = pos.x + (scale != null ? scale.x * 0.5f : 0.5f);
        float cy = pos.y + (scale != null ? scale.y * 0.5f : 0.5f);

        return new Vector2(cx, cy);
    }
    private void playShootSfx() {
        if (shootSfxKey == null || shootSfxKey.isBlank()) return;
        if (sfxCooldown > 0f) return;

        float vol = Math.max(0f, Math.min(1f, shootSfxVolume));
        try {
            var rs = ServiceLocator.getResourceService();
            com.badlogic.gdx.audio.Sound s = null;
            if (rs != null) {
                try { s = rs.getAsset(shootSfxKey, com.badlogic.gdx.audio.Sound.class); } catch (Throwable ignored) {}
            }
            if (s != null) {
                s.play(vol);
            } else {
                if (!Gdx.files.internal(shootSfxKey).exists() || Gdx.audio == null) return;
                com.badlogic.gdx.audio.Sound s2 = Gdx.audio.newSound(Gdx.files.internal(shootSfxKey));
                s2.play(vol);
            }
        } catch (Throwable t) {
            // 仅记录或忽略都行
        }
        sfxCooldown = sfxMinInterval;
    }

}