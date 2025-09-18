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
 * A grid overlay that visually highlights map tiles during tower placement and
 * shows tower attack ranges when a tower is selected.
 *
 * Features:
 * - Highlights every tile: green if free, red if occupied.
 * - Draws black borders around every tile.
 * - Shows a semi-transparent preview of the pending tower’s footprint.
 * - Allows players to click towers and see their attack range visualised as a circle.
 */
public class MapHighlighter extends UIComponent {
    private final TerrainComponent terrain;
    private final ShapeRenderer shapeRenderer;
    private final SimplePlacementController placementController;
    private final TowerFactory towerFactory;
    private TowerUpgradeMenu towerUpgradeMenu;

    private Entity selectedTower = null; // currently selected tower

    /**
     * Creates a new MapHighlighter.
     *
     * @param terrain             The terrain component used to get tile size and bounds.
     * @param placementController Controller for placement state (used to check pending tower).
     * @param towerFactory        Factory used to query tower types for size info.
     */
    public MapHighlighter(TerrainComponent terrain,
                          SimplePlacementController placementController,
                          TowerFactory towerFactory) {
        this.terrain = terrain;
        this.shapeRenderer = new ShapeRenderer();
        this.placementController = placementController;
        this.towerFactory = towerFactory;
    }

    /**
     * Updates game logic each frame:
     * - Detects tower selection by clicking on them.
     * - Deselects if clicking empty space.
     */
    @Override
    public void update() {
        if (Gdx.input.justTouched()) {
            // if the ui is touched, ignore for tower selection
            if (towerUpgradeMenu != null && towerUpgradeMenu.isTouched(Gdx.input.getX(), Gdx.input.getY())) {
                return;
            }

            Vector2 clickWorld = getWorldClickPosition();
            boolean towerFound = false;
            if (clickWorld != null) {
                Array<Entity> entities = ServiceLocator.getEntityService().getEntitiesCopy();
                for (Entity e : entities) {
                    TowerComponent tower = e.getComponent(TowerComponent.class);
                    if (tower == null) continue;

                    Vector2 pos = e.getPosition();
                    float tileSize = terrain.getTileSize();

                    // Check if click lands inside this tower's footprint
                    if (clickWorld.x >= pos.x && clickWorld.x <= pos.x + tower.getWidth() * tileSize &&
                            clickWorld.y >= pos.y && clickWorld.y <= pos.y + tower.getHeight() * tileSize) {
                        selectedTower = e;

                        if (towerUpgradeMenu != null) {
                            towerUpgradeMenu.setSelectedTower(selectedTower);
                        }
                        towerFound = true;
                        break;
                    }
                }
            }

            //if no tower was found at click location, deselect the current one
            if (!towerFound) {
                selectedTower = null;
                if  (towerUpgradeMenu != null) {
                    towerUpgradeMenu.setSelectedTower(null);
                }
            }
        }
    }


    /**
     * Converts the most recent mouse click position into world coordinates.
     *
     * @return World-space position of the click, or null if no camera is found.
     */
    private Vector2 getWorldClickPosition() {
        com.badlogic.gdx.graphics.Camera camera = null;
        Array<Entity> entities = ServiceLocator.getEntityService().getEntitiesCopy();
        for (Entity e : entities) {
            com.csse3200.game.components.CameraComponent cc =
                    e.getComponent(com.csse3200.game.components.CameraComponent.class);
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

    /**
     * Renders overlays:
     * - Green/red semi-transparent tiles for placement validity.
     * - Black grid lines.
     * - Preview outline of the pending tower footprint.
     * - Range circle for the selected tower.
     */
    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        boolean showGrid = placementController != null && placementController.isPlacementActive();

        batch.end();
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (showGrid) {
            GridPoint2 mapBounds = terrain.getMapBounds(0);
            float tileSize = terrain.getTileSize();
            Array<Entity> entities = ServiceLocator.getEntityService().getEntitiesCopy();

            // Determine footprint of pending tower type
            Entity pendingEntity;
            String pendingType = placementController.getPendingType();
            if ("Dino".equalsIgnoreCase(pendingType)) {
                pendingEntity = towerFactory.createDinoTower();
            } else if ("Cavemen".equalsIgnoreCase(pendingType)) {
                pendingEntity = towerFactory.createCavemenTower();
            } else {
                pendingEntity = towerFactory.createBoneTower();
            }
            TowerComponent placingTower = pendingEntity.getComponent(TowerComponent.class);
            int towerWidth = placingTower != null ? placingTower.getWidth() : 2;
            int towerHeight = placingTower != null ? placingTower.getHeight() : 2;

            // Fill tiles with green (free) or red (blocked)
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

            // Draw preview of tower footprint at mouse position
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.BLACK);

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

            // Always draw grid borders
            for (int x = 0; x < mapBounds.x; x++) {
                for (int y = 0; y < mapBounds.y; y++) {
                    Vector2 worldPos = terrain.tileToWorldPosition(x, y);
                    shapeRenderer.rect(worldPos.x, worldPos.y, tileSize, tileSize);
                }
            }
            shapeRenderer.end();
        }

        // Draw tower range if one is selected
        if (selectedTower != null) {
            TowerComponent tower = selectedTower.getComponent(TowerComponent.class);
            if (tower != null) {
                TowerStatsComponent stats = selectedTower.getComponent(TowerStatsComponent.class);
                float range = stats != null ? stats.getRange() : 1.0f;
                Vector2 pos = selectedTower.getPosition();
                float centerX = pos.x + terrain.getTileSize() * tower.getWidth() / 2f;
                float centerY = pos.y + terrain.getTileSize() * tower.getHeight() / 2f;

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(new Color(0.5f, 0.5f, 0.5f, 0.3f)); // grey transparent
                shapeRenderer.circle(centerX, centerY, range, 60);
                shapeRenderer.end();
            }
        }

        batch.begin();
    }

    /**
     * Returns whether a given tile is free (no tower occupies it).
     *
     * @param tileX   X coordinate of the tile.
     * @param tileY   Y coordinate of the tile.
     * @param entities All entities in the world.
     * @return true if free, false if occupied by any tower.
     */
    private boolean isTileFree(int tileX, int tileY, Array<Entity> entities) {
        if (entities == null) return true;

        // 🔹 Check if tile is on the fixed path
        if (placementController != null) {
            int[][] path = placementController.getFixedPath();
            if (path != null) {
                for (int[] p : path) {
                    if (p[0] == tileX && p[1] == tileY) {
                        return false; // tile is part of fixed enemy path
                    }
                }
            }
        }

        // 🔹 Check if tile is occupied by any existing tower
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

            if (tileX >= towerTile.x && tileX < towerTile.x + towerWidth &&
                    tileY >= towerTile.y && tileY < towerTile.y + towerHeight) {
                return false; // occupied by a tower
            }
        }

        return true; // free tile
    }

    public void setTowerUpgradeMenu(TowerUpgradeMenu menu) {
        this.towerUpgradeMenu = menu;
    }


    /**
     * Disposes of the shape renderer when no longer needed.
     */
    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
