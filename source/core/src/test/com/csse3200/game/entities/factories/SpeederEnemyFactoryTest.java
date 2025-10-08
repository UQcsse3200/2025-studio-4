package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.PlayerScoreComponent;
import com.csse3200.game.components.movement.AccelerateOverTimeComponent;
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
public class SpeederEnemyFactoryTest {

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());
        ResourceService resourceService = new ResourceService();
        ServiceLocator.registerResourceService(resourceService);

        // Load assets needed for PlayerFactory and enemy creation
        resourceService.loadTextures(new String[]{"images/basement.png", "images/grunt_enemy.png", "images/boss_enemy.png", "images/drone_enemy.png", "images/tank_enemy.png", "images/divider_enemy.png"});
        resourceService.loadTextureAtlases(new String[]{"images/boss_basic_spritesheet.atlas"});
        resourceService.loadAll();

        SpeederEnemyFactory.resetToDefaults();
    }

    @Test
    void speederEnemyHasCorrectStats() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity speeder = SpeederEnemyFactory.createSpeederEnemy(waypointList, target, Difficulty.EASY);
        CombatStatsComponent stats = speeder.getComponent(CombatStatsComponent.class);
        assertNotNull(stats);
        assertEquals(150, stats.getHealth());
        assertEquals(45, stats.getBaseAttack());
        assertEquals(DamageTypeConfig.None, stats.getResistances());
        assertEquals(DamageTypeConfig.Electricity, stats.getWeaknesses());
        assertEquals(new Vector2(0.15f, 0.15f), SpeederEnemyFactory.getSpeed());
    }

    @Test
    void speederEnemyHasAccelerationComponent() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity speeder = SpeederEnemyFactory.createSpeederEnemy(waypointList, target, Difficulty.EASY);
        AccelerateOverTimeComponent accel = speeder.getComponent(AccelerateOverTimeComponent.class);
        assertNotNull(accel, "Speeder enemy should have AccelerateOverTimeComponent");
        // Verify initial speed is set correctly by checking getCurrentSpeed at creation
        assertEquals(0.15f, accel.getCurrentSpeed(), 0.001f);
    }

    @Test
    void speederEnemyDiesCorrectly() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity speeder = SpeederEnemyFactory.createSpeederEnemy(waypointList, target, Difficulty.EASY);
        CombatStatsComponent stats = speeder.getComponent(CombatStatsComponent.class);
        stats.setHealth(0);
        speeder.getEvents().trigger("entityDeath");
        assertEquals(0, stats.getHealth());
    }

    @Test
    void speederEnemyResistanceSetAndGet() {
        // Setting resistance with an expected value
        SpeederEnemyFactory.setResistance(DamageTypeConfig.Fire);
        assertEquals(DamageTypeConfig.Fire, SpeederEnemyFactory.getResistance());
        // Setting resistance to null should default to None
        SpeederEnemyFactory.setResistance(null);
        assertEquals(DamageTypeConfig.None, SpeederEnemyFactory.getResistance());
    }

    @Test
    void speederEnemyWeaknessSetAndGet() {
        // Setting weakness with an expected value
        SpeederEnemyFactory.setWeakness(DamageTypeConfig.Fire);
        assertEquals(DamageTypeConfig.Fire, SpeederEnemyFactory.getWeakness());
        // Setting weakness to null should default to Electricity
        SpeederEnemyFactory.setWeakness(null);
        assertEquals(DamageTypeConfig.Electricity, SpeederEnemyFactory.getWeakness());
    }

    @Test
    void speederEnemySpeedSetAndGet() {
        // Setting speed with first overload method and an expected value
        Vector2 sampleSpeed = new Vector2(5f, 5f);
        SpeederEnemyFactory.setSpeed(sampleSpeed);
        assertEquals(sampleSpeed, SpeederEnemyFactory.getSpeed());
        // Setting speed with second overload method and an expected value
        SpeederEnemyFactory.setSpeed(5f, 5f);
        assertEquals(new Vector2(5f, 5f), SpeederEnemyFactory.getSpeed());
        // Setting speed to null should keep the current speed
        Vector2 firstSpeed = SpeederEnemyFactory.getSpeed();
        SpeederEnemyFactory.setSpeed(null);
        assertEquals(firstSpeed, SpeederEnemyFactory.getSpeed());
    }

    @Test
    void speederEnemyMaxSpeedSetAndGet() {
        // Setting max speed with an expected value
        SpeederEnemyFactory.setMaxSpeed(5.0f);
        assertEquals(5.0f, SpeederEnemyFactory.getMaxSpeed());
        // Setting max speed to 0 or negative should default
        SpeederEnemyFactory.setMaxSpeed(0f);
        assertEquals(2.5f, SpeederEnemyFactory.getMaxSpeed());
        SpeederEnemyFactory.setMaxSpeed(-1f);
        assertEquals(2.5f, SpeederEnemyFactory.getMaxSpeed());
    }

    @Test
    void speederEnemyAccelerationRateSetAndGet() {
        // Setting acceleration rate with an expected value
        SpeederEnemyFactory.setAccelerationRate(0.1f);
        assertEquals(0.1f, SpeederEnemyFactory.getAccelerationRate());
        // Setting acceleration rate to 0 or negative should default
        SpeederEnemyFactory.setAccelerationRate(0f);
        assertEquals(0.06f, SpeederEnemyFactory.getAccelerationRate());
        SpeederEnemyFactory.setAccelerationRate(-0.05f);
        assertEquals(0.06f, SpeederEnemyFactory.getAccelerationRate());
    }

    @Test
    void speederEnemyTexturePathSetAndGet() {
        // Setting texture path with an expected value
        String sampleTexture = "images/boss_enemy.png";
        SpeederEnemyFactory.setTexturePath(sampleTexture);
        assertEquals(sampleTexture, SpeederEnemyFactory.getTexturePath());
        // Setting texture path to null should default
        SpeederEnemyFactory.setTexturePath(null);
        assertEquals("images/boss_enemy.png", SpeederEnemyFactory.getTexturePath());
        // Setting texture path to empty string should default
        SpeederEnemyFactory.setTexturePath("");
        assertEquals("images/boss_enemy.png", SpeederEnemyFactory.getTexturePath());
        // Setting texture path to whitespace should default
        SpeederEnemyFactory.setTexturePath("     ");
        assertEquals("images/boss_enemy.png", SpeederEnemyFactory.getTexturePath());
    }

    @Test
    void speederEnemyDisplayNameSetAndGet() {
        // Setting display name with an expected value
        String displayName = "Speeder Enemy";
        SpeederEnemyFactory.setDisplayName(displayName);
        assertEquals(displayName, SpeederEnemyFactory.getDisplayName());
        // Setting display name to null should default
        SpeederEnemyFactory.setDisplayName(null);
        assertEquals("Speeder Enemy", SpeederEnemyFactory.getDisplayName());
        // Setting display name to empty string should default
        SpeederEnemyFactory.setDisplayName("");
        assertEquals("Speeder Enemy", SpeederEnemyFactory.getDisplayName());
        // Setting display name to whitespace should default
        SpeederEnemyFactory.setDisplayName("     ");
        assertEquals("Speeder Enemy", SpeederEnemyFactory.getDisplayName());
    }

    @Test
    void speederEnemyResetsToDefaults() {
        // Change default values to something else for a speeder enemy
        SpeederEnemyFactory.setResistance(DamageTypeConfig.Fire);
        SpeederEnemyFactory.setWeakness(DamageTypeConfig.Fire);
        SpeederEnemyFactory.setSpeed(new Vector2(5f, 5f));
        SpeederEnemyFactory.setMaxSpeed(10f);
        SpeederEnemyFactory.setAccelerationRate(0.5f);
        SpeederEnemyFactory.setTexturePath("images/different_enemy.png");
        SpeederEnemyFactory.setDisplayName("Different Enemy");

        // Verify that default values have been changed
        assertNotEquals(DamageTypeConfig.None, SpeederEnemyFactory.getResistance());
        assertNotEquals(DamageTypeConfig.Electricity, SpeederEnemyFactory.getWeakness());
        assertNotEquals(new Vector2(0.15f, 0.15f), SpeederEnemyFactory.getSpeed());
        assertNotEquals(2.5f, SpeederEnemyFactory.getMaxSpeed());
        assertNotEquals(0.06f, SpeederEnemyFactory.getAccelerationRate());
        assertNotEquals("images/boss_enemy.png", SpeederEnemyFactory.getTexturePath());
        assertNotEquals("Speeder Enemy", SpeederEnemyFactory.getDisplayName());

        // Reset values to defaults
        SpeederEnemyFactory.resetToDefaults();

        // Verify the values have actually been reset to default values
        assertEquals(DamageTypeConfig.None, SpeederEnemyFactory.getResistance());
        assertEquals(DamageTypeConfig.Electricity, SpeederEnemyFactory.getWeakness());
        assertEquals(new Vector2(0.15f, 0.15f), SpeederEnemyFactory.getSpeed());
        assertEquals(2.5f, SpeederEnemyFactory.getMaxSpeed());
        assertEquals(0.06f, SpeederEnemyFactory.getAccelerationRate());
        assertEquals("images/boss_enemy.png", SpeederEnemyFactory.getTexturePath());
        assertEquals("Speeder Enemy", SpeederEnemyFactory.getDisplayName());
    }

    @Test
    void speederEnemyHasCorrectDifficulty() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity speeder = SpeederEnemyFactory.createSpeederEnemy(waypointList, target, Difficulty.HARD);
        CombatStatsComponent stats = speeder.getComponent(CombatStatsComponent.class);
        assertNotNull(stats);
        assertEquals(600, stats.getHealth()); // 150 * 4 (HARD multiplier)
        assertEquals(180, stats.getBaseAttack()); // 45 * 4 (HARD multiplier)
        assertEquals(DamageTypeConfig.None, stats.getResistances());
        assertEquals(DamageTypeConfig.Electricity, stats.getWeaknesses());
        assertEquals(new Vector2(0.15f, 0.15f), SpeederEnemyFactory.getSpeed());
    }

    @Test
    void speederEnemyDeathPoints() {
        Entity player = PlayerFactory.createPlayer();
        PlayerScoreComponent score = player.getComponent(PlayerScoreComponent.class);
        int before = score.getTotalScore(); // baseline
        java.util.List<Entity> waypoints = new java.util.ArrayList<>();
        waypoints.add(new Entity());

        // Create an enemy and simulate death
        Entity speeder = SpeederEnemyFactory.createSpeederEnemy(waypoints, player, Difficulty.MEDIUM);
        speeder.getEvents().trigger("entityDeath");

        // Total should have increased by the speeder's configured points
        int expected = SpeederEnemyFactory.getPoints(); // default 400
        assertEquals(before + expected, score.getTotalScore());
    }

    @Test
    void speederEnemyHealthSetAndGet() {
        // Setting health with an expected value
        SpeederEnemyFactory.setHealth(200);
        assertEquals(200, SpeederEnemyFactory.getHealth());
        // Setting health to 0 or negative should default
        SpeederEnemyFactory.setHealth(0);
        assertEquals(150, SpeederEnemyFactory.getHealth());
        SpeederEnemyFactory.setHealth(-50);
        assertEquals(150, SpeederEnemyFactory.getHealth());
    }

    @Test
    void speederEnemyDamageSetAndGet() {
        // Setting damage with an expected value
        SpeederEnemyFactory.setDamage(100);
        assertEquals(100, SpeederEnemyFactory.getDamage());
        // Setting damage to 0 or negative should default
        SpeederEnemyFactory.setDamage(0);
        assertEquals(45, SpeederEnemyFactory.getDamage());
        SpeederEnemyFactory.setDamage(-20);
        assertEquals(45, SpeederEnemyFactory.getDamage());
    }
}
