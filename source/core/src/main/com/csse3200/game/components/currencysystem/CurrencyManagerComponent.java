package com.csse3200.game.components.currencysystem;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

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
 * Trigger "updateCurrencyUI" with current currency amount not the added or subtracted value
 */
public class CurrencyManagerComponent extends Component {
    private Map<CurrencyType, Integer> currencies = new HashMap<>();
    private List<Entity> currencyEntityList = new ArrayList<>();
    private final String updateCurrencyUIEvent = "updateCurrencyUI";

    @Override
    public void create() {
        this.entity.getEvents().addListener("dropCurrency", this::dropCurrency);
    }

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
        if (!currencyEntityList.contains(entity)){
            currencyEntityList.add(entity);
            entity.getEvents().addListener("collectCurrency", this::collectCurrency);
        }
    }

    /**
     * Gets the list of currency entities.
     *
     * @return the current list of currency entities
     */
    public List<Entity> getCurrencyEntityList() {
        return currencyEntityList;
    }

    /**
     * Increment the counter and trigger to update UI.
     * @param entity collected Currency Entity
     */
    private void collectCurrency(Entity entity) {
        CurrencyType type = entity.getComponent(CurrencyComponent.class).getType();
        int value = entity.getComponent(CurrencyComponent.class).getValue();
        this.addCurrencyAmount(type, value);

        playCollectCurrencySound(type); // Play collecting sound

        int amount = getCurrencyAmount(type);
        this.entity.getEvents().trigger(updateCurrencyUIEvent, type, amount);
    }

    /**
     * Plays the collection sound associated with the given currency type.
     *
     * @param type the {@link CurrencyType} whose collect sound should be played
     */
    private void playCollectCurrencySound(CurrencyType type) {
        ServiceLocator.getResourceService()
                .getAsset(type.getCollectSoundPath(), Sound.class)
                .play(1.0f);
    }

    /**
     * Add specified currencies when triggered on enemy's death
     *
     * Usage: playerEntity.getEvents().trigger("dropCurrency", dropsMap)
     *
     * @param drops a map of {@link CurrencyType} to the amount to drop for each type
     */
    private void dropCurrency(Map<CurrencyType, Integer> drops) {
        drops.forEach((type, value) -> {
            this.addCurrencyAmount(type, value);
            int amount = getCurrencyAmount(type);
            this.entity.getEvents().trigger(updateCurrencyUIEvent, type, amount);
        });
    }

    /**
     * Checks if the player has enough currency to cover the specified cost,
     * and if so, deducts the amount and returns true. Otherwise, returns false
     * and does not deduct anything.
     *
     * Usage: playerEntity.getComponent(CurrencyManagerComponent.class).canAffordAndSpendCurrency(costMap)
     *
     * @param costMap a map of {@link CurrencyType} to the required amount for each type
     * @return true if the player can afford the cost and the player's currencies are deducted,
     *         false if the player cannot afford the cost
     */
    public boolean canAffordAndSpendCurrency(Map<CurrencyType, Integer> costMap) {
        for (Map.Entry<CurrencyType, Integer> entry:  costMap.entrySet()) {
            // Check if the cost is larger than the player's currency
            if (entry.getValue() > this.getCurrencyAmount(entry.getKey())) {
                return false;
            }
        }
        // Deduct currencies by the cost
        costMap.forEach((type, value) -> {
            this.subtractCurrencyAmount(type, value);
            int amount = getCurrencyAmount(type);
            this.entity.getEvents().trigger(updateCurrencyUIEvent, type, amount);
        });
        return true;
    }

    /**
     * Refunds a portion of the tower’s cost according to the specified refund rate.
     *
     * Usage: playerEntity.getComponent(CurrencyManagerComponent.class).refundCurrency(costMap)
     *
     * @param costMap a map of {@link CurrencyType} to the required amount for each type
     * @param refundRate the refund rate of the cost
     *
     */
    public void refundCurrency(Map<CurrencyType, Integer> costMap, float refundRate) {
        costMap.forEach((type, value) -> {
            this.addCurrencyAmount(type, (int) (value * refundRate));
            int amount = getCurrencyAmount(type);
            this.entity.getEvents().trigger(updateCurrencyUIEvent, type, amount);
        });
    }
}
