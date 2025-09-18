package com.csse3200.game.components;

import org.junit.jupiter.api.Test;
import com.csse3200.game.components.TowerStatsComponent;
import com.csse3200.game.components.TowerComponent;
import static org.junit.jupiter.api.Assertions.*;

public class TowerStatsComponentTest {

    @Test
    void testGettersAndSetters() {
        TowerStatsComponent stats = new TowerStatsComponent(100, 15, 7f, 2f);

        assertEquals(100, stats.getHealth());
        stats.setHealth(80);
        assertEquals(80, stats.getHealth());
        stats.setHealth(-10);
        assertEquals(0, stats.getHealth());

        assertEquals(15, stats.getDamage());
        assertEquals(7f, stats.getRange());
        assertEquals(2f, stats.getAttackCooldown());

        stats.setAttackCooldown(1.5f);
        assertEquals(1.5f, stats.getAttackCooldown());
    }

    @Test
    void testAttackTimer() {
        TowerStatsComponent stats = new TowerStatsComponent(100, 10, 5f, 1f);

        assertEquals(0f, stats.getAttackTimer());
        assertFalse(stats.canAttack());

        stats.updateAttackTimer(0.5f);
        assertFalse(stats.canAttack());

        stats.updateAttackTimer(0.5f);
        assertTrue(stats.canAttack());

        stats.resetAttackTimer();
        assertEquals(0f, stats.getAttackTimer());
    }
}
