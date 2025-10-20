package com.csse3200.game.components.deck;

import com.csse3200.game.entities.configs.EngineerConfig;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.entities.configs.SamuraiConfig;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A book deck for heroes.
 * Each hero subclass (HeroConfig / EngineerConfig / SamuraiConfig) has different fields.
 * This class builds the ordered stat map directly.
 */
public class HeroBookDeckComponent extends DeckComponent {

    private HeroBookDeckComponent(Map<StatType, String> stats) {
        super(stats);
    }

    // === Base hero ===
    public static HeroBookDeckComponent from(String name, HeroConfig cfg) {
        Map<StatType, String> stats = new LinkedHashMap<>();

        stats.put(StatType.NAME, name.toUpperCase());
        stats.put(StatType.MAX_HEALTH, String.valueOf(cfg.health));
        stats.put(StatType.DAMAGE, String.valueOf(cfg.baseAttack));
        stats.put(StatType.SPEED, String.valueOf(cfg.moveSpeed));
        stats.put(StatType.COOLDOWN, String.valueOf(cfg.attackCooldown));
        stats.put(StatType.PROJECTILE_SPEED, String.valueOf(cfg.bulletSpeed));
        stats.put(StatType.PROJECTILE_LIFE, String.valueOf(cfg.bulletLife));
        stats.put(StatType.TEXTURE_PATH, cfg.heroTexture);
        //stats.put(StatType.BULLET_TEXTURE_PATH, cfg.bulletTexture);
        //stats.put(StatType.LEVEL_TEXTURES, String.join(", ", cfg.levelTextures));

        return new HeroBookDeckComponent(stats);
    }

    // === Engineer hero ===
    public static HeroBookDeckComponent from(String name, EngineerConfig cfg) {
        Map<StatType, String> stats = new LinkedHashMap<>();

        stats.put(StatType.NAME, name.toUpperCase());
        stats.put(StatType.MAX_HEALTH, String.valueOf(cfg.health));
        stats.put(StatType.DAMAGE, String.valueOf(cfg.baseAttack));
        stats.put(StatType.SPEED, String.valueOf(cfg.moveSpeed));
        stats.put(StatType.COOLDOWN, String.valueOf(cfg.attackCooldown));
        stats.put(StatType.PROJECTILE_SPEED, String.valueOf(cfg.bulletSpeed));
        stats.put(StatType.PROJECTILE_LIFE, String.valueOf(cfg.bulletLife));
        stats.put(StatType.TEXTURE_PATH, cfg.heroTexture);
        //stats.put(StatType.BULLET_TEXTURE_PATH, cfg.bulletTexture);
        //stats.put(StatType.LEVEL_TEXTURES, String.join(", ", cfg.levelTextures));
        return new HeroBookDeckComponent(stats);
    }

    // === Samurai hero ===
    public static HeroBookDeckComponent from(String name, SamuraiConfig cfg) {
        Map<StatType, String> stats = new LinkedHashMap<>();

        stats.put(StatType.NAME, name.toUpperCase());
        stats.put(StatType.MAX_HEALTH, String.valueOf(cfg.health));
        stats.put(StatType.DAMAGE, String.valueOf(cfg.baseAttack));
        stats.put(StatType.SPEED, String.valueOf(cfg.movespeed));
        stats.put(StatType.TEXTURE_PATH, cfg.heroTexture);

        return new HeroBookDeckComponent(stats);
    }
}
