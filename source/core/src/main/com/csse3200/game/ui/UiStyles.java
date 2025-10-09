package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.services.ServiceLocator;

/**
 * Centralised UI styles so every screen can share the same orange button.
 */
public final class UiStyles {
    private UiStyles() {}

    /**
     * Returns a unified orange button style based on assets/images/Main_Menu_Button_Background.png.
     * Uses the skin's "segoe_ui" font if present; else falls back to default font.
     */
    public static TextButtonStyle orangeButton(Skin skin) {
        // Try from ResourceService first (if preloaded)
        Texture tex = null;
        try {
            if (ServiceLocator.getResourceService() != null) {
                tex = ServiceLocator.getResourceService()
                        .getAsset("images/Main_Menu_Button_Background.png", Texture.class);
            }
        } catch (Exception ignored) {}

        // Fallback: load directly if not already in ResourceService
        if (tex == null) {
            tex = new Texture(Gdx.files.internal("images/Main_Menu_Button_Background.png"));
        }

        TextureRegion region = new TextureRegion(tex);
        // Tweak these inset values if corners look squished; 10/10/10/10 is safe for your art.
        NinePatch base = new NinePatch(region, 10, 10, 10, 10);

        NinePatch over = new NinePatch(region, 10, 10, 10, 10);
        over.setColor(new Color(1.05f, 1.05f, 1.05f, 1f));   // slightly brighter on hover

        NinePatch down = new NinePatch(region, 10, 10, 10, 10);
        down.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));   // slightly darker when pressed

        TextButtonStyle style = new TextButtonStyle();
        style.up   = new NinePatchDrawable(base);
        style.over = new NinePatchDrawable(over);
        style.down = new NinePatchDrawable(down);

        // Use "segoe_ui" if your skin has it; otherwise default font:
        try {
            style.font = skin.getFont("segoe_ui");
        } catch (Exception e) {
            style.font = skin.getFont("segoe_ui");
        }
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        return style;
    }
}
