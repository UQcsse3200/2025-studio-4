package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TowerComponentTest {

    @Test
    void canCreateTowerComponent() {
        TowerComponent tower = new TowerComponent("bone");
        assertNotNull(tower);
        assertEquals("bone", tower.getType());
    }

    @Test
    void canAttachTowerComponentToEntity() {
        Entity entity = new Entity().addComponent(new TowerComponent("bone"));
        assertNotNull(entity.getComponent(TowerComponent.class));
    }
}
