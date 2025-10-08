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
        public final String imagePath; // new field for upgrade texture
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
        boneA.put(2, new UpgradeStats(7, 3.5f, 1.0f, 2.0f, 600, "images/towers/bone.png"));
        boneA.put(3, new UpgradeStats(10, 4.0f, 1.0f, 2.0f, 750, "images/towers/bone.png"));
        boneA.put(4, new UpgradeStats(14, 4.5f, 1.0f, 2.0f, 900, "images/towers/bone.png"));
        boneA.put(5, new UpgradeStats(20, 5.0f, 1.0f, 2.0f, 1100, "images/towers/bone.png"));
        pathA.put("bone", boneA);

        // Dino tower
        Map<Integer, UpgradeStats> dinoA = new HashMap<>();
        dinoA.put(2, new UpgradeStats(17, 2.5f, 0.8f, 2.0f, 1200, "images/towers/dino.png"));
        dinoA.put(3, new UpgradeStats(20, 3.0f, 0.8f, 2.0f, 1500, "images/towers/dino.png"));
        dinoA.put(4, new UpgradeStats(25, 3.5f, 0.8f, 2.0f, 1800, "images/towers/dino.png"));
        dinoA.put(5, new UpgradeStats(30, 4.0f, 0.8f, 2.0f, 2000, "images/towers/dino.png"));
        pathA.put("dino", dinoA);

        // Cavemen tower
        Map<Integer, UpgradeStats> cavemenA = new HashMap<>();
        cavemenA.put(2, new UpgradeStats(35, 6.0f, 0.8f, 6.0f, 900, "images/towers/cavemen.png"));
        cavemenA.put(3, new UpgradeStats(43, 7.0f, 0.8f, 6.0f, 1100, "images/towers/cavemen.png"));
        cavemenA.put(4, new UpgradeStats(53, 8.0f, 0.8f, 6.0f, 1300, "images/towers/cavemen.png"));
        cavemenA.put(5, new UpgradeStats(65, 10.0f, 0.8f, 6.0f, 1450, "images/towers/cavemen.png"));
        pathA.put("cavemen", cavemenA);

        // Pteradactyl tower (Path A: Damage & Range)
        Map<Integer, UpgradeStats> pteroA = new HashMap<>();
        pteroA.put(2, new UpgradeStats(7, 3.5f, 1.0f, 4.0f, 60, "images/towers/pteradactyl.png"));
        pteroA.put(3, new UpgradeStats(10, 4.2f, 1.0f, 4.0f, 70, "images/towers/pteradactyl.png"));
        pteroA.put(4, new UpgradeStats(14, 5.1f, 1.0f, 4.0f, 110, "images/towers/pteradactyl.png"));
        pteroA.put(5, new UpgradeStats(20, 6.2f, 1.0f, 4.0f, 190, "images/towers/pteradactyl.png"));
        pathA.put("pteradactyl", pteroA);

        // Totem tower (Path A: Range)
        Map<Integer, UpgradeStats> totemA = new HashMap<>();
        totemA.put(2, new UpgradeStats(0, 4.0f, 0f, 0.0f, 175, "images/towers/totem.png"));
        totemA.put(3, new UpgradeStats(0, 4.5f, 0f, 0.0f, 200, "images/towers/totem.png"));
        totemA.put(4, new UpgradeStats(0, 5.0f, 0f, 0.0f, 225, "images/towers/totem.png"));
        totemA.put(5, new UpgradeStats(0, 5.5f, 0f, 0.0f, 250, "images/towers/totem.png"));
        pathA.put("totem", totemA);

        // SuperCavemen tower (Path A: Damage & Range)
        Map<Integer, UpgradeStats> superCavemenA = new HashMap<>();
        superCavemenA.put(2, new UpgradeStats(60, 7.0f, 1.0f, 10.0f, 2000, "images/towers/supercavemen.png"));
        superCavemenA.put(3, new UpgradeStats(80, 8.0f, 1.0f, 10.0f, 2500, "images/towers/supercavemen.png"));
        superCavemenA.put(4, new UpgradeStats(100, 9.0f, 1.0f, 10.0f, 3000, "images/towers/supercavemen.png"));
        superCavemenA.put(5, new UpgradeStats(130, 10.0f, 1.0f, 10.0f, 3500, "images/towers/supercavemen.png"));
        pathA.put("supercavemen", superCavemenA);

        // Cavemen Village tower (Path A: Damage & Range)
        Map<Integer, UpgradeStats> cavemenVillageA = new HashMap<>();
        cavemenVillageA.put(2, new UpgradeStats(40, 7.0f, 1.0f, 7.0f, 1200, "images/towers/cavemenvillage.png"));
        cavemenVillageA.put(3, new UpgradeStats(50, 8.0f, 1.0f, 7.0f, 1500, "images/towers/cavemenvillage.png"));
        cavemenVillageA.put(4, new UpgradeStats(65, 9.0f, 1.0f, 7.0f, 1800, "images/towers/cavemenvillage.png"));
        cavemenVillageA.put(5, new UpgradeStats(80, 10.0f, 1.0f, 7.0f, 2100, "images/towers/cavemenvillage.png"));
        pathA.put("cavemenvillage", cavemenVillageA);

        // Raft tower (Path A: Damage & Range)
        Map<Integer, UpgradeStats> raftA = new HashMap<>();
        raftA.put(2, new UpgradeStats(20, 4.0f, 1.0f, 5.0f, 900, "images/towers/raft.png"));
        raftA.put(3, new UpgradeStats(28, 5.0f, 1.0f, 5.0f, 1100, "images/towers/raft.png"));
        raftA.put(4, new UpgradeStats(36, 6.0f, 1.0f, 5.0f, 1300, "images/towers/raft.png"));
        raftA.put(5, new UpgradeStats(45, 7.0f, 1.0f, 5.0f, 1500, "images/towers/raft.png"));
        pathA.put("raft", raftA);

        // Frozen Mammoth Skull tower (Path A: Damage & Range)
        Map<Integer, UpgradeStats> frozenMamoothSkullA = new HashMap<>();
        frozenMamoothSkullA.put(2, new UpgradeStats(30, 5.0f, 1.0f, 6.0f, 1000, "images/towers/frozenmamoothskull.png"));
        frozenMamoothSkullA.put(3, new UpgradeStats(40, 6.0f, 1.0f, 6.0f, 1300, "images/towers/frozenmamoothskull.png"));
        frozenMamoothSkullA.put(4, new UpgradeStats(50, 7.0f, 1.0f, 6.0f, 1600, "images/towers/frozenmamoothskull.png"));
        frozenMamoothSkullA.put(5, new UpgradeStats(65, 8.0f, 1.0f, 6.0f, 1900, "images/towers/frozenmamoothskull.png"));
        pathA.put("frozenmamoothskull", frozenMamoothSkullA);

        // Boulder Catapult tower (Path A: Damage & Range)
        Map<Integer, UpgradeStats> boulderCatapultA = new HashMap<>();
        boulderCatapultA.put(2, new UpgradeStats(50, 8.0f, 1.0f, 8.0f, 1500, "images/towers/bouldercatapult.png"));
        boulderCatapultA.put(3, new UpgradeStats(65, 9.0f, 1.0f, 8.0f, 1800, "images/towers/bouldercatapult.png"));
        boulderCatapultA.put(4, new UpgradeStats(80, 10.0f, 1.0f, 8.0f, 2100, "images/towers/bouldercatapult.png"));
        boulderCatapultA.put(5, new UpgradeStats(100, 12.0f, 1.0f, 8.0f, 2500, "images/towers/bouldercatapult.png"));
        pathA.put("bouldercatapult", boulderCatapultA);

        // Village Shaman tower (Path A: Damage & Range)
        Map<Integer, UpgradeStats> villageShamanA = new HashMap<>();
        villageShamanA.put(2, new UpgradeStats(25, 5.0f, 1.0f, 5.0f, 1000, "images/towers/villageshaman.png"));
        villageShamanA.put(3, new UpgradeStats(35, 6.0f, 1.0f, 5.0f, 1300, "images/towers/villageshaman.png"));
        villageShamanA.put(4, new UpgradeStats(45, 7.0f, 1.0f, 5.0f, 1600, "images/towers/villageshaman.png"));
        villageShamanA.put(5, new UpgradeStats(60, 8.0f, 1.0f, 5.0f, 2000, "images/towers/villageshaman.png"));
        pathA.put("villageshaman", villageShamanA);

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
        boneB.put(2, new UpgradeStats(10, 3.0f, 0.9f, 5f, 650, "images/towers/bone.png"));
        boneB.put(3, new UpgradeStats(10, 3.0f, 0.8f, 6f, 800, "images/towers/bone.png"));
        boneB.put(4, new UpgradeStats(10, 3.0f, 0.7f, 7f, 1000, "images/towers/bone.png"));
        boneB.put(5, new UpgradeStats(10, 3.0f, 0.6f, 8f, 1250, "images/towers/bone.png"));
        pathB.put("bone", boneB);

        // Dino tower
        Map<Integer, UpgradeStats> dinoB = new HashMap<>();
        dinoB.put(2, new UpgradeStats(20, 2.0f, 1.8f, 2.2f, 1200, "images/towers/dino.png"));
        dinoB.put(3, new UpgradeStats(20, 2.0f, 1.6f, 2.5f, 1500, "images/towers/dino.png"));
        dinoB.put(4, new UpgradeStats(20, 2.0f, 1.4f, 2.8f, 1800, "images/towers/dino.png"));
        dinoB.put(5, new UpgradeStats(20, 2.0f, 1.0f, 3.2f, 2000, "images/towers/dino.png"));
        pathB.put("dino", dinoB);

        // Cavemen tower
        Map<Integer, UpgradeStats> cavemenB = new HashMap<>();
        cavemenB.put(2, new UpgradeStats(15, 5.0f, 4.75f, 6.5f, 900, "images/towers/cavemen.png"));
        cavemenB.put(3, new UpgradeStats(15, 5.0f, 4.5f, 7.0f, 1100, "images/towers/cavemen.png"));
        cavemenB.put(4, new UpgradeStats(15, 5.0f, 4.25f, 7.5f, 1300, "images/towers/cavemen.png"));
        cavemenB.put(5, new UpgradeStats(15, 5.0f, 4.0f, 8.0f, 1500, "images/towers/cavemen.png"));
        pathB.put("cavemen", cavemenB);

        // Pteradactyl tower (Path B: Cooldown & Speed)
        Map<Integer, UpgradeStats> pteroB = new HashMap<>();
        pteroB.put(2, new UpgradeStats(10, 2.5f, 0.9f, 17.0f, 60, "images/towers/pteradactyl.png"));
        pteroB.put(3, new UpgradeStats(10, 2.5f, 0.8f, 19.0f, 80, "images/towers/pteradactyl.png"));
        pteroB.put(4, new UpgradeStats(10, 2.5f, 0.7f, 22.0f, 120, "images/towers/pteradactyl.png"));
        pteroB.put(5, new UpgradeStats(10, 2.5f, 0.6f, 25.0f, 200, "images/towers/pteradactyl.png"));
        pathB.put("pteradactyl", pteroB);

        // Totem tower (Path B: Speed)
        Map<Integer, UpgradeStats> totemB = new HashMap<>();
        totemB.put(2, new UpgradeStats(0, 4.0f, 0f, 0.0f, 160, "images/towers/totem.png"));
        totemB.put(3, new UpgradeStats(0, 4.0f, 0f, 0.0f, 175, "images/towers/totem.png"));
        totemB.put(4, new UpgradeStats(0, 4.0f, 0f, 0.0f, 200, "images/towers/totem.png"));
        totemB.put(5, new UpgradeStats(0, 4.0f, 0f, 0.0f, 230, "images/towers/totem.png"));
        pathB.put("totem", totemB);

        // SuperCavemen tower (Path B: Cooldown & Speed)
        Map<Integer, UpgradeStats> superCavemenB = new HashMap<>();
        superCavemenB.put(2, new UpgradeStats(60, 6.0f, 0.8f, 15.0f, 2000, "images/towers/supercavemen.png"));
        superCavemenB.put(3, new UpgradeStats(60, 6.0f, 0.7f, 20.0f, 2500, "images/towers/supercavemen.png"));
        superCavemenB.put(4, new UpgradeStats(60, 6.0f, 0.6f, 25.0f, 3000, "images/towers/supercavemen.png"));
        superCavemenB.put(5, new UpgradeStats(60, 6.0f, 0.5f, 30.0f, 3500, "images/towers/supercavemen.png"));
        pathB.put("supercavemen", superCavemenB);

        // Cavemen Village tower (Path B: Cooldown & Speed)
        Map<Integer, UpgradeStats> cavemenVillageB = new HashMap<>();
        cavemenVillageB.put(2, new UpgradeStats(40, 6.0f, 0.8f, 8.0f, 1200, "images/towers/cavemenvillage.png"));
        cavemenVillageB.put(3, new UpgradeStats(40, 6.0f, 0.7f, 9.0f, 1500, "images/towers/cavemenvillage.png"));
        cavemenVillageB.put(4, new UpgradeStats(40, 6.0f, 0.6f, 10.0f, 1800, "images/towers/cavemenvillage.png"));
        cavemenVillageB.put(5, new UpgradeStats(40, 6.0f, 0.5f, 12.0f, 2100, "images/towers/cavemenvillage.png"));
        pathB.put("cavemenvillage", cavemenVillageB);

        // Raft tower (Path B: Cooldown & Speed)
        Map<Integer, UpgradeStats> raftB = new HashMap<>();
        raftB.put(2, new UpgradeStats(20, 3.0f, 0.8f, 7.0f, 900, "images/towers/raft.png"));
        raftB.put(3, new UpgradeStats(20, 3.0f, 0.7f, 8.0f, 1100, "images/towers/raft.png"));
        raftB.put(4, new UpgradeStats(20, 3.0f, 0.6f, 9.0f, 1300, "images/towers/raft.png"));
        raftB.put(5, new UpgradeStats(20, 3.0f, 0.5f, 10.0f, 1500, "images/towers/raft.png"));
        pathB.put("raft", raftB);

        // Frozen Mammoth Skull tower (Path B: Cooldown & Speed)
        Map<Integer, UpgradeStats> frozenMamoothSkullB = new HashMap<>();
        frozenMamoothSkullB.put(2, new UpgradeStats(30, 4.0f, 0.8f, 8.0f, 1000, "images/towers/frozenmamoothskull.png"));
        frozenMamoothSkullB.put(3, new UpgradeStats(30, 4.0f, 0.7f, 9.0f, 1300, "images/towers/frozenmamoothskull.png"));
        frozenMamoothSkullB.put(4, new UpgradeStats(30, 4.0f, 0.6f, 10.0f, 1600, "images/towers/frozenmamoothskull.png"));
        frozenMamoothSkullB.put(5, new UpgradeStats(30, 4.0f, 0.5f, 12.0f, 1900, "images/towers/frozenmamoothskull.png"));
        pathB.put("frozenmamoothskull", frozenMamoothSkullB);

        // Boulder Catapult tower (Path B: Cooldown & Speed)
        Map<Integer, UpgradeStats> boulderCatapultB = new HashMap<>();
        boulderCatapultB.put(2, new UpgradeStats(50, 7.0f, 0.8f, 10.0f, 1500, "images/towers/bouldercatapult.png"));
        boulderCatapultB.put(3, new UpgradeStats(50, 7.0f, 0.7f, 12.0f, 1800, "images/towers/bouldercatapult.png"));
        boulderCatapultB.put(4, new UpgradeStats(50, 7.0f, 0.6f, 14.0f, 2100, "images/towers/bouldercatapult.png"));
        boulderCatapultB.put(5, new UpgradeStats(50, 7.0f, 0.5f, 16.0f, 2500, "images/towers/bouldercatapult.png"));
        pathB.put("bouldercatapult", boulderCatapultB);

        // Village Shaman tower (Path B: Cooldown & Speed)
        Map<Integer, UpgradeStats> villageShamanB = new HashMap<>();
        villageShamanB.put(2, new UpgradeStats(25, 4.0f, 0.8f, 7.0f, 1000, "images/towers/villageshaman.png"));
        villageShamanB.put(3, new UpgradeStats(25, 4.0f, 0.7f, 8.0f, 1300, "images/towers/villageshaman.png"));
        villageShamanB.put(4, new UpgradeStats(25, 4.0f, 0.6f, 9.0f, 1600, "images/towers/villageshaman.png"));
        villageShamanB.put(5, new UpgradeStats(25, 4.0f, 0.5f, 10.0f, 2000, "images/towers/villageshaman.png"));
        pathB.put("villageshaman", villageShamanB);

        return pathB;
    }
}
