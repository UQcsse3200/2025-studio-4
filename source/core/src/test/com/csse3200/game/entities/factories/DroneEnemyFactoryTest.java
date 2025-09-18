package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.PlayerScoreComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.Difficulty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class DroneEnemyFactoryTest {
    
    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());
        ResourceService resourceService = new ResourceService();
        ServiceLocator.registerResourceService(resourceService);
        
        // Load assets needed for PlayerFactory
        resourceService.loadTextures(new String[]{"images/basement.png", "images/grunt_enemy.png", "images/boss_enemy.png", "images/drone_enemy.png", "images/tank_enemy.png"});
        resourceService.loadAll();

        DroneEnemyFactory.resetToDefaults();
    }
    @Test
    void droneEnemyHasCorrectStats() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity drone = DroneEnemyFactory.createDroneEnemy(waypointList, target, Difficulty.EASY);
        CombatStatsComponent stats = drone.getComponent(CombatStatsComponent.class);
        assertNotNull(stats);
        assertEquals(50, stats.getHealth());
        assertEquals(10, stats.getBaseAttack());
        assertEquals(DamageTypeConfig.None, stats.getResistances());
        assertEquals(DamageTypeConfig.None, stats.getWeaknesses());
        assertEquals(new Vector2(1.2f, 1.2f), DroneEnemyFactory.getSpeed());
    }

    @Test
    void droneEnemyDiesCorrectly() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity drone = DroneEnemyFactory.createDroneEnemy(waypointList, target, Difficulty.EASY);
        CombatStatsComponent stats = drone.getComponent(CombatStatsComponent.class);
        stats.setHealth(0);
        drone.getEvents().trigger("entityDeath");
        assertEquals(0, stats.getHealth());
    }
    @Test
    void droneEnemyResistanceSetAndGet() {
        // Setting resistance with an expected value
        DroneEnemyFactory.setResistance(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, DroneEnemyFactory.getResistance());
        // Setting resistance to null should default to None
        DroneEnemyFactory.setResistance(null);
        assertEquals(DamageTypeConfig.None, DroneEnemyFactory.getResistance());
    }

    @Test
    void droneEnemyWeaknessSetAndGet() {
        // Setting weakness with an expected value
        DroneEnemyFactory.setWeakness(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, DroneEnemyFactory.getWeakness());
        // Setting weakness to null should default to None
        DroneEnemyFactory.setWeakness(null);
        assertEquals(DamageTypeConfig.None, DroneEnemyFactory.getWeakness());
    }

    @Test
    void droneEnemySpeedSetAndGet() {
        // Setting speed with first overload method and an expected value
        Vector2 sampleSpeed = new Vector2(5f, 5f);
        DroneEnemyFactory.setSpeed(sampleSpeed);
        assertEquals(sampleSpeed, DroneEnemyFactory.getSpeed());
        // Setting speed with second overload method and an expected value
        DroneEnemyFactory.setSpeed(5f, 5f);
        assertEquals(new Vector2(5f, 5f), DroneEnemyFactory.getSpeed());
        // Setting speed to null should keep the current speed
        Vector2 firstSpeed = DroneEnemyFactory.getSpeed();
        DroneEnemyFactory.setSpeed(null);
        assertEquals(firstSpeed, DroneEnemyFactory.getSpeed());
    }

    @Test
    void droneEnemyTexturePathSetAndGet() {
        // Setting texture path with an expected value
        String sampleTexture = "images/drone_enemy.png";
        DroneEnemyFactory.setTexturePath(sampleTexture);
        assertEquals(sampleTexture, DroneEnemyFactory.getTexturePath());
        // Setting texture path to null should default
        DroneEnemyFactory.setTexturePath(null);
        assertEquals("images/drone_enemy.png", DroneEnemyFactory.getTexturePath());
        // Setting texture path to empty string should default
        DroneEnemyFactory.setTexturePath("");
        assertEquals("images/drone_enemy.png", DroneEnemyFactory.getTexturePath());
        // Setting texture path to whitespace should default
        DroneEnemyFactory.setTexturePath("     ");
        assertEquals("images/drone_enemy.png", DroneEnemyFactory.getTexturePath());
    }

    @Test
    void droneEnemyDisplayNameSetAndGet() {
        // Setting display name with an expected value
        String displayName = "Drone Enemy";
        DroneEnemyFactory.setDisplayName(displayName);
        assertEquals(displayName, DroneEnemyFactory.getDisplayName());
        // Setting display name to null should default
        DroneEnemyFactory.setDisplayName(null);
        assertEquals("Drone Enemy", DroneEnemyFactory.getDisplayName());
        // Setting display name to empty string should default
        DroneEnemyFactory.setDisplayName("");
        assertEquals("Drone Enemy", DroneEnemyFactory.getDisplayName());
        // Setting display name to whitespace should default
        DroneEnemyFactory.setDisplayName("     ");
        assertEquals("Drone Enemy", DroneEnemyFactory.getDisplayName());
    }

    @Test
    void droneEnemyResetsToDefaults() {
        // Change default values to something else for a drone enemy
        DroneEnemyFactory.setResistance(DamageTypeConfig.Electricity);
        DroneEnemyFactory.setWeakness(DamageTypeConfig.Electricity);
        DroneEnemyFactory.setSpeed(new Vector2(5f, 5f));
        DroneEnemyFactory.setTexturePath("images/different_enemy.png");
        DroneEnemyFactory.setDisplayName("Different Enemy");

        // Verify that default values have been changed
        assertNotEquals(DamageTypeConfig.None, DroneEnemyFactory.getResistance());
        assertNotEquals(DamageTypeConfig.None, DroneEnemyFactory.getWeakness());
        assertNotEquals(new Vector2(1.2f, 1.2f), DroneEnemyFactory.getSpeed());
        assertNotEquals("images/drone_enemy.png", DroneEnemyFactory.getTexturePath());
        assertNotEquals("Drone Enemy", DroneEnemyFactory.getDisplayName());

        // Reset values to defaults
        DroneEnemyFactory.resetToDefaults();

        // Verify the values have actually been reset to default values
        assertEquals(DamageTypeConfig.None, DroneEnemyFactory.getResistance());
        assertEquals(DamageTypeConfig.None, DroneEnemyFactory.getWeakness());
        assertEquals(new Vector2(1.2f, 1.2f), DroneEnemyFactory.getSpeed());
        assertEquals("images/drone_enemy.png", DroneEnemyFactory.getTexturePath());
        assertEquals("Drone Enemy", DroneEnemyFactory.getDisplayName());
    }

    @Test
    void droneEnemyHasCorrectDifficulty() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity drone = DroneEnemyFactory.createDroneEnemy(waypointList, target, Difficulty.HARD);
        CombatStatsComponent stats = drone.getComponent(CombatStatsComponent.class);
        assertNotNull(stats);
        assertEquals(200, stats.getHealth());
        assertEquals(40, stats.getBaseAttack());
        assertEquals(DamageTypeConfig.None, stats.getResistances());
        assertEquals(DamageTypeConfig.None, stats.getWeaknesses());
        assertEquals(new Vector2(1.2f, 1.2f), DroneEnemyFactory.getSpeed());
    }

    @Test
    void testEntityDeathScoring() {
        // Setup player and waypoints
        Entity player = PlayerFactory.createPlayer();
        java.util.List<Entity> waypoints = new java.util.ArrayList<>();
        waypoints.add(new Entity());

        // Get initial score
        PlayerScoreComponent scoreComponent = player.getComponent(PlayerScoreComponent.class);
        int before = scoreComponent.getTotalScore();

        // Create an enemy and simulate death
        Entity drone = DroneEnemyFactory.createDroneEnemy(waypoints, player, Difficulty.MEDIUM);
        drone.getEvents().trigger("entityDeath");

        // Total should have increased by the drone's configured points
        int expected = DroneEnemyFactory.getPoints(); // default
        assertEquals(before + expected, scoreComponent.getTotalScore());
    }

}

