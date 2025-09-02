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
import com.badlogic.gdx.math.MathUtils;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.RandomUtils;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 地图编辑器：运行时编辑地图
 * 功能：
 * ✅ 按 Q 放树（只能在路径附近）
 * ✅ 自动生成敌人路径（path.png）
 * ✅ 自动生成路径附近的可放置区域（白块）
 * ✅ 自动生成 Biomes（沙漠/雪地）和河流
 * ✅ 支持生成水晶（crystal）
 * ✅ 路径关键点标记（keypoint）
 */
public class MapEditor extends InputAdapter {
    private TerrainComponent terrain;
    private boolean editorEnabled = false;
    private InputProcessor originalProcessor;
    private Entity player;

    // 树 / 路径 / 可放置区域 记录
    private Map<String, Entity> placedTrees = new HashMap<>();
    private Map<String, GridPoint2> pathTiles = new HashMap<>();
    private Map<String, GridPoint2> placeableAreaTiles = new HashMap<>();
    // 已占用的格子，避免障碍物重叠
    private Set<String> occupiedTiles = new HashSet<>();

    // 瓦片类型
    private TiledMapTile pathTile;
    private TiledMapTile placeableAreaTile;
    private TiledMapTile keypointTile;

    // 放置范围：路径周围 n 格内可以放树
    private int placeableRange = 2;

    // 关键路径点列表
    private java.util.List<GridPoint2> keyWaypoints = new java.util.ArrayList<>();

    public MapEditor(TerrainComponent terrain, Entity player) {
        this.terrain = terrain;
        this.player = player;
        initializePathTile();
        initializePlaceableAreaTile();
        initializeKeypointTile();
    }

    /** 初始化路径瓦片 */
    private void initializePathTile() {
        try {
            Texture pathTexture = ServiceLocator.getResourceService().getAsset("images/path.png", Texture.class);
            // 避免放大时模糊
            pathTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            // 使路径瓦片尺寸与基础图层瓦片一致，防止尺寸异常
            TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
            int tileW = baseLayer.getTileWidth();
            int tileH = baseLayer.getTileHeight();
            int regionW = Math.min(tileW, pathTexture.getWidth());
            int regionH = Math.min(tileH, pathTexture.getHeight());
            TextureRegion region = new TextureRegion(pathTexture, 0, 0, regionW, regionH);
            pathTile = new StaticTiledMapTile(region);
            System.out.println("✅ path.png 瓦片初始化成功");
        } catch (Exception e) {
            System.out.println("⚠️ path.png 初始化失败: " + e.getMessage());
            pathTile = null;
        }
    }

