package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.services.leaderboard.LeaderboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing game saves and loading.
 * Handles saving and loading game state including player position, health, inventory, etc.
 */
public class SaveGameService {
    private static final Logger logger = LoggerFactory.getLogger(SaveGameService.class);
    private static final String SAVE_FILE_NAME = "game_save.json";
    private static final String SAVE_DIRECTORY = "saves";
    
    private final Json json;
    private final EntityService entityService;
    
    public SaveGameService(EntityService entityService) {
        this.entityService = entityService;
        this.json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
    }
    
    /**
     * Save the current game state to file with a custom name
     * @param saveName custom name for the save file (without .json extension)
     * @return true if save was successful, false otherwise
     */
    public boolean saveGame(String saveName) {
        try {
            GameSaveData saveData = createSaveData();
            String saveJson = json.toJson(saveData);
            
            // Create saves directory if it doesn't exist
            FileHandle savesDir = Gdx.files.local(SAVE_DIRECTORY);
            if (!savesDir.exists()) {
                savesDir.mkdirs();
            }
            
            // Use custom save name or default
            String fileName = (saveName != null && !saveName.trim().isEmpty()) 
                ? saveName.trim() + ".json" 
                : SAVE_FILE_NAME;
            
            FileHandle saveFile = Gdx.files.local(SAVE_DIRECTORY + "/" + fileName);
            saveFile.writeString(saveJson, false);
            
            logger.info("Game saved successfully to {}", saveFile.path());
            return true;
        } catch (Exception e) {
            logger.error("Failed to save game", e);
            return false;
        }
    }

    /**
     * Save the current game state to default file
     * @return true if save was successful, false otherwise
     */
    public boolean saveGame() {
        return saveGame(null);
    }
    
    /**
     * Load the game state from a specific save file
     * @param saveName name of the save file to load (without .json extension)
     * @return true if load was successful, false otherwise
     */
    public boolean loadGame(String saveName) {
        try {
            String fileName = saveName + ".json";
            
            // Try multiple possible save file locations
            FileHandle saveFile = null;
            
            // First try local directory
            saveFile = Gdx.files.local(SAVE_DIRECTORY + "/" + fileName);
            if (!saveFile.exists()) {
                // Try internal assets directory
                saveFile = Gdx.files.internal(SAVE_DIRECTORY + "/" + fileName);
            }
            if (!saveFile.exists()) {
                // Try relative path from current working directory
                saveFile = Gdx.files.absolute("./saves/" + fileName);
            }
            if (!saveFile.exists()) {
                // Try desktop build directory
                saveFile = Gdx.files.absolute("./desktop/build/resources/main/saves/" + fileName);
            }
            
            if (!saveFile.exists()) {
                logger.warn("Save file not found in any location: {}", fileName);
                return false;
            }
            
            String saveJson = saveFile.readString();
            GameSaveData saveData = json.fromJson(GameSaveData.class, saveJson);
            
            restoreGameState(saveData);
            logger.info("Game loaded successfully from {}", saveFile.path());
            return true;
        } catch (Exception e) {
            logger.error("Failed to load game from {}", saveName, e);
            return false;
        }
    }

    /**
     * Load the game state from default file
     * @return true if load was successful, false otherwise
     */
    public boolean loadGame() {
        return loadGame(SAVE_FILE_NAME.replace(".json", ""));
    }
    
    /**
     * Check if a save file exists
     * @return true if save file exists, false otherwise
     */
    public boolean hasSaveFile() {
        FileHandle saveFile = Gdx.files.local(SAVE_DIRECTORY + "/" + SAVE_FILE_NAME);
        return saveFile.exists();
    }
    
    /**
     * Create save data from current game state
     * @return GameSaveData object
     */
    private GameSaveData createSaveData() {
        GameSaveData saveData = new GameSaveData();
        
        // Find player entity
        Entity player = findPlayerEntity();
        if (player != null) {
            saveData.playerPosition = player.getPosition();
            saveData.playerHealth = getPlayerHealth(player);
            saveData.playerGold = getPlayerGold(player);
            saveData.timestamp = System.currentTimeMillis();
        }
        
        return saveData;
    }
    
    /**
     * Restore game state from save data
     * @param saveData the save data to restore from
     */
    private void restoreGameState(GameSaveData saveData) {
        // Find and remove existing player entity only (don't clear everything)
        Entity existingPlayer = findPlayerEntity();
        if (existingPlayer != null) {
            entityService.unregister(existingPlayer);
        }
        
        // Create new player at saved position
        Entity player = PlayerFactory.createPlayer();
        
        // Validate and adjust player position to be within game bounds
        Vector2 validPosition = validatePlayerPosition(saveData.playerPosition);
        player.setPosition(validPosition);
        
        // Restore player stats
        restorePlayerStats(player, saveData);
        
        // Register player
        entityService.register(player);
        
        // TODO: Restore other game entities (enemies, items, etc.)
        // Note: Terrain and other game elements will be preserved
        logger.info("Game state restored - Player at position: {}", saveData.playerPosition);
    }
    
