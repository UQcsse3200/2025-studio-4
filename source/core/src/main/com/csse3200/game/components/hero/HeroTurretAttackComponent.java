package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Hero turret-style attack component:
 * - Does not handle movement.
 * - Fires bullets towards the mouse cursor when cooldown expires.
 * - Rotates the hero sprite to face the mouse direction.
 * - Bullet damage is computed from CombatStatsComponent (baseAttack) at fire time.
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
  private final Vector2 dir = new Vector2(); // Reused to avoid frequent allocation

  /** 可选：伤害加成与倍率，方便做被动/BUFF（默认无加成） */
  private int flatBonusDamage = 0;     // +X
  private float attackScale = 1f;      // ×Y

  // 如果英雄贴图默认朝右，设 0；若默认朝上，通常设 -90
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

    float dt = (Gdx.graphics != null) ? Gdx.graphics.getDeltaTime() : (1f / 60f);
    if (cdTimer > 0f) {
      cdTimer -= dt;
    }

    // 计算朝向
    Vector2 firePos = getEntityCenter(entity);
    if (!computeAimDirection(firePos, dir)) return;

    // 旋转贴图
    RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
    if (rot != null) {
      float angleDeg = dir.angleDeg() + SPRITE_FACING_OFFSET_DEG;
      rot.setRotation(angleDeg);
    }

    // 冷却结束→开火
    if (cdTimer <= 0f) {
      float vx = dir.x * bulletSpeed;
      float vy = dir.y * bulletSpeed;

      // ⭐ 关键：每次开火实时从 CombatStatsComponent 读取攻击力
      int dmg = computeDamageFromStats();

      final Entity bullet = ProjectileFactory.createBullet(
              bulletTexture, firePos, vx, vy, bulletLife, dmg
      );

      var es = ServiceLocator.getEntityService();
      if (es != null) {
        Gdx.app.postRunnable(() -> es.register(bullet));
      } else {
        Gdx.app.error("HeroTurret", "EntityService is null; skip bullet spawn this frame");
      }

      cdTimer = cooldown;
    }
  }

  /**
   * 由火力属性计算本次伤害：
   * baseAttack（来自 CombatStatsComponent） * attackScale + flatBonusDamage
   * 最终向最近整数取整，且不低于 1。
   */
  private int computeDamageFromStats() {
    CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
    int base = (stats != null) ? stats.getBaseAttack() : 1;
    float scaled = base * attackScale + flatBonusDamage;
    return Math.max(1, Math.round(scaled));
  }

  /**
   * 计算从发射点到鼠标世界坐标的单位向量
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

  /**
   * 获取实体中心点：优先调用 getCenterPosition()，否则用位置+半尺寸兜底
   */
  private static Vector2 getEntityCenter(Entity e) {
    try {
      Vector2 center = e.getCenterPosition();
      if (center != null) return center;
    } catch (Throwable ignored) { }
    Vector2 pos = e.getPosition();
    Vector2 scale = e.getScale();
    float cx = pos.x + (scale != null ? scale.x * 0.5f : 0.5f);
    float cy = pos.y + (scale != null ? scale.y * 0.5f : 0.5f);
    return new Vector2(cx, cy);
  }

  // ===== 可选：暴露加成配置的链式 API（以后做被动/装备/药水很方便） =====

  /** 设定固定伤害加成（+X） */
  public HeroTurretAttackComponent setFlatBonusDamage(int flatBonusDamage) {
    this.flatBonusDamage = flatBonusDamage;
    return this;
  }

  /** 设定伤害倍率（×Y） */
  public HeroTurretAttackComponent setAttackScale(float attackScale) {
    this.attackScale = attackScale;
    return this;
  }
}
