package com.csse3200.game.entities.configs;

/**
 * Configuration for the Engineer hero.
 * Extends HeroConfig to reuse base fields (health, baseAttack, textures).
 */

public class EngineerConfig extends HeroConfig {
  public float summonCooldown = 5f;
  public int   maxSummons     = 3;
  public float summonSpeed    = 250f;
  public String summonTexture = "images/engineer/Sentry.png";

  public String bulletTexture = "images/engineer/Bullet.png";
  public EngineerConfig() {
    this.health = 120;
    this.baseAttack = 1;
    this.heroTexture = "images/engineer/Engineer.png";
    // 覆盖父类默认的普通英雄贴图
    this.levelTextures = new String[] {
            "images/engineer/Engineer.png"   // 如果暂时没有多级皮肤，就放同一张
            // "images/engineer/Engineer_Lv2.png", ...
    };
  }
}


