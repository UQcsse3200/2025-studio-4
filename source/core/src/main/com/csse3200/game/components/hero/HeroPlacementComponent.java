package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.MapEditor;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.input.InputComponent;
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

    // ====== 新增：放置上限控制 ======
    private int maxPlacements = 1;   // 默认最多 1
    private int placedCount   = 0;   // 已放置数量

    public HeroPlacementComponent(TerrainComponent terrain, MapEditor mapEditor, Consumer<GridPoint2> onPlace) {
        super(500);
        this.terrain = terrain;
        this.mapEditor = mapEditor;
        this.onPlace = onPlace;
        this.preview = new HeroGhostPreview(terrain, 0.5f);
    }

    /** 可选：带自定义上限的构造 */
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

                // 放置
                if (keycode == Input.Keys.S) {
                    // 已达上限 -> 提示并吞掉事件
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

                        // 达到上限后：提示 + 解绑输入 + 移除预览
                        if (placedCount >= maxPlacements) {
                            notifyUser("放置完成（" + placedCount + "/" + maxPlacements + "）。已锁定此组件。");
                            Gdx.app.postRunnable(() -> {
                                removeHotkeyAdapter();
                                preview.remove();
                            });
                        } else {
                            // 未达上限：仅清理这次预览
                            Gdx.app.postRunnable(preview::remove);
                        }
                    } catch (Exception e) {
                        warn("Placement failed due to exception: " + e.getMessage());
                    }

                    return true;
                }
                return false;
            }
        };

        // 把监听器插到 Multiplexer 的 index 0（优先级最高）
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

    @Override
    public void dispose() {
        super.dispose();
        Gdx.app.postRunnable(preview::remove);
        removeHotkeyAdapter();
    }
}


