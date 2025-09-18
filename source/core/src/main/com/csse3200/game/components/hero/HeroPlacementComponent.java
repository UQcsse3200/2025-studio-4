package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.MapEditor;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.input.InputComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.badlogic.gdx.InputMultiplexer;


import java.util.function.Consumer;

public class HeroPlacementComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeroPlacementComponent.class);

    private final TerrainComponent terrain;
    private final MapEditor mapEditor;
    private final Consumer<GridPoint2> onPlace;

    private final HeroGhostPreview preview;

    private InputAdapter hotkeyAdapter;

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
        logger.info("HeroPlacement ready. Press 'S' to place at mouse cell. '4' = cancel preview.");

        hotkeyAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                System.out.println("[HOTKEY DEBUG] keyDown=" + keycode);

                if (keycode == Input.Keys.NUM_4 || keycode == Input.Keys.NUMPAD_4) {
                    Gdx.app.postRunnable(() -> {
                        if (preview.hasGhost()) {
                            logger.info("[HeroPlacement] Key '4' -> cancel preview");
                            preview.remove();
                        }
                    });
                    return true;
                }

                if (keycode == Input.Keys.S) {
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
                    } finally {
                        Gdx.app.postRunnable(preview::remove);
                    }

                    logger.info("Hero placed at ({}, {}) via 'S'", cell.x, cell.y);
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

    @Override
    public void dispose() {
        super.dispose();
        Gdx.app.postRunnable(preview::remove);

        if (hotkeyAdapter != null && Gdx.input.getInputProcessor() instanceof InputMultiplexer mux) {
            mux.removeProcessor(hotkeyAdapter);
            hotkeyAdapter = null;
        }
    }

}

