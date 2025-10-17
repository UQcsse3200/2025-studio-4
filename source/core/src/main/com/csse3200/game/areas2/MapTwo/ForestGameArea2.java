package com.csse3200.game.areas2.MapTwo;

import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.components.HealthBarComponent;
import com.csse3200.game.components.hero.HeroUpgradeComponent;
import com.csse3200.game.components.maingame.TowerUpgradeMenu;
import com.csse3200.game.utils.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas2.terrainTwo.TerrainFactory2;
import com.csse3200.game.areas2.terrainTwo.TerrainFactory2.TerrainType;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.wavesystem.Wave;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.maingame.MainGameWin;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.entities.configs.HeroConfig2;
import com.csse3200.game.entities.configs.HeroConfig3;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.rendering.Renderer;
import com.badlogic.gdx.graphics.Camera;

import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.ArrayList;
import java.util.List;


import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.maingame.SimplePlacementController;
import com.csse3200.game.components.CameraZoomDragComponent;
import com.csse3200.game.areas.IMapEditor;


/**
 * Forest area for the demo game with trees, a player, and some enemies.
 */
public class ForestGameArea2 extends GameArea2 {
    private static final Logger logger = LoggerFactory.getLogger(ForestGameArea2.class);

    // Wave management
    private List<Wave> waves;
    private int currentWaveIndex = 0;
    private Wave.WaveSpawnCallbacks spawnCallbacks;
    
    public static int NUM_ENEMIES_TOTAL = 0;
    public static int NUM_ENEMIES_DEFEATED = 0;

    private Timer.Task waveSpawnTask;
    private List<Runnable> enemySpawnQueue;
    private boolean waveInProgress = false;
    private boolean wavePaused = false;  // Track if wave spawning is paused
    private float spawnDelay = 2f; // Delay between spawns (updated per wave)
    private float adjustedSpawnDelay = 2f; // The actual delay being used (accounting for time scale)
    private long spawnScheduledTime = 0; // When the current spawn was scheduled (in milliseconds)
    private float timeRemainingWhenPaused = 0f; // Time remaining when paused
    private List<List<Entity>> waypointLists;

    public static Difficulty gameDifficulty = Difficulty.EASY;

    public static ForestGameArea2 currentGameArea;

    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(5, 34);
    private static final float WALL_WIDTH = 0.1f;


    private static final String[] forestTextureAtlases = {
            "images/grunt_basic_spritesheet.atlas", "images/drone_basic_spritesheet.atlas", "images/tank_basic_spritesheet.atlas",
            "images/boss_basic_spritesheet.atlas"
    };

    private static final String[] forestSounds = {
            "sounds/homebase_hit_sound.mp3"
    };
    private static final String backgroundMusic = "sounds/new_menutheme.mp3";
    private static final String[] forestMusic = {backgroundMusic};

    private final TerrainFactory2 terrainFactory;
    private Entity player;
    private boolean hasExistingPlayer = false;
    private IMapEditor mapEditor; // Use interface for compatibility

    // One-time prompt: Has this been displayed?
    private boolean heroHintShown = false;

    // Obstacle Coordinate Single Fact Source: Defined by the GameArea
    // create barriers areas
    private static final int[][] BARRIER_COORDS = new int[][]{

    };

    // create snowtree areas - 避开路径坐标
    private static final int[][] SNOWTREE_COORDS = new int[][]{
            {15, 9}
    };

    /**
     * Initialise this ForestGameArea2 to use the provided TerrainFactory2.
     *
     * @param terrainFactory TerrainFactory2 used to create the terrain for the GameArea.
     * @requires terrainFactory != null
     */
    public ForestGameArea2(TerrainFactory2 terrainFactory) {
        super();
        this.terrainFactory = terrainFactory;
    }

    /**
     * Set whether this game area already has an existing player entity.
     *
     * @param hasExistingPlayer true if player already exists, false otherwise
     */
    public void setHasExistingPlayer(boolean hasExistingPlayer) {
        this.hasExistingPlayer = hasExistingPlayer;
    }

    private void initializeWaypointLists() {
        waypointLists = new ArrayList<>();
        waypointLists.add(MapEditor2.waypointList);
        waypointLists.add(MapEditor2.waypointList2);
    }

