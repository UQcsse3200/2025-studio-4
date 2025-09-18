package com.csse3200.game.screens;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.services.SaveGameService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.GameStateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MainGameOver logic (non-UI parts).
 * - We only test logic such as saveScore() here.
 * - UI drawing and button clicks must be checked manually in game.
 */
@ExtendWith(GameExtension.class)
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

        // Setup mock behavior for getEntries
        List<LeaderboardService.LeaderboardEntry> mockEntries = new ArrayList<>();
        when(mockLeaderboard.getEntries(any(LeaderboardService.LeaderboardQuery.class)))
                .thenReturn(mockEntries);

        // Register mocks in ServiceLocator
        ServiceLocator.registerLeaderboardService(mockLeaderboard);
        ServiceLocator.registerSaveGameService(mockSaveGame);
        
        // Register GameStateService to prevent NullPointerException
        ServiceLocator.registerGameStateService(new GameStateService());
    }

    @Test
    void testSaveScoreWithName() {
        // Act
        mainGameOver.saveScore("Vincent", 100);

        // Assert
        verify(mockLeaderboard).addEntry("Vincent", 100);
        verify(mockSaveGame).save(eq("leaderboard"), any());
    }

    @Test
    void testSaveScoreDefaultName() {
        // Act
        mainGameOver.saveScore("", 200);

        // Assert
        verify(mockLeaderboard).addEntry("Player", 200);
        verify(mockSaveGame).save(eq("leaderboard"), any());
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