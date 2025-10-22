package com.csse3200.game.components.deck;

import com.csse3200.game.entities.configs.EngineerConfig;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.entities.configs.SamuraiConfig;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A specialized {@link DeckComponent} for representing hero data in the in-game Book UI.
 * <p>
 * Each hero subclass ({@link HeroConfig}, {@link EngineerConfig}, {@link SamuraiConfig})
 * defines different fields and gameplay roles. This class constructs a deck entry
 * ({@code Map<StatType, String>}) for each hero type, containing ordered key-value pairs
 * such as health, damage, cooldown, textures, and a lore description.
 * </p>
 *
 * <p>
 * The deck entries created here are displayed as individual hero "pages" inside the Heroes Book,
 * allowing players to view stats, images, and brief descriptions for each hero.
 * </p>
 */
public class HeroBookDeckComponent extends DeckComponent {

    /**
     * Creates a new {@link HeroBookDeckComponent} from a prepared stats map.
     *
     * @param stats ordered hero statistics to be rendered in the book UI
     */
    private HeroBookDeckComponent(Map<StatType, String> stats) {
        super(stats);
    }

    /**
     * Creates a deck entry for the default Hero.
     * <p>
     * The Hero is a ranged fighter who rotates to face the player's cursor and fires projectiles
     * in that direction. Each projectile damages enemies upon contact.
     * </p>
     *
     * @param name the hero's display name
     * @param cfg  the {@link HeroConfig} containing hero attributes (health, attack, textures, etc.)
     * @return a {@link HeroBookDeckComponent} containing ordered hero stats and description
     */
    public static HeroBookDeckComponent from(String name, HeroConfig cfg) {
        Map<StatType, String> stats = new LinkedHashMap<>();

        stats.put(StatType.NAME, name.toUpperCase());
        stats.put(StatType.MAX_HEALTH, String.valueOf(cfg.health));
        stats.put(StatType.DAMAGE, String.valueOf(cfg.baseAttack));
        stats.put(StatType.COOLDOWN, String.valueOf(cfg.attackCooldown));
        stats.put(StatType.PROJECTILE_SPEED, String.valueOf(cfg.bulletSpeed));
        stats.put(StatType.PROJECTILE_LIFE, String.valueOf(cfg.bulletLife));
        stats.put(StatType.TEXTURE_PATH, cfg.heroTexture);
        stats.put(StatType.LORE, "A ranged fighter who follows the cursor. Rotates to face the mouse and fires projectiles that damage enemies on contact.");

        return new HeroBookDeckComponent(stats);
    }

    /**
     * Creates a deck entry for the Engineer hero.
     * <p>
     * The Engineer acts as a tactical builder who can deploy three types of support structures:
     * a wall to slow enemies, a 4-way turret that automatically fires in all directions,
     * and a currency tower that generates extra income.
     * </p>
     *
     * @param name the hero's display name
     * @param cfg  the {@link EngineerConfig} containing engineer attributes and textures
     * @return a {@link HeroBookDeckComponent} containing ordered engineer stats and description
     */
    public static HeroBookDeckComponent from(String name, EngineerConfig cfg) {
        Map<StatType, String> stats = new LinkedHashMap<>();

        stats.put(StatType.NAME, name.toUpperCase());
        stats.put(StatType.MAX_HEALTH, String.valueOf(cfg.health));
        stats.put(StatType.DAMAGE, String.valueOf(cfg.baseAttack));
        stats.put(StatType.COOLDOWN, String.valueOf(cfg.attackCooldown));
        stats.put(StatType.PROJECTILE_SPEED, String.valueOf(cfg.bulletSpeed));
        stats.put(StatType.PROJECTILE_LIFE, String.valueOf(cfg.bulletLife));
        stats.put(StatType.TEXTURE_PATH, cfg.heroTexture);
        stats.put(StatType.LORE, "A tactical builder who deploys support structures: a wall to block foes, a 4-way turret for automatic fire, and a currency tower for passive income.");
        return new HeroBookDeckComponent(stats);
    }

    /**
     * Creates a deck entry for the Samurai hero.
     * <p>
     * The Samurai is a close-range melee fighter with three distinct attack moves:
     * jab (1), slash (2), and spin (3), providing fast and versatile combat options.
     * </p>
     *
     * @param name the hero's display name
     * @param cfg  the {@link SamuraiConfig} containing samurai attributes and textures
     * @return a {@link HeroBookDeckComponent} containing ordered samurai stats and description
     */
    public static HeroBookDeckComponent from(String name, SamuraiConfig cfg) {
        Map<StatType, String> stats = new LinkedHashMap<>();

        stats.put(StatType.NAME, name.toUpperCase());
        stats.put(StatType.MAX_HEALTH, String.valueOf(cfg.health));
        stats.put(StatType.DAMAGE, String.valueOf(cfg.baseAttack));
        stats.put(StatType.TEXTURE_PATH, cfg.heroTexture);
        stats.put(StatType.LORE, "A close-range warrior with three distinct attacks: jab (1), slash (2), and spin (3) for fast, versatile melee combat.");

        return new HeroBookDeckComponent(stats);
    }
}
