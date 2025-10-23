package com.csse3200.game.areas;

import com.csse3200.game.components.HealthBarComponent;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.hero.HeroUpgradeComponent;
import com.csse3200.game.components.hero.engineer.SummonPlacementComponent;
import com.csse3200.game.entities.configs.*;
import com.csse3200.game.services.SelectedHeroService;
import com.csse3200.game.ui.Hero.DefaultHeroStatusPanelComponent;
import com.csse3200.game.ui.Hero.EngineerStatusPanelComponent;
import com.csse3200.game.ui.Hero.HeroStatusPanelComponent;
import com.csse3200.game.components.hero.HeroTurretAttackComponent;

import com.csse3200.game.components.maingame.*;

import com.csse3200.game.services.GameStateService;
import com.csse3200.game.ui.Hero.SamuraiStatusPanelComponent;
import com.csse3200.game.utils.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.wavesystem.Wave;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.rendering.Renderer;
import com.badlogic.gdx.graphics.Camera;

import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.CameraZoomDragComponent;
import com.csse3200.game.components.Component;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.effects.PlasmaImpactComponent;
import com.csse3200.game.components.effects.PlasmaStrikeComponent;
import com.csse3200.game.components.effects.PlasmaWarningComponent;
import com.csse3200.game.components.effects.PlasmaWeatherController;
import com.csse3200.game.components.enemy.EnemyTypeComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.towers.TowerComponent;

/**
 * Forest area for the demo game with trees, a player, and enemies.
 */
