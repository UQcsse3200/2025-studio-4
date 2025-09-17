package com.csse3200.game.components;

import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TowerCostComponent extends Component {
    private final Map<CurrencyType, Integer> costMap;

    public TowerCostComponent(Map<CurrencyType, Integer> costMap) {
        this.costMap = new HashMap<>(costMap);
    }

    public Map<CurrencyType, Integer> getCostMap() {
        return Collections.unmodifiableMap(costMap);
    }

    public int getCostForCurrency(CurrencyType type) {
        return costMap.getOrDefault(type, 0);
    }
}