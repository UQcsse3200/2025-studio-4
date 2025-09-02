package com.csse3200.game.entities.configs;

/** Config class to load tower stats from JSON */
public class TowerConfig {
    public TowerStats boneTower;
    public TowerStats dinoTower;
    public TowerStats cavemenTower;

    public static class TowerStats {
        public int damage;
        public float range;
        public float cooldown;
        public int cost;
    }
}
