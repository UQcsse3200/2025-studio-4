package com.csse3200.game.components.hero;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 协同测试：HeroUpgradeComponent 与 真实 CurrencyManagerComponent 的联动。
 * 验证：升级会真实扣钱；余额不足/达上限时不扣；事件正确触发；属性增长生效。
 */
public class HeroUpgradeWalletIntegrationTest {

  @BeforeEach
  void setupGdx() {
    // 避免 Gdx.app.log NPE
    Gdx.app = mock(Application.class);
  }

  /** 构建一个带“真钱包”的 player，并完成 create()（会初始化 METAL_SCRAP=500）。 */
  private Entity makePlayerWithRealWallet(CurrencyManagerComponent wallet) {
    Entity player = new Entity().addComponent(wallet);
    player.create(); // 触发 wallet.create() -> 500/0/0 & 监听器注册
    return player;
  }

  /** 构建 hero（带 CombatStats + HeroUpgrade），并注入 player。 */
  private Entity makeHeroAttachedToPlayer(Entity player, CombatStatsComponent stats, HeroUpgradeComponent upgrade) {
    Entity hero = new Entity()
        .addComponent(stats)
        .addComponent(upgrade.attachPlayer(player));
    hero.create();
    return hero;
  }

  /** 便捷：四参 CombatStats（空抗性/弱点）。 */
  private CombatStatsComponent stats(int hp, int atk) {
    return new CombatStatsComponent(hp, atk, DamageTypeConfig.None, DamageTypeConfig.None);
  }

  /** 便捷：一次性扣指定货币（若余额足够）。 */
  private static boolean spend(CurrencyManagerComponent w, CurrencyType t, int amt) {
    Map<CurrencyType, Integer> m = new EnumMap<>(CurrencyType.class);
    m.put(t, amt);
    return w.canAffordAndSpendCurrency(m);
  }

  @Test
  void upgradeTwice_shouldDeductWalletAndGrowStats_andFireUpgradedEvents() {
    // Arrange
    CurrencyManagerComponent wallet = new CurrencyManagerComponent();
    Entity player = makePlayerWithRealWallet(wallet); // METAL_SCRAP=500
    HeroUpgradeComponent upgrade = new HeroUpgradeComponent();
    CombatStatsComponent stats = stats(100, 20);
    Entity hero = makeHeroAttachedToPlayer(player, stats, upgrade);

    // 监听“upgraded”
    AtomicInteger upgradedCount = new AtomicInteger(0);
    AtomicInteger lastLevel = new AtomicInteger(-1);
    AtomicInteger lastCost = new AtomicInteger(-1);
    final CurrencyType[] lastType = new CurrencyType[1];

    hero.getEvents().addListener("upgraded", (Integer level, CurrencyType type, Integer cost) -> {
      upgradedCount.incrementAndGet();
      lastLevel.set(level);
      lastType[0] = type;
      lastCost.set(cost);
    });

    int atk0 = stats.getBaseAttack(); // 20
    int hp0  = stats.getHealth();     // 100
    int w0   = wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP); // 500

    // Act: 1->2（cost = 4）
    hero.getEvents().trigger("requestUpgrade", player);

    // Assert 1
    assertEquals(w0 - 4, wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP));
    assertEquals(atk0 + 10, stats.getBaseAttack());
    assertEquals(hp0 + 20,  stats.getHealth());
    assertEquals(1, upgradedCount.get());
    assertEquals(2, lastLevel.get());
    assertEquals(CurrencyType.METAL_SCRAP, lastType[0]);
    assertEquals(4, lastCost.get());

    // Act: 2->3（cost = 6）
    hero.getEvents().trigger("requestUpgrade", player);

    // Assert 2
    assertEquals(w0 - 4 - 6, wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP));
    assertEquals(atk0 + 20, stats.getBaseAttack());
    assertEquals(hp0 + 40,  stats.getHealth());
    assertEquals(2, upgradedCount.get());
    assertEquals(3, lastLevel.get());
    assertEquals(6, lastCost.get());
  }

  @Test
  void upgradeAtMaxLevel_shouldNotDeductAndShouldFailEvent() {
    // Arrange
    CurrencyManagerComponent wallet = new CurrencyManagerComponent();
    Entity player = makePlayerWithRealWallet(wallet);
    HeroUpgradeComponent upgrade = new HeroUpgradeComponent();
    Entity hero = makeHeroAttachedToPlayer(player, stats(100, 20), upgrade);

    AtomicBoolean failed = new AtomicBoolean(false);
    final String[] reason = new String[1];
    hero.getEvents().addListener("upgradeFailed", (String msg) -> {
      failed.set(true);
      reason[0] = msg;
    });

    // 升两级到 max=3
    hero.getEvents().trigger("requestUpgrade", player); // cost 4
    hero.getEvents().trigger("requestUpgrade", player); // cost 6
    int balanceAtMax = wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP);

    // Act: 再升级一次（应失败且不扣钱）
    failed.set(false); reason[0] = null;
    hero.getEvents().trigger("requestUpgrade", player);

    // Assert
    assertTrue(failed.get());
    assertNotNull(reason[0]);
    assertTrue(reason[0].toLowerCase().contains("max"));
    assertEquals(balanceAtMax, wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP));
  }

  @Test
  void upgradeWhenNotEnoughFunds_shouldNotDeductAndShouldFailEvent() {
    // Arrange
    CurrencyManagerComponent wallet = new CurrencyManagerComponent();
    Entity player = makePlayerWithRealWallet(wallet); // 初始 500
    // 先把钱清零：一次性扣 500（利用真钱包 API）
    assertTrue(spend(wallet, CurrencyType.METAL_SCRAP, 500));
    assertEquals(0, wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP));

    HeroUpgradeComponent upgrade = new HeroUpgradeComponent();
    Entity hero = makeHeroAttachedToPlayer(player, stats(100, 20), upgrade);

    AtomicBoolean failed = new AtomicBoolean(false);
    final String[] reason = new String[1];
    hero.getEvents().addListener("upgradeFailed", (String msg) -> {
      failed.set(true);
      reason[0] = msg;
    });

    int before = wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP);

    // Act: 余额不足去升级（cost=4）
    hero.getEvents().trigger("requestUpgrade", player);

    // Assert
    assertTrue(failed.get());
    assertNotNull(reason[0]);
    assertTrue(reason[0].toLowerCase().contains("not enough"));
    assertEquals(before, wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP)); // 不扣费
    assertEquals(1, upgrade.getLevel()); // 等级没变
  }
}
