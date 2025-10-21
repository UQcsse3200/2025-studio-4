package com.csse3200.game.components.towers;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StatsBoostComponentTest {

    @AfterEach
    void tearDown() {
        // Clear test entities hook
        StatsBoostComponent.setTestEntities(null);
    }

    @Test
    void testBoosterRemovedWhenOutOfRange() {
        Entity totem = new Entity();
        TowerStatsComponent totemStats = new TowerStatsComponent(
                100, 0f, 2f, 1f, 0f, 1f, "projectiles/bullet.png", 1, 1
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

        // Place tower out of range
        totem.setPosition(0, 0);
        tower.setPosition(100, 100);

        // No entity service; also no test entities list. update() should no-op.
        boost.update();

        // Booster should NOT be applied
        assertEquals(10f, towerStats.getDamage(), 0.001f);
        assertEquals(5f, towerStats.getRange(), 0.001f);
        assertEquals(50f, towerStats.getProjectileSpeed(), 0.001f);
        assertEquals(2f, towerStats.getAttackCooldown(), 0.001f);
    }

    @Test
    void appliesBoostWhenInRange_andRemovesWhenLeaving() {
        // Entities list for test hook
        List<Entity> entities = new ArrayList<>();

        // Totem with range 8
        Entity totem = new Entity();
        TowerStatsComponent totemStats = new TowerStatsComponent(
                100, 0f, 8f, 1f, 0f, 1f, "projectiles/bullet.png", 1, 1
        );
        totem.addComponent(totemStats);
        totem.addComponent(new TowerComponent("totem", 1, 1));
        StatsBoostComponent boost = new StatsBoostComponent();
        totem.addComponent(boost);

        // Tower initially in range
        Entity tower = new Entity();
        TowerStatsComponent towerStats = new TowerStatsComponent(
                100, 10f, 5f, 2f, 50f, 1f, "projectiles/bullet.png", 1, 1
        );
        tower.addComponent(towerStats);
        tower.addComponent(new TowerComponent("bone", 1, 1));

        totem.setPosition(0, 0);
        tower.setPosition(4, 0);
        entities.add(totem);
        entities.add(tower);
        StatsBoostComponent.setTestEntities(entities);

        // Initial apply
        boost.update();
        float expectedDamage = 10f * 1.05f;
        float expectedCooldown = 2f / 1.05f;
        assertEquals(expectedDamage, towerStats.getDamage(), 1e-4);
        assertEquals(expectedCooldown, towerStats.getAttackCooldown(), 1e-4);

        // Move tower out of range and ensure removal
        tower.setPosition(20, 0);
        boost.update();
        assertEquals(10f, towerStats.getDamage(), 1e-4);
        assertEquals(2f, towerStats.getAttackCooldown(), 1e-4);
    }

    @Test
    void adjustsExistingBoostWhenTotemLevelChanges() {
        List<Entity> entities = new ArrayList<>();

        Entity totem = new Entity();
        TowerStatsComponent totemStats = new TowerStatsComponent(
                100, 0f, 10f, 1f, 0f, 1f, "projectiles/bullet.png", 1, 1
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
        tower.setPosition(5, 0);
        entities.add(totem);
        entities.add(tower);
        StatsBoostComponent.setTestEntities(entities);

        // Apply at level_B=1 (1.05)
        boost.update();
        assertEquals(10f * 1.05f, towerStats.getDamage(), 1e-4);
        assertEquals(2f / 1.05f, towerStats.getAttackCooldown(), 1e-4);

        // Increase to level_B=3 (1.15) and update: should adjust by ratio
        totemStats.incrementLevel_B(); // -> 2
        totemStats.incrementLevel_B(); // -> 3
        boost.update();
        assertEquals(10f * 1.15f, towerStats.getDamage(), 1e-4);
        assertEquals(2f / 1.15f, towerStats.getAttackCooldown(), 1e-4);
    }

    @Test
    void multipleTotemsStackByDefault() {
        List<Entity> entities = new ArrayList<>();

        // Totem A (range 12)
        Entity totemA = new Entity();
        TowerStatsComponent statsA = new TowerStatsComponent(
                100, 0f, 12f, 1f, 0f, 1f, "projectiles/bullet.png", 1, 1
        );
        totemA.addComponent(statsA);
        totemA.addComponent(new TowerComponent("totem", 1, 1));
        StatsBoostComponent boostA = new StatsBoostComponent();
        totemA.addComponent(boostA);

        // Totem B (range 12)
        Entity totemB = new Entity();
        TowerStatsComponent statsB = new TowerStatsComponent(
                100, 0f, 12f, 1f, 0f, 1f, "projectiles/bullet.png", 1, 1
        );
        totemB.addComponent(statsB);
        totemB.addComponent(new TowerComponent("totem", 1, 1));
        StatsBoostComponent boostB = new StatsBoostComponent();
        totemB.addComponent(boostB);

        // Target tower
        Entity tower = new Entity();
        TowerStatsComponent towerStats = new TowerStatsComponent(
                100, 10f, 5f, 2f, 50f, 1f, "projectiles/bullet.png", 1, 1
        );
        tower.addComponent(towerStats);
        tower.addComponent(new TowerComponent("bone", 1, 1));

        totemA.setPosition(0, 0);
        totemB.setPosition(0, 0);
        tower.setPosition(6, 0);
        entities.add(totemA);
        entities.add(totemB);
        entities.add(tower);
        StatsBoostComponent.setTestEntities(entities);

        // Apply both
        boostA.update();
        boostB.update();

        double combined = 1.05 * 1.05;
        assertEquals(10f * combined, towerStats.getDamage(), 1e-4);
        assertEquals(2f / combined, towerStats.getAttackCooldown(), 1e-4);
    }

    @Test
    void doesNotBuffTotems() {
        List<Entity> entities = new ArrayList<>();

        Entity totem = new Entity();
        TowerStatsComponent totemStats = new TowerStatsComponent(
                100, 5f, 10f, 2f, 20f, 1f, "projectiles/bullet.png", 1, 1
        );
        totem.addComponent(totemStats);
        totem.addComponent(new TowerComponent("totem", 1, 1));
        StatsBoostComponent boost = new StatsBoostComponent();
        totem.addComponent(boost);

        // Another totem (should not be buffed)
        Entity otherTotem = new Entity();
        TowerStatsComponent otherStats = new TowerStatsComponent(
                100, 10f, 6f, 1.5f, 30f, 1f, "projectiles/bullet.png", 1, 1
        );
        otherTotem.addComponent(otherStats);
        otherTotem.addComponent(new TowerComponent("totem", 1, 1));

        totem.setPosition(0, 0);
        otherTotem.setPosition(5, 0);
        entities.add(totem);
        entities.add(otherTotem);
        StatsBoostComponent.setTestEntities(entities);

        boost.update();

        // No change on other totem
        assertEquals(10f, otherStats.getDamage(), 1e-4);
        assertEquals(1.5f, otherStats.getAttackCooldown(), 1e-4);
    }

    @Test
    void appliesToNewTowerPlacedInRange() {
        List<Entity> entities = new ArrayList<>();

        Entity totem = new Entity();
        TowerStatsComponent totemStats = new TowerStatsComponent(
                100, 0f, 10f, 1f, 0f, 1f, "projectiles/bullet.png", 1, 1
        );
        totem.addComponent(totemStats);
        totem.addComponent(new TowerComponent("totem", 1, 1));
        StatsBoostComponent boost = new StatsBoostComponent();
        totem.addComponent(boost);

        totem.setPosition(0, 0);
        entities.add(totem);
        StatsBoostComponent.setTestEntities(entities);

        // First update with only totem
        boost.update();

        // Now place a tower within range
        Entity tower = new Entity();
        TowerStatsComponent towerStats = new TowerStatsComponent(
                100, 10f, 5f, 2f, 50f, 1f, "projectiles/bullet.png", 1, 1
        );
        tower.addComponent(towerStats);
        tower.addComponent(new TowerComponent("bone", 1, 1));
        tower.setPosition(6, 0);
        entities.add(tower);

        // Next update should apply boost
        boost.update();
        assertEquals(10f * 1.05f, towerStats.getDamage(), 1e-4);
        assertEquals(2f / 1.05f, towerStats.getAttackCooldown(), 1e-4);
    }

    @Test
    void rangeIncreaseApplies_andRangeDecreaseRemoves() {
        List<Entity> entities = new ArrayList<>();

        Entity totem = new Entity();
        TowerStatsComponent totemStats = new TowerStatsComponent(
                100, 0f, 3f, 1f, 0f, 1f, "projectiles/bullet.png", 1, 1
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
        tower.setPosition(5, 0); // out of initial range (3)
        entities.add(totem);
        entities.add(tower);
        StatsBoostComponent.setTestEntities(entities);

        // No apply
        boost.update();
        assertEquals(10f, towerStats.getDamage(), 1e-4);

        // Increase range -> apply
        totemStats.setRange(6f);
        boost.update();
        assertEquals(10f * 1.05f, towerStats.getDamage(), 1e-4);

        // Decrease range -> remove
        totemStats.setRange(2f);
        boost.update();
        assertEquals(10f, towerStats.getDamage(), 1e-4);
    }

    @Test
    void onTargetStatsChanged_reappliesWithoutDouble() {
        List<Entity> entities = new ArrayList<>();

        Entity totem = new Entity();
        TowerStatsComponent totemStats = new TowerStatsComponent(
                100, 0f, 10f, 1f, 0f, 1f, "projectiles/bullet.png", 1, 1
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
        tower.setPosition(5, 0);
        entities.add(totem);
        entities.add(tower);
        StatsBoostComponent.setTestEntities(entities);

        // Apply once
        boost.update();
        assertEquals(10f * 1.05f, towerStats.getDamage(), 1e-4);

        // Simulate target stats changed (e.g., upgraded)
        boost.onTargetStatsChanged(tower);
        // The boost should have been reverted; damage should be back to base before next update
        assertEquals(10f, towerStats.getDamage(), 1e-4);

        // Next update re-applies exactly once
        boost.update();
        assertEquals(10f * 1.05f, towerStats.getDamage(), 1e-4);
    }

    @Test
    void dispose_revertsBoostsImmediately() {
        List<Entity> entities = new ArrayList<>();

        Entity totem = new Entity();
        TowerStatsComponent totemStats = new TowerStatsComponent(
                100, 0f, 10f, 1f, 0f, 1f, "projectiles/bullet.png", 1, 1
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
        tower.setPosition(5, 0);
        entities.add(totem);
        entities.add(tower);
        StatsBoostComponent.setTestEntities(entities);

        // Apply then dispose
        boost.update();
        assertEquals(10f * 1.05f, towerStats.getDamage(), 1e-4);
        boost.dispose();

        // Should be reverted to base values
        assertEquals(10f, towerStats.getDamage(), 1e-4);
        assertEquals(2f, towerStats.getAttackCooldown(), 1e-4);
    }
}
