package com.csse3200.game.services;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.Assert.assertEquals;

@ExtendWith(GameExtension.class)
class GameStateServiceTest {
    GameStateService gameState;

    @BeforeEach
    void beforeAll() {
        gameState = new GameStateService();
    }

    @Test
    void shouldGet() {
        assertEquals(0, gameState.getStars());
    }

    @Test
    void shouldSet() {
        gameState.setStars(345);
        assertEquals(345, gameState.getStars());
    }

    @Test
    void shouldIncrement() {
        gameState.updateStars(5);
        assertEquals(5, gameState.getStars());
    }

    @Test
    void multipleIncrement() {
        for (int i = 0; i < 10; i++) {
            gameState.updateStars(1);
            assertEquals(i + 1, gameState.getStars());
        }
    }
}
