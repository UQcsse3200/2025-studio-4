package com.csse3200.game.components;

import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Component representing the cost of a tower in various currencies.
 * Stores a mapping from currency types to their required amounts.
 */
public class TowerCostComponent extends Component {
    private final Map<CurrencyType, Integer> costMap;

    /**
     * Constructs a TowerCostComponent with the given cost map.
     *
     * @param costMap a map of currency types to their required amounts
     */
    public TowerCostComponent(Map<CurrencyType, Integer> costMap) {
        this.costMap = new HashMap<>(costMap);
    }

    /**
     * Returns an unmodifiable view of the cost map.
     *
     * @return the cost map
     */
    public Map<CurrencyType, Integer> getCostMap() {
        return Collections.unmodifiableMap(costMap);
    }

    /**
     * Gets the cost for a specific currency type.
     *
     * @param type the currency type
     * @return the cost for the given currency, or 0 if not present
     */
    public int getCostForCurrency(CurrencyType type) {
        return costMap.getOrDefault(type, 0);
    }
}