package com.csse3200.game.components.towers;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.HashMap;
import java.util.Map;

/**
 * Component that generates currency for the player at regular intervals.
 * Used by the Bank Tower to provide passive income.
 */
public class CurrencyGeneratorComponent extends Component {
    private CurrencyType currencyType;
    private int currencyAmount;
    private float generationInterval; // Time between currency generation (in seconds)
    private float timer = 0f;

    /**
     * Constructs a CurrencyGeneratorComponent with the specified currency type, amount, and interval.
     *
     * @param currencyType The type of currency to generate
     * @param currencyAmount The amount of currency to generate per interval
     * @param generationInterval The time between currency generation (in seconds)
     */
    public CurrencyGeneratorComponent(CurrencyType currencyType, int currencyAmount, float generationInterval) {
        this.currencyType = currencyType;
        this.currencyAmount = currencyAmount;
        this.generationInterval = generationInterval;
    }

    /**
     * Updates the generator timer and generates currency when the interval is reached.
     */
    @Override
    public void update() {
        timer += ServiceLocator.getTimeSource().getDeltaTime();

        if (timer >= generationInterval) {
            generateCurrency();
            timer = 0f;
        }
    }

    /**
     * Generates currency and adds it to the player's currency manager.
     */
    private void generateCurrency() {
        Entity player = findPlayer();
        if (player != null) {
            CurrencyManagerComponent cm = player.getComponent(CurrencyManagerComponent.class);
            if (cm != null) {
                Map<CurrencyType, Integer> drops = new HashMap<>();
                drops.put(currencyType, currencyAmount);
                player.getEvents().trigger("dropCurrency", drops);
                System.out.println(">>> Bank tower generated " + currencyAmount + " " + currencyType);
            }
        }
    }

    /**
     * Finds the player entity from registered entities.
     *
     * @return The player entity, or null if not found.
     */
    private Entity findPlayer() {
        Array<Entity> all = safeEntities();
        if (all == null) return null;
        for (Entity e : all) {
            if (e != null && e.getComponent(com.csse3200.game.components.player.PlayerActions.class) != null) {
                return e;
            }
        }
        return null;
    }

    /**
     * Gets a safe copy of all entities from the entity service.
     */
    private Array<Entity> safeEntities() {
        try {
            return ServiceLocator.getEntityService().getEntitiesCopy();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Gets the current currency type being generated.
     *
     * @return The currency type
     */
    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    /**
     * Sets the currency type to generate.
     *
     * @param currencyType The new currency type
     */
    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    /**
     * Gets the amount of currency generated per interval.
     *
     * @return The currency amount
     */
    public int getCurrencyAmount() {
        return currencyAmount;
    }

    /**
     * Sets the amount of currency generated per interval.
     *
     * @param currencyAmount The new currency amount
     */
    public void setCurrencyAmount(int currencyAmount) {
        this.currencyAmount = currencyAmount;
    }

    /**
     * Gets the generation interval in seconds.
     *
     * @return The generation interval
     */
    public float getGenerationInterval() {
        return generationInterval;
    }

    /**
     * Sets the generation interval in seconds.
     *
     * @param generationInterval The new generation interval
     */
    public void setGenerationInterval(float generationInterval) {
        this.generationInterval = generationInterval;
    }
}
