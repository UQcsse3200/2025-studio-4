package com.csse3200.game.components.towers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import core.src.main.com.csse3200.game.components.towers.TowerStatsComponent;

public class TowerStatsComponentTest {

    @Test
    void testGettersAndSetters() {
        // Provide initial levels
        TowerStatsComponent stats = new TowerStatsComponent(
                100, 15f, 7f, 2f,
                50f, 1f, "projectiles/bullet.png",
                0, 0 // level_A, level_B
        );

        assertEquals(100, stats.getHealth());
        stats.setHealth(80);
        assertEquals(80, stats.getHealth());
        stats.setHealth(-10);
        assertEquals(0, stats.getHealth());

        assertEquals(15f, stats.getDamage());
        stats.setDamage(20f);
        assertEquals(20f, stats.getDamage());

        assertEquals(7f, stats.getRange());
        stats.setRange(10f);
        assertEquals(10f, stats.getRange());

        assertEquals(2f, stats.getAttackCooldown());
        stats.setAttackCooldown(1.5f);
        assertEquals(1.5f, stats.getAttackCooldown());
    }

    @Test
    void testAttackTimer() {
        TowerStatsComponent stats = new TowerStatsComponent(
                100, 10f, 5f, 1f,
                50f, 1f, "projectiles/bullet.png",
                0, 0
        );

        assertEquals(0f, stats.getAttackTimer());
        assertFalse(stats.canAttack());

        stats.updateAttackTimer(0.5f);
        assertFalse(stats.canAttack());

        stats.updateAttackTimer(0.5f);
        assertTrue(stats.canAttack());

        stats.resetAttackTimer();
        assertEquals(0f, stats.getAttackTimer());
    }

    @Test
    void testLevelIncrement() {
        TowerStatsComponent stats = new TowerStatsComponent(
                100, 10f, 5f, 1f,
                50f, 1f, "projectiles/bullet.png",
                0, 0
        );

        // Increment A
        assertEquals(0, stats.getLevel_A());
        stats.incrementLevel_A();
        assertEquals(1, stats.getLevel_A());
        for (int i = 0; i < 5; i++) stats.incrementLevel_A(); // should cap at 5
        assertEquals(5, stats.getLevel_A());

        // Increment B
        assertEquals(0, stats.getLevel_B());
        stats.incrementLevel_B();
        assertEquals(1, stats.getLevel_B());
        for (int i = 0; i < 5; i++) stats.incrementLevel_B(); // should cap at 5
        assertEquals(5, stats.getLevel_B());
    }
}
