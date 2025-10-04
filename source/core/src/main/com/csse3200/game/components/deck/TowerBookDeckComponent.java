package com.csse3200.game.components.deck;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A deck component that represents a tower's stats in a "book deck" format.
 * This includes both gameplay-related stats (damage, range, cooldown, etc.)
 * and descriptive information (lore, texture path, lock status).
 *
 * <p>The stats are stored in an ordered map to preserve insertion order, ensuring
 * a consistent display sequence in the UI.</p>
 */
public class TowerBookDeckComponent extends DeckComponent {
    /**
     * Constructs a book deck for a tower with extended stats.
     *
     * @param name             tower name
     * @param damage           base damage dealt by the tower
     * @param range            attack range of the tower
     * @param cooldown         time delay between attacks (in seconds)
     * @param projectileSpeed  speed of the projectile fired
     * @param projectileLife   lifespan of the projectile before it disappears
     * @param metalScrapCost   cost in metal scrap resources (currently unused)
     * @param titaniumCoreCost cost in titanium core resources (currently unused)
     * @param neurochipCost    cost in neurochip resources (currently unused)
     * @param lore             description or background information about the tower
     * @param texturePath      path to the tower's texture image
     * @param locked           whether the tower is locked ("true") or unlocked ("false")
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
            String texturePath,
            String locked
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
                        texturePath,
                        locked
                )
        );
    }

    /**
     * Creates an ordered map of tower stats for display in the deck.
     * The insertion order is preserved, which defines how stats appear in the UI.
     *
     * @param name             tower name
     * @param damage           base damage dealt by the tower
     * @param range            attack range of the tower
     * @param cooldown         time delay between attacks (rounded to 2 decimal places)
     * @param projectileSpeed  speed of the projectile fired
     * @param projectileLife   lifespan of the projectile before it disappears
     * @param metalScrapCost   cost in metal scrap resources (currently unused)
     * @param titaniumCoreCost cost in titanium core resources (currently unused)
     * @param neurochipCost    cost in neurochip resources (currently unused)
     * @param lore             description or background information about the tower
     * @param texturePath      path to the tower's texture image
     * @param locked           whether the tower is locked ("true") or unlocked ("false")
     * @return a {@link Map} of {@link StatType} to their corresponding string values
     */
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
            String texturePath,
            String locked
    ) {
        Map<StatType, String> stats = new LinkedHashMap<>();
        stats.put(StatType.NAME, name.toUpperCase());
        stats.put(StatType.DAMAGE, String.valueOf(damage));
        stats.put(StatType.RANGE, String.valueOf(range));
        stats.put(StatType.COOLDOWN, String.valueOf(Math.floor(cooldown * 100) / 100));
        stats.put(StatType.PROJECTILE_SPEED, String.valueOf(projectileSpeed));
//        stats.put(StatType.PROJECTILE_LIFE, String.valueOf(Math.floor(projectileLife * 100) / 100));
        stats.put(StatType.LORE, lore);
        stats.put(StatType.TEXTURE_PATH, texturePath);
        stats.put(StatType.LOCKED, locked);
        // resource costs
//        stats.put(StatType.METAL_SCRAP_COST, String.valueOf(metalScrapCost));
//        stats.put(StatType.TITANIUM_CORE_COST, String.valueOf(titaniumCoreCost));
//        stats.put(StatType.NEUROCHIP_COST, String.valueOf(neurochipCost));
        return stats;
    }
}
