package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.badlogic.gdx.utils.Array;

/**
 * ESC/P toggles pause. Freezes time and broadcasts "gamePaused"/"gameResumed"
 * so path-followers can recompute their routes safely.
 */
public class PauseInputComponent extends UIComponent {
    private boolean paused = false;

    @Override
    public void create() {
        super.create();

        // Toggle via keyboard
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.P) {
                    togglePause();
                    return true;
                }
                return false;
            }
        });

        // Toggle via UI buttons
        entity.getEvents().addListener("togglePause", this::togglePause);
        entity.getEvents().addListener("resume", this::resumeGame);
    }

    private void togglePause() {
        if (paused) resumeGame();
        else pauseGame();
    }

    private void pauseGame() {
        paused = true;

        // Freeze the simulation (everything that uses GameTime’s delta stops)
        GameTime time = ServiceLocator.getTimeSource();
        if (time != null) {
            try { time.setTimeScale(0f); } catch (Throwable ignored) {}
        }

        // Tell everyone we paused (AI can stash state if needed)
        broadcast("gamePaused");

        // Show overlay
        entity.getEvents().trigger("showPauseUI");
    }

    private void resumeGame() {
        paused = false;

        // Restore time
        GameTime time = ServiceLocator.getTimeSource();
        if (time != null) {
            try { time.setTimeScale(1f); } catch (Throwable ignored) {}
        }

        // Hide overlay first
        entity.getEvents().trigger("hidePauseUI");

        // Tell everyone we resumed so AI can REPATH from current tile
        broadcast("gameResumed");
    }

    private void broadcast(String event) {
        if (ServiceLocator.getEntityService() == null) return;
        Array<Entity> all = ServiceLocator.getEntityService().getEntities();
        for (int i = 0; i < all.size; i++) {
            all.get(i).getEvents().trigger(event);
        }
    }

    @Override protected void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {}
    @Override public float getZIndex() { return 200f; }
}
