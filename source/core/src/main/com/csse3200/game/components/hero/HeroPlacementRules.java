package com.csse3200.game.components.hero;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.areas.MapEditor;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.rendering.Renderer;

import java.util.Map;

final class HeroPlacementRules {
    private HeroPlacementRules() {}

    static GridPoint2 screenToGridNoClamp(int sx, int sy, TerrainComponent terrain) {
        Renderer r = Renderer.getCurrentRenderer();
        if (r == null || r.getCamera() == null) return null;
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

    static boolean isBlockedCell(int x, int y, MapEditor mapEditor) {
        if (mapEditor == null) return false;
        Map<String, GridPoint2> invalid = mapEditor.getInvalidTiles();
        return invalid != null && invalid.containsKey(x + "," + y);
    }
}
