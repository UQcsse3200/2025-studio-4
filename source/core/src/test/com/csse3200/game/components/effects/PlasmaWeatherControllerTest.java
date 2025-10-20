package com.csse3200.game.components.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.IMapEditor;
import com.csse3200.game.areas.terrain.ITerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class PlasmaWeatherControllerTest {

  private final List<Entity> spawnedEntities = new ArrayList<>();
  private final AtomicReference<Vector2> impactPosition = new AtomicReference<>();
  private PlasmaWeatherController controller;

  @BeforeEach
  void setUpController() {
    ServiceLocator.registerRenderService(new RenderService());
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerTimeSource(new GameTime());
    Graphics graphics = mock(Graphics.class);
    when(graphics.getDeltaTime()).thenReturn(0.1f);
    Gdx.graphics = graphics;

    spawnedEntities.clear();
    impactPosition.set(null);
    BiConsumer<Entity, Vector2> spawner = (entity, position) -> {
      entity.setPosition(position);
      spawnedEntities.add(entity);
      ServiceLocator.getEntityService().register(entity);
    };
    controller = new PlasmaWeatherController(
        spawner,
        new StubMapEditor(),
        new StubTerrainComponent(),
        impactPosition::set);
  }

  @Test
  void switchesToPlasmaAndQueuesStrike() throws Exception {
    setPrivateField(controller, "weatherDuration", 0.5f);
    setPrivateField(controller, "weatherElapsed", 0.5f);

    controller.update(0.1f);

    assertEquals("Plasma Storm", controller.getCurrentWeatherLabel());
    assertTrue(controller.isPlasmaActive());

    setPrivateField(controller, "strikeCooldown", 0f);
    controller.update(0.1f);

    assertEquals(1, spawnedEntities.size());
    assertNotNull(spawnedEntities.get(0).getComponent(PlasmaWarningComponent.class));
  }

  @Test
  void pendingStrikeSpawnsImpact() throws Exception {
    setPrivateField(controller, "currentWeather", enumValue("PLASMA"));
    setPrivateField(controller, "strikeCooldown", 0f);
    controller.update(0.1f);

    assertEquals(1, spawnedEntities.size());

    List<?> pending = getPendingStrikes();
    assertFalse(pending.isEmpty());

    Object pendingStrike = pending.get(0);
    Field timerField = pendingStrike.getClass().getDeclaredField("timer");
    timerField.setAccessible(true);
    timerField.setFloat(pendingStrike, 0f);

    controller.update(1f);

    assertEquals(2, spawnedEntities.size());
    Entity strikeEntity =
        spawnedEntities.stream()
            .filter(e -> e.getComponent(PlasmaStrikeComponent.class) != null)
            .findFirst()
            .orElse(null);
    assertNotNull(strikeEntity);

    PlasmaStrikeComponent strike = strikeEntity.getComponent(PlasmaStrikeComponent.class);
    assertNotNull(strike);

    ServiceLocator.registerRenderService(new RenderService());
    ServiceLocator.registerTimeSource(new GameTime());
    Graphics mockGraphics = mock(Graphics.class);
    when(mockGraphics.getDeltaTime()).thenReturn(1f);
    Gdx.graphics = mockGraphics;

    strike.create();
    for (int i = 0; i < 10 && impactPosition.get() == null; i++) {
      strike.update();
    }

    assertEquals(new Vector2(4f, 4f), impactPosition.get());
  }

  @Test
  void switchingBackToSunnyClearsPendingWarnings() throws Exception {
    setPrivateField(controller, "currentWeather", enumValue("PLASMA"));
    setPrivateField(controller, "strikeCooldown", 0f);
    controller.update(0.1f);
    assertFalse(getPendingStrikes().isEmpty());

    setPrivateField(controller, "weatherDuration", 0f);
    setPrivateField(controller, "weatherElapsed", 1f);

    controller.update(0.1f);

    assertEquals("Sunny", controller.getCurrentWeatherLabel());
    assertFalse(controller.isPlasmaActive());
    assertTrue(getPendingStrikes().isEmpty(), "Pending list should be cleared when weather returns to sunny");
  }

  @Test
  void strikeCooldownResetsWithinConfiguredRange() throws Exception {
    setPrivateField(controller, "currentWeather", enumValue("PLASMA"));
    setPrivateField(controller, "strikeCooldown", 0f);

    controller.update(0.1f);

    float cooldown = getFloatField(controller, "strikeCooldown");
    assertTrue(cooldown >= 2f && cooldown <= 4f, "Cooldown should randomise between 2 and 4 seconds");
  }

  @Test
  void chooseTargetFallsBackToTerrainWhenNoWaypoints() throws Exception {
    PlasmaWeatherController terrainOnlyController =
        new PlasmaWeatherController(
            (entity, pos) -> {},
            new EmptyMapEditor(),
            new StubTerrainComponent(),
            pos -> {});

    Vector2 candidate = invokeChooseTarget(terrainOnlyController);
    assertNotNull(candidate);
    assertTrue(candidate.x >= 0 && candidate.x < 10);
    assertTrue(candidate.y >= 0 && candidate.y < 10);
  }

  private Object enumValue(String name) throws Exception {
    Class<?> weatherType = Class.forName(
        "com.csse3200.game.components.effects.PlasmaWeatherController$WeatherType");
    return Enum.valueOf((Class<Enum>) weatherType.asSubclass(Enum.class), name);
  }

  private List<?> getPendingStrikes() throws Exception {
    Field pendingField = controller.getClass().getDeclaredField("pending");
    pendingField.setAccessible(true);
    return (List<?>) pendingField.get(controller);
  }

  private float getFloatField(Object target, String name) throws Exception {
    Field f = target.getClass().getDeclaredField(name);
    f.setAccessible(true);
    return f.getFloat(target);
  }

  private Vector2 invokeChooseTarget(PlasmaWeatherController targetController) throws Exception {
    Method method = targetController.getClass().getDeclaredMethod("chooseTarget");
    method.setAccessible(true);
    return (Vector2) method.invoke(targetController);
  }

  private static void setPrivateField(Object target, String field, Object value) throws Exception {
    Field f = target.getClass().getDeclaredField(field);
    f.setAccessible(true);
    f.set(target, value);
  }

  private static class StubMapEditor implements IMapEditor {
    private final List<Entity> waypoints;

    StubMapEditor() {
      Entity waypoint = new Entity();
      waypoint.setPosition(4f, 4f);
      this.waypoints = Collections.singletonList(waypoint);
    }

    @Override
    public Map<String, GridPoint2> getInvalidTiles() {
      return Collections.emptyMap();
    }

    @Override
    public List<GridPoint2> getWaterTiles() {
      return Collections.emptyList();
    }

    @Override
    public void registerBarrierCoords(int[][] coords) {
    }

    @Override
    public void registerSnowTreeCoords(int[][] coords) {
    }

    @Override
    public void generateEnemyPath() {
    }

    @Override
    public List<Entity> getWaypointList() {
      return waypoints;
    }

    @Override
    public List<GridPoint2> getSlowZoneTiles() {
      return Collections.emptyList();
    }

    @Override
    public void setPathLayerOpacity(float opacity) {}
  }

  private static class StubTerrainComponent implements ITerrainComponent {
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
      return new GridPoint2(10, 10);
    }

    @Override
    public com.badlogic.gdx.maps.tiled.TiledMap getMap() {
      return null;
    }
  }

  private static class EmptyMapEditor implements IMapEditor {
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
      return Collections.emptyList();
    }

    @Override
    public List<GridPoint2> getSlowZoneTiles() {
      return Collections.emptyList();
    }

    @Override
    public void setPathLayerOpacity(float opacity) {}
  }
}
