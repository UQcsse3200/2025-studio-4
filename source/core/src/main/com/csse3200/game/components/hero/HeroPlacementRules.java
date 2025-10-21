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
// ✅ 修正导入路径（复数）
import com.csse3200.game.components.towers.TowerComponent;

import java.util.Map;

final class HeroPlacementRules {
    private HeroPlacementRules() {}

    /** 将屏幕坐标转换成瓦片坐标（做了边界检查） */
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
     * 判断这个格子是否被阻塞：
     * 1. 路径/障碍/水域（invalidTiles）
     * 2. 该格子上已有防御塔
     */
    static boolean isBlockedCell(int x, int y, IMapEditor mapEditor, ITerrainComponent terrain) {
        if (mapEditor == null || terrain == null) return false;

        // === 1) 路径/障碍/水域 ===
        Map<String, GridPoint2> invalidTiles = mapEditor.getInvalidTiles();
        if (invalidTiles != null && invalidTiles.containsKey(x + "," + y)) {
            return true;
        }

        // === 2) 已有防御塔占用 ===
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
                    // 建议换 logger，这里先保留输出方便你本地调试
                    System.out.println("⚠️ Hero placement blocked: tower already at (" + x + "," + y + ")");
                    return true;
                }
            }
        }

        return false;
    }
}



