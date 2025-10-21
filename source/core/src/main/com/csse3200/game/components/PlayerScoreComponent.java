package com.csse3200.game.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csse3200.game.services.ServiceLocator;

/**
 * Holds the player's points and notifies the HUD on change.
 */
public class PlayerScoreComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(PlayerScoreComponent.class);
    private int totalScore;

    public int getTotalScore() { 
        logger.debug("getTotalScore() called, returning: {}", totalScore);
        return this.totalScore; 
    }

    /**
     * Adds a non-negative amount of points to the total score and notifies the HUD on change.
     */
    public void addPoints(int points) {
        if (points <= 0) {          // only positive points allowed
            logger.debug("Ignoring non-positive points: {}", points);
            return;
        }
        int oldScore = totalScore;
        totalScore += points;
        logger.info("Added {} points to player score: {} -> {}", points, oldScore, totalScore);
        
        // 更新GameScoreService的缓存
        try {
            var scoreService = ServiceLocator.getGameScoreService();
            if (scoreService != null && scoreService.isGameInProgress()) {
                scoreService.setCurrentScore(totalScore);
            }
        } catch (Exception e) {
            logger.debug("Could not update GameScoreService cache: {}", e.getMessage());
        }
        
        if (entity != null) {
            entity.getEvents().trigger("updateScore", totalScore);
        }
    }

    /** Resets points to 0 and notifies HUD. */
    public void reset() {
        totalScore = 0;
        if (entity != null) {
            entity.getEvents().trigger("updateScore", totalScore);
        }
    }



}
