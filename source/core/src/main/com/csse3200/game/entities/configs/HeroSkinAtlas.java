// HeroSkinAtlas.java
package com.csse3200.game.entities.configs;

import com.csse3200.game.services.GameStateService;

public final class HeroSkinAtlas {
    private HeroSkinAtlas(){}

    public static String body(GameStateService.HeroType t, String skin){
        return switch (t) {
            case ENGINEER -> switch (skin) {
                case "khaki" -> "images/engineer/Engineer_khaki.png";
                case "steel" -> "images/engineer/Engineer_steel.png";
                default      -> "images/engineer/Engineer.png";
            };
            case SAMURAI -> switch (skin) {
                case "purple" -> "images/samurai/Samurai_purple.png";
                case "azure"   -> "images/samurai/Samurai_azure.png";
                default        -> "images/samurai/Samurai.png";
            };
            case HERO -> switch (skin) {
                case "purple" -> "images/hero/Heroshoot_purple.png";
                case "neo"    -> "images/hero/Heroshoot_neo.png";
                default       -> "images/hero/Heroshoot.png";
            };
        };
    }

    // ★ 新增：针对 HERO 的“形态化”身体贴图
    public static String bodyForForm(GameStateService.HeroType t, String skin, int form) {
        if (t != GameStateService.HeroType.HERO) {
            // 其他英雄不分形态：沿用单张
            return body(t, skin);
        }
        // HERO：同一皮肤下按 1/2/3 形态返回不同贴图（路径按你的资源名改）
        return switch (skin) {
            case "purple" -> switch (form) {
                case 1 -> "images/hero/Heroshoot_purple.png";
                case 2 -> "images/hero2/Heroshoot_purple.png";
                case 3 -> "images/hero3/Heroshoot_purple.png";
                default -> "images/hero/Heroshoot_purple.png";
            };
            case "neo" -> switch (form) {
                case 1 -> "images/hero/Heroshoot_neo.png";
                case 2 -> "images/hero2/Heroshoot_neo.png";
                case 3 -> "images/hero3/Heroshoot_neo.png";
                default -> "images/hero/Heroshoot_neo.png";
            };
            default -> switch (form) {
                case 1 -> "images/hero/Heroshoot.png";
                case 2 -> "images/hero2/Heroshoot.png";
                case 3 -> "images/hero3/Heroshoot.png";
                default -> "images/hero/Heroshoot.png";
            };
        };
    }

    public static String bullet(GameStateService.HeroType t, String skin){
        return switch (t) {
            case ENGINEER -> switch (skin) {
                default      -> "images/engineer/Bullet.png";
            };
            case SAMURAI -> switch (skin) {
                case "crimson" -> "images/hero3/Bullet_crimson.png";
                case "azure"   -> "images/hero3/Bullet_azure.png";
                default        -> "images/hero3/Bullet.png";
            };
            case HERO -> "images/hero/Bullet.png";
        };
    }
}
