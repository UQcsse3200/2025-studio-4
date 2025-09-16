package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.currencysystem.CurrencyComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.TowerStatsComponent;
import com.csse3200.game.components.TowerComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.entities.configs.TowerConfig;
import com.csse3200.game.components.TowerCostComponent;
import com.csse3200.game.services.ServiceLocator;

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
        // ... existing code ...
        TowerComponent tower = new TowerComponent("bone", 2, 2);
        Entity base = new Entity()
                .addComponent(tower)
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.projectileLife, stats.projectileTexture))
                .addComponent(new TowerCostComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, stats.cost))
                .addComponent(new TextureRenderComponent("images/towers/rock1.png"));

        RotatingTextureRenderComponent headRender =
                new RotatingTextureRenderComponent("images/bone.png");
        Entity head = new Entity()
                .addComponent(headRender);

        //wire head to base
        TowerComponent towerbase = base.getComponent(TowerComponent.class);
        towerbase.withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        // Register both
        var es = ServiceLocator.getEntityService();
        es.register(base);
        es.register(head);

        return towerbase.getEntity();
        // ... existing code ...
    }

    /**
     * Creates a Dino Tower entity with its stats and components.
     * @return Entity representing a Dino Tower
     */
    public static Entity createDinoTower() {
        TowerConfig.TowerStats stats = towers.dinoTower;
        // ... existing code ...
        TowerComponent tower = new TowerComponent("dino", 2, 2);
        Entity base = new Entity()
                .addComponent(tower)
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown
                        , stats.projectileSpeed, stats.projectileLife, stats.projectileTexture))
                .addComponent(new TowerCostComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, stats.cost))
                .addComponent(new TextureRenderComponent("images/towers/rock2.png"));

        RotatingTextureRenderComponent headRender =
                new RotatingTextureRenderComponent("images/dino.png");
        Entity head = new Entity()
                .addComponent(headRender);

        //wire head to base
        TowerComponent towerbase = base.getComponent(TowerComponent.class);
        towerbase.withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        // Register both
        var es = ServiceLocator.getEntityService();
        es.register(base);
        es.register(head);

        return towerbase.getEntity();
        // ... existing code ...
    }

    /**
     * Creates a Cavemen Tower entity with its stats and components.
     * @return Entity representing a Cavemen Tower
     */
    public static Entity createCavemenTower() {
        TowerConfig.TowerStats stats = towers.cavemenTower;

        TowerComponent tower = new TowerComponent("cavemen", 2, 2);
        Entity base = new Entity()
                .addComponent(tower)
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown
                        , stats.projectileSpeed, stats.projectileLife, stats.projectileTexture))
                .addComponent(new TowerCostComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, stats.cost))
                .addComponent(new TextureRenderComponent("images/towers/rock4.png"));

        RotatingTextureRenderComponent headRender =
                new RotatingTextureRenderComponent("images/cavemen.png");
        Entity head = new Entity()
                .addComponent(headRender);

        //wire head to base
        TowerComponent towerbase = base.getComponent(TowerComponent.class);
        towerbase.withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

// Register both
        var es = ServiceLocator.getEntityService();
        es.register(base);
        es.register(head);

        return towerbase.getEntity();
    }
}