    /** 初始化白色可放置瓦片 */
    private void initializePlaceableAreaTile() {
        try {
            com.badlogic.gdx.graphics.Pixmap pixmap =
                    new com.badlogic.gdx.graphics.Pixmap(32, 32, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(0.9f, 0.9f, 0.9f, 0.8f);
            pixmap.fill();
            pixmap.setColor(1f, 1f, 1f, 1f);
            pixmap.drawRectangle(0, 0, 32, 32);
            Texture areaTexture = new Texture(pixmap);
            areaTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            placeableAreaTile = new StaticTiledMapTile(new TextureRegion(areaTexture));
            pixmap.dispose();
            System.out.println("✅ 白色可放置瓦片初始化成功");
        } catch (Exception e) {
            System.out.println("⚠️ 白色瓦片初始化失败: " + e.getMessage());
            placeableAreaTile = null;
        }
    }

    /** 初始化关键点瓦片 */
    private void initializeKeypointTile() {
        try {
            Texture keypointTexture = ServiceLocator.getResourceService().getAsset("images/path_keypoint.png", Texture.class);
            // 避免放大时模糊
            keypointTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            // 使关键点瓦片尺寸与基础图层瓦片一致，防止尺寸异常
            TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
            int tileW = baseLayer.getTileWidth();
            int tileH = baseLayer.getTileHeight();
            int regionW = Math.min(tileW, keypointTexture.getWidth());
            int regionH = Math.min(tileH, keypointTexture.getHeight());
            TextureRegion region = new TextureRegion(keypointTexture, 0, 0, regionW, regionH);
            keypointTile = new StaticTiledMapTile(region);
            System.out.println("✅ path_keypoint.png 初始化成功");
        } catch (Exception e) {
            System.out.println("⚠️ path_keypoint.png 初始化失败: " + e.getMessage());
            keypointTile = null;
        }
    }

    /** 启用编辑器 */
    public void enableEditor() {
        if (!editorEnabled) {
            originalProcessor = Gdx.input.getInputProcessor();
            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(this);
            if (originalProcessor != null) multiplexer.addProcessor(originalProcessor);
            Gdx.input.setInputProcessor(multiplexer);
            editorEnabled = true;
            System.out.println("🟢 编辑器已启用 (Q 放树)");
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.Q) {
            placeTreeAbovePlayer();
            return true;
        }
        return false;
    }

    /** 在玩家上方放树（只能在可放置区域） */
    private void placeTreeAbovePlayer() {
        if (terrain == null || player == null) return;
        Vector2 pos = player.getPosition();
        float tileSize = terrain.getTileSize();
        int tx = (int)(pos.x / tileSize);
        int ty = (int)(pos.y / tileSize) + 1;

        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        if (tx < 0 || ty < 0 || tx >= layer.getWidth() || ty >= layer.getHeight()) return;

        String key = tx + "," + ty;
        if (!isPlaceableArea(tx, ty) || pathTiles.containsKey(key) || placedTrees.containsKey(key)) return;

        Entity tree = ObstacleFactory.createTree();
        tree.setPosition(terrain.tileToWorldPosition(new GridPoint2(tx, ty)));
        ServiceLocator.getEntityService().register(tree);
        placedTrees.put(key, tree);
        System.out.println("🌲 树已放置在 " + key);
    }

    /** 创建路径瓦片 */
    private void createPathTile(int tx, int ty) {
        TiledMapTileLayer baseLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        TiledMapTileLayer pathLayer = getOrCreatePathLayer(baseLayer);
        if (tx < 0 || ty < 0 || tx >= pathLayer.getWidth() || ty >= pathLayer.getHeight()) return;
        String key = tx + "," + ty;
        if (pathTiles.containsKey(key)) return;

        if (placedTrees.containsKey(key)) placedTrees.remove(key).dispose();

        if (pathTile != null) {
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(pathTile);
            pathLayer.setCell(tx, ty, cell);
        }
        pathTiles.put(key, new GridPoint2(tx, ty));
    }

    /** 获取或创建用于路径的图层，始终追加到末尾，保证在基础层与mmap之上 */
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

    /** 自动生成敌人路径 */
    public void generateEnemyPath() {
        if (terrain == null) return;

        // 清空现有路径
        pathTiles.clear();
        keyWaypoints.clear();

        // 预定义固定路径坐标 (x, y)
        int[][] fixedPath = {
                // 起点从左边开始
                {0, 10}, {1, 10}, {2, 10}, {3, 10}, {4, 10},

                // 第一个转弯向上
                {5, 10}, {5, 9}, {5, 8}, {5, 7}, {5, 6},

                // 向右走第一段
                {6, 6}, {7, 6}, {8, 6}, {9, 6}, {10, 6}, {11, 6},

                // 向下转弯
                {12, 6}, {12, 7}, {12, 8}, {12, 9}, {12, 10}, {12, 11}, {12, 12},

                // 向右继续走更长距离
                {13, 12}, {14, 12}, {15, 12}, {16, 12}, {17, 12}, {18, 12},
                {19, 12}, {20, 12}, {21, 12}, {22, 12}, {23, 12}, {24, 12},

                // 向上转弯
                {25, 12}, {25, 11}, {25, 10}, {25, 9}, {25, 8}, {25, 7}, {25, 6},

                // 最后向右走4个坐标
                {26, 6}, {27, 6}, {28, 6}, {29, 6}
        };

        // 根据预定义路径创建路径瓦片
        for (int i = 0; i < fixedPath.length; i++) {
            int x = fixedPath[i][0];
            int y = fixedPath[i][1];
            createPathTile(x, y);
        }

        // 定义关键路径点
        keyWaypoints.add(new GridPoint2(0, 10));    // 起点
        keyWaypoints.add(new GridPoint2(5, 10));    // 第一个转角
        keyWaypoints.add(new GridPoint2(5, 6));     // 向上转角完成
        keyWaypoints.add(new GridPoint2(12, 6));    // 向右走完成
        keyWaypoints.add(new GridPoint2(12, 12));   // 向下转角完成
        keyWaypoints.add(new GridPoint2(25, 12));   // 长距离向右完成
        keyWaypoints.add(new GridPoint2(25, 6));    // 向上转角完成
        keyWaypoints.add(new GridPoint2(29, 6));    // 终点

        // 标记关键点
        for (GridPoint2 wp : keyWaypoints) {
            markKeypoint(wp);
        }

        generatePlaceableAreas();
        System.out.println("✅ 固定路径生成完成, 数量=" + pathTiles.size());
        System.out.println("✅ 关键点数量=" + keyWaypoints.size());
    }

    /** 标记关键点 */
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

    /** 生成路径周围的可放置区域 */
    private void generatePlaceableAreas() {
        if (terrain == null || pathTiles.isEmpty()) return;
        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        for (GridPoint2 p : pathTiles.values()) {
            for (int dx = -placeableRange; dx <= placeableRange; dx++) {
                for (int dy = -placeableRange; dy <= placeableRange; dy++) {
                    int ax = p.x + dx, ay = p.y + dy;
                    if (ax < 0 || ay < 0 || ax >= layer.getWidth() || ay >= layer.getHeight()) continue;
                    String k = ax + "," + ay;
                    if (pathTiles.containsKey(k) || placeableAreaTiles.containsKey(k)) continue;

                    if (placeableAreaTile != null) {
                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        cell.setTile(placeableAreaTile);
                        layer.setCell(ax, ay, cell);
                    }
                    placeableAreaTiles.put(k, new GridPoint2(ax, ay));
                }
            }
        }
    }

    private boolean isPlaceableArea(int tx, int ty) {
        return placeableAreaTiles.containsKey(tx + "," + ty);
    }

    /** 生成生态群落和河流 */
    public void generateBiomesAndRivers() {
        if (terrain == null) return;
        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);

        // 沙漠
        for (int i = 0; i < 3; i++) {
            GridPoint2 center = RandomUtils.random(new GridPoint2(0,0), terrain.getMapBounds(0).sub(6,6));
            paintBiomeBlock(layer, center, 5, "images/desert.png");
        }

        // 雪地
        for (int i = 0; i < 2; i++) {
            GridPoint2 center = RandomUtils.random(new GridPoint2(0,0), terrain.getMapBounds(0).sub(8,8));
            paintBiomeBlock(layer, center, 7, "images/snow.png");
        }
    }

