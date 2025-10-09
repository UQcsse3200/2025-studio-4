package com.csse3200.game.components.hero.samurai;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.SamuraiConfig;

public class SwordLevelSyncComponent extends Component {
  private final Entity owner;    // 武士本体
  private final SamuraiConfig cfg;

  private CombatStatsComponent stats;
  private TouchAttackComponent touch;

  public SwordLevelSyncComponent(Entity owner, SamuraiConfig cfg) {
    this.owner = owner;
    this.cfg = cfg;
  }

  @Override
  public void create() {
    stats = entity.getComponent(CombatStatsComponent.class);
    touch = entity.getComponent(TouchAttackComponent.class);

    // 初始按1级应用
    applyForLevel(1);

    if (owner != null) {
      owner.getEvents().addListener("upgraded",
          (Integer level, CurrencyType t, Integer cost) -> applyForLevel(level));
    }
  }

  private void applyForLevel(int level) {
    if (stats != null) {
      int dmg = pickByLevel(cfg.swordDamageByLevel, level, /*fallback*/ stats.getBaseAttack());
      stats.setBaseAttack(dmg);
    }
  }

  // level 从 1 开始；索引越界时使用最后一个
  private static int pickByLevel(int[] arr, int level, int fallback) {
    if (arr == null || arr.length == 0) return fallback;
    int idx = Math.max(0, Math.min(level - 1, arr.length - 1));
    return arr[idx];
  }

  private static float pickByLevel(float[] arr, int level, float fallback) {
    if (arr == null || arr.length == 0) return fallback;
    int idx = Math.max(0, Math.min(level - 1, arr.length - 1));
    return arr[idx];
  }
}
