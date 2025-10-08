package com.csse3200.game.components.towers;

import com.csse3200.game.components.maingame.TowerUpgradeData;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;

import java.util.Map;

/**
 * Component that manages upgrades for a Bank Tower.
 * Path A changes the currency type.
 * Path B decreases the generation interval (faster income).
 */
public class BankTowerUpgradeComponent extends CurrencyGeneratorComponent {
    private int levelA = 1; // Path A current level
    private int levelB = 1; // Path B current level
    private final Entity towerEntity;

    public BankTowerUpgradeComponent(Entity towerEntity, CurrencyType initialType, int initialAmount, float interval) {
        super(initialType, initialAmount, interval);
        this.towerEntity = towerEntity;
    }

    /**
     * Applies an upgrade along a specified path.
     *
     * @param path  "A" for currency type, "B" for generation speed
     * @param level The target level to upgrade to
     */
    public void applyUpgrade(String path, int level) {
        TowerUpgradeData.UpgradeStats stats = null;

        if (path.equalsIgnoreCase("A")) {
            Map<Integer, TowerUpgradeData.UpgradeStats> pathA = TowerUpgradeData.getPathAUpgrades().get("bank");
            stats = pathA.get(level);
            if (stats != null) {
                levelA = level;
                // Change currency type based on level
                switch (level) {
                    case 2 -> setCurrencyType(CurrencyType.METAL_SCRAP);
                    case 3 -> setCurrencyType(CurrencyType.TITANIUM_CORE);
                    case 4 -> setCurrencyType(CurrencyType.NEUROCHIP);
                    default -> {} // level 1 default already set
                }
                System.out.println(">>> Bank Tower upgraded Path A to level " + level + " with currency " + getCurrencyType());
            }
        } else if (path.equalsIgnoreCase("B")) {
            Map<Integer, TowerUpgradeData.UpgradeStats> pathB = TowerUpgradeData.getPathBUpgrades().get("bank");
            stats = pathB.get(level);
            if (stats != null) {
                levelB = level;
                // Update generation interval based on cooldown
                setGenerationInterval(stats.cooldown);
                System.out.println(">>> Bank Tower upgraded Path B to level " + level + " with interval " + getGenerationInterval());
            }
        }
    }

    public int getLevelA() {
        return levelA;
    }

    public int getLevelB() {
        return levelB;
    }
}