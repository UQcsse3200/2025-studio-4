package com.csse3200.game.services.leaderboard;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersistentLeaderboardServiceTest {

    @Mock
    private Application mockApp;
    
    @Mock
    private FileHandle mockFileHandle;

    private PersistentLeaderboardService leaderboardService;

    @BeforeEach
    void setUp() {
        // Mock Gdx
        Gdx.app = mockApp;
        Gdx.files = mock(com.badlogic.gdx.Files.class);
        
        // Mock file operations
        when(Gdx.files.local(anyString())).thenReturn(mockFileHandle);
        when(mockFileHandle.exists()).thenReturn(false);
        
        leaderboardService = new PersistentLeaderboardService("test-player");
    }

    @Test
    void testAddEntry() {
        // Test adding a new entry
        leaderboardService.addEntry("TestPlayer", 1000);
        
        // Verify entry was added
        List<LeaderboardService.LeaderboardEntry> entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        
        assertEquals(1, entries.size());
        assertEquals("TestPlayer", entries.get(0).displayName);
        assertEquals(1000, entries.get(0).score);
        assertEquals(1, entries.get(0).rank);
    }

    @Test
    void testMultipleEntriesRanking() {
        // Add multiple entries
        leaderboardService.addEntry("Player1", 500);
        leaderboardService.addEntry("Player2", 1000);
        leaderboardService.addEntry("Player3", 750);
        
        // Get entries
        List<LeaderboardService.LeaderboardEntry> entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        
        // Verify correct ranking (highest score first)
        assertEquals(3, entries.size());
        assertEquals("Player2", entries.get(0).displayName);
        assertEquals(1000, entries.get(0).score);
        assertEquals(1, entries.get(0).rank);
        
        assertEquals("Player3", entries.get(1).displayName);
        assertEquals(750, entries.get(1).score);
        assertEquals(2, entries.get(1).rank);
        
        assertEquals("Player1", entries.get(2).displayName);
        assertEquals(500, entries.get(2).score);
        assertEquals(3, entries.get(2).rank);
    }

    @Test
    void testEmptyPlayerName() {
        // Test with empty player name - should use "Player" as default
        leaderboardService.addEntry("", 500);
        leaderboardService.addEntry(null, 600);
        leaderboardService.addEntry("   ", 700);
        
        List<LeaderboardService.LeaderboardEntry> entries = leaderboardService.getEntries(
            new LeaderboardService.LeaderboardQuery(0, 10, false)
        );
        
        assertEquals(3, entries.size());
        // All should have "Player" as display name
        for (LeaderboardService.LeaderboardEntry entry : entries) {
            assertEquals("Player", entry.displayName);
        }
    }

    @Test
    void testGetMyBest() {
        // Add some entries for the current player
        leaderboardService.addEntry("MyName", 500);
        leaderboardService.addEntry("MyName", 1000);
        leaderboardService.addEntry("OtherPlayer", 750);
        
        LeaderboardService.LeaderboardEntry myBest = leaderboardService.getMyBest();
        
        // Should return the highest score for this player
        assertEquals("MyName", myBest.displayName);
        assertEquals(1000, myBest.score);
    }

    @Test
    void testGetMyBestNoEntries() {
        // Test when no entries exist
        LeaderboardService.LeaderboardEntry myBest = leaderboardService.getMyBest();
        
        assertEquals("You", myBest.displayName);
        assertEquals(0, myBest.score);
        assertEquals(0, myBest.rank);
    }
}
