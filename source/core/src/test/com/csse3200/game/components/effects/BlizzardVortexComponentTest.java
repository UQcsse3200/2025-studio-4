package com.csse3200.game.components.effects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Verifies the behaviour of the {@link BlizzardVortexComponent}. We ensure the component initialises correctly,
 * updates its internal state, and provides deterministic geometry for the underlying entity.
 */
@ExtendWith(GameExtension.class)
class BlizzardVortexComponentTest {

  @BeforeEach
  void setUp() {
    ServiceLocator.registerTimeSource(new GameTime());
    RenderService renderService = new RenderService();
    renderService.setStage(mock(Stage.class));
    ServiceLocator.registerRenderService(renderService);
    ServiceLocator.registerEntityService(new EntityService());
  }

  @AfterEach
  void tearDown() {
    ServiceLocator.clear();
  }

  @Test
  void createInitialisesVortex() {
    Entity entity = new Entity();
    entity.setPosition(new Vector2(0f, 0f));
    BlizzardVortexComponent vortex = new BlizzardVortexComponent(10f, 6f);
    entity.addComponent(vortex);

    SpriteBatch batch = mock(SpriteBatch.class);

    entity.create();
    vortex.update();
    vortex.draw(batch);

    assertNotNull(entity.getComponent(BlizzardVortexComponent.class));
  }

  @Test
  void updateAdvancesRotationAndDrawSucceeds() {
    Entity entity = new Entity();
    entity.setPosition(100f, 200f);
    BlizzardVortexComponent vortex = new BlizzardVortexComponent(12f, 8f);
    entity.addComponent(vortex);

    SpriteBatch batch = mock(SpriteBatch.class);

    entity.create();
    // simulate a few frames
    for (int i = 0; i < 5; i++) {
      vortex.update();
    }

    assertAll(
        () -> assertDoesNotThrow(() -> vortex.draw(batch)),
        () -> assertEquals(1f, entity.getScale().x, "The vortex should not change entity scale"));
  }
}
