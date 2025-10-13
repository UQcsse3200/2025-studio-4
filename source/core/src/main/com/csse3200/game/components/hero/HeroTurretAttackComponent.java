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
 * Turret-style attack component for the hero:
 * fires bullets toward the mouse cursor on cooldown, rotates to face aim direction,
 * and computes damage from CombatStatsComponent at fire time.
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

  /**
   * @param cooldown      seconds between shots
   * @param bulletSpeed   bullet speed in world units per second
   * @param bulletLife    bullet lifetime in seconds
   * @param bulletTexture bullet sprite texture path
   * @param camera        world camera used to unproject mouse coordinates
   */
  public HeroTurretAttackComponent(float cooldown, float bulletSpeed, float bulletLife,
                                   String bulletTexture, Camera camera) {
    this.cooldown = cooldown;
    this.bulletSpeed = bulletSpeed;
    this.bulletLife = bulletLife;
    this.bulletTexture = bulletTexture;
    this.camera = camera;
  }

  /**
   * Change the bullet texture at runtime.
   * @param bulletTexture new sprite path for bullets
   */
  public void setBulletTexture(String bulletTexture) {
      this.bulletTexture = bulletTexture;
  }

  /**
   * Set the firing cooldown.
   * @param s seconds between shots
   * @return this for chaining
   */
  public HeroTurretAttackComponent setCooldown(float s) {
      this.cooldown = s;
      return this;
  }

  /**
   * Set bullet speed and lifetime.
   * @param speed speed in world units per second
   * @param life  lifetime in seconds
   * @return this for chaining
   */
  public HeroTurretAttackComponent setBulletParams(float speed, float life) {
      this.bulletSpeed = speed;
      this.bulletLife = life;
      return this;
  }

  /**
   * Subscribe to attack multiplier events used by ultimates/buffs.
   */
  @Override
  public void create() {
    // Listen for ultimate ability multipliers (HeroUltimateComponent triggers "attack.multiplier")
    entity.getEvents().addListener("attack.multiplier", (Float mul) -> {
      if (mul == null || mul <= 0f) mul = 1f;
      this.attackScale = mul; // Multiplier directly applied in computeDamageFromStats()
    });
  }

  /**
   * Rotate to face the mouse, and when off cooldown spawn a bullet with computed damage.
   * No-ops while paused (time scale == 0).
   */
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
   * Compute bullet damage from base attack and modifiers.
   * @return non-negative integer damage (min 1)
   */
  private int computeDamageFromStats() {
    CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
    int base = (stats != null) ? stats.getBaseAttack() : 1;
    float scaled = base * attackScale + flatBonusDamage;
    return Math.max(1, Math.round(scaled));
  }

  /**
   * Compute a normalized direction from the given fire position to the mouse in world space.
   * @param firePos origin point for the shot
   * @param outDir  output normalized direction
   * @return true if a valid direction was computed, false otherwise
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
   * Utility to get the entity's center if available; otherwise approximates from position/scale.
   * @param e entity
   * @return center position
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

  /**
   * Add flat bonus damage to each shot.
   * @param flatBonusDamage additive damage
   * @return this for chaining
   */
  public HeroTurretAttackComponent setFlatBonusDamage(int flatBonusDamage) {
    this.flatBonusDamage = flatBonusDamage;
    return this;
  }

  /**
   * Apply a multiplicative damage scale to base attack.
   * @param attackScale multiplier
   * @return this for chaining
   */
  public HeroTurretAttackComponent setAttackScale(float attackScale) {
    this.attackScale = attackScale;
    return this;
  }
}
