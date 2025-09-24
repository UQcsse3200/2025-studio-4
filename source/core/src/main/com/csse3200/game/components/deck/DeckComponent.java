package com.csse3200.game.components.deck;

import java.util.Map;

/**
 * The {@code DeckComponent} class represents a data container for displaying
 * information about an entity in the game, such as a tower or an enemy.
 * It stores the entity type and its relevant statistics to be rendered
 * in the deck UI.
 */
public class DeckComponent {
    public enum DeckType {
        TOWER,
        ENEMY
    }

    public enum StatType {
        NAME("", ""),
        MAX_HEALTH("Max Health", ""),
        HEALTH("Health", ""),
        DAMAGE("Damage", ""),
        RANGE("Range", ""),
        SPEED("Speed", ""),
        COOLDOWN("Cooldown", "");

        private final String texturePath;
        private final String displayName;

        /**
         * Constructs a stat type with the specified display name, texture path.
         *
         * @param displayName     the name shown to the player
         * @param texturePath     the file path to the currency's texture/image
         */
        StatType(String displayName, String texturePath) {
            this.texturePath = texturePath;
            this.displayName = displayName;
        }

        /**
         * Gets the display name of this stat type.
         *
         * @return the name of the stat shown in the UI
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Gets the file path to the texture/image for this stat type.
         *
         * @return the path to the stat's texture
         */
        public String getTexturePath() {
            return texturePath;
        }
    }

    private final DeckType type;
    private final Map<StatType, String> stats;

    /**
     * Constructs a new {@code DeckComponent} with the specified type and statistics.
     *
     * @param type  the type of entity this deck represents (e.g., {@link DeckType#TOWER} or {@link DeckType#ENEMY})
     * @param stats a map containing the statistics of the entity,
     *              where the key is the stat name (e.g., "Health", "Damage")
     *              and the value is the stat value represented as a string
     */
    public DeckComponent(DeckType type, Map<StatType, String> stats) {
        this.type = type;
        this.stats = stats;
    }

    /**
     * Returns the type of this deck component.
     *
     * @return the {@link DeckType} of this deck component
     */
    public DeckType getType() {
        return type;
    }

    /**
     * Returns the statistics associated with this deck component.
     *
     * @return a map of statistics, where each key is the stat name
     *         and each value is the stat value as a string
     */
    public Map<StatType, String> getStats() {
        return stats;
    }

    /**
     * Specialized deck component for towers.
     */
    public static class TowerDeckComponent extends DeckComponent {
        public TowerDeckComponent(String name, int damage, double range, double cooldown) {
            super(
                    DeckType.TOWER,
                    Map.of(
                            StatType.NAME, name.toUpperCase(),
                            StatType.DAMAGE, String.valueOf(damage),
                            StatType.RANGE, String.valueOf(range),
                            StatType.COOLDOWN, String.valueOf(cooldown)
                    )
            );
        }
    }

    /**
     * Specialized deck component for enemies.
     */
    public static class EnemyDeckComponent extends DeckComponent {
        public EnemyDeckComponent(String name, int health, int damage) {
            super(
                    DeckType.ENEMY,
                    Map.of(
                            StatType.NAME, name.toUpperCase(),
                            StatType.MAX_HEALTH, String.valueOf(health),
                            StatType.DAMAGE, String.valueOf(damage)
                    )
            );
        }
    }
}