    private void initializeWaves() {
        // Initialize waypoint lists first
        initializeWaypointLists();
        
        // Initialize spawn callbacks
        initializeSpawnCallbacks();
        
        waves = new ArrayList<>();
        
        // Wave 1: Basic introduction
        waves.add(new Wave(1, 6, 6, 0, 0, 0, 0, 2.0f, waypointLists));

        // Wave 2: Introduce speeders
        waves.add(new Wave(2, 16, 6, 2, 0, 0, 3, 1.5f, waypointLists));

        // Wave 3: More tanks and speeders
        waves.add(new Wave(3, 6, 6, 10, 0, 0, 4, 1.0f, waypointLists));

        // Wave 4: Dividers appear
        waves.add(new Wave(4, 10, 10, 6, 0, 2, 3, 0.75f, waypointLists));

        // Wave 5: Final challenge
        waves.add(new Wave(5, 20, 20, 10, 1, 2, 5, 0.5f, waypointLists));

        // Wave 6: Ultimate test
        waves.add(new Wave(6, 30, 20, 16, 2, 4, 8, 0.5f, waypointLists));
    }

    private void initializeSpawnCallbacks() {
        spawnCallbacks = new Wave.WaveSpawnCallbacks(
            this::spawnDrone,
            this::spawnGrunt,
            this::spawnTank,
            this::spawnBoss,
            this::spawnDivider,
            this::spawnSpeeder
        );
    }

    /**
     * Initialize and start the enemy wave spawning
     */
    private void startEnemyWave() {
        if (waveInProgress) return;
        
        waveInProgress = true;
        wavePaused = false;
        timeRemainingWhenPaused = 0f;
        buildSpawnQueue();
        scheduleNextEnemySpawn();
    }

    /**
     * Build the queue of enemies to spawn based on current wave
     */
    private void buildSpawnQueue() {
        if (currentWaveIndex >= waves.size()) {
            logger.info("All waves completed!");
            return;
        }
        
        Wave currentWave = waves.get(currentWaveIndex);
        NUM_ENEMIES_TOTAL = currentWave.getTotalEnemies();
        NUM_ENEMIES_DEFEATED = 0;
        spawnDelay = currentWave.getSpawnDelay();

        ForestGameArea.NUM_ENEMIES_TOTAL = NUM_ENEMIES_TOTAL;
        ForestGameArea.NUM_ENEMIES_DEFEATED = 0;
        
        enemySpawnQueue = currentWave.buildSpawnQueue(spawnCallbacks);
        logger.info("Starting Wave {} with {} enemies", currentWave.getWaveNumber(), NUM_ENEMIES_TOTAL);
    }

