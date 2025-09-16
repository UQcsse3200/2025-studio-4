package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class BossEnemyFactoryTest {
    
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
    }

    @Test
    void bossEnemyHasCorrectStats() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity boss = BossEnemyFactory.createBossEnemy(waypointList, target);
        CombatStatsComponent stats = boss.getComponent(CombatStatsComponent.class);
        assertNotNull(stats);
        assertEquals(200, stats.getHealth());
        assertEquals(20, stats.getBaseAttack());
        assertEquals(DamageTypeConfig.None, stats.getResistances());
        assertEquals(DamageTypeConfig.None, stats.getWeaknesses());
        assertEquals(new Vector2(0.7f, 0.7f), BossEnemyFactory.getSpeed());
    }

    @Test
    void bossEnemyDiesCorrectly() {
        Entity target = PlayerFactory.createPlayer();
        java.util.List<Entity> waypointList = new java.util.ArrayList<>();
        Entity waypoint = new Entity();
        waypointList.add(waypoint);
        Entity boss = BossEnemyFactory.createBossEnemy(waypointList, target);
        CombatStatsComponent stats = boss.getComponent(CombatStatsComponent.class);
        stats.setHealth(0);
        boss.getEvents().trigger("entityDeath");
        assertEquals(0, stats.getHealth());
    }

    @Test
    void bossEnemyResistanceSetAndGet() {
        // Setting resistance with an expected value
        BossEnemyFactory.setResistance(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, BossEnemyFactory.getResistance());
        // Setting resistance to null should default to None
        BossEnemyFactory.setResistance(null);
        assertEquals(DamageTypeConfig.None, BossEnemyFactory.getResistance());
    }

    @Test
    void bossEnemyWeaknessSetAndGet() {
        // Setting weakness with an expected value
        BossEnemyFactory.setWeakness(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, BossEnemyFactory.getWeakness());
        // Setting weakness to null should default to None
        BossEnemyFactory.setWeakness(null);
        assertEquals(DamageTypeConfig.None, BossEnemyFactory.getWeakness());
    }

    @Test
    void bossEnemySpeedSetAndGet() {
        // Setting speed with first overload method and an expected value
        Vector2 sampleSpeed = new Vector2(5f, 5f);
        BossEnemyFactory.setSpeed(sampleSpeed);
        assertEquals(sampleSpeed, BossEnemyFactory.getSpeed());
        // Setting speed with second overload method and an expected value
        BossEnemyFactory.setSpeed(5f, 5f);
        assertEquals(new Vector2(5f, 5f), BossEnemyFactory.getSpeed());
        // Setting speed to null should keep the current speed
        Vector2 firstSpeed = BossEnemyFactory.getSpeed();
        BossEnemyFactory.setSpeed(null);
        assertEquals(firstSpeed, BossEnemyFactory.getSpeed());
    }

    @Test
    void bossEnemyTexturePathSetAndGet() {
        // Setting texture path with an expected value
        String sampleTexture = "images/boss_enemy.png";
        BossEnemyFactory.setTexturePath(sampleTexture);
        assertEquals(sampleTexture, BossEnemyFactory.getTexturePath());
        // Setting texture path to null should default
        BossEnemyFactory.setTexturePath(null);
        assertEquals("images/boss_enemy.png", BossEnemyFactory.getTexturePath());
        // Setting texture path to empty string should default
        BossEnemyFactory.setTexturePath("");
        assertEquals("images/boss_enemy.png", BossEnemyFactory.getTexturePath());
        // Setting texture path to whitespace should default
        BossEnemyFactory.setTexturePath("     ");
        assertEquals("images/boss_enemy.png", BossEnemyFactory.getTexturePath());
    }

    @Test
    void bossEnemyDisplayNameSetAndGet() {
        // Setting display name with an expected value
        String displayName = "Boss Enemy";
        BossEnemyFactory.setDisplayName(displayName);
        assertEquals(displayName, BossEnemyFactory.getDisplayName());
        // Setting display name to null should default
        BossEnemyFactory.setDisplayName(null);
        assertEquals("Boss Enemy", BossEnemyFactory.getDisplayName());
        // Setting display name to empty string should default
        BossEnemyFactory.setDisplayName("");
        assertEquals("Boss Enemy", BossEnemyFactory.getDisplayName());
        // Setting display name to whitespace should default
        BossEnemyFactory.setDisplayName("     ");
        assertEquals("Boss Enemy", BossEnemyFactory.getDisplayName());
    }

    @Test
    void bossEnemyResetsToDefaults() {
        // Change default values to something else for a boss enemy
        BossEnemyFactory.setResistance(DamageTypeConfig.Electricity);
        BossEnemyFactory.setWeakness(DamageTypeConfig.Electricity);
        BossEnemyFactory.setSpeed(new Vector2(5f, 5f));
        BossEnemyFactory.setTexturePath("images/different_enemy.png");
        BossEnemyFactory.setDisplayName("Different Enemy");

        // Verify that default values have been changed
        assertNotEquals(DamageTypeConfig.None, BossEnemyFactory.getResistance());
        assertNotEquals(DamageTypeConfig.None, BossEnemyFactory.getWeakness());
        assertNotEquals(new Vector2(0.7f, 0.7f), BossEnemyFactory.getSpeed());
        assertNotEquals("images/boss_enemy.png", BossEnemyFactory.getTexturePath());
        assertNotEquals("Boss Enemy", BossEnemyFactory.getDisplayName());

        // Reset values to defaults
        BossEnemyFactory.resetToDefaults();

        // Verify the values have actually been reset to default values
        assertEquals(DamageTypeConfig.None, BossEnemyFactory.getResistance());
        assertEquals(DamageTypeConfig.None, BossEnemyFactory.getWeakness());
        assertEquals(new Vector2(0.7f, 0.7f), BossEnemyFactory.getSpeed());
        assertEquals("images/boss_enemy.png", BossEnemyFactory.getTexturePath());
        assertEquals("Boss Enemy", BossEnemyFactory.getDisplayName());
    }

}
