package com.csse3200.game.entities;

import com.csse3200.game.components.TowerStatsComponent;
import com.csse3200.game.components.TowerComponent;

public class TowerFactory {
    public static Entity createBaseTower() {
        Entity tower = new Entity()
                .addComponent(new TowerStatsComponent(10, 5.0f, 1.0f)) // Example stats
                .addComponent(new TowerComponent());
        return tower;
    }
}