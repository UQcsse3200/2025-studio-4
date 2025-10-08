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
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;


public class MapEditor2 extends InputAdapter {
    private TerrainComponent2 terrain;

    // Tree / Path / Placement Area records鏍?/ 璺緞 / 鍙斁缃尯鍩?璁板綍
    private Map<String, GridPoint2> pathTiles = new HashMap<>();
    private Map<String, GridPoint2> invalidTiles = new HashMap<>();
    private Map<String, GridPoint2> placeableAreaTiles = new HashMap<>();
    private Map<String, GridPoint2> barrierTiles = new HashMap<>();
    private Map<String, GridPoint2> snowTreeTiles = new HashMap<>();
    
    // Occupied tiles to avoid obstacle overlap宸插崰鐢ㄧ殑鏍煎瓙锛岄伩鍏嶉殰纰嶇墿閲嶅彔
    private Set<String> occupiedTiles = new HashSet<>();

    // Tile types鐡︾墖绫诲瀷
    private TiledMapTile keypointTile;
    private TiledMapTile snowTile;
    private TiledMapTile pathTile;
    // Key path points list鍏抽敭璺緞鐐瑰垪琛?
    private java.util.List<GridPoint2> keyWaypoints = new java.util.ArrayList<>();
    private java.util.List<GridPoint2> keyWaypoints2 = new java.util.ArrayList<>();
    private java.util.Map<String, Float> speedChangeWaypoints = new java.util.LinkedHashMap<>();
    private java.util.List<GridPoint2> snowCoords = new java.util.ArrayList<>();

    public java.util.List<Entity> waypointList = new java.util.ArrayList<>();

    public MapEditor2(TerrainComponent2 terrain, Entity player) {
        this.terrain = terrain;
        initializeKeypointTile();
        initializeSnowTile();
        initializePathTile();
    }


    /** Initialize keypoint tiles鍒濆鍖栧叧閿偣鐡︾墖 */
    private void initializeKeypointTile() {
        try {
            Texture keypointTexture = ServiceLocator.getResourceService().getAsset("images/path_keypoint.png", Texture.class);
            // Avoid blurring when zooming
            keypointTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            // Make the keypoint tile size consistent with the base tile layer to prevent size anomalies浣垮叧閿偣鐡︾墖灏哄涓庡熀纭€鍥惧眰鐡︾墖涓€鑷达紝闃叉灏哄寮傚父
            TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
            int tileW = baseLayer.getTileWidth();
            int tileH = baseLayer.getTileHeight();
            int regionW = Math.min(tileW, keypointTexture.getWidth());
            int regionH = Math.min(tileH, keypointTexture.getHeight());
            TextureRegion region = new TextureRegion(keypointTexture, 0, 0, regionW, regionH);
            keypointTile = new StaticTiledMapTile(region);
            System.out.println("鉁?path_keypoint.png tile initialized successfully");
        } catch (Exception e) {
            System.out.println("鈿狅笍 path_keypoint.png tile initialization failed: " + e.getMessage());
            keypointTile = null;
        }
    }

    /** Initialize snow tiles鍒濆鍖栭洩鍦扮摝鐗?*/
    private void initializeSnowTile() {
        try {
            Texture snowTexture = ServiceLocator.getResourceService().getAsset("images/snow.png", Texture.class);
            // Avoid blurring when zooming閬垮厤鏀惧ぇ鏃舵ā绯?
            snowTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            // Make the snow tile size consistent with the base tile layer to prevent size anomalies浣块洩鍦扮摝鐗囧昂瀵镐笌鍩虹鍥惧眰鐡︾墖涓€鑷达紝闃叉灏哄寮傚父
            TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
            int tileW = baseLayer.getTileWidth();
            int tileH = baseLayer.getTileHeight();
            int regionW = Math.min(tileW, snowTexture.getWidth());
            int regionH = Math.min(tileH, snowTexture.getHeight());
            TextureRegion region = new TextureRegion(snowTexture, 0, 0, regionW, regionH);
            snowTile = new StaticTiledMapTile(region);
            System.out.println("鉁?snow.png tile initialized successfully");
        } catch (Exception e) {
            System.out.println("鈿狅笍 snow.png tile initialization failed: " + e.getMessage());
            snowTile = null;
        }
    }

