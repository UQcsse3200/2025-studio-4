package com.csse3200.game.areas;

import com.csse3200.game.components.hero.HeroUpgradeComponent;
import com.csse3200.game.components.maingame.TowerUpgradeMenu;
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
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.components.maingame.MapHighlighter;
import com.badlogic.gdx.graphics.Camera;


import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.hero.HeroOneShotFormSwitchComponent;
import com.csse3200.game.components.maingame.SimplePlacementController;
import com.csse3200.game.components.CameraZoomDragComponent;


/**
 * Forest area for the demo game with trees, a player, and some enemies.
 */
public class ForestGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);

    private static final int NUM_DRONES = 3;
    private static final int NUM_GRUNTS = 2;
    private static final int NUM_TANKS = 2;
    private static final int NUM_BOSSES = 1;
    private static final int NUM_DIVIDERS = 1;
    public static final int NUM_ENEMIES_TOTAL = NUM_BOSSES + NUM_DRONES + NUM_GRUNTS + NUM_TANKS + (1 + NUM_DIVIDERS * 3);
    public static int NUM_ENEMIES_DEFEATED = 0;


    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(31, 6);
    private static final float WALL_WIDTH = 0.1f;


    private static final String[] forestTextureAtlases = {
            "images/grunt_basic_spritesheet.atlas", "images/drone_basic_spritesheet.atlas", "images/tank_basic_spritesheet.atlas",
            "images/boss_basic_spritesheet.atlas"
    };

    private static final String[] forestSounds = {
            "sounds/homebase_hit_sound.mp3"
    };
    private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
    private static final String[] forestMusic = {backgroundMusic};

    private final TerrainFactory terrainFactory;
    private Entity player;
    private boolean hasExistingPlayer = false;
    private MapEditor mapEditor;


    // 障碍物坐标单一事实源：由关卡（GameArea）定义
    // create barriers areas
    private static final int[][] BARRIER_COORDS = new int[][]{
            {27, 9}, {28, 9}, {29, 9}, {30, 9}, {31, 9},
            {26, 3}, {27, 3}, {28, 3}, {29, 3}, 
             {5, 24}, {8, 20},
             // 在x<31且y>13且x<13范围内随机添加的坐标点
             {8, 15}, {5, 17}, {11, 14}, {3, 18}, 
             {7, 25}, {2, 15},  {6, 29}, 
    };

    // create snowtree areas - 避开路径坐标
    private static final int[][] SNOWTREE_COORDS = new int[][]{
            {15, 9},{16,8},{17,10},{19,10},{14,6},{10,3},{13,5},{5,4},{7,4},{3,8},{15,3 }    };

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
     * Create the game area, including terrain, static entities (trees), dynamic entities (player)
     */
    @Override
    public void create() {
        // Load assets (textures, sounds, etc.) before creating anything that needs them
        loadAssets();


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
                // ✅ 确保旧的 player 也有 CurrencyManagerComponent
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

        // Enemies
        spawnDrones();

        spawnGrunts();

        spawnTanks();

        spawnBosses();

        spawnDividers();


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

        Entity placement = new Entity().addComponent(new HeroPlacementComponent(terrain, this::spawnHeroAt));

        spawnEntity(placement);

        playMusic();


        // 1) 准备三套配置（你已有 HeroConfig / HeroConfig2 / HeroConfig3）
        HeroConfig cfg1 = new HeroConfig();
        cfg1.heroTexture = "images/hero/Heroshoot.png";
        cfg1.bulletTexture = "images/hero/Bullet.png";

        HeroConfig2 cfg2 = new HeroConfig2();
        cfg2.heroTexture = "images/hero2/Heroshoot.png";
        cfg2.bulletTexture = "images/hero2/Bullet.png";

        HeroConfig3 cfg3 = new HeroConfig3();
        cfg3.heroTexture = "images/hero3/Heroshoot.png";
        cfg3.bulletTexture = "images/hero3/Bullet.png";

        // 2) 挂载“一次性换肤”组件（不会改变你其它逻辑）
        Entity skinSwitcher = new Entity().addComponent(
                new com.csse3200.game.components.hero.HeroOneShotFormSwitchComponent(cfg1, cfg2, cfg3)
        );
        com.csse3200.game.services.ServiceLocator.getEntityService().
                register(skinSwitcher);

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

    private void spawnDrones() {
        GridPoint2 minPos = new GridPoint2(0, 0);
        GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);
        for (int i = 0; i < NUM_DRONES; i++) {
            GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
            Entity drone = DroneEnemyFactory.createDroneEnemy(player);
            spawnEntityAt(drone, randomPos, true, true);
        }
    }

    private void spawnGrunts() {
        GridPoint2 minPos = new GridPoint2(0, 0);
        GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);
        for (int i = 0; i < NUM_GRUNTS; i++) {
            GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
            Entity grunt = GruntEnemyFactory.createGruntEnemy(player);
            spawnEntityAt(grunt, randomPos, true, true);
        }
    }

    private void spawnTanks() {
        GridPoint2 minPos = new GridPoint2(0, 0);
        GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);
        for (int i = 0; i < NUM_TANKS; i++) {
            GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
            Entity tank = TankEnemyFactory.createTankEnemy(player);
            spawnEntityAt(tank, randomPos, true, true);
        }
    }

    private void spawnBosses() {
        GridPoint2 minPos = new GridPoint2(0, 0);
        GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);
        for (int i = 0; i < NUM_BOSSES; i++) {
            GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
            Entity boss = BossEnemyFactory.createBossEnemy(player);
            spawnEntityAt(boss, randomPos, true, true);
        }
    }

    private void spawnDividers() {
        GridPoint2 minPos = new GridPoint2(0, 0);
        GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);
        for (int i = 0; i < 1; i++) {
            GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
            Entity divider2 = DividerEnemyFactory.createDividerEnemy(player, this);
            spawnEntityAt(divider2, randomPos, true, true);
        }
    }

    public static void checkEnemyCount() {
        if (NUM_ENEMIES_DEFEATED >= NUM_ENEMIES_TOTAL) {
            MainGameScreen.ui.getComponent(MainGameWin.class).addActors();

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

        // ✅ 在创建 hero 前预加载资源
        ResourceService rs = ServiceLocator.getResourceService();
        HeroFactory.loadAssets(rs, heroCfg, heroCfg2, heroCfg3);
        while (!rs.loadForMillis(10)) {
            logger.info("Loading hero assets... {}%", rs.getProgress());
        }

        Camera cam = Renderer.getCurrentRenderer().getCamera().getCamera();
        Entity hero = HeroFactory.createHero(heroCfg, cam);

        // attachPlayer 等逻辑照旧
        var up = hero.getComponent(HeroUpgradeComponent.class);
        if (up != null) {
            up.attachPlayer(player);
        }

        spawnEntityAt(hero, cell, true, true);
    }

    private void playMusic() {
        Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
        music.setLooping(true);
        music.setVolume(0.3f);
        music.play();
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

    @Override
    public void dispose() {
        super.dispose();
        if (mapEditor != null) {
            mapEditor.cleanup();
        }
        ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
        this.unloadAssets();
    }
}