// HeroSkinAtlas.java
package com.csse3200.game.entities.configs;

import com.csse3200.game.services.GameStateService;

/**
 * Central mapping from (hero type, skin [, form]) to texture paths.
 * Contains helpers for body, sword, and bullet textures.
 */
public final class HeroSkinAtlas {
    private HeroSkinAtlas() {
    }

    /**
     * Resolve the base body texture for a given hero type and skin.
     */
    public static String body(GameStateService.HeroType t, String skin) {
        return switch (t) {
            case ENGINEER -> switch (skin) {
                case "khaki" -> "images/engineer/Engineer_khaki.png";
                case "steel" -> "images/engineer/Engineer_steel.png";
                default -> "images/engineer/Engineer.png";
            };
            case SAMURAI -> switch (skin) {
                case "purple" -> "images/samurai/Samurai_purple.png";
                case "azure" -> "images/samurai/Samurai_azure.png";
                default -> "images/samurai/Samurai.png";
            };
            case HERO -> switch (skin) {
                case "purple" -> "images/hero/Heroshoot_purple.png";
                case "neo" -> "images/hero/Heroshoot_neo.png";
                default -> "images/hero/Heroshoot.png";
            };
        };
    }

    /**
     * NEW: Samurai sword blade texture by skin (Samurai only).
     */
    public static String sword(GameStateService.HeroType t, String skin) {
        if (t != GameStateService.HeroType.SAMURAI) return null;
        return switch (skin) {
            case "crimson" -> "images/samurai/sword2.png";
            case "azure" -> "images/samurai/sword3.png";
            default -> "images/samurai/sword.png"; // default sword
        };
    }

    /**
     * NEW: Form-specific body textures for HERO.
     * Other hero types use a single body texture (no forms).
     */
    public static String bodyForForm(GameStateService.HeroType t, String skin, int form) {
        if (t != GameStateService.HeroType.HERO) {
            // Non-HERO types: no form split → reuse single body texture
            return body(t, skin);
        }
        // HERO: return different texture per form (1/2/3) under the same skin
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

    /**
     * Resolve bullet texture for a given hero type and skin.
     */
    public static String bullet(GameStateService.HeroType t, String skin) {
        return switch (t) {
            case ENGINEER -> switch (skin) {
                default -> "images/engineer/Bullet.png";
            };
            case SAMURAI -> switch (skin) {
                case "crimson" -> "images/hero3/Bullet_crimson.png";
                case "azure" -> "images/hero3/Bullet_azure.png";
                default -> "images/hero3/Bullet.png";
            };
            case HERO -> "images/hero/Bullet.png";
        };
    }
}
