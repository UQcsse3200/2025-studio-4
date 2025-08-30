package com.csse3200.game.components.projectile;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;

public class DestroyOnHitComponent extends Component {
  private final short targetLayer;
  private HitboxComponent hitbox;

  public DestroyOnHitComponent(short targetLayer) {
    this.targetLayer = targetLayer;
  }

  @Override
  public void create() {
    hitbox = entity.getComponent(HitboxComponent.class);
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    if (hitbox == null || hitbox.getFixture() != me) return;
    if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) return;

    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
      try {
        entity.dispose();
      } finally {
        ServiceLocator.getEntityService().unregister(entity);
      }
    });
  }
}
