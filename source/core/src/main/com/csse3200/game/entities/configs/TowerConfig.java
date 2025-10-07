package com.csse3200.game.entities.configs;

/**
 * Config class to load tower stats from JSON.
 * Contains stats for each tower type.
 */
public class TowerConfig {
    /** Stats for the bone tower. */
    public TowerWrapper boneTower;
    /** Stats for the dino tower. */
    public TowerWrapper dinoTower;
    /** Stats for the cavemen tower. */
    public TowerWrapper cavemenTower;

    /** Stats for the advanced tower. */
    public static class TowerWrapper {
        public TowerStats base;
    }

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
        public int metalScrapCost;
        public int titaniumCoreCost;
        public int neurochipCost;
        /** projectile speed of tower projectile */
        public float projectileSpeed;
        /** projectile life of tower projectile */
        public float projectileLife;
        /** projectile texture of tower projectile */
        public String projectileTexture;
        /** image of tower */
        public String image;
        public int level_A;
        public int level_B;
    }
}