public class ForestGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);

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

    private Entity waveTrackerUI;
    private int TOTAL_WAVES;


    public static Difficulty gameDifficulty = Difficulty.EASY;

    public static ForestGameArea currentGameArea;
    private PlasmaWeatherController plasmaWeather;

    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(30, 6);
    private static final float WALL_WIDTH = 0.1f;


    private static final String[] forestTextureAtlases = {
            "images/grunt_basic_spritesheet.atlas", "images/drone_basic_spritesheet.atlas",
            "images/tank_enemy_atlas.atlas", "images/boss_basic_spritesheet.atlas", "images/tank_projectile_atlas.atlas"
    };

    private static final String[] forestSounds = {
            "sounds/homebase_hit_sound.mp3",
            CurrencyManagerComponent.SOUND_PATH,
            "sounds/book_opening.mp3",
            "sounds/book_closing.mp3",
            "sounds/Explosion_sfx.ogg",
            "sounds/hero_lv2_shot.ogg",
            "sounds/ult_shot.ogg",
            "sounds/katana_stab_2s.ogg",
            "sounds/katana_slash_2s.ogg",
            "sounds/katana_spin_2s.ogg",
            "sounds/turret_shoot.ogg",
            "sounds/explosion_2s.ogg",
            "sounds/place_soft_click.ogg",
            "sounds/place_metal_clunk.ogg",
            "sounds/place_energy_drop.ogg",
            "sounds/Impact4.ogg",
            "sounds/Explosion_sfx3.ogg",
            "sounds/sci-fi-effect-about-danger-alarm-sound.mp3",
            "sounds/explosion-in-the-cave.mp3",
            "sounds/Enemy Sounds/tank/Tank_Death.mp3",
            "sounds/Enemy Sounds/tank/Tank_Walk.mp3",
            "sounds/Enemy Sounds/tank/Tank_Attack.mp3",
            "sounds/Enemy Sounds/tank/Tank_Random_Noise.mp3",
            "sounds/Enemy Sounds/grunt/Grunt_Death.mp3",
            "sounds/Enemy Sounds/grunt/Grunt_Walk.mp3",
            "sounds/Enemy Sounds/grunt/Grunt_Attack.wav",
            "sounds/Enemy Sounds/grunt/Grunt_Random_Noise.mp3",
            "sounds/Enemy Sounds/drone/Drone_Death.mp3",
            "sounds/Enemy Sounds/drone/Drone_Walk.mp3",
            "sounds/Enemy Sounds/drone/Drone_Attack.wav",
            "sounds/Enemy Sounds/drone/Drone_Random_Noise.mp3",
            "sounds/Enemy Sounds/boss/Boss_Death.wav",
            "sounds/Enemy Sounds/boss/Boss_Walk_1.wav",
            "sounds/Enemy Sounds/boss/Boss_Walk_2.wav",
            "sounds/Enemy Sounds/boss/Boss_lazer.wav",
            "sounds/Enemy Sounds/boss/Boss_Random_Noise.mp3",
            "sounds/Enemy Sounds/boss/Boss Music.mp3",
            "sounds/Enemy Sounds/divider/Divider_Death.mp3",
            "sounds/Enemy Sounds/divider/Divider_Walk.mp3",
            "sounds/Enemy Sounds/divider/Divider_Attack.mp3",
            "sounds/Enemy Sounds/divider/Divider_Random_Noise.mp3",
            "sounds/Enemy Sounds/speedster/Speedster_Death.mp3",
            "sounds/Enemy Sounds/speedster/Speedster_Walk.mp3",
            "sounds/Enemy Sounds/speedster/Speedster_Attack.mp3",
            "sounds/Enemy Sounds/speedster/Speedster_Random_Noise.mp3"
    };
    private static final String PLASMA_WARNING_SOUND = "sounds/sci-fi-effect-about-danger-alarm-sound.mp3";
    private static final String PLASMA_IMPACT_SOUND = "sounds/explosion-in-the-cave.mp3";
    private static final String backgroundMusic = "sounds/new_menutheme.mp3";
    private static final String[] forestMusic = {backgroundMusic};

    private final TerrainFactory terrainFactory;
    private Entity player;
    private boolean hasExistingPlayer = false;
    // private MapEditor mapEditor;
    private IMapEditor mapEditor; // Use interface for flexibility

    // One-time prompt: Has this been displayed?
    private boolean heroHintShown = false;

    // Obstacle Coordinate Single Fact Source: Defined by the GameArea
    // create barriers areas
    private static final int[][] BARRIER_COORDS = new int[][]{
            {27, 9}, {28, 9}, {29, 9}, {30, 9}, {31, 9},
            {26, 3}, {27, 3}, {28, 3}, {29, 3},
            {5, 24}, {8, 20},
            // 在x<31且y>13且x<13范围内随机添加的坐标点
            {8, 15}, {5, 17}, {11, 14}, {3, 18},
            {7, 25}, {2, 15}, {6, 29},
    };

    // create snowtree areas - 避开路径坐标
    private static final int[][] SNOWTREE_COORDS = new int[][]{
            {15, 9}, {16, 8}, {17, 10}, {19, 10}, {14, 6}, {10, 3}, {13, 5}, {5, 4}, {7, 4}, {3, 8}, {15, 3}
    };

    /**
     * Initialise this ForestGameArea to use the provided TerrainFactory.
     *
     * @param terrainFactory TerrainFactory used to create the terrain for the GameArea.
     * @requires terrainFactory != null
     */
    public ForestGameArea(TerrainFactory terrainFactory) {
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
        waypointLists.add(MapEditor.waypointList);
    }

    private void initializeWaves() {
        // Initialize waypoint lists first
        initializeWaypointLists();

        // Initialize spawn callbacks
        initializeSpawnCallbacks();

        waves = new ArrayList<>();

        waves.add(new Wave(1, 5, 3, 0, 0, 0, 0, 3.0f, waypointLists));

        waves.add(new Wave(2, 0, 10, 0, 0, 0, 0, 3.0f, waypointLists));

        waves.add(new Wave(3, 8, 0, 5, 0, 0, 0, 3.0f, waypointLists));

        waves.add(new Wave(4, 0, 5, 3, 0, 1, 0, 2.0f, waypointLists));

        waves.add(new Wave(5, 5, 0, 0, 0, 1, 2, 2.0f, waypointLists));

        waves.add(new Wave(6, 15, 5, 0, 0, 0, 0, 2.0f, waypointLists));

        waves.add(new Wave(7, 5, 5, 5, 0, 2, 2, 1.0f, waypointLists));

        waves.add(new Wave(8, 3, 2, 5, 0, 5, 3, 1.0f, waypointLists));

        waves.add(new Wave(9, 5, 5, 5, 0, 0, 0, 1.0f, waypointLists));

        waves.add(new Wave(10, 20, 15, 10, 2, 5, 5, 1.0f, waypointLists));

        TOTAL_WAVES = waves.size();
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
        
        // Notify UI that wave has started
        if (MainGameScreen.ui != null) {
            MainGameScreen.ui.getEvents().trigger("waveStarted");
        }

        // Trigger boss wave message when starting wave 5
        if (waveTrackerUI != null && currentWaveIndex == waves.size() - 1) {
            WaveTrackerDisplay display = waveTrackerUI.getComponent(WaveTrackerDisplay.class);
            if (display != null) {
                display.triggerBossWaveMessage();
            }
        }
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

        // Set counters before building queue or spawning anything
        NUM_ENEMIES_TOTAL = currentWave.getTotalEnemies();
        NUM_ENEMIES_DEFEATED = 0;
        spawnDelay = currentWave.getSpawnDelay();

        logger.info("Starting Wave {} with {} enemies (Total: {}, Defeated: {})",
                currentWave.getWaveNumber(),
                NUM_ENEMIES_TOTAL,
                NUM_ENEMIES_TOTAL,
                NUM_ENEMIES_DEFEATED);

        enemySpawnQueue = currentWave.buildSpawnQueue(spawnCallbacks);
    }

    /**
     * Called when all enemies in the current wave have been defeated.
     */
    public void onWaveDefeated() {

        // Safety check: if waves not initialized (e.g., loaded from save), don't process wave progression
        if (waves == null) {
            logger.warn("Waves not initialized - skipping wave progression");
            return;
        }

        // Check if this was the final wave
        if (currentWaveIndex + 1 >= waves.size()) {
            // All waves complete - trigger victory!
            
            // Notify UI that all waves are complete (permanently disable button)
            if (MainGameScreen.ui != null) {
                MainGameScreen.ui.getEvents().trigger("allWavesComplete");
                
                MainGameWin winComponent = MainGameScreen.ui.getComponent(MainGameWin.class);
                if (winComponent != null) {
                    winComponent.addActors();
                }
            }
        } else {
            // Increment wave index and wait for player to start next wave
            currentWaveIndex++;

            // if (waveTrackerUI != null) {
            //     waveTrackerUI.getEvents().trigger("updateWave", currentWaveIndex + 1);
            // }

            // Notify UI that current wave is complete (re-enable button)
            if (MainGameScreen.ui != null) {
                MainGameScreen.ui.getEvents().trigger("waveComplete");
            }
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
                if (currentGameArea != ForestGameArea.this) {
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
        resetEnemyCounters();

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

        // 添加防御塔列表组件，但初始隐藏（如果是新游戏）
        TowerHotbarDisplay towerHotbar = new TowerHotbarDisplay();
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

        /*
        // Difficulty label UI
        Entity difficultyUi = new Entity()
                .addComponent(new DifficultyDisplay()); // your custom component
        spawnEntity(difficultyUi);

        // Send the chosen difficulty so it shows immediately
        difficultyUi.getEvents().trigger("setDifficulty", gameDifficulty.name());
        */

        // Create camera control entity for zoom and drag functionality
        Entity cameraControl = new Entity();
        cameraControl.addComponent(new CameraZoomDragComponent());
        spawnEntity(cameraControl);

        // Create background entity (renders behind everything)
        Entity background = new Entity();
        background.addComponent(new com.csse3200.game.rendering.BackgroundRenderComponent("images/main_game_background.png"));
        background.setPosition(0, 0); // Set position at origin
        spawnEntity(background);

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
                if (player.getComponent(com.csse3200.game.components.PlayerScoreComponent.class) == null) {
                    player.addComponent(new com.csse3200.game.components.PlayerScoreComponent());
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
        plasmaWeather = new PlasmaWeatherController(this::customSpawnEntityAt, mapEditor, terrain, this::handlePlasmaImpact);
        if (plasmaWeather != null) {
            Entity weatherEntity = new Entity();
            weatherEntity.addComponent(new Component() {
                @Override
                public void update() {
                    plasmaWeather.update(ServiceLocator.getTimeSource().getDeltaTime());
                }
            });
            spawnEntity(weatherEntity);
            Entity weatherDisplay = new Entity().addComponent(new WeatherStatusDisplay(plasmaWeather));
            spawnEntity(weatherDisplay);
        }

        // Register pause/resume listeners for wave system
        Entity pauseListener = new Entity();
        pauseListener.getEvents().addListener("gamePaused", this::pauseWaveSpawning);
        pauseListener.getEvents().addListener("gameResumed", this::resumeWaveSpawning);
        spawnEntity(pauseListener);

        // Generate biomes & placeable areas
        //mapEditor.generateBiomesAndRivers();


        // Tower placement highlighter
        MapHighlighter mapHighlighter =
                new MapHighlighter(terrain, placementController, new com.csse3200.game.entities.factories.TowerFactory());
        Entity highlighterEntity = new Entity().addComponent(mapHighlighter);
        highlighterEntity.addComponent(new SummonPlacementComponent());

        spawnEntity(highlighterEntity);

        //Tower Upgrade Menu
        TowerUpgradeMenu towerUpgradeMenu = new TowerUpgradeMenu();
        Entity upgradeUI = new Entity().addComponent(towerUpgradeMenu);
        spawnEntity(upgradeUI);

        //Link the upgrade menu to the map highlighter
        mapHighlighter.setTowerUpgradeMenu(towerUpgradeMenu);

        if (!hasExistingPlayer) {
            showChapterIntro();
        }

        // Add hero placement system

        // 背景音乐将在对话结束后播放
        if (hasExistingPlayer) {
            createHeroPlacementUI();
            // 如果已有玩家（从存档加载），直接播放音乐
            playMusic();
            // Spawn wave tracker immediately if loading from save
            spawnWaveTracker();
        }
    }

    private void spawnTerrain() {
        terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO);
        spawnEntity(new Entity().addComponent(terrain));
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

    //Register to MapEditor’s invalidTiles and generate obstacles on the map.
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

    private void handlePlasmaImpact(Vector2 position) {
        float radiusSquared = 0.75f * 0.75f;
        Array<Entity> entities = ServiceLocator.getEntityService().getEntitiesCopy();
        Array<Entity> towerTargets = new Array<>();
        Array<Entity> enemyTargets = new Array<>();
        for (int i = 0; i < entities.size; i++) {
            Entity entity = entities.get(i);
            if (entity == null || !entity.isActive()) {
                continue;
            }
            if (entity.getComponent(PlasmaStrikeComponent.class) != null
                    || entity.getComponent(PlasmaWarningComponent.class) != null
                    || entity.getComponent(PlasmaImpactComponent.class) != null) {
                continue;
            }
            Vector2 entityPosition = entity.getPosition();
            if (entityPosition == null || entityPosition.dst2(position) > radiusSquared) {
                continue;
            }
            if (entity.getComponent(TowerComponent.class) != null) {
                towerTargets.add(entity);
                continue;
            }
            EnemyTypeComponent typeComponent = entity.getComponent(EnemyTypeComponent.class);
            if (typeComponent != null) {
                if (isPlasmaImmune(typeComponent)) {
                    continue;
                }
                enemyTargets.add(entity);
            }
        }
        if (towerTargets.size > 0 || enemyTargets.size > 0) {
            Gdx.app.postRunnable(() -> {
                for (int i = 0; i < towerTargets.size; i++) {
                    Entity target = towerTargets.get(i);
                    if (target != null && target.isActive()) {
                        dismantleTower(target);
                    }
                }
                for (int i = 0; i < enemyTargets.size; i++) {
                    Entity target = enemyTargets.get(i);
                    if (target != null && target.isActive()) {
                        eliminateEnemy(target);
                    }
                }
            });
        }
    }

    private void dismantleTower(Entity tower) {
        TowerComponent towerComponent = tower.getComponent(TowerComponent.class);
        if (towerComponent == null) {
            tower.dispose();
            return;
        }
        if (towerComponent.hasHead()) {
            Entity head = towerComponent.getHeadEntity();
            if (head != null && head.isActive()) {
                head.dispose();
                ServiceLocator.getEntityService().unregister(head);
            }
        }
        tower.dispose();
        ServiceLocator.getEntityService().unregister(tower);
    }

    private boolean isPlasmaImmune(EnemyTypeComponent typeComponent) {
        if (typeComponent == null) {
            return false;
        }
        String type = typeComponent.getType();
        if (type == null) {
            return false;
        }
        String normalised = type.trim().toLowerCase(Locale.ROOT);
        return "tank".equals(normalised) || "boss".equals(normalised);
    }

    private void eliminateEnemy(Entity enemy) {
        CombatStatsComponent stats = enemy.getComponent(CombatStatsComponent.class);
        if (stats != null) {
            stats.setHealth(0);
            return;
        }
        enemy.dispose();
    }

    private Entity spawnPlayer() {
        // Map1 使用标准homebase缩放 (1.88f) 和较小的血条
        HealthBarComponent healthBar = new HealthBarComponent(1.4f, 0.15f, 0.8f);
        Entity newPlayer = PlayerFactory.createPlayer("images/homebase1.png", 1.88f, healthBar);
        // 确保新玩家有钱包组件
        if (newPlayer.getComponent(CurrencyManagerComponent.class) == null) {
            newPlayer.addComponent(new CurrencyManagerComponent(/* 可选初始值 */));
        }

        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);

        // Initialize MapEditor
        // If you want to use MapEditor2, replace the following line:
        // mapEditor = new MapEditor(terrain, newPlayer);
        // with:
        // mapEditor = new com.csse3200.game.areas2.MapTwo.MapEditor2((TerrainComponent2)terrain, newPlayer);
        mapEditor = new MapEditor(terrain, newPlayer);
        mapEditor.generateEnemyPath(); // Generate fixed enemy path

        // Set path layer opacity to 0.7 (70% opacity) for map1
        // 调整map1路径砖块的透明度为70%
        mapEditor.setPathLayerOpacity(0.7f);

        // Uncomment if crystal spawning is needed:
        // mapEditor.spawnCrystal(); // Generate crystal

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
        // Only initialize waves once at the very beginning
        if (waves == null) {
            initializeWaves();
        }

        // Update wave tracker to show current wave
        if (waveTrackerUI != null) {
            waveTrackerUI.getEvents().trigger("updateWave", currentWaveIndex + 1);
        }
        
        // Check if all waves are already complete
        if (currentWaveIndex >= waves.size()) {
            logger.info("All waves already completed!");
            return;
        }
        
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                startEnemyWave();
            }
        }, 2.0f);
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
        Entity divider = DividerEnemyFactory.createDividerEnemy(waypoints, this, player, gameDifficulty);
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

    private void applySkinToHeroForms(String skinKey, HeroConfig c1, HeroConfig2 c2, HeroConfig3 c3) {
        // 形态1
        c1.heroTexture   = HeroSkinAtlas.bodyForForm(GameStateService.HeroType.HERO, skinKey, 1);
        if (c1.levelTextures != null && c1.levelTextures.length > 0) c1.levelTextures[0] = c1.heroTexture;
        else c1.levelTextures = new String[]{ c1.heroTexture };

        // 形态2
        c2.heroTexture   = HeroSkinAtlas.bodyForForm(GameStateService.HeroType.HERO, skinKey, 2);
        if (c2.levelTextures != null && c2.levelTextures.length > 0) c2.levelTextures[0] = c2.heroTexture;
        else c2.levelTextures = new String[]{ c2.heroTexture };

        // 形态3
        c3.heroTexture   = HeroSkinAtlas.bodyForForm(GameStateService.HeroType.HERO, skinKey, 3);
        if (c3.levelTextures != null && c3.levelTextures.length > 0) c3.levelTextures[0] = c3.heroTexture;
        else c3.levelTextures = new String[]{ c3.heroTexture };
    }

    private void applySkinToEngineer(String skinKey, EngineerConfig cfg) {
        cfg.heroTexture   = HeroSkinAtlas.body(GameStateService.HeroType.ENGINEER, skinKey);
        cfg.bulletTexture = HeroSkinAtlas.bullet(GameStateService.HeroType.ENGINEER, skinKey);
        // ★ 关键：避免 HeroAppearanceComponent 把皮肤“打回默认”
        if (cfg.levelTextures != null && cfg.levelTextures.length > 0) {
            cfg.levelTextures[0] = cfg.heroTexture;
        } else {
            cfg.levelTextures = new String[]{ cfg.heroTexture };
        }
    }


    private void spawnHeroAt(GridPoint2 cell) {
        // 1️⃣ 加载配置（或直接手动创建，如你示例）
        HeroConfig heroCfg = new HeroConfig();
        heroCfg.heroTexture = "images/hero/Heroshoot.png";
        heroCfg.bulletTexture = "images/hero/Bullet.png";
        heroCfg.shootSfx = "sounds/Explosion_sfx.ogg";
        heroCfg.shootSfxVolume = 1.0f;
        HeroConfig2 heroCfg2 = new HeroConfig2();
        heroCfg2.heroTexture = "images/hero2/Heroshoot.png";
        heroCfg2.bulletTexture = "images/hero2/Bullet.png";
        heroCfg2.shootSfx = "sounds/Explosion_sfx2.ogg";
        heroCfg2.shootSfxVolume = 1.0f;
        HeroConfig3 heroCfg3 = new HeroConfig3();
        heroCfg3.heroTexture = "images/hero3/Heroshoot.png";
        heroCfg3.bulletTexture = "images/hero3/Bullet.png";
        heroCfg3.shootSfx = "sounds/Explosion_sfx3.ogg";
        heroCfg3.shootSfxVolume = 1.0f;
        // 2️⃣ 加载贴图资源（不放 create() 全局加载）
        ResourceService rs = ServiceLocator.getResourceService();
        java.util.ArrayList<String> sfx = new java.util.ArrayList<>();
        if (heroCfg.shootSfx  != null && !heroCfg.shootSfx.isBlank())  sfx.add(heroCfg.shootSfx);
        if (heroCfg2.shootSfx != null && !heroCfg2.shootSfx.isBlank()) sfx.add(heroCfg2.shootSfx);
        if (heroCfg3.shootSfx != null && !heroCfg3.shootSfx.isBlank()) sfx.add(heroCfg3.shootSfx);
        if (!sfx.isEmpty()) {
            rs.loadSounds(sfx.toArray(new String[0]));
            while (!rs.loadForMillis(10)) { /* wait */ }
        }

        String skinHero = ServiceLocator.getGameStateService()
                .getSelectedSkin(GameStateService.HeroType.HERO);
        logger.info("Spawn hero with skin={}", skinHero);
        applySkinToHeroForms(skinHero, heroCfg, heroCfg2, heroCfg3);

        HeroFactory.loadAssets(rs, heroCfg, heroCfg2, heroCfg3);
        while (!rs.loadForMillis(10)) {
            logger.info("Loading hero assets... {}%", rs.getProgress());
        }

        // 3️⃣ 创建英雄实体
        Camera cam = Renderer.getCurrentRenderer().getCamera().getCamera();
        Entity hero = HeroFactory.createHero(heroCfg, cam);

        // 4️⃣ 挂上 OneShotFormSwitchComponent（带三套 cfg）
        hero.addComponent(new com.csse3200.game.components.hero.HeroOneShotFormSwitchComponent(
                heroCfg, heroCfg2, heroCfg3
        ));

        // 5️⃣ 其他组件照旧
        var up = hero.getComponent(HeroUpgradeComponent.class);
        if (up != null) up.attachPlayer(player);

        hero.addComponent(new com.csse3200.game.components.hero.HeroClickableComponent(0.8f));
        hero.addComponent(new com.csse3200.game.ui.UltimateButtonComponent());

        Entity heroStatusUI = new Entity()
                .addComponent(new DefaultHeroStatusPanelComponent(hero, "Hero"));
        spawnEntity(heroStatusUI);

        Entity heroWeaponBar = new Entity()
                .addComponent(new com.csse3200.game.ui.Hero.HeroWeaponSwitcherToolbarComponent(
                        hero,
                        /* 建议使用独立图标（小尺寸方图） */
                        "images/hero/gun1.png",
                        "images/hero2/gun2.png",
                        "images/hero3/gun3.png",
                        // 也可以暂时用 heroCfg.heroTexture 等
                        "images/hero/Final_gun.png"
                ));
        spawnEntity(heroWeaponBar);

        spawnEntityAt(hero, cell, true, true);


    }


    private void spawnEngineerAt(GridPoint2 cell) {
        // 1) 只读取工程师配置
        EngineerConfig engCfg = FileLoader.readClass(EngineerConfig.class, "configs/engineer.json");
        if (engCfg == null) {
            logger.warn("Failed to load configs/engineer.json, using default EngineerConfig.");
            engCfg = new EngineerConfig();
        }

        // ★ 2) 取当前选中的工程师皮肤，并覆盖到 Config
        String skin = ServiceLocator.getGameStateService()
                .getSelectedSkin(GameStateService.HeroType.ENGINEER);
        applySkinToEngineer(skin, engCfg);

        // 2) 只加载工程师资源（HeroFactory 的 varargs 接受子类 -> 直接传 engCfg 即可）
        ResourceService rs = ServiceLocator.getResourceService();
        HeroFactory.loadAssets(rs, engCfg);  // 只传工程师
        while (!rs.loadForMillis(10)) {
            logger.info("Loading engineer assets... {}%", rs.getProgress());
        }

        // 3) 创建工程师实体（注意方法名：你现在实现的是 createEngineerHero）
        Camera cam = Renderer.getCurrentRenderer().getCamera().getCamera();
        Entity engineer = HeroFactory.createEngineerHero(engCfg, cam);

        var up = engineer.getComponent(HeroUpgradeComponent.class);
        if (up != null) {
            up.attachPlayer(player);
        }

        engineer.addComponent(new com.csse3200.game.components.hero.HeroClickableComponent(0.8f));

        // 4) 工程师 UI：状态栏 + 工具条（点击图标放置三类召唤）
        Entity heroStatusUI = new Entity()
                .addComponent(new EngineerStatusPanelComponent(engineer, "Engineer"));
        spawnEntity(heroStatusUI);

        Entity engineerToolbarUI = new Entity()
                .addComponent(new com.csse3200.game.ui.Hero.EngineerSummonToolbarComponent(engineer));
        spawnEntity(engineerToolbarUI);

        // 5) 放置
        spawnEntityAt(engineer, cell, true, true);

    }

    private void applySkinToSamurai(String skinKey, SamuraiConfig cfg) {
        // 本体随 BODY 皮肤
        cfg.heroTexture = HeroSkinAtlas.body(GameStateService.HeroType.SAMURAI, skinKey);

        // 刀：独立读取 WEAPON 皮肤
        String swordSkin = ServiceLocator.getGameStateService()
                .getSelectedWeaponSkin(GameStateService.HeroType.SAMURAI);
        String sword = HeroSkinAtlas.sword(GameStateService.HeroType.SAMURAI, swordSkin);
        if (sword != null && !sword.isBlank()) {
            cfg.swordTexture = sword;
        }

        // 防止外观被 levelTextures 还原
        if (cfg.levelTextures != null && cfg.levelTextures.length > 0) {
            cfg.levelTextures[0] = cfg.heroTexture;
        } else {
            cfg.levelTextures = new String[]{ cfg.heroTexture };
        }
    }

    private void spawnSamuraiAt(GridPoint2 cell) {
        // 1) 读 samurai 配置
        SamuraiConfig samCfg = FileLoader.readClass(SamuraiConfig.class, "configs/samurai.json");
        if (samCfg == null) {
            logger.warn("Failed to load configs/samurai.json, using default SamuraiConfig.");
            samCfg = new SamuraiConfig();
        }

        String samSkin = ServiceLocator.getGameStateService()
                .getSelectedSkin(GameStateService.HeroType.SAMURAI); // 自己已有的方法/字段
        applySkinToSamurai(samSkin, samCfg);

        // 2) 预加载 samurai 资源（主体 + 刀）
        ResourceService rs = ServiceLocator.getResourceService();
        HeroFactory.loadAssets(rs, samCfg);
        rs.loadTextures(new String[]{
                "images/samurai/slash_sheet_6x1_64.png"
        });
        while (!rs.loadForMillis(10)) {
            logger.info("Loading samurai assets... {}%", rs.getProgress());
        }

        // 3) 创建 samurai 英雄（你之前实现的 createSamuraiHero）
        Camera cam = Renderer.getCurrentRenderer().getCamera().getCamera();
        Entity samurai = HeroFactory.createSamuraiHero(samCfg, cam);

        // 4) 附加钱包/升级等（和其他英雄保持一致）
        if (samurai.getComponent(CurrencyManagerComponent.class) == null) {
            samurai.addComponent(new CurrencyManagerComponent());
        }
        var up = samurai.getComponent(HeroUpgradeComponent.class);
        if (up != null) {
            up.attachPlayer(player);
        }
        samurai.addComponent(new com.csse3200.game.components.hero.HeroClickableComponent(0.8f));

        // 5) 创建状态栏UI
        Entity heroStatusUI = new Entity()
                .addComponent(new SamuraiStatusPanelComponent(samurai, "Samurai"));
        spawnEntity(heroStatusUI);

        // 5.5) ★ 新增：创建“武士攻击工具条”UI（带 1/2/3 提示）
        Entity samuraiAttackUI = new Entity()
                .addComponent(new com.csse3200.game.ui.Hero.SamuraiAttackToolbarComponent(samurai));
        spawnEntity(samuraiAttackUI);

        // 6) 放置
        spawnEntityAt(samurai, cell, true, true);

    }

    private void createHeroPlacementUI() {
        // 先创建英雄放置实体，但初始隐藏
        var gs = ServiceLocator.getGameStateService();
        GameStateService.HeroType chosen = gs.getSelectedHero();
        java.util.function.Consumer<com.badlogic.gdx.math.GridPoint2> placeCb =
                switch (chosen) {
                    case ENGINEER -> this::spawnEngineerAt;
                    case SAMURAI  -> this::spawnSamuraiAt;
                    default       -> this::spawnHeroAt;
                };
        Entity placementEntity = new Entity()
                .addComponent(new com.csse3200.game.components.hero.HeroPlacementComponent(terrain, mapEditor, placeCb))
                .addComponent(new com.csse3200.game.components.hero.HeroHotbarDisplay());
        spawnEntity(placementEntity);
        
        // 立即隐藏英雄UI
        hideHeroUI();
        
        // 2秒后显示英雄UI
        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override
            public void run() {
                showHeroUI();
            }
        }, 2.0f);
    }
    
    /**
     * 隐藏英雄UI
     */
    private void hideHeroUI() {
        for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
            com.csse3200.game.components.hero.HeroHotbarDisplay heroUI = entity.getComponent(com.csse3200.game.components.hero.HeroHotbarDisplay.class);
            if (heroUI != null) {
                heroUI.setVisible(false);
                logger.info("英雄UI已隐藏");
                break;
            }
        }
    }
    
    /**
     * 显示英雄UI
     */
    private void showHeroUI() {
        for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
            com.csse3200.game.components.hero.HeroHotbarDisplay heroUI = entity.getComponent(com.csse3200.game.components.hero.HeroHotbarDisplay.class);
            if (heroUI != null) {
                heroUI.setVisible(true);
                logger.info("英雄UI已显示");
                break;
            }
        }
    }

    private void spawnIntroDialogue() {
        // 使用 DialogueConfig 获取地图1的对话脚本
        java.util.List<com.csse3200.game.components.maingame.IntroDialogueComponent.DialogueEntry> script =
                com.csse3200.game.components.maingame.DialogueConfig.getMap1Dialogue();

        Entity dialogueEntity = new Entity().addComponent(
                new com.csse3200.game.components.maingame.IntroDialogueComponent(
                        script,
                        () -> {
                            // 对话结束后显示防御塔列表和播放背景音乐
                            showTowerUI();

                            // Spawn wave tracker after dialogue completes
                            if (waves == null) {
                                initializeWaves();
                            }

                            spawnWaveTracker();
                            createHeroPlacementUI();
                            playMusic();
                        })
        );
        spawnEntity(dialogueEntity);
    }
    
    /**
     * 显示章节介绍
     */
    private void showChapterIntro() {
        String[] storyTexts = {
            "Chapter I : Icebox",
            "This is Icebox, a former research outpost now buried beneath eternal night.\nThe AI legions have ravaged this land beyond recognition,\nbut now, it stands as the cradle of humanity's awakening.",
            "The sorcerers gathered here forming circles of frost and flame\nunleashed the first wave of pure human magic, untouched by machines.\nGlaciers shattered. Circuits failed.\nAcross the frozen plains, the echoes of ancient power\nannounced the dawn of rebellion.\n\n\"On the coldest land, the oldest flame burns once more.\""
        };
        
        Entity chapterEntity = new Entity().addComponent(
                new com.csse3200.game.components.maingame.ChapterIntroComponent(
                        storyTexts,
                        () -> {
                            // 章节介绍结束后开始对话
                            spawnIntroDialogue();
                        })
        );
        spawnEntity(chapterEntity);
    }

    /**
     * Spawn the wave tracker UI
     */
    private void spawnWaveTracker() {
        waveTrackerUI = new Entity();
        waveTrackerUI.addComponent(new WaveTrackerDisplay(TOTAL_WAVES));
        spawnEntity(waveTrackerUI);
    }

    /**
     * 显示防御塔UI（在对话结束后调用）
     */
    private void showTowerUI() {
        // 先隐藏塔防UI
        for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
            TowerHotbarDisplay towerUI = entity.getComponent(TowerHotbarDisplay.class);
            if (towerUI != null) {
                towerUI.setVisible(false);
                logger.info("防御塔列表已隐藏");
                break;
            }
        }
        
        // 3.3秒后显示塔防UI
        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override
            public void run() {
                for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
                    TowerHotbarDisplay towerUI = entity.getComponent(TowerHotbarDisplay.class);
                    if (towerUI != null) {
                        towerUI.setVisible(true);
                        logger.info("防御塔列表已显示");
                        break;
                    }
                }
            }
        }, 2.0f);
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
    if (ServiceLocator.getAudioService() != null) {
      ServiceLocator.getAudioService().registerSound("plasma_warning", PLASMA_WARNING_SOUND);
      ServiceLocator.getAudioService().registerSound("plasma_impact", PLASMA_IMPACT_SOUND);
    }
