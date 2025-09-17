package com.csse3200.game.components.player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.csse3200.game.input.InputComponent;

/**
 * Input handler for the player for keyboard input.
 * This input handler only handles attack input since player movement is disabled.
 */
public class KeyboardPlayerInputComponent extends InputComponent {


  /**
   * Triggers player events on specific keycodes.
   * Only handles attack input since player movement is disabled.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyDown(int)
   */
  @Override
  public boolean keyDown(int keycode) {
    switch (keycode) {
      case Keys.SPACE:
        entity.getEvents().trigger("attack");
        return true;
      default:
        return false;
    }
  }

  /**
   * Triggers player events on specific keycodes.
   * No keyUp events needed since only attack is handled.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyUp(int)
   */
  @Override
  public boolean keyUp(int keycode) {
    // No keyUp events needed for attack input
    return false;
  }
}
