package com.csse3200.game.components.hero;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.areas.terrain.ITerrainComponent;
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
// HeroGhostPreview.java（示意：按你已有类名与字段来改）
public class HeroGhostPreview {
    private final ITerrainComponent terrain;
    private final float alpha;
    private Entity ghost; // 当前ghost实体

    public HeroGhostPreview(ITerrainComponent terrain, float alpha) {
        this.terrain = terrain;
        this.alpha = alpha;
    }

    public boolean hasGhost() { return ghost != null; }

    public void createGhost(String heroTexture) {
        remove(); // 先清理旧的
        ghost = HeroFactory.createHeroGhost(heroTexture, alpha);
        ServiceLocator.getEntityService().register(ghost);
        ghost.create(); // 重要：让渲染组件完成内部初始化
    }

    public void setGhostPosition(float worldX, float worldY) {
        if (ghost != null) ghost.setPosition(worldX, worldY);
    }

    public void remove() {
        if (ghost != null) {
            try { ghost.dispose(); } catch (Exception ignored) {}
            ghost = null;
        }
    }
}

