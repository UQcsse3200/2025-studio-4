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

                if (keycode == Input.Keys.H) {
                    enterPlacementMode();
                    return true;
                }

                // 取消预览
                if (keycode == Input.Keys.NUM_4 || keycode == Input.Keys.NUMPAD_4) {
                    Gdx.app.postRunnable(() -> {
                        if (preview.hasGhost()) {
                            logger.info("[HeroPlacement] Key '4' -> cancel preview");
                            preview.remove();
                        }
                    });
                    return true;
                }

                // place
                if (keycode == Input.Keys.S) {
                    // The limit has been reached -> prompt and swallow the event
                    if (!placementActive) {             // ← 关键
                        enterPlacementMode();
                        return true;                    // 本次只“进入”，下一次 S 再放置
                    }

                    if (placedCount >= maxPlacements) {
                        notifyUser("已达到放置上限（" + maxPlacements + "）。");
                        return true;
                    }

                    int mouseX = Gdx.input.getX();
                    int mouseY = Gdx.input.getY();

                    GridPoint2 cell = HeroPlacementRules.screenToGridNoClamp(mouseX, mouseY, terrain);
                    if (cell == null) {
                        warn("Out of map bounds");
                        return true;
                    }
                    if (HeroPlacementRules.isBlockedCell(cell.x, cell.y, mapEditor)) {
                        warn("Cell is blocked (obstacle/path/restricted area)");
                        return true;
                    }

                    try {
                        if (onPlace != null) onPlace.accept(new GridPoint2(cell));
                        placedCount++;
                        logger.info("Hero placed at ({}, {}) via 'S'  ({}/{})", cell.x, cell.y, placedCount, maxPlacements);

                        // After reaching the limit: prompt + unbind input + remove preview
                        if (placedCount >= maxPlacements) {
                            notifyUser("放置完成（" + placedCount + "/" + maxPlacements + "）。已锁定此组件。");
                            Gdx.app.postRunnable(() -> {
                                removeHotkeyAdapter();
                                exitPlacementMode();        // 退出 & 收网格
                            });
                        } else {
                            // Limit not reached: Clear only this preview
                            Gdx.app.postRunnable(preview::remove);
                            preview.createGhost(/*同样的 heroTexture */ "images/hero/Heroshoot.png");
                        }
                    } catch (Exception e) {
                        warn("Placement failed due to exception: " + e.getMessage());
                    }

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

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
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

        // 1) 选择正确的贴图（按当前选择的英雄）
        String heroTexture = "images/hero/Heroshoot.png";
        var gs = ServiceLocator.getGameStateService();
        if (gs != null) {
            switch (gs.getSelectedHero()) {
                case ENGINEER -> heroTexture = "images/engineer/Engineer.png"; // 按你的资源路径改
                case SAMURAI  -> heroTexture = "images/samurai/Samurai.png";   // 按你的资源路径改
                // 其它职业……
                default       -> heroTexture = "images/hero/Heroshoot.png";
            }
        }

        // 2) 创建 ghost
        preview.createGhost(heroTexture);

        // 3) 让 MapHighlighter 也画网格（如果你加了轮询 isPlacementActive，可以不发事件）
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


