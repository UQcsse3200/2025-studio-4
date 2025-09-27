package com.csse3200.game.components.deck;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.configs.DamageTypeConfig;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.Math.round;


/**
 * The {@code DeckComponent} class represents a data container for displaying
 * information about an entity in the game, such as a tower or an enemy.
 * It stores the entity type and its relevant statistics to be rendered
 * in the deck UI.
 */
public class DeckComponent extends Component {
    public enum StatType {
        NAME("NAME", ""),
        MAX_HEALTH("MAX HEALTH", "images/deck/heart.png"),
        HEALTH("HEALTH", "images/deck/heart.png"),
        DAMAGE("DAMAGE", "images/deck/sword.png"),
        RANGE("RANGE", "images/deck/bullseye_target.png"),
        SPEED("SPEED", "images/deck/shoe.png"),
        COOLDOWN("COOLDOWN", "images/deck/hourglass.png"),
        WEAKNESS("WEAKNESS", "images/deck/cracked_skull.png"),
        RESISTANCE("RESISTANCE", "images/deck/shield.png"),
        TEXTURE_PATH("", "");

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

    private final Map<StatType, String> stats;

    /**
     * Constructs a new {@code DeckComponent} with the specified type and statistics.
     *
     * @param stats a map containing the statistics of the entity,
     *              where the key is the stat name (e.g., "Health", "Damage")
     *              and the value is the stat value represented as a string
     */
    public DeckComponent(Map<StatType, String> stats) {
        this.stats = stats;
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
     *
     * Usage:
     *      // Example inside TowerComponent or EnemyComponent click handler
     *      playerEntity.getEvents().trigger("displayDeck", towerEntity.getComponent(DeckComponent.class));
     */
    public static class TowerDeckComponent extends DeckComponent {
        public TowerDeckComponent(String name, int damage, double range, double cooldown, String texturePath) {
            super(
                    createOrderedStats(name + " tower", damage, range, Math.floor(cooldown * 100) / 100, texturePath)
            );
        }

        private static Map<StatType, String> createOrderedStats(String name, int damage, double range, double cooldown, String texturePath) {
            Map<StatType, String> stats = new LinkedHashMap<>();
            stats.put(StatType.NAME, name.toUpperCase());    // Name first
            stats.put(StatType.DAMAGE, String.valueOf(damage));
            stats.put(StatType.RANGE, String.valueOf(range));
            stats.put(StatType.COOLDOWN, String.valueOf(cooldown));
            stats.put(StatType.TEXTURE_PATH, texturePath);
            return stats;
        }
    }


    /**
     * Specialized deck component for enemies.
     *
     * Usage:
     *      // Example inside TowerComponent or EnemyComponent click handler
     *      playerEntity.getEvents().trigger("displayDeck", enemyEntity.getComponent(DeckComponent.class));
     */
    public static class EnemyDeckComponent extends DeckComponent {
        public EnemyDeckComponent(String name, int maxHealth, int damage, DamageTypeConfig resistance, DamageTypeConfig weakness, String texturePath) {
            super(
                    createOrderedStats(name, maxHealth, damage, resistance, weakness, texturePath)
            );
        }

        private static Map<StatType, String> createOrderedStats(String name, int maxHealth, int damage, DamageTypeConfig resistance, DamageTypeConfig weakness, String texturePath) {
            Map<StatType, String> stats = new LinkedHashMap<>();
            stats.put(StatType.NAME, name.toUpperCase());    // Name first
            stats.put(StatType.MAX_HEALTH, String.valueOf(maxHealth));
            stats.put(StatType.DAMAGE, String.valueOf(damage));
            stats.put(StatType.RESISTANCE, resistance.name().toUpperCase());
            stats.put(StatType.WEAKNESS, weakness.name().toUpperCase());
            stats.put(StatType.TEXTURE_PATH, texturePath);
            return stats;
        }
    }

}
