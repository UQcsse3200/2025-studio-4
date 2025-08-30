package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.components.projectile.ProjectileComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.components.projectile.DestroyOnHitComponent;
import com.csse3200.game.entities.configs.DamageTypeConfig; // 👈 重要：导入伤害类型

public final class ProjectileFactory {
  private ProjectileFactory() { throw new IllegalStateException("Static factory class, do not instantiate"); }

  // 保持你原来的五参调用
  public static Entity createBullet(String texture, Vector2 startPos, float vx, float vy, float life) {
    int defaultDamage = 25;
    return createBullet(texture, startPos, vx, vy, life, defaultDamage);
  }

  // 自定义伤害版本
  public static Entity createBullet(String texture, Vector2 startPos, float vx, float vy, float life, int damage) {
    PhysicsComponent physics = new PhysicsComponent();
    physics.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.KinematicBody);

    // ⚠️ 不要链式，避免类型降级
    HitboxComponent hitbox = new HitboxComponent();
    hitbox.setLayer(PhysicsLayer.PROJECTILE);
    hitbox.setSensor(true); // Hitbox 的 create() 里本来也会 setSensor(true)，写不写都行

    Entity bullet = new Entity()
            .addComponent(physics)
            .addComponent(hitbox)
            .addComponent(new TextureRenderComponent(texture))
            .addComponent(new ProjectileComponent(vx, vy, life))
            // 👇 关键：按你们的构造签名补齐 4 个参数
            .addComponent(new CombatStatsComponent(
                    1,                               // maxHealth：子弹自身血量，给个最小值
                    damage,                          // baseAttack：子弹伤害
                    DamageTypeConfig.None,           // 子弹伤害类型（可按需换）
                    DamageTypeConfig.None            // 子弹的抗性（对子弹来说无所谓）
            ))
            .addComponent(new TouchAttackComponent(PhysicsLayer.NPC, 5f)) // 只打 NPC（你的敌人层）
            .addComponent(new DestroyOnHitComponent(PhysicsLayer.NPC));   // 命中即销毁

    bullet.setPosition(startPos);
    return bullet;
  }
}




