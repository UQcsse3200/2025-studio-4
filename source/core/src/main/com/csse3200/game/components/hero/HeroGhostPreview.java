package com.csse3200.game.components.hero;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.HeroFactory;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;

/**
 * HeroGhostPreview:
 * Handles the temporary "ghost" entity used to preview hero placement on the map.
 * Provides methods to spawn, remove, and detect clicks on the preview.
 */
final class HeroGhostPreview {
    private final TerrainComponent terrain;
    private final float ghostAlpha;
    private GridPoint2 cell = null;
    private Entity ghost = null;

    HeroGhostPreview(TerrainComponent terrain, float ghostAlpha) {
        this.terrain = terrain;
        this.ghostAlpha = ghostAlpha;
    }

    /** @return true if a ghost entity currently exists */
    boolean hasGhost() { return ghost != null; }

    /** @return the grid cell where the ghost is currently placed */
    GridPoint2 getCell() { return cell; }

    /**
     * Spawn the ghost preview entity at the given grid cell.
     * If a ghost already exists, it will be removed first.
     *
     * @param gridCell the target grid cell
     */
    void spawnAt(GridPoint2 gridCell) {
        if (ghost != null) remove();
        ghost = HeroFactory.createHeroGhost(ghostAlpha);
        ServiceLocator.getEntityService().register(ghost);
        placeEntityAtCell(ghost, gridCell);
        cell = new GridPoint2(gridCell);
    }

    /**
     * Remove the current ghost preview entity if it exists.
     */
    void remove() {
        if (ghost != null) {
            ghost.dispose();
            ghost = null;
        }
        cell = null;
    }

    /**
     * Check whether a screen click (in screen coordinates) hits the current
     * preview cell (assumed to be size 1x1 in grid space).
     *
     * @param sx screen X coordinate
     * @param sy screen Y coordinate
     * @return true if the click overlaps the ghost's grid cell
     */
    boolean hitByScreen(int sx, int sy) {
        if (ghost == null || cell == null) return false;
        Renderer r = Renderer.getCurrentRenderer();
        if (r == null || r.getCamera() == null) return false;

        Vector3 world = new Vector3(sx, sy, 0f);
        Camera cam = r.getCamera().getCamera();
        cam.unproject(world);

        float tile = terrain.getTileSize();
        float x0 = cell.x * tile, y0 = cell.y * tile;
        float x1 = x0 + tile,    y1 = y0 + tile;
        return world.x >= x0 && world.x <= x1 && world.y >= y0 && world.y <= y1;
    }

    /**
     * Place an entity at the given grid cell, scaling it to match the tile size.
     *
     * @param e    the entity to place
     * @param grid the grid cell to position the entity
     */
    private void placeEntityAtCell(Entity e, GridPoint2 grid) {
        float tile = terrain.getTileSize();
        e.setScale(tile, tile);
        e.setPosition(grid.x * tile, grid.y * tile);
    }
}
