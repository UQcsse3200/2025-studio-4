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

    /*
    @Test
    void twoUpgradesSucceed_withSufficientFunds() {
        CurrencyManagerComponent wallet = new CurrencyManagerComponent();
        Entity player = makePlayerWithRealWallet(wallet);

        HeroUpgradeComponent upgrade = new HeroUpgradeComponent();
        CombatStatsComponent stats = stats(100, 20);
        Entity hero = makeHeroAttachedToPlayer(player, stats, upgrade);

        AtomicInteger upgradedCount = new AtomicInteger();
        AtomicBoolean failed = new AtomicBoolean(false);
<<<<<<< HEAD
        final String[] failReason = new String[1];
        hero.getEvents().addListener("upgraded", (Integer level, CurrencyType type, Integer cost) -> upgradedCount.incrementAndGet());
        hero.getEvents().addListener("upgradeFailed", (String msg) -> { failed.set(true); failReason[0] = msg; });
=======
        hero.getEvents().addListener("upgraded", (Integer level, CurrencyType type, Integer cost) -> {
            upgradedCount.incrementAndGet();
        });
        hero.getEvents().addListener("upgradeFailed", (String msg) -> failed.set(true));
>>>>>>> main

        int initBalance = wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP);
        int atk0 = stats.getBaseAttack();
        int hp0 = stats.getHealth();

<<<<<<< HEAD
        final int costL2 = 400;

        // 第一次：成功（1 -> 2）
=======
        // Costs use the same formula as HeroUpgradeComponent: nextLevel * 200
        int cost1 = 2 * 200;
        int cost2 = 3 * 200;

        // First upgrade
>>>>>>> main
        hero.getEvents().trigger("requestUpgrade", player);
        assertFalse(failed.get(), "First upgrade should succeed");
        assertEquals(2, upgrade.getLevel());
        assertEquals(1, upgradedCount.get());
        // Wallet decreased by the expected cost
        int balanceAfterFirstUpgrade = wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP);
        assertEquals(initBalance - cost1, balanceAfterFirstUpgrade,
                "Wallet balance after first upgrade should match expected deduction");
        // Stats increased
        assertTrue(stats.getBaseAttack() > atk0, "Attack should increase after upgrade");
        assertTrue(stats.getHealth() > hp0, "Health should increase after upgrade");

<<<<<<< HEAD
        // 第二次：应失败（已到上限 2）
        int balAfterFirst = wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP);
        int atkAfterFirst = stats.getBaseAttack();
        int hpAfterFirst  = stats.getHealth();

        failed.set(false);
        failReason[0] = null;
        hero.getEvents().trigger("requestUpgrade", player);

        assertTrue(failed.get(), "Second upgrade should fail at max level");
        assertNotNull(failReason[0]);
        assertTrue(failReason[0].toLowerCase().contains("max"));
        assertEquals(2, upgrade.getLevel(), "Level should remain at max (2)");
        assertEquals(balAfterFirst, wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP), "No extra deduction at max level");
        assertEquals(atkAfterFirst, stats.getBaseAttack(), "No extra attack growth at max level");
        assertEquals(hpAfterFirst,  stats.getHealth(), "No extra health growth at max level");

        // 升级成功次数应为 1
        assertEquals(1, upgradedCount.get());
=======
        // Record intermediate stats for second check
        int atkAfter1 = stats.getBaseAttack();
        int hpAfter1 = stats.getHealth();

        // Second upgrade
        hero.getEvents().trigger("requestUpgrade", player);
        assertFalse(failed.get(), "Second upgrade should also succeed with enough funds");
        assertEquals(3, upgrade.getLevel());
        assertEquals(2, upgradedCount.get());
        // Wallet decreased by the combined expected costs
        int balanceAfterSecondUpgrade = wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP);
        assertEquals(initBalance - (cost1 + cost2), balanceAfterSecondUpgrade,
                "Wallet balance after second upgrade should match expected deduction");
        // Stats increased further
        assertTrue(stats.getBaseAttack() > atkAfter1, "Attack should increase after second upgrade");
        assertTrue(stats.getHealth() > hpAfter1, "Health should increase after second upgrade");
>>>>>>> main
    }
    */


    @Test
    void atMaxLevel_secondUpgradeShouldFail_withMockWalletAlwaysAffordable() {
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

        // 第一次：成功到 2 级
        hero.getEvents().trigger("requestUpgrade", player);
        assertEquals(2, upgrade.getLevel());

        // 第二次：应失败（已到上限 2）
        failed.set(false);
        reason[0] = null;
        hero.getEvents().trigger("requestUpgrade", player);

        assertTrue(failed.get(), "Should fail when already at max level (2)");
        assertNotNull(reason[0]);
        assertTrue(reason[0].toLowerCase().contains("max"));
        assertEquals(2, upgrade.getLevel(), "Level should remain 2");
    }



    @Test
    void upgradeWhenNotEnoughFunds_shouldNotDeductAndShouldFailEvent() {
        CurrencyManagerComponent wallet = new CurrencyManagerComponent();
        Entity player = makePlayerWithRealWallet(wallet);
        int initBalance = wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP);

        assertTrue(spend(wallet, CurrencyType.METAL_SCRAP, initBalance));
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

        // Act: attempt upgrade without enough funds
        hero.getEvents().trigger("requestUpgrade", player);

        // Assert
        assertTrue(failed.get());
        assertNotNull(reason[0]);
        assertTrue(reason[0].toLowerCase().contains("not enough"));
        assertEquals(before, wallet.getCurrencyAmount(CurrencyType.METAL_SCRAP));
        assertEquals(1, upgrade.getLevel()); // Level unchanged
    }
}
