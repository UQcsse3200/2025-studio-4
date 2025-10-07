package com.csse3200.game.components.movement;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.GruntEnemyFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.Difficulty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class AdjustSpeedByHealthComponentTests {

    @BeforeEach
    void BeforeEach() {
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());
        ResourceService resourceService = new ResourceService();
        ServiceLocator.registerResourceService(resourceService);

        // Load assets needed for PlayerFactory
        resourceService.loadTextures(new String[]{"images/basement.png", "images/grunt_enemy.png", "images/boss_enemy.png", "images/drone_enemy.png", "images/tank_enemy.png", "images/divider_enemy.png"});
        resourceService.loadAll();

        GruntEnemyFactory.resetToDefaults();
    }

    @Test
    void speedConstantHealthAboveThreshold() {
        // Arrange
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypoints = new java.util.ArrayList<>();
        waypoints.add(new Entity());
        Entity grunt = GruntEnemyFactory.createGruntEnemy(waypoints, target, Difficulty.EASY);
        // Run create() so dependencies are set
        grunt.create();

        // Find initial values
        CombatStatsComponent stats = grunt.getComponent(CombatStatsComponent.class);
        PhysicsMovementComponent move = grunt.getComponent(PhysicsMovementComponent.class);
        AdjustSpeedByHealthComponent adjust = grunt.getComponent(AdjustSpeedByHealthComponent.class);
        float initialSpeedX = move.maxSpeed.x;
        float initialSpeedY = move.maxSpeed.y;
        int initialHealth = stats.getHealth();

        // Set health to 60% of intial value (still above thresholds)
        stats.setHealth((int) (initialHealth * 0.6f));
        adjust.update();

        // Check speeds are unchanged
        assertEquals(initialSpeedX, move.maxSpeed.x, 0.00001);
        assertEquals(initialSpeedY, move.maxSpeed.y, 0.00001);
    }

    @Test
    void speedIncreasesHealthBelowFirstThreshold() {
        // Arrange
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypoints = new java.util.ArrayList<>();
        waypoints.add(new Entity());
        Entity grunt = GruntEnemyFactory.createGruntEnemy(waypoints, target, Difficulty.EASY);
        // Run create() so dependencies are set
        grunt.create();

        // Find initial values
        CombatStatsComponent stats = grunt.getComponent(CombatStatsComponent.class);
        PhysicsMovementComponent move = grunt.getComponent(PhysicsMovementComponent.class);
        AdjustSpeedByHealthComponent adjust = grunt.getComponent(AdjustSpeedByHealthComponent.class);
        float initialSpeedX = move.maxSpeed.x;
        float initialSpeedY = move.maxSpeed.y;
        int initialHealth = stats.getHealth();

        // Set health to 40% (below upper threshold only)
        stats.setHealth((int) (initialHealth * 0.4f));
        adjust.update();

        // Check speed has been increased to 1.4f as set
        assertEquals(1.4f, move.maxSpeed.x, 0.00001);
        assertEquals(1.4f, move.maxSpeed.y, 0.00001);
    }

    @Test
    void speedIncreasesHealthBelowSecondThreshold() {
        // Arrange
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypoints = new java.util.ArrayList<>();
        waypoints.add(new Entity());
        Entity grunt = GruntEnemyFactory.createGruntEnemy(waypoints, target, Difficulty.EASY);
        // Run create() so dependencies are set
        grunt.create();

        // Find initial values
        CombatStatsComponent stats = grunt.getComponent(CombatStatsComponent.class);
        PhysicsMovementComponent move = grunt.getComponent(PhysicsMovementComponent.class);
        AdjustSpeedByHealthComponent adjust = grunt.getComponent(AdjustSpeedByHealthComponent.class);
        int initialHealth = stats.getHealth();

        // Set health to 10% (below second threshold)
        stats.setHealth((int) (initialHealth * 0.1f));
        adjust.update();

        // Check speed has been increased to 2f as set
        assertEquals(2f, move.maxSpeed.x, 0.00001);
        assertEquals(2f, move.maxSpeed.y, 0.00001);
    }
}
