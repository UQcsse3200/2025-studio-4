package com.csse3200.game.areas;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.services.ServiceLocator;

import java.util.HashMap;
import java.util.Map;

/**
 * 地图编辑器类，用于在游戏运行时编辑地形
 * 支持通过Q键在玩家上方放置树木
 * 自动生成敌人行走通道（使用path.png纹理）
 * Map editor class, used to edit terrain while the game is running
 * Support placing trees above players by pressing the Q key
 * Automatically generate enemy walking paths (using the path.png texture)
 */
public class MapEditor extends InputAdapter {
    private TerrainComponent terrain;
    private boolean editorEnabled = false;
    private InputProcessor originalProcessor;
    private Entity player; // 玩家实体的引用 References to player entities
    
    // 用于追踪放置的树木 Used for tracking placed trees
    private Map<String, Entity> placedTrees = new HashMap<>();
    
    // 用于追踪创建的路径瓦片 Used for tracking created path tiles
    private Map<String, GridPoint2> pathTiles = new HashMap<>();
    
    // 路径瓦片（使用path.png纹理）path tile (using path.png texture)
    private TiledMapTile pathTile;
    
    // 可放置区域瓦片（白色空格）Placement area tiles (white Spaces)
    private TiledMapTile placeableAreaTile;
    
    // 用于追踪可放置区域的瓦片 Tiles used for tracking the placement area
    private Map<String, GridPoint2> placeableAreaTiles = new HashMap<>();
    
    // 路径附近的可放置区域范围 The range of the placement area near the path
    private int placeableRange = 2; // 路径周围2格内可以放置树木 Trees can be placed within two Spaces around the path

    
    public MapEditor(TerrainComponent terrain, Entity player) {
        this.terrain = terrain;
        this.player = player;
        initializePathTile();
        initializePlaceableAreaTile();
    }
    
    /**
     * 初始化路径瓦片（使用path.png纹理）
     * Initialize the path tile (using the path.png texture)
     */
    private void initializePathTile() {
        try {
            // 加载path.png纹理 load the path.png texture
            Texture pathTexture = ServiceLocator.getResourceService().getAsset("images/path.png", Texture.class);
            TextureRegion pathRegion = new TextureRegion(pathTexture);
            pathTile = new StaticTiledMapTile(pathRegion);
            System.out.println("✅ The path tile (path.png) has been initialized successfully");
        } catch (Exception e) {
            System.out.println("⚠️ The initialization of the path tile failed: " + e.getMessage());
            System.out.println("Blank tiles will be used as a substitute");
            pathTile = null;
        }
    }
    
