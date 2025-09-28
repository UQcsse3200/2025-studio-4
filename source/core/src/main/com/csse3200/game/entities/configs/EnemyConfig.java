package com.csse3200.game.entities.configs;

import java.util.List;
import java.util.Map;

/**
 * Config class to load enemy stats from JSON.
 * Contains stats for each enemy type.
 */
public class EnemyConfig {
    /** Map of all enemies by key, matching JSON field names. */
    public Map<String, EnemyStats> enemies;

    /**
     * Inner class representing stats for a single enemy type.
     * All fields are strings to match JSON config values directly.
     */
    public static class EnemyStats {
        /** Display name of the enemy. */
        public String name;
        /** Enemy health rating (e.g., Low, Moderate, Very High). */
        public String health;
        /** Enemy damage rating. */
        public String damage;
        /** Enemy speed rating. */
        public String speed;
        /** Special traits or abilities of the enemy. */
        public String traits;
        /** Enemy role on the battlefield. */
        public String role;
        /** Currency dropped when defeated. */
        public String currency;
        /** Background lore or description. */
        public String lore;
        /** Points awarded when defeated. */
        public String points;
        /** Enemy weakness. */
        public String weakness;
        /** Enemy resistance. */
        public String resistance;
        /** Path to enemy image asset. */
        public String image;
    }

    /**
     * Returns all enemy stats as a list.
     */
    public List<EnemyStats> getAllEnemies() {
        return List.copyOf(enemies.values());
    }
}

