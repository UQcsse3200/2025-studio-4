package com.csse3200.game.components.deck;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnemyBookDeckComponent extends DeckComponent {

    /**
     * Constructs a book deck for an enemy with detailed stats.
     *
     * @param name       Enemy name
     * @param health     Enemy health (string)
     * @param damage     Enemy damage (string)
     * @param speed      Enemy speed (string)
     * @param traits     Enemy traits/abilities
     * @param role       Enemy role in the game
     * @param currency   Reward currency
     * @param lore       Enemy lore/description
     * @param points     Points awarded for defeating enemy
     * @param weakness   Weakness description
     * @param resistance Resistance description
     * @param image      Enemy image path
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
            String locked
    ) {
        super(createOrderedStats(
                name, health, damage, speed, traits, role, currency, lore, points, weakness, resistance, image, locked
        ));
    }

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
            String locked
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
        stats.put(StatType.LOCKED, locked);
        return stats;
    }
}
