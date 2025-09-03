package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.areas.terrain.TerrainComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * One-time component for placing a hero with a right-click.
 * - Right-click: project screen coordinates into world space -> map to tile grid -> call onPlace(cell)
 * - ESC: cancel placement and restore the previous InputProcessor
 * - After placement, the component automatically removes itself from the InputMultiplexer,
 *   and restores the previous processor if needed
 */
public class HeroPlacementComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(HeroPlacementComponent.class);

    private final TerrainComponent terrain;
    private final Consumer<GridPoint2> onPlace;

    private volatile boolean placed = false;     // Idempotence guard
    private volatile boolean detached = false;   // Prevent double cleanup

    private InputAdapter input;
    private InputMultiplexer muxSnapshot;
    private boolean createdNewMux;
    private InputProcessor prevProcessor;

    public HeroPlacementComponent(TerrainComponent terrain, Consumer<GridPoint2> onPlace) {
        this.terrain = terrain;
        this.onPlace = onPlace;
    }

    @Override
    public void create() {
        // Record the current input processor
        prevProcessor = Gdx.input.getInputProcessor();

        // Always use a multiplexer: reuse if exists, otherwise wrap the existing processor
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
                if (keycode == Input.Keys.ESCAPE && !placed) {
                    // Delay cleanup to avoid modifying multiplexer during event dispatch
                    Gdx.app.postRunnable(() -> detachInput(/*restorePrev=*/true));
                    logger.info("Hero placement cancelled.");
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (placed || button != Input.Buttons.RIGHT) return false;

                Renderer r = Renderer.getCurrentRenderer();
                if (r == null || r.getCamera() == null) return false;
                Camera cam = r.getCamera().getCamera();

                Vector3 world = new Vector3(screenX, screenY, 0);
                cam.unproject(world);

                float tileSize = terrain.getTileSize();
                GridPoint2 bounds = terrain.getMapBounds(0);
                int gx = Math.max(0, Math.min((int) (world.x / tileSize), bounds.x - 1));
                int gy = Math.max(0, Math.min((int) (world.y / tileSize), bounds.y - 1));
                GridPoint2 cell = new GridPoint2(gx, gy);

                placed = true;
                try {
                    if (onPlace != null) onPlace.accept(cell);
                } finally {
                    // Important: use postRunnable to delay removing the processor & restoring the old one
                    Gdx.app.postRunnable(() -> detachInput(/*restorePrev=*/true));
                }

                logger.info("Hero placed at grid ({}, {}).", gx, gy);
                return true;
            }
        };

        // Insert our processor at the front to ensure higher priority
        muxSnapshot.addProcessor(0, input);
        logger.info("Right-click to place the hero.");
    }

    private void detachInput(boolean restorePrev) {
        if (detached) return; // Idempotent
        detached = true;

        // Safely remove our processor
        if (input != null && muxSnapshot != null) {
            muxSnapshot.removeProcessor(input);
            input = null;
        }

        // Restore the previous processor only if:
        // - we created a new multiplexer originally
        // - the current input processor is still the same multiplexer
        if (restorePrev && createdNewMux) {
            InputProcessor cur = Gdx.input.getInputProcessor();
            if (cur == muxSnapshot) {
                Gdx.input.setInputProcessor(prevProcessor);
            } else {
                // Someone else has already replaced it; don’t override to avoid conflicts
                logger.debug("Skip restoring prevProcessor: current InputProcessor changed by others.");
            }
        }

        // Release references
        muxSnapshot = null;
        prevProcessor = null;
    }

    @Override
    public void dispose() {
        // Use postRunnable to ensure cleanup does not conflict with ongoing event dispatch
        Gdx.app.postRunnable(() -> detachInput(/*restorePrev=*/true));
    }
}

