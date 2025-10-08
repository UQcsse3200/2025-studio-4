package com.csse3200.game.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing player achievements.
 * Tracks which achievements have been unlocked and provides methods to unlock them.
 */
public class AchievementService {
    private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);
    
    // Achievement IDs
    public static final String TOUGH_SURVIVOR = "tough_survivor";
    public static final String SPEED_RUNNER = "speed_runner";
    public static final String SLAYER = "slayer";
    public static final String PERFECT_CLEAR = "perfect_clear";
    public static final String PARTICIPATION = "participation";
    
    // Map to track unlocked achievements
    private final Map<String, Boolean> unlockedAchievements;
    
    public AchievementService() {
        this.unlockedAchievements = new HashMap<>();
        
        // Initialize all achievements as locked
        unlockedAchievements.put(TOUGH_SURVIVOR, false);
        unlockedAchievements.put(SPEED_RUNNER, false);
        unlockedAchievements.put(SLAYER, false);
        unlockedAchievements.put(PERFECT_CLEAR, false);
        unlockedAchievements.put(PARTICIPATION, false);
        
        logger.info("AchievementService initialized");
    }
    
    /**
     * Unlock an achievement by ID
     * @param achievementId the ID of the achievement to unlock
     */
    public void unlockAchievement(String achievementId) {
        if (unlockedAchievements.containsKey(achievementId)) {
            if (!unlockedAchievements.get(achievementId)) {
                unlockedAchievements.put(achievementId, true);
                logger.info("Achievement unlocked: {}", achievementId);
            }
        } else {
            logger.warn("Unknown achievement ID: {}", achievementId);
        }
    }
    
    /**
     * Check if an achievement is unlocked
     * @param achievementId the ID of the achievement to check
     * @return true if unlocked, false otherwise
     */
    public boolean isUnlocked(String achievementId) {
        return unlockedAchievements.getOrDefault(achievementId, false);
    }
    
    /**
     * Get all achievement IDs
     * @return set of all achievement IDs
     */
    public Set<String> getAllAchievementIds() {
        return unlockedAchievements.keySet();
    }
    
    /**
     * Get the number of unlocked achievements
     * @return count of unlocked achievements
     */
    public int getUnlockedCount() {
        return (int) unlockedAchievements.values().stream().filter(unlocked -> unlocked).count();
    }
    
    /**
     * Get the total number of achievements
     * @return total count of achievements
     */
    public int getTotalCount() {
        return unlockedAchievements.size();
    }
    
    /**
     * Reset all achievements to locked state
     */
    public void resetAchievements() {
        unlockedAchievements.replaceAll((id, unlocked) -> false);
        logger.info("All achievements reset to locked state");
    }
}
