package com.csse3200.game.areas;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;


public class MapEditor extends InputAdapter {
    private TerrainComponent terrain;

    // Tree / Path / Placement Area records树 / 路径 / 可放置区域 记录
    private Map<String, GridPoint2> pathTiles = new HashMap<>();
    private Map<String, GridPoint2> invalidTiles = new HashMap<>();
    private Map<String, GridPoint2> placeableAreaTiles = new HashMap<>();
    private Map<String, GridPoint2> barrierTiles = new HashMap<>();
    private Map<String, GridPoint2> snowTreeTiles = new HashMap<>();
    
    // Occupied tiles to avoid obstacle overlap已占用的格子，避免障碍物重叠
    private Set<String> occupiedTiles = new HashSet<>();

    // Tile types瓦片类型
    private TiledMapTile pathTile;
    private TiledMapTile keypointTile;
    private TiledMapTile snowTile;
    // Key path points list关键路径点列表
    private java.util.List<GridPoint2> keyWaypoints = new java.util.ArrayList<>();
    private java.util.List<GridPoint2> snowCoords = new java.util.ArrayList<>();

    public MapEditor(TerrainComponent terrain, Entity player) {
        this.terrain = terrain;
        initializePathTile();
        initializeKeypointTile();
        initializeSnowTile();
    }

    /** Initialize path tiles初始化路径瓦片 */
    private void initializePathTile() {
        try {
            Texture pathTexture = ServiceLocator.getResourceService().getAsset("images/path.png", Texture.class);
            // Avoid blurring when zooming
            pathTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            // Make the path tile size consistent with the base tile layer to prevent size anomalies
            TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
            int tileW = baseLayer.getTileWidth();
            int tileH = baseLayer.getTileHeight();
            int regionW = Math.min(tileW, pathTexture.getWidth());
            int regionH = Math.min(tileH, pathTexture.getHeight());
            TextureRegion region = new TextureRegion(pathTexture, 0, 0, regionW, regionH);
            pathTile = new StaticTiledMapTile(region);
            System.out.println("Path tile initialized successfully");
        } catch (Exception e) {
            System.out.println("Path tile initialization failed: " + e.getMessage());
            pathTile = null;
        }
    }


    /** Initialize keypoint tiles初始化关键点瓦片 */
    private void initializeKeypointTile() {
        try {
            Texture keypointTexture = ServiceLocator.getResourceService().getAsset("images/path_keypoint.png", Texture.class);
            // Avoid blurring when zooming
            keypointTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            // Make the keypoint tile size consistent with the base tile layer to prevent size anomalies使关键点瓦片尺寸与基础图层瓦片一致，防止尺寸异常
            TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
            int tileW = baseLayer.getTileWidth();
            int tileH = baseLayer.getTileHeight();
            int regionW = Math.min(tileW, keypointTexture.getWidth());
            int regionH = Math.min(tileH, keypointTexture.getHeight());
            TextureRegion region = new TextureRegion(keypointTexture, 0, 0, regionW, regionH);
            keypointTile = new StaticTiledMapTile(region);
            System.out.println("✅ path_keypoint.png tile initialized successfully");
        } catch (Exception e) {
            System.out.println("⚠️ path_keypoint.png tile initialization failed: " + e.getMessage());
            keypointTile = null;
        }
    }

    /** Initialize snow tiles初始化雪地瓦片 */
    private void initializeSnowTile() {
        try {
            Texture snowTexture = ServiceLocator.getResourceService().getAsset("images/snow.png", Texture.class);
            // Avoid blurring when zooming避免放大时模糊
            snowTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            // Make the snow tile size consistent with the base tile layer to prevent size anomalies使雪地瓦片尺寸与基础图层瓦片一致，防止尺寸异常
            TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
            int tileW = baseLayer.getTileWidth();
            int tileH = baseLayer.getTileHeight();
            int regionW = Math.min(tileW, snowTexture.getWidth());
            int regionH = Math.min(tileH, snowTexture.getHeight());
            TextureRegion region = new TextureRegion(snowTexture, 0, 0, regionW, regionH);
            snowTile = new StaticTiledMapTile(region);
            System.out.println("✅ snow.png tile initialized successfully");
        } catch (Exception e) {
            System.out.println("⚠️ snow.png tile initialization failed: " + e.getMessage());
            snowTile = null;
        }
    }

