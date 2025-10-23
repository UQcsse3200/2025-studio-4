package com.csse3200.game.entities.configs;

/**
 * Defines a basic set of properties stored in entities config files to be loaded by Entity Factories.
 */
public class SamuraiConfig extends HeroConfig {
    public float movespeed = 220.0f;
    public float swordRadius = 0.9f;
    public float swordAngularSpeed=360.0f;

    public int[] swordDamageByLevel = {10, 16, 24};

    public int[] jabDamageByLevel   = {20, 26, 28};
    public int[] sweepDamageByLevel = {12, 18, 26};
    public int[] spinDamageByLevel  = { 8, 14, 22};

    public String heroTexture = "images/samurai/Samurai.png";
    public String[] levelTextures = {
            "images/samurai/Sword.png",
    };
    public SamuraiConfig() {
        this.health = 120;
        this.baseAttack = 0;
        this.heroTexture = "images/samurai/Samurai.png";
        this.levelTextures = new String[] {
                "images/samurai/Samurai.png"
        };
    }

    public String swordTexture = "images/samurai/Sword.png";
    public String[] swordLevelTextures = {
            "images/samurai/Sword.png",              // L1
            "images/samurai/Katana_Level_2.png"     // L2
    };
}