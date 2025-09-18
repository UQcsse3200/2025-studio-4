package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.files.FileLoader;
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
    private GameSaveData pendingRestoreData;
    
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
            
            // Create saves directory at project working dir to align with SaveSelection
            FileHandle savesDir = Gdx.files.absolute("./" + SAVE_DIRECTORY);
            if (!savesDir.exists()) {
                savesDir.mkdirs();
            }
            
            // Use custom save name or default
            String fileName = (saveName != null && !saveName.trim().isEmpty()) 
                ? saveName.trim() + ".json" 
                : SAVE_FILE_NAME;
            
            FileHandle saveFile = Gdx.files.absolute("./" + SAVE_DIRECTORY + "/" + fileName);
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
            
            // Try multiple possible save file locations (prefer working dir ./saves)
            FileHandle saveFile = Gdx.files.absolute("./" + SAVE_DIRECTORY + "/" + fileName);
            if (!saveFile.exists()) {
                // Try internal assets directory
                saveFile = Gdx.files.internal(SAVE_DIRECTORY + "/" + fileName);
            }
            if (!saveFile.exists()) {
                // Try local (platform-dependent) directory
                saveFile = Gdx.files.local(SAVE_DIRECTORY + "/" + fileName);
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
            logger.info("Loading save from {} ({} bytes)", saveFile.path(), saveJson != null ? saveJson.length() : 0);
            GameSaveData saveData = json.fromJson(GameSaveData.class, saveJson);

            // Defer actual restoration until after assets and game area are created
            this.pendingRestoreData = saveData;
            logger.info("Save data queued for restoration from {}", saveFile.path());
            return true;
        } catch (Exception e) {
            logger.error("Failed to load game from {}", saveName, e);
            return false;
        }
    }

    /**
     * Apply pending restoration if available. Should be called after game area/assets are ready.
     * @return true if restoration was applied, false otherwise
     */
    public boolean applyPendingRestore() {
        if (pendingRestoreData == null) {
            return false;
        }
        try {
            restoreGameState(pendingRestoreData);
            pendingRestoreData = null;
            return true;
        } catch (Exception e) {
            logger.error("Failed to apply pending game restoration", e);
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
        
        // Collect tower snapshots
        saveData.towers = collectTowerData();

        // Collect enemy snapshots
        saveData.enemies = collectEnemyData();

        logger.info("createSaveData: towers={}, enemies={}",
                saveData.towers != null ? saveData.towers.size() : -1,
                saveData.enemies != null ? saveData.enemies.size() : -1);

        return saveData;
    }
    
    /**
     * Restore game state from save data
     * @param saveData the save data to restore from
     */
    private void restoreGameState(GameSaveData saveData) {
        // Find existing player entity if present
        Entity player = findPlayerEntity();
        boolean createdNewPlayer = false;
        if (player == null) {
            // Create new player if none exists yet
            player = PlayerFactory.createPlayer();
            createdNewPlayer = true;
        }
        
        // Validate and adjust player position to be within game bounds
        Vector2 validPosition = validatePlayerPosition(saveData.playerPosition);
        player.setPosition(validPosition);
        
        // Restore player stats
        restorePlayerStats(player, saveData);
        
        // Register player only if newly created
        if (createdNewPlayer) {
            entityService.register(player);
            logger.info("restoreGameState: created and registered new player entity");
        } else {
            logger.info("restoreGameState: reused existing player entity");
        }
        
        // Remove existing towers/enemies and restore from save
        removeExistingTowersAndEnemies();

        // Restore towers
        if (saveData.towers != null) {
            for (GameSaveData.TowerData t : saveData.towers) {
                Entity tower = createTowerByType(t.type);
                if (tower != null) {
                    tower.setPosition(t.position);
                    var stats = tower.getComponent(com.csse3200.game.components.TowerStatsComponent.class);
                    if (stats != null) {
                        stats.setHealth(t.health);
                        stats.setAttackCooldown(t.attackCooldown);
                    }
                    entityService.register(tower);
                }
            }
        }

        // Restore enemies
        if (saveData.enemies != null) {
            for (GameSaveData.EnemyData e : saveData.enemies) {
                Entity enemy = createEnemyByType(e.type, player);
                if (enemy != null) {
                    enemy.setPosition(e.position);
                    var combat = enemy.getComponent(com.csse3200.game.components.CombatStatsComponent.class);
                    if (combat != null) {
                        combat.setHealth(e.health);
                    }
                    entityService.register(enemy);
                }
            }
        }

        logger.info("Game state restored - Player at position: {} ({} towers, {} enemies)", saveData.playerPosition,
                saveData.towers != null ? saveData.towers.size() : 0,
                saveData.enemies != null ? saveData.enemies.size() : 0);
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
    
    /**
     * Remove existing towers and enemies to avoid duplication when restoring from a save.
     */
    private void removeExistingTowersAndEnemies() {
        List<Entity> toRemove = new ArrayList<>();
        for (Entity entity : entityService.getEntities()) {
            if (entity.getComponent(com.csse3200.game.components.TowerComponent.class) != null) {
                toRemove.add(entity);
                continue;
            }
            if (entity.getComponent(com.csse3200.game.components.enemy.EnemyTypeComponent.class) != null) {
                toRemove.add(entity);
            }
        }
        for (Entity e : toRemove) {
            entityService.unregister(e);
        }
    }
    
    /**
     * Traverse entities and collect tower states
     */
    private List<GameSaveData.TowerData> collectTowerData() {
        List<GameSaveData.TowerData> towers = new ArrayList<>();
        for (Entity entity : entityService.getEntities()) {
            var tower = entity.getComponent(com.csse3200.game.components.TowerComponent.class);
            if (tower == null) continue;

            var stats = entity.getComponent(com.csse3200.game.components.TowerStatsComponent.class);
            GameSaveData.TowerData data = new GameSaveData.TowerData();
            data.type = tower.getType();
            data.width = tower.getWidth();
            data.height = tower.getHeight();
            data.position = entity.getPosition();
            if (stats != null) {
                data.health = stats.getHealth();
                data.damage = stats.getDamage();
                data.range = stats.getRange();
                data.attackCooldown = stats.getAttackCooldown();
                data.attackTimer = stats.getAttackTimer();
            }
            towers.add(data);
        }
        return towers;
    }

    /**
     * Traverse entities and collect enemy states (generic)
     */
    private List<GameSaveData.EnemyData> collectEnemyData() {
        List<GameSaveData.EnemyData> enemies = new ArrayList<>();
        for (Entity entity : entityService.getEntities()) {
            // Exclude towers
            if (entity.getComponent(com.csse3200.game.components.TowerComponent.class) != null) continue;
            // Exclude player
            if (entity.getComponent(com.csse3200.game.components.player.PlayerActions.class) != null) continue;

            var combat = entity.getComponent(com.csse3200.game.components.CombatStatsComponent.class);
            if (combat == null) continue;

            GameSaveData.EnemyData data = new GameSaveData.EnemyData();
            data.position = entity.getPosition();
            data.health = combat.getHealth();
            var enemyType = entity.getComponent(com.csse3200.game.components.enemy.EnemyTypeComponent.class);
            data.type = enemyType != null ? enemyType.getType() : "enemy";
            enemies.add(data);
        }
        return enemies;
    }
    
    /**
     * Data class for storing game save information
     */
    public static class GameSaveData {
        public Vector2 playerPosition = new Vector2(7.5f, 7.5f);
        public int playerHealth = 100;
        public int playerGold = 0;
        public long timestamp = 0;
        public List<TowerData> towers = new ArrayList<>();
        public List<EnemyData> enemies = new ArrayList<>();
        
        public GameSaveData() {}

        public static class TowerData {
            public String type;
            public int width;
            public int height;
            public Vector2 position;
            public int health;
            public float damage;
            public float range;
            public float attackCooldown;
            public float attackTimer;
        }

        public static class EnemyData {
            public String type; // concrete type for factory mapping
            public Vector2 position;
            public int health;
        }
    }

    private Entity createTowerByType(String type) {
        if (type == null) return null;
        // Use default currency type for restored towers
        com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType defaultCurrency = 
            com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType.METAL_SCRAP;
        
        switch (type) {
            case "bone":
                return com.csse3200.game.entities.factories.TowerFactory.createBoneTower(defaultCurrency);
            case "dino":
                return com.csse3200.game.entities.factories.TowerFactory.createDinoTower(defaultCurrency);
            case "cavemen":
                return com.csse3200.game.entities.factories.TowerFactory.createCavemenTower(defaultCurrency);
            default:
                return null;
        }
    }

    private Entity createEnemyByType(String type, Entity targetPlayer) {
        if (type == null) return null;
        
        // Use default values for restored enemies
        java.util.List<Entity> emptyWaypoints = new java.util.ArrayList<>();
        com.csse3200.game.utils.Difficulty defaultDifficulty = com.csse3200.game.utils.Difficulty.EASY;
        
        switch (type) {
            case "grunt":
                return com.csse3200.game.entities.factories.GruntEnemyFactory.createGruntEnemy(emptyWaypoints, targetPlayer, defaultDifficulty);
            case "drone":
                return com.csse3200.game.entities.factories.DroneEnemyFactory.createDroneEnemy(emptyWaypoints, targetPlayer, defaultDifficulty);
            case "tank":
                return com.csse3200.game.entities.factories.TankEnemyFactory.createTankEnemy(emptyWaypoints, targetPlayer, defaultDifficulty);
            case "boss":
                return com.csse3200.game.entities.factories.BossEnemyFactory.createBossEnemy(emptyWaypoints, targetPlayer, defaultDifficulty);
            case "divider_child":
                return com.csse3200.game.entities.factories.DividerChildEnemyFactory.createDividerChildChildEnemy(targetPlayer, emptyWaypoints, 0, defaultDifficulty);
            default:
                return null;
        }
    }
} 