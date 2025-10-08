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
import com.csse3200.game.components.hero.engineer.EngineerSummonComponent;
import com.csse3200.game.components.hero.engineer.SummonOwnerComponent;
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

        if (ghostSummon != null) {
            safeDispose(ghostSummon);
            ghostSummon = null;
        }

        placementActive = false;
        needRelease = false;
        mode = Mode.NONE;
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
     * Current placement mode.
     */
    private Mode mode = Mode.NONE;

    /**
     * Temporary ghost entity for summon placement preview.
     */
    private Entity ghostSummon = null;

    /**
     * Generic placement ghost (used for towers, etc.).
     */
    private Entity ghost = null;

    /**
     * Specification for a summon being placed.
     * <p>
     * Holds data such as the summon’s texture and type. Designed to be
     * easily extended (e.g., to include attack range, stats, or cost).
     * </p>
     */
    public static class SummonSpec {
        public final String texture; // Texture for the summon
        public final String type;    // Summon type: "melee", "turret", "currencyBot", etc.

        public SummonSpec(String texture, String type) {
            this.texture = texture;
            this.type = type;
        }
    }

    /**
     * Arms the system for summon placement (e.g., when the player presses 1–3).
     * <p>
     * Initializes placement mode, creates a ghost summon for visual preview,
     * and registers it to the world for rendering and following the mouse.
     * </p>
     *
     * @param spec Summon specification (texture + type)
     */
    public void armSummon(SummonSpec spec) {
        cancelPlacement(); // Cancel any previous placement action

        this.mode = Mode.SUMMON;
        this.placementActive = true;
        this.needRelease = true;
        this.pendingType = spec.type;
        this.pendingSummonTexture = (spec.texture != null && !spec.texture.isEmpty())
                ? spec.texture : "images/engineer/Sentry.png";

        // Create the ghost entity based on summon type
        if ("turret".equals(spec.type)) {
            // Use melee summon ghost as placeholder for turret preview
            this.ghostSummon = SummonFactory.createMeleeSummonGhost(this.pendingSummonTexture, 1f);
        } else {
            // Default ghost for melee or other summon types
            this.ghostSummon = SummonFactory.createMeleeSummonGhost(this.pendingSummonTexture, 1f);
        }

        // Register the ghost entity so it appears in the world
        ServiceLocator.getEntityService().register(this.ghostSummon);
        this.ghostSummon.create();

        System.out.println(">>> placement ON (summon: " + spec.type + ")");
    }

    /**
     * Checks whether there is already a summon occupying the given tile.
     * <p>
     * Determines occupancy by checking for any entity that represents a summon,
     * including turrets, melee summons, or currency generators.
     * A summon is identified if it has either a
     * {@link com.csse3200.game.components.hero.engineer.SummonOwnerComponent}
     * or an {@link com.csse3200.game.components.hero.engineer.OwnerComponent}.
     * </p>
     *
     * @param tile    The tile position to check.
     * @param terrain Reference to the terrain component (used for tile size).
     * @return {@code true} if any summon entity exists on that tile; otherwise {@code false}.
     */
    private boolean hasSummonOnTile(GridPoint2 tile, TerrainComponent terrain) {
        Array<Entity> all = safeEntities();
        if (all == null) return false;
        float tileSize = terrain.getTileSize();

        for (Entity e : all) {
            if (e == null) continue;

            // 🔍 Check whether the entity represents a summon:
            // It must have either SummonOwnerComponent or OwnerComponent
            boolean isSummon =
                    e.getComponent(com.csse3200.game.components.hero.engineer.SummonOwnerComponent.class) != null
                            || e.getComponent(com.csse3200.game.components.hero.engineer.OwnerComponent.class) != null;

            if (!isSummon) continue;

            Vector2 p = e.getPosition();
            if (p == null) continue;

            // Convert summon’s position to tile coordinates
            GridPoint2 etile = new GridPoint2(
                    (int) (p.x / tileSize),
                    (int) (p.y / tileSize)
            );

            // If a summon occupies the same tile, mark as occupied
            if (etile.x == tile.x && etile.y == tile.y) {
                return true; // ✅ Found summon on same tile
            }
        }
        return false;
    }

    /**
     * Updates summon placement preview each frame.
     * <p>
     * Moves the summon "ghost" (preview entity) to follow the mouse cursor,
     * snapping it to tile positions if valid, and handles placement when
     * the player left-clicks. Summons can only be placed on path tiles,
     * and overlapping placement on the same tile is prevented.
     * </p>
     *
     * @param terrain    The terrain reference (for tile conversion).
     * @param mouseWorld The current mouse position in world coordinates.
     */
    private void updateSummonPlacement(TerrainComponent terrain, Vector2 mouseWorld) {
        if (ghostSummon == null) return;

        // Convert mouse position to grid coordinates
        GridPoint2 tile = new GridPoint2(
                (int) (mouseWorld.x / terrain.getTileSize()),
                (int) (mouseWorld.y / terrain.getTileSize())
        );
        GridPoint2 bounds = terrain.getMapBounds(0);
        boolean inBounds = tile.x >= 0 && tile.y >= 0 && tile.x < bounds.x && tile.y < bounds.y;

        // Placement allowed only on path tiles
        boolean onPath = inBounds && isOnPath(tile);

        // Snap ghost to the grid position if in bounds; otherwise follow the mouse freely
        Vector2 snapPos = inBounds ? terrain.tileToWorldPosition(tile.x, tile.y) : mouseWorld;

        // Move ghost entity to preview location
        ghostSummon.setPosition(snapPos);

        // Left-click to place summon
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!onPath) return; // Must be on a valid path tile
            if (hasSummonOnTile(tile, terrain)) return; // Prevent overlapping summons

            // Proceed to finalize placement
            placeSummon(snapPos, tile);
        }
    }

    /**
     * Finalizes summon placement and spawns the actual summon entity in the world.
     * <p>
     * Depending on {@code pendingType}, this method creates a turret, a currency bot,
     * or a melee summon. The placement ghost is destroyed once placement is confirmed.
     * </p>
     *
     * @param snapPos The world position where the summon should be placed.
     * @param tile    The grid tile coordinate of the placement.
     */
    private void placeSummon(Vector2 snapPos, GridPoint2 tile) {
        // === Step 1: Check if placement is allowed via event system ===
        String typeToPlace = pendingType; // Save type before reset for accurate logging
        if (!requestCanSpawn(typeToPlace)) {
            if (ghostSummon != null) {
                ghostSummon.dispose();
                ghostSummon = null;
            }
            placementActive = false;
            mode = Mode.NONE;
            pendingType = "bone";
            System.out.println(">>> summon blocked by cap at " + tile + " type=" + typeToPlace);
            return;
        }

        // === Step 2: Clean up placement ghost ===
        if (ghostSummon != null) {
            ghostSummon.dispose();
            ghostSummon = null;
        }

        EngineerSummonComponent owner = findEngineerOwner();

        // === Step 3: Create summon based on type ===
        if ("turret".equals(typeToPlace)) {
            // Example: create a directional turret facing left (can be extended for multiple directions)
            Vector2[] dirs = new Vector2[]{
                    new Vector2(-1, 0)
            };

            for (Vector2 d : dirs) {
                Entity t = SummonFactory.createDirectionalTurret(
                        pendingSummonTexture, 1f, 1.0f, d.nor()
                );
                t.addComponent(new SummonOwnerComponent(owner, typeToPlace));
                t.setPosition(snapPos);
                ServiceLocator.getEntityService().register(t);
                t.create();
            }

        } else if ("currencyBot".equals(typeToPlace)) {
            // Create a currency generator bot
            Entity player = findPlayerEntity();

            Entity bot = SummonFactory.createCurrencyBot(
                    pendingSummonTexture,
                    1f,
                    player,
                    CurrencyType.METAL_SCRAP,
                    300,   // collection radius or range
                    2f     // collection interval (seconds)
            );
            bot.addComponent(new SummonOwnerComponent(owner, typeToPlace));
            bot.setPosition(snapPos);
            ServiceLocator.getEntityService().register(bot);
            bot.create();
            System.out.println(">>> currencyBot placed at " + tile);

        } else {
            // Default: melee summon (e.g., sentry or barrier)
            Entity summon = SummonFactory.createMeleeSummon(
                    pendingSummonTexture, false, 1f
            );
            summon.addComponent(new SummonOwnerComponent(owner, typeToPlace));
            summon.setPosition(snapPos);
            ServiceLocator.getEntityService().register(summon);
            summon.create();
        }

        // === Step 4: Reset placement state ===
        placementActive = false;
        mode = Mode.NONE;
        pendingType = "bone";
        System.out.println(">>> summon placed at " + tile + " type=" + typeToPlace);
    }

    /**
     * Sends an event to check whether the engineer can spawn a new summon of the given type.
     * <p>
     * If no EngineerSummonComponent is found, the placement is allowed by default.
     * </p>
     *
     * @param type The type of summon being requested (e.g., "turret", "melee").
     * @return {@code true} if placement is allowed, otherwise {@code false}.
     */
    private boolean requestCanSpawn(String type) {
        EngineerSummonComponent owner = findEngineerOwner();
        if (owner == null) return true; // Allow placement if no engineer found
        final boolean[] allow = {true};
        owner.getEntity().getEvents().trigger("summon:canSpawn?", type, allow);
        return allow[0];
    }

    /**
     * Finds the EngineerSummonComponent that represents the owner of summons.
     * <p>
     * This allows the placement controller to query summon caps, cooldowns, etc.
     * </p>
     *
     * @return The {@link EngineerSummonComponent} if found, or {@code null} if none exists.
     */
    private EngineerSummonComponent findEngineerOwner() {
        var all = ServiceLocator.getEntityService().getEntitiesCopy();
        if (all == null) return null;
        for (var e : all) {
            if (e == null) continue;
            EngineerSummonComponent c = e.getComponent(EngineerSummonComponent.class);
            if (c != null) return c;
        }
        return null;
    }


}