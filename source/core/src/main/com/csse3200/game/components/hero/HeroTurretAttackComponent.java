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
  private final Camera camera;

  private float cdTimer = 0f;
  private final Vector3 tmp3 = new Vector3();
  private final Vector2 mouseWorld = new Vector2();
  private final Vector2 dir = new Vector2(); // 复用，避免频繁分配

  private int damage = 25;

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
    // 用 TimeSource（兜底 1/60）
    float dt = 1f / 60f;
    var ts = ServiceLocator.getTimeSource();
    if (ts != null) dt = ts.getDeltaTime();

    if (cdTimer > 0f) {
      cdTimer -= dt;
      return;
    }

    // 计算朝向
    Vector2 firePos = entity.getCenterPosition();
    if (!computeAimDirection(firePos, dir)) return;

    // 可选：旋转英雄贴图使其朝向鼠标
    TextureRenderComponent tex = entity.getComponent(TextureRenderComponent.class);
    if (tex != null) {
      tex.setRotation(dir.angleDeg() - 90f);
    }

    float vx = dir.x * bulletSpeed;
    float vy = dir.y * bulletSpeed;
    final Entity bullet = ProjectileFactory.createBullet(bulletTexture, firePos, vx, vy, bulletLife, damage);

    // ✅ 只注册一次：使用 postRunnable，避免“遍历中修改集合”
    var es = ServiceLocator.getEntityService();
    if (es != null) {
      Gdx.app.postRunnable(() -> es.register(bullet));
    } else {
      Gdx.app.error("HeroTurret", "EntityService is null; skip bullet spawn this frame");
    }

    cdTimer = cooldown;
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

  // 需要时提供 setter
  public HeroTurretAttackComponent setDamage(int damage) {
    this.damage = damage;
    return this;
  }
}


