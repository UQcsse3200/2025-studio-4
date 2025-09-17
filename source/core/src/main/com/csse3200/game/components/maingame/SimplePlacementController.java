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

import java.util.ArrayList;
import java.util.List;

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

    public void setMapEditor(MapEditor mapEditor) {
        this.mapEditor = mapEditor;
        refreshInvalidTiles();
    }

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

    public int[][] getFixedPath() { return FIXED_PATH; }

    @Override
    public void create() {
        entity.getEvents().addListener("startPlacementBone", this::armBone);
        entity.getEvents().addListener("startPlacementDino", this::armDino);
        entity.getEvents().addListener("startPlacementCavemen", this::armCavemen);
        System.out.println(">>> SimplePlacementController ready; minSpacing=" + minSpacing);
    }

    private void armBone() { startPlacement("bone"); }
    private void armDino() { startPlacement("dino"); }
    private void armCavemen() { startPlacement("cavemen"); }

    private void startPlacement(String type) {
        pendingType = type;
        placementActive = true;
        needRelease = true;
        selectedCurrencyType = CurrencyType.METAL_SCRAP; // default or UI selection

        if ("dino".equalsIgnoreCase(type)) ghostTower = TowerFactory.createDinoTower(selectedCurrencyType);
        else if ("cavemen".equalsIgnoreCase(type)) ghostTower = TowerFactory.createCavemenTower(selectedCurrencyType);
        else ghostTower = TowerFactory.createBoneTower(selectedCurrencyType);

        TowerComponent tc = ghostTower.getComponent(TowerComponent.class);
        if (tc != null) tc.setActive(false);

        ServiceLocator.getEntityService().register(ghostTower);
        System.out.println(">>> placement ON (" + type + ")");
    }

    @Override
    public void update() {
        if (camera == null) findWorldCamera();
        if (!placementActive || camera == null || ghostTower == null) return;
        if (needRelease) { if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) needRelease = false; return; }

        Vector3 mousePos3D = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mousePos3D);
        Vector2 mouseWorld = new Vector2(mousePos3D.x, mousePos3D.y);

        TerrainComponent terrain = findTerrain();
        if (terrain == null) return;

        int towerWidth = 2;
        int towerHeight = 2;
        Vector2 snapPos = mouseWorld;
        boolean inBounds = true;
        GridPoint2 tile = new GridPoint2((int)(mouseWorld.x / terrain.getTileSize()), (int)(mouseWorld.y / terrain.getTileSize()));

        GridPoint2 mapBounds = terrain.getMapBounds(0);
        if (tile.x < 0 || tile.y < 0 || tile.x + towerWidth > mapBounds.x || tile.y + towerHeight > mapBounds.y) inBounds = false;
        else snapPos = terrain.tileToWorldPosition(tile.x, tile.y);

        ghostTower.setPosition(snapPos);

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!inBounds || isTowerOnPath(tile, towerWidth, towerHeight) || !isPositionFree(snapPos, towerWidth, towerHeight, terrain)) return;

            // Check player currency
            Entity player = findPlayerEntity();
            if (player == null) return;
            CurrencyManagerComponent currencyManager = player.getComponent(CurrencyManagerComponent.class);
            Entity newTower;
            if ("dino".equalsIgnoreCase(pendingType)) newTower = TowerFactory.createDinoTower(selectedCurrencyType);
            else if ("cavemen".equalsIgnoreCase(pendingType)) newTower = TowerFactory.createCavemenTower(selectedCurrencyType);
            else newTower = TowerFactory.createBoneTower(selectedCurrencyType);

            TowerCostComponent costComponent = newTower.getComponent(TowerCostComponent.class);
            int cost = costComponent != null ? costComponent.getCostForCurrency(selectedCurrencyType) : 0;
            if (currencyManager == null || !currencyManager.canAffordAndSpendSingleCurrency(selectedCurrencyType, cost)) return;

            if (ghostTower != null) { ghostTower.dispose(); ghostTower = null; }

            newTower.setPosition(snapPos);
            TowerComponent tc = newTower.getComponent(TowerComponent.class);
            if (tc != null && tc.hasHead()) tc.getHeadEntity().setPosition(snapPos.x, snapPos.y - 0.01f);

            ServiceLocator.getEntityService().register(newTower);
            if (tc != null && tc.hasHead()) ServiceLocator.getEntityService().register(tc.getHeadEntity());
            placementActive = false;
        }
    }

    private boolean isTowerOnPath(GridPoint2 tile, int towerWidth, int towerHeight) {
        for (int tx = 0; tx < towerWidth; tx++)
            for (int ty = 0; ty < towerHeight; ty++)
                if (isOnPath(new GridPoint2(tile.x + tx, tile.y + ty))) return true;
        return false;
    }

    private boolean isOnPath(GridPoint2 tile) {
        for (int[] p : FIXED_PATH)
            if (p[0] == tile.x && p[1] == tile.y) return true;
        return false;
    }

    private boolean isPositionFree(Vector2 candidate, int towerWidth, int towerHeight, TerrainComponent terrain) {
        Array<Entity> all = safeEntities();
        if (all == null || candidate == null) return true;
        float tileSize = terrain.getTileSize();

        for (int tx = 0; tx < towerWidth; tx++)
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
        return true;
    }

    private TerrainComponent findTerrain() {
        Array<Entity> all = safeEntities();
        if (all == null) return null;
        for (Entity e : all) { if (e == null) continue; TerrainComponent t = e.getComponent(TerrainComponent.class); if (t != null) return t; }
        return null;
    }

    private Array<Entity> safeEntities() {
        try { return ServiceLocator.getEntityService().getEntitiesCopy(); }
        catch (Exception ex) { return null; }
    }

    private void findWorldCamera() {
        Array<Entity> all = safeEntities();
        if (all == null) return;
        for (Entity e : all) { if (e == null) continue; CameraComponent cc = e.getComponent(CameraComponent.class); if (cc != null && cc.getCamera() instanceof OrthographicCamera) { camera = (OrthographicCamera) cc.getCamera(); return; } }
    }

    private Entity findPlayerEntity() {
        Array<Entity> entities = safeEntities();
        if (entities == null) return null;
        for (Entity e : entities) if (e != null && e.getComponent(CurrencyManagerComponent.class) != null) return e;
        return null;
    }

    public void cancelPlacement() {
        if (ghostTower != null) { ghostTower.dispose(); ghostTower = null; }
        placementActive = false; needRelease = false;
        System.out.println(">>> placement OFF");
    }

    public boolean isPlacementActive() { return placementActive; }
    public String getPendingType() { return pendingType; }
    public void setSelectedCurrencyType(CurrencyType currencyType) { this.selectedCurrencyType = currencyType; }
}
