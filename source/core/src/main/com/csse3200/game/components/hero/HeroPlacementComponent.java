// === 新增/修改点：HeroPlacementComponent ===
package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import com.csse3200.game.areas.IMapEditor;
import com.csse3200.game.areas.terrain.ITerrainComponent;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class HeroPlacementComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroPlacementComponent.class);

    private final ITerrainComponent terrain;
    private final IMapEditor mapEditor;
    private final Consumer<GridPoint2> onPlace;

    private final HeroGhostPreview preview;
    private InputAdapter hotkeyAdapter;

    private boolean placementActive = false;

    // 放置上限
    private int maxPlacements = 1;
    private int placedCount = 0;

    // === 新增：由UI选择的英雄类型（用于决定Ghost贴图与放置的“业务类型”） ===
    private String pendingType = "default"; // "engineer" | "samurai" | "default"

    public HeroPlacementComponent(ITerrainComponent terrain, IMapEditor mapEditor, Consumer<GridPoint2> onPlace) {
        super(500);
        this.terrain = terrain;
        this.mapEditor = mapEditor;
        this.onPlace = onPlace;
        this.preview = new HeroGhostPreview(terrain, 0.5f);
    }

    public HeroPlacementComponent(ITerrainComponent terrain, IMapEditor mapEditor,
                                  Consumer<GridPoint2> onPlace, int maxPlacements) {
        this(terrain, mapEditor, onPlace);
        this.maxPlacements = Math.max(1, maxPlacements);
    }

    @Override
    public void create() {
        super.create();
        logger.info("HeroPlacement ready. Press 'S' to place at mouse cell. Right-click to cancel. (cap: {}/{})",
                placedCount, maxPlacements);

        // 可保留 S 键，也可删除。如果不想要键盘入口，删除整个 hotkeyAdapter 即可。
        hotkeyAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.S) {
                    // 用默认类型进入
                    requestPlacement("default");
                    return true;
                }
                return false;
            }
        };

        if (Gdx.input.getInputProcessor() instanceof InputMultiplexer mux) {
            mux.addProcessor(0, hotkeyAdapter);
        } else {
            InputMultiplexer mux = new InputMultiplexer();
            mux.addProcessor(hotkeyAdapter);
            if (Gdx.input.getInputProcessor() != null) {
                mux.addProcessor(Gdx.input.getInputProcessor());
            }
            Gdx.input.setInputProcessor(mux);
        }
    }

    @Override
    public void update() {
        if (!placementActive) return;

        OrthographicCamera camera = null;
        var all = ServiceLocator.getEntityService().getEntitiesCopy();
        if (all != null) {
            for (var e : all) {
                var cc = e.getComponent(com.csse3200.game.components.CameraComponent.class);
                if (cc != null && cc.getCamera() instanceof OrthographicCamera oc) {
                    camera = oc; break;
                }
            }
        }
        if (camera == null) return;

        Vector3 mp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mp);

        float tile = terrain.getTileSize();
        GridPoint2 bounds = terrain.getMapBounds(0);
        int gx = Math.max(0, Math.min((int)(mp.x / tile), bounds.x - 1));
        int gy = Math.max(0, Math.min((int)(mp.y / tile), bounds.y - 1));
        Vector2 snap = terrain.tileToWorldPosition(gx, gy);

        preview.setGhostPosition(snap.x, snap.y);
        entity.getEvents().trigger("placement:hover", new GridPoint2(gx, gy));
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) {
            if (placementActive) {
                notifyUser("已退出放置模式。");
                cancelPlacement();
                return true;
            }
            return false;
        }

        if (!placementActive || button != Input.Buttons.LEFT) return false;

        if (placedCount >= maxPlacements) {
            notifyUser("已达到放置上限（" + maxPlacements + "）。");
            return true;
        }

        GridPoint2 cell = HeroPlacementRules.screenToGridNoClamp(screenX, screenY, terrain);
        if (cell == null) {
            warn("Out of map bounds");
            return true;
        }
        if (HeroPlacementRules.isBlockedCell(cell.x, cell.y, mapEditor, terrain)) {
            warn("Cell is blocked (obstacle/path/restricted area)");
            return true;
        }

        try {
            if (onPlace != null) onPlace.accept(new GridPoint2(cell));
            placedCount++;
            logger.info("Hero placed at ({}, {}) type={} ({}/{})", cell.x, cell.y, pendingType, placedCount, maxPlacements);

            if (placedCount >= maxPlacements) {
                notifyUser("放置完成（" + placedCount + "/" + maxPlacements + "）。已锁定此组件。");
                Gdx.app.postRunnable(() -> {
                    removeHotkeyAdapter();
                    exitPlacementMode();
                });
            } else {
                // 继续放置下一次（如果你希望单次点击后退出，这里可以直接 exitPlacementMode()）
                Gdx.app.postRunnable(() -> {
                    preview.remove();
                    preview.createGhost(resolveTextureByType(pendingType));
                });
            }
        } catch (Exception e) {
            warn("Placement failed due to exception: " + e.getMessage());
        }
        return true;
    }

    // === 新增：供 UI 调用的方法 ===
    public void requestPlacement(String heroType) {
        // 若已在放置模式并且点击的是同一类型 → 视为“切换/取消”
        if (placementActive && heroType != null && heroType.equalsIgnoreCase(pendingType)) {
            cancelPlacement();
            return;
        }

        this.pendingType = heroType == null ? "default" : heroType.toLowerCase();
        enterPlacementModeWithTexture(resolveTextureByType(this.pendingType));
    }

    public void cancelPlacement() {
        if (!placementActive) return;
        exitPlacementMode();
    }

    public boolean isPlacementActive() {
        return placementActive;
    }

    public String getPendingType() {
        return pendingType;
    }

    public void setMaxPlacements(int n) {
        this.maxPlacements = Math.max(1, n);
    }

    // === 贴图解析：根据英雄类型决定 Ghost 贴图 ===
    private String resolveTextureByType(String type) {
        if (type == null) return currentHeroTexture();
        switch (type.toLowerCase()) {
            case "engineer": return "images/engineer/Engineer.png";
            case "samurai":  return "images/samurai/Samurai.png";
            // 你有更多英雄就在这里扩展
            default:         return currentHeroTexture();
        }
    }

    private void removeHotkeyAdapter() {
        if (hotkeyAdapter != null && Gdx.input.getInputProcessor() instanceof InputMultiplexer mux) {
            mux.removeProcessor(hotkeyAdapter);
            hotkeyAdapter = null;
        }
    }

    private void enterPlacementModeWithTexture(String texturePath) {
        if (placementActive) {
            // 切换类型时更新 Ghost
            Gdx.app.postRunnable(preview::remove);
        }
        placementActive = true;
        preview.createGhost(texturePath);
        entity.getEvents().trigger("placement:on");
    }

    private void enterPlacementMode() {
        enterPlacementModeWithTexture(currentHeroTexture());
    }

    private void exitPlacementMode() {
        placementActive = false;
        Gdx.app.postRunnable(preview::remove);
        entity.getEvents().trigger("placement:off");
    }

    private String currentHeroTexture() {
        String heroTexture = "images/hero/Heroshoot.png";
        var gs = ServiceLocator.getGameStateService();
        if (gs != null) {
            switch (gs.getSelectedHero()) {
                case ENGINEER -> heroTexture = "images/engineer/Engineer.png";
                case SAMURAI  -> heroTexture = "images/samurai/Samurai.png";
                default       -> heroTexture = "images/hero/Heroshoot.png";
            }
        }
        return heroTexture;
    }

    private void warn(String msg) {
        logger.warn("Placement blocked: {}", msg);
        System.out.println("❌ Placement failed: " + msg);
    }

    private void notifyUser(String msg) {
        logger.info(msg);
        System.out.println("ℹ " + msg);
    }

    @Override
    public void dispose() {
        super.dispose();
        Gdx.app.postRunnable(preview::remove);
        removeHotkeyAdapter();
    }
}




