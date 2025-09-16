package com.csse3200.game.components.hero;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.HeroFactory;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;

final class HeroGhostPreview {
    private final TerrainComponent terrain;
    private final float ghostAlpha;
    private GridPoint2 cell = null;
    private Entity ghost = null;

    HeroGhostPreview(TerrainComponent terrain, float ghostAlpha) {
        this.terrain = terrain;
        this.ghostAlpha = ghostAlpha;
    }

    boolean hasGhost() { return ghost != null; }
    GridPoint2 getCell() { return cell; }

    void spawnAt(GridPoint2 gridCell) {
        if (ghost != null) remove();
        ghost = HeroFactory.createHeroGhost(ghostAlpha);
        ServiceLocator.getEntityService().register(ghost);
        placeEntityAtCell(ghost, gridCell);
        cell = new GridPoint2(gridCell);
    }

    void remove() {
        if (ghost != null) {
            ghost.dispose();
            ghost = null;
        }
        cell = null;
    }

    /** 点击是否命中当前预览格（按 1×1 大小）。 */
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

    private void placeEntityAtCell(Entity e, GridPoint2 grid) {
        float tile = terrain.getTileSize();
        e.setScale(tile, tile);
        e.setPosition(grid.x * tile, grid.y * tile);
    }
}
