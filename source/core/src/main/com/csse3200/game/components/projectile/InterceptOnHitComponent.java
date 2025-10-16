package com.csse3200.game.components.projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;

/**
 * Overlap-based interception that destroys this interceptor and any overlapping tower projectile.
 * Each update scans registered entities, skipping interceptors and inactive projectiles.
 * Uses a simple radius-based approximation derived from entity scale.
 */
public class InterceptOnHitComponent extends Component {
    private boolean scheduledDispose = false; // prevent double-dispose
    private static final float RADIUS_SCALE = 0.5f;

    /**
     * Scan for overlapping non-interceptor projectiles and destroy both when found.
     * Stops after scheduling its own dispose to avoid duplicate work.
     */
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

            ProjectileComponent pcOther = other.getComponent(ProjectileComponent.class);
            if (pcOther == null || pcOther.isInactive()) continue; // only live projectiles
            if (other.getComponent(InterceptorTagComponent.class) != null) continue; // skip other interceptors

            float otherRadius = calcRadius(other);
            float combined = myRadius + otherRadius;
            if (myPos.dst2(other.getPosition()) <= combined * combined) {
                destroyBoth(other);
                break; // interceptor gone, stop scanning
            }
        }
    }

    /**
     * Derive a collision radius from an entity's scale, using RADIUS_SCALE as a multiplier.
     * @param e entity whose approximate radius is calculated
     * @return radius in world units
     */
    private float calcRadius(Entity e) {
        Vector2 scale = e.getScale();
        if (scale == null) return 0.25f;
        return Math.max(scale.x, scale.y) * RADIUS_SCALE;
    }

    /**
     * Schedule deactivation and pooling for both this interceptor and the collided projectile.
     * Uses Gdx.app.postRunnable for thread safety and guards against double-dispose.
     * @param other the other projectile entity to destroy
     */
    private void destroyBoth(final Entity other) {
        if (scheduledDispose) return;
        scheduledDispose = true;
        Gdx.app.postRunnable(() -> {
            try {
                ProjectileComponent pcO = other.getComponent(ProjectileComponent.class);
                if (pcO != null) {
                    pcO.deactivate();
                    var es = ServiceLocator.getEntityService();
                    if (es != null) es.despawnEntity(other);
                } else {
                    other.dispose();
                }
            } catch (Exception ignored) {}
            try {
                ProjectileComponent pcSelf = entity.getComponent(ProjectileComponent.class);
                if (pcSelf != null) {
                    pcSelf.deactivate();
                    var es = ServiceLocator.getEntityService();
                    if (es != null) es.despawnEntity(entity);
                } else {
                    entity.dispose();
                }
            } catch (Exception ignored) {}
        });
    }
}