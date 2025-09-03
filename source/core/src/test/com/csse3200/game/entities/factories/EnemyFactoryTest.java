package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockConstruction;

class EnemyFactoryTest {

    @Test
    void createBaseEnemy_buildsExpectedComponents_withoutNPE() {
        Entity target = new Entity();
        Vector2 speed = new Vector2(2f, 2f);

        // 拦截在构造阶段容易触发 GDX/ServiceLocator 的组件
        try (MockedConstruction<PhysicsMovementComponent> mockMove =
                     mockConstruction(PhysicsMovementComponent.class);
             MockedConstruction<TouchAttackComponent> mockTouch =
                     mockConstruction(TouchAttackComponent.class);
             MockedConstruction<AITaskComponent> mockAI =
                     mockConstruction(AITaskComponent.class)) {

            Entity enemy = EnemyFactory.createBaseEnemy(target, speed);
            assertNotNull(enemy, "Factory should return a non-null enemy");

            // 物理/碰撞组件
            assertNotNull(enemy.getComponent(PhysicsComponent.class), "PhysicsComponent missing");
            assertNotNull(enemy.getComponent(ColliderComponent.class), "ColliderComponent missing");

            HitboxComponent hitbox = enemy.getComponent(HitboxComponent.class);
            assertNotNull(hitbox, "HitboxComponent missing");
            assertEquals(PhysicsLayer.NPC, hitbox.getLayer(), "Hitbox layer should be NPC");

            // 被拦截的“重”组件仍应存在于实体中
            assertNotNull(enemy.getComponent(PhysicsMovementComponent.class),
                    "PhysicsMovementComponent missing");
            assertNotNull(enemy.getComponent(TouchAttackComponent.class),
                    "TouchAttackComponent missing");
            assertNotNull(enemy.getComponent(AITaskComponent.class),
                    "AITaskComponent missing");

            // （可选）确认构造器确实被调用过
            assertEquals(1, mockMove.constructed().size());
            assertEquals(1, mockTouch.constructed().size());
            assertEquals(1, mockAI.constructed().size());
        }
    }
}
