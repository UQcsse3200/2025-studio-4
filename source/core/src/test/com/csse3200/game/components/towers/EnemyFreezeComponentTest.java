package com.csse3200.game.components.towers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Very small smoke tests for EnemyFreezeComponent.
 * Matches the simple style used by CurrencyGeneratorComponentTest so it compiles and runs quickly.
 */
class EnemyFreezeComponentTest {

    @Test
    void testConstructor() {
        EnemyFreezeComponent comp = new EnemyFreezeComponent();
        assertNotNull(comp);
    }

    @Test
    void testDisposeDoesNotThrow() {
        EnemyFreezeComponent comp = new EnemyFreezeComponent();
        assertDoesNotThrow(comp::dispose);
    }
}

