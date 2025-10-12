package com.csse3200.game.components.projectile;

import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Projectile component:
 * <p>
 * Handles projectile velocity and lifetime. When the lifetime expires,
 * the projectile is safely disposed (which will also unregister it from
 * the EntityService via Entity#dispose()).
 * Destruction is deferred to the next frame to avoid concurrent modification.
 * </p>
 */
public class ProjectileComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(ProjectileComponent.class);
  /** Flag to prevent double-destruction */
  private boolean dead = false;

  /** Initial velocity (units/second) */
  private final float vx, vy;

  /** Lifetime of the projectile in seconds */
  private final float life;

  /** Remaining time before expiration */
  private float timer;

  /** Cached physics component for motion and collision */
  private PhysicsComponent physics;

  /**
   * @param vx   horizontal velocity (units/second)
   * @param vy   vertical velocity (units/second)
   * @param life lifetime in seconds
   */
  public ProjectileComponent(float vx, float vy, float life) {
    this.vx = vx;
    this.vy = vy;
    this.life = life;
  }

  /** Apply initial velocity on creation. */
  @Override
  public void create() {
    physics = entity.getComponent(PhysicsComponent.class);
    timer = life;

    if (physics != null && physics.getBody() != null) {
      float setVx = vx;
      float setVy = vy;
      if (!isFinite(setVx) || !isFinite(setVy)) {
        logger.warn("ProjectileComponent.create(): invalid vx/vy {}, {}; clamping to 0.", vx, vy);
        setVx = 0f;
        setVy = 0f;
      }
      physics.getBody().setLinearVelocity(setVx, setVy);

      physics.getBody().setBullet(true);
    }
  }

  /** Tick lifetime; when expired, stop physics and dispose next frame. */
  @Override
  public void update() {
    float dt = ServiceLocator.getTimeSource().getDeltaTime();
    timer -= dt;

    if (timer <= 0f) {
      if (dead) return;
      dead = true;

      // 1) Stop physics immediately to prevent further collisions this frame
      if (physics != null && physics.getBody() != null) {
        physics.getBody().setLinearVelocity(0, 0);
        physics.getBody().setActive(false);
      }

      // 2) Defer disposal to next frame (Entity.dispose() will unregister internally)
      com.badlogic.gdx.Gdx.app.postRunnable(() -> {
        try {
          entity.dispose(); // Will call ServiceLocator.getEntityService().unregister(this)
        } catch (Exception ignored) {
          // Swallow to avoid crashing render thread; entity will be cleaned up by service.
        }
      });
    }
  }

  private static boolean isFinite(float v) {
    return !Float.isNaN(v) && !Float.isInfinite(v);
  }
}
