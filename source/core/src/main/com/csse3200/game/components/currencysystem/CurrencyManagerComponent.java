package com.csse3200.game.components.currencysystem;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private List<Entity> currencyList = new ArrayList<>();

    /**
     * Adds a specified amount of the given currency type.
     *
     * @param type   the type of currency to add
     * @param amount the amount to add
     */
    private void addCurrencyAmount(CurrencyType type, int amount) {
        currencies.put(type, currencies.getOrDefault(type, 0) + amount);
    }

    /**
     * Subtracts a specified amount of the given currency type.
     * If the result is negative, the value is zero.
     *
     * @param type   the type of currency to subtract
     * @param amount the amount to subtract
     */
    private void subtractCurrencyAmount(CurrencyType type, int amount) {
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

    /**
     * Add the Currency entity to the list if it is not already present.
     * And add event listeners to handle on collect action.
     *
     * @param entity
     */
    public void addCurrencyEntity(Entity entity) {
        if (!currencyList.contains(entity)){
            currencyList.add(entity);
            entity.getEvents().addListener("collectCurrency", this::collectCurrency);
        }
    }

    /**
     * Gets the list of currency entities.
     *
     * @return the current list of currency entities
     */
    public List<Entity> getCurrencyList() {
        return currencyList;
    }

    /**
     * Increment the counter and trigger to update UI.
     * @param entity collected Currency Entity
     */
    private void collectCurrency(Entity entity) {
        CurrencyType type = entity.getComponent(CurrencyComponent.class).getType();
        int amount = entity.getComponent(CurrencyComponent.class).getValue();
        this.addCurrencyAmount(type, amount);
        this.entity.getEvents().trigger("updateScrap", this.getCurrencyAmount(type));
    }

    /**
     * Spawns currency entities at the given position based on the specified drops.
     * Consider spreading them slightly so they don’t all spawn exactly at the same position.
     * Should add event listener "dropCurrency".
     *
     * Usage: playerEntity.getEvents().trigger("dropCurrency", dropsMap, x, y)
     *
     * @param drops a map of {@link CurrencyType} to the amount to drop for each type
     * @param x     the x-coordinate where the currency should appear
     * @param y     the y-coordinate where the currency should appear
     */
    private void dropCurrency(Map<CurrencyType, Integer> drops, float x, float y) {

    }

    /**
     * Checks if the player has enough currency to cover the specified cost,
     * and if so, deducts the amount and returns true. Otherwise, returns false
     * and does not deduct anything.
     *
     * Usage: playerEntity.getComponent(CurrencyManagerComponent.class).canAffordAndSpendCurrency(costMap)
     *
     * @param cost a map of {@link CurrencyType} to the required amount for each type
     * @return true if the player can afford the cost and it was deducted,
     *         false if the player cannot afford the cost
     */
    public boolean canAffordAndSpendCurrency(Map<CurrencyType, Integer> cost) {
        return true;
    }
}
