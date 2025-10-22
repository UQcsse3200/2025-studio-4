package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.IMapEditor;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.Component;

import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.hero.engineer.EngineerSummonComponent;
import com.csse3200.game.components.hero.engineer.SummonOwnerComponent;

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

    /**
     * Whether tower placement mode is active.
     */
    private boolean placementActive = false;

    /**
     * Ensures left-click is released before placing tower.
     */
    private boolean needRelease = false;

    /**
     * Pending tower type for placement (bone/dino/cavemen/pterodactyl).
     */
    private String pendingType = "bone";

    /**
     * Reference to the world camera.
     */
    private OrthographicCamera camera;

    /**
     * Minimum spacing between towers (world units).
     */
    private final float minSpacing = 1.0f;

    /**
     * Ghost tower entity used for preview during placement.
     */
    private Entity ghostTower = null;

    /**
     * Reference to the map editor for checking invalid tiles.
     */
    private IMapEditor mapEditor; // Use interface instead of MapEditor
    private static int[][] FIXED_PATH = {};
    private static int[][] WATER_TILES = {};

    private static int[][] PATH_TILES = {};

    // --- Currency selection for placement ---
    private com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType selectedCurrencyType =
            com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType.METAL_SCRAP;

    /**
     * Sets the selected currency type for tower placement.
     * @param currencyType The currency type to select.
     */
    /**
     * Sets the selected currency type for tower placement.
     *
     * @param currencyType The currency type to select.
     */
    public void setSelectedCurrencyType(com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType currencyType) {
        this.selectedCurrencyType = currencyType;
    }

    /**
     * Sets the map editor instance used to check invalid tiles.
     *
     * @param mapEditor the IMapEditor instance
     */
    public void setMapEditor(IMapEditor mapEditor) {
        this.mapEditor = mapEditor;
        refreshInvalidTiles();
        refreshPathTiles();
        refreshWaterTiles();
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
     * Refreshes the list of water tiles for raft tower placement from the MapEditor.
     */
    public void refreshWaterTiles() {
        if (mapEditor == null) return;
        List<GridPoint2> tiles = mapEditor.getWaterTiles();
        if (tiles == null || tiles.isEmpty()) return;
        int[][] newWater = new int[tiles.size()][2];
        for (int i = 0; i < tiles.size(); i++) {
            GridPoint2 t = tiles.get(i);
            newWater[i][0] = t.x;
            newWater[i][1] = t.y;
        }
        WATER_TILES = newWater;
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
     * Returns the water tiles as a 2D array of tile coordinates.
     *
     * @return water tiles array
     */
    public int[][] getWaterTiles() {
        return WATER_TILES;
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
        entity.getEvents().addListener("startPlacementBank", this::armBank);
        entity.getEvents().addListener("startPlacementRaft", this::armRaft);
        entity.getEvents().addListener("startPlacementFrozenmamoothskull", this::armFrozenmamoothskull);
        entity.getEvents().addListener("startPlacementBouldercatapult", this::armBouldercatapult);
        entity.getEvents().addListener("startPlacementVillageshaman", this::armVillageshaman);
        System.out.println(">>> SimplePlacementController ready; minSpacing=" + minSpacing);
    }

    /**
     * Arms the controller to start placing a Bone tower.
     */
    private void armBone() {
        startPlacement("bone");
    }

    /**
     * Arms the controller to start placing a Dino tower.
     */
    private void armDino() {
        startPlacement("dino");
    }

    /**
     * Arms the controller to start placing a Cavemen tower.
     */
    private void armCavemen() {
        startPlacement("cavemen");
    }

    /**
     * Arms the controller to start placing a Pterodactyl tower.
     */
    private void armPterodactyl() {
        startPlacement("pterodactyl");
    }

    /**
     * Arms the controller to start placing a SuperCavemen tower.
     */
    private void armSuperCavemen() {
        startPlacement("supercavemen");
    }

    /**
     * Arms the controller to start placing a Totem tower.
     */
    private void armTotem() {
        startPlacement("totem");
    }

    /**
     * Arms the controller to start placing a Bank tower.
     */
    private void armBank() {
        startPlacement("bank");
    }

    /**
     * Arms the controller to start placing a Raft tower.
     */
    private void armRaft() {
        startPlacement("raft");
    }

    /**
     * Arms the controller to start placing a FrozenMamoothSkull tower.
     */
    private void armFrozenmamoothskull() {
        startPlacement("frozenmamoothskull");
    }

    /**
     * Arms the controller to start placing a BoulderCatapult tower.
     */
    private void armBouldercatapult() {
        startPlacement("bouldercatapult");
    }

    /**
     * Arms the controller to start placing a VillageShaman tower.
     */
    private void armVillageshaman() {
        startPlacement("villageshaman");
    }

    /**
     * Public API for UI to request a placement directly on this controller instance.
     * This avoids trying to trigger events on other entities (hotbar -> placement controller).
     *
     * @param type canonical tower type string ("bone","dino","cavemen","pterodactyl","supercavemen","totem","bank")
     */
    public void requestPlacement(String type) {
        if (type == null) return;
        startPlacement(type);
    }

    /**
     * Starts placement mode for the specified tower type.
     *
     * @param type tower type ("bone", "dino", "cavemen", "pterodactyl", "supercavemen", "totem", "bank")
     */
    private void startPlacement(String type) {
        pendingType = type;
        placementActive = true;
        needRelease = true;

        switch (type.toLowerCase()) {
            case "bone" -> ghostTower = TowerFactory.createBoneTower();
            case "dino" -> ghostTower = TowerFactory.createDinoTower();
            case "cavemen" -> ghostTower = TowerFactory.createCavemenTower();
            case "pterodactyl" -> ghostTower = TowerFactory.createPterodactylTower();
            case "supercavemen" -> ghostTower = TowerFactory.createSuperCavemenTower();
            case "totem" -> ghostTower = TowerFactory.createTotemTower();
            case "bank" -> ghostTower = TowerFactory.createBankTower();
            case "raft" -> ghostTower = TowerFactory.createRaftTower();
            case "frozenmamoothskull" -> ghostTower = TowerFactory.createFrozenmamoothskullTower();
            case "bouldercatapult" -> ghostTower = TowerFactory.createBouldercatapultTower();
            case "villageshaman" -> ghostTower = TowerFactory.createVillageshamanTower();
            default -> ghostTower = TowerFactory.createBoneTower();
        }

        TowerComponent tc = ghostTower.getComponent(TowerComponent.class);
        if (tc != null) tc.setActive(false);

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
        if (!placementActive || camera == null) return;


        com.csse3200.game.areas.terrain.ITerrainComponent terrain = findTerrain();
        if (terrain == null) return;

        // 屏幕 -> 世界
        Vector3 mousePos3D = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mousePos3D);
        Vector2 mouseWorld = new Vector2(mousePos3D.x, mousePos3D.y);


        if (ghostTower == null) return;

        // Wait for left-click release
        if (needRelease) {
            if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) needRelease = false;
            return;
        }


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
        boolean inBounds = !(tile.x < 0 || tile.y < 0
                || tile.x + towerWidth > mapBounds.x
                || tile.y + towerHeight > mapBounds.y);


        Vector2 snapPos;
        if (inBounds) {
            snapPos = terrain.tileToWorldPosition(tile.x, tile.y);
        } else {
            snapPos = terrain.tileToWorldPosition(
                    Math.max(0, Math.min(tile.x, mapBounds.x - towerWidth)),
                    Math.max(0, Math.min(tile.y, mapBounds.y - towerHeight))
            );
        }
        ghostTower.setPosition(snapPos);

        // Only allow placement if in bounds
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && inBounds) {
            // Raft tower: only allow placement on water tiles
            if (pendingType.equalsIgnoreCase("raft")) {
                if (!isTowerOnWater(tile, towerWidth, towerHeight) ||
                        !isPositionFree(snapPos, towerWidth, towerHeight, terrain)) return;
            } else {
                // For all other towers: only block placement on path/barrier/snowtree/invalid tiles
                if (isTowerOnInvalid(tile, towerWidth, towerHeight) ||
                        !isPositionFree(snapPos, towerWidth, towerHeight, terrain)) return;
            }

            // --- Begin: Cost logic ---
            // Get player entity and currency manager
            Entity player = findPlayer();
            CurrencyManagerComponent cm = player != null ? player.getComponent(CurrencyManagerComponent.class) : null;

            // Get cost from TowerConfig
            TowerConfig.TowerStats stats = null;
            switch (pendingType.toLowerCase()) {
                case "bone" -> stats = TowerFactory.getTowerConfig().boneTower.base;
                case "dino" -> stats = TowerFactory.getTowerConfig().dinoTower.base;
                case "cavemen" -> stats = TowerFactory.getTowerConfig().cavemenTower.base;
                case "pterodactyl" -> stats = TowerFactory.getTowerConfig().pterodactylTower.base;
                case "supercavemen" -> stats = TowerFactory.getTowerConfig().supercavemenTower.base;
                case "totem" -> stats = TowerFactory.getTowerConfig().totemTower.base;
                case "bank" -> stats = TowerFactory.getTowerConfig().bankTower.base;
                case "raft" -> stats = TowerFactory.getTowerConfig().raftTower.base;
                case "frozenmamoothskull" -> stats = TowerFactory.getTowerConfig().frozenmamoothskullTower.base;
                case "bouldercatapult" -> stats = TowerFactory.getTowerConfig().bouldercatapultTower.base;
                case "villageshaman" -> stats = TowerFactory.getTowerConfig().villageshamanTower.base;
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
                case "bone" -> newTower = TowerFactory.createBoneTower();
                case "dino" -> newTower = TowerFactory.createDinoTower();
                case "cavemen" -> newTower = TowerFactory.createCavemenTower();
                case "pterodactyl" -> newTower = TowerFactory.createPterodactylTower();
                case "supercavemen" -> newTower = TowerFactory.createSuperCavemenTower();
                case "totem" -> newTower = TowerFactory.createTotemTower();
                case "bank" -> newTower = TowerFactory.createBankTower();
                case "raft" -> newTower = TowerFactory.createRaftTower();
                case "frozenmamoothskull" -> newTower = TowerFactory.createFrozenmamoothskullTower();
                case "bouldercatapult" -> newTower = TowerFactory.createBouldercatapultTower();
                case "villageshaman" -> newTower = TowerFactory.createVillageshamanTower();
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

            /**
             * Finds the player entity from registered entities.
             * @return The player entity, or null if not found.
             */
            placementActive = false;
        }
    }

    /**
     * Finds the player entity from registered entities.
     *
     * @return The player entity, or null if not found.
     */
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


    /**
     * Checks if tower overlaps the fixed path.
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
     * Checks if a single tile is on the fixed path.
     */
    private boolean isOnPath(GridPoint2 tile) {
        for (int[] p : FIXED_PATH) {
            if (p[0] == tile.x && p[1] == tile.y) return true;
        }
        return false;
    }

    /**
     * Checks if a given world position is free for tower placement.
     */
    private boolean isPositionFree(Vector2 candidate, int towerWidth, int towerHeight, com.csse3200.game.areas.terrain.ITerrainComponent terrain) {
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
     * Finds the terrain component from registered entities.
     */
    private com.csse3200.game.areas.terrain.ITerrainComponent findTerrain() {
        Array<Entity> all = safeEntities();
        if (all == null) return null;
        for (Entity e : all) {
            if (e == null) continue;
            com.csse3200.game.areas.terrain.TerrainComponent t = e.getComponent(com.csse3200.game.areas.terrain.TerrainComponent.class);
            if (t != null) return t;
            // --- Support TerrainComponent2 ---
            com.csse3200.game.areas2.terrainTwo.TerrainComponent2 t2 = e.getComponent(com.csse3200.game.areas2.terrainTwo.TerrainComponent2.class);
            if (t2 != null) return t2; // TerrainComponent2 implements ITerrainComponent
        }
        return null;
    }

    /**
     * Gets a safe copy of all entities from the entity service.
     */
    private Array<Entity> safeEntities() {
        try {
            return ServiceLocator.getEntityService().getEntitiesCopy();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Finds the world camera from registered entities.
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
            e.dispose();
        } catch (Exception ignore) {
        }
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
        pendingType = "bone";
        System.out.println(">>> placement OFF");
    }

    /**
     * Returns true if tower placement mode is currently active.
     */
    public boolean isPlacementActive() {
        return placementActive;
    }

    /**
     * Returns the pending tower type for placement.
     */
    public String getPendingType() {
        return pendingType;
    }

    /**
     * Checks if tower overlaps the water tiles (for raft tower).
     */
    private boolean isTowerOnWater(GridPoint2 tile, int towerWidth, int towerHeight) {
        for (int tx = 0; tx < towerWidth; tx++) {
            for (int ty = 0; ty < towerHeight; ty++) {
                if (!isOnWater(new GridPoint2(tile.x + tx, tile.y + ty))) return false;
            }
        }
        return true;
    }

    /**
     * Checks if a single tile is on the water area.
     */
    private boolean isOnWater(GridPoint2 tile) {
        for (int[] p : WATER_TILES) {
            if (p[0] == tile.x && p[1] == tile.y) return true;
        }
        return false;
    }

    // === Inside the SimplePlacementController class ===

    /**
     * Pending summon texture (used for preview/ghost rendering).
     */
    private String pendingSummonTexture = null;

    /**
     * Placement mode (NONE = inactive, TOWER = placing tower, SUMMON = placing summon).
     */
    private enum Mode {NONE, TOWER, SUMMON}


    /**
     * Checks if tower overlaps any invalid tiles (path, barrier, snowtree, snow, etc).
     */
    private boolean isTowerOnInvalid(GridPoint2 tile, int towerWidth, int towerHeight) {
        for (int tx = 0; tx < towerWidth; tx++) {
            for (int ty = 0; ty < towerHeight; ty++) {
                if (isOnInvalid(new GridPoint2(tile.x + tx, tile.y + ty))) return true;
            }
        }
        return false;
    }

    /**
     * Checks if a single tile is in the invalid tiles array.
     */
    private boolean isOnInvalid(GridPoint2 tile) {
        for (int[] p : FIXED_PATH) {
            if (p[0] == tile.x && p[1] == tile.y) return true;
        }
        return false;
    }

    public void refreshPathTiles() {
        if (mapEditor == null) return;
        List<GridPoint2> tiles = mapEditor.getPathTiles(); // ✅ 只拿路径
        if (tiles == null || tiles.isEmpty()) { PATH_TILES = new int[0][0]; return; }

        int[][] arr = new int[tiles.size()][2];
        for (int i = 0; i < tiles.size(); i++) {
            arr[i][0] = tiles.get(i).x;
            arr[i][1] = tiles.get(i).y;
        }
        PATH_TILES = arr;
    }

    public boolean isPath(int x, int y) {
        // 优先问 IMapEditor，拿不到就用缓存（或遍历 PATH_TILES）
        if (mapEditor != null) return mapEditor.isPath(x, y);
        for (int[] p : PATH_TILES) if (p[0] == x && p[1] == y) return true;
        return false;
    }

}

// The placement controller only creates a ghost tower for preview when you click a hotbar icon.
// It does NOT place a real tower until you click on the map and all checks (location, cost, overlap) pass.
// If towers are being created instantly, check your UI/hotbar code for direct calls to TowerFactory.
