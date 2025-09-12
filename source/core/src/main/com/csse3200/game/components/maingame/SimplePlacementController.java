package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.TowerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.TowerFactory;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.MapEditor;

/**
 * Controller for managing tower placement within the game world.
 * <p>
 * Handles the "ghost" tower preview that follows the mouse, snaps towers to tiles,
 * ensures placement is within bounds, and prevents overlap with other towers and paths.
 * </p>
 */
public class SimplePlacementController extends Component {
    /** Whether tower placement is currently active */
    private boolean placementActive = false;
    /** Whether the mouse button must be released before placing */
    private boolean needRelease = false;
    /** The type of tower pending placement (bone, dino, cavemen) */
    private String pendingType = "bone";
    /** Camera used to project mouse coordinates into world space */
    private OrthographicCamera camera;
    /** Minimum spacing between towers (currently unused, adjacency is allowed) */
    private final float minSpacing = 1.0f;
    /** Ghost tower entity that previews placement before confirming */
    private Entity ghostTower = null;
    private MapEditor mapEditor;

    public void setMapEditor(MapEditor mapEditor) {
        this.mapEditor = mapEditor;
        loadPathFromMapEditor();
    }

    // All tiles that belong to the enemy path (invalid for tower placement)
    private static int[][] FIXED_PATH = { };

    public int[][] getFixedPath() {
        return FIXED_PATH;
    }


    private void loadPathFromMapEditor() {
        if (mapEditor == null) {
            System.out.println(">>> No MapEditor set, cannot load path tiles.");
            return;
        }

        java.util.List<GridPoint2> tiles = new java.util.ArrayList<>(mapEditor.getInvalidTiles().values());
        if (tiles == null || tiles.isEmpty()) {
            System.out.println(">>> MapEditor returned no path tiles!");
            return;
        }

        // Rebuild FIXED_PATH
        int[][] newPath = new int[tiles.size()][2];
        for (int i = 0; i < tiles.size(); i++) {
            GridPoint2 tile = tiles.get(i);
            newPath[i][0] = tile.x;
            newPath[i][1] = tile.y;
        }

        FIXED_PATH = newPath; // overwrite
        System.out.println(">>> Loaded " + FIXED_PATH.length + " path tiles from MapEditor");
    }

    @Override
    public void create() {
        entity.getEvents().addListener("startPlacementBone", this::armBone);
        entity.getEvents().addListener("startPlacementDino", this::armDino);
        entity.getEvents().addListener("startPlacementCavemen", this::armCavemen);
        System.out.println(">>> SimplePlacementController ready; minSpacing=" + minSpacing);
    }

    /** Arms placement for a Bone Tower. */
    private void armBone() { startPlacement("bone"); }
    /** Arms placement for a Dino Tower. */
    private void armDino() { startPlacement("dino"); }
    /** Arms placement for a Cavemen Tower. */
    private void armCavemen() { startPlacement("cavemen"); }

    private void startPlacement(String type) {
        pendingType = type;
        placementActive = true;
        needRelease = true;

        if ("dino".equalsIgnoreCase(type)) {
            ghostTower = TowerFactory.createDinoTower();
        } else if ("cavemen".equalsIgnoreCase(type)) {
            ghostTower = TowerFactory.createCavemenTower();
        } else {
            ghostTower = TowerFactory.createBoneTower();
        }

        ServiceLocator.getEntityService().register(ghostTower);
        System.out.println(">>> placement ON (" + type + ")");
    }

    @Override
    public void update() {
        if (camera == null) findWorldCamera();
        if (!placementActive || camera == null || ghostTower == null) return;

        if (needRelease) {
            if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) needRelease = false;
            return;
        }

