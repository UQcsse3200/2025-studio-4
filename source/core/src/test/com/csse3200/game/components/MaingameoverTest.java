package com.csse3200.game.components;

import com.csse3200.game.components.maingame.MainGameOver;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.services.SaveGameService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MainGameOver logic (non-UI parts).
 * - We only test logic such as saveScore() here.
 * - UI drawing and button clicks must be checked manually in game.
 */
class MainGameOverTest {
    private MainGameOver mainGameOver;
    private LeaderboardService mockLeaderboard;
    private SaveGameService mockSaveGame;

    @BeforeEach
    void setUp() {
        // Create new instance for each test
        mainGameOver = new MainGameOver();

        // Mock leaderboard and saveGame
        mockLeaderboard = mock(LeaderboardService.class);
        mockSaveGame = mock(SaveGameService.class);

        // Register mocks in ServiceLocator
        ServiceLocator.registerLeaderboardService(mockLeaderboard);
        ServiceLocator.registerSaveGameService(mockSaveGame);
    }

    @Test
    void testSaveScoreWithName() {
        // Act
        mainGameOver.saveScore("Vincent", 100);

        // Assert
        verify(mockLeaderboard).addEntry("Vincent", 100);
        verify(mockSaveGame).save(eq("leaderboard"), any(List.class));
    }

    @Test
    void testSaveScoreDefaultName() {
        // Act
        mainGameOver.saveScore("", 200);

        // Assert
        verify(mockLeaderboard).addEntry("Player", 200);
        verify(mockSaveGame).save(eq("leaderboard"), any(List.class));
    }

    @Test
    void testSaveScoreWhenServicesMissing() {
        // Arrange: unregister mocks (simulate missing services)
        ServiceLocator.clear();

        // Should not throw even if leaderboard/saveGame not available
        mainGameOver.saveScore("NoService", 50);

        // Nothing to verify, just make sure no exception
    }
}
