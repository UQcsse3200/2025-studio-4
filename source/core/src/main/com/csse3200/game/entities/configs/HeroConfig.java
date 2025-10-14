package com.csse3200.game.entities.configs;

/**
 * Configuration class for the hero entity.
 * <p>
 * Extends {@link BaseEntityConfig}, which already provides
 * common fields such as {@code health} and {@code baseAttack}.
 * This class adds hero-specific attributes such as movement speed,
 * attack cooldown, projectile properties, and texture paths.
 * Values can be loaded from JSON via {@code FileLoader}.
 * </p>
 */
public class HeroConfig extends BaseEntityConfig {
  /** Movement speed in pixels per second */
  public float moveSpeed = 220f;

  /** Attack cooldown (seconds per shot) — lower values mean faster attack speed */
  public float attackCooldown = 1.80f;

  /** Bullet lifetime (seconds) */
  public float bulletLife = 1.6f;

  /** Bullet speed (pixels per second) */
  public float bulletSpeed = 400f;

  /** Path to hero sprite texture (relative to assets) */
  public String heroTexture = "images/hero/Heroshoot.png";

  /** Path to bullet sprite texture (relative to assets) */
  public String bulletTexture = "images/hero/Bullet.png";

  public String[] levelTextures = {
          "images/hero/Heroshoot.png",
          "images/hero/Hero_level2.png",
  };
  public String shootSfx;
  public Float shootSfxVolume;

  /**
   * Default constructor.
   * <p>
   * By default, {@link BaseEntityConfig} provides:
   * {@code health = 1}, {@code baseAttack = 0}.
   * Here we override with more appropriate hero starting values.
   * </p>
   */
  public HeroConfig() {
    this.health = 100;
    this.baseAttack = 8;
  }
}

