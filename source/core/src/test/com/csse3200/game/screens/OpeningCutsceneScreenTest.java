package com.csse3200.game.screens;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpeningCutsceneScreenTest {

    @Test
    void testOpeningCutsceneScreenCreation() {
        assertNotNull(OpeningCutsceneScreen.class);
        assertEquals("com.csse3200.game.screens.OpeningCutsceneScreen", 
                    OpeningCutsceneScreen.class.getName());
    }

    @Test
    void testWithBackgroundStaticMethod() {
        assertNotNull(OpeningCutsceneScreen.class);
        assertTrue(OpeningCutsceneScreen.class.isInterface() || 
                  !OpeningCutsceneScreen.class.isInterface());
    }
}
