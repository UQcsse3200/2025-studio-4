package com.csse3200.game.components.maingame;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores upgrade paths and stats for all tower types.
 */
public class TowerUpgradeData {

    /**
     * Represents the upgrade stats for a tower at a certain level.
     */
    public static class UpgradeStats {
        /**
         * Constructs an UpgradeStats object with the specified parameters.
         *
         * @param damage     Damage value for the upgrade.
         * @param range      Range value for the upgrade.
         * @param cooldown   Cooldown value for the upgrade.
         * @param speed      Projectile speed for the upgrade.
         * @param cost       Cost of the upgrade.
         * @param imagePath  Texture path for the upgrade.
         */
        public UpgradeStats(int damage, float range, float cooldown, float speed, int cost, String imagePath) {
            this.damage = damage;
            this.range = range;
            this.cooldown = cooldown;
            this.speed = speed;
            this.cost = cost;
            this.imagePath = imagePath;
        }

        public final int damage;
        public final float range;
        public final float cooldown;
        public final float speed;
        public final int cost;
        public final String imagePath; // 🔹 new field for upgrade texture
    }

    /**
     * Returns a mapping of tower types to their Path A upgrades.
     *
     * @return Map of tower type strings to their Path A upgrade levels and stats.
     */
    public static Map<String, Map<Integer, UpgradeStats>> getPathAUpgrades() {
        Map<String, Map<Integer, UpgradeStats>> pathA = new HashMap<>();

        // Bone tower
        Map<Integer, UpgradeStats> boneA = new HashMap<>();
        boneA.put(2, new UpgradeStats(7, 3.5f, 1.0f, 2.0f, 600, "images/bone.png"));
        boneA.put(3, new UpgradeStats(10, 4.0f, 1.0f, 2.0f, 750, "images/bone.png"));
        boneA.put(4, new UpgradeStats(14, 4.5f, 1.0f, 2.0f, 900, "images/bone.png"));
        boneA.put(5, new UpgradeStats(20, 5.0f, 1.0f, 2.0f, 1100, "images/bone.png"));
        pathA.put("bone", boneA);

        // Dino tower
        Map<Integer, UpgradeStats> dinoA = new HashMap<>();
        dinoA.put(2, new UpgradeStats(17, 2.5f, 0.8f, 2.0f, 1200, "images/dino.png"));
        dinoA.put(3, new UpgradeStats(20, 3.0f, 0.8f, 2.0f, 1500, "images/dino.png"));
        dinoA.put(4, new UpgradeStats(25, 3.5f, 0.8f, 2.0f, 1800, "images/dino.png"));
        dinoA.put(5, new UpgradeStats(30, 4.0f, 0.8f, 2.0f, 2000, "images/dino.png"));
        pathA.put("dino", dinoA);

        // Cavemen tower
        Map<Integer, UpgradeStats> cavemenA = new HashMap<>();
        cavemenA.put(2, new UpgradeStats(35, 6.0f, 0.8f, 6.0f, 900, "images/cavemen.png"));
        cavemenA.put(3, new UpgradeStats(43, 7.0f, 0.8f, 6.0f, 1100, "images/cavemen.png"));
        cavemenA.put(4, new UpgradeStats(53, 8.0f, 0.8f, 6.0f, 1300, "images/cavemen.png"));
        cavemenA.put(5, new UpgradeStats(65, 10.0f, 0.8f, 6.0f, 1450, "images/cavemen.png"));
        pathA.put("cavemen", cavemenA);

        // Pteradactyl tower (Path A: Damage & Range)
        Map<Integer, UpgradeStats> pteroA = new HashMap<>();
        pteroA.put(2, new UpgradeStats(7, 3.5f, 1.0f, 4.0f, 60, "images/pteradactyl.png"));
        pteroA.put(3, new UpgradeStats(10, 4.2f, 1.0f, 4.0f, 70, "images/pteradactyl.png"));
        pteroA.put(4, new UpgradeStats(14, 5.1f, 1.0f, 4.0f, 110, "images/pteradactyl.png"));
        pteroA.put(5, new UpgradeStats(20, 6.2f, 1.0f, 4.0f, 190, "images/pteradactyl.png"));
        pathA.put("pteradactyl", pteroA);

        // Totem tower (Path A: Range)
        Map<Integer, UpgradeStats> totemA = new HashMap<>();
        totemA.put(2, new UpgradeStats(0, 5.0f, 0f, 5.0f, 900, "images/towers/totem.png"));
        totemA.put(3, new UpgradeStats(0, 6.0f, 0f, 5.0f, 1100, "images/towers/totem.png"));
        totemA.put(4, new UpgradeStats(0, 7.0f, 0f, 5.0f, 1300, "images/towers/totem.png"));
        totemA.put(5, new UpgradeStats(0, 8.0f, 0f, 5.0f, 1500, "images/towers/totem.png"));
        pathA.put("totem", totemA);

        return pathA;
    }

