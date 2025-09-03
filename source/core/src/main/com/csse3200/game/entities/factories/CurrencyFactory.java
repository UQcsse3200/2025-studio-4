package com.csse3200.game.entities.factories;

import com.csse3200.game.components.currencysystem.CollectibleComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * A factory class for creating currency entities in the game.
 *
 * This factory provides methods to create currency entities
 */
public class CurrencyFactory {
    /**
     * Creates a new currency entity with the given parameters.
     *
     * @param type        the type of currency (for example, metal scrap)
     * @param value       the value amount of this currency
     * @param x           the x-coordinate position of the entity
     * @param y           the y-coordinate position of the entity
     * @param texturePath the file path to the texture image
     * @return a fully initialized currency entity
     */
    public static Entity createCurrency(CurrencyType type, int value, float x, float y, String texturePath) {
        Entity currency = new Entity()
                .addComponent(new TextureRenderComponent(texturePath))
                .addComponent(new CurrencyComponent(type, value))
                .addComponent(new CollectibleComponent());

        currency.setPosition(x, y);
        currency.getComponent(TextureRenderComponent.class).scaleEntity();

        return currency;
    }

    /**
     * Creates a currency entity representing a piece of metal scrap.
     *
     * The created entity has a default value of 1 and uses the
     * predefined metal scrap texture.
     *
     * @param x the x-coordinate position of the entity
     * @param y the y-coordinate position of the entity
     * @return a metal scrap currency entity
     */
    public static Entity createMetalScrap(float x, float y) {
        return createCurrency(CurrencyType.METAL_SCRAP, 1, x, y, "images/metal-scrap-currency.png");
    }
}


