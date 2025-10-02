package com.csse3200.game.areas;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.hero.HeroSelectionComponent;
import com.csse3200.game.components.hero.HeroUpgradeComponent;
import com.csse3200.game.components.maingame.TowerUpgradeMenu;
import com.csse3200.game.services.SelectedHeroService;
import com.csse3200.game.utils.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.maingame.MainGameWin;
import com.csse3200.game.components.hero.HeroPlacementComponent;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.entities.configs.HeroConfig2;
import com.csse3200.game.entities.configs.HeroConfig3;
import com.csse3200.game.entities.configs.EngineerConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.components.maingame.MapHighlighter;
import com.badlogic.gdx.graphics.Camera;

import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.List;


import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.hero.HeroOneShotFormSwitchComponent;
import com.csse3200.game.components.maingame.SimplePlacementController;
import com.csse3200.game.components.CameraZoomDragComponent;


/**
 * Forest area for the demo game with trees, a player, and some enemies.
 */
public class ForestGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);

    private static final int NUM_DRONES = 5;
    private static final int NUM_GRUNTS = 3;
    private static final int NUM_TANKS = 2;
    private static final int NUM_BOSSES = 1;
    private static final int NUM_DIVIDERS = 1;
    public static int NUM_ENEMIES_TOTAL = 0;
    public static int NUM_ENEMIES_DEFEATED = 0;

    private Timer.Task waveSpawnTask;
    private List<Runnable> enemySpawnQueue;
    private boolean waveInProgress = false;
    private float spawnDelay = 2f; // Delay between spawns

    // When loading from a save/continue, we don't want to auto-start waves and duplicate enemies
    private boolean autoStartWaves = true;

    public static Difficulty gameDifficulty = Difficulty.EASY;

    private static ForestGameArea currentGameArea;

    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(31, 6);
    private static final float WALL_WIDTH = 0.1f;


    private static final String[] forestTextureAtlases = {
            "images/grunt_basic_spritesheet.atlas", "images/drone_basic_spritesheet.atlas", "images/tank_basic_spritesheet.atlas",
            "images/boss_basic_spritesheet.atlas"
    };

    private static final String[] forestSounds = {
            "sounds/homebase_hit_sound.mp3",
            CurrencyManagerComponent.SOUND_PATH
    };
    private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
    private static final String[] forestMusic = {backgroundMusic};

    private final TerrainFactory terrainFactory;
    private Entity player;
    private boolean hasExistingPlayer = false;
    private MapEditor mapEditor;

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
            {15, 9}, {16, 8}, {17, 10}, {19, 10}, {14, 6}, {10, 3}, {13, 5}, {5, 4}, {7, 4}, {3, 8}, {15, 3}};

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

    /**
     * Initialize and start the enemy wave spawning
     */
    private void startEnemyWave() {
        if (waveInProgress) return;

        waveInProgress = true;
        enemySpawnQueue = new ArrayList<>();

        buildSpawnQueue();

        scheduleNextEnemySpawn();
    }


    /**
     * Build the queue of enemies to spawn in wave order
     */
    private void buildSpawnQueue() {
        NUM_ENEMIES_TOTAL = (NUM_DRONES + NUM_GRUNTS + NUM_TANKS + NUM_BOSSES + (NUM_DIVIDERS * 4));
        NUM_ENEMIES_DEFEATED = 0;
        // Add drones to spawn queue
        for (int i = 0; i < NUM_DRONES; i++) {
            enemySpawnQueue.add(this::spawnSingleDrone);
        }

        // Add grunts to spawn queue
        for (int i = 0; i < NUM_GRUNTS; i++) {
            enemySpawnQueue.add(this::spawnSingleGrunt);
        }

        // Add tanks to spawn queue
        for (int i = 0; i < NUM_TANKS; i++) {
            enemySpawnQueue.add(this::spawnSingleTank);
        }

        // Add dividers to spawn queue
        for (int i = 0; i < NUM_DIVIDERS; i++) {
            enemySpawnQueue.add(this::spawnSingleDivider);
        }

        // Add bosses to spawn queue
        for (int i = 0; i < NUM_BOSSES; i++) {
            enemySpawnQueue.add(this::spawnSingleBoss);
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
            // Wave complete
            waveInProgress = false;
            logger.info("Wave completed successfully");
            return;
        }

        // Cancel any existing spawn task
        if (waveSpawnTask != null) {
            waveSpawnTask.cancel();
        }

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
        }, spawnDelay);
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

        // Create camera control entity for zoom and drag functionality
        Entity cameraControl = new Entity();
        cameraControl.addComponent(new CameraZoomDragComponent());
        spawnEntity(cameraControl);

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

        if (autoStartWaves) {
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

        spawnEntity(highlighterEntity);

        //Tower Upgrade Menu
        TowerUpgradeMenu towerUpgradeMenu = new TowerUpgradeMenu();
        Entity upgradeUI = new Entity().addComponent(towerUpgradeMenu);
        spawnEntity(upgradeUI);

        //Link the upgrade menu to the map highlighter
        mapHighlighter.setTowerUpgradeMenu(towerUpgradeMenu);

        // Add hero placement system

        //Entity placement = new Entity().addComponent(new HeroPlacementComponent(terrain,mapEditor, this::spawnEngineerAt));

        //spawnEntity(placement);
        var svc = ServiceLocator.getSelectedHeroService();
        if (svc == null) {
            throw new IllegalStateException("SelectedHeroService not registered before MAIN_GAME!");
        }
        SelectedHeroService.HeroType chosen = svc.getSelected();
        Gdx.app.log("ForestGameArea", "chosen=" + chosen);

// 根据选择安装一个只放“指定英雄”的放置器
        java.util.function.Consumer<com.badlogic.gdx.math.GridPoint2> placeCb =
                (chosen == SelectedHeroService.HeroType.ENGINEER) ? this::spawnEngineerAt : this::spawnHeroAt;

        Entity placementEntity = new Entity().addComponent(
                new com.csse3200.game.components.hero.HeroPlacementComponent(terrain, mapEditor, placeCb)
        );
        spawnEntity(placementEntity);


        playMusic();

        HeroConfig cfg1 = new HeroConfig();
        cfg1.heroTexture = "images/hero/Heroshoot.png";
        cfg1.bulletTexture = "images/hero/Bullet.png";

        HeroConfig2 cfg2 = new HeroConfig2();
        cfg2.heroTexture = "images/hero2/Heroshoot.png";
        cfg2.bulletTexture = "images/hero2/Bullet.png";

        HeroConfig3 cfg3 = new HeroConfig3();
        cfg3.heroTexture = "images/hero3/Heroshoot.png";
        cfg3.bulletTexture = "images/hero3/Bullet.png";

// 一次性换肤组件，注册到 EntityService
        Entity skinSwitcher = new Entity().addComponent(
                new com.csse3200.game.components.hero.HeroOneShotFormSwitchComponent(cfg1, cfg2, cfg3)
        );
        com.csse3200.game.services.ServiceLocator.getEntityService().register(skinSwitcher);


    }

    private void spawnTerrain() {
        terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO);
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


    //Register to MapEditor’s invalidTiles and generate obstacles on the map.
    private void registerBarrierAndSpawn(int[][] coords) {
        if (coords == null) return;
        // 如果 mapEditor 还未创建，先缓存到本地生成；MapEditor 在 spawnPlayer() 中创建后再注册
        for (int[] p : coords) {
            if (p == null || p.length != 2) continue;
            spawnEntityAt(ObstacleFactory.createBarrier(), new GridPoint2(p[0], p[1]), true, false);
        }
        if (mapEditor != null) {
            mapEditor.registerBarrierCoords(coords);
        }
    }

    //注册雪树到 MapEditor 的 invalidTiles，并在地图上生成雪树障碍物。
    //Register snowtrees to MapEditor's invalidTiles and generate snowtree obstacles on the map.
    private void registerSnowTreeAndSpawn(int[][] coords) {
        if (coords == null) return;
        // 如果 mapEditor 还未创建，先缓存到本地生成；MapEditor 在 spawnPlayer() 中创建后再注册
        for (int[] p : coords) {
            if (p == null || p.length != 2) continue;
            spawnEntityAt(ObstacleFactory.createSnowTree(), new GridPoint2(p[0], p[1]), true, false);
        }
        if (mapEditor != null) {
            mapEditor.registerSnowTreeCoords(coords);
        }
    }

    private Entity spawnPlayer() {
        Entity newPlayer = PlayerFactory.createPlayer();
        // 确保新玩家有钱包组件
        if (newPlayer.getComponent(CurrencyManagerComponent.class) == null) {
            newPlayer.addComponent(new CurrencyManagerComponent(/* 可选初始值 */));
        }

        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);

        // Initialize MapEditor
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
        return (mapEditor != null) ? mapEditor.waypointList : java.util.Collections.emptyList();
    }

    private void spawnSingleDrone() {
        //NUM_ENEMIES_TOTAL++;
        Entity drone = DroneEnemyFactory.createDroneEnemy(mapEditor.waypointList, player, gameDifficulty);
        spawnEntityAt(drone, new GridPoint2(0, 10), true, true);
        logger.debug("Spawned drone. Total enemies: {}", NUM_ENEMIES_TOTAL);
    }

    private void spawnSingleGrunt() {
        //NUM_ENEMIES_TOTAL++;
        Entity grunt = GruntEnemyFactory.createGruntEnemy(mapEditor.waypointList, player, gameDifficulty);
        spawnEntityAt(grunt, new GridPoint2(0, 10), true, true);
        logger.debug("Spawned grunt. Total enemies: {}", NUM_ENEMIES_TOTAL);
    }

    private void spawnSingleTank() {
        //NUM_ENEMIES_TOTAL++;
        Entity tank = TankEnemyFactory.createTankEnemy(mapEditor.waypointList, player, gameDifficulty);
        spawnEntityAt(tank, new GridPoint2(0, 10), true, true);
        logger.debug("Spawned tank. Total enemies: {}", NUM_ENEMIES_TOTAL);
    }

    private void spawnSingleBoss() {
        //NUM_ENEMIES_TOTAL++;
        Entity boss = BossEnemyFactory.createBossEnemy(mapEditor.waypointList, player, gameDifficulty);
        spawnEntityAt(boss, new GridPoint2(0, 10), true, true);
        logger.debug("Spawned boss. Total enemies: {}", NUM_ENEMIES_TOTAL);
    }

    private void spawnSingleDivider() {
        //NUM_ENEMIES_TOTAL += 4; // Dividers count as 4 enemies
        Entity divider = DividerEnemyFactory.createDividerEnemy(mapEditor.waypointList, this, player, gameDifficulty);
        spawnEntityAt(divider, new GridPoint2(0, 10), true, true);
        logger.debug("Spawned divider. Total enemies: {}", NUM_ENEMIES_TOTAL);
    }

    public static void checkEnemyCount() {
        if (NUM_ENEMIES_DEFEATED >= NUM_ENEMIES_TOTAL) {
            // Only try to access UI if we're in a real game environment
            if (MainGameScreen.ui != null) {
                MainGameWin winComponent = MainGameScreen.ui.getComponent(MainGameWin.class);
                if (winComponent != null) {
                    winComponent.addActors();
                }
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

        // 4) 放置
        spawnEntityAt(engineer, cell, true, true);

        // 5) 一次性提示
        if (!heroHintShown) {
            var stage = ServiceLocator.getRenderService().getStage();
            new com.csse3200.game.ui.HeroHintDialog(engineer).showOnceOn(stage);
            heroHintShown = true;
        }
    }

    private void playMusic() {
        // Route all music through AudioService to avoid overlaps across screens
        if (ServiceLocator.getAudioService() != null) {
            ServiceLocator.getAudioService().registerMusic("forest_bgm", backgroundMusic);
            ServiceLocator.getAudioService().playMusic("forest_bgm", true);
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
        super.dispose();
        if (mapEditor != null) {
            mapEditor.cleanup();
        }
        if (ServiceLocator.getAudioService() != null) {
            ServiceLocator.getAudioService().stopMusic();
        } else {
            ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
        }
        this.unloadAssets();
    }
}