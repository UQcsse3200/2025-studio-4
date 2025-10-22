package com.csse3200.game.components.hero.samurai;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.SamuraiConfig;

/**
 * Syncs the sword's gameplay stats with the Samurai's level.
 * <p>
 * Listens to the owner's <b>"upgraded"</b> event and updates this entity's
 * {@link CombatStatsComponent} (e.g., base attack) based on values provided in
 * {@link SamuraiConfig#swordDamageByLevel}. Applies level 1 on create.
 */
public class SwordLevelSyncComponent extends Component {
  /** Samurai (owner) entity that emits level-up events. */
  private final Entity owner;
  /** Per-level tuning for the sword (damage tables, etc.). */
  private final SamuraiConfig cfg;

  /** Local stats to update on level change (e.g., base attack). */
  private CombatStatsComponent stats;
  /** Optional: present if sword uses touch/collision damage (not modified here). */
  private TouchAttackComponent touch;

  public SwordLevelSyncComponent(Entity owner, SamuraiConfig cfg) {
    this.owner = owner;
    this.cfg = cfg;
  }

  @Override
  public void create() {
    stats = entity.getComponent(CombatStatsComponent.class);
    touch = entity.getComponent(TouchAttackComponent.class);

    // Apply level-1 values on startup.
    applyForLevel(1);

    // React to Samurai level-ups by refreshing sword stats.
    if (owner != null) {
      owner.getEvents().addListener("upgraded",
              (Integer level, CurrencyType t, Integer cost) -> applyForLevel(level));
    }
  }

  /** Apply per-level values (currently: base attack) to local components. */
  private void applyForLevel(int level) {
    if (stats != null) {
      int dmg = pickByLevel(cfg.swordDamageByLevel, level, /* fallback */ stats.getBaseAttack());
      stats.setBaseAttack(dmg);
    }
    // If needed, add similar per-level updates for TouchAttackComponent here.
  }

  /**
   * Pick an int value from a 1-indexed level table.
   * Clamps out-of-range levels to the last entry; returns fallback if table is empty.
   */
  private static int pickByLevel(int[] arr, int level, int fallback) {
    if (arr == null || arr.length == 0) return fallback;
    int idx = Math.max(0, Math.min(level - 1, arr.length - 1));
    return arr[idx];
  }

  /**
   * Pick a float value from a 1-indexed level table.
   * Clamps out-of-range levels to the last entry; returns fallback if table is empty.
   */
  private static float pickByLevel(float[] arr, int level, float fallback) {
    if (arr == null || arr.length == 0) return fallback;
    int idx = Math.max(0, Math.min(level - 1, arr.length - 1));
    return arr[idx];
  }
}
