package com.csse3200.game.areas2.MapTwo;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.areas2.terrainTwo.TerrainComponent2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.enemy.SpeedWaypointComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.areas.IMapEditor;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;


public class MapEditor2 extends InputAdapter implements IMapEditor {
    private TerrainComponent2 terrain;

    // Tree / Path / Placement Area records树 / 路径 / 可放置区域 记录
    private Map<String, GridPoint2> pathTiles = new HashMap<>();
    private Map<String, GridPoint2> invalidTiles = new HashMap<>();
    private Map<String, GridPoint2> placeableAreaTiles = new HashMap<>();
    private Map<String, GridPoint2> barrierTiles = new HashMap<>();
    private Map<String, GridPoint2> snowTreeTiles = new HashMap<>();
    
    // Occupied tiles to avoid obstacle overlap已占用的格子，避免障碍物重叠
    private Set<String> occupiedTiles = new HashSet<>();

    // Tile types瓦片类型
    private TiledMapTile keypointTile;
    private TiledMapTile snowTile;
    private TiledMapTile pathTile;
    // Key path points list关键路径点列表
    private java.util.List<GridPoint2> keyWaypoints = new java.util.ArrayList<>();
    private java.util.List<GridPoint2> keyWaypoints2 = new java.util.ArrayList<>();
    private java.util.List<GridPoint2> snowCoords = new java.util.ArrayList<>();

    public java.util.List<Entity> waypointList = new java.util.ArrayList<>();

    public MapEditor2(TerrainComponent2 terrain, Entity player) {
        this.terrain = terrain;
        initializeKeypointTile();
        initializeSnowTile();
        initializePathTile();
    }


