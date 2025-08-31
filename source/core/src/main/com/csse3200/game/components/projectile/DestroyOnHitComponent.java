package com.csse3200.game.components.projectile;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component that destroys its entity when colliding with a specified target layer.
 * <p>
 * Typical usage: attach this to a bullet entity so that it disappears
 * immediately after hitting an enemy. This component does not directly apply
 * damage; it only handles the safe destruction of the projectile.
 * </p>
 */
public class DestroyOnHitComponent extends Component {
  /** Physics layer that this projectile should be destroyed on collision with */
  private final short targetLayer;

  /** Reference to the entity's hitbox */
  private HitboxComponent hitbox;

  /**
   * @param targetLayer the physics layer which triggers destruction (e.g., ENEMY/NPC layer)
   */
  public DestroyOnHitComponent(short targetLayer) {
    this.targetLayer = targetLayer;
  }

  /**
   * On creation, cache the hitbox and register a listener
   * for the "collisionStart" event.
   */
  @Override
  public void create() {
    hitbox = entity.getComponent(HitboxComponent.class);
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  /**
   * Collision callback: destroys the entity if this hitbox collides
   * with a fixture on the target layer.
   *
   * @param me    the fixture belonging to this entity
   * @param other the fixture belonging to the other entity
   */
  private void onCollisionStart(Fixture me, Fixture other) {
    if (hitbox == null || hitbox.getFixture() != me) return;
    if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) return;

    // Defer destruction until next frame to avoid concurrent modification
    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
      try {
        entity.dispose(); // free resources (rendering, physics, etc.)
      } finally {
        ServiceLocator.getEntityService().unregister(entity); // remove from entity system
      }
    });
  }
}