    /** Initialize path tiles鍒濆鍖栬矾寰勭摝鐗?*/
    private void initializePathTile() {
        try {
            Texture pathTexture = ServiceLocator.getResourceService().getAsset("images/path.png", Texture.class);
            // Avoid blurring when zooming閬垮厤鏀惧ぇ鏃舵ā绯?
            pathTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            // Make the path tile size consistent with the base tile layer to prevent size anomalies浣胯矾寰勭摝鐗囧昂瀵镐笌鍩虹鍥惧眰鐡︾墖涓€鑷达紝闃叉灏哄寮傚父
            TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
            int tileW = baseLayer.getTileWidth();
            int tileH = baseLayer.getTileHeight();
            int regionW = Math.min(tileW, pathTexture.getWidth());
            int regionH = Math.min(tileH, pathTexture.getHeight());
            TextureRegion region = new TextureRegion(pathTexture, 0, 0, regionW, regionH);
            pathTile = new StaticTiledMapTile(region);
            System.out.println("鉁?path.png tile initialized successfully");
        } catch (Exception e) {
            System.out.println("鈿狅笍 path.png tile initialization failed: " + e.getMessage());
            pathTile = null;
        }
    }


    /** Get or create layer for keypoints and other elements, always append to the end, ensuring it is above the base layer and mmap2鑾峰彇鎴栧垱寤虹敤浜庡叧閿偣鍜屽叾浠栧厓绱犵殑鍥惧眰锛屽缁堣拷鍔犲埌鏈熬锛屼繚璇佸湪鍩虹灞備笌mmap2涔嬩笂 */
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