    /**
     * Called when all enemies in the current wave have been defeated.
     */
    public void onWaveDefeated() {
        logger.info("Wave {} defeated!", currentWaveIndex + 1);
        
        // Check if this was the final wave
        if (currentWaveIndex + 1 >= waves.size()) {
            // All waves complete - trigger victory!
            logger.info("All waves completed! Victory!");
            if (MainGameScreen.ui != null) {
                MainGameWin winComponent = MainGameScreen.ui.getComponent(MainGameWin.class);
                if (winComponent != null) {
                    winComponent.addActors();
                }
            }
        } else {
            // Start next wave after delay
            currentWaveIndex++;
            // Get current time scale, but don't divide by zero
            float timeScale = ServiceLocator.getTimeSource().getTimeScale();
            float adjustedDelay = timeScale > 0 ? 3.0f / timeScale : 3.0f;
            
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    startEnemyWave();
                }
            }, adjustedDelay);
        }
    }

    /**
     * Pause wave spawning (called when game is paused)
     */
    private void pauseWaveSpawning() {
        if (waveInProgress && !wavePaused) {
            wavePaused = true;
            
            // Calculate how much time has elapsed since the spawn was scheduled
            if (waveSpawnTask != null && spawnScheduledTime > 0) {
                long currentTime = TimeUtils.millis();
                float elapsedSeconds = (currentTime - spawnScheduledTime) / 1000f;
                
                // Calculate remaining time
                timeRemainingWhenPaused = Math.max(0f, adjustedSpawnDelay - elapsedSeconds);
                
                logger.info("Wave spawning paused. Elapsed: {}s, Remaining: {}s", 
                           elapsedSeconds, timeRemainingWhenPaused);
                
                // Cancel the current task
                waveSpawnTask.cancel();
                waveSpawnTask = null;
            }
        }
    }

    /**
     * Resume wave spawning (called when game is resumed)
     */
    private void resumeWaveSpawning() {
        if (waveInProgress && wavePaused) {
            wavePaused = false;
            logger.info("Wave spawning resumed with {}s remaining", timeRemainingWhenPaused);
            scheduleNextEnemySpawn();
        }
    }

    /**
     * Schedule the next enemy spawn with delay (with comprehensive safety checks)
     */
    private void scheduleNextEnemySpawn() {
        // Don't schedule if paused
        if (wavePaused) {
            logger.debug("Wave is paused, not scheduling next spawn");
            return;
        }

        // Safety check: ensure this game area is still the active one
        if (currentGameArea != this) {
            logger.info("Game area changed, stopping wave spawning");
            forceStopWave();
            return;
        }
        
        // Safety check: ensure services are still available
        try {
            if (ServiceLocator.getPhysicsService() == null || 
                ServiceLocator.getEntityService() == null ||
                ServiceLocator.getResourceService() == null) {
                logger.warn("Services not available, stopping wave spawning");
                forceStopWave();
                return;
            }
        } catch (Exception e) {
            logger.warn("Error checking services, stopping wave spawning: {}", e.getMessage());
            forceStopWave();
            return;
        }
        
        if (enemySpawnQueue == null || enemySpawnQueue.isEmpty()) {
            // Spawning complete, but enemies still alive
            waveInProgress = false;
            logger.info("Wave {} spawning completed", currentWaveIndex + 1);
            return;
        }
        
        // Cancel any existing spawn task
        if (waveSpawnTask != null) {
            waveSpawnTask.cancel();
        }
        
        // Get time scale and handle pause (time scale = 0)
        float timeScale = ServiceLocator.getTimeSource().getTimeScale();
        if (timeScale <= 0) {
            // Game is paused, don't schedule
            wavePaused = true;
            logger.debug("Time scale is 0, wave spawning paused");
            return;
        }
        
        // Determine the delay to use
        float delayToUse;
        if (timeRemainingWhenPaused > 0f) {
            // We're resuming from a pause - use the remaining time, adjusted for current time scale
            delayToUse = timeRemainingWhenPaused / timeScale;
            logger.debug("Resuming spawn with remaining time: {}s (adjusted: {}s)", 
                        timeRemainingWhenPaused, delayToUse);
            timeRemainingWhenPaused = 0f; // Reset for next spawn
        } else {
            // Normal spawn - use full delay adjusted for time scale
            delayToUse = spawnDelay / timeScale;
        }
        
        // Store the adjusted delay and schedule time for pause calculations
        adjustedSpawnDelay = delayToUse;
        spawnScheduledTime = TimeUtils.millis();

        // Schedule next spawn
        waveSpawnTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Check if paused before spawning
                if (wavePaused) {
                    logger.debug("Wave paused, skipping spawn");
                    return;
                }
                
                // Double-check we're still the active game area
                if (currentGameArea != ForestGameArea2.this) {
                    logger.info("Game area changed during spawn, stopping");
                    return;
                }
                
                // Double-check services are still available when task runs
                try {
                    if (ServiceLocator.getPhysicsService() == null || 
                        ServiceLocator.getEntityService() == null) {
                        logger.warn("Services disposed during spawn, stopping wave");
                        forceStopWave();
                        return;
                    }
                    
                    if (enemySpawnQueue != null && !enemySpawnQueue.isEmpty()) {
                        // Spawn the next enemy
                        Runnable spawnAction = enemySpawnQueue.remove(0);
                        spawnAction.run();
                        
                        // Schedule the next one
                        scheduleNextEnemySpawn();
                    }
                } catch (Exception e) {
                    logger.error("Error spawning enemy: {}", e.getMessage());
                    forceStopWave();
                }
            }
        }, delayToUse);
    }

    /**
     * Create the game area, including terrain, static entities (trees), dynamic entities (player)
     */
    @Override
    public void create() {
        // 停止主菜单音乐
        if (ServiceLocator.getAudioService() != null) {
            ServiceLocator.getAudioService().stopMusic();
            logger.info("主菜单音乐已停止");
        }
        
        // Load assets (textures, sounds, etc.) before creating anything that needs them
        loadAssets();
        registerForCleanup();


        // Create the main UI entity that will handle area info, hotbar, and tower placement
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Box Forest")); // Shows the game area's name
        
        // 添加防御塔列表组件，但初始隐藏（如果是新游戏）
        com.csse3200.game.components.maingame.TowerHotbarDisplay towerHotbar = new com.csse3200.game.components.maingame.TowerHotbarDisplay();
        if (!hasExistingPlayer) {
            towerHotbar.setVisible(false); // 新游戏时隐藏，对话结束后显示
        }
        ui.addComponent(towerHotbar);
        
        ui.addComponent(new com.csse3200.game.components.maingame.MainGameWin());

        SimplePlacementController placementController = new SimplePlacementController();
        ui.addComponent(placementController); // Handles user input for tower placement
        spawnEntity(ui);

        if (MainGameScreen.ui != null) {
            MainGameScreen.ui.getEvents().addListener("startWave", this::startWaves);
        }

        // Create camera control entity for zoom and drag functionality
        Entity cameraControl = new Entity();
        cameraControl.addComponent(new CameraZoomDragComponent());
        spawnEntity(cameraControl);
        
        // Create background entity (renders behind everything)
        Entity background = new Entity();
        background.addComponent(new com.csse3200.game.rendering.BackgroundRenderComponent("images/game background.jpg"));
        background.setPosition(0, 0); // Set position at origin
        spawnEntity(background);
        
        // 设置areas2的默认缩放为1.8f
        setCameraZoom(1.69f);
        
        // 模拟向上移动三格
        moveUpThreeSteps();
        
        // 模拟向右移动三格
        moveRightThreeSteps();

        spawnTerrain();

        // Only spawn new player if one doesn't already exist
        if (!hasExistingPlayer) {
            player = spawnPlayer();
        } else {
            // Find existing player entity
            player = findExistingPlayer();
            if (player == null) {
                logger.warn("Expected existing player not found, creating new one");
                player = spawnPlayer();
            } else {
                if (player.getComponent(CurrencyManagerComponent.class) == null) {
                    player.addComponent(new CurrencyManagerComponent());
                }
            }
        }

        // ✅ Now that mapEditor is created in spawnPlayer, link it to placementController
        if (mapEditor != null) {
            placementController.setMapEditor(mapEditor);
        }

        registerBarrierAndSpawn(BARRIER_COORDS);
        registerSnowTreeAndSpawn(SNOWTREE_COORDS);
        placementController.refreshInvalidTiles();
        spawnSlowZoneEffects();

        // Register pause/resume listeners for wave system
        Entity pauseListener = new Entity();
        pauseListener.getEvents().addListener("gamePaused", this::pauseWaveSpawning);
        pauseListener.getEvents().addListener("gameResumed", this::resumeWaveSpawning);
        spawnEntity(pauseListener);

        // Generate biomes & placeable areas
        //mapEditor.generateBiomesAndRivers();

        // Tower placement highlighter
        // Note: MapHighlighter expects TerrainComponent, but we have TerrainComponent2
        // We need to create a compatible version or cast - for now, comment out
        // MapHighlighter mapHighlighter =
        //         new MapHighlighter(terrain, placementController, new com.csse3200.game.entities.factories.TowerFactory());
        // Entity highlighterEntity = new Entity().addComponent(mapHighlighter);

        // spawnEntity(highlighterEntity);

        //Tower Upgrade Menu
        TowerUpgradeMenu towerUpgradeMenu = new TowerUpgradeMenu();
        Entity upgradeUI = new Entity().addComponent(towerUpgradeMenu);
        spawnEntity(upgradeUI);

        // --- ADD: MapHighlighter for tower placement preview ---
        com.csse3200.game.components.maingame.MapHighlighter mapHighlighter =
            new com.csse3200.game.components.maingame.MapHighlighter(
                terrain, // TerrainComponent2 implements ITerrainComponent
                placementController,
                new com.csse3200.game.entities.factories.TowerFactory()
            );
        Entity highlighterEntity = new Entity().addComponent(mapHighlighter);
        spawnEntity(highlighterEntity);

        //Link the upgrade menu to the map highlighter
        mapHighlighter.setTowerUpgradeMenu(towerUpgradeMenu);

        if (!hasExistingPlayer) {
            spawnIntroDialogue();
        } else {
            // 如果已有玩家（从存档加载），直接播放音乐
            playMusic();
        }

        // Add hero placement system
        // Note: HeroPlacementComponent expects TerrainComponent and MapEditor, but we have TerrainComponent2 and MapEditor2
        // We need to create a compatible version - for now, comment out
        // Entity placement = new Entity().addComponent(new HeroPlacementComponent(terrain,mapEditor, this::spawnHeroAt));
        // spawnEntity(placement);

        HeroConfig cfg1 = new HeroConfig();
        cfg1.heroTexture = "images/hero/Heroshoot.png";
        cfg1.bulletTexture = "images/hero/Bullet.png";

        HeroConfig2 cfg2 = new HeroConfig2();
        cfg2.heroTexture = "images/hero2/Heroshoot.png";
        cfg2.bulletTexture = "images/hero2/Bullet.png";

        HeroConfig3 cfg3 = new HeroConfig3();
        cfg3.heroTexture = "images/hero3/Heroshoot.png";
        cfg3.bulletTexture = "images/hero3/Bullet.png";

        // 2) 挂载"一次性换肤"组件（不会改变你其它逻辑）
        Entity skinSwitcher = new Entity().addComponent(
                new com.csse3200.game.components.hero.HeroOneShotFormSwitchComponent(cfg1, cfg2, cfg3)
        );
        com.csse3200.game.services.ServiceLocator.getEntityService().
                register(skinSwitcher);

        // --- ADD: MapHighlighter for tower placement preview ---
        com.csse3200.game.components.maingame.MapHighlighter mapHighlighter2 =
            new com.csse3200.game.components.maingame.MapHighlighter(
                terrain, // TerrainComponent2 implements ITerrainComponent
                placementController,
                new com.csse3200.game.entities.factories.TowerFactory()
            );
        Entity highlighterEntity2 = new Entity().addComponent(mapHighlighter2);
        spawnEntity(highlighterEntity2);

        //Tower Upgrade Menu
        TowerUpgradeMenu towerUpgradeMenu2 = new TowerUpgradeMenu();
        Entity upgradeUI2 = new Entity().addComponent(towerUpgradeMenu2);
        spawnEntity(upgradeUI2);

        //Link the upgrade menu to the map highlighter
        mapHighlighter2.setTowerUpgradeMenu(towerUpgradeMenu2);
    }

    private void spawnTerrain() {
        terrain = terrainFactory.createTerrain(TerrainType.MAP_TWO);
        spawnEntity(new Entity().addComponent(terrain));
        // Create boundary walls
        createBoundaryWalls();
    }

    private void createBoundaryWalls() {
        float tileSize = terrain.getTileSize();
        GridPoint2 tileBounds = terrain.getMapBounds(0);
        Vector2 worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

        // Left
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y),
                GridPoint2Utils.ZERO, false, false);
        // Right
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y),
                new GridPoint2(tileBounds.x, 0),
                false,
                false);
        // Top
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH),
                new GridPoint2(0, tileBounds.y),
                false,
                false);
        // Bottom
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH),
                GridPoint2Utils.ZERO, false, false);
    }


