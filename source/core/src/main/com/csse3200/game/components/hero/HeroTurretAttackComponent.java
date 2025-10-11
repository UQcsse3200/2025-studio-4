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
import com.csse3200.game.services.GameTime;

/**
 * Hero turret-style attack component:
 * - Does not handle movement.
 * - Fires bullets towards the mouse cursor when cooldown expires.
 * - Rotates the hero sprite to face the mouse direction.
 * - Bullet damage is computed from CombatStatsComponent (baseAttack) at fire time.
 */
public class HeroTurretAttackComponent extends Component {
  private float cooldown;
  private float bulletSpeed;
  private float bulletLife;
  private String bulletTexture;
  private Camera camera;

  private float cdTimer = 0f;
  private final Vector3 tmp3 = new Vector3();
  private final Vector2 mouseWorld = new Vector2();
  private final Vector2 dir = new Vector2(); // Reused to avoid frequent allocation

  /** Optional: flat bonus damage and scaling multiplier for buffs/passives (default = no bonus). */
  private int flatBonusDamage = 0;     // +X damage
  private float attackScale = 1f;      // ×Y multiplier

  /** Adjust based on default sprite orientation: 0 if facing right, -90 if facing up. */
  private static final float SPRITE_FACING_OFFSET_DEG = -90f;

  public HeroTurretAttackComponent(float cooldown, float bulletSpeed, float bulletLife,
                                   String bulletTexture, Camera camera) {
    this.cooldown = cooldown;
    this.bulletSpeed = bulletSpeed;
    this.bulletLife = bulletLife;
    this.bulletTexture = bulletTexture;
    this.camera = camera;
  }
  public void setBulletTexture(String bulletTexture) {
      this.bulletTexture = bulletTexture;
  }
  public HeroTurretAttackComponent setCooldown(float s) {
      this.cooldown = s;
      return this;
  }
  public HeroTurretAttackComponent setBulletParams(float speed, float life) {
      this.bulletSpeed = speed;
      this.bulletLife = life;
      return this;
  }


    @Override
  public void create() {
    // Listen for ultimate ability multipliers (HeroUltimateComponent triggers "attack.multiplier")
    entity.getEvents().addListener("attack.multiplier", (Float mul) -> {
      if (mul == null || mul <= 0f) mul = 1f;
      this.attackScale = mul; // Multiplier directly applied in computeDamageFromStats()
    });
  }

  @Override
  public void update() {
    if (entity == null) return;

    // Use GameTime's delta time which respects time scale (paused = 0, double speed = 2x)
    GameTime gameTime = ServiceLocator.getTimeSource();
    float dt = gameTime != null ? gameTime.getDeltaTime() : 0f;
    
    if (cdTimer > 0f) {
      cdTimer -= dt;
    }

    // Compute aiming direction
    Vector2 firePos = getEntityCenter(entity);
    if (!computeAimDirection(firePos, dir)) return;

    // Rotate sprite to face aim direction
    RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
    if (rot != null && gameTime.getTimeScale() > 0f) {
      float angleDeg = dir.angleDeg() + SPRITE_FACING_OFFSET_DEG;
      rot.setRotation(angleDeg);
    }

    // Only fire if game is not paused (time scale > 0) and cooldown has expired
    if (gameTime != null && gameTime.getTimeScale() > 0f && cdTimer <= 0f) {
      float vx = dir.x * bulletSpeed;
      float vy = dir.y * bulletSpeed;

      // ⭐ Damage is always read from CombatStatsComponent at fire time
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
   * Compute damage based on combat stats:
   * baseAttack (from CombatStatsComponent) * attackScale + flatBonusDamage.
   * Rounded to nearest integer, minimum value = 1.
   */
  private int computeDamageFromStats() {
    CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
    int base = (stats != null) ? stats.getBaseAttack() : 1;
    float scaled = base * attackScale + flatBonusDamage;
    return Math.max(1, Math.round(scaled));
  }

  /**
   * Compute normalized direction vector from fire position to mouse world coordinates.
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
   * Get entity center:
   * - Prefer using getCenterPosition() if available.
   * - Otherwise, fall back to position + half-scale.
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

  // ===== Optional: chainable API for setting damage modifiers (useful for passives, items, buffs) =====

  /** Set flat bonus damage (+X). */
  public HeroTurretAttackComponent setFlatBonusDamage(int flatBonusDamage) {
    this.flatBonusDamage = flatBonusDamage;
    return this;
  }

  /** Set damage multiplier (×Y). */
  public HeroTurretAttackComponent setAttackScale(float attackScale) {
    this.attackScale = attackScale;
    return this;
  }
}

