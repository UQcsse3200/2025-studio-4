package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.npc.EnemyAnimationController;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;


public class EnemyFactory {   
  /**
   * Creates a generic Enemy to be used as a base entity by more specific Enemy creation methods.
   *
   * @param target entity to chase
   * @param speed max speed of the created enemy
   * @return entity
   */
  public static Entity createBaseEnemy(Entity target, Vector2 speed, java.util.List<Entity> waypoints) {
    target = waypoints.get(0);
    AITaskComponent aiComponent =
        new AITaskComponent()
            .addTask(new ChaseTask(target, 1, 100f, 100f, speed));
    Entity npc =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent())
            .addComponent(new ColliderComponent().setSensor(true))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
            .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER))
            .addComponent(new com.csse3200.game.ui.DamagePopupComponent())
            .addComponent(aiComponent);

    PhysicsUtils.setScaledCollider(npc, 0.1f, 0.1f);
    npc.getEvents().addListener("entityDeath", () -> destroyEnemy(npc));

    return npc;
  }

  private static void destroyEnemy(Entity entity) {
    //Gdx.app.postRunnable(entity::dispose);
    //Eventually add point/score logic here maybe?
  }

  private EnemyFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }

public static Entity createBaseEnemyAnimated(
      Entity target,
      Vector2 speed,
      java.util.List<Entity> waypoints,
      String atlasPath,
      float walkFrameDur,
      Float idleFrameDur
  ) {
    Entity e = createBaseEnemy(target, speed, waypoints);
    //Loading the asset manager directly from the disk
    com.badlogic.gdx.files.FileHandle fh = com.badlogic.gdx.Gdx.files.internal(atlasPath);
    if (!fh.exists()) {
    throw new com.badlogic.gdx.utils.GdxRuntimeException("Atlas file not found at: " + fh.path());
    }

    com.badlogic.gdx.graphics.g2d.TextureAtlas atlas = new com.badlogic.gdx.graphics.g2d.TextureAtlas(fh);

    AnimationRenderComponent anim = new AnimationRenderComponent(atlas);

    // Build animations only if frames exist
    com.badlogic.gdx.utils.Array<com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion> walkRegions = atlas.findRegions("walk");
    com.badlogic.gdx.utils.Array<com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion> idleRegions = atlas.findRegions("idle");

    if (walkRegions.size > 0) {
      anim.addAnimation("walk", walkFrameDur, PlayMode.LOOP);
    }
    if (idleRegions.size > 0 && idleFrameDur != null) {
      anim.addAnimation("idle", idleFrameDur, PlayMode.LOOP);
    }

    e.addComponent(anim)
     .addComponent(new EnemyAnimationController());

    // Start something that exists so a current frame is available
    boolean started = false;
    if (walkRegions.size > 0) {
      anim.startAnimation("walk");
      started = true;
    } else if (idleRegions.size > 0) {
      anim.startAnimation("idle");
      started = true;
    } else {
      throw new com.badlogic.gdx.utils.GdxRuntimeException(
          "Atlas has no 'walk' or 'idle' regions: " + atlasPath);
    }

    //Entity scaling
    com.badlogic.gdx.graphics.g2d.TextureRegion base =
        (walkRegions.size > 0) ? walkRegions.first()
                              : idleRegions.first();

    float pixelsPerUnit = 1024f;

    float wUnits = base.getRegionWidth()  / pixelsPerUnit;
    float hUnits = base.getRegionHeight() / pixelsPerUnit;

    e.setScale(wUnits, hUnits);
    return e;
  }

}