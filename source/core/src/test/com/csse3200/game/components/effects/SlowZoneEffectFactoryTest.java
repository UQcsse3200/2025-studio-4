package com.csse3200.game.components.effects;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.SlowZoneEffectFactory;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class SlowZoneEffectFactoryTest {

  @Test
  void createSlowZoneEffectAddsVisualComponent() {
    float tileSize = 1.5f;
    Entity effect = SlowZoneEffectFactory.create(tileSize);

    assertNotNull(effect, "Factory should return a valid entity instance");

    SlowZoneVisualComponent visualComponent =
        effect.getComponent(SlowZoneVisualComponent.class);
    assertNotNull(visualComponent, "Entity should include SlowZoneVisualComponent");

    Vector2 scale = effect.getScale();
    assertEquals(tileSize, scale.x, 1e-6f, "Scale.x should match tile size");
    assertEquals(tileSize, scale.y, 1e-6f, "Scale.y should match tile size");
  }
}
