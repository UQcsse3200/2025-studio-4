package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.areas.IMapEditor;
import com.csse3200.game.areas.terrain.ITerrainComponent;
import com.csse3200.game.components.effects.PlasmaWeatherController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class WeatherStatusDisplayTest {

  @Test
  void updatesLabelWhenWeatherChanges() throws Exception {
    Graphics graphics = mock(Graphics.class);
    when(graphics.getWidth()).thenReturn(800);
    when(graphics.getHeight()).thenReturn(600);
    when(graphics.getDeltaTime()).thenReturn(0.016f);
    Gdx.graphics = graphics;

    RenderService renderService = new RenderService();
    Stage stage = mock(Stage.class);
    doAnswer(invocation -> null).when(stage).addActor(any());
    renderService.setStage(stage);
    ServiceLocator.registerRenderService(renderService);
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerTimeSource(new GameTime());

    PlasmaWeatherController controller =
        new PlasmaWeatherController((entity, position) -> {}, new TestMapEditor(), new TestTerrain(), pos -> {});

    WeatherStatusDisplay display = new WeatherStatusDisplay(controller);
    Entity uiEntity = new Entity().addComponent(display);
    ServiceLocator.getEntityService().register(uiEntity);

    assertEquals("Weather: Sunny", currentLabel(display).getText().toString());

    setControllerWeather(controller, "PLASMA");
    display.update();

    assertEquals("Weather: Plasma Storm", currentLabel(display).getText().toString());

    renderService.dispose();
  }

  private void setControllerWeather(PlasmaWeatherController controller, String value) throws Exception {
    Class<?> weatherType = Class.forName(
        "com.csse3200.game.components.effects.PlasmaWeatherController$WeatherType");
    Enum<?> enumValue = Enum.valueOf((Class<Enum>) weatherType.asSubclass(Enum.class), value);
    Field field = controller.getClass().getDeclaredField("currentWeather");
    field.setAccessible(true);
    field.set(controller, enumValue);
  }

  private Label currentLabel(WeatherStatusDisplay display) throws Exception {
    Field field = WeatherStatusDisplay.class.getDeclaredField("statusLabel");
    field.setAccessible(true);
    return (Label) field.get(display);
  }

  private static class TestMapEditor implements IMapEditor {
    @Override
    public Map<String, GridPoint2> getInvalidTiles() {
      return Collections.emptyMap();
    }

    @Override
    public List<GridPoint2> getWaterTiles() {
      return Collections.emptyList();
    }

    @Override
    public void registerBarrierCoords(int[][] coords) {}

    @Override
    public void registerSnowTreeCoords(int[][] coords) {}

    @Override
    public void generateEnemyPath() {}

    @Override
    public List<Entity> getWaypointList() {
      return new ArrayList<>();
    }

    @Override
    public List<GridPoint2> getSlowZoneTiles() {
      return Collections.emptyList();
    }
  }

  private static class TestTerrain implements ITerrainComponent {
    @Override
    public Vector2 tileToWorldPosition(GridPoint2 tilePos) {
      return new Vector2(tilePos.x, tilePos.y);
    }

    @Override
    public Vector2 tileToWorldPosition(int x, int y) {
      return new Vector2(x, y);
    }

    @Override
    public float getTileSize() {
      return 1f;
    }

    @Override
    public GridPoint2 getMapBounds(int layer) {
      return new GridPoint2(1, 1);
    }

    @Override
    public com.badlogic.gdx.maps.tiled.TiledMap getMap() {
      return null;
    }
  }
}