    /**
     * Find the player entity in the current game
     * @return player entity or null if not found
     */
    private Entity findPlayerEntity() {
        // This is a simplified implementation
        // In a real game, you might want to tag entities or use a more sophisticated approach
        for (Entity entity : entityService.getEntities()) {
            if (entity.getComponent(com.csse3200.game.components.player.PlayerActions.class) != null) {
                return entity;
            }
        }
        return null;
    }
    
    /**
     * Get player health from entity
     * @param player the player entity
     * @return player health
     */
    private int getPlayerHealth(Entity player) {
        var combatStats = player.getComponent(com.csse3200.game.components.PlayerCombatStatsComponent.class);
        return combatStats != null ? combatStats.getHealth() : 100;
    }
    
    /**
     * Get player gold from entity
     * @param player the player entity
     * @return player gold
     */
    private int getPlayerGold(Entity player) {
        var inventory = player.getComponent(com.csse3200.game.components.player.InventoryComponent.class);
        return inventory != null ? inventory.getGold() : 0;
    }
    
    /**
     * Restore player stats from save data
     * @param player the player entity
     * @param saveData the save data
     */
    private void restorePlayerStats(Entity player, GameSaveData saveData) {
        var combatStats = player.getComponent(com.csse3200.game.components.PlayerCombatStatsComponent.class);
        if (combatStats != null) {
            combatStats.setHealth(saveData.playerHealth);
        }
        
        var inventory = player.getComponent(com.csse3200.game.components.player.InventoryComponent.class);
        if (inventory != null) {
            inventory.setGold(saveData.playerGold);
        }
    }
    
    /**
     * Validates and adjusts player position to be within valid game bounds
     * @param position the original position to validate
     * @return valid position within game bounds
     */
    private Vector2 validatePlayerPosition(Vector2 position) {
        // Define game area bounds (adjust these values based on your game)
        float minX = 0f;
        float maxX = 15f;
        float minY = 0f;
        float maxY = 15f;
        
        float x = Math.max(minX, Math.min(maxX, position.x));
        float y = Math.max(minY, Math.min(maxY, position.y));
        
        Vector2 validPosition = new Vector2(x, y);
        
        if (!validPosition.equals(position)) {
            logger.warn("Player position adjusted from {} to {} to stay within bounds", position, validPosition);
        }
        
        return validPosition;
    }

    /**
     * Clear existing entities from the game
     */
    private void clearExistingEntities() {
        // Get a copy of the entities list to avoid concurrent modification
        List<Entity> entitiesToRemove = new ArrayList<>();
        for (Entity entity : entityService.getEntities()) {
            entitiesToRemove.add(entity);
        }
        
        // Remove all entities
        for (Entity entity : entitiesToRemove) {
            entityService.unregister(entity);
        }
    }

    public void save(String fileName, List<LeaderboardService.LeaderboardEntry> entries) {
        try {
            String saveJson = json.toJson(entries, List.class);
            FileHandle savesDir = Gdx.files.local(SAVE_DIRECTORY);
            if (!savesDir.exists()) {
                savesDir.mkdirs();
            }
            FileHandle saveFile = Gdx.files.local(SAVE_DIRECTORY + "/" + fileName + ".json");
            saveFile.writeString(saveJson, false);
            logger.info("Leaderboard saved successfully to {}", saveFile.path());
        } catch (Exception e) {
            logger.error("Failed to save leaderboard", e);
        }
    }

    public <T> T load(String fileName, Class<T> type) {
        try {
            FileHandle saveFile = Gdx.files.local(SAVE_DIRECTORY + "/" + fileName + ".json");
            if (!saveFile.exists()) {
                logger.info("Leaderboard save file not found: {}", fileName);
                return null;
            }
            String saveJson = saveFile.readString();
            return json.fromJson(type, saveJson);
        } catch (Exception e) {
            logger.error("Failed to load leaderboard from {}", fileName, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<LeaderboardService.LeaderboardEntry> loadLeaderboardEntries(String fileName) {
        try {
            FileHandle saveFile = Gdx.files.local(SAVE_DIRECTORY + "/" + fileName + ".json");
            if (!saveFile.exists()) {
                logger.info("Leaderboard save file not found: {}", fileName);
                return null;
            }
            String saveJson = saveFile.readString();
            // Use fromJson with type parameters to handle generic list
            return json.fromJson(List.class, LeaderboardService.LeaderboardEntry.class, saveJson);
        } catch (Exception e) {
            logger.error("Failed to load leaderboard from {}", fileName, e);
            return null;
        }
    }

    /**
     * Data class for storing game save information
     */
    public static class GameSaveData {
        public Vector2 playerPosition = new Vector2(7.5f, 7.5f);
        public int playerHealth = 100;
        public int playerGold = 0;
        public long timestamp = 0;
        
        public GameSaveData() {}
    }
} 