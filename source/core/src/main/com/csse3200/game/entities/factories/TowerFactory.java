package com.csse3200.game.entities.factories;

import com.csse3200.game.components.currencysystem.CurrencyComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.TowerStatsComponent;
import com.csse3200.game.components.TowerComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.entities.configs.TowerConfig;
import com.csse3200.game.components.TowerCostComponent;

/**
 * Factory class for creating different types of tower entities.
 * Loads tower stats from configuration and attaches relevant components.
 */
public class TowerFactory {
    /** Tower configuration loaded from JSON file. */
    private static final TowerConfig towers =
            FileLoader.readClass(TowerConfig.class, "configs/tower.json");

    /**
     * Creates a Bone Tower entity with its stats and components.
     * @return Entity representing a Bone Tower
     */
    public static Entity createBoneTower() {
        TowerConfig.TowerStats stats = towers.boneTower;
        return new Entity()
                .addComponent(new TowerComponent("bone", 2, 2)) // revert to 2x2
                .addComponent(new TowerCostComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, stats.cost))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown))
                .addComponent(new TextureRenderComponent("images/bone.png"));
    }

    /**
     * Creates a Dino Tower entity with its stats and components.
     * @return Entity representing a Dino Tower
     */
    public static Entity createDinoTower() {
        TowerConfig.TowerStats stats = towers.dinoTower;
        return new Entity()
                .addComponent(new TowerComponent("dino", 2, 2))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown))
                .addComponent(new TowerCostComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, stats.cost))
                .addComponent(new TextureRenderComponent("images/dino.png"));
    }

    /**
     * Creates a Cavemen Tower entity with its stats and components.
     * @return Entity representing a Cavemen Tower
     */
    public static Entity createCavemenTower() {
        TowerConfig.TowerStats stats = towers.cavemenTower;
        return new Entity()
                .addComponent(new TowerComponent("cavemen", 2, 2))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown))
                .addComponent(new TowerCostComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, stats.cost))
                .addComponent(new TextureRenderComponent("images/cavemen.png"));
    }
}
