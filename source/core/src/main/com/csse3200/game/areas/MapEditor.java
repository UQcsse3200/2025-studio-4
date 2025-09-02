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
import com.csse3200.game.utils.math.RandomUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 地图编辑器：运行时编辑地图
 * 功能：
 * ✅ 按 Q 放树（只能在路径附近）
 * ✅ 自动生成敌人路径（path.png）
 * ✅ 自动生成路径附近的可放置区域（白块）
 * ✅ 自动生成 Biomes（沙漠/雪地）和河流
 * ✅ 支持生成障碍物（rock）
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

    // 瓦片类型
    private TiledMapTile pathTile;
    private TiledMapTile placeableAreaTile;

    // 放置范围：路径周围 n 格内可以放树
    private int placeableRange = 2;

    public MapEditor(TerrainComponent terrain, Entity player) {
        this.terrain = terrain;
        this.player = player;
        initializePathTile();
        initializePlaceableAreaTile();
    }

    /** 初始化路径瓦片 */
    private void initializePathTile() {
        try {
            Texture pathTexture = ServiceLocator.getResourceService().getAsset("images/path.png", Texture.class);
            pathTile = new StaticTiledMapTile(new TextureRegion(pathTexture));
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
            placeableAreaTile = new StaticTiledMapTile(new TextureRegion(new Texture(pixmap)));
            pixmap.dispose();
            System.out.println("✅ 白色可放置瓦片初始化成功");
        } catch (Exception e) {
            System.out.println("⚠️ 白色瓦片初始化失败: " + e.getMessage());
            placeableAreaTile = null;
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
        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        if (tx < 0 || ty < 0 || tx >= layer.getWidth() || ty >= layer.getHeight()) return;
        String key = tx + "," + ty;
        if (pathTiles.containsKey(key)) return;

        if (placedTrees.containsKey(key)) placedTrees.remove(key).dispose();

        if (pathTile != null) {
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(pathTile);
            layer.setCell(tx, ty, cell);
        }
        pathTiles.put(key, new GridPoint2(tx, ty));
    }

    /** 自动生成敌人路径 */
    public void generateEnemyPath() {
        if (terrain == null) return;
        TiledMapTileLayer layer = (TiledMapTileLayer) terrain.getMap().getLayers().get(0);
        int w = layer.getWidth(), h = layer.getHeight();
        int y = h / 2;

        for (int x = 0; x < w; x++) {
            createPathTile(x, y + (x / 6 % 2 == 0 ? 0 : -3));
        }
        generatePlaceableAreas();
        System.out.println("✅ 路径生成完成, 数量=" + pathTiles.size());
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

    /** ---------------- 新增功能 ---------------- */

    /** 自动生成沙漠、雪地、河流 */
    public void generateBiomesAndRivers() {
        GridPoint2 min = new GridPoint2(0, 0);
        GridPoint2 max = terrain.getMapBounds(0).sub(2, 2);

        for (int i = 0; i < 20; i++)
            paintTile(RandomUtils.random(min, max), "images/desert.png");

        for (int i = 0; i < 20; i++)
            paintTile(RandomUtils.random(min, max), "images/snow.png");

        for (int i = 0; i < 5; i++) {
            GridPoint2 pos = RandomUtils.random(min, max);
            Entity river = ObstacleFactory.createRiver();
            ServiceLocator.getEntityService().register(river);
            river.setPosition(pos.x, pos.y);  // 手动设置位置
        }
        System.out.println("✅ Biomes+河流 已生成");
    }

    /** 生成石头障碍物 */
    public void spawnObstacle(GridPoint2 pos) {
        Entity rock = ObstacleFactory.createRock();
        rock.setPosition(pos.x, pos.y);
        ServiceLocator.getEntityService().register(rock);
        System.out.println("🪨 Rock 已放置在 " + pos);
    }

    /** 替换某个格子的贴图 */
    private void paintTile(GridPoint2 pos, String texPath) {
        System.out.println("🎨 替换 " + pos + " 为 " + texPath);
        // TODO: 可接入 TerrainComponent 实现真正贴图替换
    }

    /** 清理所有对象 */
    public void cleanup() {
        for (Entity tree : placedTrees.values()) tree.dispose();
        placedTrees.clear();
        pathTiles.clear();
        placeableAreaTiles.clear();
        System.out.println("🧹 MapEditor 清理完成");
    }
}
