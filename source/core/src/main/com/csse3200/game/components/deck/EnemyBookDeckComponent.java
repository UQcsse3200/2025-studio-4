package com.csse3200.game.components.deck;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A specialized {@link DeckComponent} used for representing enemy information
 * in the in-game "book" deck UI. This deck includes descriptive stats such as
 * health, damage, speed, lore, traits, weaknesses, resistances, and whether the
 * enemy is locked/unlocked for the player.
 *
 * <p>Each enemy deck entry is displayed with an image, name, and relevant stats
 * to give the player both functional (combat-related) and narrative information.</p>
 */
public class EnemyBookDeckComponent extends DeckComponent {

    /**
     * Constructs a book deck for an enemy with detailed stats.
     *
     * @param name       Enemy name
     * @param health     Enemy maximum health (string)
     * @param damage     Enemy damage (string)
     * @param speed      Enemy movement speed (string)
     * @param traits     Enemy traits or abilities
     * @param role       Enemy role in the game (e.g., boss, grunt) — currently unused
     * @param currency   Reward currency on defeat — currently unused
     * @param lore       Narrative description or lore of the enemy
     * @param points     Points awarded for defeating the enemy — currently unused
     * @param weakness   Enemy weakness description
     * @param resistance Enemy resistance description
     * @param image      Path to the enemy's image
     * @param locked     Whether the enemy is locked ("true" or "false")
     */
    public EnemyBookDeckComponent(
            String name,
            String health,
            String damage,
            String speed,
            String traits,
            String role,
            String currency,
            String lore,
            String points,
            String weakness,
            String resistance,
            String image,
            boolean locked
    ) {
        super(createOrderedStats(
                name, health, damage, speed, traits, role, currency, lore, points, weakness, resistance, image, locked
        ));
    }

    /**
     * Creates an ordered mapping of enemy statistics for display in the book deck.
     * The order in which the stats are inserted defines the order of rendering in the UI.
     *
     * @param name       Enemy name
     * @param health     Enemy maximum health
     * @param damage     Enemy damage
     * @param speed      Enemy speed
     * @param traits     Enemy traits/abilities
     * @param role       Enemy role (currently unused)
     * @param currency   Reward currency (currently unused)
     * @param lore       Enemy lore/description
     * @param points     Points for defeating enemy (currently unused)
     * @param weakness   Enemy weakness
     * @param resistance Enemy resistance
     * @param image      Path to enemy image
     * @param locked     Whether the enemy is locked ("true" or "false")
     * @return a {@link Map} of {@link StatType} to stat values for rendering in the UI
     */
    private static Map<StatType, String> createOrderedStats(
            String name,
            String health,
            String damage,
            String speed,
            String traits,
            String role,
            String currency,
            String lore,
            String points,
            String weakness,
            String resistance,
            String image,
            boolean locked
    ) {
        Map<StatType, String> stats = new LinkedHashMap<>();
        stats.put(StatType.NAME, name.toUpperCase());
        stats.put(StatType.MAX_HEALTH, health);
        stats.put(StatType.DAMAGE, damage);
        stats.put(StatType.SPEED, speed);
        stats.put(StatType.LORE, lore);
        stats.put(StatType.TRAITS, traits);
//        stats.put(StatType.ROLE, role);
//        stats.put(StatType.CURRENCY, currency);
//        stats.put(StatType.POINTS, points);
        stats.put(StatType.WEAKNESS, weakness);
        stats.put(StatType.RESISTANCE, resistance);
        stats.put(StatType.TEXTURE_PATH, image); // Image
        stats.put(StatType.LOCKED, String.valueOf(locked));
        return stats;
    }
}
