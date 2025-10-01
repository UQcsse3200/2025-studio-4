package com.csse3200.game.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing player name throughout the game.
 * Stores and retrieves the player's name for use in leaderboards and other game features.
 */
public class PlayerNameService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerNameService.class);
    private static final String DEFAULT_PLAYER_NAME = "Player";
    
    private String playerName;
    
    public PlayerNameService() {
        this.playerName = DEFAULT_PLAYER_NAME;
        logger.debug("PlayerNameService initialized with default name: {}", playerName);
    }
    
    /**
     * Sets the player's name
     * @param name the player's name
     */
    public void setPlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Attempted to set empty or null player name, using default");
            this.playerName = DEFAULT_PLAYER_NAME;
        } else {
            this.playerName = name.trim();
            logger.info("Player name set to: {}", this.playerName);
        }
    }
    
    /**
     * Gets the current player's name
     * @return the player's name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Checks if the player name is the default name
     * @return true if using default name
     */
    public boolean isDefaultName() {
        return DEFAULT_PLAYER_NAME.equals(playerName);
    }
    
    /**
     * Resets the player name to default
     */
    public void resetToDefault() {
        this.playerName = DEFAULT_PLAYER_NAME;
        logger.debug("Player name reset to default: {}", playerName);
    }
}
