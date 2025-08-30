package com.csse3200.game.components.projectile;

import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * 子弹逻辑：设定线速度与寿命，到时间由 EntityService 统一删除，避免 iterator 嵌套崩溃。
 */
public class ProjectileComponent extends Component {
  private boolean dead = false;
  private final float vx, vy;   // 速度（单位/秒）
  private final float life;     // 寿命（秒）
  private float timer;
  private PhysicsComponent physics;

  public ProjectileComponent(float vx, float vy, float life) {
    this.vx = vx;
    this.vy = vy;
    this.life = life;
  }

  @Override
  public void create() {
    physics = entity.getComponent(PhysicsComponent.class);
    timer = life;
    // 如果有物理组件，使用物理速度；否则你也可以在 update 里用 setPosition 手动移动
    if (physics != null && physics.getBody() != null) {
      physics.getBody().setLinearVelocity(vx, vy);
    }
  }

  @Override
  public void update() {
    float dt = ServiceLocator.getTimeSource().getDeltaTime();
    timer -= dt;

    if (timer <= 0f) {
      if (dead) return;
      dead = true;

      // 1) 停物理（避免这一帧还发生碰撞）
      if (physics != null && physics.getBody() != null) {
        physics.getBody().setLinearVelocity(0, 0);
        physics.getBody().setActive(false);
      }

      // 2) 把“真正销毁”丢到下一帧，避开当前 for-each 遍历
      com.badlogic.gdx.Gdx.app.postRunnable(() -> {
        try {
          entity.dispose(); // 释放组件（Render/Physics等）
        } finally {
          ServiceLocator.getEntityService().unregister(entity); // 从实体列表移除
        }
      });
    }
  }
}

