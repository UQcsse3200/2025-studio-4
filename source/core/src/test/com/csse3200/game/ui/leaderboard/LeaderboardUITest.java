package com.csse3200.game.ui.leaderboard;

import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import com.csse3200.game.services.leaderboard.SessionLeaderboardService;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LeaderboardUI class.
 */
@ExtendWith(GameExtension.class)
class LeaderboardUITest {
    
    private LeaderboardService leaderboardService;
    
    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        
        leaderboardService = new SessionLeaderboardService("test-player");
        ServiceLocator.registerLeaderboardService(leaderboardService);
    }
    
    @Test
    void testLeaderboardControllerCreation() {
        // Test that LeaderboardController can be created
        LeaderboardController controller = new LeaderboardController(leaderboardService);
        assertNotNull(controller);
    }
    
    @Test
    void testLeaderboardControllerFunctionality() {
        LeaderboardController controller = new LeaderboardController(leaderboardService);
        
        // Test initial state
        assertTrue(controller.isFirstPage());
        assertFalse(controller.isFriendsOnly());
        
        // Test pagination
        controller.nextPage();
        assertFalse(controller.isFirstPage());
        
        controller.prevPage();
        assertTrue(controller.isFirstPage());
        
        // Test friends toggle
        controller.toggleFriends();
        assertTrue(controller.isFriendsOnly());
        
        controller.toggleFriends();
        assertFalse(controller.isFriendsOnly());
    }
    
    @Test
    void testLeaderboardServiceIntegration() {
        // Test that LeaderboardUI uses the registered LeaderboardService
        assertSame(leaderboardService, ServiceLocator.getLeaderboardService());
        
        // Test leaderboard functionality
        leaderboardService.submitScore(1000);
        
        var entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        
        assertEquals(1, entries.size());
        assertEquals(1000, entries.get(0).score);
        assertEquals(1, entries.get(0).rank);
    }
    
    @Test
    void testLeaderboardControllerWithScores() {
        LeaderboardController controller = new LeaderboardController(leaderboardService);
        
        // Submit some scores
        leaderboardService.submitScore(500);
        leaderboardService.submitScore(1000);
        leaderboardService.submitScore(750);
        
        // Test loading page
        var entries = controller.loadPage();
        assertEquals(1, entries.size()); // SessionLeaderboardService only keeps highest score
        assertEquals(1000, entries.get(0).score);
        
        // Test getting my best
        var myBest = controller.getMyBest();
        assertNotNull(myBest);
        assertEquals(1000, myBest.score);
    }
    
    @Test
    void testServiceLocatorIntegration() {
        // Test that LeaderboardService is properly registered
        assertNotNull(ServiceLocator.getLeaderboardService());
        assertSame(leaderboardService, ServiceLocator.getLeaderboardService());
    }
    
    @Test
    void testLeaderboardServiceFallback() {
        // Test the fallback behavior when no leaderboard service is registered
        ServiceLocator.clear();
        
        // This should not throw an exception
        assertNull(ServiceLocator.getLeaderboardService());
    }
}
