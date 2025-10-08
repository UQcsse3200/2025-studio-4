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
import com.csse3200.game.components.towers.TowerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.TowerFactory;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.towers.TowerCostComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.configs.TowerConfig;
import com.csse3200.game.entities.factories.SummonFactory;


import java.util.ArrayList;
import java.util.List;

/**
 * Controller for managing tower placement within the game world.
 * <p>
 * Features:
 * <ul>
 *   <li>Ghost preview of tower before placement</li>
 *   <li>Snaps towers to grid tiles</li>
 *   <li>Prevents placement on fixed paths or overlapping towers</li>
 *   <li>Supports Bone, Dino, Cavemen, and Pterodactyl towers</li>
 *   <li>Pterodactyl heads orbit nests using OrbitComponent</li>
 * </ul>
 */
public class SimplePlacementController extends Component {

    /** Whether tower placement mode is active. */
    private boolean placementActive = false;

    /** Ensures left-click is released before placing tower. */
    private boolean needRelease = false;

    /** Pending tower type for placement (bone/dino/cavemen/pterodactyl). */
    private String pendingType = "bone";

    /** Reference to the world camera. */
    private OrthographicCamera camera;

    /** Minimum spacing between towers (world units). */
    private final float minSpacing = 1.0f;

    /** Ghost tower entity used for preview during placement. */
    private Entity ghostTower = null;

    /** Reference to the map editor for checking invalid tiles. */
    private MapEditor mapEditor;

    /** Static 2D array storing fixed path tiles to prevent tower placement. */
    private static int[][] FIXED_PATH = {};

    /**
     * Sets the map editor instance used to check invalid tiles.
     *
     * @param mapEditor the MapEditor instance
     */
    public void setMapEditor(MapEditor mapEditor) {
        this.mapEditor = mapEditor;
        refreshInvalidTiles();
    }

    /**
     * Refreshes the list of invalid tiles for tower placement from the MapEditor.
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
     * Returns the fixed path as a 2D array of tile coordinates.
     *
     * @return fixed path array
     */
    public int[][] getFixedPath() {
        return FIXED_PATH;
    }

    /**
     * Registers listeners for tower placement events.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("startPlacementBone", this::armBone);
        entity.getEvents().addListener("startPlacementDino", this::armDino);
        entity.getEvents().addListener("startPlacementCavemen", this::armCavemen);
        entity.getEvents().addListener("startPlacementPterodactyl", this::armPterodactyl);
        entity.getEvents().addListener("startPlacementSuperCavemen", this::armSuperCavemen);
        entity.getEvents().addListener("startPlacementTotem", this::armTotem);
        System.out.println(">>> SimplePlacementController ready; minSpacing=" + minSpacing);
    }


    /** Arms the controller to start placing a Bone tower. */
    /**
     * Arms the controller for bone tower placement.
     */
    private void armBone() {
        startPlacement("bone");
    }

    /** Arms the controller to start placing a Dino tower. */
    /**
     * Arms the controller for dino tower placement.
     */
    private void armDino() {
        startPlacement("dino");
    }

    /** Arms the controller to start placing a Cavemen tower. */
    private void armCavemen() {
        startPlacement("cavemen");
    }

    /** Arms the controller to start placing a Pterodactyl tower. */
    private void armPterodactyl() {
        startPlacement("pterodactyl");
    }

    /** Arms the controller to start placing a SuperCavemen tower. */
    private void armSuperCavemen() {
        startPlacement("supercavemen");
    }

    /** Arms the controller to start placing a Totem tower. */
    private void armTotem() {
        startPlacement("totem");
    }

