package com.csse3200.game.services.leaderboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryLeaderboardService implementation.
 */
class InMemoryLeaderboardServiceTest {
    
    private InMemoryLeaderboardService leaderboardService;
    
    @BeforeEach
    void setUp() {
        leaderboardService = new InMemoryLeaderboardService("test-player");
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
        assertEquals("avatar_1", entries.get(0).avatarId); // 默认头像
    }
    
    @Test
    void testMultipleScoresRanking() {
        // Submit multiple scores
        leaderboardService.submitScore(500);
        leaderboardService.submitScore(1000);
        leaderboardService.submitScore(750);
        
        var entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        
        assertEquals(3, entries.size());
        // Should be ranked by score (highest first)
        assertEquals(1000, entries.get(0).score);
        assertEquals(1, entries.get(0).rank);
        assertEquals(750, entries.get(1).score);
        assertEquals(2, entries.get(1).rank);
        assertEquals(500, entries.get(2).score);
        assertEquals(3, entries.get(2).rank);
    }
    
    @Test
    void testGetMyBest() {
        leaderboardService.submitScore(500);
        leaderboardService.submitScore(1000);
        leaderboardService.submitScore(750);
        
        var myBest = leaderboardService.getMyBest();
        assertNotNull(myBest);
        assertEquals(1000, myBest.score); // Should return highest score
    }
    
    @Test
    void testEmptyLeaderboard() {
        var entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        
        assertTrue(entries.isEmpty());
        // Implementation returns a default LeaderboardEntry when no scores submitted
        var myBest = leaderboardService.getMyBest();
        assertNotNull(myBest);
        assertEquals(0, myBest.score);
    }
    
    @Test
    void testPagination() {
        // Submit 25 scores
        for (int i = 1; i <= 25; i++) {
            leaderboardService.submitScore(i * 100);
        }
        
        // Test first page
        var firstPage = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        assertEquals(10, firstPage.size());
        assertEquals(2500, firstPage.get(0).score); // Highest score first
        
        // Test second page
        var secondPage = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(10, 10, false)
        );
        assertEquals(10, secondPage.size());
        assertEquals(1500, secondPage.get(0).score); // 16th highest score
    }
}
