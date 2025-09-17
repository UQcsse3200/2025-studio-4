package com.csse3200.game.services;

/**
 * Service for managing player name throughout the game session.
 * Stores and provides access to the current player's name.
 */
public class PlayerNameService {
    private String playerName;
    private static final String DEFAULT_NAME = "Player";
    
    public PlayerNameService() {
        this.playerName = DEFAULT_NAME;
    }
    
    public PlayerNameService(String playerName) {
        this.playerName = playerName != null && !playerName.trim().isEmpty() 
            ? playerName.trim() : DEFAULT_NAME;
    }
    
    /**
     * Set the player's name
     * @param playerName the name to set
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName != null && !playerName.trim().isEmpty() 
            ? playerName.trim() : DEFAULT_NAME;
    }
    
    /**
     * Get the current player's name
     * @return the player's name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Check if a custom name has been set (not the default)
     * @return true if a custom name is set, false if using default
     */
    public boolean hasCustomName() {
        return !DEFAULT_NAME.equals(playerName);
    }
    
    /**
     * Reset to default name
     */
    public void resetToDefault() {
        this.playerName = DEFAULT_NAME;
    }
}
