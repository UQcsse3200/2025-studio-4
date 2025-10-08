package com.csse3200.game.components.maingame;

import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.AchievementService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for achievement unlock logic during gameplay
 * Verifies that achievements are unlocked based on game conditions
 */
@ExtendWith(GameExtension.class)
public class AchievementUnlockTest {
    
    private AchievementService achievementService;

    @BeforeEach
    public void setUp() {
        achievementService = new AchievementService();
        ServiceLocator.registerAchievementService(achievementService);
        
        // Reset enemy defeat counter
        ForestGameArea.NUM_ENEMIES_DEFEATED = 0;
    }

    @Test
    public void testParticipationUnlock_OnFirstGame() {
        // Simulate playing first game
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        
        assertTrue(achievementService.isUnlocked(AchievementService.PARTICIPATION));
    }

    @Test
    public void testPerfectClearUnlock_OnVictory() {
        // Simulate winning the game
        achievementService.unlockAchievement(AchievementService.PERFECT_CLEAR);
        
        assertTrue(achievementService.isUnlocked(AchievementService.PERFECT_CLEAR));
    }

    @Test
    public void testSpeedRunnerUnlock_With5Enemies() {
        // Simulate defeating 5 enemies
        ForestGameArea.NUM_ENEMIES_DEFEATED = 5;
        
        // Check condition and unlock
        if (ForestGameArea.NUM_ENEMIES_DEFEATED >= 5) {
            achievementService.unlockAchievement(AchievementService.SPEED_RUNNER);
        }
        
        assertTrue(achievementService.isUnlocked(AchievementService.SPEED_RUNNER));
    }

    @Test
    public void testSpeedRunnerNotUnlock_WithLessThan5Enemies() {
        // Simulate defeating only 4 enemies
        ForestGameArea.NUM_ENEMIES_DEFEATED = 4;
        
        // Check condition and unlock
        if (ForestGameArea.NUM_ENEMIES_DEFEATED >= 5) {
            achievementService.unlockAchievement(AchievementService.SPEED_RUNNER);
        }
        
        assertFalse(achievementService.isUnlocked(AchievementService.SPEED_RUNNER));
    }

    @Test
    public void testSlayerUnlock_With20Enemies() {
        // Simulate defeating 20 enemies
        ForestGameArea.NUM_ENEMIES_DEFEATED = 20;
        
        // Check condition and unlock
        if (ForestGameArea.NUM_ENEMIES_DEFEATED >= 20) {
            achievementService.unlockAchievement(AchievementService.SLAYER);
        }
        
        assertTrue(achievementService.isUnlocked(AchievementService.SLAYER));
    }

    @Test
    public void testSlayerNotUnlock_WithLessThan20Enemies() {
        // Simulate defeating only 19 enemies
        ForestGameArea.NUM_ENEMIES_DEFEATED = 19;
        
        // Check condition and unlock
        if (ForestGameArea.NUM_ENEMIES_DEFEATED >= 20) {
            achievementService.unlockAchievement(AchievementService.SLAYER);
        }
        
        assertFalse(achievementService.isUnlocked(AchievementService.SLAYER));
    }

    @Test
    public void testMultipleAchievementsUnlock_OnHighEnemyCount() {
        // Simulate defeating 25 enemies (should unlock both Speed Runner and Slayer)
        ForestGameArea.NUM_ENEMIES_DEFEATED = 25;
        
        // Check and unlock both
        if (ForestGameArea.NUM_ENEMIES_DEFEATED >= 5) {
            achievementService.unlockAchievement(AchievementService.SPEED_RUNNER);
        }
        
        if (ForestGameArea.NUM_ENEMIES_DEFEATED >= 20) {
            achievementService.unlockAchievement(AchievementService.SLAYER);
        }
        
        assertTrue(achievementService.isUnlocked(AchievementService.SPEED_RUNNER));
        assertTrue(achievementService.isUnlocked(AchievementService.SLAYER));
        assertEquals(2, achievementService.getUnlockedCount());
    }

    @Test
    public void testToughSurvivorUnlock_OnGameCompletion() {
        // Simulate completing the game
        achievementService.unlockAchievement(AchievementService.TOUGH_SURVIVOR);
        
        assertTrue(achievementService.isUnlocked(AchievementService.TOUGH_SURVIVOR));
    }

    @Test
    public void testVictoryUnlocksMultipleAchievements() {
        // Simulate a victory scenario with 15 enemies defeated
        ForestGameArea.NUM_ENEMIES_DEFEATED = 15;
        
        // Unlock victory-based achievements
        achievementService.unlockAchievement(AchievementService.PERFECT_CLEAR);
        achievementService.unlockAchievement(AchievementService.TOUGH_SURVIVOR);
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        
        // Unlock based on enemy count
        if (ForestGameArea.NUM_ENEMIES_DEFEATED >= 5) {
            achievementService.unlockAchievement(AchievementService.SPEED_RUNNER);
        }
        
        // Should have unlocked 4 achievements
        assertEquals(4, achievementService.getUnlockedCount());
        assertTrue(achievementService.isUnlocked(AchievementService.PERFECT_CLEAR));
        assertTrue(achievementService.isUnlocked(AchievementService.TOUGH_SURVIVOR));
        assertTrue(achievementService.isUnlocked(AchievementService.PARTICIPATION));
        assertTrue(achievementService.isUnlocked(AchievementService.SPEED_RUNNER));
        assertFalse(achievementService.isUnlocked(AchievementService.SLAYER));
    }
}

