package com.csse3200.game.components.currencysystem;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the player's currencies within the game.
 *
 * This component keeps track of different types of currencies (e.g., metal scraps)
 * and provides methods to add, subtract, and retrieve amounts of each type.
 *
 * Usage example:
 *   CurrencyManagerComponent manager = new CurrencyManagerComponent();
 *   manager.addCurrencyAmount(CurrencyType.METAL_SCRAP, 10);
 *   int scraps = manager.getCurrencyAmount(CurrencyType.METAL_SCRAP);
 */
public class CurrencyManagerComponent extends Component {
    private Map<CurrencyType, Integer> currencies = new HashMap<>();

    /**
     * Adds a specified amount of the given currency type.
     *
     * @param type   the type of currency to add
     * @param amount the amount to add
     */
    public void addCurrencyAmount(CurrencyType type, int amount) {
        currencies.put(type, currencies.getOrDefault(type, 0) + amount);
    }

    /**
     * Subtracts a specified amount of the given currency type.
     * If the result is negative, the value is zero.
     *
     * @param type   the type of currency to subtract
     * @param amount the amount to subtract
     */
    public void subtractCurrencyAmount(CurrencyType type, int amount) {
        currencies.put(type, Math.max(0, currencies.getOrDefault(type, 0) - amount));
    }

    /**
     * Gets the current amount of the given currency type.
     *
     * @param type the type of currency to check
     * @return the current amount of the specified currency, or 0 if none exists
     */
    public int getCurrencyAmount(CurrencyType type) {
        return currencies.getOrDefault(type, 0);
    }
}
