package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;


public final class SummonFactory {
    private SummonFactory() {}

    /**
     * 近战型召唤物（静止版）：
     * - 无移动、无追击任务；
     * - 敌人碰到命中盒即触发 TouchAttackComponent；
     * - 身体/命中盒均在 PLAYER 层，仅攻击 NPC 层目标。
     *
     * @param texturePath 召唤物贴图
     * @param colliderSensor 是否把实体碰撞体设为传感器（true=不阻挡，仅用于触发）
     * @param scale 视觉缩放（与碰撞体按比例设置）
     * @return 静止的近战召唤物实体
     */
    // 静止近战召唤物（会造成伤害）
    public static Entity createMeleeSummon(String texturePath,
                                           boolean colliderSensor,
                                           float scale) {

        var resistance = DamageTypeConfig.None;
        var weakness   = DamageTypeConfig.None;

        Entity s = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent()
                        .setSensor(false)                 // 实体碰撞（用于“顶开/挡住”）
                        .setLayer(PhysicsLayer.PLAYER)    // ← 还需保证与 NPC 互撞（见下方注意）
                )
                .addComponent(new HitboxComponent()
                        .setLayer(PhysicsLayer.PLAYER))   // 通常 Hitbox 是传感器，用于触发碰撞事件
                .addComponent(new TextureRenderComponent(texturePath))
                .addComponent(new CombatStatsComponent(1, /*baseAttack*/0, resistance, weakness))
                .addComponent(new TouchAttackComponent(PhysicsLayer.NPC, /*knockback*/4.0f)); // ✅ 只击退

// 建议：把召唤物做成路障更稳定
        var phys = s.getComponent(PhysicsComponent.class);
        if (phys != null) {
            phys.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);
        }

// 碰撞盒尺寸别太小，尽量接近 1×1（按你们世界单位/Tile 来）
        PhysicsUtils.setScaledCollider(s, 0.9f * scale, 0.9f * scale);
        s.setScale(scale, scale);

        return s;

    }

    /** 幽灵版近战召唤物：仅显示用于放置预览；不攻击、不阻挡 */
    public static Entity createMeleeSummonGhost(String texturePath, float scale) {
        Entity ghost = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent()
                        .setSensor(true)                 // 不阻挡
                        .setLayer(PhysicsLayer.NONE))    // 不参与任何碰撞层
                .addComponent(new TextureRenderComponent(texturePath));

        // 与正式体保持相同视觉/占位尺寸（便于预览时对齐）
        PhysicsUtils.setScaledCollider(ghost, 0.12f * scale, 0.12f * scale);
        ghost.setScale(scale, scale);

        // 如需半透明，可视你们的 TextureRenderComponent 能力加上（可选）：
        // TextureRenderComponent rc = ghost.getComponent(TextureRenderComponent.class);
        // if (rc != null) rc.setOpacity(0.5f); // 或 rc.setColor(new Color(1f,1f,1f,0.5f));

        return ghost;
    }


}

