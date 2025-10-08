package com.csse3200.game.components.mainmenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.AchievementService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Achievement functionality in Main Menu
 * Tests the interaction between MainMenuActions and AchievementService
 */
@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class MainMenuAchievementIntegrationTest {
    
    @Mock
    private GdxGame game;
    
    @Mock
    private ResourceService resourceService;
    
    private AchievementService achievementService;

    @BeforeEach
    public void setUp() {
        // Register services
        ServiceLocator.registerResourceService(resourceService);
        
        // Create and register achievement service
        achievementService = new AchievementService();
        ServiceLocator.registerAchievementService(achievementService);
    }

    @Test
    public void testAchievementServiceRegistered() {
        // Verify achievement service is properly registered
        assertNotNull(ServiceLocator.getAchievementService());
        assertEquals(achievementService, ServiceLocator.getAchievementService());
    }

    @Test
    public void testInitialAchievementState() {
        // All achievements should start locked
        assertEquals(0, achievementService.getUnlockedCount());
        assertEquals(5, achievementService.getTotalCount());
    }

    @Test
    public void testAchievementUnlockDuringGameplay() {
        // Simulate achievement unlocking during gameplay
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        achievementService.unlockAchievement(AchievementService.SPEED_RUNNER);
        
        // Verify achievements are unlocked
        assertEquals(2, achievementService.getUnlockedCount());
        assertTrue(achievementService.isUnlocked(AchievementService.PARTICIPATION));
        assertTrue(achievementService.isUnlocked(AchievementService.SPEED_RUNNER));
    }

    @Test
    public void testAllAchievementsCanBeUnlocked() {
        // Unlock all achievements
        achievementService.unlockAchievement(AchievementService.TOUGH_SURVIVOR);
        achievementService.unlockAchievement(AchievementService.SPEED_RUNNER);
        achievementService.unlockAchievement(AchievementService.SLAYER);
        achievementService.unlockAchievement(AchievementService.PERFECT_CLEAR);
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        
        // Verify all are unlocked
        assertEquals(5, achievementService.getUnlockedCount());
        assertEquals(achievementService.getTotalCount(), achievementService.getUnlockedCount());
    }

    @Test
    public void testAchievementPersistenceAcrossScreens() {
        // Unlock some achievements
        achievementService.unlockAchievement(AchievementService.PARTICIPATION);
        achievementService.unlockAchievement(AchievementService.PERFECT_CLEAR);
        
        // Verify they remain unlocked (simulating screen change)
        assertEquals(2, achievementService.getUnlockedCount());
        
        // Get service again (as if from another screen)
        AchievementService retrievedService = ServiceLocator.getAchievementService();
        
        // Should be the same instance
        assertSame(achievementService, retrievedService);
        assertEquals(2, retrievedService.getUnlockedCount());
    }

    @Test
    public void testAchievementIdsAreConstant() {
        // Verify achievement ID constants are defined
        assertNotNull(AchievementService.TOUGH_SURVIVOR);
        assertNotNull(AchievementService.SPEED_RUNNER);
        assertNotNull(AchievementService.SLAYER);
        assertNotNull(AchievementService.PERFECT_CLEAR);
        assertNotNull(AchievementService.PARTICIPATION);
        
        // Verify they are unique
        assertEquals("tough_survivor", AchievementService.TOUGH_SURVIVOR);
        assertEquals("speed_runner", AchievementService.SPEED_RUNNER);
        assertEquals("slayer", AchievementService.SLAYER);
        assertEquals("perfect_clear", AchievementService.PERFECT_CLEAR);
        assertEquals("participation", AchievementService.PARTICIPATION);
    }
}

