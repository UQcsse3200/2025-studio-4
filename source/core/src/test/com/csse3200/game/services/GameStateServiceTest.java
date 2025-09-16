package com.csse3200.game.services;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GameStateServiceTest {
    GameStateService gameState;

    @BeforeEach
    void beforeAll() {
        gameState = new GameStateService();
    }

    @Test
    void shouldGet() {
        assert(gameState.getStars() == 0);
    }

    @Test
    void shouldSet() {
        gameState.setStars(345);
        assert(gameState.getStars() == 345);
    }

    @Test
    void shouldIncrement() {
        gameState.updateStars(5);
        assert(gameState.getStars() == 5);
    }

    @Test
    void multipleIncrement() {
        for (int i = 0; i < 10; i++) {
            gameState.updateStars(1);
            assert(gameState.getStars() == i+1);
        }
    }
}
