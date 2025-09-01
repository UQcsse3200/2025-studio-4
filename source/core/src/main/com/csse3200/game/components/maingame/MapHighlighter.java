package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.TowerComponent;
import com.csse3200.game.components.TowerStatsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.entities.factories.TowerFactory;

/**
 * Highlights the map tiles with semi-transparent colors.
 * Tiles with towers are red, empty tiles are green.
 * Every tile has a constant black border.
 * Multi-tile towers occupy multiple tiles.
 */
public class MapHighlighter extends UIComponent {
    private final TerrainComponent terrain;
    private final ShapeRenderer shapeRenderer;
    private final SimplePlacementController placementController;
    private final TowerFactory towerFactory; // Add reference
    private Entity selectedTower = null; // Track selected tower

    // Update constructor to accept placementController
    public MapHighlighter(TerrainComponent terrain, SimplePlacementController placementController, TowerFactory towerFactory) {
        this.terrain = terrain;
        this.shapeRenderer = new ShapeRenderer();
        this.placementController = placementController;
        this.towerFactory = towerFactory;
    }

    @Override
    public void update() {
        // Detect click on towers
        if (Gdx.input.justTouched()) {
            Vector2 clickWorld = getWorldClickPosition();
            if (clickWorld != null) {
                Array<Entity> entities = ServiceLocator.getEntityService().getEntitiesCopy();
                for (Entity e : entities) {
                    TowerComponent tower = e.getComponent(TowerComponent.class);
                    if (tower == null) continue;
                    Vector2 pos = e.getPosition();
                    float tileSize = terrain.getTileSize();
                    // Check if click is within tower's bounds
                    if (clickWorld.x >= pos.x && clickWorld.x <= pos.x + tower.getWidth() * tileSize &&
                            clickWorld.y >= pos.y && clickWorld.y <= pos.y + tower.getHeight() * tileSize) {
                        selectedTower = e;
                        return;
                    }
                }
                // If no tower clicked, deselect
                selectedTower = null;
            }
        }
    }

    private Vector2 getWorldClickPosition() {
        // Get camera from ServiceLocator
        com.badlogic.gdx.graphics.Camera camera = null;
        Array<Entity> entities = ServiceLocator.getEntityService().getEntitiesCopy();
        for (Entity e : entities) {
            com.csse3200.game.components.CameraComponent cc = e.getComponent(com.csse3200.game.components.CameraComponent.class);
            if (cc != null) {
                camera = cc.getCamera();
                break;
            }
        }
        if (camera == null) return null;
        Vector3 screenPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(screenPos);
        return new Vector2(screenPos.x, screenPos.y);
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        boolean showGrid = placementController != null && placementController.isPlacementActive();

        batch.end();
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        if (showGrid) {
            GridPoint2 mapBounds = terrain.getMapBounds(0);
            float tileSize = terrain.getTileSize();
            Array<Entity> entities = ServiceLocator.getEntityService().getEntitiesCopy();

            // Use TowerFactory to get the pending tower's component
            TowerComponent placingTower = null;
            String pendingType = placementController.getPendingType();
            Entity pendingEntity;
            if ("sun".equalsIgnoreCase(pendingType)) {
                pendingEntity = towerFactory.createSunTower();
            } else if ("archer".equalsIgnoreCase(pendingType)) {
                pendingEntity = towerFactory.createArcherTower();
            } else {
                pendingEntity = towerFactory.createBaseTower();
            }
            placingTower = pendingEntity.getComponent(TowerComponent.class);
            int towerWidth = placingTower != null ? placingTower.getWidth() : 2;
            int towerHeight = placingTower != null ? placingTower.getHeight() : 2;

            // Draw semi-transparent tile fills
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (int x = 0; x < mapBounds.x; x++) {
                for (int y = 0; y < mapBounds.y; y++) {
                    Vector2 worldPos = terrain.tileToWorldPosition(x, y);
                    boolean valid = isTileFree(x, y, entities);

                    Color fill = valid
                            ? new Color(0, 1, 0, 0.2f)  // green transparent
                            : new Color(1, 0, 0, 0.2f); // red transparent
                    shapeRenderer.setColor(fill);
                    shapeRenderer.rect(worldPos.x, worldPos.y, tileSize, tileSize);
                }
            }
            shapeRenderer.end();

            // Draw black borders over the area the tower will occupy
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.BLACK);

            // Example: highlight all tiles that would be covered by the tower at mouse position
            Vector2 mouseWorld = getWorldClickPosition();
            if (mouseWorld != null) {
                int baseTileX = (int) (mouseWorld.x / tileSize);
                int baseTileY = (int) (mouseWorld.y / tileSize);
                for (int dx = 0; dx < towerWidth; dx++) {
                    for (int dy = 0; dy < towerHeight; dy++) {
                        int tx = baseTileX + dx;
                        int ty = baseTileY + dy;
                        if (tx >= 0 && ty >= 0 && tx < mapBounds.x && ty < mapBounds.y) {
                            Vector2 tilePos = terrain.tileToWorldPosition(tx, ty);
                            shapeRenderer.rect(tilePos.x, tilePos.y, tileSize, tileSize);
                        }
                    }
                }
            }

            // Draw constant black borders for all tiles
            for (int x = 0; x < mapBounds.x; x++) {
                for (int y = 0; y < mapBounds.y; y++) {
                    Vector2 worldPos = terrain.tileToWorldPosition(x, y);
                    shapeRenderer.rect(worldPos.x, worldPos.y, tileSize, tileSize);
                }
            }
            shapeRenderer.end();
        }

        // Draw tower range if a tower is selected (always, not just in placement mode)
        if (selectedTower != null) {
            TowerComponent tower = selectedTower.getComponent(TowerComponent.class);
            if (tower != null) {
                TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
                float range = stats != null ? stats.getRange() : 1.0f;
                Vector2 pos = selectedTower.getPosition();
                float centerX = pos.x + terrain.getTileSize() * tower.getWidth() / 2f;
                float centerY = pos.y + terrain.getTileSize() * tower.getHeight() / 2f;

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(new Color(0.5f, 0.5f, 0.5f, 0.3f)); // grey, semi-transparent
                shapeRenderer.circle(centerX, centerY, range, 60); // Use 60 segments for smoothness
                shapeRenderer.end();
            }
        }

        batch.begin();
    }

    /**
     * Returns false if the tile is occupied by any tower (including multi-tile towers)
     */
    private boolean isTileFree(int tileX, int tileY, Array<Entity> entities) {
        if (entities == null) return true;

        for (Entity e : entities) {
            if (e == null) continue;
            TowerComponent tower = e.getComponent(TowerComponent.class);
            if (tower == null) continue;

            Vector2 pos = e.getPosition();
            if (pos == null) continue;

            int towerWidth = tower.getWidth();
            int towerHeight = tower.getHeight();

            GridPoint2 towerTile = new GridPoint2(
                    (int) (pos.x / terrain.getTileSize()),
                    (int) (pos.y / terrain.getTileSize())
            );

            // Check if this tile is inside the tower's area
            if (tileX >= towerTile.x && tileX < towerTile.x + towerWidth &&
                    tileY >= towerTile.y && tileY < towerTile.y + towerHeight) {
                return false; // tile occupied
            }
        }
        return true; // free
    }


    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