        // Mouse world position
        Vector3 mousePos3D = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mousePos3D);
        Vector2 mouseWorld = new Vector2(mousePos3D.x, mousePos3D.y);

        TerrainComponent terrain = findTerrain();

        // Tower size (width x height in tiles)
        int towerWidth = 2;
        int towerHeight = 2;

        Vector2 snapPos = mouseWorld;
        boolean inBounds = true;
        GridPoint2 tile = null;

        if (terrain != null) {
            float tileSize = terrain.getTileSize();
            tile = new GridPoint2(
                    (int) (mouseWorld.x / tileSize),
                    (int) (mouseWorld.y / tileSize)
            );

            GridPoint2 mapBounds = terrain.getMapBounds(0);

            // Ensure the tower stays within map bounds
            if (tile.x < 0 || tile.y < 0
                    || tile.x + towerWidth > mapBounds.x
                    || tile.y + towerHeight > mapBounds.y) {
                inBounds = false;
            } else {
                snapPos = terrain.tileToWorldPosition(tile.x, tile.y);
            }
        } else {
            return;
        }

        ghostTower.setPosition(snapPos);

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            // Prevent placement if out of bounds
            if (!inBounds) {
                System.out.println(">>> blocked: cannot place " + pendingType + " outside map bounds");
                return;
            }

            // 🔹 Block placement if any part of the tower overlaps the path
            if (tile != null && isTowerOnPath(tile, towerWidth, towerHeight)) {
                System.out.println(">>> blocked: cannot place " + pendingType + " on/over path at tile " + tile);
                return;
            }

            if (!isPositionFree(snapPos, towerWidth, towerHeight, terrain)) {
                System.out.println(">>> blocked: cannot place " + pendingType + " at " + snapPos);
                return;
            }

            Entity newTower;
            if ("dino".equalsIgnoreCase(pendingType)) {
                newTower = TowerFactory.createDinoTower();
            } else if ("cavemen".equalsIgnoreCase(pendingType)) {
                newTower = TowerFactory.createCavemenTower();
            } else {
                newTower = TowerFactory.createBoneTower();
            }

            newTower.setPosition(snapPos);
            ServiceLocator.getEntityService().register(newTower);
            System.out.println(">>> placed " + pendingType + " at " + snapPos);

            ghostTower = null;
            placementActive = false;
        }
    }

    /** Returns true if any tile of a tower of size (width x height) at 'tile' overlaps the path */
    private boolean isTowerOnPath(GridPoint2 tile, int towerWidth, int towerHeight) {
        for (int tx = 0; tx < towerWidth; tx++) {
            for (int ty = 0; ty < towerHeight; ty++) {
                GridPoint2 t = new GridPoint2(tile.x + tx, tile.y + ty);
                if (isOnPath(t)) return true;
            }
        }
        return false;
    }

    private boolean isPositionFree(Vector2 candidate, int towerWidth, int towerHeight, TerrainComponent terrain) {
        Array<Entity> all = safeEntities();
        if (all == null || candidate == null) return true;

        float tileSize = terrain != null ? terrain.getTileSize() : 1.0f;

        for (int tx = 0; tx < towerWidth; tx++) {
            for (int ty = 0; ty < towerHeight; ty++) {
                Vector2 tilePos = new Vector2(candidate.x + tx * tileSize, candidate.y + ty * tileSize);
                for (Entity e : all) {
                    if (e == null || e == ghostTower) continue;
                    TowerComponent tower = e.getComponent(TowerComponent.class);
                    if (tower == null) continue;

                    Vector2 pos = e.getPosition();
                    if (pos == null) continue;

                    int existingWidth = tower.getWidth();
                    int existingHeight = tower.getHeight();
                    float existingTileSize = tileSize;

                    if (tilePos.x < pos.x + existingWidth * existingTileSize &&
                            tilePos.x + tileSize > pos.x &&
                            tilePos.y < pos.y + existingHeight * existingTileSize &&
                            tilePos.y + tileSize > pos.y) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

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

    private Array<Entity> safeEntities() {
        try {
            return ServiceLocator.getEntityService().getEntitiesCopy();
        } catch (Exception ex) {
            System.out.println("!!! getEntitiesCopy failed: " + ex.getMessage());
            return null;
        }
    }

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

    public void cancelPlacement() {
        if (ghostTower != null) {
            ghostTower.dispose();
            ghostTower = null;
        }
        placementActive = false;
        needRelease = false;
        System.out.println(">>> placement OFF");
    }

    public boolean isPlacementActive() { return placementActive; }
    public String getPendingType() { return pendingType; }

    // 🔹 Helper: check if a single tile is part of the fixed path
    private boolean isOnPath(GridPoint2 tile) {
        for (int[] p : FIXED_PATH) {
            if (p[0] == tile.x && p[1] == tile.y) {
                return true;
            }
        }
        return false;
    }
}
