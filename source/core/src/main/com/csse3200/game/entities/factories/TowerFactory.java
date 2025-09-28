package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.components.deck.DeckComponent;
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
 * Ensures the tower base and head scale to match their footprint.
 */
public class TowerFactory {
    private static final TowerConfig towers =
            FileLoader.readClass(TowerConfig.class, "configs/tower.json");

    /**
     * Scales the tower base and head entities to match the specified footprint size.
     *
     * @param base       Tower base entity.
     * @param head       Tower head entity (can be null).
     * @param footprintX Width in tiles.
     * @param footprintY Height in tiles.
     */
    private static void scaleToFootprint(Entity base, Entity head, int footprintX, int footprintY) {
        float tileSize = 1f; // 1 unit per tile in world coordinates
        base.setScale(tileSize * footprintX / 2f, tileSize * footprintY / 2f);
        if (head != null) {
            head.setScale(tileSize * footprintX / 2f, tileSize * footprintY / 2f);
        }
    }

    /**
     * Creates a tower entity of the specified type, footprint, currency, stats, and texture.
     *
     * @param type         The tower type string.
     * @param footprintX   Width in tiles.
     * @param footprintY   Height in tiles.
     * @param currencyType The currency type used for purchase.
     * @param stats        The tower stats configuration.
     * @param texturePath  The texture path for the tower base.
     * @return The constructed tower entity.
     */
    private static Entity createTower(String type, int footprintX, int footprintY, CurrencyType currencyType, TowerConfig.TowerStats stats, String texturePath) {
        Map<CurrencyType, Integer> costMap = new HashMap<>();
        costMap.put(currencyType, stats.metalScrapCost);

        Entity base = new Entity()
                .addComponent(new TowerComponent(type, footprintX, footprintY))
                .addComponent(new TowerCostComponent(costMap))
                .addComponent(new TowerStatsComponent(
                        1, stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.projectileLife,
                        stats.projectileTexture, stats.level_A, stats.level_B))
                .addComponent(new DeckComponent.TowerDeckComponent(type, stats.damage, stats.range, stats.cooldown, stats.projectileSpeed, texturePath))
                .addComponent(new TextureRenderComponent(texturePath));

        base.getComponent(TowerComponent.class).setSelectedPurchaseCurrency(currencyType);

        RotatingTextureRenderComponent headRender = new RotatingTextureRenderComponent(stats.image);
        Entity head = new Entity().addComponent(headRender);

        base.getComponent(TowerComponent.class)
                .withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        scaleToFootprint(base, head, footprintX, footprintY);
        return base;
    }

    /**
     * Creates a bone tower entity using the specified currency type.
     *
     * @param currencyType The currency type used for purchase.
     * @return The constructed bone tower entity.
     */
    public static Entity createBoneTower(CurrencyType currencyType) {
        TowerConfig.TowerStats stats = towers.boneTower.base;
        return createTower("bone", 2, 2, currencyType, stats, "images/towers/rock1.png");
    }

    /**
     * Creates a dino tower entity using the specified currency type.
     *
     * @param currencyType The currency type used for purchase.
     * @return The constructed dino tower entity.
     */
    public static Entity createDinoTower(CurrencyType currencyType) {
        TowerConfig.TowerStats stats = towers.dinoTower.base;
        return createTower("dino", 2, 2, currencyType, stats, "images/towers/rock2.png");
    }

    /**
     * Creates a cavemen tower entity using the specified currency type.
     *
     * @param currencyType The currency type used for purchase.
     * @return The constructed cavemen tower entity.
     */
    public static Entity createCavemenTower(CurrencyType currencyType) {
        TowerConfig.TowerStats stats = towers.cavemenTower.base;
        return createTower("cavemen", 3, 3, currencyType, stats, "images/towers/rock4.png");
    }
}
