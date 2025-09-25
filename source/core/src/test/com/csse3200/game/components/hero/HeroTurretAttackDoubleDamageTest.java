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
 * Unit tests for HeroUltimateComponent focusing on external observable behavior
 * related to the "double damage" effect:
 * - Free activation path (no upgrade component).
 * - Insufficient funds path (upgrade + wallet present but wallet cannot afford).
 * - Expiration path (ultimate ends after duration, damage multiplier restored to 1.0).
 */
class HeroUltimateComponentDoubleDamageTest {

  private Entity entity;
  private HeroUltimateComponent ult;

  @BeforeEach
  void setUp() {
    entity = new Entity();
    ult = new HeroUltimateComponent();
    entity.addComponent(ult);
    entity.create(); // register event listeners
  }

  /** Case A: No HeroUpgradeComponent (free activation) → must broadcast multiplier=2.0 and state=true */
  @Test
  void activateWithoutUpgrade_ShouldMultiplyDamage() {
    AtomicReference<Float> attackMul = new AtomicReference<>(null);
    AtomicReference<Boolean> state = new AtomicReference<>(null);

    entity.getEvents().addListener("attack.multiplier", (Float v) -> attackMul.set(v));
    entity.getEvents().addListener("ultimate.state",   (Boolean v) -> state.set(v));

    // Trigger ultimate activation
    entity.getEvents().trigger("ultimate.request");

    assertEquals(2.0f, attackMul.get(), 1e-6, "On activation should broadcast attack.multiplier = 2.0");
    assertEquals(Boolean.TRUE, state.get(), "On activation should broadcast ultimate.state = true");
  }

  /** Case B: Has upgrade and wallet, but wallet cannot afford → should fail and not multiply damage */
  @Test
  void walletCantAfford_ShouldNotActivate() {
    // 1) Mock upgrade component and wallet
    HeroUpgradeComponent upgrade = mock(HeroUpgradeComponent.class, RETURNS_DEEP_STUBS);
    CurrencyManagerComponent wallet = mock(CurrencyManagerComponent.class);
    when(upgrade.getWallet()).thenReturn(wallet);
    when(wallet.canAffordAndSpendCurrency(Map.of(CurrencyType.METAL_SCRAP, 200)))
            .thenReturn(false);

    // 2) Attach upgrade and ultimate to entity, then call create()
    Entity e = new Entity();
    e.addComponent(upgrade);
    HeroUltimateComponent u = new HeroUltimateComponent();
    e.addComponent(u);
    e.create(); // now u.create() can cache upgrade

    AtomicBoolean failed = new AtomicBoolean(false);
    AtomicReference<Float> attackMul = new AtomicReference<>(null);

    e.getEvents().addListener("ultimate.failed", (String msg) -> failed.set(true));
    e.getEvents().addListener("attack.multiplier", (Float v) -> attackMul.set(v));

    // 3) Trigger ultimate: should fail due to insufficient funds
    e.getEvents().trigger("ultimate.request");

    assertTrue(failed.get(), "When wallet cannot afford, should broadcast ultimate.failed");
    assertNull(attackMul.get(), "When wallet cannot afford, should not broadcast attack.multiplier");
  }

  /** Case C: Wallet can afford → should activate double damage, then expire and restore to 1.0 */
  @Test
  void walletOk_ActivateThenExpire_ShouldRestoreDamage() throws Exception {
    // 1) Mock upgrade and wallet (sufficient funds)
    HeroUpgradeComponent upgrade = mock(HeroUpgradeComponent.class, RETURNS_DEEP_STUBS);
    CurrencyManagerComponent wallet = mock(CurrencyManagerComponent.class);
    when(upgrade.getWallet()).thenReturn(wallet);
    when(wallet.canAffordAndSpendCurrency(Map.of(CurrencyType.METAL_SCRAP, 200)))
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

    // 2) Activate: should double damage
    e.getEvents().trigger("ultimate.request");
    assertEquals(2.0f, attackMul.get(), 1e-6, "On activation should double damage");
    assertEquals(Boolean.TRUE, state.get(), "On activation state should be true");

    // 3) Force timer expiry: set endAtMs to current time
    Field endAt = HeroUltimateComponent.class.getDeclaredField("endAtMs");
    endAt.setAccessible(true);
    endAt.setLong(u, System.currentTimeMillis()); // expire immediately

    // 4) Call update(): should restore to 1.0, state=false, and broadcast remaining=0
    u.update();

    assertEquals(1.0f, attackMul.get(), 1e-6, "After expiry should restore to 1.0");
    assertEquals(Boolean.FALSE, state.get(), "After expiry state should be false");
    assertEquals(0.0f, remaining.get(), 1e-6, "After expiry should broadcast remaining=0");
  }
}


