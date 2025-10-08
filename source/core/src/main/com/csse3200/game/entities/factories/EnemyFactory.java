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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.List;

/** Factory for creating enemy entities. */
public class EnemyFactory {

  /**
   * Creates a generic Enemy to be used as a base entity by more specific Enemy creation methods.
   *
   * @param target entity to chase (usually a waypoint/next target)
   * @param speed  max movement speed
   * @param waypoints ordered list of waypoint entities along the path
   * @param waypointIndex index within waypoints to start chasing from
   */
  public static Entity createBaseEnemy(Entity target, Vector2 speed, List<Entity> waypoints, int waypointIndex) {
    // Make sure we start by chasing the requested waypoint in the path
    if (waypoints != null && !waypoints.isEmpty()) {
      int idx = Math.max(0, Math.min(waypointIndex, waypoints.size() - 1));
      target = waypoints.get(idx);
    }

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

    // --- Pause / Resume hooks -------------------------------------------------
    final PhysicsComponent phys = npc.getComponent(PhysicsComponent.class);

    npc.getEvents().addListener("gamePaused", () -> {
      if (phys != null && phys.getBody() != null) {
        // Freeze physics so enemies fully stop while overlay is up
        phys.getBody().setLinearVelocity(0f, 0f);
        phys.getBody().setAngularVelocity(0f);
        phys.getBody().setActive(false);
      }
    });

    npc.getEvents().addListener("gameResumed", () -> {
      if (phys != null && phys.getBody() != null) {
        // Wake physics
        phys.getBody().setActive(true);
        phys.getBody().setAwake(true);
        phys.getBody().setLinearVelocity(0f, 0f);
        phys.getBody().setAngularVelocity(0f);

        // SNAP to nearest waypoint to eliminate steering drift after resume
        if (waypoints != null && !waypoints.isEmpty()) {
          float best = Float.MAX_VALUE;
          Vector2 bestPos = null;
          for (Entity wp : waypoints) {
            Vector2 wpPos;
            PhysicsComponent wpPhys = wp.getComponent(PhysicsComponent.class);
            if (wpPhys != null && wpPhys.getBody() != null) {
              wpPos = wpPhys.getBody().getPosition();
            } else {
              wpPos = wp.getPosition(); // fallback
            }
            float d2 = phys.getBody().getPosition().dst2(wpPos);
            if (d2 < best) {
              best = d2;
              bestPos = wpPos;
            }
          }
          if (bestPos != null) {
            phys.getBody().setTransform(bestPos.x, bestPos.y, phys.getBody().getAngle());
            phys.getBody().setLinearVelocity(0f, 0f);
            phys.getBody().setAngularVelocity(0f);
          }
        }
      }
    });
    // -------------------------------------------------------------------------

    return npc;
  }

  private static void destroyEnemy(Entity entity) {
    // Placeholder for future score/cleanup logic
  }

  private EnemyFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }

  /**
   * Creates an animated base enemy using a texture atlas.
   */
  public static Entity createBaseEnemyAnimated(
          Entity target,
          Vector2 speed,
          List<Entity> waypoints,
          String atlasPath,
          float walkFrameDur,
          Float idleFrameDur,
          int waypointIndex
  ) {
    Entity e = createBaseEnemy(target, speed, waypoints, waypointIndex);

    // Load atlas directly (consistent with existing project pattern)
    FileHandle fh = com.badlogic.gdx.Gdx.files.internal(atlasPath);
    if (!fh.exists()) {
      throw new GdxRuntimeException("Atlas file not found at: " + fh.path());
    }
    TextureAtlas atlas = new TextureAtlas(fh);

    AnimationRenderComponent anim = new AnimationRenderComponent(atlas);

    Array<TextureAtlas.AtlasRegion> walkRegions = atlas.findRegions("walk");
    Array<TextureAtlas.AtlasRegion> idleRegions = atlas.findRegions("idle");

    if (walkRegions.size > 0) {
      anim.addAnimation("walk", walkFrameDur, PlayMode.LOOP);
    }
    if (idleRegions.size > 0 && idleFrameDur != null) {
      anim.addAnimation("idle", idleFrameDur, PlayMode.LOOP);
    }

    e.addComponent(anim)
            .addComponent(new EnemyAnimationController());

    // Start something valid so there is a current frame
    if (walkRegions.size > 0) {
      anim.startAnimation("walk");
    } else if (idleRegions.size > 0) {
      anim.startAnimation("idle");
    } else {
      throw new GdxRuntimeException("Atlas has no 'walk' or 'idle' regions: " + atlasPath);
    }

    // Scale entity to atlas frame size (project uses high PPU)
    final float pixelsPerUnit = 1024f;
    final TextureAtlas.AtlasRegion base =
            (walkRegions.size > 0) ? walkRegions.first() : idleRegions.first();
    float wUnits = base.getRegionWidth() / pixelsPerUnit;
    float hUnits = base.getRegionHeight() / pixelsPerUnit;
    e.setScale(wUnits, hUnits);

    return e;
  }
}
