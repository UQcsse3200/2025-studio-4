package com.csse3200.game.data;

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
        public final int damage;
        public final float range;
        public final float cooldown;
        public final float speed;
        public final int cost;
        public final String imagePath; // 🔹 new field for upgrade texture

        public UpgradeStats(int damage, float range, float cooldown, float speed, int cost, String imagePath) {
            this.damage = damage;
            this.range = range;
            this.cooldown = cooldown;
            this.speed = speed;
            this.cost = cost;
            this.imagePath = imagePath;
        }
    }

    /**
     * Returns a mapping of tower types to their Path A upgrades.
     */
    public static Map<String, Map<Integer, UpgradeStats>> getPathAUpgrades() {
        Map<String, Map<Integer, UpgradeStats>> pathA = new HashMap<>();

        // Bone tower
        Map<Integer, UpgradeStats> boneA = new HashMap<>();
        boneA.put(2, new UpgradeStats(20, 3.5f, 1.0f, 2.0f, 600, "images/bone.png"));
        boneA.put(3, new UpgradeStats(30, 4.0f, 1.0f, 2.0f, 750, "images/bone.png"));
        boneA.put(4, new UpgradeStats(40, 4.5f, 1.0f, 2.0f, 900, "images/bone.png"));
        boneA.put(5, new UpgradeStats(60, 5.0f, 1.0f, 2.0f, 1100, "images/bone.png"));
        pathA.put("bone", boneA);

        // Dino tower
        Map<Integer, UpgradeStats> dinoA = new HashMap<>();
        dinoA.put(2, new UpgradeStats(30, 2.5f, 0.8f, 2.0f, 1200, "images/dino.png"));
        dinoA.put(3, new UpgradeStats(40, 3.0f, 0.8f, 2.0f, 1500, "images/dino.png"));
        dinoA.put(4, new UpgradeStats(50, 3.5f, 0.8f, 2.0f, 1800, "images/dino.png"));
        dinoA.put(5, new UpgradeStats(60, 4.0f, 0.8f, 2.0f, 2000, "images/dino.png"));
        pathA.put("dino", dinoA);

        // Cavemen tower
        Map<Integer, UpgradeStats> cavemenA = new HashMap<>();
        cavemenA.put(2, new UpgradeStats(25, 6.0f, 0.8f, 2.0f, 900, "images/cavemen.png"));
        cavemenA.put(3, new UpgradeStats(35, 7.0f, 0.8f, 2.0f, 1100, "images/cavemen.png"));
        cavemenA.put(4, new UpgradeStats(45, 8.0f, 0.8f, 2.0f, 1300, "images/cavemen.png"));
        cavemenA.put(5, new UpgradeStats(55, 9.0f, 0.8f, 2.0f, 1450, "images/cavemen.png"));
        pathA.put("cavemen", cavemenA);

        return pathA;
    }

    /**
     * Returns a mapping of tower types to their Path B upgrades.
     */
    public static Map<String, Map<Integer, UpgradeStats>> getPathBUpgrades() {
        Map<String, Map<Integer, UpgradeStats>> pathB = new HashMap<>();

        // Bone tower
        Map<Integer, UpgradeStats> boneB = new HashMap<>();
        boneB.put(2, new UpgradeStats(10, 3.0f, 0.9f, 3f, 650, "images/bone.png"));
        boneB.put(3, new UpgradeStats(10, 3.0f, 0.8f, 4f, 800, "images/bone.png"));
        boneB.put(4, new UpgradeStats(10, 3.0f, 0.7f, 6f, 1000, "images/bone.png"));
        boneB.put(5, new UpgradeStats(10, 3.0f, 0.6f, 8f, 1250, "images/bone.png"));
        pathB.put("bone", boneB);

        // Dino tower
        Map<Integer, UpgradeStats> dinoB = new HashMap<>();
        dinoB.put(2, new UpgradeStats(20, 2.0f, 0.7f, 2.2f, 1200, "images/dino.png"));
        dinoB.put(3, new UpgradeStats(20, 2.0f, 0.6f, 2.5f, 1500, "images/dino.png"));
        dinoB.put(4, new UpgradeStats(20, 2.0f, 0.5f, 2.8f, 1800, "images/dino.png"));
        dinoB.put(5, new UpgradeStats(20, 2.0f, 0.4f, 3.0f, 2000, "images/dino.png"));
        pathB.put("dino", dinoB);

        // Cavemen tower
        Map<Integer, UpgradeStats> cavemenB = new HashMap<>();
        cavemenB.put(2, new UpgradeStats(15, 5.0f, 0.7f, 2.5f, 900, "images/cavemen.png"));
        cavemenB.put(3, new UpgradeStats(15, 5.0f, 0.6f, 3.0f, 1100, "images/cavemen.png"));
        cavemenB.put(4, new UpgradeStats(15, 5.0f, 0.5f, 3.5f, 1300, "images/cavemen.png"));
        cavemenB.put(5, new UpgradeStats(15, 5.0f, 0.4f, 4.0f, 1500, "images/cavemen.png"));
        pathB.put("cavemen", cavemenB);

        return pathB;
    }
}