    /** Create path tiles创建路径瓦片 */
    private void createPathTile(int tx, int ty) {
        TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        TiledMapTileLayer pathLayer = getOrCreatePathLayer(baseLayer);
        if (tx < 0 || ty < 0 || tx >= pathLayer.getWidth() || ty >= pathLayer.getHeight()) return;
        String key = tx + "," + ty;
        if (pathTiles.containsKey(key)) return;

        //if (placedTrees.containsKey(key)) placedTrees.remove(key).dispose();

        if (pathTile != null) {
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(pathTile);
            pathLayer.setCell(tx, ty, cell);
        }
        pathTiles.put(key, new GridPoint2(tx, ty));
    }

    /** Get or create path layer for path, always append to the end, ensuring it is above the base layer and mmap获取或创建用于路径的图层，始终追加到末尾，保证在基础层与mmap之上 */
    private TiledMapTileLayer getOrCreatePathLayer(TiledMapTileLayer baseLayer) {
        int count = terrain.getMap().getLayers().getCount();
        if (count > 1 && terrain.getMap().getLayers().get(count - 1) instanceof TiledMapTileLayer) {
            TiledMapTileLayer lastLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(count - 1);
            if ("path-layer".equals(lastLayer.getName())) {
                return lastLayer;
            }
        }
        TiledMapTileLayer newLayer = new TiledMapTileLayer(
                baseLayer.getWidth(), baseLayer.getHeight(),
                baseLayer.getTileWidth(), baseLayer.getTileHeight());
        newLayer.setName("path-layer");
        terrain.getMap().getLayers().add(newLayer);
        return newLayer;
    }

