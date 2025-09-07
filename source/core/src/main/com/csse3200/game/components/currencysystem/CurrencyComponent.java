package com.csse3200.game.components.currencysystem;

import com.csse3200.game.components.Component;

/**
 * A component that represents a currency in the game.
 * Each currency has a type and an integer value.
 *
 * Example: a CurrencyComponent could represent 10 units of METAL_SCRAP.
 */
public class CurrencyComponent extends Component {
    /**
     * Supported types of currencies in the game.
     */
    public enum CurrencyType {
        METAL_SCRAP(
                "Metal Scrap",
                "images/metal_scrap_currency.png",
                "sounds/metal_scrap_currency_collect.ogg"
        ),
        TITANIUM_CORE(
                "Titanium Core",
                "images/titanium_core_currency.png",
                "sounds/titanium_core_currency_collect.ogg"
        ),
        NEUROCHIP(
                "Neurochip",
                "images/neurochip_currency.png",
                "sounds/neurochip_currency_collect.ogg"
        );

        private final String texturePath;
        private final String displayName;
        private final String collectSoundPath;

        /**
         * Constructs a currency type with the specified display name, texture path, and collection sound path.
         *
         * @param displayName     the name shown to the player
         * @param texturePath     the file path to the currency's texture/image
         * @param collectSoundPath the file path to the sound played when the currency is collected
         */
        CurrencyType(String displayName, String texturePath, String collectSoundPath) {
            this.texturePath = texturePath;
            this.displayName = displayName;
            this.collectSoundPath = collectSoundPath;
        }

        /**
         * Gets the display name of this currency type.
         *
         * @return the name of the currency shown in the UI
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Gets the file path to the texture/image for this currency type.
         *
         * @return the path to the currency's texture
         */
        public String getTexturePath() {
            return texturePath;
        }

        /**
         * Gets the file path to the sound played when this currency type is collected.
         *
         * @return the path to the collection sound
         */
        public String getCollectSoundPath() {
            return collectSoundPath;
        }
    }

    private CurrencyType type;
    private int value;

    /**
     * Creates a new currency component with the specified type and value.
     *
     * @param type  the type of currency (for example, METAL_SCRAP)
     * @param value the amount of this currency
     */
    public CurrencyComponent(CurrencyType type, int value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Returns the type of this currency.
     *
     * @return the currency type
     */
    public CurrencyType getType() {
        return type;
    }

    /**
     * Returns the value (amount) of this currency.
     *
     * @return the currency value
     */
    public int getValue() {
        return value;
    }
}
