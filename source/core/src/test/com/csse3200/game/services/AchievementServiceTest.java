package com.csse3200.game.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AchievementService
 * Tests achievement unlock, status checking, and statistics
 */
public class AchievementServiceTest {
    private AchievementService achievementService;

    @BeforeEach
    public void setUp() {
        achievementService = new AchievementService();
    }

    @Test
    public void testInitialState_AllAchievementsLocked() {
        // All achievements should be locked initially
        assertFalse(achievementService.isUnlocked(AchievementService.TOUGH_SURVIVOR));
        assertFalse(achievementService.isUnlocked(AchievementService.SPEED_RUNNER));
        assertFalse(achievementService.isUnlocked(AchievementService.SLAYER));
        assertFalse(achievementService.isUnlocked(AchievementService.PERFECT_CLEAR));
        assertFalse(achievementService.isUnlocked(AchievementService.PARTICIPATION));
    }

    @Test
    public void testUnlockAchievement_SingleAchievement() {
        // Unlock participation achievement
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        
        // Check it's unlocked
        assertTrue(achievementService.isUnlocked(AchievementService.PARTICIPATION));
        
        // Check others are still locked
        assertFalse(achievementService.isUnlocked(AchievementService.TOUGH_SURVIVOR));
        assertFalse(achievementService.isUnlocked(AchievementService.SPEED_RUNNER));
    }

    @Test
    public void testUnlockAchievement_MultipleAchievements() {
        // Unlock multiple achievements
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        achievementService.unlockAchievement(AchievementService.SPEED_RUNNER);
        achievementService.unlockAchievement(AchievementService.PERFECT_CLEAR);
        
        // Check all three are unlocked
        assertTrue(achievementService.isUnlocked(AchievementService.PARTICIPATION));
        assertTrue(achievementService.isUnlocked(AchievementService.SPEED_RUNNER));
        assertTrue(achievementService.isUnlocked(AchievementService.PERFECT_CLEAR));
        
        // Check others are still locked
        assertFalse(achievementService.isUnlocked(AchievementService.TOUGH_SURVIVOR));
        assertFalse(achievementService.isUnlocked(AchievementService.SLAYER));
    }

    @Test
    public void testUnlockAchievement_AlreadyUnlocked() {
        // Unlock achievement twice
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        
        // Should still be unlocked only once
        assertTrue(achievementService.isUnlocked(AchievementService.PARTICIPATION));
        assertEquals(1, achievementService.getUnlockedCount());
    }

    @Test
    public void testUnlockAchievement_InvalidId() {
        // Try to unlock non-existent achievement
        achievementService.unlockAchievement("invalid_achievement_id");
        
        // Should not crash, just log warning
        assertEquals(0, achievementService.getUnlockedCount());
    }

    @Test
    public void testGetUnlockedCount_NoAchievements() {
        assertEquals(0, achievementService.getUnlockedCount());
    }

    @Test
    public void testGetUnlockedCount_SomeAchievements() {
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        achievementService.unlockAchievement(AchievementService.SPEED_RUNNER);
        
        assertEquals(2, achievementService.getUnlockedCount());
    }

    @Test
    public void testGetUnlockedCount_AllAchievements() {
        achievementService.unlockAchievement(AchievementService.TOUGH_SURVIVOR);
        achievementService.unlockAchievement(AchievementService.SPEED_RUNNER);
        achievementService.unlockAchievement(AchievementService.SLAYER);
        achievementService.unlockAchievement(AchievementService.PERFECT_CLEAR);
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        
        assertEquals(5, achievementService.getUnlockedCount());
    }

    @Test
    public void testGetTotalCount() {
        // Should always return 5 (total number of achievements)
        assertEquals(5, achievementService.getTotalCount());
    }

    @Test
    public void testResetAchievements() {
        // Unlock some achievements
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        achievementService.unlockAchievement(AchievementService.SPEED_RUNNER);
        achievementService.unlockAchievement(AchievementService.SLAYER);
        
        assertEquals(3, achievementService.getUnlockedCount());
        
        // Reset all achievements
        achievementService.resetAchievements();
        
        // All should be locked again
        assertEquals(0, achievementService.getUnlockedCount());
        assertFalse(achievementService.isUnlocked(AchievementService.PARTICIPATION));
        assertFalse(achievementService.isUnlocked(AchievementService.SPEED_RUNNER));
        assertFalse(achievementService.isUnlocked(AchievementService.SLAYER));
    }

    @Test
    public void testGetAllAchievementIds() {
        var ids = achievementService.getAllAchievementIds();
        
        // Should contain all 5 achievement IDs
        assertEquals(5, ids.size());
        assertTrue(ids.contains(AchievementService.TOUGH_SURVIVOR));
        assertTrue(ids.contains(AchievementService.SPEED_RUNNER));
        assertTrue(ids.contains(AchievementService.SLAYER));
        assertTrue(ids.contains(AchievementService.PERFECT_CLEAR));
        assertTrue(ids.contains(AchievementService.PARTICIPATION));
    }

    @Test
    public void testIsUnlocked_UnknownId() {
        // Should return false for unknown achievement
        assertFalse(achievementService.isUnlocked("unknown_achievement"));
    }

    @Test
    public void testAchievementPersistence_SimulateGameSession() {
        // Simulate a game session
        // Start: all locked
        assertEquals(0, achievementService.getUnlockedCount());
        
        // Player plays game
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        
        // Player defeats 5 enemies
        achievementService.unlockAchievement(AchievementService.SPEED_RUNNER);
        
        // Player wins game
        achievementService.unlockAchievement(AchievementService.PERFECT_CLEAR);
        achievementService.unlockAchievement(AchievementService.TOUGH_SURVIVOR);
        
        // Verify 4 achievements unlocked
        assertEquals(4, achievementService.getUnlockedCount());
        
        // Verify specific achievements
        assertTrue(achievementService.isUnlocked(AchievementService.PARTICIPATION));
        assertTrue(achievementService.isUnlocked(AchievementService.SPEED_RUNNER));
        assertTrue(achievementService.isUnlocked(AchievementService.PERFECT_CLEAR));
        assertTrue(achievementService.isUnlocked(AchievementService.TOUGH_SURVIVOR));
        assertFalse(achievementService.isUnlocked(AchievementService.SLAYER));
    }
}

