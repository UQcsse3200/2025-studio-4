package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.deck.DeckComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.towers.BeamAttackComponent;
import core.src.main.com.csse3200.game.components.towers.TowerStatsComponent;
import core.src.main.com.csse3200.game.components.towers.TowerComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.entities.configs.TowerConfig;
import core.src.main.com.csse3200.game.components.towers.OrbitComponent;
import core.src.main.com.csse3200.game.components.towers.StatsBoostComponent;

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
        if (base != null) {
            base.setScale(tileSize * footprintX / 2f, tileSize * footprintY / 2f);
        }
        if (head != null) {
            head.setScale(tileSize * footprintX / 2f, tileSize * footprintY / 2f);
        }
    }

    /**
     * Creates a bone tower entity.
     */
    public static Entity createBoneTower() {
        TowerConfig.TowerStats stats = towers.boneTower.base;

        Entity base = new Entity()
                .addComponent(new TowerComponent("bone", 2, 2))
                .addComponent(new TowerStatsComponent(
                        1, stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.projectileLife,
                        stats.projectileTexture, stats.level_A, stats.level_B))
                .addComponent(new DeckComponent.TowerDeckComponent(
                        "bone", stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.image))
                .addComponent(new TextureRenderComponent("images/towers/rock1.png"));

        RotatingTextureRenderComponent headRender = new RotatingTextureRenderComponent(stats.image);
        Entity head = new Entity().addComponent(headRender);
        base.getComponent(TowerComponent.class)
                .withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        scaleToFootprint(base, head, 2, 2);
        return base;
    }

    /**
     * Creates a dino tower entity.
     */
    public static Entity createDinoTower() {
        TowerConfig.TowerStats stats = towers.dinoTower.base;

        Entity base = new Entity()
                .addComponent(new TowerComponent("dino", 2, 2))
                .addComponent(new TowerStatsComponent(
                        1, stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.projectileLife,
                        stats.projectileTexture, stats.level_A, stats.level_B))
                .addComponent(new DeckComponent.TowerDeckComponent(
                        "dino", stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.image))
                .addComponent(new TextureRenderComponent("images/towers/rock2.png"));

        RotatingTextureRenderComponent headRender = new RotatingTextureRenderComponent(stats.image);
        Entity head = new Entity().addComponent(headRender);
        base.getComponent(TowerComponent.class)
                .withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        scaleToFootprint(base, head, 2, 2);
        return base;
    }

    /**
     * Creates a cavemen tower entity.
     */
    public static Entity createCavemenTower() {
        TowerConfig.TowerStats stats = towers.cavemenTower.base;

        Entity base = new Entity()
                .addComponent(new TowerComponent("cavemen", 3, 3))
                .addComponent(new TowerStatsComponent(
                        1, stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.projectileLife,
                        stats.projectileTexture, stats.level_A, stats.level_B))
                .addComponent(new DeckComponent.TowerDeckComponent(
                        "cavemen", stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.image))
                .addComponent(new TextureRenderComponent("images/towers/rock4.png"));

        RotatingTextureRenderComponent headRender = new RotatingTextureRenderComponent(stats.image);
        Entity head = new Entity().addComponent(headRender);
        base.getComponent(TowerComponent.class)
                .withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        scaleToFootprint(base, head, 3, 3);
        return base;
    }

    /**
     * Creates a pterodactyl tower entity with a nest and an orbiting pterodactyl head.
     */
    public static Entity createPterodactylTower() {
        TowerConfig.TowerStats stats = towers.pterodactylTower.base;

        // Create the nest (base)
        Entity nest = new Entity()
                .addComponent(new TowerComponent("pterodactyl", 3, 3))
                .addComponent(new TowerStatsComponent(
                        1, stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.projectileLife,
                        stats.projectileTexture, stats.level_A, stats.level_B))
                .addComponent(new DeckComponent.TowerDeckComponent(
                        "pterodactyl", stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.image))
                .addComponent(new TextureRenderComponent("images/towers/rock4.png"));

        // Create the pterodactyl head (the part that orbits and shoots)
        RotatingTextureRenderComponent headRender = new RotatingTextureRenderComponent(stats.image);
        Entity head = new Entity()
                .addComponent(headRender)
                .addComponent(new OrbitComponent(nest, stats.range, 0.5f)); // radius and speed can be tuned

        // Attach the head to the nest via TowerComponent
        nest.getComponent(TowerComponent.class)
                .withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        scaleToFootprint(nest, head, 3, 3);
        return nest;
    }

    /**
     * Creates a SuperCavemen tower entity that fires a red beam at enemies while rotating.
     */
    public static Entity createSuperCavemenTower() {
        TowerConfig.TowerStats stats = towers.supercavemenTower.base;

        Entity base = new Entity()
                .addComponent(new TowerComponent("supercavemen", 3, 3))
                .addComponent(new TowerStatsComponent(
                        1, stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.projectileLife,
                        stats.projectileTexture, stats.level_A, stats.level_B))
                .addComponent(new DeckComponent.TowerDeckComponent(
                        "supercavemen", stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.image))
                .addComponent(new TextureRenderComponent("images/towers/rock4.png"));

        RotatingTextureRenderComponent headRender = new RotatingTextureRenderComponent(stats.image);
        // Pass damage and cooldown directly to BeamAttackComponent
        BeamAttackComponent beam = new BeamAttackComponent(
                stats.range,
                stats.damage,
                stats.cooldown
        );
        Entity head = new Entity()
                .addComponent(headRender)
                .addComponent(beam)
                .addComponent(new BeamAttackComponent.BeamRenderComponent(beam));

        base.getComponent(TowerComponent.class)
                .withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        scaleToFootprint(base, head, 3, 3);
        return base;
    }



    /**
     * Creates a totem tower entity.
     */
    public static Entity createTotemTower() {
        TowerConfig.TowerStats stats = towers.totemTower.base;

        Entity base = new Entity()
                .addComponent(new TowerComponent("totem", 2, 2))
                .addComponent(new TowerStatsComponent(
                        1, stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.projectileLife,
                        stats.projectileTexture, stats.level_A, stats.level_B))
                .addComponent(new DeckComponent.TowerDeckComponent(
                        "totem", stats.damage, stats.range, stats.cooldown,
                        stats.projectileSpeed, stats.image))
                .addComponent(new TextureRenderComponent("images/towers/totem.png"))
                .addComponent(new StatsBoostComponent());

        RotatingTextureRenderComponent headRender = new RotatingTextureRenderComponent(stats.image);
        Entity head = new Entity().addComponent(headRender);
        base.getComponent(TowerComponent.class)
                .withHead(head, headRender, new Vector2(0f, 0f), 0.01f);

        scaleToFootprint(base, head, 2, 2);
        return base;
    }

    /**
     * Returns the loaded TowerConfig for external access (e.g., for costs).
     */
    public static TowerConfig getTowerConfig() {
        return towers;
    }
}
