package com.csse3200.game.entities.factories;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.TowerStatsComponent;
import com.csse3200.game.components.TowerComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.files.FileLoader;
import core.src.main.com.csse3200.game.entities.configs.TowerConfig;

public class TowerFactory {
    private static final TowerConfig towers =
            FileLoader.readClass(TowerConfig.class, "configs/tower.json");

    public static Entity createBoneTower() {
        TowerConfig.TowerStats stats = towers.boneTower;
        return new Entity()
                .addComponent(new TowerComponent("bone", 2, 2)) // revert to 2x2
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown))
                .addComponent(new TextureRenderComponent("images/bone.png"));
    }

    public static Entity createDinoTower() {
        TowerConfig.TowerStats stats = towers.dinoTower;
        return new Entity()
                .addComponent(new TowerComponent("dino", 2, 2))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown))
                .addComponent(new TextureRenderComponent("images/dino.png"));
    }

    public static Entity createCavemenTower() {
        TowerConfig.TowerStats stats = towers.cavemenTower;
        return new Entity()
                .addComponent(new TowerComponent("cavemen", 1, 1))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown))
                .addComponent(new TextureRenderComponent("images/cavemen.png"));
    }
}
