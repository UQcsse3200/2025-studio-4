package com.csse3200.game.components.maingame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.TowerComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Highlights the map tiles with semi-transparent colors.
 * Tiles with towers are red, empty tiles are green.
 * Every tile has a constant black border.
 * Multi-tile towers occupy multiple tiles.
 */
public class MapHighlighter extends UIComponent {
    private final TerrainComponent terrain;
    private final ShapeRenderer shapeRenderer;
    private final SimplePlacementController placementController; // Add reference

    // Update constructor to accept placementController
    public MapHighlighter(TerrainComponent terrain, SimplePlacementController placementController) {
        this.terrain = terrain;
        this.shapeRenderer = new ShapeRenderer();
        this.placementController = placementController;
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        // Only show grid if placement mode is active
        if (placementController == null || !placementController.isPlacementActive()) {
            return;
        }

        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        // Enable blending for transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        GridPoint2 mapBounds = terrain.getMapBounds(0);
        float tileSize = terrain.getTileSize();
        Array<Entity> entities = ServiceLocator.getEntityService().getEntitiesCopy();

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

        // Draw constant black borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        for (int x = 0; x < mapBounds.x; x++) {
            for (int y = 0; y < mapBounds.y; y++) {
                Vector2 worldPos = terrain.tileToWorldPosition(x, y);
                shapeRenderer.rect(worldPos.x, worldPos.y, tileSize, tileSize);
            }
        }
        shapeRenderer.end();

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
