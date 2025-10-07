package com.csse3200.game.components.projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Destroys the entity after colliding with a specified physics layer.
 * This is one-time only, to avoid multiple triggers in the same frame.
 */
public class DestroyOnHitComponent extends Component {
  private final short targetLayer;
  private HitboxComponent hitbox;

  /** One-time fuse: ensures destroy is only scheduled once */
  private final AtomicBoolean scheduled = new AtomicBoolean(false);

  // Add a flag to ensure only one hit per projectile
  private boolean hasHit = false;

  public DestroyOnHitComponent(short targetLayer) {
    this.targetLayer = targetLayer;
  }

  @Override
  public void create() {
    hitbox = entity.getComponent(HitboxComponent.class);
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

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

    // Schedule destruction after event dispatch, to avoid concurrent modification
    Gdx.app.postRunnable(() -> {
      entity.dispose(); // Entity.dispose() will internally unregister
    });
  }
}