    /**
     * 初始化可放置区域瓦片（创建白色空格瓦片）
     * Initialize the placement area tiles (create white space tiles)
     */
    private void initializePlaceableAreaTile() {
        try {
            // 创建一个白色像素纹理，使其比背景更明显 Create a white pixel texture to make it more prominent than the background
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(32, 32, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(0.9f, 0.9f, 0.9f, 0.8f); // 浅灰白色，较明显 Light grayish white, quite distinct
            pixmap.fill();
            
            // 添加边框使其更明显 Add a border to make it more obvious
            pixmap.setColor(1.0f, 1.0f, 1.0f, 1.0f); // 纯白色边框 Pure white border
            pixmap.drawRectangle(0, 0, 32, 32);
            
            Texture whiteTexture = new Texture(pixmap);
            pixmap.dispose();
            
            TextureRegion whiteRegion = new TextureRegion(whiteTexture);
            placeableAreaTile = new StaticTiledMapTile(whiteRegion);
            System.out.println("✅ The initialization of the placement area tiles (white Spaces) has been successful");
        } catch (Exception e) {
            System.out.println("⚠️ The initialization of the tiles in the placement area failed: " + e.getMessage());
            placeableAreaTile = null;
        }
    }
    
    /**
     * 启用地图编辑模式（现在始终启用）Enable map editing mode (always enabled now)
     */
    public void enableEditor() {
        if (!editorEnabled) {
            // 保存原有的输入处理器 Save the original input processor
            originalProcessor = Gdx.input.getInputProcessor();
            
            // 创建复合输入处理器，既能处理编辑又能保持原有功能
            //Create a composite input processor that can handle editing while maintaining the original functionality
            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(this); // 地图编辑处理器优先 The map editing processor takes priority
            if (originalProcessor != null) {
                multiplexer.addProcessor(originalProcessor); // 保持原有功能 Maintain the original function
            }
            
            Gdx.input.setInputProcessor(multiplexer);
            editorEnabled = true;
            System.out.println("🟢 The map editing mode is enabled - Press the Q key to place trees in the white blank space");
        }
    }
    @Override
    public boolean keyDown(int keycode) {
        // 按Q键在人物上方放置树木 Press the Q key to place a tree above the character
        if (keycode == Input.Keys.Q) {
            System.out.println("🌳 The Q key was detected to be pressed! Try placing towers above the character");
            placeTreeAbovePlayer();
            return true;
        }
        

        
        return false;
    }
    /**
     * 在人物上方放置树木（仅限路径附近区域）Place trees above the characters (only in the area near the path)
     */
    private void placeTreeAbovePlayer() {
        if (terrain == null || player == null) {
            System.out.println("❌ The terrain or the player is empty and trees cannot be placed");
            return;
        }
        
        // 获取玩家当前位置 Get the current position of the player
        Vector2 playerPos = player.getPosition();
        System.out.println("👤 Player Position: (" + playerPos.x + ", " + playerPos.y + ")");
        
        // 将玩家的世界坐标转换为瓦片坐标  Convert the player's world coordinates to tile coordinates
        float tileSize = terrain.getTileSize();
        int playerTileX = (int) (playerPos.x / tileSize);
        int playerTileY = (int) (playerPos.y / tileSize);
        
        // 计算玩家上方的瓦片坐标 Calculate the coordinates of the tiles above the player
        int targetTileX = playerTileX;
        int targetTileY = playerTileY + 1; // 上方一格 One square above
        
        System.out.println("🌳 Location of the target tower: (" + targetTileX + ", " + targetTileY + ")");
        
        // 检查坐标是否在有效范围内 Check whether the coordinates are within the valid range
        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        if (layer != null && targetTileX >= 0 && targetTileY >= 0 && 
            targetTileX < layer.getWidth() && targetTileY < layer.getHeight()) {
            
            // 检查该位置是否在可放置区域内 Check whether this position is within the placement area
            if (!isPlaceableArea(targetTileX, targetTileY)) {
                System.out.println("⚠️ location\n (" + targetTileX + ", " + targetTileY + ") Not within the placement area (must be near the path)");
                return;
            }
            
            // 检查该位置是否已经有树木 Check if there are already tower at this location
            String treeKey = targetTileX + "," + targetTileY;
            if (placedTrees.containsKey(treeKey)) {
                System.out.println("⚠️ location (" + targetTileX + ", " + targetTileY + ") There is already a tower");
                return;
            }
            
            // 检查该位置是否是路径瓦片 Check whether this position is a path tile
            if (pathTiles.containsKey(treeKey)) {
                System.out.println("⚠️ location (" + targetTileX + ", " + targetTileY + ") t's the path tiles and no towers can be placed there");
                return;
            }
            
            // 在该位置创建树木实体 Create a tree entity at this location
            Entity tree = ObstacleFactory.createTree();
            Vector2 treeWorldPos = terrain.tileToWorldPosition(new GridPoint2(targetTileX, targetTileY));
            tree.setPosition(treeWorldPos);
            
            // 在游戏中注册塔 Register tower in the game
            ServiceLocator.getEntityService().register(tree);
            
            // 追踪放置的塔 Track the placed towers
            placedTrees.put(treeKey, tree);
            
            System.out.println("🌳 Success lies in coordinates (" + targetTileX + ", " + targetTileY + ") Towers were placed.");
        } else {
            System.out.println("❌ target position (" + targetTileX + ", " + targetTileY + ")Beyond the map range");
        }
    }
    
    /**
     * 在指定位置创建敌人行走通道（使用path.png纹理）Create an enemy walking path at the specified location (using the path.png texture)
     */
    private void createPathTile(int tileX, int tileY) {
        // 获取地形图层 Obtain the topographic map layer
        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        if (layer == null) {
            System.out.println("❌ The topographic map layer cannot be obtained");
            return;
        }
        
        // 检查坐标是否在有效范围内 Check whether the coordinates are within the valid range
        if (tileX >= 0 && tileY >= 0 && 
            tileX < layer.getWidth() && tileY < layer.getHeight()) {
            
            String pathKey = tileX + "," + tileY;
            
            // 检查该位置是否已经是路径 Check whether this position is already a path
            if (pathTiles.containsKey(pathKey)) {
                return; // 已经是路径了 It's already a path
            }
            
            // 检查该位置是否有树木，如果有则移除 Check if there are any trees at this location. If so, remove them
            if (placedTrees.containsKey(pathKey)) {
                Entity existingTree = placedTrees.get(pathKey);
                existingTree.dispose();
                placedTrees.remove(pathKey);
            }

            // 创建路径瓦片 Create path tiles
            if (pathTile != null) {
                // 使用path.png纹理瓦片
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(pathTile);
                layer.setCell(tileX, tileY, cell);
                System.out.println("🛤️ At (" + tileX + ", " + tileY + ") Path tiles have been created(path.png)");
//            } else {
//                // 回退到空白瓦片 Retreat to the blank tile
//                layer.setCell(tileX, tileY, null);
//                System.out.println("🛤️ 在坐标 (" + tileX + ", " + tileY + ") 创建了空白路径（回退模式）");
            }

            // 追踪路径瓦片 Track the path tiles
            pathTiles.put(pathKey, new GridPoint2(tileX, tileY));
        }
    }
    
    /**
     * 自动生成敌人行走路线
     * 创建一条从地图左侧到右侧的简单路径
     * Automatically generate the enemy's walking route
     * Create a simple path from the left side to the right side of the map
     */
    public void generateEnemyPath() {
        if (terrain == null) {
            System.out.println("❌ The terrain is empty and no enemy paths can be generated");
            return;
        }
        
        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        if (layer == null) {
            System.out.println("❌ The topographic map layer cannot be obtained");
            return;
        }
        
        int mapWidth = layer.getWidth();
        int mapHeight = layer.getHeight();
        
        // 创建一条简单的S形路径，从左到右 Create a simple S-shaped path from left to right
        int pathY = mapHeight / 2; // 从地图中间开始
        
        System.out.println("🛤️ Start generating enemy paths...");
        
        // 第1段：直线向右 The straight line goes right
        for (int x = 0; x < mapWidth / 3; x++) {
            createPathTile(x, pathY);
        }
        
        // 第2段：向下弯曲 The path goes down and curves
        int midX = mapWidth / 3;
        for (int y = pathY; y > pathY - 3 && y >= 0; y--) {
            createPathTile(midX, y);
        }
        
        // 第3段：继续向右 The path continues to the right
        int newY = pathY - 3;
        for (int x = midX; x < mapWidth * 2 / 3; x++) {
            createPathTile(x, newY);
        }
        
        // 第4段：向上弯曲 The path goes up and curves
        int endX = mapWidth * 2 / 3;
        for (int y = newY; y < newY + 6 && y < mapHeight; y++) {
            createPathTile(endX, y);
        }
        
        // 第5段：最后直线到终点 The last straight line to the end
        int finalY = newY + 6;
        for (int x = endX; x < mapWidth; x++) {
            createPathTile(x, finalY);
        }
        
        System.out.println("✅ The enemy path generation is complete! Jointly created " + pathTiles.size() + " Path tiles");
        
        // 路径生成后，创建可放置区域 Create placeable areas after the path is generated
        generatePlaceableAreas();
    }
    
    /**
     * 获取敌人路径的有序路径点列表
     * 返回按照路径顺序排列的世界坐标点
     * Obtain an ordered list of path points for the enemy's path
     * Return a list of world coordinates in order of path
     */
//    public java.util.List<Vector2> getOrderedPathPoints() {
//        java.util.List<Vector2> pathPoints = new java.util.ArrayList<>();
//
//        if (terrain == null) {
//            return pathPoints;
//        }
//
//        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
//        if (layer == null) {
//            return pathPoints;
//        }
//
//        int mapWidth = layer.getWidth();
//        int mapHeight = layer.getHeight();
//        int pathY = mapHeight / 2;
//
//        // 重新生成路径点序列（与generateEnemyPath中的逻辑一致）
//
//        // 第一段：直线向右
//        for (int x = 0; x < mapWidth / 3; x++) {
//            Vector2 worldPos = terrain.tileToWorldPosition(new GridPoint2(x, pathY));
//            pathPoints.add(worldPos);
//        }
//
//        // 第二段：向下弯曲
//        int midX = mapWidth / 3;
//        for (int y = pathY; y > pathY - 3 && y >= 0; y--) {
//            Vector2 worldPos = terrain.tileToWorldPosition(new GridPoint2(midX, y));
//            pathPoints.add(worldPos);
//        }
//
//        // 第三段：继续向右
//        int newY = pathY - 3;
//        for (int x = midX + 1; x < mapWidth * 2 / 3; x++) { // +1 避免重复点
//            Vector2 worldPos = terrain.tileToWorldPosition(new GridPoint2(x, newY));
//            pathPoints.add(worldPos);
//        }
//
//        // 第四段：向上弯曲
//        int endX = mapWidth * 2 / 3;
//        for (int y = newY + 1; y < newY + 6 && y < mapHeight; y++) { // +1 避免重复点
//            Vector2 worldPos = terrain.tileToWorldPosition(new GridPoint2(endX, y));
//            pathPoints.add(worldPos);
//        }
//
//        // 第五段：最后直线到终点
//        int finalY = newY + 6;
//        for (int x = endX + 1; x < mapWidth; x++) { // +1 避免重复点
//            Vector2 worldPos = terrain.tileToWorldPosition(new GridPoint2(x, finalY));
//            pathPoints.add(worldPos);
//        }
//
//        System.out.println("🗺️ 生成了 " + pathPoints.size() + " 个有序路径点");
//        return pathPoints;
//    }
    /**
     * 清理所有创建的树木、路径和可放置区域
     * Clear all created trees, paths and placement areas
     */
    public void cleanup() {
        for (Entity tree : placedTrees.values()) {
            tree.dispose();
        }
        placedTrees.clear();

        // 清理路径瓦片（从地图中移除）Clear the path tiles (remove from the map)
        if (terrain != null && !pathTiles.isEmpty()) {
            TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
            if (layer != null) {
                for (GridPoint2 pathPos : pathTiles.values()) {
                    layer.setCell(pathPos.x, pathPos.y, null);
                }
            }
        }
        pathTiles.clear();

        // 清理可放置区域瓦片 Clean the tiles in the placement area
        clearPlaceableAreas();

        System.out.println("🧹 All towers, paths and placement areas created by the map editor have been cleared");
    }
    
    public boolean isEditorEnabled() {
        return editorEnabled;
    }
    
    /**
     * 获取所有路径瓦片的位置列表（供敌人AI寻路使用）
     * Obtain the position list of all path tiles (for the enemy AI to use for pathfinding)
     */
//    public Map<String, GridPoint2> getPathTiles() {
//        return new HashMap<>(pathTiles);
//    }
    
    /**
     * 检查指定位置是否为路径瓦片
     * Check whether the specified position is a path tile
     */
//    public boolean isPathTile(int tileX, int tileY) {
//        String key = tileX + "," + tileY;
//        return pathTiles.containsKey(key);
//    }
    
    /**
     * 生成路径附近的可放置区域（用白色空格表示）
     * The placement area near the generated path (indicated by white Spaces)
     */
    private void generatePlaceableAreas() {
        if (terrain == null || pathTiles.isEmpty()) {
            System.out.println("⚠️ When the terrain is empty or there are no path tiles, no placement area can be generated");
            return;
        }
        
        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        if (layer == null) {
            System.out.println("❌ The topographic map layer cannot be obtained");
            return;
        }
        
        System.out.println("🟦 Start generating the placement area...");
        
        // 遍历所有路径瓦片，在其周围生成可放置区域 Traverse all path tiles and generate placement areas around them
        for (GridPoint2 pathPos : pathTiles.values()) {
            // 在路径瓦片周围的范围内创建可放置区域 Create a placement area within the range around the path tiles
            for (int dx = -placeableRange; dx <= placeableRange; dx++) {
                for (int dy = -placeableRange; dy <= placeableRange; dy++) {
                    int areaX = pathPos.x + dx;
                    int areaY = pathPos.y + dy;
                    
                    // 检查坐标是否在地图范围内 Check whether the coordinates are within the range of the map
                    if (areaX >= 0 && areaY >= 0 && 
                        areaX < layer.getWidth() && areaY < layer.getHeight()) {
                        
                        String areaKey = areaX + "," + areaY;
                        
                        // 跳过已经是路径的位置 Skip the position that is already a path
                        if (pathTiles.containsKey(areaKey)) {
                            continue;
                        }
                        
                        // 跳过已经是可放置区域的位置 Skip the positions that are already playable areas
                        if (placeableAreaTiles.containsKey(areaKey)) {
                            continue;
                        }
                        
                        // 创建可放置区域瓦片 Create placement area tiles
                        createPlaceableAreaTile(areaX, areaY);
                    }
                }
            }
        }
        
        System.out.println("✅ The placement area has been generated successfully! Jointly created " + placeableAreaTiles.size() + " removable area tiles");
    }
    
    /**
     * 在指定位置创建可放置区域瓦片（白色空格）
     * Create placement area tiles (white Spaces) at the designated location
     */
    private void createPlaceableAreaTile(int tileX, int tileY) {
        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        if (layer == null) {
            return;
        }
        
        String areaKey = tileX + "," + tileY;
        
        // 创建白色空格瓦片 Create white space tiles
        if (placeableAreaTile != null) {
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(placeableAreaTile);
            layer.setCell(tileX, tileY, cell);
        }
        
        // 追踪可放置区域瓦片 Track the tiles in the placement area
        placeableAreaTiles.put(areaKey, new GridPoint2(tileX, tileY));
    }
    
    /**
     * 检查指定位置是否在可放置区域内
     * Check whether the specified position is within the placement area
     */
    private boolean isPlaceableArea(int tileX, int tileY) {
        String key = tileX + "," + tileY;
        return placeableAreaTiles.containsKey(key);
    }
    
    /**
     * 获取可放置区域范围
     * Obtain the range of the placement area
     */
    public int getPlaceableRange() {
        return placeableRange;
    }
    
    /**
     * 设置可放置区域范围
     * Set the range of the placement area
     */
    public void setPlaceableRange(int range) {
        this.placeableRange = range;
        // 重新生成可放置区域 Regenerate the placement area
        clearPlaceableAreas();
        generatePlaceableAreas();
    }
    
    /**
     * 清理所有可放置区域瓦片
     * 释放内存
     * Clear all the tiles in the placement area
     * release memory
     */
    private void clearPlaceableAreas() {
        if (terrain != null && !placeableAreaTiles.isEmpty()) {
            TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
            if (layer != null) {
                for (GridPoint2 areaPos : placeableAreaTiles.values()) {
                    // 只清理可放置区域瓦片，不影响其他瓦片 Only clean the tiles in the placement area and do not affect the other tiles
                    TiledMapTileLayer.Cell cell = layer.getCell(areaPos.x, areaPos.y);
                    if (cell != null && cell.getTile() == placeableAreaTile) {
                        layer.setCell(areaPos.x, areaPos.y, null);
                    }
                }
            }
        }
        placeableAreaTiles.clear();
    }
}
