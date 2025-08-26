package com.csse3200.game.components.currencysystem;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;

import java.util.HashMap;
import java.util.Map;

public class CurrencyManagerComponent extends Component {
    private Map<CurrencyType, Integer> currencies = new HashMap<>();

    public void addCurrencyAmount(CurrencyType type, int amount) {
        currencies.put(type, currencies.getOrDefault(type, 0) + amount);
    }

    public void subtractCurrencyAmount(CurrencyType type, int amount) {
        currencies.put(type, Math.max(0, currencies.getOrDefault(type, 0) - amount));
    }

    public int getCurrencyAmount(CurrencyType type) {
        return currencies.getOrDefault(type, 0);
    }
}
