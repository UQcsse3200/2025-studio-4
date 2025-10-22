package com.csse3200.game.components.hero.samurai;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RotatingSheetAnimationRenderComponent;

/** Wrapper for the Samurai “Sword Qi” animation using a PNG sprite sheet. */
public final class SwordQiAnimatorPng {
    private SwordQiAnimatorPng() {}

    /**
     * Apply a rotating sheet animation to an entity.
     *
     * @param entity        target entity
     * @param pngPath       PNG sprite sheet path (e.g., "images/samurai/slash_sheet_6x1_64.png")
     * @param cols          number of columns (e.g., 6)
     * @param rows          number of rows (e.g., 1)
     * @param frameW        frame width in pixels
     * @param frameH        frame height in pixels
     * @param frameDur      duration per frame in seconds
     * @param angleDeg      world-facing angle (0° = right, 90° = up)
     * @param baseRotation  source-art facing baseline (e.g., art facing up = 90f, right = 0f)
     * @param loop          true = loop; false = play once
     */
    public static void apply(Entity entity,
                             String pngPath,
                             int cols, int rows, int frameW, int frameH,
                             float frameDur,
                             float angleDeg,
                             float baseRotation,
                             boolean loop) {

        var comp = RotatingSheetAnimationRenderComponent.fromSheet(
                pngPath, cols, rows, frameW, frameH, frameDur,
                loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL,
                "samurai_qi"
        );
        comp.setBaseRotation(baseRotation);
        comp.setRotation(angleDeg);
        comp.setScale(1f, 1f);

        // Important: render in a foreground layer with a very high z-index to avoid occlusion.
        comp.setLayer(5);
        comp.setZIndexOverride(9999f);

        entity.addComponent(comp);

        // Safety: if the entity has no size, give it a visible default.
        if (entity.getScale().isZero()) {
            entity.setScale(3.0f, 3.0f); // Large for visibility; tune down to ~1.2f once verified.
        }

        comp.start();
    }
}




