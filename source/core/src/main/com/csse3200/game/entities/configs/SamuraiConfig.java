package com.csse3200.game.entities.configs;

/**
 * Defines a basic set of properties stored in entities config files to be loaded by Entity Factories.
 */
public class SamuraiConfig extends HeroConfig {
    public float movespeed = 220.0f;
    public float swordRadius = 0.9f;
    public float swordAngularSpeed = 360.0f;

    public int[] swordDamageByLevel = {10, 16, 24};

    public String heroTexture = "images/samurai/Samurai.png";

    // 角色分级外观（暂留）
    public String[] levelTextures = {
            "images/samurai/Samurai.png"
    };

    // 初始与可选武器贴图
    public String swordTexture  = "images/samurai/Sword.png";   // “Normal Sword”
    public String swordTexture2 = "images/samurai/Sword2.png";  // “Weapon 2”
    // 你有第三把就继续加：
    public String swordTexture3 = null; // 例如 "images/samurai/Sword3.png"

    // 刀的分级外观（按需要保留）
    public String[] swordLevelTextures = {
            "images/samurai/Sword.png",            // L1
            "images/samurai/Katana_Level_2.png"    // L2
    };

    public SamuraiConfig() {
        this.health = 120;
        this.baseAttack = 0;
        this.heroTexture = "images/samurai/Samurai.png";
        this.levelTextures = new String[] { "images/samurai/Samurai.png" };
    }

    /** UI 显示名 → 贴图路径（默认兜底 Normal Sword）。 */
    public String getSwordTextureForLabel(String label) {
        if (label == null) return swordTexture;
        String s = label.trim().toLowerCase();
        if ("weapon 2".equals(s) && swordTexture2 != null && !swordTexture2.isBlank()) return swordTexture2;
        if ("weapon 3".equals(s) && swordTexture3 != null && !swordTexture3.isBlank()) return swordTexture3;
        // "normal sword" 或其他任意值都回退
        return swordTexture;
    }
}
