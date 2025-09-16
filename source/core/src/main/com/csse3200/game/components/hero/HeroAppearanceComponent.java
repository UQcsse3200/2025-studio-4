package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Handles hero appearance upgrades (supports both RotatingTextureRenderComponent / TextureRenderComponent).
 * Since texture fields are final in the rendering components, this approach replaces the old
 * component with a new one, while attempting to preserve properties such as rotation.
 */
public class HeroAppearanceComponent extends Component {
    private final HeroConfig cfg;

    public HeroAppearanceComponent(HeroConfig cfg) {
        this.cfg = cfg;
    }

    @Override
    public void create() {
        // Apply level 1 appearance on creation (optional)
        applyTextureForLevel(1);

        // Change appearance when upgraded
        entity.getEvents().addListener("upgraded", (Integer level, CurrencyType t, Integer cost) -> {
            applyTextureForLevel(level);
        });
    }

    private void applyTextureForLevel(int level) {
        final String path = getTextureForLevel(level);
        if (path == null || path.isBlank()) return;

        // If the entity has a rotating render component (main hero sprite)
        RotatingTextureRenderComponent rot = entity.getComponent(RotatingTextureRenderComponent.class);
        if (rot != null) {
            float angle = rot.getRotation();
            rot.setTexture(path);   // ✅ Replace texture directly
            rot.setRotation(angle); // Restore rotation angle (optional)
            return;
        }

        // If the entity has a standard render component (ghost hero, preview)
        TextureRenderComponent tex = entity.getComponent(TextureRenderComponent.class);
        if (tex != null) {
            final float rotDeg = tex.getRotation();
            Gdx.app.postRunnable(() -> {
                TextureRenderComponent newTex = new TextureRenderComponent(path);
                entity.addComponent(newTex);
                newTex.setRotation(rotDeg);
            });
        }
    }

    /**
     * Gets the texture path for the given hero level.
     * If levelTextures is defined and contains a valid entry for the level, use it.
     * Otherwise, fall back to the default heroTexture.
     *
     * @param level hero level (starting from 1)
     * @return texture path as a String
     */
    private String getTextureForLevel(int level) {
        if (cfg.levelTextures != null) {
            int idx = level - 1;
            if (idx >= 0 && idx < cfg.levelTextures.length) {
                String s = cfg.levelTextures[idx];
                if (s != null && !s.isBlank()) return s;
            }
        }
        return cfg.heroTexture;
    }
}



