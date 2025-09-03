package com.csse3200.game.entities.configs;

/**
 * Config class to load tower stats from JSON.
 * Contains stats for each tower type.
 */
public class TowerConfig {
    /** Stats for the bone tower. */
    public TowerStats boneTower;
    /** Stats for the dino tower. */
    public TowerStats dinoTower;
    /** Stats for the cavemen tower. */
    public TowerStats cavemenTower;

    /**
     * Inner class representing stats for a single tower type.
     */
    public static class TowerStats {
        /** Damage dealt by the tower. */
        public int damage;
        /** Attack range of the tower (in game units). */
        public float range;
        /** Cooldown between attacks (in seconds). */
        public float cooldown;
        /** Cost to place the tower. */
        public int cost;
    }
}
