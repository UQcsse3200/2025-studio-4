package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;

import com.csse3200.game.areas.IMapEditor;
import com.csse3200.game.areas.terrain.ITerrainComponent;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.services.ServiceLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class HeroPlacementComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroPlacementComponent.class);

    private final ITerrainComponent terrain;     // ✅ 接口
    private final IMapEditor mapEditor;          // ✅ 接口
    private final Consumer<GridPoint2> onPlace;

    private final HeroGhostPreview preview;      // 需要让它也接受 ITerrainComponent（见下）
    private InputAdapter hotkeyAdapter;

    private boolean placementActive = false;

    // 放置上限
    private int maxPlacements = 1;
    private int placedCount = 0;

    // ✅ 用接口作为构造参数
    public HeroPlacementComponent(ITerrainComponent terrain, IMapEditor mapEditor, Consumer<GridPoint2> onPlace) {
        super(500);
        this.terrain = terrain;
        this.mapEditor = mapEditor;
        this.onPlace = onPlace;
        this.preview = new HeroGhostPreview(terrain, 0.5f); // 见第3步
    }

    // 可选：带上限的重载
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

        hotkeyAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.S) { // 进入放置模式
                    enterPlacementMode();
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

        // 1) 找正交相机
        OrthographicCamera camera = null;
        var all = ServiceLocator.getEntityService().getEntitiesCopy();
        if (all != null) {
            for (var e : all) {
                var cc = e.getComponent(com.csse3200.game.components.CameraComponent.class);
                if (cc != null && cc.getCamera() instanceof OrthographicCamera oc) {
                    camera = oc;
                    break;
                }
            }
        }
        if (camera == null) return;

        // 2) 屏幕->世界
        Vector3 mp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mp);

        // 3) 吸附到格子（接口提供的 tile 尺寸/边界/转换）
        float tile = terrain.getTileSize();
        GridPoint2 bounds = terrain.getMapBounds(0); // 或者改为不带层的 getMapBounds()
        int gx = Math.max(0, Math.min((int)(mp.x / tile), bounds.x - 1));
        int gy = Math.max(0, Math.min((int)(mp.y / tile), bounds.y - 1));
        Vector2 snap = terrain.tileToWorldPosition(gx, gy);

        // 4) 移动 Ghost
        preview.setGhostPosition(snap.x, snap.y);

        // 5) 通知高亮
        entity.getEvents().trigger("placement:hover", new GridPoint2(gx, gy));
    }

    @Override
    public boolean keyDown(int keycode) {
        // 让 InputMultiplexer 把按键优先交给 hotkeyAdapter，这里一般返回 false
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) {
            if (placementActive) {
                notifyUser("已退出放置模式。");
                exitPlacementMode();
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
        if (HeroPlacementRules.isBlockedCell(cell.x, cell.y, mapEditor,terrain)) {
            warn("Cell is blocked (obstacle/path/restricted area)");
            return true;
        }

        try {
            if (onPlace != null) onPlace.accept(new GridPoint2(cell));
            placedCount++;
            logger.info("Hero placed at ({}, {}) ({}/{})", cell.x, cell.y, placedCount, maxPlacements);

            if (placedCount >= maxPlacements) {
                notifyUser("放置完成（" + placedCount + "/" + maxPlacements + "）。已锁定此组件。");
                Gdx.app.postRunnable(() -> {
                    removeHotkeyAdapter();
                    exitPlacementMode();
                });
            } else {
                Gdx.app.postRunnable(() -> {
                    preview.remove();
                    preview.createGhost(currentHeroTexture());
                });
            }
        } catch (Exception e) {
            warn("Placement failed due to exception: " + e.getMessage());
        }
        return true;
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

    private void removeHotkeyAdapter() {
        if (hotkeyAdapter != null && Gdx.input.getInputProcessor() instanceof InputMultiplexer mux) {
            mux.removeProcessor(hotkeyAdapter);
            hotkeyAdapter = null;
        }
    }

    private void enterPlacementMode() {
        if (placementActive) return;
        placementActive = true;
        preview.createGhost(currentHeroTexture());
        entity.getEvents().trigger("placement:on");
    }

    private void exitPlacementMode() {
        placementActive = false;
        Gdx.app.postRunnable(preview::remove);
        entity.getEvents().trigger("placement:off");
    }

    public boolean isPlacementActive() {
        return placementActive;
    }

    @Override
    public void dispose() {
        super.dispose();
        Gdx.app.postRunnable(preview::remove);
        removeHotkeyAdapter();
    }
}



