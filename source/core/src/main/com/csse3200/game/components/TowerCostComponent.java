package core.src.main.com.csse3200.game.components;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;

public class TowerCostComponent extends Component {
    private final CurrencyType currencyType;
    private final int cost;

    public TowerCostComponent(CurrencyType currencyType, int cost) {
        this.currencyType = currencyType;
        this.cost = cost;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public int getCost() {
        return cost;
    }
}
