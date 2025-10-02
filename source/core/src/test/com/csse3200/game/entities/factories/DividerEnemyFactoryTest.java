package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
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
public class DividerEnemyFactoryTest {
    private GameArea area;

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

        // Anonymous sublcass to instantiate a GameArea using in divider creation
        GameArea area = new GameArea() { @Override public void create() {} };

        DividerEnemyFactory.resetToDefaults();
    }

    @Test
    void dividerEnemyHasCorrectStats() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity divider = DividerEnemyFactory.createDividerEnemy(waypointList, area, target, Difficulty.EASY);
        CombatStatsComponent stats = divider.getComponent(CombatStatsComponent.class);
        assertNotNull(stats);
        assertEquals(150, stats.getHealth());
        assertEquals(5, stats.getBaseAttack());
        assertEquals(DamageTypeConfig.None, stats.getResistances());
        assertEquals(DamageTypeConfig.None, stats.getWeaknesses());
        assertEquals(new Vector2(0.75f, 0.75f), DividerEnemyFactory.getSpeed());
    }

    @Test
    void dividerEnemyDiesCorrectly() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity divider = DividerEnemyFactory.createDividerEnemy(waypointList, area, target, Difficulty.EASY);
        CombatStatsComponent stats = divider.getComponent(CombatStatsComponent.class);
        stats.setHealth(0);
        divider.getEvents().trigger("entityDeath");
        assertEquals(0, stats.getHealth());
    }

    @Test
    void dividerEnemyResistanceSetAndGet() {
        // Setting resistance with an expected value
        DividerEnemyFactory.setResistance(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, DividerEnemyFactory.getResistance());
        // Setting resistance to null should default to None
        DividerEnemyFactory.setResistance(null);
        assertEquals(DamageTypeConfig.None, DividerEnemyFactory.getResistance());
    }

    @Test
    void dividerEnemyWeaknessSetAndGet() {
        // Setting weakness with an expected value
        DividerEnemyFactory.setWeakness(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, DividerEnemyFactory.getWeakness());
        // Setting weakness to null should default to None
        DividerEnemyFactory.setWeakness(null);
        assertEquals(DamageTypeConfig.None, DividerEnemyFactory.getWeakness());
    }

    @Test
    void dividerEnemySpeedSetAndGet() {
        // Setting speed with first overload method and an expected value
        Vector2 sampleSpeed = new Vector2(5f, 5f);
        DividerEnemyFactory.setSpeed(sampleSpeed);
        assertEquals(sampleSpeed, DividerEnemyFactory.getSpeed());
        // Setting speed with second overload method and an expected value
        DividerEnemyFactory.setSpeed(5f, 5f);
        assertEquals(new Vector2(5f, 5f), DividerEnemyFactory.getSpeed());
        // Setting speed to null should keep the current speed
        Vector2 firstSpeed = DividerEnemyFactory.getSpeed();
        DividerEnemyFactory.setSpeed(null);
        assertEquals(firstSpeed, DividerEnemyFactory.getSpeed());
    }

    @Test
    void dividerEnemyTexturePathSetAndGet() {
        // Setting texture path with an expected value
        String sampleTexture = "images/divider_enemy.png";
        DividerEnemyFactory.setTexturePath(sampleTexture);
        assertEquals(sampleTexture, DividerEnemyFactory.getTexturePath());
        // Setting texture path to null should default
        DividerEnemyFactory.setTexturePath(null);
        assertEquals("images/divider_enemy.png", DividerEnemyFactory.getTexturePath());
        // Setting texture path to empty string should default
        DividerEnemyFactory.setTexturePath("");
        assertEquals("images/divider_enemy.png", DividerEnemyFactory.getTexturePath());
        // Setting texture path to whitespace should default
        DividerEnemyFactory.setTexturePath("     ");
        assertEquals("images/divider_enemy.png", DividerEnemyFactory.getTexturePath());
    }

    @Test
    void dividerEnemyDisplayNameSetAndGet() {
        // Setting display name with an expected value
        String displayName = "Divider Enemy";
        DividerEnemyFactory.setDisplayName(displayName);
        assertEquals(displayName, DividerEnemyFactory.getDisplayName());
        // Setting display name to null should default
        DividerEnemyFactory.setDisplayName(null);
        assertEquals("Divider Enemy", DividerEnemyFactory.getDisplayName());
        // Setting display name to empty string should default
        DividerEnemyFactory.setDisplayName("");
        assertEquals("Divider Enemy", DividerEnemyFactory.getDisplayName());
        // Setting display name to whitespace should default
        DividerEnemyFactory.setDisplayName("     ");
        assertEquals("Divider Enemy", DividerEnemyFactory.getDisplayName());
    }

    @Test
    void dividerEnemyResetsToDefaults() {
        // Change default values to something else for a divider enemy
        DividerEnemyFactory.setResistance(DamageTypeConfig.Electricity);
        DividerEnemyFactory.setWeakness(DamageTypeConfig.Electricity);
        DividerEnemyFactory.setSpeed(new Vector2(5f, 5f));
        DividerEnemyFactory.setTexturePath("images/different_enemy.png");
        DividerEnemyFactory.setDisplayName("Different Enemy");

        // Verify that default values have been changed
        assertNotEquals(DamageTypeConfig.None, DividerEnemyFactory.getResistance());
        assertNotEquals(DamageTypeConfig.None, DividerEnemyFactory.getWeakness());
        assertNotEquals(new Vector2(0.5f, 0.5f), DividerEnemyFactory.getSpeed());
        assertNotEquals("images/divider_enemy.png", DividerEnemyFactory.getTexturePath());
        assertNotEquals("Divider Enemy", DividerEnemyFactory.getDisplayName());

        // Reset values to defaults
        DividerEnemyFactory.resetToDefaults();

        // Verify the values have actually been reset to default values
        assertEquals(DamageTypeConfig.None, DividerEnemyFactory.getResistance());
        assertEquals(DamageTypeConfig.None, DividerEnemyFactory.getWeakness());
        assertEquals(new Vector2(0.75f, 0.75f), DividerEnemyFactory.getSpeed());
        assertEquals("images/divider_enemy.png", DividerEnemyFactory.getTexturePath());
        assertEquals("Divider Enemy", DividerEnemyFactory.getDisplayName());
    }

    @Test
    void dividerEnemyHasCorrectDifficulty() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity divider = DividerEnemyFactory.createDividerEnemy(waypointList, area, target, Difficulty.HARD);
        CombatStatsComponent stats = divider.getComponent(CombatStatsComponent.class);
        assertNotNull(stats);
        assertEquals(600, stats.getHealth());
        assertEquals(20, stats.getBaseAttack());
        assertEquals(DamageTypeConfig.None, stats.getResistances());
        assertEquals(DamageTypeConfig.None, stats.getWeaknesses());
        assertEquals(new Vector2(0.75f, 0.75f), DividerEnemyFactory.getSpeed());
    }

    @Test
    void dividerEnemyDeathPoints() {
        Entity player = PlayerFactory.createPlayer();
        PlayerScoreComponent score = player.getComponent(PlayerScoreComponent.class);
        int before = score.getTotalScore(); // baseline
        java.util.List<Entity> waypoints = new java.util.ArrayList<>();
        waypoints.add(new Entity());

        // Create an enemy and simulate death
        Entity divider = DividerEnemyFactory.createDividerEnemy(waypoints, area, player, Difficulty.MEDIUM);
        divider.getEvents().trigger("entityDeath");

        // Total should have increased by the divider’s configured points
        int expected = DividerEnemyFactory.getPoints(); // default
        assertEquals(before + expected, score.getTotalScore());
    }

}
