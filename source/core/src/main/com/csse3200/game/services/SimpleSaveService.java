package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.components.towers.TowerComponent;
import com.csse3200.game.components.towers.TowerStatsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SimpleSaveService {
  private static final Logger logger = LoggerFactory.getLogger(SimpleSaveService.class);
  private static final String SAVE_FILE = "saves/game_save.json";

  private final Json json;
  private final EntityService entityService;
  private SaveData pending;

  public SimpleSaveService(EntityService entityService) {
    this.entityService = entityService;
    this.json = new Json();
    this.json.setTypeName(null); //removes class name in json for hashmap
    this.json.setOutputType(JsonWriter.OutputType.json);
  }

  public boolean save() {
    try {
      SaveData data = collect();
      FileHandle dir = Gdx.files.absolute("./saves");
      if (!dir.exists()) dir.mkdirs();
      Gdx.files.absolute("./" + SAVE_FILE).writeString(json.toJson(data), false);
      logger.info("Saved to ./{} (players={}, towers={}, enemies={})", SAVE_FILE,
              data.player != null, data.towers.size(), data.enemies.size());
      return true;
    } catch (Exception e) {
      logger.error("Save failed", e);
      return false;
    }
  }

  /** Save to a specific filename under saves/, using provided name (without extension or with). */
  public boolean saveAs(String name) {
    try {
      SaveData data = collect();
      String safe = sanitize(name);
      if (safe.endsWith(".json")) {
        safe = safe.substring(0, safe.length() - 5);
      }
      FileHandle dir = Gdx.files.absolute("./saves");
      if (!dir.exists()) dir.mkdirs();
      String target = "saves/" + safe + ".json";
      Gdx.files.absolute("./" + target).writeString(json.toJson(data), false);
      logger.info("Saved to ./{} (players={}, towers={}, enemies={})", target,
              data.player != null, data.towers.size(), data.enemies.size());
      return true;
    } catch (Exception e) {
      logger.error("SaveAs failed", e);
      return false;
    }
  }

  public boolean loadToPending() {
    try {
      FileHandle fh = Gdx.files.absolute("./" + SAVE_FILE);
      if (!fh.exists()) fh = Gdx.files.local(SAVE_FILE);
      if (!fh.exists()) return false;
      pending = json.fromJson(SaveData.class, fh.readString());
      return true;
    } catch (Exception e) {
      logger.error("Load failed", e);
      return false;
    }
  }

  /** Load a specific save file by name (with or without .json) into pending. */
  public boolean loadToPending(String name) {
    try {
      String safe = sanitize(name);
      if (safe.endsWith(".json")) {
        safe = safe.substring(0, safe.length() - 5);
      }
      String target = "saves/" + safe + ".json";
      FileHandle fh = Gdx.files.absolute("./" + target);
      if (!fh.exists()) fh = Gdx.files.local(target);
      if (!fh.exists()) {
        logger.warn("Named save not found: {}", target);
        return false;
      }
      pending = json.fromJson(SaveData.class, fh.readString());
      return true;
    } catch (Exception e) {
      logger.error("Load (named) failed", e);
      return false;
    }
  }

  public boolean applyPendingRestore() {
    if (pending == null) return false;
    try {
      restore(pending, null);
      pending = null;
      return true;
    } catch (Exception e) {
      logger.error("Apply restore failed", e);
      return false;
    }
  }

  /**
   * Apply pending restore but using provided canonical waypoints from the current game area.
   * This avoids spawning with dummy waypoints and prevents a frame of drifting to origin.
   */
  public boolean applyPendingRestoreWithWaypoints(java.util.List<Entity> canonicalWaypoints) {
    if (pending == null) return false;
    try {
      restore(pending, canonicalWaypoints);
      pending = null;
      return true;
    } catch (Exception e) {
      logger.error("Apply restore (with waypoints) failed", e);
      return false;
    }
  }

  private SaveData collect() {
    SaveData data = new SaveData();

    // player
    Entity player = findPlayer();
    if (player != null) {
      data.player = new SaveData.Player();
      data.player.pos = player.getPosition();
      var combat = player.getComponent(com.csse3200.game.components.PlayerCombatStatsComponent.class);
      if (combat != null) data.player.hp = combat.getHealth();
      // Prefer currency manager (team economy) if present; fallback to InventoryComponent
      var cm = player.getComponent(com.csse3200.game.components.currencysystem.CurrencyManagerComponent.class);
      if (cm != null) {
        data.player.gold = cm.getCurrencyAmount(
            com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType.METAL_SCRAP);
      } else {
        var wallet = player.getComponent(com.csse3200.game.components.player.InventoryComponent.class);
        if (wallet != null) data.player.gold = wallet.getGold();
      }
    }

    for (Entity e : entityService.getEntities()) {
      if (e.getComponent(TowerComponent.class) != null) {
        SaveData.Tower t = new SaveData.Tower();
        t.pos = e.getPosition();
        var tc = e.getComponent(TowerComponent.class);
        t.type = tc != null ? tc.getType() : "bone";
        var ts = e.getComponent(TowerStatsComponent.class);
        if (ts != null) {
          t.hp = ts.getHealth();
          t.cd = ts.getAttackCooldown();
        }
        data.towers.add(t);
      }

      // enemy snapshot
      if (e.getComponent(com.csse3200.game.components.enemy.EnemyTypeComponent.class) != null) {
        var combat = e.getComponent(com.csse3200.game.components.CombatStatsComponent.class);
        if (combat == null) continue;
        SaveData.Enemy en = new SaveData.Enemy();
        en.pos = e.getPosition();
        en.hp = combat.getHealth();
        var et = e.getComponent(com.csse3200.game.components.enemy.EnemyTypeComponent.class);
        en.type = et != null ? et.getType() : "grunt";
        data.enemies.add(en);
      }
    }

    GameStateService gs = ServiceLocator.getGameStateService();

    if (gs != null) {
        data.heroUnlocks = gs.getHeroUnlocks();
        data.stars = gs.getStars();
        data.selectedHero = gs.getSelectedHero();
    }

    return data;
  }

  private void restore(SaveData data) {
    restore(data, null);
  }

  /** Restore game state. If waypoints provided, enemies will be created bound to these. */
  private void restore(SaveData data, java.util.List<Entity> canonicalWaypoints) {
      // restore player
    Entity player = findPlayer();
    boolean newPlayer = false;
    if (player == null) {
      player = com.csse3200.game.entities.factories.PlayerFactory.createPlayer();
      newPlayer = true;
    }
    if (data.player != null) {
      player.setPosition(validPos(data.player.pos));
      var combat = player.getComponent(com.csse3200.game.components.PlayerCombatStatsComponent.class);
      if (combat != null) combat.setHealth(data.player.hp);
      var cm = player.getComponent(com.csse3200.game.components.currencysystem.CurrencyManagerComponent.class);
      if (cm != null) {
        cm.setCurrencyAmount(
            com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType.METAL_SCRAP,
            data.player.gold);
      } else {
        var wallet = player.getComponent(com.csse3200.game.components.player.InventoryComponent.class);
        if (wallet != null) wallet.setGold(data.player.gold);
      }
    }
    if (newPlayer) entityService.register(player);

    // clear current towers/enemies
    List<Entity> toRemove = new ArrayList<>();
    for (Entity e : entityService.getEntities()) {
      if (e.getComponent(TowerComponent.class) != null
              || e.getComponent(com.csse3200.game.components.enemy.EnemyTypeComponent.class) != null) {
        toRemove.add(e);
      }
    }
    for (Entity e : toRemove) entityService.unregister(e);

    // restore towers
    for (SaveData.Tower t : data.towers) {
      var type = t.type == null ? "bone" : t.type;
      Entity tower = null;
      var defaultCurrency = com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType.METAL_SCRAP;
        switch (type) {
            case "bone":
                tower = com.csse3200.game.entities.factories.TowerFactory.createBoneTower();
                break;
            case "dino":
                tower = com.csse3200.game.entities.factories.TowerFactory.createDinoTower();
                break;
            case "cavemen":
                tower = com.csse3200.game.entities.factories.TowerFactory.createCavemenTower();
                break;
            default:
                tower = com.csse3200.game.entities.factories.TowerFactory.createBoneTower();
        }

        if (tower != null) {
        tower.setPosition(t.pos);
        var ts = tower.getComponent(TowerStatsComponent.class);
        if (ts != null) {
          ts.setHealth(t.hp);
          ts.setAttackCooldown(t.cd);
        }
        entityService.register(tower);
      }
    }

    // restore enemies. Prefer canonical waypoints from area to avoid dummy (0,0) drift
    List<Entity> wps = canonicalWaypoints;
    if (wps == null || wps.isEmpty()) {
      // fallback: use a waypoint at each enemy's position to avoid drifting; will be rebound later
      wps = null; // we will handle per-enemy below
    }
    for (SaveData.Enemy en : data.enemies) {
      List<Entity> useWps = wps;
      if (useWps == null) {
        useWps = new ArrayList<>();
        Entity wpAtPos = new Entity();
        wpAtPos.setPosition(en.pos);
        useWps.add(wpAtPos);
      }

      Entity enemy;
      if (canonicalWaypoints != null && !canonicalWaypoints.isEmpty()) {
        int startIdx = computeForwardIndex(canonicalWaypoints, en.pos);
        enemy = createEnemyAtIndex(en.type, player, canonicalWaypoints, startIdx);
      } else {
        enemy = createEnemy(en.type, player, useWps);
      }
      if (enemy != null) {
        enemy.setPosition(en.pos);
        var combat = enemy.getComponent(com.csse3200.game.components.CombatStatsComponent.class);
        if (combat != null) combat.setHealth(en.hp);
        // If we had canonical waypoints, snap progression before first update, then register
        try {
          if (canonicalWaypoints != null && !canonicalWaypoints.isEmpty()) {
            var wc = enemy.getComponent(com.csse3200.game.components.enemy.WaypointComponent.class);
            if (wc != null) {
              wc.rebindWaypointsAndSnap(canonicalWaypoints, en.pos);
            }
          }
        } catch (Throwable ignored) {}

        entityService.register(enemy);
        // Ensure physics body matches restored position and has no residual velocity
        try {
          var phys = enemy.getComponent(com.csse3200.game.physics.components.PhysicsComponent.class);
          if (phys != null && phys.getBody() != null) {
            // If snapped, keep at position; movement system will steer to next target
            phys.getBody().setTransform(enemy.getPosition(), 0f);
            phys.getBody().setLinearVelocity(0f, 0f);
            phys.getBody().setAngularVelocity(0f);
          }
        } catch (Throwable ignored) {}
      }
    }

    //restore game state
    GameStateService gs = new GameStateService();
    ServiceLocator.registerGameStateService(gs);

    if (!data.heroUnlocks.isEmpty()) {
        gs.setHeroUnlocks(data.heroUnlocks);
    }

    gs.setStars(data.stars);
    gs.setSelectedHero(data.selectedHero);

  }

  private Entity createEnemy(String type, Entity player, List<Entity> waypoints) {
    var diff = com.csse3200.game.utils.Difficulty.EASY;
    switch (type) {
      case "drone":
        return com.csse3200.game.entities.factories.DroneEnemyFactory.createDroneEnemy(waypoints, player, diff);
      case "tank":
        return com.csse3200.game.entities.factories.TankEnemyFactory.createTankEnemy(waypoints, player, diff);
      case "boss":
        return com.csse3200.game.entities.factories.BossEnemyFactory.createBossEnemy(waypoints, player, diff);
      case "grunt":
      default:
        return com.csse3200.game.entities.factories.GruntEnemyFactory.createGruntEnemy(waypoints, player, diff);
    }
  }

  // Overload: create enemy starting from a specific waypoint index
  private Entity createEnemyAtIndex(String type, Entity player, List<Entity> waypoints, int startIdx) {
    var diff = com.csse3200.game.utils.Difficulty.EASY;
    switch (type) {
      case "drone":
        return com.csse3200.game.entities.factories.DroneEnemyFactory.createDroneEnemy(waypoints, player, diff, startIdx);
      case "tank":
        return com.csse3200.game.entities.factories.TankEnemyFactory.createTankEnemy(waypoints, player, diff, startIdx);
      case "boss":
        return com.csse3200.game.entities.factories.BossEnemyFactory.createBossEnemy(waypoints, player, diff, startIdx);
      case "grunt":
      default:
        return com.csse3200.game.entities.factories.GruntEnemyFactory.createGruntEnemy(waypoints, player, diff, startIdx);
    }
  }

  // Choose a forward index: nearest waypoint, but if next is closer, pick next to avoid backtracking
  private int computeForwardIndex(List<Entity> waypoints, Vector2 pos) {
    int nearest = 0;
    float best = Float.MAX_VALUE;
    for (int i = 0; i < waypoints.size(); i++) {
      Entity wp = waypoints.get(i);
      if (wp == null) continue;
      float d2 = wp.getPosition().dst2(pos);
      if (d2 < best) {
        best = d2;
        nearest = i;
      }
    }
    if (nearest + 1 < waypoints.size()) {
      float dCurr = waypoints.get(nearest).getPosition().dst2(pos);
      float dNext = waypoints.get(nearest + 1).getPosition().dst2(pos);
      if (dNext <= dCurr) return nearest + 1;
    }
    return nearest;
  }

  private Entity findPlayer() {
    for (Entity e : entityService.getEntities()) {
      if (e.getComponent(com.csse3200.game.components.player.PlayerActions.class) != null) return e;
    }
    return null;
  }

  private Vector2 validPos(Vector2 p) {
    if (p == null) return new Vector2(7.5f, 7.5f);
    float x = Math.max(0f, Math.min(15f, p.x));
    float y = Math.max(0f, Math.min(15f, p.y));
    return new Vector2(x, y);
  }

  private static String sanitize(String name) {
    if (name == null) return "save";
    return name.replaceAll("[^a-zA-Z0-9 _-]", "_");
  }

  // --- DTO ---
  public static class SaveData {
    public Player player;
    public List<Tower> towers = new ArrayList<>();
    public List<Enemy> enemies = new ArrayList<>();

    public Map<GameStateService.HeroType, Boolean> heroUnlocks = new HashMap<>();
    public GameStateService.HeroType selectedHero = GameStateService.HeroType.HERO;
    public int stars = 0;

    public static class Player { public Vector2 pos; public int hp; public int gold; }
    public static class Tower { public String type; public Vector2 pos; public int hp; public float cd; }
    public static class Enemy { public String type; public Vector2 pos; public int hp; }
  }
}


