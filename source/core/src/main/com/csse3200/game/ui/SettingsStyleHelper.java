package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.graphics.Color;

/**
 * Helpers to build the same panel + button look used in SettingsMenuDisplay:
 * - Panel NinePatch: images/settings_bg.png
 * - Button image:   images/settings_bg_button.png
 */
public final class SettingsStyleHelper {
    private SettingsStyleHelper() {}

    /** Create the orange rounded button style used in Settings. */
    public static TextButtonStyle createSettingsButtonStyle(Skin skin) {
        ResourceService rs = ServiceLocator.getResourceService();
        Texture buttonTex = rs.getAsset("images/settings_bg_button.png", Texture.class);
        TextureRegion region = new TextureRegion(buttonTex);

        TextButtonStyle style = new TextButtonStyle();
        style.font = skin.getFont("segoe_ui");

        // up / over / down drawables from the same texture with small tints
        TextureRegionDrawable up = new TextureRegionDrawable(region);
        TextureRegionDrawable over = new TextureRegionDrawable(region);
        over.tint(new Color(1.08f, 1.08f, 1.08f, 1f));
        TextureRegionDrawable down = new TextureRegionDrawable(region);
        down.tint(new Color(0.82f, 0.82f, 0.82f, 1f));

        style.up = up;
        style.over = over;
        style.down = down;

        style.fontColor = Color.WHITE;
        style.overFontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        return style;
    }

    /** NinePatchDrawable for the Settings panel frame. */
    public static NinePatchDrawable createSettingsPanelDrawable() {
        ResourceService rs = ServiceLocator.getResourceService();
        Texture panelTex = rs.getAsset("images/settings_bg.png", Texture.class);
        NinePatch patch = new NinePatch(new TextureRegion(panelTex), 20, 20, 20, 20);
        return new NinePatchDrawable(patch);
    }
}
