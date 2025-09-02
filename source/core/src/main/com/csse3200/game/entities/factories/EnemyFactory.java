package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;

public class EnemyFactory {   
  /**
   * Creates a generic Enemy to be used as a base entity by more specific Enemy creation methods.
   *
   * @param target entity to chase
   * @param speed max speed of the created enemy
   * @return entity
   */
  public static Entity createBaseEnemy(Entity target, Vector2 speed) {
    AITaskComponent aiComponent =
        new AITaskComponent()
            .addTask(new ChaseTask(target, 10, 100f, 100f, speed));
    Entity npc =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
            .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f))
            .addComponent(aiComponent);

    PhysicsUtils.setScaledCollider(npc, 0.9f, 0.4f);
    npc.getEvents().addListener("entityDeath", () -> destroyEnemy(npc));

    return npc;
  }
  
  private static void destroyEnemy(Entity entity) {
    Gdx.app.postRunnable(entity::dispose);
    //Eventually add point/score logic here maybe?
  }

  private EnemyFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}