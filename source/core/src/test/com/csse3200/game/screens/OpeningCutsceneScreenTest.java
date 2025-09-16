package com.csse3200.game.screens;

import com.csse3200.game.GdxGame;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class OpeningCutsceneScreenTest {

    @Mock
    private GdxGame mockGame;

    @BeforeEach
    void setUp() {
        // Basic setup
    }

    @Test
    void testOpeningCutsceneScreenCreation() {
        assertDoesNotThrow(() -> {
            OpeningCutsceneScreen screen = new OpeningCutsceneScreen(mockGame);
            assertNotNull(screen);
        });
    }

    @Test
    void testWithBackgroundStaticMethod() {
        assertDoesNotThrow(() -> {
            OpeningCutsceneScreen screen = OpeningCutsceneScreen.withBackground(mockGame, 0);
            assertNotNull(screen);
        });
    }
}
