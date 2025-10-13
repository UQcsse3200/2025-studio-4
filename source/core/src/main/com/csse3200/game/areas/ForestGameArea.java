package com.csse3200.game.areas;

import com.csse3200.game.components.HealthBarComponent;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.hero.HeroUpgradeComponent;
import com.csse3200.game.components.hero.engineer.SummonPlacementComponent;
import com.csse3200.game.components.maingame.TowerUpgradeMenu;
import com.csse3200.game.services.SelectedHeroService;
import com.csse3200.game.ui.Hero.DefaultHeroStatusPanelComponent;
import com.csse3200.game.ui.Hero.EngineerStatusPanelComponent;
import com.csse3200.game.ui.Hero.HeroStatusPanelComponent;


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
import com.csse3200.game.areas2.MapTwo.MapEditor2;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.hero.HeroPlacementComponent;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.entities.configs.HeroConfig2;
import com.csse3200.game.entities.configs.HeroConfig3;
import com.csse3200.game.entities.configs.EngineerConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.rendering.Renderer;
import com.badlogic.gdx.graphics.Camera;
import com.csse3200.game.entities.configs.SamuraiConfig;

import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.List;


import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.hero.HeroOneShotFormSwitchComponent;
import com.csse3200.game.components.maingame.SimplePlacementController;
import com.csse3200.game.components.hero.HeroOneShotFormSwitchComponent;
import com.csse3200.game.components.CameraZoomDragComponent;

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
    private float spawnDelay = 2f; // Delay between spawns (updated per wave)
    private List<List<Entity>> waypointLists;

    // When loading from a save/continue, we don't want to auto-start waves and duplicate enemies
    private boolean autoStartWaves = true;

    public static Difficulty gameDifficulty = Difficulty.EASY;

    public static ForestGameArea currentGameArea;

    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(30, 6);
    private static final float WALL_WIDTH = 0.1f;


    private static final String[] forestTextureAtlases = {
            "images/grunt_basic_spritesheet.atlas", "images/drone_basic_spritesheet.atlas",
            "images/tank_basic_spritesheet.atlas", "images/boss_basic_spritesheet.atlas"
    };

    private static final String[] forestSounds = {
            "sounds/homebase_hit_sound.mp3",
            CurrencyManagerComponent.SOUND_PATH,
            "sounds/book_opening.mp3",
            "sounds/book_closing.mp3",
    };
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

    /**
     * Control whether waves should auto start during create().
     * Useful to disable when restoring from a save to avoid duplicated spawns.
     */
    public void setAutoStartWaves(boolean autoStartWaves) {
        this.autoStartWaves = autoStartWaves;
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

        // Wave 1: Basic enemies
        waves.add(new Wave(1, 5, 1, 0, 0, 0, 0, 3.0f, waypointLists));

        // Wave 2: Introduce speeders
        waves.add(new Wave(2, 8, 3, 0, 0, 0, 2, 2.0f, waypointLists));

        // Wave 3: More variety
        waves.add(new Wave(3, 10, 5, 2, 0, 0, 3, 2.0f, waypointLists));

        // Wave 4: Dividers appear
        waves.add(new Wave(4, 8, 6, 3, 0, 1, 2, 1.5f, waypointLists));

        // Wave 5: Final wave with boss
        waves.add(new Wave(5, 10, 8, 4, 1, 1, 4, 1.2f, waypointLists));
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
            // Adjust delay by time scale
            float adjustedDelay = 3.0f / ServiceLocator.getTimeSource().getTimeScale();
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    startEnemyWave();
                }
            }, adjustedDelay);
        }
    }

    /**
     * Schedule the next enemy spawn with delay (with comprehensive safety checks)
     */
    private void scheduleNextEnemySpawn() {
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

        // Adjust spawn delay to compensate for time scale
        float adjustedSpawnDelay = spawnDelay / ServiceLocator.getTimeSource().getTimeScale();

        // Schedule next spawn
        waveSpawnTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
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
        }, adjustedSpawnDelay);
    }

    /**
     * Create the game area, including terrain, static entities (trees), dynamic entities (player)
     */
    @Override
    public void create() {
        // Load assets (textures, sounds, etc.) before creating anything that needs them
        loadAssets();
        registerForCleanup();

        // Create the main UI entity that will handle area info, hotbar, and tower placement
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Box Forest")); // Shows the game area's name
        ui.addComponent(new com.csse3200.game.components.maingame.TowerHotbarDisplay()); // UI for selecting towers
        ui.addComponent(new com.csse3200.game.components.maingame.MainGameWin());
        SimplePlacementController placementController = new SimplePlacementController();
        ui.addComponent(placementController); // Handles user input for tower placement
        spawnEntity(ui);


        /*
        // Difficulty label UI
        Entity difficultyUi = new Entity()
                .addComponent(new DifficultyDisplay()); // your custom component
        spawnEntity(difficultyUi);

        // Send the chosen difficulty so it shows immediately
        difficultyUi.getEvents().trigger("setDifficulty", gameDifficulty.name());
        */

        // Create time speed control button
        Entity timeSpeedUI = new Entity();
        timeSpeedUI.addComponent(new com.csse3200.game.components.maingame.TimeSpeedButton());
        spawnEntity(timeSpeedUI);


        // Create camera control entity for zoom and drag functionality
        Entity cameraControl = new Entity();
        cameraControl.addComponent(new CameraZoomDragComponent());
        spawnEntity(cameraControl);

        // Create background entity (renders behind everything)
        Entity background = new Entity();
        background.addComponent(new com.csse3200.game.rendering.BackgroundRenderComponent("images/game background.jpg"));
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

        if (autoStartWaves) {
            initializeWaves();
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    startEnemyWave();
                }
            }, 2.0f); // Start wave after 2 seconds (gives player time to prepare)
        }

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

        // Add hero placement system
        var gameState = ServiceLocator.getGameStateService();
        if (gameState == null) {
            throw new IllegalStateException("GameStateService not registered before MAIN_GAME!");
        }
        GameStateService.HeroType chosen = gameState.getSelectedHero();
        Gdx.app.log("ForestGameArea", "chosen=" + chosen);

