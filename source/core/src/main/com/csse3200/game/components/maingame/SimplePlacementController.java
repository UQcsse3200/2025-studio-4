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
 * Simple controller to place towers snapped to tile corners, supporting multi-tile towers.
 * Supports drag placement and exposes pendingType for grey radius circle.
 */
public class SimplePlacementController extends Component {
    private boolean placementActive = false;
    private boolean needRelease = false;
    private String pendingType = "base";
    private OrthographicCamera camera;
    private final float minSpacing = 1.0f; // fallback if no terrain

    private Entity draggingTower = null;

    @Override
    public void create() {
        entity.getEvents().addListener("startPlacementBase", this::armBase);
        entity.getEvents().addListener("startPlacementSun", this::armSun);
        entity.getEvents().addListener("startPlacementArcher", this::armArcher);
        System.out.println(">>> SimplePlacementController ready; minSpacing=" + minSpacing);
    }

    private void armBase() {
        startPlacement("base");
    }

    private void armSun() {
        startPlacement("sun");
    }

    private void armArcher() {
        startPlacement("archer");
    }

    private void startPlacement(String type) {
        pendingType = type;
        placementActive = true;
        needRelease = true;

        if ("sun".equalsIgnoreCase(type)) {
            draggingTower = TowerFactory.createSunTower();
        } else if ("archer".equalsIgnoreCase(type)) {
            draggingTower = TowerFactory.createArcherTower();
        } else {
            draggingTower = TowerFactory.createBaseTower();
        }

        ServiceLocator.getEntityService().register(draggingTower);

        System.out.println(">>> placement ON (" + type + ")");
    }

    @Override
    public void update() {
        if (camera == null) findWorldCamera();
        if (!placementActive || camera == null || draggingTower == null) return;

        if (needRelease) {
            if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) needRelease = false;
            return;
        }

        Vector3 mousePos3D = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mousePos3D);
        Vector2 mouseWorld = new Vector2(mousePos3D.x, mousePos3D.y);

        TerrainComponent terrain = findTerrain();
        Vector2 snapPos = mouseWorld;
        int towerWidth = 1;
        int towerHeight = 1;

        if (terrain != null) {
            float tileSize = terrain.getTileSize();
            GridPoint2 tile = new GridPoint2(
                    (int) (mouseWorld.x / tileSize),
                    (int) (mouseWorld.y / tileSize)
            );

            GridPoint2 mapBounds = terrain.getMapBounds(0);
            if (tile.x < 0 || tile.y < 0 || tile.x >= mapBounds.x || tile.y >= mapBounds.y - 1) {
                snapPos = mouseWorld;
            } else {
                snapPos = terrain.tileToWorldPosition(tile.x, tile.y);
            }
        }

        draggingTower.setPosition(snapPos);

        // The grey radius circle should be drawn based on draggingTower's position and pendingType.
        // (Assumed to be handled in rendering code elsewhere, using getPendingType())

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!isPositionFree(snapPos, minSpacing, towerWidth, towerHeight, terrain)) {
                System.out.println(">>> blocked: cannot place " + pendingType + " at " + snapPos);
                draggingTower = null;
                placementActive = false;
                return;
            }

            System.out.println(">>> placed " + pendingType + " at " + snapPos);
            draggingTower = null;
            placementActive = false;
        }
    }

    private boolean isPositionFree(Vector2 candidate, float spacing, int towerWidth, int towerHeight, TerrainComponent terrain) {
        Array<Entity> all = safeEntities();
        if (all == null || candidate == null || !Float.isFinite(spacing)) return true;

        float spacing2 = spacing * spacing;
        float tileSize = terrain != null ? terrain.getTileSize() : 1.0f;

        for (int tx = 0; tx < towerWidth; tx++) {
            for (int ty = 0; ty < towerHeight; ty++) {
                Vector2 tilePos = new Vector2(candidate.x + tx * tileSize, candidate.y + ty * tileSize);
                for (Entity e : all) {
                    if (e == null || e == entity) continue;
                    TowerComponent tower = e.getComponent(TowerComponent.class);
                    if (tower == null) continue;

                    Vector2 pos = e.getPosition();
                    if (pos == null || !Float.isFinite(pos.x) || !Float.isFinite(pos.y)) continue;

                    float dx = pos.x - tilePos.x;
                    float dy = pos.y - tilePos.y;
                    if (dx * dx + dy * dy < spacing2) return false;
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
                System.out.println(">>> world camera found; vp=" +
                        camera.viewportWidth + "x" + camera.viewportHeight);
                return;
            }
        }
    }

    public boolean isPlacementActive() {
        return placementActive;
    }

    public String getPendingType() {
        return pendingType;
    }
}
