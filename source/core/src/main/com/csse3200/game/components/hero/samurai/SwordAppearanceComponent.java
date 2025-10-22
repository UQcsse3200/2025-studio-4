package com.csse3200.game.components.hero.samurai;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.SamuraiConfig;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Keeps the sword sprite in sync with the Samurai's level.
 * Listens to the owner's upgrade events and swaps the sword texture accordingly.
 */
public class SwordAppearanceComponent extends Component {
    /** Reference to the Samurai entity (owner of the sword). */
    private final Entity owner;
    /** Samurai configuration that provides per-level sword textures. */
    private final SamuraiConfig cfg;

    public SwordAppearanceComponent(Entity owner, SamuraiConfig cfg) {
        this.owner = owner;
        this.cfg = cfg;
    }

    @Override
    public void create() {
        // Initial application could set level-1 texture if desired.

        // Listen for Samurai upgrades and refresh the sword texture on level-up.
        if (owner != null) {
            owner.getEvents().addListener("upgraded",
                    (Integer level, CurrencyType t, Integer cost) -> applySwordTextureForLevel(level));
        }
    }

    /** Applies the sword texture that corresponds to the given level. */
    private void applySwordTextureForLevel(int level) {
        String path = getSwordTextureForLevel(level);
        if (path == null || path.isBlank()) return;

        // Prefer rotating render component (typical for the sword).
        RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
        if (rot != null) {
            float angle = rot.getRotation();
            rot.setTexture(path);     // swap texture
            rot.setRotation(angle);   // preserve current rotation
            return;
        }

        // Fallbacks (if ever needed) could be added here, e.g., TextureRenderComponent.
        // TextureRenderComponent tr = entity.getComponent(TextureRenderComponent.class);
        // if (tr != null) tr.setTexture(path);
    }

    /** Resolve the proper sword texture path for a given level, with fallback to the base sword texture. */
    private String getSwordTextureForLevel(int level) {
        if (cfg.swordLevelTextures != null) {
            int idx = level - 1;
            if (idx >= 0 && idx < cfg.swordLevelTextures.length) {
                String s = cfg.swordLevelTextures[idx];
                if (s != null && !s.isBlank()) return s;
            }
        }
        // Fallback: use the initial/base sword texture
        return cfg.swordTexture;
    }
}