    /** 绘制生态群落区块 */
    private void paintBiomeBlock(TiledMapTileLayer layer, GridPoint2 center, int size, String texPath) {
        Texture tex = ServiceLocator.getResourceService().getAsset(texPath, Texture.class);
        TiledMapTile tile = new StaticTiledMapTile(new TextureRegion(tex));
        int half = size / 2;
        for (int dx = -half; dx <= half; dx++) {
            for (int dy = -half; dy <= half; dy++) {
                GridPoint2 pos = new GridPoint2(center.x + dx, center.y + dy);
                if (canPaintTile(pos)) {
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(tile);
                    layer.setCell(pos.x, pos.y, cell);
                }
            }
        }
    }

    /** 不允许覆盖路径或塔防区 */
    private boolean canPaintTile(GridPoint2 pos) {
        String key = pos.x + "," + pos.y;
        if (pathTiles.containsKey(key) || placeableAreaTiles.containsKey(key)) {
            return false;
        }
        return true;
    }

    /** 在指定格子生成水晶（防止重叠） */
    public void spawnCrystal(GridPoint2 pos) {
        String key = pos.x + "," + pos.y;
        if (occupiedTiles.contains(key)) {
            return;
        }
        Entity crystal = ObstacleFactory.createCrystal();
        crystal.setPosition(terrain.tileToWorldPosition(pos));
        ServiceLocator.getEntityService().register(crystal);
        occupiedTiles.add(key);
        System.out.println("💎 Crystal 已放置在 " + pos);
    }

    /** 在路径终点生成水晶 */
    public void spawnCrystal() {
        spawnCrystal(new GridPoint2(29, 6));
    }

    /** 清理所有对象 */
    public void cleanup() {
        for (Entity tree : placedTrees.values()) tree.dispose();
        placedTrees.clear();
        pathTiles.clear();
        placeableAreaTiles.clear();
        occupiedTiles.clear();
        keyWaypoints.clear();
        System.out.println("🧹 MapEditor 清理完成");
    }
}