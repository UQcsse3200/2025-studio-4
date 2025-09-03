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

/**
 * Controller for managing tower placement within the game world.
 * <p>
 * Handles the "ghost" tower preview that follows the mouse, snaps towers to tiles,
 * ensures placement is within bounds, and prevents overlap with other towers.
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

    /**
     * Registers event listeners for starting tower placement.
     */
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

    /**
     * Starts placement of a tower of the given type.
     *
     * @param type tower type ("bone", "dino", "cavemen")
     */
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

    /**
     * Updates the placement logic each frame.
     * <p>
     * - Moves the ghost tower to follow the mouse.<br>
     * - Snaps the ghost to valid tiles.<br>
     * - Ensures placement is in bounds and not overlapping.<br>
     * - Places the tower when the mouse is clicked.
     * </p>
     */
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

        if (terrain != null) {
            float tileSize = terrain.getTileSize();
            GridPoint2 tile = new GridPoint2(
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

    /**
     * Checks if the candidate position is free for tower placement.
     *
     * @param candidate   position to test
     * @param towerWidth  tower width in tiles
     * @param towerHeight tower height in tiles
     * @param terrain     terrain reference for tile size
     * @return true if no overlap with existing towers
     */
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

                    // Check if this tile overlaps any existing tower
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

    /**
     * Finds the terrain entity in the world.
     *
     * @return TerrainComponent if found, null otherwise
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
     * Safely retrieves a copy of all active entities.
     *
     * @return list of entities or null on failure
     */
    private Array<Entity> safeEntities() {
        try {
            return ServiceLocator.getEntityService().getEntitiesCopy();
        } catch (Exception ex) {
            System.out.println("!!! getEntitiesCopy failed: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Finds the active world camera (OrthographicCamera).
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
     * Cancels the current placement.
     * <p>
     * Disposes of the ghost tower and resets placement state.
     * </p>
     */
    public void cancelPlacement() {
        if (ghostTower != null) {
            ghostTower.dispose(); // or remove from EntityService if needed
            ghostTower = null;
        }
        placementActive = false;
        needRelease = false;
        System.out.println(">>> placement OFF");
    }

    /**
     * @return true if placement is currently active
     */
    public boolean isPlacementActive() { return placementActive; }

    /**
     * @return the type of tower pending placement
     */
    public String getPendingType() { return pendingType; }
}
