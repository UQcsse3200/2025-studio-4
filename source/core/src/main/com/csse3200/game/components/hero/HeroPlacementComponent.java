package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.HeroFactory;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Two-phase hero placement component:
 * <ul>
 *   <li>Right-click: Create a semi-transparent ghost preview at the clicked grid cell.</li>
 *   <li>Left-click: If the preview is active and the click hits the preview cell,
 *       confirm placement by calling {@code onPlace(cell)}. The preview and input are then cleaned up.</li>
 *   <li>ESC: Cancel the preview (remove the ghost) but stay in placement mode so the player
 *       can try again.</li>
 * </ul>
 * <p>
 * This version does not implement mouse-follow or rotation. Those can be added later if needed.
 * </p>
 */
public class HeroPlacementComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(HeroPlacementComponent.class);

    private final TerrainComponent terrain;
    private final Consumer<GridPoint2> onPlace;

    // Input state
    private InputAdapter input;
    private InputMultiplexer muxSnapshot;
    private InputProcessor prevProcessor;
    private boolean createdNewMux;

    // Placement state
    private boolean placed = false; // True after confirmation (idempotence protection)

    // Preview state
    private GridPoint2 previewCell = null;
    private Entity ghostEntity = null;
    private final float ghostAlpha = 0.5f; // Transparency for the ghost preview

    public HeroPlacementComponent(TerrainComponent terrain, Consumer<GridPoint2> onPlace) {
        this.terrain = terrain;
        this.onPlace = onPlace;
    }

    @Override
    public void create() {
        // Setup input multiplexer
        prevProcessor = Gdx.input.getInputProcessor();
        if (prevProcessor instanceof InputMultiplexer) {
            muxSnapshot = (InputMultiplexer) prevProcessor;
            createdNewMux = false;
        } else {
            muxSnapshot = new InputMultiplexer();
            if (prevProcessor != null) muxSnapshot.addProcessor(prevProcessor);
            Gdx.input.setInputProcessor(muxSnapshot);
            createdNewMux = true;
        }

        input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
                    // ESC cancels the current preview but does not exit placement mode
                    Gdx.app.postRunnable(() -> {
                        removeGhost();
                        placed = false;
                    });
                    logger.info("Hero preview cancelled (placement mode still active).");
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (pointer != 0) return false; // Ignore multi-touch

                if (button == Input.Buttons.RIGHT) {
                    // Right-click: generate preview if none exists
                    if (ghostEntity == null) {
                        GridPoint2 cell = screenToGrid(screenX, screenY);
                        if (cell == null) return true;
                        // Optional: check if terrain.isPlaceable(cell)
                        spawnGhost(cell);
                        previewCell = cell;
                        logger.info("Preview hero at ({}, {})", cell.x, cell.y);
                        return true;
                    }
                    return false;
                }

                if (button == Input.Buttons.LEFT) {
                    // Left-click: confirm placement if clicking the preview cell
                    if (ghostEntity != null && previewCell != null && hitGhostByScreen(screenX, screenY)) {
                        final GridPoint2 cell = new GridPoint2(previewCell);
                        placed = true;
                        try {
                            if (onPlace != null) onPlace.accept(cell);
                        } finally {
                            Gdx.app.postRunnable(() -> {
                                removeGhost();
                                detachInput(true);
                            });
                        }
                        logger.info("Hero confirmed at ({}, {})", cell.x, cell.y);
                        return true;
                    }
                    return false;
                }

                return false;
            }
        };

        // Insert at highest priority
        muxSnapshot.addProcessor(0, input);
        logger.info("Right-click to preview, left-click to confirm, ESC to cancel preview.");
    }

    /** Convert screen coordinates to a clamped grid cell. */
    private GridPoint2 screenToGrid(int sx, int sy) {
        Renderer r = Renderer.getCurrentRenderer();
        if (r == null || r.getCamera() == null) return null;
        Camera cam = r.getCamera().getCamera();

        Vector3 world = new Vector3(sx, sy, 0f);
        cam.unproject(world);

        float tile = terrain.getTileSize();
        GridPoint2 bounds = terrain.getMapBounds(0); // width, height
        int gx = MathUtils.clamp((int) Math.floor(world.x / tile), 0, bounds.x - 1);
        int gy = MathUtils.clamp((int) Math.floor(world.y / tile), 0, bounds.y - 1);
        return new GridPoint2(gx, gy);
    }

    /** Spawn a ghost entity at the given grid cell. */
    private void spawnGhost(GridPoint2 cell) {
        ghostEntity = HeroFactory.createHeroGhost(ghostAlpha);
        ServiceLocator.getEntityService().register(ghostEntity);

        // Scale to exactly one tile (TextureRenderComponent uses entity scale as width/height)
        float tile = terrain.getTileSize();
        ghostEntity.setScale(tile, tile);

        placeEntityAtCell(ghostEntity, cell);
    }

    /** Place the given entity at the bottom-left of a grid cell. */
    private void placeEntityAtCell(Entity e, GridPoint2 cell) {
        float tile = terrain.getTileSize();
        e.setPosition(cell.x * tile, cell.y * tile); // bottom-left aligned; add tile/2 for center alignment
    }

    /** Check if a screen click falls within the ghost preview cell. */
    private boolean hitGhostByScreen(int sx, int sy) {
        if (ghostEntity == null || previewCell == null) return false;
        Renderer r = Renderer.getCurrentRenderer();
        if (r == null || r.getCamera() == null) return false;

        Vector3 world = new Vector3(sx, sy, 0f);
        r.getCamera().getCamera().unproject(world);

        float tile = terrain.getTileSize();
        float gx0 = previewCell.x * tile, gy0 = previewCell.y * tile;
        float gx1 = gx0 + tile, gy1 = gy0 + tile;

        return world.x >= gx0 && world.x <= gx1 && world.y >= gy0 && world.y <= gy1;
    }

    /** Remove the ghost preview if it exists. */
    private void removeGhost() {
        if (ghostEntity != null) {
            ghostEntity.dispose();
            ghostEntity = null;
        }
        previewCell = null;
    }

    /** Detach this component’s input processor and optionally restore the previous one. */
    private void detachInput(boolean restorePrev) {
        if (input != null && muxSnapshot != null) {
            muxSnapshot.removeProcessor(input);
            input = null;
        }
        if (restorePrev && createdNewMux) {
            InputProcessor cur = Gdx.input.getInputProcessor();
            if (cur == muxSnapshot) {
                Gdx.input.setInputProcessor(prevProcessor);
            } else {
                // If another system replaced the processor, do not override it
            }
        }
        muxSnapshot = null;
        prevProcessor = null;
    }

    @Override
    public void dispose() {
        Gdx.app.postRunnable(() -> {
            removeGhost();
            detachInput(true);
        });
    }
}