    /**
     * Public API for UI to request a placement directly on this controller instance.
     * This avoids trying to trigger events on other entities (hotbar -> placement controller).
     *
     * @param type canonical tower type string ("bone","dino","cavemen","pterodactyl","supercavemen")
     */
    public void requestPlacement(String type) {
        if (type == null) return;
        startPlacement(type);
    private void armCavemen() {
        startPlacement("cavemen");
    }

    /**
     * Starts placement mode for the specified tower type.
     *
     * @param type tower type ("bone", "dino", "cavemen", "pterodactyl")
     */
    private void startPlacement(String type) {
        pendingType = type;
        placementActive = true;
        needRelease = true;

        switch (type.toLowerCase()) {
            case "dino" -> ghostTower = TowerFactory.createDinoTower();
            case "cavemen" -> ghostTower = TowerFactory.createCavemenTower();
            case "pterodactyl" -> ghostTower = TowerFactory.createPterodactylTower();
            case "supercavemen" -> ghostTower = TowerFactory.createSuperCavemenTower();
            case "totem" -> ghostTower = TowerFactory.createTotemTower();
            default -> ghostTower = TowerFactory.createBoneTower();
        }

        TowerComponent tc = ghostTower.getComponent(TowerComponent.class);
        if (tc != null) tc.setActive(false);
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
     * Updates placement controller each frame:
     * <ul>
     *   <li>Handles ghost tower movement</li>
     *   <li>Snaps tower to grid</li>
     *   <li>Places tower if valid</li>
     * </ul>
     */
    @Override
    public void update() {
        if (camera == null) findWorldCamera();
        if (!placementActive || camera == null || ghostTower == null) return;

        // Wait for left-click release
        if (needRelease) {
            if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) needRelease = false;
            return;
        }

        // Get mouse world position
        Vector3 mousePos3D = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mousePos3D);
        Vector2 mouseWorld = new Vector2(mousePos3D.x, mousePos3D.y);

        TerrainComponent terrain = findTerrain();
        if (terrain == null) return;

        TowerComponent ghostTC = ghostTower.getComponent(TowerComponent.class);
        int towerWidth = ghostTC != null ? ghostTC.getWidth() : 1;
        int towerHeight = ghostTC != null ? ghostTC.getHeight() : 1;
        float tileSize = terrain.getTileSize();

        // Snap to grid
        GridPoint2 tile = new GridPoint2(
                (int) (mouseWorld.x / tileSize),
                (int) (mouseWorld.y / tileSize)
        );

        GridPoint2 mapBounds = terrain.getMapBounds(0);
        boolean inBounds = tile.x >= 0 && tile.y >= 0 &&
                tile.x + towerWidth <= mapBounds.x &&
                tile.y + towerHeight <= mapBounds.y;

        Vector2 snapPos;
        if (inBounds) {
            snapPos = terrain.tileToWorldPosition(tile.x, tile.y);
        } else {
            // Prevent ghost tower from snapping outside map
            snapPos = terrain.tileToWorldPosition(
                Math.max(0, Math.min(tile.x, mapBounds.x - towerWidth)),
                Math.max(0, Math.min(tile.y, mapBounds.y - towerHeight))
            );
        }
        ghostTower.setPosition(snapPos);

        // Only allow placement if in bounds
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && inBounds) {
            if (isTowerOnPath(tile, towerWidth, towerHeight) ||
                    !isPositionFree(snapPos, towerWidth, towerHeight, terrain)) return;

            // --- Begin: Cost logic ---
            // Get player entity and currency manager
            Entity player = findPlayer();
            CurrencyManagerComponent cm = player != null ? player.getComponent(CurrencyManagerComponent.class) : null;

            // Get cost from TowerConfig
            TowerConfig.TowerStats stats = null;
            switch (pendingType.toLowerCase()) {
                case "dino" -> stats = TowerFactory.getTowerConfig().dinoTower.base;
                case "cavemen" -> stats = TowerFactory.getTowerConfig().cavemenTower.base;
                case "pterodactyl" -> stats = TowerFactory.getTowerConfig().pterodactylTower.base;
                case "supercavemen" -> stats = TowerFactory.getTowerConfig().supercavemenTower.base;
                case "totem" -> stats = TowerFactory.getTowerConfig().totemTower.base;
                default -> stats = TowerFactory.getTowerConfig().boneTower.base;
            }

            // Build cost map
            java.util.Map<CurrencyType, Integer> costMap = new java.util.HashMap<>();
            if (stats != null) {
                if (stats.metalScrapCost > 0) costMap.put(CurrencyType.METAL_SCRAP, stats.metalScrapCost);
                if (stats.titaniumCoreCost > 0) costMap.put(CurrencyType.TITANIUM_CORE, stats.titaniumCoreCost);
                if (stats.neurochipCost > 0) costMap.put(CurrencyType.NEUROCHIP, stats.neurochipCost);
            }

            // Check if player can afford
            if (cm != null && !costMap.isEmpty() && !cm.canAffordAndSpendCurrency(costMap)) {
                System.out.println(">>> Not enough currency to place " + pendingType + " tower.");
                return;
            }
            // --- End: Cost logic ---

            // Create new tower entity
            Entity newTower;
            switch (pendingType.toLowerCase()) {
                case "dino" -> newTower = TowerFactory.createDinoTower();
                case "cavemen" -> newTower = TowerFactory.createCavemenTower();
                case "pterodactyl" -> newTower = TowerFactory.createPterodactylTower();
                case "supercavemen" -> newTower = TowerFactory.createSuperCavemenTower();
                case "totem" -> newTower = TowerFactory.createTotemTower();
                default -> newTower = TowerFactory.createBoneTower();
            }

            // Attach TowerCostComponent to the new tower
            if (!costMap.isEmpty()) {
                newTower.addComponent(new TowerCostComponent(costMap));
            }

            // Remove ghost tower
            ghostTower.dispose();
            ghostTower = null;

            // Place new tower
            newTower.setPosition(snapPos);

            TowerComponent newTC = newTower.getComponent(TowerComponent.class);
            if (newTC != null && newTC.hasHead() && !pendingType.equalsIgnoreCase("pterodactyl")) {
                Vector2 headOffset = new Vector2(towerWidth * tileSize / 2f, towerHeight * tileSize / 2f);
                newTC.getHeadEntity().setPosition(snapPos.x + headOffset.x, snapPos.y + headOffset.y - 0.01f);
            }

            ServiceLocator.getEntityService().register(newTower);
            if (newTC != null && newTC.hasHead()) ServiceLocator.getEntityService().register(newTC.getHeadEntity());

            placementActive = false;
        }
    }

    private Entity findPlayer() {
        Array<Entity> all = safeEntities();
        if (all == null) return null;
        for (Entity e : all) {
            if (e != null && e.getComponent(com.csse3200.game.components.player.PlayerActions.class) != null) {
                return e;
            }
        }
        return null;
    }



    /** Checks if tower overlaps the fixed path. */

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

    /** Checks if a single tile is on the fixed path. */
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

    /** Checks if a given world position is free for tower placement. */
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

    /** Finds the terrain component from registered entities. */
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

    /** Gets a safe copy of all entities from the entity service. */
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

    /** Finds the world camera from registered entities. */
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

    /** Cancels current placement and disposes of the ghost tower. */
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

    /**
     * Cancels the current placement and disposes of the ghost tower.
     */
    public void cancelPlacement() {
        if (ghostTower != null) {
            ghostTower.dispose();
            ghostTower = null;
        }
        placementActive = false;
        needRelease = false;
        System.out.println(">>> placement OFF");
    }

    /** Returns true if tower placement mode is currently active. */
    /**
     * Checks if placement mode is currently active.
     *
     * @return true if placement is active, false otherwise
     */
    public boolean isPlacementActive() {
        return placementActive;
    }

    /** Returns the pending tower type for placement. */
    public String getPendingType() {
        return pendingType;
    }
}