//Register to MapEditor's invalidTiles and generate obstacles on the map.
    private void registerBarrierAndSpawn(int[][] coords) {
        if (coords == null) return;
        for (int[] p : coords) {
            if (p == null || p.length != 2) continue;
            spawnEntityAt(ObstacleFactory.createBarrier(), new GridPoint2(p[0], p[1]), true, false);
        }
        if (mapEditor != null) {
            mapEditor.registerBarrierCoords(coords);
        }
    }

    private void registerSnowTreeAndSpawn(int[][] coords) {
        if (coords == null) return;
        for (int[] p : coords) {
            if (p == null || p.length != 2) continue;
            spawnEntityAt(ObstacleFactory.createSnowTree(), new GridPoint2(p[0], p[1]), true, false);
        }
        if (mapEditor != null) {
            mapEditor.registerSnowTreeCoords(coords);
        }
    }

    private void spawnSlowZoneEffects() {
        if (mapEditor == null || terrain == null) {
            return;
        }
        float tileSize = terrain.getTileSize();
        java.util.Set<String> spawned = new java.util.HashSet<>();
        for (GridPoint2 tile : mapEditor.getSlowZoneTiles()) {
            Entity effect = com.csse3200.game.entities.factories.SlowZoneEffectFactory.create(tileSize);
            spawnEntityAt(effect, tile, false, false);
            spawned.add(tile.x + "," + tile.y);
        }
        java.util.List<GridPoint2> manualTiles = java.util.Arrays.asList(
                new GridPoint2(12, 12),
                new GridPoint2(5, 6)
        );
        for (GridPoint2 tile : manualTiles) {
            String key = tile.x + "," + tile.y;
            if (spawned.contains(key)) {
                continue;
            }
            Entity effect = com.csse3200.game.entities.factories.SlowZoneEffectFactory.create(tileSize);
            spawnEntityAt(effect, tile, false, false);
        }
    }

    private Entity spawnPlayer() {
        // Map2 使用更大的homebase缩放 (3f) 和更大的血条
        HealthBarComponent healthBar = new HealthBarComponent(2.5f, 0.25f, 1.3f);
        Entity newPlayer = PlayerFactory.createPlayer("images/homebase2.png", 3f, healthBar);
        // 确保新玩家有钱包组件
        if (newPlayer.getComponent(CurrencyManagerComponent.class) == null) {
            newPlayer.addComponent(new CurrencyManagerComponent(/* 可选初始值 */));
        }

        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);

        // Initialize MapEditor2 as IMapEditor
        mapEditor = new MapEditor2(terrain, newPlayer);
        mapEditor.generateEnemyPath(); // Generate fixed enemy path

        return newPlayer;
    }

    private Entity findExistingPlayer() {
        for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
            if (entity.getComponent(com.csse3200.game.components.player.PlayerActions.class) != null) {
                return entity;
            }
        }
        return null;
    }

    public void startWaves() {
        initializeWaves();
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                startEnemyWave();
            }
        }, 2.0f); // Start wave after 2 seconds
    }

    /**
     * Expose current waypoint list generated by the map editor, for save/load rebinding.
     */
    public java.util.List<Entity> getWaypointList() {
        return mapEditor != null ? mapEditor.getWaypointList() : java.util.Collections.emptyList();
    }

    private void spawnDrone(List<Entity> waypoints) {
        GridPoint2 spawnPos = getSpawnPosition(waypoints);
        Entity drone = DroneEnemyFactory.createDroneEnemy(waypoints, player, gameDifficulty);
        spawnEntityAt(drone, spawnPos, true, true);
        logger.debug("Spawned drone at {}. Total enemies: {}", spawnPos, NUM_ENEMIES_TOTAL);
    }

    private void spawnGrunt(List<Entity> waypoints) {
        GridPoint2 spawnPos = getSpawnPosition(waypoints);
        Entity grunt = GruntEnemyFactory.createGruntEnemy(waypoints, player, gameDifficulty);
        spawnEntityAt(grunt, spawnPos, true, true);
        logger.debug("Spawned grunt at {}. Total enemies: {}", spawnPos, NUM_ENEMIES_TOTAL);
    }

    private void spawnTank(List<Entity> waypoints) {
        GridPoint2 spawnPos = getSpawnPosition(waypoints);
        Entity tank = TankEnemyFactory.createTankEnemy(waypoints, player, gameDifficulty);
        spawnEntityAt(tank, spawnPos, true, true);
        logger.debug("Spawned tank at {}. Total enemies: {}", spawnPos, NUM_ENEMIES_TOTAL);
    }

    private void spawnBoss(List<Entity> waypoints) {
        GridPoint2 spawnPos = getSpawnPosition(waypoints);
        Entity boss = BossEnemyFactory.createBossEnemy(waypoints, player, gameDifficulty);
        spawnEntityAt(boss, spawnPos, true, true);
        logger.debug("Spawned boss at {}. Total enemies: {}", spawnPos, NUM_ENEMIES_TOTAL);
    }

    private void spawnDivider(List<Entity> waypoints) {
        GridPoint2 spawnPos = getSpawnPosition(waypoints);
        Entity divider = DividerEnemyFactoryLevel2.createDividerEnemy(waypoints, this, player, gameDifficulty);
        spawnEntityAt(divider, spawnPos, true, true);
        logger.debug("Spawned divider at {}. Total enemies: {}", spawnPos, NUM_ENEMIES_TOTAL);
    }

    private void spawnSpeeder(List<Entity> waypoints) {
        GridPoint2 spawnPos = getSpawnPosition(waypoints);
        Entity speeder = SpeederEnemyFactory.createSpeederEnemy(waypoints, player, gameDifficulty);
        spawnEntityAt(speeder, spawnPos, true, true);
        logger.debug("Spawned speeder at {}. Total enemies: {}", spawnPos, NUM_ENEMIES_TOTAL);
    }

    /**
     * Helper method to get spawn position from the first waypoint in the list
     */
    private GridPoint2 getSpawnPosition(List<Entity> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) {
            logger.warn("No waypoints provided, using default spawn position");
            return new GridPoint2(0, 0);
        }
        // Get position from the first waypoint entity
        Entity firstWaypoint = waypoints.get(0);
        Vector2 pos = firstWaypoint.getPosition();
        return new GridPoint2((int) pos.x * 2, (int) pos.y * 2);
    }

    public static void checkEnemyCount() {
        if (NUM_ENEMIES_DEFEATED >= NUM_ENEMIES_TOTAL) {
            // Wave complete - let onWaveDefeated handle progression
            if (currentGameArea != null) {
                currentGameArea.onWaveDefeated();
            }
        }
    }

    private void spawnHeroAt(GridPoint2 cell) {

        HeroConfig heroCfg = FileLoader.readClass(HeroConfig.class, "configs/hero.json");
        if (heroCfg == null) {
            logger.warn("Failed to load configs/hero.json, using default HeroConfig.");
            heroCfg = new HeroConfig();
        }

        HeroConfig2 heroCfg2 = FileLoader.readClass(HeroConfig2.class, "configs/hero2.json");
        if (heroCfg2 == null) {
            logger.warn("Failed to load configs/hero2.json, using default HeroConfig.");
            heroCfg2 = new HeroConfig2();
        }

        HeroConfig3 heroCfg3 = FileLoader.readClass(HeroConfig3.class, "configs/hero3.json");
        if (heroCfg3 == null) {
            logger.warn("Failed to load configs/hero.json, using default HeroConfig.");
            heroCfg3 = new HeroConfig3();
        }

        ResourceService rs = ServiceLocator.getResourceService();
        HeroFactory.loadAssets(rs, heroCfg, heroCfg2, heroCfg3);
        while (!rs.loadForMillis(10)) {
            logger.info("Loading hero assets... {}%", rs.getProgress());
        }

        Camera cam = Renderer.getCurrentRenderer().getCamera().getCamera();
        Entity hero = HeroFactory.createHero(heroCfg, cam);

        var up = hero.getComponent(HeroUpgradeComponent.class);
        if (up != null) {
            up.attachPlayer(player);
        }

        hero.addComponent(new com.csse3200.game.components.hero.HeroClickableComponent(0.8f));

        spawnEntityAt(hero, cell, true, true);

        // 放置完成后的一次性提示窗口
        if (!heroHintShown) {
            com.badlogic.gdx.scenes.scene2d.Stage stage = ServiceLocator.getRenderService().getStage();
            new com.csse3200.game.ui.HeroHintDialog(hero).showOnceOn(stage);
            heroHintShown = true;
        }
    }

    private void spawnIntroDialogue() {
        // 使用 DialogueConfig 获取地图2的对话脚本
        java.util.List<com.csse3200.game.components.maingame.IntroDialogueComponent.DialogueEntry> script =
                com.csse3200.game.components.maingame.DialogueConfig.getMap2Dialogue();

        Entity dialogueEntity = new Entity().addComponent(
                new com.csse3200.game.components.maingame.IntroDialogueComponent(
                        script,
                        () -> {
                            // 对话结束后显示防御塔列表和播放背景音乐
                            showTowerUI();
                            playMusic();
                            
                            if (MainGameScreen.ui != null) {
                                MainGameScreen.ui.getEvents().trigger("startWave");
                            } else {
                                startWaves();
                            }
                        })
        );
        spawnEntity(dialogueEntity);
    }

    /**
     * 显示防御塔UI（在对话结束后调用）
     */
    private void showTowerUI() {
        // 找到主UI实体并显示防御塔列表组件
        for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
            com.csse3200.game.components.maingame.TowerHotbarDisplay towerUI = entity.getComponent(com.csse3200.game.components.maingame.TowerHotbarDisplay.class);
            if (towerUI != null) {
                towerUI.setVisible(true);
                logger.info("防御塔列表已显示");
                break;
            }
        }
    }

    private void playMusic() {
        // Route all music through AudioService to avoid overlaps across screens
        if (ServiceLocator.getAudioService() != null) {
            ServiceLocator.getAudioService().registerMusic("new_menutheme", backgroundMusic);
            ServiceLocator.getAudioService().playMusic("new_menutheme", true);
            ServiceLocator.getAudioService().setMusicVolume(0.3f);
        } else {
            Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
            music.setLooping(true);
            music.setVolume(0.3f);
            music.play();
        }
    }

    private void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextureAtlases(forestTextureAtlases);
        resourceService.loadSounds(forestSounds);
        resourceService.loadMusic(forestMusic);

        while (!resourceService.loadForMillis(10)) {
            logger.info("Loading... {}%", resourceService.getProgress());
        }
    }

    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(forestTextureAtlases);
        resourceService.unloadAssets(forestSounds);
        resourceService.unloadAssets(forestMusic);
    }

    public static void cleanupAllWaves() {
        if (currentGameArea != null) {
            currentGameArea.forceStopWave();
            currentGameArea = null;
        }
        logger.info("Wave cleanup completed");
    }

    private void registerForCleanup() {
        currentGameArea = this;
    }

    public void forceStopWave() {
        try {
            if (waveSpawnTask != null) {
                waveSpawnTask.cancel();
                waveSpawnTask = null;
            }
            waveInProgress = false;
            wavePaused = false;
            timeRemainingWhenPaused = 0f;
            if (enemySpawnQueue != null) {
                enemySpawnQueue.clear();
                enemySpawnQueue = null;
            }
            logger.info("Wave spawning force stopped");
        } catch (Exception e) {
            logger.error("Error during force stop: {}", e.getMessage());
        }
    }
    
    /**
     * 设置相机缩放
     * @param zoom 缩放倍数
     */
    private void setCameraZoom(float zoom) {
        com.csse3200.game.rendering.Renderer renderer = com.csse3200.game.rendering.Renderer.getCurrentRenderer();
        if (renderer != null && renderer.getCamera() != null) {
            com.badlogic.gdx.graphics.Camera camera = renderer.getCamera().getCamera();
            if (camera instanceof com.badlogic.gdx.graphics.OrthographicCamera) {
                ((com.badlogic.gdx.graphics.OrthographicCamera) camera).zoom = zoom;
                camera.update();
            }
        }
    }
    
    /**
     * 模拟向上移动三格
     * 用于游戏开始时的初始相机位置调整
     */
    private void moveUpThreeSteps() {
        com.csse3200.game.rendering.Renderer renderer = com.csse3200.game.rendering.Renderer.getCurrentRenderer();
        if (renderer == null || renderer.getCamera() == null) return;
        
        float moveDistance = 5.0f * 0.1f * 3; // 移动三格的距离
        
        if (renderer.getCamera().getEntity() != null) {
            com.badlogic.gdx.math.Vector2 currentPos = renderer.getCamera().getEntity().getPosition();
            com.badlogic.gdx.math.Vector2 newPos = new com.badlogic.gdx.math.Vector2(currentPos.x, currentPos.y + moveDistance);
            renderer.getCamera().getEntity().setPosition(newPos);
        } else {
            com.badlogic.gdx.graphics.Camera camera = renderer.getCamera().getCamera();
            camera.position.add(0, moveDistance, 0);
            camera.update();
        }
    }
    
    /**
     * 模拟向右移动三格
     * 用于游戏开始时的初始相机位置调整
     */
    private void moveRightThreeSteps() {
        com.csse3200.game.rendering.Renderer renderer = com.csse3200.game.rendering.Renderer.getCurrentRenderer();
        if (renderer == null || renderer.getCamera() == null) return;
        
        float moveDistance = 5.0f * 0.1f * 3; // 移动三格的距离
        
        if (renderer.getCamera().getEntity() != null) {
            com.badlogic.gdx.math.Vector2 currentPos = renderer.getCamera().getEntity().getPosition();
            com.badlogic.gdx.math.Vector2 newPos = new com.badlogic.gdx.math.Vector2(currentPos.x + moveDistance, currentPos.y);
            renderer.getCamera().getEntity().setPosition(newPos);
        } else {
            com.badlogic.gdx.graphics.Camera camera = renderer.getCamera().getCamera();
            camera.position.add(moveDistance, 0, 0);
            camera.update();
        }
    }

    @Override
    public void dispose() {
        forceStopWave();

        if (currentGameArea == this) {
            currentGameArea = null;
        }

        super.dispose();
        if (mapEditor != null) {
            // Both MapEditor and MapEditor2 have cleanup()
            if (mapEditor instanceof com.csse3200.game.areas2.MapTwo.MapEditor2) {
                ((com.csse3200.game.areas2.MapTwo.MapEditor2)mapEditor).cleanup();
            }
        }
        if (ServiceLocator.getAudioService() != null) {
            //ServiceLocator.getAudioService().stopMusic();
        } else {
            //ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
        }
        this.unloadAssets();
    }
}
