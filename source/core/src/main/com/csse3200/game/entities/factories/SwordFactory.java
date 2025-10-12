package com.csse3200.game.entities.factories;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.hero.samurai.SkillCooldowns;
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
     * This version allows detailed configuration of sword behavior, including
     * angular speed, sprite alignment, jab timing, and hit cooldown.
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
                                     int damage,          // ← 现在对剑本体无用，可保留签名
                                     float hitCooldown) { // ← 现在对剑本体无用，可保留签名

        Entity sword = new Entity()
                // === 仅保留用于 setTransform 的物理体 ===
                .addComponent(new PhysicsComponent())

                // ❌ 不再添加碰撞体，避免任何近战接触
                // .addComponent(new ColliderComponent())
                // .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER_ATTACK).setSensor(true))

                // === 渲染 ===
                .addComponent(new RotatingTextureRenderComponent(swordTexture))
                .addComponent(new SwordAppearanceComponent(owner, cfg))

                // === 运动与轨道逻辑（保留，用于视觉/朝向/发射方向计算）===
                .addComponent(new SwordJabPhysicsComponent(owner, radius)
                        .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                        .setCenterToHandle(centerToHandle)
                        .setJabParams(0.18f, 0.8f)
                        .setJabCooldown(0.05f))

                // ❌ 不再在“剑”上挂近战 CombatStats（如果系统别处强依赖，可保留一个占位，攻击力设置为0）
                // .addComponent(new CombatStatsComponent(10, damage, DamageTypeConfig.None, DamageTypeConfig.None))

                // ❌ 不再在“剑”上挂触碰伤害
                // .addComponent(new TouchAttackComponent(PhysicsLayer.NPC, hitCooldown))

                // === 仍保留多技能冷却，用于 Jab/Sweep/Spin 的触发节流 ===
                .addComponent(new SkillCooldowns()
                        .setTotal("jab",   3.0f)
                        .setTotal("spin",  5.0f)
                        .setTotal("sweep", 1.2f))

                // === 等级同步（保留）===
                .addComponent(new SwordLevelSyncComponent(owner, cfg));

        // 透传冷却事件到 owner（保留）
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
     * Overloaded helper method that creates a sword with common default parameters.
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

