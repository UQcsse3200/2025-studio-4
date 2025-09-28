package com.csse3200.game.entities.configs;

import java.util.List;
import java.util.Map;

/**
 * Config class to load currency stats from JSON.
 * Contains stats for each currency type.
 */
public class CurrencyConfig {
    public Map<String, CurrencyStats> currencies;

    /**
     * Inner class representing stats for a single currency type.
     * All fields are strings to match JSON config values directly.
     */
    public static class CurrencyStats {
        /** Display name of the currency. */
        public String name;
        /** Background lore or description of the currency. */
        public String lore;
        /** Path to currency image asset. */
        public String image;
        /** Path to currency collection sound effect. */
        public String sound;
    }

    /**
     * Returns all currencies as a list.
     */
    public List<CurrencyStats> getAllCurrencies() {
        return List.copyOf(currencies.values());
    }
}

