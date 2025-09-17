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

/**
 * HeroPlacementComponent:
 * Handles the two-phase placement of a hero entity on the map.
 *
 * <ul>
 *   <li>Right mouse button: Creates a semi-transparent ghost preview at a valid grid cell.</li>
 *   <li>Left mouse button: Confirms placement if the click hits the preview cell.</li>
 *   <li>Key '4': Cancels the preview.</li>
 * </ul>
 */
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
        logger.info("HeroPlacement ready. RMB = preview, LMB = confirm, '4' = cancel.");
    }

    /**
     * Keyboard input: cancel preview with '4'.
     */
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.NUM_4 || keycode == Input.Keys.NUMPAD_4) {
            Gdx.app.postRunnable(() -> {
                if (preview.hasGhost()) {
                    logger.info("[HeroPlacement] Key '4' -> cancel preview");
                    preview.remove();
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Mouse input:
     * <ul>
     *   <li>Right click: Spawn preview at a valid grid cell (if none exists).</li>
     *   <li>Left click: Confirm placement at the preview cell (with validation).</li>
     * </ul>
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0) return false;

        // Right mouse button: create preview (only if the cell is valid)
        if (button == Input.Buttons.RIGHT) {
            if (!preview.hasGhost()) {
                GridPoint2 cell = HeroPlacementRules.screenToGridNoClamp(screenX, screenY, terrain);
                if (cell == null) {
                    warn("Out of map bounds");
                    return true;
                }
                if (HeroPlacementRules.isBlockedCell(cell.x, cell.y, mapEditor)) {
                    warn("Cell is blocked (obstacle/path/restricted area)");
                    return true;
                }
                preview.spawnAt(cell);
                logger.info("Preview at ({}, {})", cell.x, cell.y);
                return true;
            }
            return false;
        }

        // Left mouse button: confirm placement on the preview cell (with re-check)
        if (button == Input.Buttons.LEFT) {
            if (preview.hasGhost() && preview.hitByScreen(screenX, screenY)) {
                GridPoint2 cell = preview.getCell();

                // Re-validate position
                GridPoint2 recheck = HeroPlacementRules.screenToGridNoClamp(screenX, screenY, terrain);
                if (recheck == null) {
                    warn("Out of map bounds");
                    return true;
                }
                if (HeroPlacementRules.isBlockedCell(cell.x, cell.y, mapEditor)) {
                    warn("Cell is blocked (obstacle/path/restricted area)");
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

    /**
     * Logs and prints a placement warning message.
     */
    private void warn(String msg) {
        logger.warn("Placement blocked: {}", msg);
        System.out.println("❌ Placement failed: " + msg);
    }

    @Override
    public void dispose() {
        super.dispose();
        Gdx.app.postRunnable(preview::remove);
    }
}




