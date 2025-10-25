package com.csse3200.game.screens;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VictoryScreenTest {

    @Test
    void testVictoryScreenCreation() {
        assertNotNull(VictoryScreen.class);
        assertEquals("com.csse3200.game.screens.VictoryScreen", 
                    VictoryScreen.class.getName());
    }
}
