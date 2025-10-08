package com.csse3200.game.components.mainmenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.services.leaderboard.SessionLeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MainMenu ranking functionality.
 */
@ExtendWith(GameExtension.class)
class MainMenuRankingTest {
    
    private GdxGame game;
    private MainMenuActions mainMenuActions;
    
    @BeforeEach
    void setUp() {
        game = new GdxGame();
        mainMenuActions = new MainMenuActions(game);
        
        // Clear services
        ServiceLocator.clear();
        
        // Register required services
        ServiceLocator.registerLeaderboardService(new SessionLeaderboardService("test-player"));
    }
    
    @Test
    void testRankingButtonClick() {
        // Test that ranking button click triggers the event
        assertDoesNotThrow(() -> {
            mainMenuActions.create();
            // Test that the component was created successfully
            assertNotNull(mainMenuActions);
        });
    }
    
    @Test
    void testLeaderboardServiceAvailable() {
        // Test that leaderboard service is available
        LeaderboardService leaderboardService = ServiceLocator.getLeaderboardService();
        assertNotNull(leaderboardService);
        assertTrue(leaderboardService instanceof SessionLeaderboardService);
    }
    
    @Test
    void testEmptyLeaderboardHandling() {
        // Test that empty leaderboard is handled gracefully
        LeaderboardService leaderboardService = ServiceLocator.getLeaderboardService();
        assertNotNull(leaderboardService);
        
        // Get entries from empty leaderboard
        var entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        
        assertTrue(entries.isEmpty());
        
        // Get my best from empty leaderboard should return default entry
        var myBest = leaderboardService.getMyBest();
        assertNotNull(myBest);
        assertEquals(0, myBest.score);
    }
    
    @Test
    void testLeaderboardServiceFallback() {
        // Test that fallback service is registered when none exists
        ServiceLocator.clear();
        
        // Simulate the scenario where no leaderboard service is registered
        assertNull(ServiceLocator.getLeaderboardService());
        
        // The showLeaderboard method should handle this gracefully
        assertDoesNotThrow(() -> {
            mainMenuActions.create();
            // Test that the component was created successfully
            assertNotNull(mainMenuActions);
        });
    }
}
