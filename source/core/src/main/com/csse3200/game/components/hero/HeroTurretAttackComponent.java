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
 * Hero turret-style attack component:
 * - Does not handle movement.
 * - Fires bullets towards the mouse cursor when cooldown expires.
 * - Rotates the hero sprite to face the mouse direction.
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

  private int damage = 25;

  // If the hero sprite faces "right" by default, keep as 0.
  // If it faces "up" by default, usually set to -90.
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

    // Use Gdx deltaTime; fallback to 1/60 if unavailable
    float dt = Gdx.graphics != null ? Gdx.graphics.getDeltaTime() : (1f / 60f);

    if (cdTimer > 0f) {
      cdTimer -= dt;
    }

    // Calculate aiming direction
    Vector2 firePos = getEntityCenter(entity);
    if (!computeAimDirection(firePos, dir)) return;

    // Rotate sprite towards aim direction (RotatingTextureRenderComponent)
    RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
    if (rot != null) {
      float angleDeg = dir.angleDeg() + SPRITE_FACING_OFFSET_DEG;
      rot.setRotation(angleDeg);
    }

    // Only fire when cooldown has finished
    if (cdTimer <= 0f) {
      float vx = dir.x * bulletSpeed;
      float vy = dir.y * bulletSpeed;

      final Entity bullet = ProjectileFactory.createBullet(
              bulletTexture, firePos, vx, vy, bulletLife, damage
      );

      var es = ServiceLocator.getEntityService();
      if (es != null) {
        // Avoid modifying entity collection during iteration
        Gdx.app.postRunnable(() -> es.register(bullet));
      } else {
        Gdx.app.error("HeroTurret", "EntityService is null; skip bullet spawn this frame");
      }

      cdTimer = cooldown;
    }
  }

  /**
   * Compute a normalized direction vector from fire position to the mouse cursor.
   * @param firePos The starting position of the bullet.
   * @param outDir  The output vector (will be normalized).
   * @return true if direction is valid, false if cursor overlaps firePos.
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
   * Get the center position of an entity.
   * If the entity implements getCenterPosition(), use that.
   * Otherwise, fallback to position + scale/2.
   */
  private static Vector2 getEntityCenter(Entity e) {
    try {
      Vector2 center = e.getCenterPosition();
      if (center != null) return center;
    } catch (Throwable ignored) { /* fallback if method not available */ }

    Vector2 pos = e.getPosition();
    Vector2 scale = e.getScale();
    float cx = pos.x + (scale != null ? scale.x * 0.5f : 0.5f);
    float cy = pos.y + (scale != null ? scale.y * 0.5f : 0.5f);
    return new Vector2(cx, cy);
  }

  /**
   * Set the damage value of bullets fired by this component.
   * @param damage Damage per bullet
   * @return this (for chaining)
   */
  public HeroTurretAttackComponent setDamage(int damage) {
    this.damage = damage;
    return this;
  }
}




