package com.csse3200.game.components.hero;

import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 专测 Ultimate 期间“伤害倍数”的外部可观察行为（通过事件）：
 * - 免费激活路径（无 upgrade）
 * - 钱包不足路径（有 upgrade + wallet.canAffordAndSpendCurrency=false）
 * - 到期恢复路径（强制 endAtMs 到期后 update() 恢复 1.0）
 */
class HeroUltimateComponentDoubleDamageTest {

  private Entity entity;
  private HeroUltimateComponent ult;

  @BeforeEach
  void setUp() {
    entity = new Entity();
    ult = new HeroUltimateComponent();
    entity.addComponent(ult);
    entity.create(); // 让 ult.create() 注册事件监听
  }

  /** 情况A：没有 HeroUpgradeComponent（免费激活） → 必须加倍并置 state=true */
  @Test
  void activateWithoutUpgrade_ShouldMultiplyDamage() {
    AtomicReference<Float> attackMul = new AtomicReference<>(null);
    AtomicReference<Boolean> state = new AtomicReference<>(null);

    entity.getEvents().addListener("attack.multiplier", (Float v) -> attackMul.set(v));
    entity.getEvents().addListener("ultimate.state",   (Boolean v) -> state.set(v));

    // 触发
    entity.getEvents().trigger("ultimate.request");

    assertEquals(2.0f, attackMul.get(), 1e-6, "激活时应广播 attack.multiplier = 2.0");
    assertEquals(Boolean.TRUE, state.get(), "激活时应广播 ultimate.state = true");
  }

  /** 情况B：有升级组件和钱包，但钱包不足 → 不应加倍，并广播失败 */
  @Test
  void walletCantAfford_ShouldNotActivate() {
    // 1) mock 升级组件与钱包
    HeroUpgradeComponent upgrade = mock(HeroUpgradeComponent.class, RETURNS_DEEP_STUBS);
    CurrencyManagerComponent wallet = mock(CurrencyManagerComponent.class);
    when(upgrade.getWallet()).thenReturn(wallet);
    when(wallet.canAffordAndSpendCurrency(Map.of(CurrencyType.METAL_SCRAP, 2)))
        .thenReturn(false);

    // 2) 关键顺序：先把 upgrade 加到实体，再把 ultimate 加进去，然后再 create()
    Entity e = new Entity();
    e.addComponent(upgrade);
    HeroUltimateComponent u = new HeroUltimateComponent();
    e.addComponent(u);
    e.create(); // 这一步后，u.create() 会缓存到 upgrade（非 null）

    AtomicBoolean failed = new AtomicBoolean(false);
    AtomicReference<Float> attackMul = new AtomicReference<>(null);

    e.getEvents().addListener("ultimate.failed", (String msg) -> failed.set(true));
    e.getEvents().addListener("attack.multiplier", (Float v) -> attackMul.set(v));

    // 3) 触发：由于钱包不足，应失败且不加倍
    e.getEvents().trigger("ultimate.request");

    assertTrue(failed.get(), "钱包不足时应广播 ultimate.failed");
    assertNull(attackMul.get(), "钱包不足时不应触发 attack.multiplier");
  }

  /** 情况C：钱包足够 → 激活倍伤；然后强制到期 → 恢复为1.0 与 state=false，remaining=0 */
  @Test
  void walletOk_ActivateThenExpire_ShouldRestoreDamage() throws Exception {
    // 1) mock 升级组件和钱包（足够支付）
    HeroUpgradeComponent upgrade = mock(HeroUpgradeComponent.class, RETURNS_DEEP_STUBS);
    CurrencyManagerComponent wallet = mock(CurrencyManagerComponent.class);
    when(upgrade.getWallet()).thenReturn(wallet);
    when(wallet.canAffordAndSpendCurrency(Map.of(CurrencyType.METAL_SCRAP, 2)))
        .thenReturn(true);

    Entity e = new Entity();
    e.addComponent(upgrade);
    HeroUltimateComponent u = new HeroUltimateComponent();
    e.addComponent(u);
    e.create();

    AtomicReference<Float> attackMul = new AtomicReference<>(null);
    AtomicReference<Boolean> state = new AtomicReference<>(null);
    AtomicReference<Float> remaining = new AtomicReference<>(null);

    e.getEvents().addListener("attack.multiplier", (Float v) -> attackMul.set(v));
    e.getEvents().addListener("ultimate.state",   (Boolean v) -> state.set(v));
    e.getEvents().addListener("ultimate.remaining", (Float v) -> remaining.set(v));

    // 2) 激活：应加倍
    e.getEvents().trigger("ultimate.request");
    assertEquals(2.0f, attackMul.get(), 1e-6, "激活时应加倍");
    assertEquals(Boolean.TRUE, state.get(), "激活时应 state=true");

    // 3) 强制让计时器到期：把 endAtMs 设为当前时间
    Field endAt = HeroUltimateComponent.class.getDeclaredField("endAtMs");
    endAt.setAccessible(true);
    endAt.setLong(u, System.currentTimeMillis()); // 立即到期

    // 4) 调用 update()：应恢复为1.0、state=false，并广播 remaining=0
    u.update();

    assertEquals(1.0f, attackMul.get(), 1e-6, "到期后应恢复为1.0");
    assertEquals(Boolean.FALSE, state.get(), "到期后应 state=false");
    assertEquals(0.0f, remaining.get(), 1e-6, "到期后应广播最后一次 remaining=0");
  }
}

