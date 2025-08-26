package com.csse3200.game.components.currencysystem;

import com.csse3200.game.components.Component;

public class CurrencyComponent extends Component {
    public enum CurrencyType {
        METAL_SCRAP
    }

    private CurrencyType type;
    private int value;

    public CurrencyComponent(CurrencyType type, int value) {
        this.type = type;
        this.value = value;
    }

    public CurrencyType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }
}
