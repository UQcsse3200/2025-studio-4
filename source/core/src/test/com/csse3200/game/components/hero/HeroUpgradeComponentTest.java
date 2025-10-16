package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HeroUpgradeComponent.
 *
 * Notes:
 * - Upgrades are triggered via "requestUpgrade" event instead of keyboard to avoid Gdx.input dependency.
 * - Gdx.app is mocked to prevent NPE from Gdx.app.log calls.
 * - CurrencyManagerComponent is mocked; verification ensures canAffordAndSpendCurrency() is called with expected cost.
 */
public class HeroUpgradeComponentTest {

    private Application mockApp;

    @BeforeEach
    void setupGdxApp() {
        // Mock Gdx.app for logging to avoid NPE
        mockApp = mock(Application.class);
        Gdx.app = mockApp;
    }

    /** Helper bundle holding hero entity and related components under test. */
    private static class HeroBundle {
        Entity hero;
        HeroUpgradeComponent upgrade;
        CombatStatsComponent stats;
    }

    /**
     * Builds a hero with CombatStats and HeroUpgradeComponent.
     * Optionally injects a player entity and a wallet (CurrencyManagerComponent).
     */
    private HeroBundle buildHeroWithUpgrade(Entity player, CurrencyManagerComponent wallet) {
        if (player == null) {
            player = new Entity();
        }
        if (wallet != null) {
            // Attach mocked wallet to player
            player.addComponent(wallet);
            player.create();
        }

        HeroBundle b = new HeroBundle();
        b.hero = new Entity();
        b.stats = new CombatStatsComponent(
                100, 20,
                DamageTypeConfig.None,
                DamageTypeConfig.None
        );
        b.upgrade = new HeroUpgradeComponent().attachPlayer(player);

        b.hero.addComponent(b.stats);
        b.hero.addComponent(b.upgrade);
        b.hero.create(); // Register event listeners
        return b;
    }

    @Test
    void upgradeSuccess_shouldDeductCost_applyGrowth_andTriggerUpgradedEvent() {
        // Arrange: mock wallet with sufficient currency
        CurrencyManagerComponent wallet = mock(CurrencyManagerComponent.class);
        // From level 1 -> 2: expected cost = 4
        when(wallet.canAffordAndSpendCurrency(anyMap())).thenReturn(true);

        Entity player = new Entity();
        HeroBundle b = buildHeroWithUpgrade(player, wallet);

        // Listen to "upgraded" event
        AtomicBoolean upgradedCalled = new AtomicBoolean(false);
        AtomicInteger receivedLevel = new AtomicInteger(-1);
        final CurrencyType[] receivedType = new CurrencyType[1];
        final int[] receivedCost = new int[1];

        b.hero.getEvents().addListener("upgraded", (Integer level, CurrencyType type, Integer cost) -> {
            upgradedCalled.set(true);
            receivedLevel.set(level);
            receivedType[0] = type;
            receivedCost[0] = cost;
        });

        int baseAttackBefore = b.stats.getBaseAttack();
        int healthBefore = b.stats.getHealth();

        // Act: trigger upgrade
        b.hero.getEvents().trigger("requestUpgrade", player);

        // Assert: wallet deduction with correct cost
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<CurrencyType, Integer>> mapCaptor =
                ArgumentCaptor.forClass((Class) Map.class);
        verify(wallet, times(1)).canAffordAndSpendCurrency(mapCaptor.capture());
        Map<CurrencyType, Integer> costMap = mapCaptor.getValue();
        assertEquals(1, costMap.size());
        assertEquals(400, costMap.get(CurrencyType.METAL_SCRAP));

        // Assert: stat growth (+10 atk, +20 hp)
        assertEquals(baseAttackBefore + 10, b.stats.getBaseAttack());
        assertEquals(healthBefore + 20, b.stats.getHealth());

        // Assert: event payload
        assertTrue(upgradedCalled.get());
        assertEquals(2, receivedLevel.get());
        assertEquals(CurrencyType.METAL_SCRAP, receivedType[0]);
        assertEquals(400, receivedCost[0]);

        verify(mockApp, atLeast(0)).log(anyString(), anyString());
    }

