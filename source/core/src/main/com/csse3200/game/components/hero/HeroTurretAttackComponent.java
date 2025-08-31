package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * 固定炮台式英雄攻击组件：
 * - 不处理移动
 * - 冷却结束时，朝着鼠标方向发射子弹
 * - 同步旋转英雄贴图指向鼠标
 */
public class HeroTurretAttackComponent extends Component {
  private final float cooldown;
  private final float bulletSpeed;
  private final float bulletLife;
  private final String bulletTexture;
  private final Camera camera;

  private float cdTimer = 0f;
  private final Vector3 tmp3 = new Vector3();
  private final Vector2 mouseWorld = new Vector2();
  private final Vector2 dir = new Vector2(); // 复用，避免频繁分配

  private int damage = 25;

  // 若你的英雄贴图默认朝“右”，设为 0；若默认朝“上”，一般设为 -90。
  private static final float SPRITE_FACING_OFFSET_DEG = -90f;

  public HeroTurretAttackComponent(float cooldown, float bulletSpeed, float bulletLife,
                                   String bulletTexture, Camera camera) {
    this.cooldown = cooldown;
    this.bulletSpeed = bulletSpeed;
    this.bulletLife = bulletLife;
    this.bulletTexture = bulletTexture;
    this.camera = camera;
  }

  @Override
  public void update() {
    if (entity == null) return;

    // 用 Gdx 的 deltaTime；若不可用再兜底 1/60
    float dt = Gdx.graphics != null ? Gdx.graphics.getDeltaTime() : (1f / 60f);

    if (cdTimer > 0f) {
      cdTimer -= dt;
    }

    // 计算朝向
    Vector2 firePos = getEntityCenter(entity);
    if (!computeAimDirection(firePos, dir)) return;

    // 设置贴图旋转（RotatingTextureRenderComponent）
    RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
    if (rot != null) {
      float angleDeg = dir.angleDeg() + SPRITE_FACING_OFFSET_DEG;
      rot.setRotation(angleDeg);
    }

    // 冷却结束才开火
    if (cdTimer <= 0f) {
      float vx = dir.x * bulletSpeed;
      float vy = dir.y * bulletSpeed;

      final Entity bullet = ProjectileFactory.createBullet(
              bulletTexture, firePos, vx, vy, bulletLife, damage
      );

      var es = ServiceLocator.getEntityService();
      if (es != null) {
        // 避免在实体遍历中修改集合
        Gdx.app.postRunnable(() -> es.register(bullet));
      } else {
        Gdx.app.error("HeroTurret", "EntityService is null; skip bullet spawn this frame");
      }

      cdTimer = cooldown;
    }
  }

  /**
   * 计算从 firePos 指向鼠标的单位向量；dir 为输出（已归一化）
   */
  private boolean computeAimDirection(Vector2 firePos, Vector2 outDir) {
    if (camera == null) return false;

    tmp3.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
    camera.unproject(tmp3);
    mouseWorld.set(tmp3.x, tmp3.y);

    outDir.set(mouseWorld).sub(firePos);
    if (outDir.isZero(0.0001f)) return false;

    outDir.nor();
    return true;
  }

  /** 获取实体中心点（若没有 getCenterPosition，就用 position+scale/2 计算） */
  private static Vector2 getEntityCenter(Entity e) {
    try {
      // 如果你的 Entity 实现了 getCenterPosition()，优先用它
      Vector2 center = e.getCenterPosition();
      if (center != null) return center;
    } catch (Throwable ignored) { /* 无该方法就走下方逻辑 */ }

    Vector2 pos = e.getPosition();
    Vector2 scale = e.getScale();
    float cx = pos.x + (scale != null ? scale.x * 0.5f : 0.5f);
    float cy = pos.y + (scale != null ? scale.y * 0.5f : 0.5f);
    return new Vector2(cx, cy);
  }

  public HeroTurretAttackComponent setDamage(int damage) {
    this.damage = damage;
    return this;
  }
}



