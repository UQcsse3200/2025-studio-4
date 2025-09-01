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
 * Controller for tower placement with a ghost preview following the mouse.
 * Snaps towers to map tiles, enforces map boundaries, and prevents overlap.
 */
public class SimplePlacementController extends Component {
    private boolean placementActive = false;
    private boolean needRelease = false;
    private String pendingType = "base";
    private OrthographicCamera camera;

    private Entity ghostTower = null;

    @Override
    public void create() {
        entity.getEvents().addListener("startPlacementBase", this::armBase);
        entity.getEvents().addListener("startPlacementSun", this::armSun);
        entity.getEvents().addListener("startPlacementArcher", this::armArcher);
        System.out.println(">>> SimplePlacementController ready");
    }

    private void armBase() { startPlacement("base"); }
    private void armSun() { startPlacement("sun"); }
    private void armArcher() { startPlacement("archer"); }

    private void startPlacement(String type) {
        pendingType = type;
        placementActive = true;
        needRelease = true;

        if ("sun".equalsIgnoreCase(type)) {
            ghostTower = TowerFactory.createSunTower();
        } else if ("archer".equalsIgnoreCase(type)) {
            ghostTower = TowerFactory.createArcherTower();
        } else {
            ghostTower = TowerFactory.createBaseTower();
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
        Vector2 snapPos = new Vector2(mousePos3D.x, mousePos3D.y);

        TerrainComponent terrain = findTerrain();
        int towerWidth = ghostTower.getComponent(TowerComponent.class).getWidth();
        int towerHeight = ghostTower.getComponent(TowerComponent.class).getHeight();

        if (terrain != null) {
            float tileSize = terrain.getTileSize();
            GridPoint2 tile = new GridPoint2(
                    (int) (snapPos.x / tileSize),
                    (int) (snapPos.y / tileSize)
            );

            GridPoint2 mapBounds = terrain.getMapBounds(0);

            // Ensure the tower stays within map bounds
            if (tile.x < 0 || tile.y < 0
                    || tile.x + towerWidth > mapBounds.x
                    || tile.y + towerHeight > mapBounds.y) {
                return;
            }

            snapPos = terrain.tileToWorldPosition(tile.x, tile.y);
        } else {
            return;
        }

        ghostTower.setPosition(snapPos);

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!isPositionFree(snapPos, towerWidth, towerHeight, terrain)) {
                System.out.println(">>> blocked: cannot place " + pendingType + " at " + snapPos);
                return;
            }

            Entity newTower;
            if ("sun".equalsIgnoreCase(pendingType)) {
                newTower = TowerFactory.createSunTower();
            } else if ("archer".equalsIgnoreCase(pendingType)) {
                newTower = TowerFactory.createArcherTower();
            } else {
                newTower = TowerFactory.createBaseTower();
            }

            newTower.setPosition(snapPos);
            ServiceLocator.getEntityService().register(newTower);
            System.out.println(">>> placed " + pendingType + " at " + snapPos);

            ghostTower = null;
            placementActive = false;
        }
    }

    /**
     * Checks if a tower can be placed at the given position.
     * Towers are allowed to be adjacent (no spacing required).
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

    public boolean isPlacementActive() { return placementActive; }
    public String getPendingType() { return pendingType; }
}
