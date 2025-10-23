package com.csse3200.game.entities.configs;

import java.util.List;

public class EnemyConfig {
    public EnemiesWrapper enemies;

    public static class EnemiesWrapper {
        public EnemyStats bossEnemy;
        public EnemyStats dividerEnemy;
        public EnemyStats dividerChildEnemy;
        public EnemyStats droneEnemy;
        public EnemyStats gruntEnemy;
        public EnemyStats tankEnemy;
        public EnemyStats speederEnemy;

        public List<EnemyStats> getAllEnemies() {
            return List.of(bossEnemy, dividerEnemy, dividerChildEnemy, droneEnemy, gruntEnemy, tankEnemy, speederEnemy);
        }
    }

    public static class EnemyStats {
        public String name;
        public String health;
        public String damage;
        public String speed;
        public String traits;
        public String role;
        public String currency;
        public String lore;
        public String points;
        public String weakness;
        public String resistance;
        public String image;
        public boolean locked;
    }

    public List<EnemyStats> getAllEnemies() {
        return enemies.getAllEnemies();
    }
}


