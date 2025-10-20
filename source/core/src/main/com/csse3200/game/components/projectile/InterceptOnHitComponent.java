package com.csse3200.game.components.projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;

/**
 * Overlap-based interception destroys this interceptor and any overlapping projectile.
 */
public class InterceptOnHitComponent extends Component {
  private boolean scheduledDispose = false; // prevent double-dispose
  private static final float RADIUS_SCALE = 0.5f;

  @Override
  public void update() {
    if (scheduledDispose) return;
    EntityService es = ServiceLocator.getEntityService();
    if (es == null) return;

    final Vector2 myPos = entity.getPosition();
    final float myRadius = calcRadius(entity);

    com.badlogic.gdx.utils.Array<Entity> all = es.getEntities();
    for (int i = 0, n = all.size; i < n; i++) {
      Entity other = all.get(i);
      if (other == entity) continue;
      if (other.getComponent(InterceptorTagComponent.class) != null) continue; // skip other interceptors
      if (other.getComponent(ProjectileComponent.class) == null) continue;     // only consider projectiles

      float otherRadius = calcRadius(other);
      float combined = myRadius + otherRadius;
      if (myPos.dst2(other.getPosition()) <= combined * combined) {
        destroyBoth(other);
        break; // interceptor gone, stop scanning
      }
    }
  }

  private float calcRadius(Entity e) {
    Vector2 scale = e.getScale();
    if (scale == null) return 0.25f;
    return Math.max(scale.x, scale.y) * RADIUS_SCALE;
  }

  private void destroyBoth(final Entity other) {
    if (scheduledDispose) return;
    scheduledDispose = true;
    Gdx.app.postRunnable(() -> {
      // Properly dispose of the other projectile using ProjectileComponent pooling
      try {
        ProjectileComponent otherPC = other.getComponent(ProjectileComponent.class);
        if (otherPC != null) {
          otherPC.deactivate();
          EntityService es = ServiceLocator.getEntityService();
          if (es != null) {
            es.despawnEntity(other);
          } else {
            other.dispose();
          }
        } else {
          other.dispose();
        }
      } catch (Exception ignored) {}

      // Properly dispose of this interceptor using ProjectileComponent pooling
      try {
        ProjectileComponent myPC = entity.getComponent(ProjectileComponent.class);
        if (myPC != null) {
          myPC.deactivate();
          EntityService es = ServiceLocator.getEntityService();
          if (es != null) {
            es.despawnEntity(entity);
          } else {
            entity.dispose();
          }
        } else {
          entity.dispose();
        }
      } catch (Exception ignored) {}
    });
  }
}