package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.TowerStatsComponent;
import com.csse3200.game.components.TowerComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TowerComponentTest {
    private Entity tower;
    private Entity target;
    private EntityService entityService;

    // Minimal test double for entity storage
    static class TestEntityService extends EntityService {
        private final Array<Entity> entities = new Array<>();
        @Override
        public Array<Entity> getEntitiesCopy() {
            return new Array<>(entities);
        }
        @Override
        public void register(Entity entity) {
            entities.add(entity);
        }
        @Override
        public void unregister(Entity entity) {
            entities.removeValue(entity, true);
        }
    }

    @BeforeEach
    void setUp() {
        entityService = new TestEntityService();
        ServiceLocator.registerEntityService(entityService);

        tower = new Entity()
                .addComponent(new TowerStatsComponent(100, 10, 5f, 0f))
                .addComponent(new TowerComponent());
        tower.setPosition(0, 0);
        entityService.register(tower);

        target = new Entity()
                .addComponent(new CombatStatsComponent(20, 0));
        target.setPosition(3, 4); // distance = 5
        entityService.register(target);
    }

    @Test
    void towerAttacksEntityInRange() {
        tower.getComponent(TowerComponent.class).update();
        CombatStatsComponent stats = target.getComponent(CombatStatsComponent.class);
        assertEquals(10, stats.getHealth());
    }

    @Test
    void towerDoesNotAttackEntityOutOfRange() {
        target.setPosition(10, 10); // distance > 5
        tower.getComponent(TowerComponent.class).update();
        CombatStatsComponent stats = target.getComponent(CombatStatsComponent.class);
        assertEquals(20, stats.getHealth());
    }

    @Test
    void towerDoesNotAttackDeadEntity() {
        CombatStatsComponent stats = target.getComponent(CombatStatsComponent.class);
        stats.setHealth(0);
        tower.getComponent(TowerComponent.class).update();
        assertEquals(0, stats.getHealth());
    }

    @Test
    void towerRespectsAttackCooldown() {
        TowerStatsComponent stats = tower.getComponent(TowerStatsComponent.class);
        stats.resetAttackTimer();
        stats.setHealth(100);
        stats.updateAttackTimer(0f); // timer = 0
        stats.setAttackCooldown(1f); // set cooldown to 1 second

        // Should not attack yet
        tower.getComponent(TowerComponent.class).update();
        assertEquals(20, target.getComponent(CombatStatsComponent.class).getHealth());

        // Simulate time passing
        stats.updateAttackTimer(1f);
        tower.getComponent(TowerComponent.class).update();
        assertEquals(10, target.getComponent(CombatStatsComponent.class).getHealth());
    }
}