// 根据选择安装一个只放“指定英雄”的放置器

        java.util.function.Consumer<com.badlogic.gdx.math.GridPoint2> placeCb;
        switch (chosen) {
            case ENGINEER -> placeCb = this::spawnEngineerAt;
            case SAMURAI -> placeCb = this::spawnSamuraiAt;   // ★ 新增武士
            default -> placeCb = this::spawnHeroAt;
        }


        Entity placementEntity = new Entity().addComponent(
                new com.csse3200.game.components.hero.HeroPlacementComponent(terrain, mapEditor, placeCb)
        ).addComponent(new com.csse3200.game.components.hero.HeroHotbarDisplay());
        spawnEntity(placementEntity);

        playMusic();


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

    private void spawnHeroAt(GridPoint2 cell) {
        // 1️⃣ 加载配置（或直接手动创建，如你示例）
        HeroConfig heroCfg = new HeroConfig();
        heroCfg.heroTexture = "images/hero/Heroshoot.png";
        heroCfg.bulletTexture = "images/hero/Bullet.png";

        HeroConfig2 heroCfg2 = new HeroConfig2();
        heroCfg2.heroTexture = "images/hero2/Heroshoot.png";
        heroCfg2.bulletTexture = "images/hero2/Bullet.png";

        HeroConfig3 heroCfg3 = new HeroConfig3();
        heroCfg3.heroTexture = "images/hero3/Heroshoot.png";
        heroCfg3.bulletTexture = "images/hero3/Bullet.png";

        // 2️⃣ 加载贴图资源（不放 create() 全局加载）
        ResourceService rs = ServiceLocator.getResourceService();
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
                        "images/hero3/gun3.png"
                        // 也可以暂时用 heroCfg.heroTexture 等
                ));
        spawnEntity(heroWeaponBar);

        spawnEntityAt(hero, cell, true, true);

        // 6️⃣ 一次性提示窗口
        if (!heroHintShown) {
            var stage = ServiceLocator.getRenderService().getStage();
            new com.csse3200.game.ui.Hero.HeroHintDialog(hero).showOnceOn(stage);
            heroHintShown = true;
        }

    }


    private void spawnEngineerAt(GridPoint2 cell) {
        // 1) 只读取工程师配置
        EngineerConfig engCfg = FileLoader.readClass(EngineerConfig.class, "configs/engineer.json");
        if (engCfg == null) {
            logger.warn("Failed to load configs/engineer.json, using default EngineerConfig.");
            engCfg = new EngineerConfig();
        }

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

        // 6) 一次性提示
        if (!heroHintShown) {
            var stage = ServiceLocator.getRenderService().getStage();
            new com.csse3200.game.ui.Hero.HeroHintDialog(engineer).showOnceOn(stage);
            heroHintShown = true;
        }
    }

    private void spawnSamuraiAt(GridPoint2 cell) {
        // 1) 读 samurai 配置
        SamuraiConfig samCfg = FileLoader.readClass(SamuraiConfig.class, "configs/samurai.json");
        if (samCfg == null) {
            logger.warn("Failed to load configs/samurai.json, using default SamuraiConfig.");
            samCfg = new SamuraiConfig();
        }

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

        // 7) 一次性提示
        if (!heroHintShown) {
            var stage = ServiceLocator.getRenderService().getStage();
            new com.csse3200.game.ui.Hero.HeroHintDialog(samurai).showOnceOn(stage);
            heroHintShown = true;
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
            if (enemySpawnQueue != null) {
                enemySpawnQueue.clear();
                enemySpawnQueue = null;
            }
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
    }
}



