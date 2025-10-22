package com.csse3200.game.components.hero;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.IMapEditor;
import com.csse3200.game.areas.terrain.ITerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;
// ✅ Fixed import path (plural)
import com.csse3200.game.components.towers.TowerComponent;

import java.util.Map;

final class HeroPlacementRules {
    private HeroPlacementRules() {}

    /** Convert screen coordinates to tile coordinates (with bounds checking). */
    static GridPoint2 screenToGridNoClamp(int sx, int sy, ITerrainComponent terrain) {
        Renderer r = Renderer.getCurrentRenderer();
        if (r == null || r.getCamera() == null || terrain == null) return null;

        Camera cam = r.getCamera().getCamera();
        Vector3 world = new Vector3(sx, sy, 0f);
        cam.unproject(world);

        float tile = terrain.getTileSize();
        int gx = (int) Math.floor(world.x / tile);
        int gy = (int) Math.floor(world.y / tile);

        GridPoint2 bounds = terrain.getMapBounds(0);
        if (gx < 0 || gy < 0 || gx >= bounds.x || gy >= bounds.y) return null;
        return new GridPoint2(gx, gy);
    }

    /**
     * Determine whether a cell is blocked:
     * 1. Path/obstacle/water (invalidTiles)
     * 2. A tower already occupies this cell
     */
    static boolean isBlockedCell(int x, int y, IMapEditor mapEditor, ITerrainComponent terrain) {
        if (mapEditor == null || terrain == null) return false;

        // === 1) Path/obstacle/water ===
        Map<String, GridPoint2> invalidTiles = mapEditor.getInvalidTiles();
        if (invalidTiles != null && invalidTiles.containsKey(x + "," + y)) {
            return true;
        }

        // === 2) Occupied by an existing tower ===
        var entityService = ServiceLocator.getEntityService();
        if (entityService != null) {
            Array<Entity> all = entityService.getEntitiesCopy();
            float tileSize = terrain.getTileSize();

            for (Entity e : all) {
                if (e == null) continue;

                TowerComponent tower = e.getComponent(TowerComponent.class);
                if (tower == null) continue;

                Vector2 pos = e.getPosition();
                if (pos == null) continue;

                int tx = (int) Math.floor(pos.x / tileSize);
                int ty = (int) Math.floor(pos.y / tileSize);

                if (tx == x && ty == y) {
                    // Consider switching to a logger; println kept here for local debugging convenience
                    System.out.println("⚠️ Hero placement blocked: tower already at (" + x + "," + y + ")");
                    return true;
                }
            }
        }

        return false;
    }
}