    /** Automatically generate enemy paths鑷姩鐢熸垚鏁屼汉璺緞 */
    public void generateEnemyPath() {
        if (terrain == null) return;

        pathTiles.clear();
        keyWaypoints.clear();
        keyWaypoints2.clear();
        waypointList.clear();
        speedChangeWaypoints.clear();

        GridPoint2[] primaryKeyPoints = {
                new GridPoint2(5, 0),
                new GridPoint2(5, 10),
                new GridPoint2(10, 10),
                new GridPoint2(15, 14),
                new GridPoint2(15, 25),
                new GridPoint2(5, 25),
                new GridPoint2(5, 32)
        };
        java.util.Map<String, GridPoint2> primaryKeyLookup = new java.util.HashMap<>();
        for (GridPoint2 keyPoint : primaryKeyPoints) {
            keyWaypoints.add(keyPoint);
            primaryKeyLookup.put(keyPoint.x + "," + keyPoint.y, keyPoint);
        }

        GridPoint2[] secondaryKeyPoints = {
                new GridPoint2(28, 6),
                new GridPoint2(33, 12),
                new GridPoint2(33, 21),
                new GridPoint2(28, 27),
                new GridPoint2(18, 27),
                new GridPoint2(15, 25),
                new GridPoint2(5, 25),
                new GridPoint2(5, 32)
        };
        java.util.Map<String, GridPoint2> secondaryKeyLookup = new java.util.HashMap<>();
        for (GridPoint2 keyPoint : secondaryKeyPoints) {
            keyWaypoints2.add(keyPoint);
            secondaryKeyLookup.put(keyPoint.x + "," + keyPoint.y, keyPoint);
        }

        speedChangeWaypoints.put("5,2", 0.5f);
        speedChangeWaypoints.put("5,3", 0.5f);
        speedChangeWaypoints.put("5,4", 0.5f);
        speedChangeWaypoints.put("5,5", 0.5f);
        speedChangeWaypoints.put("7,10", 0.5f);
        speedChangeWaypoints.put("8,10", 0.5f);
        speedChangeWaypoints.put("9,10", 0.5f);
        speedChangeWaypoints.put("10,10", 0.5f);

        int[][] orderedMainWaypoints = {
                {5, 0},
                {5, 2}, {5, 3}, {5, 4}, {5, 5},
                {5, 10},
                {7, 10}, {8, 10}, {9, 10},
                {10, 10},
                {15, 14},
                {15, 25},
                {5, 25},
                {5, 32}
        };

        for (int[] coords : orderedMainWaypoints) {
            int x = coords[0];
            int y = coords[1];
            String key = x + "," + y;

            GridPoint2 keyPoint = primaryKeyLookup.get(key);
            Float modifier = speedChangeWaypoints.get(key);

            if (keyPoint == null && modifier == null) {
                continue;
            }

            if (keyPoint != null) {
                markKeypoint(keyPoint);
            }

            Entity waypoint = new Entity();
            waypoint.setPosition(x / 2f, y / 2f);
            if (modifier != null) {
                waypoint.addComponent(new SpeedWaypointComponent(modifier));
            }
            waypointList.add(waypoint);
        }

        int[][] orderedSecondaryWaypoints = {
                {28, 6},
                {33, 12},
                {33, 21},
                {28, 27},
                {18, 27},
                {15, 25},
                {5, 25},
                {5, 32}
        };

        for (int[] coords : orderedSecondaryWaypoints) {
            int x = coords[0];
            int y = coords[1];
            String key = x + "," + y;

            GridPoint2 keyPoint = secondaryKeyLookup.get(key);
            if (keyPoint == null) {
                continue;
            }

            markKeypoint(keyPoint);
            Entity waypoint = new Entity();
            waypoint.setPosition(x / 2f, y / 2f);
            waypointList.add(waypoint);
        }

        connectWaypointsWithPath();
        connectWaypoints2WithPath();
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
            
            System.out.println("馃敶InvalidTiles: x=" + startX + "-" + endX + ", y=" + startY + "-" + endY);
            
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    addSnow(x, y);
                }
            }
        }

       // generatePlaceableAreas();
        System.out.println("鉁?Key path points generated, number=" + keyWaypoints.size());
        System.out.println("鉁?Snow coordinates number=" + snowCoords.size());
    }

    /** Mark key path points鏍囪鍏抽敭璺緞鐐?*/
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

    /** Connect waypoints with path tiles and mark them as invalid for placement杩炴帴鍏抽敭鐐逛箣闂寸殑璺緞骞舵爣璁颁负涓嶅彲鏀剧疆 */
    private void connectWaypointsWithPath() {
        if (keyWaypoints.size() < 2) return;
        
        System.out.println("馃洡锔?Connecting waypoints with path tiles...");
        
        // 杩炴帴鐩搁偦鐨勫叧閿偣
        for (int i = 0; i < keyWaypoints.size() - 1; i++) {
            GridPoint2 start = keyWaypoints.get(i);
            GridPoint2 end = keyWaypoints.get(i + 1);
            
            System.out.println("馃敆 Connecting waypoint " + i + " (" + start.x + "," + start.y + ") to waypoint " + (i+1) + " (" + end.x + "," + end.y + ")");
            
            // 鐢熸垚涓ょ偣涔嬮棿鐨勭洿绾胯矾寰?
            generatePathBetweenPoints(start, end);
        }
        
        System.out.println("鉁?Path connection completed. Total path tiles: " + pathTiles.size());
    }

    /** Connect keyWaypoints2 with path tiles and mark them as invalid for placement杩炴帴绗簩缁勫叧閿偣涔嬮棿鐨勮矾寰勫苟鏍囪涓轰笉鍙斁缃?*/
    private void connectWaypoints2WithPath() {
        if (keyWaypoints2.size() < 2) return;
        
        System.out.println("馃洡锔?Connecting keyWaypoints2 with path tiles...");
        
        // 杩炴帴鐩搁偦鐨勫叧閿偣
        for (int i = 0; i < keyWaypoints2.size() - 1; i++) {
            GridPoint2 start = keyWaypoints2.get(i);
            GridPoint2 end = keyWaypoints2.get(i + 1);
            
            System.out.println("馃敆 Connecting keyWaypoints2 " + i + " (" + start.x + "," + start.y + ") to keyWaypoints2 " + (i+1) + " (" + end.x + "," + end.y + ")");
            
            // 鐢熸垚涓ょ偣涔嬮棿鐨勭洿绾胯矾寰?
            generatePathBetweenPoints(start, end);
        }
        
        System.out.println("鉁?keyWaypoints2 Path connection completed. Total path tiles: " + pathTiles.size());
    }

    /** Generate path tiles between two points鐢熸垚涓ょ偣涔嬮棿鐨勮矾寰勭摝鐗?*/
    private void generatePathBetweenPoints(GridPoint2 start, GridPoint2 end) {
        // 浣跨敤Bresenham绠楁硶鐢熸垚鐩寸嚎璺緞
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
            // 鏍囪璺緞鐡︾墖涓轰笉鍙斁缃?
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

    /** Mark a tile as path tile and add to invalid tiles鏍囪鐡︾墖涓鸿矾寰勭摝鐗囧苟娣诲姞鍒颁笉鍙斁缃尯鍩?*/
    private void markPathTile(GridPoint2 pos) {
        String key = pos.x + "," + pos.y;
        
        // 閬垮厤閲嶅鏍囪
        if (pathTiles.containsKey(key)) return;
        
        // 妫€鏌ユ槸鍚︽槸鍏抽敭鐐癸紝濡傛灉鏄叧閿偣鍒欒烦杩囪矾寰勭摝鐗囨樉绀?
        boolean isKeypoint = false;
        for (GridPoint2 wp : keyWaypoints) {
            if (wp.x == pos.x && wp.y == pos.y) {
                isKeypoint = true;
                break;
            }
        }
        
        // 娣诲姞鍒拌矾寰勭摝鐗囪褰?
        pathTiles.put(key, pos);
        
        // 娣诲姞鍒颁笉鍙斁缃尯鍩?
        invalidTiles.put(key, pos);
        
        // 鍦ㄥ湴鍥句笂鏄剧ず璺緞鐡︾墖锛堜絾涓嶅湪鍏抽敭鐐逛笂鏄剧ず锛?
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
        
        System.out.println("馃洡锔?Path tile marked at (" + pos.x + "," + pos.y + ")" + (isKeypoint ? " (keypoint, no path tile)" : ""));
    }

    /** Clean up all objects娓呯悊鎵€鏈夊璞?*/
    public void cleanup() {
        //for (Entity tree : placedTrees.values()) tree.dispose();
        //placedTrees.clear();
        pathTiles.clear();
        placeableAreaTiles.clear();
        snowTreeTiles.clear();
        occupiedTiles.clear();
        keyWaypoints.clear();
        keyWaypoints2.clear();
        speedChangeWaypoints.clear();
        waypointList.clear();
        snowCoords.clear();
        System.out.println("馃Ч MapEditor cleaned up");
    }

    public Map<String, GridPoint2> getInvalidTiles() {
        invalidTiles.clear();
        // 鍖呭惈鎵€鏈夋棤鏁堝尯鍩燂細闅滅鐗┿€侀洩鏍戙€侀洩鍦般€佽矾寰勭摝鐗?
        invalidTiles.putAll(barrierTiles);
        invalidTiles.putAll(snowTreeTiles);
        invalidTiles.putAll(pathTiles);  // 娣诲姞璺緞鐡︾墖鍒颁笉鍙斁缃尯鍩?
        snowCoords.forEach(coord -> invalidTiles.put(coord.x + "," + coord.y, coord));
        return invalidTiles;
    }
     /** Add snow at specified coordinates鍦ㄦ寚瀹氬潗鏍囨坊鍔犻洩鍦?*/
     public void addSnow(int x, int y) {
        if (snowTile == null) {
            System.out.println("鈿狅笍 Snow tile not initialized");
            return;
        }
        TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        TiledMapTileLayer pathLayer = getOrCreatePathLayer(baseLayer);
        
        if (x < 0 || y < 0 || x >= pathLayer.getWidth() || y >= pathLayer.getHeight()) {
            System.out.println("鈿狅笍 Snow coordinates out of bounds: (" + x + ", " + y + ")");
            return;
        }
        String key = x + "," + y;
        if (invalidTiles.containsKey(key) || 
            barrierTiles.containsKey(key)) {
            System.out.println("馃毇 Position (" + x + ", " + y + ") is occupied by barrier, skipping");
            return;
        }
         // Add to snow coordinates list娣诲姞鍒伴洩鍦板潗鏍囧垪琛?
         snowCoords.add(new GridPoint2(x, y));
        
         // Create snow tile鍒涘缓闆湴鐡︾墖
         TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
         cell.setTile(snowTile);
         pathLayer.setCell(x, y, cell);
         
         System.out.println("鉁?Snow added at coordinates (" + x + ", " + y + ")");
     }

    /**
     * Register the coordinates of the obstacles for getInvalidTiles() to return uniformly
     * coords: int[][]锛孍ach element is {x, y}
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
     * coords: int[][]锛孍ach element is {x, y}
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
