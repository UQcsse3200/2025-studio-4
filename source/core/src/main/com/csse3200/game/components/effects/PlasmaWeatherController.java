package com.csse3200.game.components.effects;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.IMapEditor;
import com.csse3200.game.areas.terrain.ITerrainComponent;
import com.csse3200.game.entities.Entity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PlasmaWeatherController {
  private enum WeatherType { SUNNY, PLASMA }

  private static class PendingStrike {
    final Vector2 position;
    float timer;
    final Entity warning;

    PendingStrike(Vector2 position, float timer, Entity warning) {
      this.position = position;
      this.timer = timer;
      this.warning = warning;
    }
  }

  private final BiConsumer<Entity, Vector2> spawner;
  private final IMapEditor mapEditor;
  private final ITerrainComponent terrain;
  private final Consumer<Vector2> impactListener;
  private final List<PendingStrike> pending = new ArrayList<>();
  private WeatherType currentWeather = WeatherType.SUNNY;
  private float weatherElapsed = 0f;
  private float weatherDuration = MathUtils.random(25f, 40f);
  private float strikeCooldown = 0f;

  public PlasmaWeatherController(BiConsumer<Entity, Vector2> spawner, IMapEditor mapEditor, ITerrainComponent terrain, Consumer<Vector2> impactListener) {
    this.spawner = spawner;
    this.mapEditor = mapEditor;
    this.terrain = terrain;
    this.impactListener = impactListener;
  }

  public void update(float delta) {
    weatherElapsed += delta;
    if (weatherElapsed >= weatherDuration) {
      switchWeather(currentWeather == WeatherType.SUNNY ? WeatherType.PLASMA : WeatherType.SUNNY);
    }
    if (currentWeather == WeatherType.PLASMA) {
      strikeCooldown -= delta;
      if (strikeCooldown <= 0f) {
        queueStrike();
        strikeCooldown = MathUtils.random(2f, 4f);
      }
    }
    Iterator<PendingStrike> iterator = pending.iterator();
    while (iterator.hasNext()) {
      PendingStrike strike = iterator.next();
      strike.timer -= delta;
      if (strike.timer <= 0f) {
        if (strike.warning != null) {
          strike.warning.dispose();
        }
        spawnStrike(strike.position);
        iterator.remove();
      }
    }
  }

  private void switchWeather(WeatherType next) {
    currentWeather = next;
    weatherElapsed = 0f;
    if (currentWeather == WeatherType.SUNNY) {
      weatherDuration = MathUtils.random(25f, 40f);
      for (PendingStrike strike : pending) {
        if (strike.warning != null) {
          strike.warning.dispose();
        }
      }
      pending.clear();
    } else {
      weatherDuration = MathUtils.random(12f, 18f);
      strikeCooldown = 1f;
    }
  }

  private void queueStrike() {
    Vector2 position = chooseTarget();
    if (position == null) {
      return;
    }
    Entity warning = new Entity();
    warning.addComponent(new PlasmaWarningComponent(1.2f, 0.9f));
    spawner.accept(warning, position.cpy());
    pending.add(new PendingStrike(position, 1.2f, warning));
  }

  private void spawnStrike(Vector2 position) {
    Entity strike = new Entity();
    float startHeight = 8f;
    strike.addComponent(new PlasmaStrikeComponent(position.cpy(), startHeight, 6f, this::impact));
    spawner.accept(strike, new Vector2(position.x, position.y + startHeight));
  }

  private void impact(Vector2 position) {
    impactListener.accept(position);
    Entity impact = new Entity();
    impact.addComponent(new PlasmaImpactComponent(0.5f));
    spawner.accept(impact, position.cpy());
  }

  private Vector2 chooseTarget() {
    List<Entity> waypoints = mapEditor != null ? mapEditor.getWaypointList() : null;
    if (waypoints != null && !waypoints.isEmpty()) {
      Entity chosen = waypoints.get(MathUtils.random(waypoints.size() - 1));
      if (chosen != null && chosen.getPosition() != null) {
        return chosen.getPosition().cpy();
      }
    }
    if (terrain == null) {
      return null;
    }
    GridPoint2 bounds = terrain.getMapBounds(0);
    if (bounds == null || bounds.x <= 0 || bounds.y <= 0) {
      return null;
    }
    int x = MathUtils.random(0, bounds.x - 1);
    int y = MathUtils.random(0, bounds.y - 1);
    return terrain.tileToWorldPosition(x, y);
  }

  public String getCurrentWeatherLabel() {
    return currentWeather == WeatherType.PLASMA ? "Plasma Storm" : "Sunny";
  }

  public boolean isPlasmaActive() {
    return currentWeather == WeatherType.PLASMA;
  }
}
