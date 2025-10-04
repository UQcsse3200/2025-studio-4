package com.csse3200.game.components.deck;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The {@link CurrencyBookDeckComponent} class represents a specialized deck component
 * for currencies in the game. It displays information such as the currency's name,
 * lore (description), associated image, and sound effect.
 *
 * <p>
 * This component can be used inside a book or encyclopedia UI where currencies
 * are listed with their details.
 * </p>
 */
public class CurrencyBookDeckComponent extends DeckComponent {

    /**
     * Constructs a book deck for a currency.
     *
     * @param name        Currency name
     * @param lore        Description of currency
     * @param image       Currency image path
     * @param sound       Sound effect path when collected
     */
    public CurrencyBookDeckComponent(String name, String lore, String image, String sound) {
        super(createOrderedStats(name, lore, image, sound));
    }

    /**
     * Creates an ordered map of stats for the currency deck component.
     * The order ensures consistent display in the UI.
     *
     * @param name   the name of the currency
     * @param lore   a description or lore of the currency
     * @param image  the file path to the image representing the currency
     * @param sound  the file path to the sound effect played when the currency is collected
     * @return a {@link Map} containing currency statistics keyed by {@link StatType}
     */
    private static Map<StatType, String> createOrderedStats(String name, String lore, String image, String sound) {
        Map<StatType, String> stats = new LinkedHashMap<>();
        stats.put(StatType.NAME, name.toUpperCase());
        stats.put(StatType.LORE, lore);
        stats.put(StatType.TEXTURE_PATH, image);
        stats.put(StatType.SOUND, sound);
        return stats;
    }
}

