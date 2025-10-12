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
 *
 * ❗ 本工厂只创建“可视 + 物理”的剑；不再附加近战命中/老的 SwordJabPhysicsComponent。
 * 三种攻击组件（Jab/Sweep/Spin）、互斥锁、控制器由上层（如 SamuraiSpinAttackComponent）负责挂载。
 */
public final class SwordFactory {
    private SwordFactory() {}

    /**
     * 创建“纯可视 + 物理”的剑实体，并配置冷却与等级同步。
     *
     * @param owner                武士实体（用于外观/事件转发/等级同步）
     * @param swordTexture         剑贴图路径
     * @param radius               视觉上剑柄绕 owner 的半径（供外观组件/攻击组件计算用）
     * @param cfg                  Samurai 配置
     * @param angularSpeedDeg      （可选）保留签名；当前不在工厂内使用，由上层控制
     * @return                     已配置好的“剑”实体（未注册；由调用者自行注册）
     */
    public static Entity createSword(Entity owner,
                                     SamuraiConfig cfg,
                                     String swordTexture,
                                     float radius,
                                     float angularSpeedDeg) {

        Entity sword = new Entity()
                // 仅用于 setTransform 的物理体
                .addComponent(new PhysicsComponent())

                // 渲染（旋转贴图）+ 外观（可根据等级/配置调整显示）
                .addComponent(new RotatingTextureRenderComponent(swordTexture))
                .addComponent(new SwordAppearanceComponent(owner, cfg))

                // 多技能冷却（供 Jab/Sweep/Spin 使用）
                .addComponent(new SkillCooldowns()
                        .setTotal("jab",   3.0f)
                        .setTotal("spin",  5.0f)
                        .setTotal("sweep", 1.2f))

                // 等级同步（如需根据武士等级动态调整外观/参数）
                .addComponent(new SwordLevelSyncComponent(owner, cfg));

        // 冷却事件向 owner 透传（UI 在 hero 上监听即可）
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
     * 旧签名：保留一个重载以兼容你现有调用处。
     * （参数顺序与你当前使用一致：owner, cfg, swordTexture, radius, angularSpeedDeg）
     */
    public static Entity createSword(Entity owner,
                                     String swordTexture,
                                     float radius,
                                     SamuraiConfig cfg,
                                     float angularSpeedDeg) {
        // 转调到新的主实现（参数顺序略不同，这里统一一下）
        return createSword(owner, cfg, swordTexture, radius, angularSpeedDeg);
    }
}


