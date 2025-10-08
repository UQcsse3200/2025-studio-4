package com.csse3200.game.components.movement;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.services.ServiceLocator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(GameExtension.class)
public class AdjustSpeedByHealthComponentTests {

    /**
     * Creates a minimal test entity mocking a grunt enemy.
     */
    private Entity createMinimalTestEntity() {
        Entity entity = new Entity();
        entity.addComponent(new PhysicsComponent());
        PhysicsMovementComponent moveComponent = new PhysicsMovementComponent();
        moveComponent.maxSpeed = new Vector2((float) 0.8, (float) 0.8);
        entity.addComponent(moveComponent);

        // Match grunt enemy values
        entity.addComponent(new CombatStatsComponent(
                50,
                12,
                DamageTypeConfig.None,
                DamageTypeConfig.None
        ));

        // Match the grunt enemy values
        entity.addComponent(new AdjustSpeedByHealthComponent()
                .addThreshold(0.25f, 2.0f)   // Health <= 25% --> speed x 2.0
                .addThreshold(0.5f, 1.4f)    // Health <= 50% --> speed x 1.4
        );

        // Initialize all components
        entity.create();
        return entity;
    }

    @BeforeEach
    void setUp() {
        ServiceLocator.registerPhysicsService(new PhysicsService());
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void speedConstantWhenHealthAboveThreshold() {
        Entity testEntity = createMinimalTestEntity();

        CombatStatsComponent stats = testEntity.getComponent(CombatStatsComponent.class);
        PhysicsMovementComponent move = testEntity.getComponent(PhysicsMovementComponent.class);
        AdjustSpeedByHealthComponent adjust = testEntity.getComponent(AdjustSpeedByHealthComponent.class);

        float initialSpeed = move.maxSpeed.x;

        // Health > 50% --> speed constant
        stats.setHealth(50 * 60 / 100);
        adjust.update();

        assertEquals(initialSpeed, move.maxSpeed.x, 0.00001);
        assertEquals(initialSpeed, move.maxSpeed.y, 0.00001);
    }

    @Test
    void speedIncreasesWhenHealthEqualsFirstThreshold() {
        Entity testEntity = createMinimalTestEntity();

        CombatStatsComponent stats = testEntity.getComponent(CombatStatsComponent.class);
        PhysicsMovementComponent move = testEntity.getComponent(PhysicsMovementComponent.class);
        AdjustSpeedByHealthComponent adjust = testEntity.getComponent(AdjustSpeedByHealthComponent.class);

        // Health == 50% --> speed x 1.4
        stats.setHealth(50 * 50 / 100);
        adjust.update();

        assertEquals(1.4f, move.maxSpeed.x, 0.00001);
        assertEquals(1.4f, move.maxSpeed.y, 0.00001);
    }

    @Test
    void speedIncreasesWhenHealthBelowFirstThreshold() {
        Entity testEntity = createMinimalTestEntity();

        CombatStatsComponent stats = testEntity.getComponent(CombatStatsComponent.class);
        PhysicsMovementComponent move = testEntity.getComponent(PhysicsMovementComponent.class);
        AdjustSpeedByHealthComponent adjust = testEntity.getComponent(AdjustSpeedByHealthComponent.class);

        // 50% > Health > 25% --> speed x 1.4
        stats.setHealth(50 * 40 / 100);
        adjust.update();

        assertEquals(1.4f, move.maxSpeed.x, 0.00001);
        assertEquals(1.4f, move.maxSpeed.y, 0.00001);
    }

    @Test
    void speedIncreasesWhenHealthEqualsSecondThreshold() {
        Entity testEntity = createMinimalTestEntity();

        CombatStatsComponent stats = testEntity.getComponent(CombatStatsComponent.class);
        PhysicsMovementComponent move = testEntity.getComponent(PhysicsMovementComponent.class);
        AdjustSpeedByHealthComponent adjust = testEntity.getComponent(AdjustSpeedByHealthComponent.class);

        // Health == 25% --> speed x 2.0
        stats.setHealth(50 * 25 / 100);
        adjust.update();

        assertEquals(2.0f, move.maxSpeed.x, 0.00001);
        assertEquals(2.0f, move.maxSpeed.y, 0.00001);
    }

    @Test
    void speedIncreasesWhenHealthBelowSecondThreshold() {
        Entity testEntity = createMinimalTestEntity();

        CombatStatsComponent stats = testEntity.getComponent(CombatStatsComponent.class);
        PhysicsMovementComponent move = testEntity.getComponent(PhysicsMovementComponent.class);
        AdjustSpeedByHealthComponent adjust = testEntity.getComponent(AdjustSpeedByHealthComponent.class);

        // Health < 25% --> speed x 2.0
        stats.setHealth(50 * 10 / 100);
        adjust.update();

        assertEquals(2.0f, move.maxSpeed.x, 0.00001);
        assertEquals(2.0f, move.maxSpeed.y, 0.00001);
    }
}