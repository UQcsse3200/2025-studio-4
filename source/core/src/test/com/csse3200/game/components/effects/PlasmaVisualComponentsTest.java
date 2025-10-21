package com.csse3200.game.components.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class PlasmaVisualComponentsTest {

  @BeforeEach
  void setupServices() {
    Graphics graphics = mock(Graphics.class);
    when(graphics.getDeltaTime()).thenReturn(0.1f);
    Gdx.graphics = graphics;

    ServiceLocator.registerRenderService(new RenderService());
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerTimeSource(new GameTime());
  }

  @Test
  void warningSchedulesDisposeAfterDuration() throws Exception {
    Entity warningEntity = new Entity();
    PlasmaWarningComponent warning = new PlasmaWarningComponent(0.2f, 1f);
    warningEntity.addComponent(warning);
    ServiceLocator.getEntityService().register(warningEntity);

    for (int i = 0; i < 20; i++) {
      warning.update();
    }

    assertTrue(getDisposeFlag(warning), "Warning should schedule disposal after duration");
  }

  @Test
  void strikeFallsAndNotifiesListener() throws Exception {
    AtomicReference<Vector2> impact = new AtomicReference<>();
    Entity strikeEntity = new Entity();
    PlasmaStrikeComponent strike =
        new PlasmaStrikeComponent(new Vector2(2f, 2f), 0.6f, 2f, impact::set);
    strikeEntity.addComponent(strike);
    ServiceLocator.getEntityService().register(strikeEntity);

    for (int i = 0; i < 30 && impact.get() == null; i++) {
      strike.update();
    }

    assertNotNull(impact.get(), "Impact listener should be invoked when strike hits ground");
    assertTrue(getDisposeFlag(strike), "Strike should schedule disposal after impact");
  }

  @Test
  void impactSchedulesDisposeAsItFades() throws Exception {
    Entity impactEntity = new Entity();
    PlasmaImpactComponent impactComponent = new PlasmaImpactComponent(0.3f);
    impactEntity.addComponent(impactComponent);
    ServiceLocator.getEntityService().register(impactEntity);

    for (int i = 0; i < 30; i++) {
      impactComponent.update();
    }

    assertTrue(getDisposeFlag(impactComponent), "Impact should schedule disposal after finishing animation");
  }

  private boolean getDisposeFlag(Object component) throws Exception {
    Field field = component.getClass().getDeclaredField("disposeScheduled");
    field.setAccessible(true);
    return field.getBoolean(component);
  }
}