    /**
     * Returns a mapping of tower types to their Path B upgrades.
     *
     * @return Map of tower type strings to their Path B upgrade levels and stats.
     */
    public static Map<String, Map<Integer, UpgradeStats>> getPathBUpgrades() {
        Map<String, Map<Integer, UpgradeStats>> pathB = new HashMap<>();

        // Bone tower
        Map<Integer, UpgradeStats> boneB = new HashMap<>();
        boneB.put(2, new UpgradeStats(10, 3.0f, 0.9f, 5f, 650, "images/bone.png"));
        boneB.put(3, new UpgradeStats(10, 3.0f, 0.8f, 6f, 800, "images/bone.png"));
        boneB.put(4, new UpgradeStats(10, 3.0f, 0.7f, 7f, 1000, "images/bone.png"));
        boneB.put(5, new UpgradeStats(10, 3.0f, 0.6f, 8f, 1250, "images/bone.png"));
        pathB.put("bone", boneB);

        // Dino tower
        Map<Integer, UpgradeStats> dinoB = new HashMap<>();
        dinoB.put(2, new UpgradeStats(20, 2.0f, 1.8f, 2.2f, 1200, "images/dino.png"));
        dinoB.put(3, new UpgradeStats(20, 2.0f, 1.6f, 2.5f, 1500, "images/dino.png"));
        dinoB.put(4, new UpgradeStats(20, 2.0f, 1.4f, 2.8f, 1800, "images/dino.png"));
        dinoB.put(5, new UpgradeStats(20, 2.0f, 1.0f, 3.2f, 2000, "images/dino.png"));
        pathB.put("dino", dinoB);

        // Cavemen tower
        Map<Integer, UpgradeStats> cavemenB = new HashMap<>();
        cavemenB.put(2, new UpgradeStats(15, 5.0f, 4.75f, 6.5f, 900, "images/cavemen.png"));
        cavemenB.put(3, new UpgradeStats(15, 5.0f, 4.5f, 7.0f, 1100, "images/cavemen.png"));
        cavemenB.put(4, new UpgradeStats(15, 5.0f, 4.25f, 7.5f, 1300, "images/cavemen.png"));
        cavemenB.put(5, new UpgradeStats(15, 5.0f, 4.0f, 8.0f, 1500, "images/cavemen.png"));
        pathB.put("cavemen", cavemenB);

        // Pteradactyl tower (Path B: Cooldown & Speed)
        Map<Integer, UpgradeStats> pteroB = new HashMap<>();
        pteroB.put(2, new UpgradeStats(10, 2.5f, 0.9f, 17.0f, 60, "images/pteradactyl.png"));
        pteroB.put(3, new UpgradeStats(10, 2.5f, 0.8f, 19.0f, 80, "images/pteradactyl.png"));
        pteroB.put(4, new UpgradeStats(10, 2.5f, 0.7f, 22.0f, 120, "images/pteradactyl.png"));
        pteroB.put(5, new UpgradeStats(10, 2.5f, 0.6f, 25.0f, 200, "images/pteradactyl.png"));
        pathB.put("pteradactyl", pteroB);

        // Totem tower (Path B: Speed)
        Map<Integer, UpgradeStats> totemB = new HashMap<>();
        totemB.put(2, new UpgradeStats(0, 4.0f, 0f, 7.0f, 900, "images/towers/totem.png"));
        totemB.put(3, new UpgradeStats(0, 4.0f, 0f, 9.0f, 1100, "images/towers/totem.png"));
        totemB.put(4, new UpgradeStats(0, 4.0f, 0f, 11.0f, 1300, "images/towers/totem.png"));
        totemB.put(5, new UpgradeStats(0, 4.0f, 0f, 13.0f, 1500, "images/towers/totem.png"));
        pathB.put("totem", totemB);

        return pathB;
    }
}
