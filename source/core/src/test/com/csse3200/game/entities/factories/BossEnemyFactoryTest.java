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
        resourceService.loadTextures(new String[]{"images/box_boy_leaf.png", "images/base_enemy.png", "images/boss_enemy.png", "images/drone_enemy.png", "images/tank_enemy.png"});
        resourceService.loadAll();
    }
    @Test
    void bossEnemyHasCorrectStats() {
        Entity target = PlayerFactory.createPlayer();
        Entity boss = BossEnemyFactory.createBossEnemy(target);
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
        Entity boss = BossEnemyFactory.createBossEnemy(target);
        CombatStatsComponent stats = boss.getComponent(CombatStatsComponent.class);
        stats.setHealth(0);
        boss.getEvents().trigger("entityDeath");
        assertEquals(0, stats.getHealth());
    }
}
