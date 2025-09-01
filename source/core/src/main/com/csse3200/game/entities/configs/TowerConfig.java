package core.src.main.com.csse3200.game.entities.configs;

/** Config class to load tower stats from JSON */
public class TowerConfig {
    public TowerStats baseTower;
    public TowerStats sunTower;
    public TowerStats archerTower;

    public static class TowerStats {
        public int damage;
        public float range;
        public float cooldown;
    }
}
