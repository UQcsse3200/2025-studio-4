package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

    public HeroPlacementComponent(TerrainComponent terrain, MapEditor mapEditor, Consumer<GridPoint2> onPlace) {
        super(500);
        this.terrain = terrain;
        this.mapEditor = mapEditor;
        this.onPlace = onPlace;
        this.preview = new HeroGhostPreview(terrain, 0.5f);
    }

    @Override
    public void create() {
        super.create();
        logger.info("HeroPlacement ready. RMB preview, LMB confirm, 'S' cancel.");
    }

    // 键盘：S 取消预览
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.NUM_4 || keycode == Input.Keys.NUMPAD_4) {
            Gdx.app.postRunnable(() -> {
                if (preview.hasGhost()) {
                    logger.info("[HeroPlacement] S -> cancel preview");
                    preview.remove();
                }
            });
            return true;
        }
        return false;
    }

    // 鼠标
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0) return false;

        // 右键：生成预览（仅合法格）
        if (button == Input.Buttons.RIGHT) {
            if (!preview.hasGhost()) {
                GridPoint2 cell = HeroPlacementRules.screenToGridNoClamp(screenX, screenY, terrain);
                if (cell == null) {
                    warn("超出地图边界");
                    return true;
                }
                if (HeroPlacementRules.isBlockedCell(cell.x, cell.y, mapEditor)) {
                    warn("该位置不可放置（障碍/路径/禁放区）");
                    return true;
                }
                preview.spawnAt(cell);
                logger.info("Preview at ({}, {})", cell.x, cell.y);
                return true;
            }
            return false;
        }

        // 左键：点击预览格确认（再次校验）
        if (button == Input.Buttons.LEFT) {
            if (preview.hasGhost() && preview.hitByScreen(screenX, screenY)) {
                GridPoint2 cell = preview.getCell();
                // 再校验一次
                GridPoint2 recheck = HeroPlacementRules.screenToGridNoClamp(screenX, screenY, terrain);
                if (recheck == null) {
                    warn("超出地图边界");
                    return true;
                }
                if (HeroPlacementRules.isBlockedCell(cell.x, cell.y, mapEditor)) {
                    warn("该位置不可放置（障碍/路径/禁放区）");
                    return true;
                }
                try {
                    if (onPlace != null) onPlace.accept(new GridPoint2(cell));
                } finally {
                    Gdx.app.postRunnable(preview::remove);
                }
                logger.info("Hero confirmed at ({}, {})", cell.x, cell.y);
                return true;
            }
            return false;
        }

        return false;
    }

    private void warn(String msg) {
        logger.warn("Placement blocked: {}", msg);
        System.out.println("❌ 放置失败: " + msg);
    }

    @Override
    public void dispose() {
        super.dispose();
        Gdx.app.postRunnable(preview::remove);
    }
}




