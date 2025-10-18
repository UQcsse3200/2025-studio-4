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
     * @return a fully initialized currency entity
     */
    public static Entity createCurrency(CurrencyType type, int value, float x, float y) {
        return createCurrency(type, value, x, y, 0f); // 0 -> no TTL auto-despawn
    }

    /**
     * Overload that allows specifying a TTL (seconds) after which the currency despawns automatically.
     *
     * @param type  currency type
     * @param value currency value
     * @param x     world x
     * @param y     world y
     * @param ttlSeconds time to live in seconds; <= 0 means no auto-despawn
     */
    public static Entity createCurrency(CurrencyType type, int value, float x, float y, float ttlSeconds) {
        Entity currency = new Entity()
                .addComponent(new TextureRenderComponent(type.getTexturePath()))
                .addComponent(new CurrencyComponent(type, value))
                .addComponent(ttlSeconds > 0f ? new CollectibleComponent(ttlSeconds) : new CollectibleComponent());

        currency.setPosition(x, y);
        currency.getComponent(TextureRenderComponent.class).scaleEntity();

        return currency;
    }
}
