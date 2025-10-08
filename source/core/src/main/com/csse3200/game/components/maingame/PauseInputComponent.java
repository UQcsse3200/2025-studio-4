package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles ESC key toggling of pause state.
 * - Shows/hides PauseMenuDisplay via events.
 * - Broadcasts gamePaused/gameResumed to all entities so AI/physics can re-sync.
 *   (This avoids touching EntityService internals.)
 */
public class PauseInputComponent extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(PauseInputComponent.class);

    private boolean paused = false;

    @Override
    public void update() {
        super.update();

        // ESC toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (paused) {
                resume();
            } else {
                pause();
            }
        }
    }

    private void pause() {
        if (paused) return;
        paused = true;
        logger.info("Game paused");

        // Show pause overlay
        entity.getEvents().trigger("showPauseUI");

        // Let every entity know we paused (AI can stop steering, physics can sleep, etc.)
        EntityService es = ServiceLocator.getEntityService();
        if (es != null) {
            Array<Entity> all = es.getEntities();
            for (Entity e : all) {
                e.getEvents().trigger("gamePaused");
            }
        }
    }

    private void resume() {
        if (!paused) return;
        paused = false;
        logger.info("Game resumed");

        // Hide pause overlay
        entity.getEvents().trigger("hidePauseUI");

        // Let every entity know we resumed (AI can reacquire paths/targets, wake bodies, etc.)
        EntityService es = ServiceLocator.getEntityService();
        if (es != null) {
            Array<Entity> all = es.getEntities();
            for (Entity e : all) {
                e.getEvents().trigger("gameResumed");
            }
        }
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // No direct drawing; stage handles UI
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
