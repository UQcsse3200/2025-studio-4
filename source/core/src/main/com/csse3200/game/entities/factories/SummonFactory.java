package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.hero.engineer.AutoDespawnOnDeathComponent; // ✅ 新增 import

import com.csse3200.game.components.hero.engineer.TurretAttackComponent;
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

    // —— 近战召唤物 ——
    public static Entity createMeleeSummon(String texturePath, boolean colliderSensor, float scale) {
        var resistance = DamageTypeConfig.None;
        var weakness = DamageTypeConfig.None;

        Entity s = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent()
                        .setSensor(false)
                        .setLayer(PhysicsLayer.PLAYER))
                .addComponent(new HitboxComponent()
                        .setLayer(PhysicsLayer.PLAYER))
                .addComponent(new TextureRenderComponent(texturePath))
                .addComponent(new CombatStatsComponent(100, /*baseAttack*/0, resistance, weakness))
                .addComponent(new TouchAttackComponent(PhysicsLayer.NPC, /*knockback*/4.0f))
                .addComponent(new AutoDespawnOnDeathComponent());              // ✅ 死亡自动移除

        var phys = s.getComponent(PhysicsComponent.class);
        if (phys != null) {
            phys.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);
        }
        PhysicsUtils.setScaledCollider(s, 0.9f * scale, 0.9f * scale);
        s.setScale(scale, scale);
        return s;
    }

    // —— 定向炮台 ——
    public static Entity createDirectionalTurret(String texturePath, float scale,
                                                 float attackCooldown, Vector2 fireDirection) {
        var resistance = DamageTypeConfig.None;
        var weakness = DamageTypeConfig.None;

        Entity turret = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent()
                        .setSensor(false)
                        .setLayer(PhysicsLayer.PLAYER))
                .addComponent(new HitboxComponent()
                        .setLayer(PhysicsLayer.PLAYER))
                .addComponent(new TextureRenderComponent(texturePath))
                .addComponent(new CombatStatsComponent(5, 15, resistance, weakness))
                .addComponent(new AutoDespawnOnDeathComponent());              // ✅ 死亡自动移除

        var phys = turret.getComponent(PhysicsComponent.class);
        if (phys != null) {
            phys.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);
        }
        PhysicsUtils.setScaledCollider(turret, 0.8f * scale, 0.8f * scale);
        turret.setScale(scale, scale);

        turret.addComponent(new TurretAttackComponent(
                fireDirection.nor(), attackCooldown, 6f, 3f, "images/hero/Bullet.png"));

        return turret;
    }

    // —— 幽灵预览保持不变 ——
    public static Entity createMeleeSummonGhost(String texturePath, float scale) {
        Entity ghost = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent()
                        .setSensor(true)
                        .setLayer(PhysicsLayer.NONE))
                .addComponent(new TextureRenderComponent(texturePath));

        PhysicsUtils.setScaledCollider(ghost, 0.12f * scale, 0.12f * scale);
        ghost.setScale(scale, scale);
        return ghost;
    }
}


