package com.csse3200.game.components.projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Destroys (despawns) the entity after the first collision with a specified physics layer.
 * This is pooling-friendly: calls ProjectileComponent.deactivate() and then EntityService.despawnEntity().
 * A one-time fuse prevents multiple triggers in the same frame.
 */
public class DestroyOnHitComponent extends Component {
    private final short targetLayer;
    private HitboxComponent hitbox;

    /** One-time fuse: ensures destroy is only scheduled once */
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    // Add a flag to ensure only one hit per projectile
    private boolean hasHit = false;

    /**
     * @param targetLayer physics layer mask that the other fixture must match to trigger destruction
     */
    public DestroyOnHitComponent(short targetLayer) {
        this.targetLayer = targetLayer;
    }

    /**
     * Subscribe to collision events and reset one-shot flags when the projectile is reactivated.
     */
    @Override
    public void create() {
        hitbox = entity.getComponent(HitboxComponent.class);
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);

        // Reset per-shot state when the projectile is reactivated from the pool
        entity.getEvents().addListener("projectile.activated", () -> {
            hasHit = false;
            scheduled.set(false);
        });
    }

    /**
     * Handle collision start for this entity's hitbox. If the other fixture matches the target layer,
     * mark hit, schedule a single despawn via Gdx.app.postRunnable, and return the entity to the pool.
     * @param me    this entity's fixture
     * @param other the other entity's fixture
     */
    private void onCollisionStart(Fixture me, Fixture other) {
        // Only handle contacts involving this entity's hitbox
        if (hitbox == null || hitbox.getFixture() != me) return;

        // Only allow one hit per projectile
        if (hasHit) return;

        // Process only if the other fixture matches the target layer
        if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) return;

        // Mark as hit so future collisions are ignored
        hasHit = true;

        // Allow entry only once
        if (!scheduled.compareAndSet(false, true)) return;

        // Prefer pooling over dispose when available
        Gdx.app.postRunnable(() -> {
            try {
                ProjectileComponent pc = entity.getComponent(ProjectileComponent.class);
                if (pc != null) {
                    pc.deactivate();
                    var es = ServiceLocator.getEntityService();
                    if (es != null) {
                        es.despawnEntity(entity); // uses poolKey if set
                        return;
                    }
                }
            } catch (Exception ignored) { }
            // Fallback
            try { entity.dispose(); } catch (Exception ignored) {}
        });
    }
}
