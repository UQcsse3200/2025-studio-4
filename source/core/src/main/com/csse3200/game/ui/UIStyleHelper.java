package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.services.ServiceLocator;

import static com.csse3200.game.ui.UIComponent.skin;

/**
 * Provides a unified orange button style for all menus.
 */
public class UIStyleHelper {

    private static final String BUTTON_PATH = "images/Main_Menu_Button_Background.png";

    public static TextButton.TextButtonStyle orangeButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        Texture buttonTexture = ServiceLocator.getResourceService().getAsset(BUTTON_PATH, Texture.class);
        if (buttonTexture == null) {
            buttonTexture = new Texture(BUTTON_PATH); // fallback if not preloaded
        }

        TextureRegion region = new TextureRegion(buttonTexture);
        NinePatch patch = new NinePatch(region, 10, 10, 10, 10);

        NinePatch pressed = new NinePatch(region, 10, 10, 10, 10);
        pressed.setColor(new Color(0.9f, 0.7f, 0.5f, 1f));

        NinePatch hover = new NinePatch(region, 10, 10, 10, 10);
        hover.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));

        style.up = new NinePatchDrawable(patch);
        style.down = new NinePatchDrawable(pressed);
        style.over = new NinePatchDrawable(hover);

        style.font = skin.getFont("segoe_ui");

        style.fontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        style.overFontColor = Color.WHITE;

        return style;
    }

    public static TextButton.TextButtonStyle mainGameMenuButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        // Colors
        Color upFill = new Color(0.15f, 0.15f, 0.18f, 0.9f);
        Color downFill   = new Color(0.10f, 0.10f, 0.12f, 0.9f); // pressed (darker)
        Color overFill   = new Color(0.20f, 0.20f, 0.23f, 0.9f); // hover (slightly brighter)

        int size = 32;   // texture size
        int radius = 8;  // corner radius

        // Build rounded textures
        Texture upTex = buildRoundedTexture(upFill, radius, size);
        Texture overTex = buildRoundedTexture(overFill, radius, size);
        Texture downTex = buildRoundedTexture(downFill, radius, size);

        // Convert to drawables
        Drawable upDrawable = new TextureRegionDrawable(new TextureRegion(upTex));
        Drawable overDrawable = new TextureRegionDrawable(new TextureRegion(overTex));
        Drawable downDrawable = new TextureRegionDrawable(new TextureRegion(downTex));

        // Apply
        style.up = upDrawable;
        style.over = overDrawable;
        style.down = downDrawable;

        // Font setup
        style.font = skin.getFont("segoe_ui");
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;

        return style;
    }


    private static Texture buildRoundedTexture(Color color, int radius, int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0, 0, 0, 0);
        pm.fill();

        pm.setBlending(Pixmap.Blending.SourceOver);
        pm.setColor(color);

        // Draw rounded rectangle manually
        pm.fillRectangle(radius, 0, size - 2 * radius, size);
        pm.fillRectangle(0, radius, size, size - 2 * radius);
        pm.fillCircle(radius, radius, radius);
        pm.fillCircle(size - radius - 1, radius, radius);
        pm.fillCircle(radius, size - radius - 1, radius);
        pm.fillCircle(size - radius - 1, size - radius - 1, radius);

        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }
}
