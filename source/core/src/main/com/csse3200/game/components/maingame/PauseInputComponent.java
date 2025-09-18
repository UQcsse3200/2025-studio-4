package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Input;
import com.csse3200.game.input.InputComponent;

/** Handles ESC / P to toggle the pause menu. */
public class PauseInputComponent extends InputComponent {
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.P) {
            entity.getEvents().trigger("togglePause");
            return true;
        }
        return false;
    }

    // No-op other inputs
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}
