package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.configs.DamageTypeConfig;
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
public class TankFactoryTest {
    
    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());
        ResourceService resourceService = new ResourceService();
        ServiceLocator.registerResourceService(resourceService);
        
        // Load assets needed for PlayerFactory
        resourceService.loadTextures(new String[]{"images/box_boy_leaf.png", "images/grunt_enemy.png", "images/boss_enemy.png", "images/drone_enemy.png", "images/tank_enemy.png"});
        resourceService.loadAll();
    }
    @Test
    void tankEnemyHasCorrectStats() {
        Entity target = PlayerFactory.createPlayer();
        Entity tank = TankEnemyFactory.createTankEnemy(target);
        CombatStatsComponent stats = tank.getComponent(CombatStatsComponent.class);
        assertNotNull(stats);
        assertEquals(150, stats.getHealth());
        assertEquals(15, stats.getBaseAttack());
        assertEquals(new Vector2(0.2f, 0.2f), TankEnemyFactory.getSpeed());
    }

    @Test
    void tankEnemyDiesCorrectly() {
        Entity target = PlayerFactory.createPlayer();
        Entity tank = TankEnemyFactory.createTankEnemy(target);
        CombatStatsComponent stats = tank.getComponent(CombatStatsComponent.class);
        stats.setHealth(0);
        tank.getEvents().trigger("entityDeath");
    }

    @Test
    void tankEnemyResistanceSetAndGet() {
        // Setting resistance with an expected value
        TankEnemyFactory.setResistance(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, TankEnemyFactory.getResistance());
        // Setting resistance to null should default to None
        TankEnemyFactory.setResistance(null);
        assertEquals(DamageTypeConfig.None, TankEnemyFactory.getResistance());
    }

    @Test
    void tankEnemyWeaknessSetAndGet() {
        // Setting weakness with an expected value
        TankEnemyFactory.setWeakness(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, TankEnemyFactory.getWeakness());
        // Setting weakness to null should default to None
        TankEnemyFactory.setWeakness(null);
        assertEquals(DamageTypeConfig.None, TankEnemyFactory.getWeakness());
    }

    @Test
    void tankEnemySpeedSetAndGet() {
        // Setting speed with first overload method and an expected value
        Vector2 sampleSpeed = new Vector2(5f, 5f);
        TankEnemyFactory.setSpeed(sampleSpeed);
        assertEquals(sampleSpeed, TankEnemyFactory.getSpeed());
        // Setting speed with second overload method and an expected value
        TankEnemyFactory.setSpeed(5f, 5f);
        assertEquals(new Vector2(5f, 5f), TankEnemyFactory.getSpeed());
        // Setting speed to null should keep the current speed
        Vector2 firstSpeed = TankEnemyFactory.getSpeed();
        TankEnemyFactory.setSpeed(null);
        assertEquals(firstSpeed, TankEnemyFactory.getSpeed());
    }

    @Test
    void tankEnemyTexturePathSetAndGet() {
        // Setting texture path with an expected value
        String sampleTexture = "images/tank_enemy.png";
        TankEnemyFactory.setTexturePath(sampleTexture);
        assertEquals(sampleTexture, TankEnemyFactory.getTexturePath());
        // Setting texture path to null should default
        TankEnemyFactory.setTexturePath(null);
        assertEquals("images/tank_enemy.png", TankEnemyFactory.getTexturePath());
        // Setting texture path to empty string should default
        TankEnemyFactory.setTexturePath("");
        assertEquals("images/tank_enemy.png", TankEnemyFactory.getTexturePath());
        // Setting texture path to whitespace should default
        TankEnemyFactory.setTexturePath("     ");
        assertEquals("images/tank_enemy.png", TankEnemyFactory.getTexturePath());
    }

    @Test
    void tankEnemyDisplayNameSetAndGet() {
        // Setting display name with an expected value
        String displayName = "Tank Enemy";
        TankEnemyFactory.setDisplayName(displayName);
        assertEquals(displayName, TankEnemyFactory.getDisplayName());
        // Setting display name to null should default
        TankEnemyFactory.setDisplayName(null);
        assertEquals("Tank Enemy", TankEnemyFactory.getDisplayName());
        // Setting display name to empty string should default
        TankEnemyFactory.setDisplayName("");
        assertEquals("Tank Enemy", TankEnemyFactory.getDisplayName());
        // Setting display name to whitespace should default
        TankEnemyFactory.setDisplayName("     ");
        assertEquals("Tank Enemy", TankEnemyFactory.getDisplayName());
    }

    @Test
    void tankEnemyResetsToDefaults() {
        // Change default values to something else for a tank enemy
        TankEnemyFactory.setResistance(DamageTypeConfig.Electricity);
        TankEnemyFactory.setWeakness(DamageTypeConfig.Electricity);
        TankEnemyFactory.setSpeed(new Vector2(5f, 5f));
        TankEnemyFactory.setTexturePath("images/different_enemy.png");
        TankEnemyFactory.setDisplayName("Different Enemy");

        // Verify that default values have been changed
        assertNotEquals(DamageTypeConfig.None, TankEnemyFactory.getResistance());
        assertNotEquals(DamageTypeConfig.None, TankEnemyFactory.getWeakness());
        assertNotEquals(new Vector2(0.2f, 0.2f), TankEnemyFactory.getSpeed());
        assertNotEquals("images/tank_enemy.png", TankEnemyFactory.getTexturePath());
        assertNotEquals("Tank Enemy", TankEnemyFactory.getDisplayName());

        // Reset values to defaults
        TankEnemyFactory.resetToDefaults();

        // Verify the values have actually been reset to default values
        assertEquals(DamageTypeConfig.None, TankEnemyFactory.getResistance());
        assertEquals(DamageTypeConfig.None, TankEnemyFactory.getWeakness());
        assertEquals(new Vector2(0.2f, 0.2f), TankEnemyFactory.getSpeed());
        assertEquals("images/tank_enemy.png", TankEnemyFactory.getTexturePath());
        assertEquals("Tank Enemy", TankEnemyFactory.getDisplayName());
    }
}
