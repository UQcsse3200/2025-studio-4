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
public class DividerChildEnemyFactoryTest {
    int waypointIndex = 0;

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

        DividerChildEnemyFactory.resetToDefaults();
    }

    @Test
    void dividerChildEnemyHasCorrectStats() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity dividerChild = DividerChildEnemyFactory.createDividerChildEnemy(target, waypointList, waypointIndex, Difficulty.EASY);
        CombatStatsComponent stats = dividerChild.getComponent(CombatStatsComponent.class);
        assertNotNull(stats);
        assertEquals(50, stats.getHealth());
        assertEquals(20, stats.getBaseAttack());
        assertEquals(DamageTypeConfig.None, stats.getResistances());
        assertEquals(DamageTypeConfig.None, stats.getWeaknesses());
        assertEquals(new Vector2(2f, 2f), DividerChildEnemyFactory.getSpeed());
    }

    @Test
    void dividerChildEnemyDiesCorrectly() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity dividerChild = DividerChildEnemyFactory.createDividerChildEnemy(target, waypointList, waypointIndex, Difficulty.EASY);
        CombatStatsComponent stats = dividerChild.getComponent(CombatStatsComponent.class);
        stats.setHealth(0);
        dividerChild.getEvents().trigger("entityDeath");
        assertEquals(0, stats.getHealth());
    }

    @Test
    void dividerChildEnemyResistanceSetAndGet() {
        // Setting resistance with an expected value
        DividerChildEnemyFactory.setResistance(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, DividerChildEnemyFactory.getResistance());
        // Setting resistance to null should default to None
        DividerChildEnemyFactory.setResistance(null);
        assertEquals(DamageTypeConfig.None, DividerChildEnemyFactory.getResistance());
    }

    @Test
    void dividerChildEnemyWeaknessSetAndGet() {
        // Setting weakness with an expected value
        DividerChildEnemyFactory.setWeakness(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, DividerChildEnemyFactory.getWeakness());
        // Setting weakness to null should default to None
        DividerChildEnemyFactory.setWeakness(null);
        assertEquals(DamageTypeConfig.None, DividerChildEnemyFactory.getWeakness());
    }

    @Test
    void dividerChildEnemySpeedSetAndGet() {
        // Setting speed with first overload method and an expected value
        Vector2 sampleSpeed = new Vector2(5f, 5f);
        DividerChildEnemyFactory.setSpeed(sampleSpeed);
        assertEquals(sampleSpeed, DividerChildEnemyFactory.getSpeed());
        // Setting speed with second overload method and an expected value
        DividerChildEnemyFactory.setSpeed(5f, 5f);
        assertEquals(new Vector2(5f, 5f), DividerChildEnemyFactory.getSpeed());
        // Setting speed to null should keep the current speed
        Vector2 firstSpeed = DividerChildEnemyFactory.getSpeed();
        DividerChildEnemyFactory.setSpeed(null);
        assertEquals(firstSpeed, DividerChildEnemyFactory.getSpeed());
    }

    @Test
    void dividerChildEnemyTexturePathSetAndGet() {
        // Setting texture path with an expected value
        String sampleTexture = "images/divider_enemy.png";
        DividerChildEnemyFactory.setTexturePath(sampleTexture);
        assertEquals(sampleTexture, DividerChildEnemyFactory.getTexturePath());
        // Setting texture path to null should default
        DividerChildEnemyFactory.setTexturePath(null);
        assertEquals("images/divider_enemy.png", DividerChildEnemyFactory.getTexturePath());
        // Setting texture path to empty string should default
        DividerChildEnemyFactory.setTexturePath("");
        assertEquals("images/divider_enemy.png", DividerChildEnemyFactory.getTexturePath());
        // Setting texture path to whitespace should default
        DividerChildEnemyFactory.setTexturePath("     ");
        assertEquals("images/divider_enemy.png", DividerChildEnemyFactory.getTexturePath());
    }

    @Test
    void dividerChildEnemyDisplayNameSetAndGet() {
        // Setting display name with an expected value
        String displayName = "Divider Child Enemy";
        DividerChildEnemyFactory.setDisplayName(displayName);
        assertEquals(displayName, DividerChildEnemyFactory.getDisplayName());
        // Setting display name to null should default
        DividerChildEnemyFactory.setDisplayName(null);
        assertEquals("Divider Child Enemy", DividerChildEnemyFactory.getDisplayName());
        // Setting display name to empty string should default
        DividerChildEnemyFactory.setDisplayName("");
        assertEquals("Divider Child Enemy", DividerChildEnemyFactory.getDisplayName());
        // Setting display name to whitespace should default
        DividerChildEnemyFactory.setDisplayName("     ");
        assertEquals("Divider Child Enemy", DividerChildEnemyFactory.getDisplayName());
    }

    @Test
    void dividerChildEnemyResetsToDefaults() {
        // Change default values to something else for a divider enemy
        DividerChildEnemyFactory.setResistance(DamageTypeConfig.Electricity);
        DividerChildEnemyFactory.setWeakness(DamageTypeConfig.Electricity);
        DividerChildEnemyFactory.setSpeed(new Vector2(5f, 5f));
        DividerChildEnemyFactory.setTexturePath("images/different_enemy.png");
        DividerChildEnemyFactory.setDisplayName("Different Enemy");

        // Verify that default values have been changed
        assertNotEquals(DamageTypeConfig.None, DividerChildEnemyFactory.getResistance());
        assertNotEquals(DamageTypeConfig.None, DividerChildEnemyFactory.getWeakness());
        assertNotEquals(new Vector2(2f, 2f), DividerChildEnemyFactory.getSpeed());
        assertNotEquals("images/divider_enemy.png", DividerChildEnemyFactory.getTexturePath());
        assertNotEquals("Divider Child Enemy", DividerChildEnemyFactory.getDisplayName());

        // Reset values to defaults
        DividerChildEnemyFactory.resetToDefaults();

        // Verify the values have actually been reset to default values
        assertEquals(DamageTypeConfig.None, DividerChildEnemyFactory.getResistance());
        assertEquals(DamageTypeConfig.None, DividerChildEnemyFactory.getWeakness());
        assertEquals(new Vector2(2f, 2f), DividerChildEnemyFactory.getSpeed());
        assertEquals("images/divider_enemy.png", DividerChildEnemyFactory.getTexturePath());
        assertEquals("Divider Child Enemy", DividerChildEnemyFactory.getDisplayName());
    }

    @Test
    void dividerChildEnemyHasCorrectDifficulty() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity dividerChild = DividerChildEnemyFactory.createDividerChildEnemy(target, waypointList, waypointIndex, Difficulty.HARD);
        CombatStatsComponent stats = dividerChild.getComponent(CombatStatsComponent.class);
        assertNotNull(stats);
        assertEquals(50, stats.getHealth());
        assertEquals(20, stats.getBaseAttack());
        assertEquals(DamageTypeConfig.None, stats.getResistances());
        assertEquals(DamageTypeConfig.None, stats.getWeaknesses());
        assertEquals(new Vector2(2f, 2f), DividerChildEnemyFactory.getSpeed());
    }

    @Test
    void dividerChildEnemyDeathPoints() {
        Entity player = PlayerFactory.createPlayer();
        PlayerScoreComponent score = player.getComponent(PlayerScoreComponent.class);
        int before = score.getTotalScore(); // baseline
        java.util.List<Entity> waypoints = new java.util.ArrayList<>();
        waypoints.add(new Entity());

        // Create an enemy and simulate death
        Entity dividerChild = DividerChildEnemyFactory.createDividerChildEnemy(player, waypoints, waypointIndex, Difficulty.MEDIUM);
        dividerChild.getEvents().trigger("entityDeath");

        // Total should have increased by the divider child’s configured points
        int expected = DividerChildEnemyFactory.getPoints(); // default
        assertEquals(before + expected, score.getTotalScore());
    }

}
