package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.EnemyMeleeEngageComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.enemy.EnemyHealthTintComponent;
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
                    .addComponent(new EnemyMeleeEngageComponent(
                            PhysicsLayer.ALLY, /*fallbackDmg*/ 3, /*tick*/ 0.3f,
                            /*push*/ 0f, /*pauseWhileEngaged*/ false))
                    .addComponent(aiComponent);

    PhysicsUtils.setScaledCollider(npc, 0.1f, 0.1f);
    npc.getEvents().addListener("entityDeath", () -> destroyEnemy(npc));
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

    // Prefer ResourceService-managed assets; fallback to direct file loading
    com.csse3200.game.services.ResourceService rs =
            com.csse3200.game.services.ServiceLocator.getResourceService();
    TextureAtlas atlas = null;
    if (rs != null) {
      atlas = rs.getAsset(atlasPath, TextureAtlas.class);
    }
    if (atlas == null) {
      FileHandle fh = com.badlogic.gdx.Gdx.files.internal(atlasPath);
      if (!fh.exists()) {
        throw new GdxRuntimeException("Atlas file not found at: " + fh.path());
      }
      atlas = new TextureAtlas(fh);
    }

    AnimationRenderComponent anim = new AnimationRenderComponent(atlas);

    Array<TextureAtlas.AtlasRegion> walkRegions = atlas.findRegions("walk");
    Array<TextureAtlas.AtlasRegion> idleRegions = atlas.findRegions("idle");

    //Gdx.app.log("ATLAS", atlasPath + "  attack regions=" + atlas.findRegions("attack").size +
                     //"  death regions=" + atlas.findRegions("death").size);

    if (walkRegions.size > 0) {
        anim.addAnimation("walk", walkFrameDur, PlayMode.LOOP);
    }
    if (idleRegions.size > 0 && idleFrameDur != null) {
        anim.addAnimation("idle", idleFrameDur, PlayMode.LOOP);
    }

    float attackDur = EnemyAnimationController.ATTACK_FRAME_DUR;
    float deathDur  = EnemyAnimationController.DEATH_FRAME_DUR;

    if (atlas.findRegions("attack").size > 0)
        anim.addAnimation("attack", attackDur, PlayMode.NORMAL);
    if (atlas.findRegions("death").size > 0)
        anim.addAnimation("death", deathDur, PlayMode.NORMAL);

    e.addComponent(anim)
            .addComponent(new EnemyAnimationController())
            .addComponent(new EnemyHealthTintComponent());

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
