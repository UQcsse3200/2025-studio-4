package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.terrain.ITerrainComponent;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.towers.TowerComponent;
import com.csse3200.game.components.towers.TowerStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.components.enemy.clickable;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.TowerFactory;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * A grid overlay that visually highlights map tiles during tower placement and
 * shows tower attack ranges when a tower is selected.
 */
public class MapHighlighter extends UIComponent {
    private final ITerrainComponent terrain;
    private final ShapeRenderer shapeRenderer;
    private final SimplePlacementController placementController;
    private final TowerFactory towerFactory;
    private TowerUpgradeMenu towerUpgradeMenu;

    private Entity selectedTower = null; // currently selected tower

    /**
     * Constructs a MapHighlighter for the given terrain and placement controller.
     *
     * @param terrain the terrain component
     * @param placementController the placement controller
     * @param towerFactory the tower factory
     */
    public MapHighlighter(ITerrainComponent terrain,
                          SimplePlacementController placementController,
                          TowerFactory towerFactory) {
        this.terrain = terrain;
        this.shapeRenderer = new ShapeRenderer();
        this.placementController = placementController;
        this.towerFactory = towerFactory;
    }

    /**
     * Updates the highlighter, handling tower selection on mouse click.
     */
    @Override
    public void update() {
        if (Gdx.input.justTouched()) {
            Vector2 clickWorld = getWorldClickPosition();

            // If clicking on tower upgrade menu itself, ignore
            if (towerUpgradeMenu != null && towerUpgradeMenu.isTouched(Gdx.input.getX(), Gdx.input.getY())) {
                return;
            }

            // If placement is active, don't open upgrade menu
            if (placementController != null && placementController.isPlacementActive()) {
                // Only show placement preview / range if click is on map
                return;
            }

            // Normal tower selection when not placing
            boolean towerFound = false;
            boolean enemyFound = false;
            Entity player = findPlayerEntity();
            if (player == null) return;
            if (clickWorld != null) {
                Array<Entity> entities = ServiceLocator.getEntityService().getEntitiesCopy();
                for (Entity e : entities) {
                    TowerComponent tower = e.getComponent(TowerComponent.class);
                    DeckComponent deck = e.getComponent(DeckComponent.TowerDeckComponent.class);
                    clickable enemy = e.getComponent(clickable.class);
                    if (tower == null) continue;

                    Vector2 pos = e.getPosition();
                    float tileSize = terrain.getTileSize();

                    if (clickWorld.x >= pos.x && clickWorld.x <= pos.x + tower.getWidth() * tileSize &&
                            clickWorld.y >= pos.y && clickWorld.y <= pos.y + tower.getHeight() * tileSize) {

                        selectedTower = e;
                        player.getEvents().trigger("displayDeck", deck);
                        if (towerUpgradeMenu != null) {
                            TowerComponent towerComp = selectedTower.getComponent(TowerComponent.class);
                            String towerType = towerComp != null ? towerComp.getType() : "";
                            towerUpgradeMenu.setSelectedTower(selectedTower, towerType);
                        }
                        towerFound = true;
                        break;
                    }

                    if (enemy == null) {
                        continue;
                    }

                    Vector2 enemyPos = enemy.getEntity().getPosition();
                    float clickRadius = enemy.getClickRadius();

                    if ((Math.abs(clickWorld.x - (enemyPos.x + clickRadius/2)) < clickRadius &&
                            Math.abs(clickWorld.y - (enemyPos.y + clickRadius)) < clickRadius)) {
                        enemyFound = true;
                    }
                }
                if (!towerFound && !enemyFound) {
                    player.getEvents().trigger("clearDeck");
                }
            }

            if (!towerFound) {
                selectedTower = null;
                if (towerUpgradeMenu != null) {
                    towerUpgradeMenu.setSelectedTower(null, "");
                }
            }
        }
    }



