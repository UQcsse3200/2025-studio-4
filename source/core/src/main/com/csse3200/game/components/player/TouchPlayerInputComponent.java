package com.csse3200.game.components.player;

import com.csse3200.game.input.InputComponent;
import com.badlogic.gdx.InputProcessor;

/**
 * Input handler for the player for touch (mouse) input.
 * This input handler only handles attack input since player movement is disabled.
 */
public class TouchPlayerInputComponent extends InputComponent {

  /**
   * Triggers player events on specific keycodes.
   * No keyboard input needed since player movement is disabled.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyDown(int)
   */
  @Override
  public boolean keyDown(int keycode) {
    // No keyboard input needed for static player
    return false;
  }

  /**
   * Triggers player events on specific keycodes.
   * No keyboard input needed since player movement is disabled.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyUp(int)
   */
  @Override
  public boolean keyUp(int keycode) {
    // No keyboard input needed for static player
    return false;
  }

  /**
   * Triggers the player attack on touch/click.
   * @return whether the input was processed
   * @see InputProcessor#touchDown(int, int, int, int)
   */
  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    entity.getEvents().trigger("attack");
    return true;
  }
}
