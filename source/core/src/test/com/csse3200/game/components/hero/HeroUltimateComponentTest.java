package com.csse3200.game.components.hero;

import com.badlogic.gdx.utils.TimeUtils;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HeroUltimateComponent.
 *
 * We avoid sleeping or time mocking by setting the component's private fields via reflection
 * to simulate "time passed" (endAtMs <= now) and to trigger remaining-time broadcasts.
 */
public class HeroUltimateComponentTest {

    /** Helper: set a private field on an object via reflection. */
    private static void setPrivate(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new AssertionError("Failed to set field '" + fieldName + "'", e);
        }
    }

    /** Helper: read a private field via reflection. */
    @SuppressWarnings("unchecked")
    private static <T> T getPrivate(Object target, String fieldName, Class<T> type) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(target);
        } catch (Exception e) {
            throw new AssertionError("Failed to get field '" + fieldName + "'", e);
        }
    }

    /** Create a bare hero entity with CombatStats (some listeners rely on a real entity). */
    private static Entity newHeroEntity() {
        return new Entity()
                .addComponent(new CombatStatsComponent(
                        /*hp*/100, /*atk*/10, DamageTypeConfig.None, DamageTypeConfig.None));
    }

    /** Create an upgrade component that returns the provided wallet (or null). */
    private static HeroUpgradeComponent makeUpgradeWithWallet(CurrencyManagerComponent wallet) {
        return new HeroUpgradeComponent() {
            @Override
            public CurrencyManagerComponent getWallet() {
                return wallet;
            }
        };
    }

    @Test
    void ultimate_withSufficientWallet_entersAndThenResets_onExpire() {
        // Wallet can pay 200
        CurrencyManagerComponent wallet = mock(CurrencyManagerComponent.class);
        when(wallet.canAffordAndSpendCurrency(anyMap())).thenAnswer(inv -> {
            Map<CurrencyType, Integer> cost = inv.getArgument(0);
            return cost.getOrDefault(CurrencyType.METAL_SCRAP, 0) == 200;
        });

        Entity hero = newHeroEntity();
        HeroUltimateComponent ult = new HeroUltimateComponent();
        HeroUpgradeComponent upgrade = makeUpgradeWithWallet(wallet);

        hero.addComponent(upgrade);
        hero.addComponent(ult);

        // Capture events
        AtomicReference<Float> lastMultiplier = new AtomicReference<>(1f);
        AtomicReference<Boolean> lastState = new AtomicReference<>(false);
        AtomicReference<Float> lastRemain = new AtomicReference<>(-1f);

        hero.getEvents().addListener("attack.multiplier", (Float m) -> lastMultiplier.set(m));
        hero.getEvents().addListener("ultimate.state", (Boolean s) -> lastState.set(s));
        hero.getEvents().addListener("ultimate.remaining", (Float s) -> lastRemain.set(s));

        hero.create();

        // Request ultimate
        hero.getEvents().trigger("ultimate.request");

        // Entered
        assertEquals(2.0f, lastMultiplier.get(), 1e-6);
        assertTrue(lastState.get(), "Ultimate should be active");

        // Force "time up" by setting endAtMs to now and keep active=true
        long now = TimeUtils.millis();
        setPrivate(ult, "endAtMs", now);
        setPrivate(ult, "active", true);

        // Update should reset to normal
        ult.update();
        assertEquals(1.0f, lastMultiplier.get(), 1e-6, "Multiplier should reset to 1.0 on expire");
        assertFalse(lastState.get(), "Ultimate should be inactive after expire");

        // Wallet deduction was attempted once
        @SuppressWarnings("unchecked")
        Map<CurrencyType, Integer> expected = Map.of(CurrencyType.METAL_SCRAP, 200);

    }

    @Test
    void ultimate_withInsufficientWallet_firesFailed_andDoesNotActivate() {
        CurrencyManagerComponent wallet = mock(CurrencyManagerComponent.class);
        when(wallet.canAffordAndSpendCurrency(anyMap())).thenReturn(false);

        Entity hero = newHeroEntity();
        HeroUltimateComponent ult = new HeroUltimateComponent();
        HeroUpgradeComponent upgrade = makeUpgradeWithWallet(wallet);

        hero.addComponent(upgrade);
        hero.addComponent(ult);

        AtomicBoolean failed = new AtomicBoolean(false);
        AtomicReference<String> reason = new AtomicReference<>();

        AtomicReference<Boolean> state = new AtomicReference<>(false);
        AtomicReference<Float> mult = new AtomicReference<>(1f);

        hero.getEvents().addListener("ultimate.failed", (String msg) -> {
            failed.set(true);
            reason.set(msg);
        });
        hero.getEvents().addListener("ultimate.state", (Boolean s) -> state.set(s));
        hero.getEvents().addListener("attack.multiplier", (Float m) -> mult.set(m));

        hero.create();
        hero.getEvents().trigger("ultimate.request");

    }

    @Test
    void ultimate_withoutUpgradeComponent_activatesForFree() {
        // No upgrade added -> ult should activate without checking wallet
        Entity hero = newHeroEntity();
        HeroUltimateComponent ult = new HeroUltimateComponent();
        hero.addComponent(ult);

        AtomicReference<Boolean> state = new AtomicReference<>(false);
        AtomicReference<Float> mult = new AtomicReference<>(1f);
        hero.getEvents().addListener("ultimate.state", (Boolean s) -> state.set(s));
        hero.getEvents().addListener("attack.multiplier", (Float m) -> mult.set(m));

        hero.create();
        hero.getEvents().trigger("ultimate.request");

        assertTrue(state.get(), "Ultimate should be active (free mode)");
        assertEquals(2f, mult.get(), 1e-6);

        // Force expire immediately
        long now = TimeUtils.millis();
        setPrivate(ult, "endAtMs", now);
        setPrivate(ult, "active", true);
        ult.update();

        assertFalse(state.get());
        assertEquals(1f, mult.get(), 1e-6);
    }

    @Test
    void update_broadcasts_remaining_time_every_tenth() {
        Entity hero = newHeroEntity();
        HeroUltimateComponent ult = new HeroUltimateComponent();
        hero.addComponent(ult);
        hero.create();

        // Assume active already (simulate activation)
        setPrivate(ult, "active", true);
        setPrivate(ult, "lastTenths", -1);

        AtomicReference<Float> remain = new AtomicReference<>(-1f);
        hero.getEvents().addListener("ultimate.remaining", (Float s) -> remain.set(s));

        // Case 1: 450ms remaining -> tenths=4 -> 0.4s
        long now = TimeUtils.millis();
        setPrivate(ult, "endAtMs", now + 450);
        ult.update();
        assertEquals(0.4f, remain.get(), 1e-6);

        // Case 2: 90ms remaining -> tenths=0 -> 0.0s (but still active)
        setPrivate(ult, "endAtMs", now + 90);
        ult.update();
        assertEquals(0.0f, remain.get(), 1e-6);

        // Case 3: 0ms remaining -> component will reset and broadcast final 0.0
        setPrivate(ult, "endAtMs", now);
        ult.update();
        assertEquals(0.0f, remain.get(), 1e-6);
        assertFalse(getPrivate(ult, "active", Boolean.class));
    }

    @Test
    void dispose_whileActive_resets_multiplier_and_state() {
        Entity hero = newHeroEntity();
        HeroUltimateComponent ult = new HeroUltimateComponent();
        hero.addComponent(ult);
        hero.create();

        AtomicReference<Float> mult = new AtomicReference<>(1f);
        AtomicReference<Boolean> state = new AtomicReference<>(false);
        hero.getEvents().addListener("attack.multiplier", (Float m) -> mult.set(m));
        hero.getEvents().addListener("ultimate.state", (Boolean s) -> state.set(s));

        // Simulate active
        setPrivate(ult, "active", true);
        setPrivate(ult, "endAtMs", TimeUtils.millis() + 3000);

        // Dispose should reset
        ult.dispose();
        assertEquals(1.0f, mult.get(), 1e-6);
        assertFalse(state.get());
        assertFalse(getPrivate(ult, "active", Boolean.class));
    }
}
