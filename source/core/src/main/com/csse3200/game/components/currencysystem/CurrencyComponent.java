package com.csse3200.game.components.currencysystem;

import com.csse3200.game.components.Component;

/**
 * A component that represents a currency in the game.
 * Each currency has a type and an integer value.
 *
 * Example: a CurrencyComponent could represent 10 units of METAL_SCRAP.
 */
public class CurrencyComponent extends Component {
    /**
     * Supported types of currencies in the game.
     */
    public enum CurrencyType {
        METAL_SCRAP
    }

    private CurrencyType type;
    private int value;

    /**
     * Creates a new currency component with the specified type and value.
     *
     * @param type  the type of currency (for example, METAL_SCRAP)
     * @param value the amount of this currency
     */
    public CurrencyComponent(CurrencyType type, int value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Returns the type of this currency.
     *
     * @return the currency type
     */
    public CurrencyType getType() {
        return type;
    }

    /**
     * Returns the value (amount) of this currency.
     *
     * @return the currency value
     */
    public int getValue() {
        return value;
    }
}
