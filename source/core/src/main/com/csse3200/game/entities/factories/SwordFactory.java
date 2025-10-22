package com.csse3200.game.entities.factories;

import com.csse3200.game.components.hero.samurai.SkillCooldowns;
import com.csse3200.game.components.hero.samurai.SwordAppearanceComponent;
import com.csse3200.game.components.hero.samurai.SwordLevelSyncComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.SamuraiConfig;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;

/**
 * Factory class for creating the Samurai's sword entity.
 * <p>
 * ❗ This factory creates a sword with **visuals + physics only**.
 * It does NOT attach melee hit logic or the legacy SwordJabPhysicsComponent.
 * The three attack components (Jab/Sweep/Spin), the mutual-exclusion lock, and the controller
 * should be attached by upper layers (e.g., {@code SamuraiSpinAttackComponent}).
 */
public final class SwordFactory {
    private SwordFactory() {
    }

    /**
     * Create a sword entity with only visuals and physics, and configure cooldowns and level sync.
     *
     * @param owner           The samurai entity (used for appearance/events forwarding/level sync)
     * @param swordTexture    Path to the sword texture
     * @param radius          Visual radius for the sword handle circling around the owner
     *                        (used by appearance/attack components for calculations)
     * @param cfg             Samurai configuration
     * @param angularSpeedDeg (Optional) Kept in signature; not used inside the factory for now,
     *                        controlled by upper layers instead
     * @return A configured sword entity (NOT registered; caller should register it)
     */
    public static Entity createSword(Entity owner,
                                     SamuraiConfig cfg,
                                     String swordTexture,
                                     float radius,
                                     float angularSpeedDeg) {

        Entity sword = new Entity()
                // Physics body only for setTransform / positioning
                .addComponent(new PhysicsComponent())

                // Rendering (rotating texture) + appearance (can adjust visuals based on level/config)
                .addComponent(new RotatingTextureRenderComponent(swordTexture))
                .addComponent(new SwordAppearanceComponent(owner, cfg))

                // Multi-skill cooldowns (to be used by Jab/Sweep/Spin)
                .addComponent(new SkillCooldowns()
                        .setTotal("jab", 3.0f)
                        .setTotal("spin", 5.0f)
                        .setTotal("sweep", 1.2f))

                // Level sync (if visuals/params need to react to the samurai's level)
                .addComponent(new SwordLevelSyncComponent(owner, cfg));

        // Forward cooldown events to the owner (UI can simply listen on the hero)
        sword.getEvents().addListener("skill:cooldown",
                (com.csse3200.game.components.hero.samurai.SkillCooldowns.SkillCooldownInfo info) -> {
                    if (owner != null) owner.getEvents().trigger("skill:cooldown", info);
                });
        sword.getEvents().addListener("skill:ready",
                (String skill) -> {
                    if (owner != null) owner.getEvents().trigger("skill:ready", skill);
                });

        return sword;
    }

    /**
     * Legacy signature: keep an overload to maintain compatibility with existing call sites.
     * (Parameter order matches your current usage: owner, cfg, swordTexture, radius, angularSpeedDeg)
     */
    public static Entity createSword(Entity owner,
                                     String swordTexture,
                                     float radius,
                                     SamuraiConfig cfg,
                                     float angularSpeedDeg) {
        // Delegate to the new primary implementation (parameter order differs slightly)
        return createSword(owner, cfg, swordTexture, radius, angularSpeedDeg);
    }
}

