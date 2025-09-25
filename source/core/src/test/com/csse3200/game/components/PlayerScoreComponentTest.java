package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerScoreComponentTest {
    private Entity player;
    private PlayerScoreComponent playerScoreComponent;

    // Setup before each test
    @BeforeEach
    void beforeEach() {
        player = new Entity();
        playerScoreComponent = new PlayerScoreComponent();
        player.addComponent(playerScoreComponent);
        player.create();
    }

    // Ensure the score is initialised to 0
    @Test
    void startsAtZero() {
        assertEquals(0, playerScoreComponent.getTotalScore());
    }

    // Positive points can be added and accumulate
    @Test
    void addPositivePoints() {
        playerScoreComponent.addPoints(50);
        assertEquals(50, playerScoreComponent.getTotalScore());
        playerScoreComponent.addPoints(100);
        assertEquals(150, playerScoreComponent.getTotalScore());    // accumulates
    }

    // Zero points cannot be added
    @Test
    void addZeroPoints() {
        playerScoreComponent.addPoints(0);
        assertEquals(0, playerScoreComponent.getTotalScore());      // adding 0 to 0 points
        playerScoreComponent.addPoints(100);
        playerScoreComponent.addPoints(0);
        assertEquals(100, playerScoreComponent.getTotalScore());    // adding 0 to positive points
    }

    // Negative points cannot be added
    @Test
    void addNegativePoints() {
        playerScoreComponent.addPoints(-50);
        assertEquals(0, playerScoreComponent.getTotalScore());      // adding negative to 0 points
        playerScoreComponent.addPoints(100);
        playerScoreComponent.addPoints(-50);
        assertEquals(100, playerScoreComponent.getTotalScore());    // adding negative to positive points
    }

    // Reset the score to 0
    @Test
    void resetScore() {
        playerScoreComponent.addPoints(50);
        assertEquals(50, playerScoreComponent.getTotalScore());
        playerScoreComponent.reset();
        assertEquals(0, playerScoreComponent.getTotalScore());
    }

}
