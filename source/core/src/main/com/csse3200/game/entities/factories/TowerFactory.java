package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.components.deck.DeckDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.TowerStatsComponent;
import com.csse3200.game.components.TowerComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.entities.configs.TowerConfig;
import com.csse3200.game.components.TowerCostComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating different types of tower entities.
 */
public class TowerFactory {
    private static final TowerConfig towers =
            FileLoader.readClass(TowerConfig.class, "configs/tower.json");

    /**
     * Creates a Bone Tower entity with the specified currency type for cost.
     *
     * @param currencyType The currency type used for purchasing the tower.
     * @return The created Bone Tower entity.
     */
    public static Entity createBoneTower(CurrencyType currencyType) {
        TowerConfig.TowerStats stats = towers.boneTower;

        Map<CurrencyType, Integer> costMap = new HashMap<>();
        costMap.put(currencyType, stats.metalScrapCost);

        Entity base = new Entity()
                .addComponent(new TowerComponent("bone", 2, 2))
                .addComponent(new TowerCostComponent(costMap))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.projectileLife, stats.projectileTexture))
                .addComponent(new TextureRenderComponent("images/towers/rock1.png"))
                .addComponent(new DeckComponent.TowerDeckComponent("Bone Tower",stats.damage, stats.range,
                        stats.cooldown, "images/bone.png"));

        base.getComponent(TowerComponent.class).setSelectedPurchaseCurrency(currencyType);

        RotatingTextureRenderComponent headRender = new RotatingTextureRenderComponent("images/bone.png");
        Entity head = new Entity().addComponent(headRender);

        base.getComponent(TowerComponent.class)
                .withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        return base;
    }

    /**
     * Creates a Dino Tower entity with the specified currency type for cost.
     *
     * @param currencyType The currency type used for purchasing the tower.
     * @return The created Dino Tower entity.
     */
    public static Entity createDinoTower(CurrencyType currencyType) {
        TowerConfig.TowerStats stats = towers.dinoTower;

        Map<CurrencyType, Integer> costMap = new HashMap<>();
        costMap.put(currencyType, stats.metalScrapCost);

        Entity base = new Entity()
                .addComponent(new TowerComponent("dino", 2, 2))
                .addComponent(new TowerCostComponent(costMap))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.projectileLife, stats.projectileTexture))
                .addComponent(new TextureRenderComponent("images/towers/rock2.png"))
                .addComponent(new DeckComponent.TowerDeckComponent("Dino Tower",stats.damage, stats.range,
                        stats.cooldown, "images/dino.png"));

        base.getComponent(TowerComponent.class).setSelectedPurchaseCurrency(currencyType);

        RotatingTextureRenderComponent headRender = new RotatingTextureRenderComponent("images/dino.png");
        Entity head = new Entity().addComponent(headRender);

        base.getComponent(TowerComponent.class)
                .withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        return base;
    }

    /**
     * Creates a Cavemen Tower entity with the specified currency type for cost.
     *
     * @param currencyType The currency type used for purchasing the tower.
     * @return The created Cavemen Tower entity.
     */
    public static Entity createCavemenTower(CurrencyType currencyType) {
        TowerConfig.TowerStats stats = towers.cavemenTower;

        Map<CurrencyType, Integer> costMap = new HashMap<>();
        costMap.put(currencyType, stats.metalScrapCost);

        Entity base = new Entity()
                .addComponent(new TowerComponent("cavemen", 2, 2))
                .addComponent(new TowerCostComponent(costMap))
                .addComponent(new TowerStatsComponent(1, stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.projectileLife, stats.projectileTexture))
                .addComponent(new TextureRenderComponent("images/towers/rock4.png"))
                .addComponent(new DeckComponent.TowerDeckComponent("Cavemen Tower",stats.damage, stats.range,
                        stats.cooldown, "images/cavemen.png"));

        base.getComponent(TowerComponent.class).setSelectedPurchaseCurrency(currencyType);

        RotatingTextureRenderComponent headRender = new RotatingTextureRenderComponent("images/cavemen.png");
        Entity head = new Entity().addComponent(headRender);

        base.getComponent(TowerComponent.class)
                .withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        return base;
    }
}