    @Test
    void upgradeFail_whenNotEnoughCurrency_shouldTriggerUpgradeFailed() {
        // Arrange: wallet mock returns false (cannot afford)
        CurrencyManagerComponent wallet = mock(CurrencyManagerComponent.class);
        when(wallet.canAffordAndSpendCurrency(anyMap())).thenReturn(false);

        Entity player = new Entity();
        HeroBundle b = buildHeroWithUpgrade(player, wallet);

        AtomicBoolean failedCalled = new AtomicBoolean(false);
        final String[] reason = new String[1];

        b.hero.getEvents().addListener("upgradeFailed", (String msg) -> {
            failedCalled.set(true);
            reason[0] = msg;
        });

        int baseAttackBefore = b.stats.getBaseAttack();
        int healthBefore = b.stats.getHealth();

        // Act: trigger upgrade
        b.hero.getEvents().trigger("requestUpgrade", player);

        // Assert: no stat change, failed event triggered
        verify(wallet, times(1)).canAffordAndSpendCurrency(anyMap());
        assertEquals(baseAttackBefore, b.stats.getBaseAttack());
        assertEquals(healthBefore, b.stats.getHealth());
        assertTrue(failedCalled.get());
        assertNotNull(reason[0]);
        assertTrue(reason[0].toLowerCase().contains("not enough"));

        verify(mockApp, atLeast(0)).log(anyString(), anyString());
    }

    @Test
    void upgradeFail_whenAtMaxLevel_shouldTriggerUpgradeFailed() {
        // Arrange: wallet always returns true
        CurrencyManagerComponent wallet = mock(CurrencyManagerComponent.class);
        when(wallet.canAffordAndSpendCurrency(anyMap())).thenReturn(true);

        Entity player = new Entity();
        HeroBundle b = buildHeroWithUpgrade(player, wallet);

        AtomicBoolean failedCalled = new AtomicBoolean(false);
        final String[] reason = new String[1];
        b.hero.getEvents().addListener("upgradeFailed", (String msg) -> {
            failedCalled.set(true);
            reason[0] = msg;
        });

        // Level up to max (3)
        b.hero.getEvents().trigger("requestUpgrade", player); // 1 -> 2
        assertEquals(2, b.upgrade.getLevel());

        // Attempt another upgrade (should fail)
        failedCalled.set(false);
        reason[0] = null;
        b.hero.getEvents().trigger("requestUpgrade", player);

        assertTrue(failedCalled.get());
        assertNotNull(reason[0]);
        assertTrue(reason[0].toLowerCase().contains("max level"));

        verify(mockApp, atLeast(0)).log(anyString(), anyString());
    }

    @Test
    void attachPlayer_shouldCacheWalletReference() {
        // Arrange: attach wallet to player
        CurrencyManagerComponent wallet = mock(CurrencyManagerComponent.class);
        Entity player = new Entity();

        HeroBundle b = buildHeroWithUpgrade(player, wallet);

        // Assert cached references
        assertSame(player, b.upgrade.getPlayer());
        assertSame(wallet, b.upgrade.getWallet());

        // Act: perform one upgrade
        when(wallet.canAffordAndSpendCurrency(anyMap())).thenReturn(true);
        b.hero.getEvents().trigger("requestUpgrade", player);

        verify(wallet, times(1)).canAffordAndSpendCurrency(anyMap());
        assertEquals(2, b.upgrade.getLevel());
    }

    @Test
    void upgradeFail_whenWalletMissing_shouldTriggerUpgradeFailed_withoutServiceLocator() {
        // Arrange: player exists but without wallet
        Entity playerWithoutWallet = new Entity();

        Entity hero = new Entity()
                .addComponent(new CombatStatsComponent(
                        100, 10, DamageTypeConfig.None, DamageTypeConfig.None))
                .addComponent(new HeroUpgradeComponent().attachPlayer(playerWithoutWallet));
        hero.create();

        AtomicBoolean failedCalled = new AtomicBoolean(false);
        hero.getEvents().addListener("upgradeFailed", (String msg) -> failedCalled.set(true));

        // Act: trigger upgrade
        hero.getEvents().trigger("requestUpgrade", playerWithoutWallet);

        // Assert: upgrade should fail
        assertTrue(failedCalled.get(), "Should fail when wallet not ready");
        verify(mockApp, atLeast(0)).log(anyString(), anyString());
    }
}
