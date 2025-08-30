package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.components.projectile.ProjectileComponent;
import com.csse3200.game.physics.PhysicsLayer;

/**
 * 用来创建子弹/投射物实体的工厂类
 */
public final class ProjectileFactory {
  private ProjectileFactory() {
    throw new IllegalStateException("Static factory class, do not instantiate");
  }

  /**
   * 创建一颗子弹实体
   *
   * @param texture   子弹贴图路径
   * @param startPos  初始位置
   * @param vx        初始速度 X
   * @param vy        初始速度 Y
   * @param life      子弹寿命（秒）
   * @return 子弹实体
   */
  public static Entity createBullet(String texture, Vector2 startPos, float vx, float vy, float life) {
    PhysicsComponent physics = new PhysicsComponent();
    physics.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.KinematicBody); // 👈 子弹刚体类型

    ColliderComponent collider = new ColliderComponent()
            .setLayer(PhysicsLayer.PROJECTILE)
            .setSensor(true); // 👈 只触发，不推开其它物体

    Entity bullet = new Entity()
            .addComponent(physics)
            .addComponent(collider)
            .addComponent(new TextureRenderComponent(texture))
            .addComponent(new ProjectileComponent(vx, vy, life));

    bullet.setPosition(startPos);
    return bullet;
  }
}
