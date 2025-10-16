package com.csse3200.game.components.deck;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The {@link AchievementBookDeckComponent} class represents a specialized deck component
 * for achievements in the game. It displays information such as the achievement's name,
 * description, associated image, and locked/unlocked status.
 *
 * <p>
 * This component is used inside the achievement book UI where achievements
 * are listed with their details.
 * </p>
 */
public class AchievementBookDeckComponent extends DeckComponent {

    /**
     * Constructs a book deck for an achievement.
     *
     * @param name        Achievement name
     * @param description Description of the achievement
     * @param image       Achievement image path
     * @param locked      Whether the achievement is locked
     */
    public AchievementBookDeckComponent(String name, String description, String image, boolean locked) {
        super(createOrderedStats(name, description, image, locked));
    }

    /**
     * Creates an ordered map of stats for the achievement deck component.
     * The order ensures consistent display in the UI.
     *
     * @param name        the name of the achievement
     * @param description a description of the achievement
     * @param image       the file path to the image representing the achievement
     * @param locked      whether the achievement is locked
     * @return a {@link Map} containing achievement statistics keyed by {@link StatType}
     */
    private static Map<StatType, String> createOrderedStats(String name, String description, String image, boolean locked) {
        Map<StatType, String> stats = new LinkedHashMap<>();
        stats.put(StatType.NAME, name.toUpperCase());
        stats.put(StatType.LORE, description);
        stats.put(StatType.TEXTURE_PATH, image);
        stats.put(StatType.LOCKED, String.valueOf(locked));
        return stats;
    }
}

