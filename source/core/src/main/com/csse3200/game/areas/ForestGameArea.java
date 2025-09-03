package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.NPCFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

public class ForestGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);
    private static final int NUM_TREES = 7;
    private static final int NUM_GHOSTS = 2;
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
            "images/river.png"
    };

    private static final String[] forestTextureAtlases = {
            "images/terrain_iso_grass.atlas", "images/ghost.atlas", "images/ghostKing.atlas"
    };

    private static final String[] forestSounds = {"sounds/Impact4.ogg"};
    private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
    private static final String[] forestMusic = {backgroundMusic};

    private final TerrainFactory terrainFactory;
    private MapEditor mapEditor;
    private Entity player;

    public ForestGameArea(TerrainFactory terrainFactory) {
        super();
        this.terrainFactory = terrainFactory;
    }

    @Override
    public void create() {
        loadAssets();
        displayUI();

        spawnTerrain();                // Generate terrain and fill the grassland生成地形并填充草地
        player = spawnPlayer();        // Initialize player and mapEditor初始化玩家和mapEditor
        mapEditor.generateEnemyPath(); // Generate fixed enemy path生成固定敌人路径
        generateBiomesAndRivers();     // Generate desert/snow/rivers生成沙漠/雪地/河流
        mapEditor.generatePlaceableAreas(); // 显示可放置防御塔区域
        //spawnTrees();                  // Generate trees生成树木
        //spawnGhosts();                 // Generate ghosts生成幽灵
       // spawnGhostKing();              // 生成幽灵王

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

        spawnEntityAt(
                ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y),
                GridPoint2Utils.ZERO, false, false);
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y),
                new GridPoint2(tileBounds.x, 0),
                false,
                false);
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH),
                new GridPoint2(0, tileBounds.y),
                false,
                false);
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH),
                GridPoint2Utils.ZERO, false, false);
    }

//    private void spawnTrees() {
//        GridPoint2 minPos = new GridPoint2(0, 0);
//        GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);
//
//        for (int i = 0; i < NUM_TREES; i++) {
//            GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
//            Entity tree = ObstacleFactory.createTree();
//            spawnEntityAt(tree, randomPos, true, false);
//        }
//    }

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

//    private void spawnGhosts() {
//        GridPoint2 minPos = new GridPoint2(0, 0);
//        GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);
//
//        for (int i = 0; i < NUM_GHOSTS; i++) {
//            GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
//            Entity ghost = NPCFactory.createGhost(player);
//            spawnEntityAt(ghost, randomPos, true, true);
//        }
//    }

//    private void spawnGhostKing() {
//        GridPoint2 minPos = new GridPoint2(0, 0);
//        GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);
//
//        GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
//        Entity ghostKing = NPCFactory.createGhostKing(player);
//        spawnEntityAt(ghostKing, randomPos, true, true);
//    }

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