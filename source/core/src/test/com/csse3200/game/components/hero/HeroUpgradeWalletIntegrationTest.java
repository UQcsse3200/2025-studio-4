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
 * Integration tests for HeroUpgradeComponent with a real CurrencyManagerComponent.
 * <p>
 * Verifies:
 * - Currency is deducted correctly when upgrading.
 * - Upgrades fail and no deduction occurs when funds are insufficient or at max level.
 * - Upgrade events and failure events are triggered correctly.
 * - Hero stats grow as expected.
 */
public class HeroUpgradeWalletIntegrationTest {

    @BeforeEach
    void setupGdx() {
        // Prevent NPE from Gdx.app.log
        Gdx.app = mock(Application.class);
    }

    /**
     * Build a player with a real wallet and initialize it (default METAL_SCRAP = 500).
     */
    private Entity makePlayerWithRealWallet(CurrencyManagerComponent wallet) {
        Entity player = new Entity().addComponent(wallet);
        player.create(); // triggers wallet.create() -> sets up balances & listeners
        return player;
    }

    /**
     * Build a hero (with CombatStats + HeroUpgrade) and attach to player.
     */
    private Entity makeHeroAttachedToPlayer(Entity player, CombatStatsComponent stats, HeroUpgradeComponent upgrade) {
        Entity hero = new Entity()
                .addComponent(stats)
                .addComponent(upgrade.attachPlayer(player));
        hero.create();
        return hero;
    }

    /**
     * Convenience: CombatStats constructor with no resistances/weaknesses.
     */
    private CombatStatsComponent stats(int hp, int atk) {
        return new CombatStatsComponent(hp, atk, DamageTypeConfig.None, DamageTypeConfig.None);
    }

    /**
     * Utility: spend a specific currency amount (if balance is sufficient).
     */
    private static boolean spend(CurrencyManagerComponent w, CurrencyType t, int amt) {
        Map<CurrencyType, Integer> m = new EnumMap<>(CurrencyType.class);
        m.put(t, amt);
        return w.canAffordAndSpendCurrency(m);
    }

    @Test
    void firstUpgradeSucceeds_secondUpgradeFails_dueToInsufficientFunds() {
        // Arrange
        CurrencyManagerComponent wallet = new CurrencyManagerComponent();
        Entity player = makePlayerWithRealWallet(wallet); // METAL_SCRAP = 500
        HeroUpgradeComponent upgrade = new HeroUpgradeComponent();
        CombatStatsComponent stats = stats(100, 20);
        Entity hero = makeHeroAttachedToPlayer(player, stats, upgrade);

        AtomicInteger upgradeCount = new AtomicInteger();
        AtomicBoolean failed = new AtomicBoolean(false);
        hero.getEvents().addListener("upgraded", (Integer level, CurrencyType type, Integer cost) -> upgradeCount.incrementAndGet());
        hero.getEvents().addListener("upgradeFailed", (String msg) -> failed.set(true));

        int atk0 = stats.getBaseAttack(); // 20
        int hp0 = stats.getHealth();     // 100

        // Act1: 1 -> 2 (cost = 400)
        hero.getEvents().trigger("requestUpgrade", player);
        assertEquals(100, wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP));
        assertEquals(atk0 + 10, stats.getBaseAttack());
        assertEquals(hp0 + 20, stats.getHealth());
        assertEquals(2, upgrade.getLevel());
        assertEquals(1, upgradeCount.get());

        failed.set(false);
        hero.getEvents().trigger("requestUpgrade", player);

        assertTrue(failed.get(), "Second upgrade should fail due to insufficient funds");
        assertEquals(100, wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP));
        assertEquals(2, upgrade.getLevel());
    }

    @Test
    void atMaxLevel_thirdUpgradeShouldFail_withMockWalletAlwaysAffordable() {
        CurrencyManagerComponent wallet = mock(CurrencyManagerComponent.class);
        when(wallet.canAffordAndSpendCurrency(anyMap())).thenReturn(true);

        Entity player = new Entity().addComponent(wallet);
        player.create();

        HeroUpgradeComponent upgrade = new HeroUpgradeComponent();
        Entity hero = makeHeroAttachedToPlayer(player, stats(100, 20), upgrade);

        AtomicBoolean failed = new AtomicBoolean(false);
        final String[] reason = new String[1];
        hero.getEvents().addListener("upgradeFailed", (String msg) -> {
            failed.set(true);
            reason[0] = msg;
        });

        hero.getEvents().trigger("requestUpgrade", player);
        hero.getEvents().trigger("requestUpgrade", player);
        assertEquals(3, upgrade.getLevel(), "Should reach max level 3 first");

        failed.set(false);
        reason[0] = null;
        hero.getEvents().trigger("requestUpgrade", player);

        assertTrue(failed.get(), "Should fail when already at max level");
        assertNotNull(reason[0]);
        assertTrue(reason[0].toLowerCase().contains("max"));
    }


    @Test
    void upgradeWhenNotEnoughFunds_shouldNotDeductAndShouldFailEvent() {
        // Arrange
        CurrencyManagerComponent wallet = new CurrencyManagerComponent();
        Entity player = makePlayerWithRealWallet(wallet); // initial 500
        // Empty wallet: spend all 500
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

        // Act: attempt upgrade without enough funds (cost = 400)
        hero.getEvents().trigger("requestUpgrade", player);

        // Assert
        assertTrue(failed.get());
        assertNotNull(reason[0]);
        assertTrue(reason[0].toLowerCase().contains("not enough"));
        assertEquals(before, wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP)); // No deduction
        assertEquals(1, upgrade.getLevel()); // Level unchanged
    }
}
