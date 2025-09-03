package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Lightweight UI toolkit: provides (1) simple BitmapFont scaling; (2) rounded rectangle backgrounds with optional borders.
 * Does not depend on external skin/ttf files.
 */
public final class UiKit {
    private UiKit() {}

    /**
     * Generates a scaled BitmapFont (without TTF dependency).
     * @param size   target font size in pixels (internally scales based on 16px baseline)
     * @param color  font color (you can also set fontColor in LabelStyle)
     */
    public static BitmapFont font(int size, Color color) {
        BitmapFont f = new BitmapFont();           // Default bitmap font
        float scale = Math.max(1f, size / 16f);    // Scale proportionally based on 16px baseline
        f.getData().setScale(scale);
        f.setColor(color);
        return f;
    }

    /**
     * Generates a rounded rectangle background (with optional border), returns a Drawable that can be used directly with Table.setBackground(drawable).
     * Does not use NinePatch to avoid constructor parameter issues.
     *
     * @param fillColor    fill color (with transparency)
     * @param borderColor  border color; if null or borderWidth less than or equal to 0, no border will be drawn
     * @param cornerRadius corner radius in pixels
     * @param borderWidth  border width in pixels
     */
    public static Drawable roundRect(Color fillColor,
                                     Color borderColor,
                                     int cornerRadius,
                                     int borderWidth) {
        // To make it scalable as a "background", create a small texture (size determined by corner radius + border)
        int pad = Math.max(1, cornerRadius + Math.max(0, borderWidth));
        int w = pad * 2 + 1;
        int h = w;

        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        // setBlending is an instance method
        pm.setBlending(Pixmap.Blending.SourceOver);

        // First draw the filled rounded rectangle (coordinates fixed from 0,0 to avoid unused parameter warnings)
        fillRoundRect(pm, w, h, cornerRadius, fillColor);

        // Then draw the border
        if (borderColor != null && borderWidth > 0) {
            strokeRoundRect(pm, w, h, cornerRadius, borderWidth, borderColor);
        }

        Texture texture = new Texture(pm);
        pm.dispose();

        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    // ----------------- Private drawing utilities -----------------

    private static void fillRoundRect(Pixmap p, int w, int h, int r, Color color) {
        p.setColor(color);
        int right = w - 1;
        int top = h - 1;

        // Center rectangle
        p.fillRectangle(r, 0, w - 2 * r, h);
        // Left and right vertical bars
        p.fillRectangle(0, r, r, h - 2 * r);
        p.fillRectangle(right - r + 1, r, r, h - 2 * r);

        // Four rounded corners
        p.fillCircle(r, r, r);
        p.fillCircle(right - r, r, r);
        p.fillCircle(r, top - r, r);
        p.fillCircle(right - r, top - r, r);
    }

    private static void strokeRoundRect(Pixmap p, int w, int h,
                                        int r, int bw, Color color) {
        p.setColor(color);
        int right = w - 1;
        int top = h - 1;

        for (int i = 0; i < bw; i++) {
            // Four sides
            p.drawRectangle(r + i, i, w - 2 * (r + i), 1);          // Bottom
            p.drawRectangle(r + i, top - i, w - 2 * (r + i), 1);    // Top
            p.drawRectangle(i, r + i, 1, h - 2 * (r + i));          // Left
            p.drawRectangle(right - i, r + i, 1, h - 2 * (r + i));  // Right

            // Four corners
            p.drawCircle(r, r, r - i);
            p.drawCircle(right - r, r, r - i);
            p.drawCircle(r, top - r, r - i);
            p.drawCircle(right - r, top - r, r - i);
        }
    }
}