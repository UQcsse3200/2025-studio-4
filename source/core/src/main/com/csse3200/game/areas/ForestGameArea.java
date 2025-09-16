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

/**
 * Forest area for the demo game with trees, a player, and some enemies.
 */
public class ForestGameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);
  private static final int NUM_TREES = 0;
  private static final int NUM_DRONES = 1;
  private static final int NUM_GRUNTS = 1;
  private static final int NUM_TANKS = 1;
  private static final int NUM_BOSSES = 1;
  private static final int NUM_DIVIDERS = 1;
  public static int NUM_ENEMIES_TOTAL = 0;
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
    // Load assets (textures, sounds, etc.) before creating anything that needs them
    loadAssets();

    // Set up the UI display for the game area
    displayUI();

    // Create the main UI entity that will handle area info, hotbar, and tower placement
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Box Forest")); // Shows the game area's name
    ui.addComponent(new com.csse3200.game.components.maingame.TowerHotbarDisplay()); // UI for selecting towers
    com.csse3200.game.components.maingame.SimplePlacementController placementController =
            new com.csse3200.game.components.maingame.SimplePlacementController();
    ui.addComponent(placementController); // Handles user input for tower placement
    spawnEntity(ui);

    spawnTerrain();
    spawnTrees();

    
    // Only spawn new player if one doesn't already exist
    if (!hasExistingPlayer) {
      player = spawnPlayer();
    } else {
      // Find existing player entity
      player = findExistingPlayer();
      if (player == null) {
        logger.warn("Expected existing player not found, creating new one");
        player = spawnPlayer();
      }
    }
    

    // Spawn Enemies
    spawnDrones();
    spawnGrunts();
    spawnTanks();
    spawnBosses();
    spawnDividers();
    
    spawnTestMetalScraps();

    // Generate desert/snow/rivers生成沙漠/雪地/河流
    generateBiomesAndRivers();

    // Display the area where defense towers can be placed显示可放置防御塔区域
    mapEditor.generatePlaceableAreas();

    // Set up map highlighting for tower placement feedback
    MapHighlighter mapHighlighter =
            new MapHighlighter(terrain, placementController, new com.csse3200.game.entities.factories.TowerFactory());
    Entity highlighterEntity = new Entity().addComponent(mapHighlighter);
    spawnEntity(highlighterEntity);

    // Add hero placement system
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
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO);
    spawnEntity(new Entity().addComponent(terrain));

    // Get the tile layer (now back to index 0)获取瓦片层（现在回到索引0）
    TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
    Texture grassTex = ServiceLocator.getResourceService().getAsset("images/grass_1.png", Texture.class);
    TiledMapTile grassTile = new StaticTiledMapTile(new TextureRegion(grassTex));

    // Fill all tiles with grass用草地填充所有瓦片
    for (int x = 0; x < layer.getWidth(); x++) {
        for (int y = 0; y < layer.getHeight(); y++) {
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(grassTile);
            layer.setCell(x, y, cell);
        }
    }

    // Create boundary walls创建边界墙
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

        // Initialize map editor初始化地图编辑器
        mapEditor = new MapEditor(terrain, newPlayer);
        mapEditor.enableEditor();
        mapEditor.generateEnemyPath();  // Generate fixed enemy path生成固定敌人路径
        mapEditor.spawnCrystal();       // Generate crystal生成水晶

        return newPlayer;
    }

  private void spawnDrones() {
    for (int i = 0; i < NUM_DRONES; i++) {
      NUM_ENEMIES_TOTAL++;
      Entity drone = DroneEnemyFactory.createDroneEnemy(mapEditor.waypointList, player);
      spawnEntityAt(drone, new GridPoint2(0, 10), true, true);
    }
  }

  /**
   * Find an existing player entity in the game.
   * @return existing player entity or null if not found
   */
  private Entity findExistingPlayer() {
    for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
      if (entity.getComponent(com.csse3200.game.components.player.PlayerActions.class) != null) {
        return entity;
      }
    }
    return null;
  }

  private void spawnGrunts() {
    for (int i = 0; i < NUM_GRUNTS; i++) {
      NUM_ENEMIES_TOTAL++;
      Entity grunt = GruntEnemyFactory.createGruntEnemy(mapEditor.waypointList, player);
      spawnEntityAt(grunt, new GridPoint2(0, 10), true, true);
    }
  }

  private void spawnTanks() {
    for (int i = 0; i < NUM_TANKS; i++) {
      NUM_ENEMIES_TOTAL++;
      Entity tank = TankEnemyFactory.createTankEnemy(mapEditor.waypointList, player);
      spawnEntityAt(tank, new GridPoint2(0, 10), true, true);
    }
  }

  private void spawnBosses() {
    for (int i = 0; i < NUM_BOSSES; i++) {
      NUM_ENEMIES_TOTAL++;
      Entity boss = BossEnemyFactory.createBossEnemy(mapEditor.waypointList, player);
      spawnEntityAt(boss, new GridPoint2(0, 10), true, true);
    }
  }

  private void spawnDividers() {
    for (int i = 0; i < 1; i++) {
      NUM_ENEMIES_TOTAL = NUM_ENEMIES_TOTAL + 4;
      Entity divider2 = DividerEnemyFactory.createDividerEnemy(mapEditor.waypointList, this, player);
      spawnEntityAt(divider2, new GridPoint2(0, 10), true, true);
    }
  }

  public void spawnEntityAt(Entity entity, GridPoint2 location) {
    location = new GridPoint2(5,5);
    spawnEntityAt(entity, location, true, true);
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

    private void generateBiomesAndRivers() {
        if (mapEditor == null) {
            return;
        }
        mapEditor.generateBiomesAndRivers();
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
