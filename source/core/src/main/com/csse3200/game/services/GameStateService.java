package com.csse3200.game.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that handles persistent state within the game runtime
 * Stores persistent data such as stars, unlocks, and level progress
 */
public class GameStateService {
    private static final Logger logger = LoggerFactory.getLogger(GameStateService.class);

    private int stars;

    public GameStateService() {
        // should load from save file later
        logger.info("Loading GameStateService");
        stars = 0;
    }

    /**
     * Gets the current number of stars
     * @return current number of stars
     */
    public int getStars() {
        return stars;
    }

    /**
     * Sets the current number of stars to the given number
     * @param newStars new number of stars
     */
    public void setStars(int newStars) {
        stars = newStars;
    }

    /**
     * Increments the current number of stars by the given number
     * @param increment the number of stars to increment by
     */
    public void updateStars(int increment) {
        stars += increment;
    }

    /**
     * Attempts to spend the given number of stars.
     *
     * @param amount number of stars to spend
     * @return true if the stars were successfully spent, false if not enough stars
     */
    public boolean spendStars(int amount) {
        if (amount < 0) {
            logger.warn("Tried to spend a negative amount of stars: {}", amount);
            return false;
        }
        if (stars >= amount) {
            stars -= amount;
            logger.info("Spent {} stars. Remaining: {}", amount, stars);
            return true;
        } else {
            logger.info("Not enough stars to spend {}. Current: {}", amount, stars);
            return false;
        }
    }

}
