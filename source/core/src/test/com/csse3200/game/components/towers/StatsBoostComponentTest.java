package com.csse3200.game.components.towers;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatsBoostComponentTest {

    @AfterEach
    void tearDown() {
        // Clear test entities hook so Timer won't be suppressed in non-test runs
        StatsBoostComponent.setTestEntities(null);
    }

    @Test
    void removesBoostWhenLeavingRange() {
        List<Entity> entities = new ArrayList<>();

        Entity totem = new Entity();
        TowerStatsComponent totemStats = new TowerStatsComponent(
                100, 0f, 8f, 1f, 0f, 1f, "projectiles/bullet.png", 1, 1
        );
        totem.addComponent(totemStats);
        totem.addComponent(new TowerComponent("totem", 1, 1));
        StatsBoostComponent boost = new StatsBoostComponent();
        totem.addComponent(boost);

        Entity tower = new Entity();
        TowerStatsComponent towerStats = new TowerStatsComponent(
                100, 10f, 5f, 2f, 50f, 1f, "projectiles/bullet.png", 1, 1
        );
        tower.addComponent(towerStats);
        tower.addComponent(new TowerComponent("bone", 1, 1));

        totem.setPosition(0, 0);
        tower.setPosition(4, 0); // in range
        entities.add(totem);
        entities.add(tower);
        StatsBoostComponent.setTestEntities(entities);

        // Apply
        boost.update();
        // Leave range
        tower.setPosition(20, 0);
        boost.update();

        // Reverted to base
        assertEquals(10f, towerStats.getDamage(), 1e-4);
        assertEquals(2f, towerStats.getAttackCooldown(), 1e-4);
    }
}
