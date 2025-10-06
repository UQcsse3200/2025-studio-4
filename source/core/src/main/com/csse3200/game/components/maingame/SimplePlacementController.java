package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.MapEditor;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.TowerComponent;
import com.csse3200.game.components.TowerCostComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.TowerFactory;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.entities.factories.SummonFactory;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing tower placement within the game world.
 * Handles ghost preview, snapping, currency checks, path restrictions, and tower placement.
 */
public class SimplePlacementController extends Component {
    private boolean placementActive = false;
    private boolean needRelease = false;
    private String pendingType = "bone";
    private OrthographicCamera camera;
    private final float minSpacing = 1.0f;
    private Entity ghostTower = null;
    private MapEditor mapEditor;
    private static int[][] FIXED_PATH = {};

    private CurrencyType selectedCurrencyType = CurrencyType.METAL_SCRAP; // Default

    /**
     * Sets the map editor and refreshes invalid tiles.
     *
     * @param mapEditor the map editor to set
     */
    public void setMapEditor(MapEditor mapEditor) {
        this.mapEditor = mapEditor;
        refreshInvalidTiles();
    }

    /**
     * Refreshes the list of invalid tiles for placement.
     */
    public void refreshInvalidTiles() {
        if (mapEditor == null) return;
        List<GridPoint2> tiles = new ArrayList<>(mapEditor.getInvalidTiles().values());
        if (tiles.isEmpty()) return;

        int[][] newPath = new int[tiles.size()][2];
        for (int i = 0; i < tiles.size(); i++) {
            GridPoint2 t = tiles.get(i);
            newPath[i][0] = t.x;
            newPath[i][1] = t.y;
        }
        FIXED_PATH = newPath;
    }

    /**
     * Gets the fixed path as a 2D array of tile coordinates.
     *
     * @return the fixed path
     */
    public int[][] getFixedPath() {
        return FIXED_PATH;
    }

    /**
     * Registers placement event listeners.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("startPlacementBone", this::armBone);
        entity.getEvents().addListener("startPlacementDino", this::armDino);
        entity.getEvents().addListener("startPlacementCavemen", this::armCavemen);
        System.out.println(">>> SimplePlacementController ready; minSpacing=" + minSpacing);
    }

    /**
     * Arms the controller for bone tower placement.
     */
    private void armBone() {
        startPlacement("bone");
    }

    /**
     * Arms the controller for dino tower placement.
     */
    private void armDino() {
        startPlacement("dino");
    }

    /**
     * Arms the controller for cavemen tower placement.
     */
    private void armCavemen() {
        startPlacement("cavemen");
    }

    /**
     * Starts placement mode for the specified tower type.
     *
     * @param type the tower type to place
     */
    private void startPlacement(String type) {
        pendingType = type;
        placementActive = true;
        needRelease = true;
        selectedCurrencyType = CurrencyType.METAL_SCRAP; // default or UI selection

        if ("dino".equalsIgnoreCase(type)) {
            ghostTower = TowerFactory.createDinoTower(selectedCurrencyType);
        } else if ("cavemen".equalsIgnoreCase(type)) {
            ghostTower = TowerFactory.createCavemenTower(selectedCurrencyType);
        } else {
            ghostTower = TowerFactory.createBoneTower(selectedCurrencyType);
        }

        TowerComponent tc = ghostTower.getComponent(TowerComponent.class);
        if (tc != null) {
            tc.setActive(false);
        }

        ServiceLocator.getEntityService().register(ghostTower);
        System.out.println(">>> placement ON (" + type + ")");
    }

    /**
     * Updates the placement controller, handling ghost tower and placement logic.
     */
    @Override
    public void update() {
        if (camera == null) findWorldCamera();
        if (!placementActive || camera == null) return;

        // 防止长按触发
        if (needRelease) {
            if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) needRelease = false;
            return;
        }

