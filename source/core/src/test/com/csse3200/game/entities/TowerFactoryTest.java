package com.csse3200.game.entities;

import com.csse3200.game.components.TowerStatsComponent;
import com.csse3200.game.components.TowerComponent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TowerFactoryTest {
    @Test
    void createsBaseTowerWithComponents() {
        Entity tower = TowerFactory.createBaseTower();
        assertNotNull(tower.getComponent(TowerStatsComponent.class));
        assertNotNull(tower.getComponent(TowerComponent.class));

        TowerStatsComponent stats = tower.getComponent(TowerStatsComponent.class);
        assertEquals(10, stats.getDamage());
        assertEquals(5.0f, stats.getRange());
        assertEquals(1.0f, stats.getAttackSpeed());
    }
}