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
        Entity base = new Entity()
                .addComponent(new TowerComponent("bone", 2, 2))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.projectileLife, stats.projectileTexture))
                .addComponent(new TowerCostComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, stats.cost))
                .addComponent(new TextureRenderComponent("images/towers/rock1.png"));

        RotatingTextureRenderComponent headRender =
                new RotatingTextureRenderComponent("images/bone.png");
        Entity head = new Entity()
                .addComponent(headRender);

        // Wire head to base (with a tiny Y nudge so it draws above)
        TowerComponent tower = base.getComponent(TowerComponent.class);
        tower.withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        return base;
    }

    /**
     * Creates a Dino Tower entity with its stats and components.
     * @return Entity representing a Dino Tower
     */
    public static Entity createDinoTower() {
        TowerConfig.TowerStats stats = towers.dinoTower;

        Entity base = new Entity()
                .addComponent(new TowerComponent("dino", 2, 2))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown
                        , stats.projectileSpeed, stats.projectileLife, stats.projectileTexture))
                .addComponent(new TowerCostComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, stats.cost))
                .addComponent(new TextureRenderComponent("images/towers/rock2.png"));

        RotatingTextureRenderComponent headRender =
                new RotatingTextureRenderComponent("images/dino.png");
        Entity head = new Entity().addComponent(headRender);

        TowerComponent tower = base.getComponent(TowerComponent.class);
        tower.withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        return base;
    }

    /**
     * Creates a Cavemen Tower entity with its stats and components.
     * @return Entity representing a Cavemen Tower
     */
    public static Entity createCavemenTower() {
        TowerConfig.TowerStats stats = towers.cavemenTower;

        Entity base = new Entity()
                .addComponent(new TowerComponent("cavemen", 2, 2))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown
                        , stats.projectileSpeed, stats.projectileLife, stats.projectileTexture))
                .addComponent(new TowerCostComponent(CurrencyComponent.CurrencyType.METAL_SCRAP, stats.cost))
                .addComponent(new TextureRenderComponent("images/towers/rock4.png"));

        RotatingTextureRenderComponent headRender =
                new RotatingTextureRenderComponent("images/cavemen.png");
        Entity head = new Entity()
                .addComponent(headRender);

        TowerComponent tower = base.getComponent(TowerComponent.class);
        tower.withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        return base;
    }
}