        // 屏幕 -> 世界
        Vector3 mousePos3D = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mousePos3D);
        Vector2 mouseWorld = new Vector2(mousePos3D.x, mousePos3D.y);

        TerrainComponent terrain = findTerrain();
        if (terrain == null) return;

        // =========================
        // ===== 召唤物分支（ghostSummon）=====
        if (mode == Mode.SUMMON) {
            updateSummonPlacement(terrain, mouseWorld);
            return; // 不走塔逻辑
        }

        // =========================
        // 塔模式
        // =========================
        if (ghostTower == null) return;

        int towerWidth = 2;
        int towerHeight = 2;
        GridPoint2 tile = new GridPoint2(
                (int) (mouseWorld.x / terrain.getTileSize()),
                (int) (mouseWorld.y / terrain.getTileSize())
        );

        GridPoint2 mapBounds = terrain.getMapBounds(0);
        boolean inBounds = !(tile.x < 0 || tile.y < 0
                || tile.x + towerWidth > mapBounds.x
                || tile.y + towerHeight > mapBounds.y);

        Vector2 snapPos = inBounds ? terrain.tileToWorldPosition(tile.x, tile.y) : mouseWorld;

        // 移动塔幽灵
        ghostTower.setPosition(snapPos);

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!inBounds
                    || isTowerOnPath(tile, towerWidth, towerHeight)
                    || !isPositionFree(snapPos, towerWidth, towerHeight, terrain)) {
                return;
            }

            // 扣费 + 落地
            Entity player = findPlayerEntity();
            if (player == null) return;
            CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);

            Entity newTower;
            if ("dino".equalsIgnoreCase(pendingType)) {
                newTower = TowerFactory.createDinoTower(selectedCurrencyType);
            } else if ("cavemen".equalsIgnoreCase(pendingType)) {
                newTower = TowerFactory.createCavemenTower(selectedCurrencyType);
            } else {
                newTower = TowerFactory.createBoneTower(selectedCurrencyType);
            }

            TowerCostComponent costComponent = newTower.getComponent(TowerCostComponent.class);
            int cost = (costComponent != null) ? costComponent.getCostForCurrency(selectedCurrencyType) : 0;

            if (currencyManager == null
                    || !currencyManager.canAffordAndSpendCurrency(Map.of(selectedCurrencyType, cost))) {
                return;
            }

            if (ghostTower != null) {
                ghostTower.dispose();
                ghostTower = null;
            }

            newTower.setPosition(snapPos);
            TowerComponent tc = newTower.getComponent(TowerComponent.class);
            if (tc != null && tc.hasHead()) {
                tc.getHeadEntity().setPosition(snapPos.x, snapPos.y - 0.01f);
            }

            ServiceLocator.getEntityService().register(newTower);
            if (tc != null && tc.hasHead()) {
                ServiceLocator.getEntityService().register(tc.getHeadEntity());
            }

            placementActive = false;
        }
    }


    /**
     * Checks if the tower would overlap the fixed path.
     *
     * @param tile        the base tile
     * @param towerWidth  tower width in tiles
     * @param towerHeight tower height in tiles
     * @return true if tower would overlap path, false otherwise
     */
    private boolean isTowerOnPath(GridPoint2 tile, int towerWidth, int towerHeight) {
        for (int tx = 0; tx < towerWidth; tx++) {
            for (int ty = 0; ty < towerHeight; ty++) {
                if (isOnPath(new GridPoint2(tile.x + tx, tile.y + ty))) return true;
            }
        }
        return false;
    }

    /**
     * Checks if a tile is on the fixed path.
     *
     * @param tile the tile to check
     * @return true if on path, false otherwise
     */
    private boolean isOnPath(GridPoint2 tile) {
        for (int[] p : FIXED_PATH) {
            if (p[0] == tile.x && p[1] == tile.y) return true;
        }
        return false;
    }

    /**
     * Checks if the given position is free for tower placement.
     *
     * @param candidate   the world position
     * @param towerWidth  tower width in tiles
     * @param towerHeight tower height in tiles
     * @param terrain     the terrain component
     * @return true if position is free, false otherwise
     */
    private boolean isPositionFree(Vector2 candidate, int towerWidth, int towerHeight, TerrainComponent terrain) {
        Array<Entity> all = safeEntities();
        if (all == null || candidate == null) return true;
        float tileSize = terrain.getTileSize();

        for (int tx = 0; tx < towerWidth; tx++) {
            for (int ty = 0; ty < towerHeight; ty++) {
                Vector2 tilePos = new Vector2(candidate.x + tx * tileSize, candidate.y + ty * tileSize);
                for (Entity e : all) {
                    if (e == null || e == ghostTower) continue;
                    TowerComponent tower = e.getComponent(TowerComponent.class);
                    if (tower == null) continue;
                    Vector2 pos = e.getPosition();
                    if (pos == null) continue;

                    if (tilePos.x < pos.x + tower.getWidth() * tileSize &&
                            tilePos.x + tileSize > pos.x &&
                            tilePos.y < pos.y + tower.getHeight() * tileSize &&
                            tilePos.y + tileSize > pos.y) return false;
                }
            }
        }
        return true;
    }

    /**
     * Finds the terrain component from entities.
     *
     * @return the terrain component, or null if not found
     */
    private TerrainComponent findTerrain() {
        Array<Entity> all = safeEntities();
        if (all == null) return null;
        for (Entity e : all) {
            if (e == null) continue;
            TerrainComponent t = e.getComponent(TerrainComponent.class);
            if (t != null) return t;
        }
        return null;
    }

    /**
     * Gets a safe copy of all entities.
     *
     * @return array of entities, or null if unavailable
     */
    private Array<Entity> safeEntities() {
        try {
            return ServiceLocator.getEntityService().getEntitiesCopy();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Finds the world camera from entities.
     */
    private void findWorldCamera() {
        Array<Entity> all = safeEntities();
        if (all == null) return;
        for (Entity e : all) {
            if (e == null) continue;
            CameraComponent cc = e.getComponent(CameraComponent.class);
            if (cc != null && cc.getCamera() instanceof OrthographicCamera) {
                camera = (OrthographicCamera) cc.getCamera();
                return;
            }
        }
    }

    /**
     * Finds the player entity (with a currency manager).
     *
     * @return the player entity, or null if not found
     */
    private Entity findPlayerEntity() {
        Array<Entity> entities = safeEntities();
        if (entities == null) return null;
        for (Entity e : entities) {
            if (e != null && e.getComponent(CurrencyManagerComponent.class) != null) return e;
        }
        return null;
    }

    private void safeDispose(Entity e) {
        try {
            e.dispose();  // 你们的 Entity.dispose() 通常会从 EntityService 注销
        } catch (Exception ignore) {}
    }

    /**
     * Cancels the current placement and disposes of the ghost tower.
     */
    public void cancelPlacement() {
        if (ghostTower != null) {
            ghostTower.dispose();
            ghostTower = null;
        }

        if (ghostSummon != null) {
            safeDispose(ghostSummon);
            ghostSummon = null;
        }

        placementActive = false;
        needRelease = false;
        mode = Mode.NONE;           // ★ 重要：复位模式
        pendingType = "bone";
        System.out.println(">>> placement OFF");
    }

    /**
     * Checks if placement mode is currently active.
     *
     * @return true if placement is active, false otherwise
     */
    public boolean isPlacementActive() {
        return placementActive;
    }

    /**
     * Gets the pending tower type for placement.
     *
     * @return the pending tower type
     */
    public String getPendingType() {
        return pendingType;
    }

    /**
     * Sets the selected currency type for tower placement.
     *
     * @param currencyType the currency type to set
     */
    public void setSelectedCurrencyType(CurrencyType currencyType) {
        this.selectedCurrencyType = currencyType;
    }


    // === 放在 SimplePlacementController 类里 ===

    // 放置状态
    private String pendingSummonTexture = null;    // 召唤物贴图

    private enum Mode {NONE, TOWER, SUMMON}

    private Mode mode = Mode.NONE;

    private Entity ghostSummon = null;       // 召唤物幽灵

    // 幽灵实体
    private Entity ghost = null;

    // 贴图传递的规格（保持可扩展，只要 texture）
    public static class SummonSpec {
        public final String texture;
        public final String type; // "melee" / "turret"

        public SummonSpec(String texture, String type) {
            this.texture = texture;
            this.type = type;
        }
    }


    public void armSummon(SummonSpec spec) {
        cancelPlacement();

        this.mode = Mode.SUMMON;
        this.placementActive = true;
        this.needRelease = true;
        this.pendingType = spec.type; // 保存类型
        this.pendingSummonTexture = (spec.texture != null && !spec.texture.isEmpty())
                ? spec.texture : "images/engineer/Sentry.png";

        if ("turret".equals(spec.type)) {
            // 炮台幽灵（这里可以直接用路障的幽灵代替，也可以做单独的 createTurretGhost）
            this.ghostSummon = SummonFactory.createMeleeSummonGhost(this.pendingSummonTexture, 1f);
        } else {
            // 默认路障幽灵
            this.ghostSummon = SummonFactory.createMeleeSummonGhost(this.pendingSummonTexture, 1f);
        }

        ServiceLocator.getEntityService().register(this.ghostSummon);
        this.ghostSummon.create();

        System.out.println(">>> placement ON (summon: " + spec.type + ")");
    }

    /**
     * 该瓦片上是否已有炮台（按 TurretAttackComponent 判断）
     */
    private boolean hasTurretOnTile(GridPoint2 tile, TerrainComponent terrain) {
        Array<Entity> all = safeEntities();
        if (all == null) return false;
        float tileSize = terrain.getTileSize();

        for (Entity e : all) {
            if (e == null) continue;
            if (e.getComponent(com.csse3200.game.components.hero.engineer.TurretAttackComponent.class) == null)
                continue;

            Vector2 p = e.getPosition();
            if (p == null) continue;

            GridPoint2 etile = new GridPoint2(
                    (int) (p.x / tileSize),
                    (int) (p.y / tileSize)
            );
            if (etile.x == tile.x && etile.y == tile.y) {
                return true;
            }
        }
        return false;
    }

    private void updateSummonPlacement(TerrainComponent terrain, Vector2 mouseWorld) {
        if (ghostSummon == null) return;

        // 计算网格、边界
        GridPoint2 tile = new GridPoint2(
                (int) (mouseWorld.x / terrain.getTileSize()),
                (int) (mouseWorld.y / terrain.getTileSize())
        );
        GridPoint2 bounds = terrain.getMapBounds(0);
        boolean inBounds = tile.x >= 0 && tile.y >= 0 && tile.x < bounds.x && tile.y < bounds.y;

        // 只允许在路径上
        boolean onPath = inBounds && isOnPath(tile);

        // 吸附/跟随
        Vector2 snapPos = inBounds ? terrain.tileToWorldPosition(tile.x, tile.y) : mouseWorld;

        // 移动幽灵
        ghostSummon.setPosition(snapPos);

        // 左键落地
        // 左键落地
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!onPath) return;

            // 新增：瓦片占位检测
            if (hasTurretOnTile(tile, terrain)) {
                return;
            }
            placeSummon(snapPos, tile);
        }

    }


    private void placeSummon(Vector2 snapPos, GridPoint2 tile) {
        if (ghostSummon != null) {
            ghostSummon.dispose();
            ghostSummon = null;
        }

        if ("turret".equals(pendingType)) {
            // 四向同时射击
            Vector2[] dirs = new Vector2[]{
                    new Vector2(-1, 0), // 左

            };

            // 可选：给每个炮台一个起始冷却偏移，避免同帧齐射卡顿
            float[] phaseShift = new float[]{0f, 0.25f, 0.50f, 0.75f};

            for (int i = 0; i < dirs.length; i++) {
                Vector2 d = dirs[i].nor();
                Entity t = SummonFactory.createDirectionalTurret(
                        pendingSummonTexture,
                        1f,          // 缩放
                        1.0f,        // 攻击间隔（你原来的值）
                        d            // 射击方向
                );

                // 如果 DirectionalTurret 支持设置起始冷却（可选）
                // 例如 t.getComponent(TurretShootComponent.class).setInitialCooldown(phaseShift[i]);
                // 没有就删掉这段注释

                t.setPosition(snapPos);
                ServiceLocator.getEntityService().register(t);
                t.create();
            }

        } else if ("currencyBot".equals(pendingType)) {
            // === 产币机器人分支 ===
            Entity owner = findPlayerEntity(); // ✅ 改成自动查找玩家

            Entity bot = SummonFactory.createCurrencyBot(
                    pendingSummonTexture,     // 贴图
                    1f,                       // 缩放比例
                    owner,                    // ★ 归属玩家
                    CurrencyType.METAL_SCRAP, // ★ 币种（可改）
                    300,                        // ★ 每次产币量
                    2f                      // ★ 间隔秒
            );

            bot.setPosition(snapPos);
            ServiceLocator.getEntityService().register(bot);
            bot.create();
            System.out.println(">>> currencyBot placed at " + tile);
        } else {
            // 原有近战/路障逻辑
            Entity summon = SummonFactory.createMeleeSummon(
                    pendingSummonTexture,
                    false,
                    1f
            );
            summon.setPosition(snapPos);
            ServiceLocator.getEntityService().register(summon);
            summon.create();
        }

        placementActive = false;
        mode = Mode.NONE;
        pendingType = "bone";
        System.out.println(">>> turret(四向) placed at " + tile);
    }


}