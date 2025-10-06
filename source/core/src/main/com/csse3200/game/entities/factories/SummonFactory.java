package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.hero.engineer.*;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public final class SummonFactory {
    private SummonFactory() {
    }

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
                .addComponent(new CombatStatsComponent(500, /*baseAttack*/0, resistance, weakness))
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
                .addComponent(new CombatStatsComponent(20, 15, resistance, weakness))
                .addComponent(new AutoDespawnOnDeathComponent());              // ✅ 死亡自动移除

        var phys = turret.getComponent(PhysicsComponent.class);
        if (phys != null) {
            phys.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);
        }
        PhysicsUtils.setScaledCollider(turret, 0.8f * scale, 0.8f * scale);
        turret.setScale(scale, scale);

        turret.addComponent(new TurretAttackComponent(
                fireDirection.nor(), attackCooldown, 10f, 1.2f, "images/hero/Bullet.png"));

        turret.addComponent(new FourWayCycleComponent(attackCooldown, fireDirection));

        return turret;
    }

    // —— 产币机器人 ——
// 需要 import CurrencyType, CurrencyGeneratorComponent, AutoDespawnOnDeathComponent
    public static Entity createCurrencyBot(
            String texturePath, float scale,
            Entity owner,
            com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType currencyType,
            int amountPerTick, float intervalSec
    ) {
        var resistance = DamageTypeConfig.None;
        var weakness = DamageTypeConfig.None;

        Entity bot = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent()
                        .setSensor(false)
                        .setLayer(PhysicsLayer.PLAYER))
                .addComponent(new HitboxComponent()
                        .setLayer(PhysicsLayer.PLAYER))
                .addComponent(new TextureRenderComponent(texturePath))
                .addComponent(new CombatStatsComponent(5, 0, resistance, weakness)) // 有少量血量
                .addComponent(new AutoDespawnOnDeathComponent())
                .addComponent(new OwnerComponent(owner))
                .addComponent(new CurrencyGeneratorComponent(owner, currencyType, amountPerTick, intervalSec));

        var phys = bot.getComponent(PhysicsComponent.class);
        if (phys != null) {
            phys.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);
        }
        PhysicsUtils.setScaledCollider(bot, 0.8f * scale, 0.8f * scale);
        bot.setScale(scale, scale);


        return bot;
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


