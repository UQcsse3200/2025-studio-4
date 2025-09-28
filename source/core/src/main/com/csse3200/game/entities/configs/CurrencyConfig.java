package com.csse3200.game.entities.configs;

import java.util.List;

/**
 * Config class to load currency stats from JSON.
 * Contains stats for each currency type.
 */
public class CurrencyConfig {
    /** Wrapper for all currency types. Matches JSON top-level field. */
    public CurrenciesWrapper currencies;

    /** Wrapper class containing each currency as a separate field. */
    public static class CurrenciesWrapper {
        public CurrencyStats metalScrapCurrency;
        public CurrencyStats titaniumCoreCurrency;
        public CurrencyStats neurochipCurrency;

        /** Returns all currencies as a list. */
        public List<CurrencyStats> getAllCurrencies() {
            return List.of(metalScrapCurrency, titaniumCoreCurrency, neurochipCurrency);
        }
    }

    /** Inner class representing stats for a single currency type. */
    public static class CurrencyStats {
        public String name;
        public String lore;
        public String image;
        public String sound;
    }

    /** Returns all currencies as a list. */
    public List<CurrencyStats> getAllCurrencies() {
        return currencies.getAllCurrencies();
    }
}


