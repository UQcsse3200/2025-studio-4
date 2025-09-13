package com.csse3200.game.entities;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.entities.factories.DroneEnemyFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
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
public class WeaknessAndResistanceTest {
    
    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());
        ResourceService resourceService = new ResourceService();
        ServiceLocator.registerResourceService(resourceService);
        
        // Load assets needed for PlayerFactory
        resourceService.loadTextures(new String[]{"images/base_enemy.png", "images/boss_enemy.png", "images/drone_enemy.png", "images/tank_enemy.png"});
        resourceService.loadAll();
    }
    @Test
    void canChangeResistances() {
        Entity target = PlayerFactory.createPlayer();
        Entity testEnemy = DroneEnemyFactory.createDroneEnemy(target);
        CombatStatsComponent stats = testEnemy.getComponent(CombatStatsComponent.class);
        assertEquals(DamageTypeConfig.None, stats.getResistances());
        stats.setResistances(DamageTypeConfig.Fire);
        assertEquals(DamageTypeConfig.Fire, stats.getResistances());
    }

    @Test
    void canChangeWeaknesses() {
        Entity target = PlayerFactory.createPlayer();
        Entity testEnemy = DroneEnemyFactory.createDroneEnemy(target);
        CombatStatsComponent stats = testEnemy.getComponent(CombatStatsComponent.class);
        assertEquals(DamageTypeConfig.None, stats.getWeaknesses());
        stats.setWeaknesses(DamageTypeConfig.Electricity);
        assertEquals(DamageTypeConfig.Electricity, stats.getWeaknesses());
    }

    @Test
    void doesWeaknessIncreaseDamage() {
        Entity target = PlayerFactory.createPlayer();
        Entity testEnemy = DroneEnemyFactory.createDroneEnemy(target);
        CombatStatsComponent stats = testEnemy.getComponent(CombatStatsComponent.class);
        stats.setWeaknesses(DamageTypeConfig.Fire);
        stats.addHealth(-10, DamageTypeConfig.Fire);
        assertEquals(30, stats.getHealth());
    }

    @Test
    void doesResistanceDecreaseDamage() {
        Entity target = PlayerFactory.createPlayer();
        Entity testEnemy = DroneEnemyFactory.createDroneEnemy(target);
        CombatStatsComponent stats = testEnemy.getComponent(CombatStatsComponent.class);
        stats.setResistances(DamageTypeConfig.Electricity);
        stats.addHealth(-10, DamageTypeConfig.Electricity);
        assertEquals(45, stats.getHealth());
    }
}