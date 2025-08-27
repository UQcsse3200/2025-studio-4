package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class EnemyFactory {

      /**
   * Creates a drone enemy.
   *
   * @param target entity to chase
   * @return entity
   */
  public static Entity createDroneEnemy(Entity target) {
    // The Vector2 that createBaseEnemy takes as an input is the SPEED of the enemy //
    Entity drone = createBaseEnemy(target, new Vector2(3f, 3f));

    drone
        // For now this is where I'm setting the individual enemy stats, I'll need to modify CombatStatsComponent or make our own at a later point //
        .addComponent(new CombatStatsComponent(50, 10))
        .addComponent(new TextureRenderComponent("images/placeholder-enemy.png"));

    return drone;
  }
    
  /**
   * Creates a generic Enemy to be used as a base entity by more specific Enemy creation methods.
   *
   * @return entity
   */
  private static Entity createBaseEnemy(Entity target, Vector2 speed) {
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
    return npc;
  }

  private EnemyFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
