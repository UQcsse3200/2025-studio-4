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

/**
 * Controls plasma weather effects in the game world.
 * 
 * <p>This controller manages alternating weather states between sunny and plasma storm conditions.
 * During plasma storms, it generates warning markers and plasma strikes that can impact the terrain
 * and entities. The weather cycles automatically with random durations to create dynamic gameplay.</p>
 * 
 * <p>The plasma strikes target random positions on the map, prioritizing waypoint locations
 * when available. Each strike follows a sequence: warning marker → plasma bolt → impact effect.</p>
 * 
 * @author Team1
 * @since sprint 4
 */
public class PlasmaWeatherController {
  /** Weather states for the controller */
  private enum WeatherType { SUNNY, PLASMA }

  /**
   * Represents a plasma strike that is pending execution.
   * Contains the target position, countdown timer, and warning entity.
   */
  private static class PendingStrike {
    /** Target position for the plasma strike */
    final Vector2 position;
    /** Time remaining before the strike occurs */
    float timer;
    /** Warning entity displayed at the target location */
    final Entity warning;

    /**
     * Creates a new pending strike.
     * 
     * @param position target position for the strike
     * @param timer time remaining before strike occurs
     * @param warning warning entity to display
     */
    PendingStrike(Vector2 position, float timer, Entity warning) {
      this.position = position;
      this.timer = timer;
      this.warning = warning;
    }
  }

  /** Function to spawn entities at specific positions */
  private final BiConsumer<Entity, Vector2> spawner;
  /** Map editor for accessing waypoint information */
  private final IMapEditor mapEditor;
  /** Terrain component for map bounds and positioning */
  private final ITerrainComponent terrain;
  /** Callback function for when plasma strikes impact */
  private final Consumer<Vector2> impactListener;
  /** List of plasma strikes waiting to be executed */
  private final List<PendingStrike> pending = new ArrayList<>();
  /** Current weather state */
  private WeatherType currentWeather = WeatherType.SUNNY;
  /** Time elapsed in current weather state */
  private float weatherElapsed = 0f;
  /** Duration of current weather state */
  private float weatherDuration = MathUtils.random(25f, 40f);
  /** Cooldown timer for spawning new strikes */
  private float strikeCooldown = 0f;

  /**
   * Creates a new plasma weather controller.
   * 
   * @param spawner function to spawn entities at positions
   * @param mapEditor map editor for waypoint access
   * @param terrain terrain component for map bounds
   * @param impactListener callback for plasma impact events
   */
  public PlasmaWeatherController(BiConsumer<Entity, Vector2> spawner, IMapEditor mapEditor, ITerrainComponent terrain, Consumer<Vector2> impactListener) {
    this.spawner = spawner;
    this.mapEditor = mapEditor;
    this.terrain = terrain;
    this.impactListener = impactListener;
  }

  /**
   * Updates the weather controller state.
   * 
   * <p>This method should be called every frame to update weather transitions,
   * manage pending strikes, and spawn new plasma effects during storms.</p>
   * 
   * @param delta time elapsed since last update in seconds
   */
  public void update(float delta) {
    weatherElapsed += delta;
    if (weatherElapsed >= weatherDuration) {
      switchWeather(currentWeather == WeatherType.SUNNY ? WeatherType.PLASMA : WeatherType.SUNNY);
    }
    if (currentWeather == WeatherType.PLASMA) {
      strikeCooldown -= delta;
      if (strikeCooldown <= 0f) {
        queueStrike();
        strikeCooldown = MathUtils.random(4f, 5f);
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

  /**
   * Switches to a new weather state.
   * 
   * @param next the weather type to switch to
   */
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

  /**
   * Queues a new plasma strike for execution.
   * Creates a warning marker and schedules the strike.
   */
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

  /**
   * Spawns a plasma strike at the specified position.
   * 
   * @param position target position for the strike
   */
  private void spawnStrike(Vector2 position) {
    Entity strike = new Entity();
    float startHeight = 8f;
    strike.addComponent(new PlasmaStrikeComponent(position.cpy(), startHeight, 6f, this::impact));
    spawner.accept(strike, new Vector2(position.x, position.y + startHeight));
  }

  /**
   * Handles plasma impact at the specified position.
   * Creates impact effects and notifies the impact listener.
   * 
   * @param position position where the plasma struck
   */
  private void impact(Vector2 position) {
    impactListener.accept(position);
    Entity impact = new Entity();
    impact.addComponent(new PlasmaImpactComponent(0.5f));
    spawner.accept(impact, position.cpy());
  }

  /**
   * Chooses a target position for plasma strikes.
   * Targets are selected uniformly at random from the current terrain bounds.
   *
   * @return target position for the strike, or null if no valid position found
   */
  private Vector2 chooseTarget() {
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

  /**
   * Gets the current weather state as a display label.
   * 
   * @return human-readable weather description
   */
  public String getCurrentWeatherLabel() {
    return currentWeather == WeatherType.PLASMA ? "Plasma Storm" : "Sunny";
  }

  /**
   * Checks if plasma storm weather is currently active.
   * 
   * @return true if plasma storm is active, false if sunny
   */
  public boolean isPlasmaActive() {
    return currentWeather == WeatherType.PLASMA;
  }
}