//        try {
//            com.badlogic.gdx.audio.Sound s =
//                    resourceService.getAsset("sounds/Explosion_sfx.ogg", com.badlogic.gdx.audio.Sound.class);
//            if (s != null) {
//                long id = s.play(1f);
//                Gdx.app.log("SmokeTest", "Played Explosion_sfx.ogg right after load, id=" + id);
//            } else {
//                Gdx.app.error("SmokeTest", "ResourceService returned NULL for Explosion_sfx.ogg");
//            }
//        } catch (Throwable t) {
//            Gdx.app.error("SmokeTest", "Exception while smoke testing Explosion_sfx.ogg", t);
//        }
    }

    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(forestTextureAtlases);
        resourceService.unloadAssets(forestSounds);
        resourceService.unloadAssets(forestMusic);
    }

    public static void resetEnemyCounters() {
        NUM_ENEMIES_TOTAL = 0;
        NUM_ENEMIES_DEFEATED = 0;
        logger.info("Enemy counters reset to 0");
    }

    public static void cleanupAllWaves() {
        if (currentGameArea != null) {
            currentGameArea.forceStopWave();
            currentGameArea = null;
        }
        resetEnemyCounters();
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
            resetEnemyCounters();
            logger.info("Wave spawning force stopped");
        } catch (Exception e) {
            logger.error("Error during force stop: {}", e.getMessage());
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
            if (mapEditor instanceof MapEditor) {
                ((MapEditor) mapEditor).cleanup();
            } else if (mapEditor instanceof com.csse3200.game.areas2.MapTwo.MapEditor2) {
                ((com.csse3200.game.areas2.MapTwo.MapEditor2) mapEditor).cleanup();
            }
        }
        if (ServiceLocator.getAudioService() != null) {
            //ServiceLocator.getAudioService().stopMusic();
        } else {
            //ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
        }
        this.unloadAssets();

        resetEnemyCounters();
    }
}
