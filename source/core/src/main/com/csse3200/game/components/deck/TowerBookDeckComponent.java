package com.csse3200.game.components.deck;

import java.util.LinkedHashMap;
import java.util.Map;

public class TowerBookDeckComponent extends DeckComponent {
    /**
     * Constructs a book deck for a tower with extended stats.
     *
     * @param name        tower name
     * @param damage      base damage
     * @param range       attack range
     * @param cooldown    attack cooldown
     * @param projectileSpeed  projectile speed
     * @param projectileLife   projectile lifespan
     * @param metalScrapCost   metal scrap cost
     * @param titaniumCoreCost titanium core cost
     * @param neurochipCost    neurochip cost
     * @param lore             description / background
     * @param texturePath      image path
     */
    public TowerBookDeckComponent(
            String name,
            int damage,
            double range,
            double cooldown,
            double projectileSpeed,
            double projectileLife,
            int metalScrapCost,
            int titaniumCoreCost,
            int neurochipCost,
            String lore,
            String texturePath
    ) {
        super(
                createOrderedStats(
                        name,
                        damage,
                        range,
                        cooldown,
                        projectileSpeed,
                        projectileLife,
                        metalScrapCost,
                        titaniumCoreCost,
                        neurochipCost,
                        lore,
                        texturePath
                )
        );
    }

    private static Map<StatType, String> createOrderedStats(
            String name,
            int damage,
            double range,
            double cooldown,
            double projectileSpeed,
            double projectileLife,
            int metalScrapCost,
            int titaniumCoreCost,
            int neurochipCost,
            String lore,
            String texturePath
    ) {
        Map<StatType, String> stats = new LinkedHashMap<>();
        stats.put(StatType.NAME, name.toUpperCase());
        stats.put(StatType.DAMAGE, String.valueOf(damage));
        stats.put(StatType.RANGE, String.valueOf(range));
        stats.put(StatType.COOLDOWN, String.valueOf(cooldown));
        stats.put(StatType.PROJECTILE_SPEED, String.valueOf(projectileSpeed));
        stats.put(StatType.SPEED, String.valueOf(projectileLife)); // reusing SPEED icon for projectile life?
        stats.put(StatType.LORE, lore);
        stats.put(StatType.TEXTURE_PATH, texturePath);

        // resource costs
        stats.put(StatType.HEALTH, String.valueOf(metalScrapCost));     // maybe swap HEALTH icon to a scrap icon
        stats.put(StatType.MAX_HEALTH, String.valueOf(titaniumCoreCost));
        stats.put(StatType.RESISTANCE, String.valueOf(neurochipCost));

        return stats;
    }
}