// The placement controller only creates a ghost tower for preview when you click a hotbar icon.
// It does NOT place a real tower until you click on the map and all checks (location, cost, overlap) pass.
// If towers are being created instantly, check your UI/hotbar code for direct calls to TowerFactory.
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

        public SummonSpec(String texture) {
            this.texture = texture;
        }
    }

    public void armSummon(SummonSpec spec) {
        // 先清理任何已有放置
        cancelPlacement();

        // 进入召唤物模式
        this.mode = Mode.SUMMON;
        this.placementActive = true;
        this.needRelease = true;
        this.pendingType = "summon";
        this.pendingSummonTexture = (spec != null && spec.texture != null && !spec.texture.isEmpty())
                ? spec.texture : "images/engineer/Sentry.png";

        // 1) 造“幽灵召唤物”：只显示，不攻击/不阻挡（工厂里你已实现 createMeleeSummonGhost）
        this.ghostSummon = SummonFactory.createMeleeSummonGhost(this.pendingSummonTexture, 1f);

        // 2) 注册并初始化
        ServiceLocator.getEntityService().register(this.ghostSummon);
        this.ghostSummon.create();

        System.out.println(">>> placement ON (summon)");
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
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!onPath) return; // 不合法直接忽略
            placeSummon(snapPos, tile);
        }
    }

    private void placeSummon(Vector2 snapPos, GridPoint2 tile) {
        // 清理幽灵
        if (ghostSummon != null) {
            ghostSummon.dispose();
            ghostSummon = null;
        }

        // 造真实召唤物（可挡路/能攻击）
        Entity summon = SummonFactory.createMeleeSummon(
                (pendingSummonTexture != null && !pendingSummonTexture.isEmpty())
                        ? pendingSummonTexture : "images/engineer/Sentry.png",
                /*colliderSensor=*/false,
                /*scale=*/1f
        );
        summon.setPosition(snapPos);
        ServiceLocator.getEntityService().register(summon);
        summon.create();

        // 退出模式并复位标志
        placementActive = false;
        mode = Mode.NONE;
        pendingType = "bone"; // 可选：恢复默认塔类型
        System.out.println(">>> SUMMON placed at " + tile);
    }


}