    /** Initialize keypoint tiles初始化关键点瓦片 */
    private void initializeKeypointTile() {
        try {
            Texture keypointTexture = ServiceLocator.getResourceService().getAsset("images/snow.png", Texture.class);
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

    /** Initialize path tiles初始化路径瓦片 */
    private void initializePathTile() {
        try {
            Texture pathTexture = ServiceLocator.getResourceService().getAsset("images/snow.png", Texture.class);
            // Avoid blurring when zooming避免放大时模糊
            pathTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            // Make the path tile size consistent with the base tile layer to prevent size anomalies使路径瓦片尺寸与基础图层瓦片一致，防止尺寸异常
            TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
            int tileW = baseLayer.getTileWidth();
            int tileH = baseLayer.getTileHeight();
            int regionW = Math.min(tileW, pathTexture.getWidth());
            int regionH = Math.min(tileH, pathTexture.getHeight());
            TextureRegion region = new TextureRegion(pathTexture, 0, 0, regionW, regionH);
            pathTile = new StaticTiledMapTile(region);
            System.out.println("✅ path.png tile initialized successfully");
        } catch (Exception e) {
            System.out.println("⚠️ path.png tile initialization failed: " + e.getMessage());
            pathTile = null;
        }
    }


    /** Get or create layer for keypoints and other elements, always append to the end, ensuring it is above the base layer and mmap2获取或创建用于关键点和其他元素的图层，始终追加到末尾，保证在基础层与mmap2之上 */
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
        keyWaypoints2.clear();

        // 只定义关键路径点，不生成path瓦片
        // Define key path points定义关键路径点
        keyWaypoints.add(new GridPoint2(5, 0));     // Start
        keyWaypoints.add(new GridPoint2(5, 2));
        keyWaypoints.add(new GridPoint2(5, 3));
        keyWaypoints.add(new GridPoint2(5, 4));
        keyWaypoints.add(new GridPoint2(5, 5));
        keyWaypoints.add(new GridPoint2(5, 10));     // First waypoint
        keyWaypoints.add(new GridPoint2(7, 10));
        keyWaypoints.add(new GridPoint2(8, 10));
        keyWaypoints.add(new GridPoint2(9, 10));
        keyWaypoints.add(new GridPoint2(10, 10));    // Second waypoint
        keyWaypoints.add(new GridPoint2(15, 14));   // Third waypoint
        keyWaypoints.add(new GridPoint2(15, 25));   // Fifth waypoint
        keyWaypoints.add(new GridPoint2(5, 25));    // Fourth waypoint
        keyWaypoints.add(new GridPoint2(5, 32));    // End
        
        // 新增的5个关键点
        keyWaypoints2.add(new GridPoint2(28, 6));    // 新坐标5
        keyWaypoints2.add(new GridPoint2(33, 12));   // 新坐标4
        keyWaypoints2.add(new GridPoint2(33, 21));   // 新坐标3
        keyWaypoints2.add(new GridPoint2(28, 27));   // 新坐标2
        keyWaypoints2.add(new GridPoint2(18, 27));   // 新坐标1
        keyWaypoints2.add(new GridPoint2(15, 25));   // Fifth waypoint
        keyWaypoints2.add(new GridPoint2(5, 25));    // Fourth waypoint
        keyWaypoints2.add(new GridPoint2(5, 32));    // End

        Map<String, Float> speedModifiers = Map.of(
            "5,2", 0.5f,
            "5,3", 0.5f,
            "5,4", 0.5f,
            "5,5", 0.5f,
            "7,10", 0.5f,
            "8,10", 0.5f,
            "9,10", 0.5f,
            "10,10", 0.5f
        );

        // Mark key path points标记关键路径点
        for (GridPoint2 wp : keyWaypoints) {
            String key = wp.x + "," + wp.y;
            Float modifier = speedModifiers.get(key);
            if (modifier == null) {
                markKeypoint(wp);
            }
            Entity waypoint = new Entity();
            waypoint.setPosition(wp.x / 2f, wp.y / 2f);
            if (modifier != null) {
                waypoint.addComponent(new SpeedWaypointComponent(modifier));
            }
            waypointList.add(waypoint);
        }

        // Mark keyWaypoints2 points标记第二组关键路径点
        for (GridPoint2 wp : keyWaypoints2) {
            markKeypoint(wp);
            Entity waypoint = new Entity();
            waypoint.setPosition(wp.x/2, wp.y/2);
            waypointList.add(waypoint);
        }

        // Connect waypoints with path tiles连接关键点之间的路径
        connectWaypointsWithPath();
        
        // Connect keyWaypoints2 with path tiles连接第二组关键点之间的路径
        connectWaypoints2WithPath();
        
        // 重新标记关键点，确保它们不被路径瓦片覆盖
        for (GridPoint2 wp : keyWaypoints) {
            String key = wp.x + "," + wp.y;
            if (!speedModifiers.containsKey(key)) {
                markKeypoint(wp);
            }
        }
        
        // 重新标记第二组关键点，确保它们不被路径瓦片覆盖
        for (GridPoint2 wp : keyWaypoints2) {
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
                {0, 8, 12, 22},
                {9, 13, 18, 22},
                {10, 14, 7, 9},
                {9, 11, 28, 30},
                {13, 17, 31, 33},
                {28, 34, 31, 35},
                {22, 28, 14, 23},
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
        System.out.println("✅ Key path points generated, number=" + keyWaypoints.size());
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

    /** Connect waypoints with path tiles and mark them as invalid for placement连接关键点之间的路径并标记为不可放置 */
    private void connectWaypointsWithPath() {
        if (keyWaypoints.size() < 2) return;
        
        System.out.println("🛤️ Connecting waypoints with path tiles...");
        
        // 连接相邻的关键点
        for (int i = 0; i < keyWaypoints.size() - 1; i++) {
            GridPoint2 start = keyWaypoints.get(i);
            GridPoint2 end = keyWaypoints.get(i + 1);
            
            System.out.println("🔗 Connecting waypoint " + i + " (" + start.x + "," + start.y + ") to waypoint " + (i+1) + " (" + end.x + "," + end.y + ")");
            
            // 生成两点之间的直线路径
            generatePathBetweenPoints(start, end);
        }
        
        System.out.println("✅ Path connection completed. Total path tiles: " + pathTiles.size());
    }

    /** Connect keyWaypoints2 with path tiles and mark them as invalid for placement连接第二组关键点之间的路径并标记为不可放置 */
    private void connectWaypoints2WithPath() {
        if (keyWaypoints2.size() < 2) return;
        
        System.out.println("🛤️ Connecting keyWaypoints2 with path tiles...");
        
        // 连接相邻的关键点
        for (int i = 0; i < keyWaypoints2.size() - 1; i++) {
            GridPoint2 start = keyWaypoints2.get(i);
            GridPoint2 end = keyWaypoints2.get(i + 1);
            
            System.out.println("🔗 Connecting keyWaypoints2 " + i + " (" + start.x + "," + start.y + ") to keyWaypoints2 " + (i+1) + " (" + end.x + "," + end.y + ")");
            
            // 生成两点之间的直线路径
            generatePathBetweenPoints(start, end);
        }
        
        System.out.println("✅ keyWaypoints2 Path connection completed. Total path tiles: " + pathTiles.size());
    }

    /** Generate path tiles between two points生成两点之间的路径瓦片 */
    private void generatePathBetweenPoints(GridPoint2 start, GridPoint2 end) {
        // 使用Bresenham算法生成直线路径
        int x0 = start.x;
        int y0 = start.y;
        int x1 = end.x;
        int y1 = end.y;
        
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        
        int x = x0;
        int y = y0;
        
        while (true) {
            // 标记路径瓦片为不可放置
            markPathTile(new GridPoint2(x, y));
            
            if (x == x1 && y == y1) break;
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    /** Mark a tile as path tile and add to invalid tiles标记瓦片为路径瓦片并添加到不可放置区域 */
    private void markPathTile(GridPoint2 pos) {
        String key = pos.x + "," + pos.y;
        
        // 避免重复标记
        if (pathTiles.containsKey(key)) return;
        
        // 检查是否是关键点，如果是关键点则跳过路径瓦片显示
        boolean isKeypoint = false;
        for (GridPoint2 wp : keyWaypoints) {
            if (wp.x == pos.x && wp.y == pos.y) {
                isKeypoint = true;
                break;
            }
        }
        
        // 添加到路径瓦片记录
        pathTiles.put(key, pos);
        
        // 添加到不可放置区域
        invalidTiles.put(key, pos);
        
        // 在地图上显示路径瓦片（但不在关键点上显示）
        if (pathTile != null && !isKeypoint) {
            TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
            TiledMapTileLayer pathLayer = getOrCreatePathLayer(baseLayer);
            TiledMapTileLayer.Cell cell = pathLayer.getCell(pos.x, pos.y);
            if (cell == null) {
                cell = new TiledMapTileLayer.Cell();
                pathLayer.setCell(pos.x, pos.y, cell);
            }
            cell.setTile(pathTile);
        }
        
        System.out.println("🛤️ Path tile marked at (" + pos.x + "," + pos.y + ")" + (isKeypoint ? " (keypoint, no path tile)" : ""));
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
        keyWaypoints2.clear();
        snowCoords.clear();
        System.out.println("🧹 MapEditor cleaned up");
    }

    @Override
    public Map<String, GridPoint2> getInvalidTiles() {
        invalidTiles.clear();
        // 包含所有无效区域：障碍物、雪树、雪地、路径瓦片
        invalidTiles.putAll(barrierTiles);
        invalidTiles.putAll(snowTreeTiles);
        invalidTiles.putAll(pathTiles);  // 添加路径瓦片到不可放置区域
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
        if (invalidTiles.containsKey(key) || 
            barrierTiles.containsKey(key)) {
            System.out.println("🚫 Position (" + x + ", " + y + ") is occupied by barrier, skipping");
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
    @Override
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
    @Override
    public void registerSnowTreeCoords(int[][] coords) {
        if (coords == null) return;
        for (int[] p : coords) {
            if (p == null || p.length != 2) continue;
            String key = p[0] + "," + p[1];
            snowTreeTiles.put(key, new GridPoint2(p[0], p[1]));
        }
    }

    @Override
    public java.util.List<GridPoint2> getWaterTiles() {
        // If you have a waterTiles list, return it here. Otherwise, return an empty list.
        // Example:
        return new java.util.ArrayList<>(); // Replace with your actual water tiles if available
    }

    @Override
    public java.util.List<Entity> getWaypointList() {
        return waypointList;
    }

}
