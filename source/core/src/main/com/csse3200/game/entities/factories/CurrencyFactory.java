package com.csse3200.game.entities.factories;

import com.csse3200.game.components.currencysystem.CollectibleComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;

public class CurrencyFactory {
    public static Entity createCurrency(CurrencyType type, int value, float x, float y, String texturePath) {
        Entity currency = new Entity()
                .addComponent(new TextureRenderComponent(texturePath))
                .addComponent(new CurrencyComponent(type, value))
                .addComponent(new CollectibleComponent());

        currency.setPosition(x, y);
        currency.getComponent(TextureRenderComponent.class).scaleEntity();

        return currency;
    }

    public static Entity createMetalScrap(float x, float y) {
        return createCurrency(CurrencyType.METAL_SCRAP, 5, x, y, "images/metal-scrap-currency.png");
    }
}


