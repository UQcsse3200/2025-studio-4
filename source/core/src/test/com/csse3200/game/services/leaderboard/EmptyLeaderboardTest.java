package com.csse3200.game.services.leaderboard;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for empty leaderboard functionality.
 */
@ExtendWith(GameExtension.class)
class EmptyLeaderboardTest {
    
    private SessionLeaderboardService leaderboardService;
    
    @BeforeEach
    void setUp() {
        // Clear services
        ServiceLocator.clear();
        
        // Create a fresh leaderboard service
        leaderboardService = new SessionLeaderboardService("test-player");
    }
    
    @Test
    void testEmptyLeaderboardGetEntries() {
        // Test that getEntries returns empty list for empty leaderboard
        var entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }
    
    @Test
    void testEmptyLeaderboardGetMyBest() {
        // Test that getMyBest returns default entry for empty leaderboard
        var myBest = leaderboardService.getMyBest();
        
        assertNotNull(myBest);
        assertEquals("test-player", myBest.playerId);
        assertEquals(0, myBest.score);
        assertEquals("avatar_1", myBest.avatarId);
    }
    
    @Test
    void testEmptyLeaderboardWithOffset() {
        // Test that getEntries with offset returns empty list
        var entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(10, 10, false)
        );
        
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }
    
    @Test
    void testEmptyLeaderboardWithLargeOffset() {
        // Test that getEntries with large offset returns empty list
        var entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(100, 10, false)
        );
        
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }
    
    @Test
    void testEmptyLeaderboardTotalEntries() {
        // Test that total entries is 0 for empty leaderboard
        assertEquals(0, leaderboardService.getTotalEntries());
    }
    
    @Test
    void testEmptyLeaderboardClearSession() {
        // Test that clearSession works on empty leaderboard
        assertDoesNotThrow(() -> {
            leaderboardService.clearSession();
        });
        
        assertEquals(0, leaderboardService.getTotalEntries());
    }
}