    /** Automatically generate enemy paths自动生成敌人路径 */
    public void generateEnemyPath() {
        if (terrain == null) return;

        // Clear existing paths清空现有路径
        pathTiles.clear();
        keyWaypoints.clear();

        // Predefined fixed path coordinates (x, y)预定义固定路径坐标 (x, y)
        int[][] fixedPath = {
                // Start from the left
                {0, 10}, {1, 10}, {2, 10}, {3, 10}, {4, 10},

                // First turn up
                {5, 10}, {5, 9}, {5, 8}, {5, 7}, {5, 6},

                // Walk to the right first segment
                {6, 6}, {7, 6}, {8, 6}, {9, 6}, {10, 6}, {11, 6},

                // Turn down
                {12, 6}, {12, 7}, {12, 8}, {12, 9}, {12, 10}, {12, 11}, {12, 12},

                // Walk to the right for a longer distance
                {13, 12}, {14, 12}, {15, 12}, {16, 12}, {17, 12}, {18, 12},
                {19, 12}, {20, 12}, {21, 12}, {22, 12}, {23, 12}, {24, 12},

                // Turn up
                {25, 12}, {25, 11}, {25, 10}, {25, 9}, {25, 8}, {25, 7}, {25, 6},

                // Finally walk to the right for 4 coordinates
                {26, 6}, {27, 6}, {28, 6}, {29, 6}
        };

        // Create path tiles based on predefined path根据预定义路径创建路径瓦片
        for (int i = 0; i < fixedPath.length; i++) {
            int x = fixedPath[i][0];
            int y = fixedPath[i][1];
            createPathTile(x, y);
        }

        // Define key path points定义关键路径点
        keyWaypoints.add(new GridPoint2(0, 10));    // Start
        keyWaypoints.add(new GridPoint2(5, 10));    // First turn
        keyWaypoints.add(new GridPoint2(5, 6));     // Up turn completed
        keyWaypoints.add(new GridPoint2(12, 6));    // Walk to the right completed
        keyWaypoints.add(new GridPoint2(12, 12));   // Down turn completed
        keyWaypoints.add(new GridPoint2(25, 12));   // Long distance to the right completed
        keyWaypoints.add(new GridPoint2(25, 6));    // Up turn completed
        keyWaypoints.add(new GridPoint2(29, 6));    // End

        // Mark key path points标记关键路径点
        for (GridPoint2 wp : keyWaypoints) {
            markKeypoint(wp);
        }
        int[][] redCircledArea = {
            {12, 17, 5, 12}, 
            {9,17,3,5}, 
            {21,23,10,19},
                {24,26,16,19},
                {25,31,18,20},
                {27,31,21,22},
                {18,24,9,12},
                {18,22,6,9},
                {0,21,0,3},
                {0,4,4,9},
                {22,24,0,2},
                {25,31,0,1},
                {30,31,2,5},
                {30,31,6,7},
        };
        
        for (int[] range : redCircledArea) {
            int startX = range[0];
            int endX = range[1];
            int startY = range[2];
            int endY = range[3];
            
            System.out.println("🔴InvalidTiles: x=" + startX + "-" + endX + ", y=" + startY + "-" + endY);
            
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    addSnow(x, y);
                }
            }
        }

       // generatePlaceableAreas();
        System.out.println("✅ Fixed path generated, number=" + pathTiles.size());
        System.out.println("✅ Key path points number=" + keyWaypoints.size());
        System.out.println("✅ Snow coordinates number=" + snowCoords.size());
    }

    /** Mark key path points标记关键路径点 */
    private void markKeypoint(GridPoint2 pos) {
        if (keypointTile == null) return;
        TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        TiledMapTileLayer pathLayer = getOrCreatePathLayer(baseLayer);
        TiledMapTileLayer.Cell cell = pathLayer.getCell(pos.x, pos.y);
        if (cell == null) {
            cell = new TiledMapTileLayer.Cell();
            pathLayer.setCell(pos.x, pos.y, cell);
        }
        cell.setTile(keypointTile);
    }

    /** Clean up all objects清理所有对象 */
    public void cleanup() {
        //for (Entity tree : placedTrees.values()) tree.dispose();
        //placedTrees.clear();
        pathTiles.clear();
        placeableAreaTiles.clear();
        snowTreeTiles.clear();
        occupiedTiles.clear();
        keyWaypoints.clear();
        snowCoords.clear();
        System.out.println("🧹 MapEditor cleaned up");
    }

    public Map<String, GridPoint2> getInvalidTiles() {
        invalidTiles.clear();
        invalidTiles.putAll(pathTiles);
        invalidTiles.putAll(barrierTiles);
        invalidTiles.putAll(snowTreeTiles);
        snowCoords.forEach(coord -> invalidTiles.put(coord.x + "," + coord.y, coord));
        return invalidTiles;
    }
     /** Add snow at specified coordinates在指定坐标添加雪地 */
     public void addSnow(int x, int y) {
        if (snowTile == null) {
            System.out.println("⚠️ Snow tile not initialized");
            return;
        }
        TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        TiledMapTileLayer pathLayer = getOrCreatePathLayer(baseLayer);
        
        if (x < 0 || y < 0 || x >= pathLayer.getWidth() || y >= pathLayer.getHeight()) {
            System.out.println("⚠️ Snow coordinates out of bounds: (" + x + ", " + y + ")");
            return;
        }
        String key = x + "," + y;
        if (pathTiles.containsKey(key) || 
            invalidTiles.containsKey(key) || 
            barrierTiles.containsKey(key)) {
            System.out.println("🚫 Position (" + x + ", " + y + ") is occupied by path/barrier, skipping");
            return;
        }
         // Add to snow coordinates list添加到雪地坐标列表
         snowCoords.add(new GridPoint2(x, y));
        
         // Create snow tile创建雪地瓦片
         TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
         cell.setTile(snowTile);
         pathLayer.setCell(x, y, cell);
         
         System.out.println("✅ Snow added at coordinates (" + x + ", " + y + ")");
     }

    /**
     * Register the coordinates of the obstacles for getInvalidTiles() to return uniformly
     * coords: int[][]，Each element is {x, y}
     */
    public void registerBarrierCoords(int[][] coords) {
        if (coords == null) return;
        for (int[] p : coords) {
            if (p == null || p.length != 2) continue;
            String key = p[0] + "," + p[1];
            barrierTiles.put(key, new GridPoint2(p[0], p[1]));
        }
    }

    /**
     * Register the snow tree coordinates for getInvalidTiles() to return uniformly
     * coords: int[][]，Each element is {x, y}
     */
    public void registerSnowTreeCoords(int[][] coords) {
        if (coords == null) return;
        for (int[] p : coords) {
            if (p == null || p.length != 2) continue;
            String key = p[0] + "," + p[1];
            snowTreeTiles.put(key, new GridPoint2(p[0], p[1]));
        }
    }


}