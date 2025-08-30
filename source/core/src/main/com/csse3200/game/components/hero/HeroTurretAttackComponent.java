package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * 固定炮台式英雄攻击组件：
 * - 不处理移动
 * - 冷却结束时，朝着鼠标方向发射子弹
 */
public class HeroTurretAttackComponent extends Component {
  private final float cooldown;
  private final float bulletSpeed;
  private final float bulletLife;
  private final String bulletTexture;
  private final Camera camera;   // ✅ 直接保存相机引用

  private float cdTimer = 0f;
  private final Vector3 tmp3 = new Vector3();
  private final Vector2 mouseWorld = new Vector2();

  public HeroTurretAttackComponent(float cooldown, float bulletSpeed, float bulletLife,
                                   String bulletTexture, Camera camera) {
    this.cooldown = cooldown;
    this.bulletSpeed = bulletSpeed;
    this.bulletLife = bulletLife;
    this.bulletTexture = bulletTexture;
    this.camera = camera; // ✅ 注入相机
  }

  @Override
  public void update() {
    // ② TimeSource 兜底，避免有时未注册导致 NPE
    float dt = 1/60f;
    var ts = ServiceLocator.getTimeSource();
    if (ts != null) dt = ts.getDeltaTime();

    if (cdTimer > 0f) cdTimer -= dt;
    if (cdTimer > 0f) return;

    Vector2 firePos = entity.getCenterPosition();
    Vector2 dir = getAimDirection(firePos);
    if (dir == null) return;

    // 旋转英雄贴图（可选）
    TextureRenderComponent tex = entity.getComponent(TextureRenderComponent.class);
    if (tex != null) {
      float angleDeg = dir.angleDeg();
      tex.setRotation(angleDeg - 90f);
    }

    float vx = dir.x * bulletSpeed;
    float vy = dir.y * bulletSpeed;
    Entity bullet = ProjectileFactory.createBullet(bulletTexture, firePos, vx, vy, bulletLife);

    // ① ③ 延迟注册，避免“遍历中修改集合”；并且判空
    var es = ServiceLocator.getEntityService();
    if (es != null) {
      final Entity toAdd = bullet; // 闭包捕获需 final
      Gdx.app.postRunnable(() -> es.register(toAdd));
    } else {
      Gdx.app.error("HeroTurret", "EntityService is null; skip bullet spawn this frame");
    }

    cdTimer = cooldown;
  }

  private Vector2 getAimDirection(Vector2 firePos) {
    if (camera == null) return null;

    tmp3.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
    camera.unproject(tmp3);
    mouseWorld.set(tmp3.x, tmp3.y);

    Vector2 dir = mouseWorld.cpy().sub(firePos);
    if (dir.isZero(0.0001f)) return null;
    return dir.nor();
  }
}


