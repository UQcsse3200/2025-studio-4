package com.csse3200.game.components.deck;

import java.util.LinkedHashMap;
import java.util.Map;

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

    private static Map<StatType, String> createOrderedStats(String name, String lore, String image, String sound) {
        Map<StatType, String> stats = new LinkedHashMap<>();
        stats.put(StatType.NAME, name.toUpperCase());
        stats.put(StatType.LORE, lore);
        stats.put(StatType.TEXTURE_PATH, image);
        stats.put(StatType.SOUND, sound);
        return stats;
    }
}

