package com.csse3200.game.areas;

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
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.components.maingame.MapHighlighter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.graphics.Camera;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.maingame.SimplePlacementController;

/**
 * Forest area for the demo game with trees, a player, and some enemies.
 */
public class ForestGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);
    private static final int NUM_TREES = 7;
    private static final int NUM_DRONES = 3;
    private static final int NUM_GRUNTS = 2;
    private static final int NUM_TANKS = 2;
    private static final int NUM_BOSSES = 1;
    private static final int NUM_DIVIDERS = 1;
    public static final int NUM_ENEMIES_TOTAL = NUM_BOSSES + NUM_DRONES + NUM_GRUNTS + NUM_TANKS + (1 + NUM_DIVIDERS * 3);
    public static int NUM_ENEMIES_DEFEATED = 0;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);
    private static final float WALL_WIDTH = 0.1f;
    private static final String[] forestTextures = {
            "images/mmap.png",
            "images/box_boy_leaf.png",
            "images/crystal.png",
            "images/tree.png",
            "images/path.png",
            "images/path_keypoint.png",
            "images/ghost_king.png",
            "images/ghost_1.png",
            "images/grass_1.png",
            "images/grass_2.png",
            "images/grass_3.png",
            "images/hex_grass_1.png",
            "images/hex_grass_2.png",
            "images/hex_grass_3.png",
            "images/iso_grass_1.png",
            "images/iso_grass_2.png",
            "images/iso_grass_3.png",
            "images/desert.png",
            "images/snow.png",
            "images/river.png",
            "images/drone_enemy.png",
            "images/base_enemy.png",
            "images/tank_enemy.png",
            "images/boss_enemy.png",
            "images/hero/Heroshoot.png",
            "images/hero/Bullet.png",
            "images/metal-scrap-currency.png",
            "images/bone.png",
            "images/cavemen.png",
            "images/dino.png"
    };

    private static final String[] forestTextureAtlases = {
            "images/terrain_iso_grass.atlas", "images/ghost.atlas", "images/ghostKing.atlas",
            "images/grunt_basic_spritesheet.atlas", "images/drone_basic_spritesheet.atlas", "images/tank_basic_spritesheet.atlas",
            "images/boss_basic_spritesheet.atlas"
    };

    private static final String[] forestSounds = {"sounds/Impact4.ogg"};
    private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
    private static final String[] forestMusic = {backgroundMusic};

    private final TerrainFactory terrainFactory;
    private Entity player;
    private boolean hasExistingPlayer = false;
    private MapEditor mapEditor;

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
     * @param hasExistingPlayer true if player already exists, false otherwise
     */
    public void setHasExistingPlayer(boolean hasExistingPlayer) {
        this.hasExistingPlayer = hasExistingPlayer;
    }

    /** Create the game area, including terrain, static entities (trees), dynamic entities (player) */
    @Override
    public void create() {
        loadAssets();
        displayUI();

        // Create UI entity + placement controller
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Box Forest"));
        ui.addComponent(new com.csse3200.game.components.maingame.TowerHotbarDisplay());
        SimplePlacementController placementController = new SimplePlacementController();
        ui.addComponent(placementController);
        spawnEntity(ui);

        spawnTerrain();
        spawnTrees();

        // Spawn or retrieve player
        if (!hasExistingPlayer) {
            player = spawnPlayer();
        } else {
            player = findExistingPlayer();
            if (player == null) {
                logger.warn("Expected existing player not found, creating new one");
                player = spawnPlayer();
            }
        }

        // ✅ Now that mapEditor is created in spawnPlayer, link it to placementController
        if (mapEditor != null) {
            placementController.setMapEditor(mapEditor);
        }

        // Enemies
        spawnDrones();
        spawnGrunts();
        spawnTanks();
        spawnBosses();
        spawnDividers();

        spawnTestMetalScraps();

        // Generate biomes & placeable areas
        mapEditor.generateBiomesAndRivers();
        mapEditor.generatePlaceableAreas();

        // Tower placement highlighter
        MapHighlighter mapHighlighter =
                new MapHighlighter(terrain, placementController, new com.csse3200.game.entities.factories.TowerFactory());
        Entity highlighterEntity = new Entity().addComponent(mapHighlighter);
        spawnEntity(highlighterEntity);

        // Hero placement
        Entity placement = new Entity().addComponent(new HeroPlacementComponent(terrain, this::spawnHeroAt));
        spawnEntity(placement);

        playMusic();
    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Box Forest"));
        spawnEntity(ui);
    }

    private void spawnTerrain() {
        terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO);
        spawnEntity(new Entity().addComponent(terrain));

        // Fill background with grass
        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        Texture grassTex = ServiceLocator.getResourceService().getAsset("images/grass_1.png", Texture.class);
        TiledMapTile grassTile = new StaticTiledMapTile(new TextureRegion(grassTex));

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(grassTile);
                layer.setCell(x, y, cell);
            }
        }

        // Boundary walls
        float tileSize = terrain.getTileSize();
        GridPoint2 tileBounds = terrain.getMapBounds(0);
        Vector2 worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

        spawnEntityAt(ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y), GridPoint2Utils.ZERO, false, false);
        spawnEntityAt(ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y), new GridPoint2(tileBounds.x, 0), false, false);
        spawnEntityAt(ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH), new GridPoint2(0, tileBounds.y), false, false);
        spawnEntityAt(ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH), GridPoint2Utils.ZERO, false, false);
    }

    private void spawnTrees() {
        GridPoint2 minPos = new GridPoint2(0, 0);
        GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

        for (int i = 0; i < NUM_TREES; i++) {
            GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
            Entity tree = ObstacleFactory.createTree();
            spawnEntityAt(tree, randomPos, true, false);
        }
    }

    private Entity spawnPlayer() {
        Entity newPlayer = PlayerFactory.createPlayer();
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);

        // Init MapEditor
        mapEditor = new MapEditor(terrain, newPlayer);
        mapEditor.enableEditor();
        mapEditor.generateEnemyPath();
        mapEditor.spawnCrystal();

        return newPlayer;
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

    private Entity findExistingPlayer() {
        for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
            if (entity.getComponent(com.csse3200.game.components.player.PlayerActions.class) != null) {
                return entity;
            }
        }
        return null;
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

    public void spawnEntityAt(Entity entity, GridPoint2 location) {
        location = new GridPoint2(5, 5);
        spawnEntityAt(entity, location, true, true);
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
        Renderer r = Renderer.getCurrentRenderer();
        if (r == null || r.getCamera() == null) {
            logger.warn("Renderer/Camera not ready, skip spawnHeroAt.");
            return;
        }
        Camera cam = r.getCamera().getCamera();
        Entity hero = HeroFactory.createHero(heroCfg, cam);
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
        resourceService.loadTextures(forestTextures);
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
        resourceService.unloadAssets(forestTextures);
        resourceService.unloadAssets(forestTextureAtlases);
        resourceService.unloadAssets(forestSounds);
        resourceService.unloadAssets(forestMusic);
    }

    private void spawnTestMetalScraps() {
        GridPoint2 minPos = new GridPoint2(0, 0);
        GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);
        final int METAL_SCRAPS_COUNT = 10;
        for (int i = 0; i < METAL_SCRAPS_COUNT; i++) {
            GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
            float x = randomPos.x * terrain.getTileSize();
            float y = randomPos.y * terrain.getTileSize();
            Entity metalScrap = CurrencyFactory.createMetalScrap(x, y);
            player.getComponent(CurrencyManagerComponent.class).addCurrencyEntity(metalScrap);
            spawnEntity(metalScrap);
        }
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