    /**
     * Gets the world position of the last mouse click.
     *
     * @return the world position as a Vector2, or null if camera not found
     */
    private Vector2 getWorldClickPosition() {
        Camera camera = null;
        Array<Entity> entities = ServiceLocator.getEntityService().getEntitiesCopy();
        for (Entity e : entities) {
            CameraComponent cc = e.getComponent(CameraComponent.class);
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
     * Draws the grid overlay and tower range highlights.
     *
     * @param batch the sprite batch
     */
    @Override
    public void draw(SpriteBatch batch) {
        boolean showGrid = placementController != null && placementController.isPlacementActive();

        batch.end();
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (showGrid) {
            drawGridAndPreview();
        }

        drawSelectedTowerRange();
        batch.begin();
    }

    /**
     * Draws the grid and preview for tower placement.
     */
    private void drawGridAndPreview() {
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
        } else if ("Pterodactyl".equalsIgnoreCase(pendingType)) {
            pendingEntity = towerFactory.createPterodactylTower();
        } else if ("SuperCavemen".equalsIgnoreCase(pendingType)) {
            pendingEntity = towerFactory.createSuperCavemenTower();
        } else if ("Totem".equalsIgnoreCase(pendingType)) {
            pendingEntity = towerFactory.createTotemTower();
        } else if ("Bank".equalsIgnoreCase(pendingType)) {
            pendingEntity = towerFactory.createBankTower();
        } else if ("Raft".equalsIgnoreCase(pendingType)) {
            pendingEntity = towerFactory.createRaftTower();
        } else if ("Frozenmamoothskull".equalsIgnoreCase(pendingType)) {
            pendingEntity = towerFactory.createFrozenmamoothskullTower();
        } else if ("Bouldercatapult".equalsIgnoreCase(pendingType)) {
            pendingEntity = towerFactory.createBouldercatapultTower();
        } else if ("Villageshaman".equalsIgnoreCase(pendingType)) {
            pendingEntity = towerFactory.createVillageshamanTower();
        } else {
            pendingEntity = towerFactory.createBoneTower();
        }
        TowerComponent placingTower = pendingEntity.getComponent(TowerComponent.class);
        int towerWidth = placingTower != null ? placingTower.getWidth() : 2;
        int towerHeight = placingTower != null ? placingTower.getHeight() : 2;

        boolean isRaft = "Raft".equalsIgnoreCase(pendingType);
        int[][] waterTiles = null;
        if (isRaft && placementController != null) {
            waterTiles = placementController.getWaterTiles();
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int x = 0; x < mapBounds.x; x++) {
            for (int y = 0; y < mapBounds.y; y++) {
                Vector2 worldPos = terrain.tileToWorldPosition(x, y);
                boolean valid;
                if (isRaft) {
                    valid = isTileWater(x, y, waterTiles, towerWidth, towerHeight, mapBounds);
                } else {
                    // For all other towers: valid if not on invalid tiles and not overlapping towers
                    valid = isTileFree(x, y, entities);
                }
                shapeRenderer.setColor(valid ? new Color(0, 1, 0, 0.2f) : new Color(1, 0, 0, 0.2f));
                shapeRenderer.rect(worldPos.x, worldPos.y, tileSize, tileSize);
            }
        }
        shapeRenderer.end();

        // Draw preview of tower footprint at mouse position
        Vector2 mouseWorld = getWorldClickPosition();
        if (mouseWorld != null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.BLACK);

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
            shapeRenderer.end();
        }

        // Always draw grid borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        for (int x = 0; x < mapBounds.x; x++) {
            for (int y = 0; y < mapBounds.y; y++) {
                Vector2 worldPos = terrain.tileToWorldPosition(x, y);
                shapeRenderer.rect(worldPos.x, worldPos.y, tileSize, tileSize);
            }
        }
        shapeRenderer.end();
    }

    /**
     * Checks if a tile is a valid raft placement (all footprint tiles must be water).
     */
    private boolean isTileWater(int tileX, int tileY, int[][] waterTiles, int towerWidth, int towerHeight, GridPoint2 mapBounds) {
        if (waterTiles == null) return false;
        for (int dx = 0; dx < towerWidth; dx++) {
            for (int dy = 0; dy < towerHeight; dy++) {
                int tx = tileX + dx;
                int ty = tileY + dy;
                if (tx < 0 || ty < 0 || tx >= mapBounds.x || ty >= mapBounds.y) return false;
                boolean found = false;
                for (int[] p : waterTiles) {
                    if (p[0] == tx && p[1] == ty) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
        }
        return true;
    }

    /**
     * Draws the attack range of the currently selected tower.
     */
    private void drawSelectedTowerRange() {
        if (selectedTower == null) return;

        TowerComponent tower = selectedTower.getComponent(TowerComponent.class);
        if (tower == null) return;

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

    /**
     * Checks if a tile is free for tower placement.
     *
     * @param tileX the x coordinate of the tile
     * @param tileY the y coordinate of the tile
     * @param entities the list of entities
     * @return true if the tile is free, false otherwise
     */
    private boolean isTileFree(int tileX, int tileY, Array<Entity> entities) {
        if (entities == null) return true;

        // Check invalid tiles (path, barrier, snowtree, snow, etc)
        if (placementController != null) {
            int[][] invalid = placementController.getFixedPath();
            if (invalid != null) {
                for (int[] p : invalid) {
                    if (p[0] == tileX && p[1] == tileY) return false;
                }
            }
        }

        // Check towers
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
                return false;
            }
        }

        return true;
    }

    public void setTowerUpgradeMenu(TowerUpgradeMenu menu) {
        this.towerUpgradeMenu = menu;
    }

    /**
     * Gets a safe copy of all entities.
     *
     * @return array of entities, or null if unavailable
     */
    private Array<Entity> safeEntities()
    {
        try
        {
            return ServiceLocator.getEntityService().getEntitiesCopy();
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * Finds the player entity (with a currency manager).
     *
     * @return the player entity, or null if not found
     */
    private Entity findPlayerEntity()
    {
        Array<Entity> entities = safeEntities();
        if (entities == null) return null;
        for (Entity e : entities)
        {
            if (e != null && e.getComponent(CurrencyManagerComponent.class) != null) return e;
        }
        return null;
    }


    /**
     * Disposes of the shape renderer when no longer needed.
     */
    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
