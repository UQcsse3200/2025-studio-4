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

    public static Entity createBaseTower() {
        TowerConfig.TowerStats stats = towers.baseTower;
        return new Entity()
                .addComponent(new TowerComponent("base", 2, 2)) // revert to 2x2
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown))
                .addComponent(new TextureRenderComponent("images/base_tower.png"));
    }

    public static Entity createSunTower() {
        TowerConfig.TowerStats stats = towers.sunTower;
        return new Entity()
                .addComponent(new TowerComponent("sun", 2, 2))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown))
                .addComponent(new TextureRenderComponent("images/sun.png"));
    }

    public static Entity createArcherTower() {
        TowerConfig.TowerStats stats = towers.archerTower;
        return new Entity()
                .addComponent(new TowerComponent("archer", 1, 1))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown))
                .addComponent(new TextureRenderComponent("images/archer.png"));
    }
}
