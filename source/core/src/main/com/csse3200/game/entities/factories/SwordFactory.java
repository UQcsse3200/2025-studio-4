package com.csse3200.game.entities.factories;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.hero.samurai.SwordAppearanceComponent;
import com.csse3200.game.components.hero.samurai.SwordJabPhysicsComponent;
import com.csse3200.game.components.hero.samurai.SwordLevelSyncComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.entities.configs.SamuraiConfig;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;

/**
 * Factory class for creating the Samurai's sword entity.
 * <p>
 * The sword is treated as a separate orbiting entity attached to the Samurai.
 * It handles its own visual rendering, rotation physics, and collision-based
 * attack logic. The sword can jab (thrust forward) and deal periodic damage
 * to enemies within range.
 * </p>
 */
public final class SwordFactory {
    private SwordFactory() {
        // Prevent instantiation (utility class)
    }

    /**
     * Creates a sword entity that orbits around its owner like a rotating blade.
     * <p>
     * This version allows detailed configuration of sword behavior, including
     * angular speed, sprite alignment, jab timing, and hit cooldown.
     * </p>
     *
     * @param owner                  The Samurai entity that owns the sword.
     * @param swordTexture           Path to the sword’s texture.
     * @param radius                 Orbit radius where the sword handle sits (in world units).
     * @param cfg                    The {@link SamuraiConfig} containing hero stats and settings.
     * @param angularSpeedDeg        Angular speed in degrees per second (counterclockwise positive).
     * @param spriteForwardOffsetDeg Default facing direction for the sword sprite:
     *                               → = 0°, ↑ = 90°, ← = 180°, ↓ = 270°.
     * @param centerToHandle         Distance from the sprite’s center to its handle
     *                               along the forward direction (usually negative).
     * @param damage                 Base damage dealt by the sword.
     * @param hitCooldown            Cooldown between hitting the same target (in seconds).
     * @return A configured sword entity orbiting the Samurai.
     */
    public static Entity createSword(Entity owner,
                                     String swordTexture,
                                     float radius,
                                     SamuraiConfig cfg,
                                     float angularSpeedDeg,
                                     float spriteForwardOffsetDeg,
                                     float centerToHandle,
                                     int damage,
                                     float hitCooldown) {

        Entity sword = new Entity()
                // === Physics and collision setup ===
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent()
                        .setLayer(PhysicsLayer.PLAYER_ATTACK)
                        .setSensor(true)) // Sensor: detects collisions but doesn’t apply physics forces
                // === Rendering ===
                .addComponent(new RotatingTextureRenderComponent(swordTexture))
                .addComponent(new SwordAppearanceComponent(owner, cfg))
                // === Movement and orbiting physics ===
                .addComponent(new SwordJabPhysicsComponent(owner, radius)
                        .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                        .setCenterToHandle(centerToHandle)
                        .setJabParams(0.18f, 0.8f)     // Jab duration/distance (tweak for animation feel)
                        .setJabCooldown(0.05f))        // Minimum interval between jabs
                // === Combat logic ===
                .addComponent(new CombatStatsComponent(
                        10, damage,                   // Health, Attack Power
                        DamageTypeConfig.None,
                        DamageTypeConfig.None))
                .addComponent(new TouchAttackComponent(PhysicsLayer.NPC, hitCooldown))
                // === Level sync ===
                .addComponent(new SwordLevelSyncComponent(owner, cfg));

        return sword;
    }

    /**
     * Overloaded helper method that creates a sword with common default parameters.
     * <p>
     * Defaults:
     * <ul>
     *   <li>{@code spriteForwardOffsetDeg = 0°}</li>
     *   <li>{@code centerToHandle = -0.25f}</li>
     *   <li>{@code damage = 10}</li>
     *   <li>{@code hitCooldown = 0.2s}</li>
     * </ul>
     * </p>
     *
     * @param owner           The Samurai entity that owns the sword.
     * @param cfg             The {@link SamuraiConfig} with stats and texture paths.
     * @param swordTexture    Path to the sword’s texture.
     * @param radius          Orbit radius around the Samurai.
     * @param angularSpeedDeg Angular speed in degrees per second.
     * @return A sword entity configured with default combat and animation values.
     */
    public static Entity createSword(Entity owner,
                                     SamuraiConfig cfg,
                                     String swordTexture,
                                     float radius,
                                     float angularSpeedDeg) {
        return createSword(
                owner,
                swordTexture,
                radius,
                cfg,
                angularSpeedDeg,
                0f,        // Sprite forward offset (facing right)
                -0.25f,    // Distance from sprite center to handle
                10,        // Default damage
                0.2f       // Default hit cooldown (seconds)
        );
    }
}

