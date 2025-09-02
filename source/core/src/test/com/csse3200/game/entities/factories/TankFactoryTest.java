package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
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
public class TankFactoryTest {
    
    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());
        ResourceService resourceService = new ResourceService();
        ServiceLocator.registerResourceService(resourceService);
        
        // Load assets needed for PlayerFactory
        resourceService.loadTextures(new String[]{"images/box_boy_leaf.png", "images/base_enemy.png", "images/boss_enemy.png", "images/drone_enemy.png", "images/tank_enemy.png"});
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
}
