package com.csse3200.game.components.projectile;

import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Projectile component:
 * <p>
 * Handles projectile velocity and lifetime. When the lifetime expires,
 * the projectile is safely destroyed via the {@link ServiceLocator}'s
 * EntityService. Destruction is deferred to the next frame to avoid
 * concurrent modification during entity iteration.
 * </p>
 */
public class ProjectileComponent extends Component {
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

  /**
   * On creation, initialize the lifetime timer and apply velocity
   * to the physics body if available.
   */
  @Override
  public void create() {
    physics = entity.getComponent(PhysicsComponent.class);
    timer = life;

    if (physics != null && physics.getBody() != null) {
      physics.getBody().setLinearVelocity(vx, vy);
    }
  }

  /**
   * Updates the lifetime timer and schedules entity destruction
   * when the projectile expires.
   */
  @Override
  public void update() {
    float dt = ServiceLocator.getTimeSource().getDeltaTime();
    timer -= dt;

    if (timer <= 0f) {
      if (dead) return;
      dead = true;

      // 1) Stop physics (prevent collisions in the same frame)
      if (physics != null && physics.getBody() != null) {
        physics.getBody().setLinearVelocity(0, 0);
        physics.getBody().setActive(false);
      }

      // 2) Schedule destruction in the next frame to avoid concurrent modification
      com.badlogic.gdx.Gdx.app.postRunnable(() -> {
        try {
          entity.dispose(); // Dispose of components (render, physics, etc.)
        } finally {
          ServiceLocator.getEntityService().unregister(entity); // Remove from entity system
        }
      });
    }
  }
}



