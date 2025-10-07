package com.csse3200.game.services.leaderboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SessionLeaderboardService implementation.
 */
class SessionLeaderboardServiceTest {
    
    private SessionLeaderboardService leaderboardService;
    private static final String TEST_PLAYER_ID = "test-player-123";
    
    @BeforeEach
    void setUp() {
        leaderboardService = new SessionLeaderboardService(TEST_PLAYER_ID);
    }
    
    @Test
    void testSubmitScore() {
        // Test submitting a score
        leaderboardService.submitScore(1000);
        
        var entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        
        assertEquals(1, entries.size());
        assertEquals(1000, entries.get(0).score);
        assertEquals(1, entries.get(0).rank);
        assertEquals(TEST_PLAYER_ID, entries.get(0).playerId);
    }
    
    @Test
    void testMultipleScoresFromSamePlayer() {
        // Submit multiple scores from same player
        leaderboardService.submitScore(500);
        leaderboardService.submitScore(1000);
        leaderboardService.submitScore(750);
        
        var entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        
        // Should only keep the highest score
        assertEquals(1, entries.size());
        assertEquals(1000, entries.get(0).score);
        assertEquals(1, entries.get(0).rank);
    }
    
    @Test
    void testGetMyBest() {
        leaderboardService.submitScore(500);
        leaderboardService.submitScore(1000);
        leaderboardService.submitScore(750);
        
        var myBest = leaderboardService.getMyBest();
        assertNotNull(myBest);
        assertEquals(1000, myBest.score);
        assertEquals(TEST_PLAYER_ID, myBest.playerId);
    }
    
    @Test
    void testEmptyLeaderboard() {
        var entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        
        assertTrue(entries.isEmpty());
        assertNull(leaderboardService.getMyBest());
    }
    
    @Test
    void testPlayerIdConsistency() {
        leaderboardService.submitScore(1000);
        
        var myBest = leaderboardService.getMyBest();
        assertEquals(TEST_PLAYER_ID, myBest.playerId);
        
        var entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        assertEquals(TEST_PLAYER_ID, entries.get(0).playerId);
    }
    
    @Test
    void testScoreUpdate() {
        // Submit initial score
        leaderboardService.submitScore(500);
        var initialBest = leaderboardService.getMyBest();
        assertEquals(500, initialBest.score);
        
        // Submit higher score
        leaderboardService.submitScore(1000);
        var updatedBest = leaderboardService.getMyBest();
        assertEquals(1000, updatedBest.score);
        
        // Should only have one entry (the highest)
        var entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        assertEquals(1, entries.size());
        assertEquals(1000, entries.get(0).score);
    }
}
