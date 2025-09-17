package com.csse3200.game.components.hero;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Map;

/**
 * Hero ultimate ability component:
 * - Triggered via button press or event.
 * - Consumes currency from the player's wallet.
 * - Temporarily increases attack damage by a multiplier.
 * - Lasts for 5 seconds, then resets to normal.
 */
public class HeroUltimateComponent extends Component {
    private static final int ULT_COST = 200;             // Currency cost per ultimate activation
    private static final long ULT_DURATION_MS = 5000;  // Duration: 5 seconds
    private static final float ULT_MULTIPLIER = 2.0f;  // Damage multiplier during ultimate

    private boolean active = false;
    private long endAtMs = 0L;
    private int lastTenths = -1; // Last broadcast "tenths of a second" count

    // Reference to the upgrade component (caches player + wallet)
    private HeroUpgradeComponent upgrade;

    @Override
    public void create() {
        upgrade = entity.getComponent(HeroUpgradeComponent.class);

        // Listen for "ultimate.request" (from UI or keyboard)
        entity.getEvents().addListener("ultimate.request", () -> onRequest());
    }

    /**
     * Handles ultimate activation request:
     * - Validates state and wallet.
     * - Attempts to spend required currency.
     * - If successful, activates the ultimate.
     */
    private void onRequest() {
        if (active) return;

        if (upgrade == null) {
            // No upgrade component → allow free ultimate (useful for testing)
            activateNow();
            return;
        }

        CurrencyManagerComponent wallet = (upgrade != null) ? upgrade.getWallet() : null;

        if (wallet == null) {
            // Wallet not ready → broadcast failure
            entity.getEvents().trigger("ultimate.failed", "Wallet not ready");
            return;
        }

        // Attempt to spend currency
        boolean ok = wallet.canAffordAndSpendCurrency(Map.of(CurrencyType.METAL_SCRAP, ULT_COST));
        if (!ok) {
            entity.getEvents().trigger("ultimate.failed", "Not enough " + CurrencyType.METAL_SCRAP);
            return;
        }

        // Payment succeeded → activate ultimate
        activateNow();
    }

    /**
     * Activates ultimate:
     * - Marks as active.
     * - Starts duration timer.
     * - Notifies attack components to apply multiplier.
     */
    private void activateNow() {
        active = true;
        endAtMs = TimeUtils.millis() + ULT_DURATION_MS;
        lastTenths = -1; // Reset broadcast throttling

        // Notify other components
        entity.getEvents().trigger("attack.multiplier", ULT_MULTIPLIER);
        entity.getEvents().trigger("ultimate.state", true);
    }

    /**
     * Update ultimate state:
     * - Broadcasts remaining time every 0.1s (in seconds with one decimal place).
     * - Resets attack multiplier and state when duration ends.
     */
    public void update() {
        if (!active) return;

        long now = TimeUtils.millis();
        long remainMs = Math.max(0, endAtMs - now);

        // Broadcast remaining time every 0.1s
        int tenths = (int) (remainMs / 100); // e.g., 5000ms → 50, 490ms → 4
        if (tenths != lastTenths) {
            lastTenths = tenths;
            float remainSec = tenths / 10f;
            entity.getEvents().trigger("ultimate.remaining", remainSec);
        }

        // Timer expired → reset
        if (remainMs == 0) {
            active = false;
            entity.getEvents().trigger("attack.multiplier", 1.0f);
            entity.getEvents().trigger("ultimate.state", false);
            entity.getEvents().trigger("ultimate.remaining", 0f); // Final broadcast
        }
    }

    @Override
    public void dispose() {
        if (active) {
            // Ensure state is reset if disposed mid-ultimate
            entity.getEvents().trigger("attack.multiplier", 1.0f);
            entity.getEvents().trigger("ultimate.state", false);
            active = false;
        }
    }
}
