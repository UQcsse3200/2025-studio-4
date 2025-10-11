package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.MapEditor;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class HeroPlacementComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroPlacementComponent.class);

    private final TerrainComponent terrain;
    private final MapEditor mapEditor;
    private final Consumer<GridPoint2> onPlace;

    private final HeroGhostPreview preview;
    private InputAdapter hotkeyAdapter;

    private boolean placementActive = false;

    // ====== New: Placement limit control======
    private int maxPlacements = 1;   // Default most 1
    private int placedCount = 0;   // Placed quantity

    public HeroPlacementComponent(TerrainComponent terrain, MapEditor mapEditor, Consumer<GridPoint2> onPlace) {
        super(500);
        this.terrain = terrain;
        this.mapEditor = mapEditor;
        this.onPlace = onPlace;
        this.preview = new HeroGhostPreview(terrain, 0.5f);
    }

    /**
     * Optional: Construction with custom caps
     */
    public HeroPlacementComponent(TerrainComponent terrain, MapEditor mapEditor, Consumer<GridPoint2> onPlace, int maxPlacements) {
        this(terrain, mapEditor, onPlace);
        this.maxPlacements = Math.max(1, maxPlacements);
    }


    @Override
    public void create() {
        super.create();
        logger.info("HeroPlacement ready. Press 'S' to place at mouse cell. '4' = cancel preview. (cap: {}/{})",
                placedCount, maxPlacements);

        hotkeyAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                System.out.println("[HOTKEY DEBUG] keyDown=" + keycode);

                if (keycode == Input.Keys.S) {        // 进入放置模式
                    enterPlacementMode();
                    return true;
                }
                return false;
            }
        };

        // Insert the listener at index 0 of the Multiplexer (highest priority)
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
        // 如果没在放置模式，就不用更新
        if (!placementActive) return;

        // === 1. 获取相机 ===
        com.badlogic.gdx.graphics.OrthographicCamera camera = null;
        var all = ServiceLocator.getEntityService().getEntitiesCopy();
        if (all != null) {
            for (var e : all) {
                var cc = e.getComponent(com.csse3200.game.components.CameraComponent.class);
                if (cc != null && cc.getCamera() instanceof com.badlogic.gdx.graphics.OrthographicCamera oc) {
                    camera = oc;
                    break;
                }
            }
        }
        if (camera == null) return;

        // === 2. 屏幕坐标 -> 世界坐标 ===
        com.badlogic.gdx.math.Vector3 mp = new com.badlogic.gdx.math.Vector3(
                Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mp);

        // === 3. 吸附到格子 ===
        float tile = terrain.getTileSize();
        com.badlogic.gdx.math.GridPoint2 bounds = terrain.getMapBounds(0);
        int gx = Math.max(0, Math.min((int)(mp.x / tile), bounds.x - 1));
        int gy = Math.max(0, Math.min((int)(mp.y / tile), bounds.y - 1));
        com.badlogic.gdx.math.Vector2 snap = terrain.tileToWorldPosition(gx, gy);

        // === 4. 移动 Ghost ===
        preview.setGhostPosition(snap.x, snap.y);

        // === 5. （可选）通知 MapHighlighter 高亮当前格 ===
        entity.getEvents().trigger("placement:hover", new com.badlogic.gdx.math.GridPoint2(gx, gy));
    }


    @Override
    public boolean keyDown(int keycode) {
        System.out.println("[COMPONENT DEBUG] keyDown=" + keycode);
        return false;
    }

    // 2) 用鼠标左键完成放置（替换/实现 touchDown）
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) {
            if (placementActive) {
                logger.info("[HeroPlacement] Right-click -> exit placement mode");
                notifyUser("已退出放置模式。");
                exitPlacementMode();
                return true; // 消费右键事件
            }
            return false;
        }

        // 只在放置模式 + 左键 时处理
        if (!placementActive || button != com.badlogic.gdx.Input.Buttons.LEFT) {
            return false;
        }

        // 上限检查
        if (placedCount >= maxPlacements) {
            notifyUser("已达到放置上限（" + maxPlacements + "）。");
            // 消费事件，避免被其他处理器继续处理（例如编辑器）
            return true;
        }

        // 计算目标格
        GridPoint2 cell = HeroPlacementRules.screenToGridNoClamp(screenX, screenY, terrain);
        if (cell == null) {
            warn("Out of map bounds");
            return true;
        }
        if (HeroPlacementRules.isBlockedCell(cell.x, cell.y, mapEditor)) {
            warn("Cell is blocked (obstacle/path/restricted area)");
            return true;
        }

        // 执行放置
        try {
            if (onPlace != null) onPlace.accept(new GridPoint2(cell));
            placedCount++;
            logger.info("Hero placed at ({}, {}) via MOUSE LEFT  ({}/{})", cell.x, cell.y, placedCount, maxPlacements);

            if (placedCount >= maxPlacements) {
                notifyUser("放置完成（" + placedCount + "/" + maxPlacements + "）。已锁定此组件。");
                Gdx.app.postRunnable(() -> {
                    removeHotkeyAdapter(); // 如需继续允许 H 重新进入放置，可不移除
                    exitPlacementMode();
                });
            } else {
                // 未达上限：仅刷新 ghost（保持继续放置）
                Gdx.app.postRunnable(() -> {
                    preview.remove();
                    preview.createGhost(currentHeroTexture()); // 见下方小工具函数
                });
            }
        } catch (Exception e) {
            warn("Placement failed due to exception: " + e.getMessage());
        }

        // 一定要返回 true，拦截事件，避免被别的系统（例如地图编辑器）继续处理
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
        // 如果有 UI 弹窗/Toast，可在此接入：
        // ServiceLocator.getUIService().toast(msg);
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

    // 提供只读方法给高亮器查询
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


