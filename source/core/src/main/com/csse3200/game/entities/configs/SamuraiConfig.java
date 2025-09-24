package com.csse3200.game.entities.configs;

/**
 * Defines a basic set of properties stored in entities config files to be loaded by Entity Factories.
 */
public class SamuraiConfig extends BaseEntityConfig {
    public float movespeed = 220.0f;
    public float swordRadius = 0.9f;
    public float swordAngularSpeed=360.0f;
    public String heroTexture = "images/samurai/Samurai.png";
    public String swordTexture = "images/samurai/Sword.png";
    public String[] levelTextures = {
            "images/samurai/Sword.png",
    };
    public SamuraiConfig() {
        this.health = 120;
        this.baseAttack = 1000;
        this.heroTexture = "images/samurai/Samurai.png";
        this.levelTextures = new String[] {
                "images/samurai/Samurai.png"
        };
    }
}