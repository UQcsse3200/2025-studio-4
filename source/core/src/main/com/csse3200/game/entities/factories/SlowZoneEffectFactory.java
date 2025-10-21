package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.effects.SlowZoneVisualComponent;
import com.csse3200.game.entities.Entity;

/**
 * Factory for decorative slow-zone effects placed along enemy paths.
 */
public final class SlowZoneEffectFactory {
  private SlowZoneEffectFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static Entity create(float tileSize) {
    Entity effect = new Entity();
    effect.addComponent(new SlowZoneVisualComponent(tileSize));
    effect.setScale(new Vector2(tileSize, tileSize));
    return effect;
  }
}
