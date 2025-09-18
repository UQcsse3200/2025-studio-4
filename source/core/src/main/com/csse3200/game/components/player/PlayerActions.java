package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * Action component for interacting with the player. Player events should be initialised in create()
 * and when triggered should call methods within this class.
 * Note: Player movement is disabled - this is a static base entity.
 */
public class PlayerActions extends Component {

  @Override
  public void create() {
    // Only listen to attack events since movement is disabled
    entity.getEvents().addListener("attack", this::attack);
  }

  @Override
  public void update() {
    // Static base entity - no movement needed
  }

  /**
   * Makes the player attack.
   */
  void attack() {
    Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
    attackSound.play();
  }
}
