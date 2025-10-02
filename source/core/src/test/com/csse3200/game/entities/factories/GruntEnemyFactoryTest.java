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
public class GruntEnemyFactoryTest {
    
    @BeforeEach
    void beforeEach() {
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
    void gruntEnemyHasCorrectStats() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity grunt = GruntEnemyFactory.createGruntEnemy(waypointList, target, Difficulty.EASY);
        CombatStatsComponent stats = grunt.getComponent(CombatStatsComponent.class);
        assertNotNull(stats);
        assertEquals(50, stats.getHealth());
        assertEquals(12, stats.getBaseAttack());
        assertEquals(DamageTypeConfig.None, stats.getResistances());
        assertEquals(DamageTypeConfig.None, stats.getWeaknesses());
        assertEquals(new Vector2(0.8f, 0.8f), GruntEnemyFactory.getSpeed());
    }

    @Test
    void gruntEnemyDiesCorrectly() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity grunt = GruntEnemyFactory.createGruntEnemy(waypointList, target, Difficulty.EASY);
        CombatStatsComponent stats = grunt.getComponent(CombatStatsComponent.class);
        stats.setHealth(0);
        grunt.getEvents().trigger("entityDeath");
        assertEquals(0, stats.getHealth());
    }

    @Test
    void bossEnemyResistanceSetAndGet() {
        // Setting resistance with an expected value
        GruntEnemyFactory.setResistance(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, GruntEnemyFactory.getResistance());
        // Setting resistance to null should default to None
        GruntEnemyFactory.setResistance(null);
        assertEquals(DamageTypeConfig.None, GruntEnemyFactory.getResistance());
    }

    @Test
    void gruntEnemyWeaknessSetAndGet() {
        // Setting weakness with an expected value
        GruntEnemyFactory.setWeakness(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, GruntEnemyFactory.getWeakness());
        // Setting weakness to null should default to None
        GruntEnemyFactory.setWeakness(null);
        assertEquals(DamageTypeConfig.None, GruntEnemyFactory.getWeakness());
    }

    @Test
    void gruntEnemySpeedSetAndGet() {
        // Setting speed with first overload method and an expected value
        Vector2 sampleSpeed = new Vector2(5f, 5f);
        GruntEnemyFactory.setSpeed(sampleSpeed);
        assertEquals(sampleSpeed, GruntEnemyFactory.getSpeed());
        // Setting speed with second overload method and an expected value
        GruntEnemyFactory.setSpeed(5f, 5f);
        assertEquals(new Vector2(5f, 5f), GruntEnemyFactory.getSpeed());
        // Setting speed to null should keep the current speed
        Vector2 firstSpeed = GruntEnemyFactory.getSpeed();
        GruntEnemyFactory.setSpeed(null);
        assertEquals(firstSpeed, GruntEnemyFactory.getSpeed());
    }

    @Test
    void gruntEnemyTexturePathSetAndGet() {
        // Setting texture path with an expected value
        String sampleTexture = "images/grunt_enemy.png";
        GruntEnemyFactory.setTexturePath(sampleTexture);
        assertEquals(sampleTexture, GruntEnemyFactory.getTexturePath());
        // Setting texture path to null should default
        GruntEnemyFactory.setTexturePath(null);
        assertEquals("images/grunt_enemy.png", GruntEnemyFactory.getTexturePath());
        // Setting texture path to empty string should default
        GruntEnemyFactory.setTexturePath("");
        assertEquals("images/grunt_enemy.png", GruntEnemyFactory.getTexturePath());
        // Setting texture path to whitespace should default
        GruntEnemyFactory.setTexturePath("     ");
        assertEquals("images/grunt_enemy.png", GruntEnemyFactory.getTexturePath());
    }

    @Test
    void gruntEnemyDisplayNameSetAndGet() {
        // Setting display name with an expected value
        String displayName = "Grunt Enemy";
        GruntEnemyFactory.setDisplayName(displayName);
        assertEquals(displayName, GruntEnemyFactory.getDisplayName());
        // Setting display name to null should default
        GruntEnemyFactory.setDisplayName(null);
        assertEquals("Grunt Enemy", GruntEnemyFactory.getDisplayName());
        // Setting display name to empty string should default
        GruntEnemyFactory.setDisplayName("");
        assertEquals("Grunt Enemy", GruntEnemyFactory.getDisplayName());
        // Setting display name to whitespace should default
        GruntEnemyFactory.setDisplayName("     ");
        assertEquals("Grunt Enemy", GruntEnemyFactory.getDisplayName());
    }

    @Test
    void gruntEnemyResetsToDefaults() {
        // Change default values to something else for a grunt enemy
        GruntEnemyFactory.setResistance(DamageTypeConfig.Electricity);
        GruntEnemyFactory.setWeakness(DamageTypeConfig.Electricity);
        GruntEnemyFactory.setSpeed(new Vector2(5f, 5f));
        GruntEnemyFactory.setTexturePath("images/different_enemy.png");
        GruntEnemyFactory.setDisplayName("Different Enemy");

        // Verify that default values have been changed
        assertNotEquals(DamageTypeConfig.None, GruntEnemyFactory.getResistance());
        assertNotEquals(DamageTypeConfig.None, GruntEnemyFactory.getWeakness());
        assertNotEquals(new Vector2(0.5f, 0.5f), GruntEnemyFactory.getSpeed());
        assertNotEquals("images/grunt_enemy.png", GruntEnemyFactory.getTexturePath());
        assertNotEquals("Grunt Enemy", GruntEnemyFactory.getDisplayName());

        // Reset values to defaults
        GruntEnemyFactory.resetToDefaults();

        // Verify the values have actually been reset to default values
        assertEquals(DamageTypeConfig.None, GruntEnemyFactory.getResistance());
        assertEquals(DamageTypeConfig.None, GruntEnemyFactory.getWeakness());
        assertEquals(new Vector2(0.8f, 0.8f), GruntEnemyFactory.getSpeed());
        assertEquals("images/grunt_enemy.png", GruntEnemyFactory.getTexturePath());
        assertEquals("Grunt Enemy", GruntEnemyFactory.getDisplayName());
    }

    @Test
    void gruntEnemyHasCorrectDifficulty() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity grunt = GruntEnemyFactory.createGruntEnemy(waypointList, target, Difficulty.HARD);
        CombatStatsComponent stats = grunt.getComponent(CombatStatsComponent.class);
        assertNotNull(stats);
        assertEquals(200, stats.getHealth());
        assertEquals(48, stats.getBaseAttack());
        assertEquals(DamageTypeConfig.None, stats.getResistances());
        assertEquals(DamageTypeConfig.None, stats.getWeaknesses());
        assertEquals(new Vector2(0.8f, 0.8f), GruntEnemyFactory.getSpeed());
    }

    @Test
    void gruntEnemyDeathPoints() {
        Entity player = PlayerFactory.createPlayer();
        PlayerScoreComponent score = player.getComponent(PlayerScoreComponent.class);
        int before = score.getTotalScore(); // baseline
        java.util.List<Entity> waypoints = new java.util.ArrayList<>();
        waypoints.add(new Entity());

        // Create an enemy and simulate death
        Entity grunt = GruntEnemyFactory.createGruntEnemy(waypoints, player, Difficulty.MEDIUM);
        grunt.getEvents().trigger("entityDeath");

        // Total should have increased by the grunt’s configured points
        int expected = GruntEnemyFactory.getPoints(); // default
        assertEquals(before + expected, score.getTotalScore());
    }
}
