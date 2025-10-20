package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
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

    /**
     * 创建使用continue.png作为贴图的按钮样式
     * @return continue按钮样式
     */
    public static TextButton.TextButtonStyle continueButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        String continueButtonPath = "images/continue.png";
        Texture buttonTexture = ServiceLocator.getResourceService().getAsset(continueButtonPath, Texture.class);
        if (buttonTexture == null) {
            buttonTexture = new Texture(continueButtonPath); // fallback if not preloaded
        }

        TextureRegion region = new TextureRegion(buttonTexture);
        NinePatch patch = new NinePatch(region, 10, 10, 10, 10);

        NinePatch pressed = new NinePatch(region, 10, 10, 10, 10);
        pressed.setColor(new Color(0.9f, 0.9f, 0.9f, 1f));

        NinePatch hover = new NinePatch(region, 10, 10, 10, 10);
        hover.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));

        style.up = new NinePatchDrawable(patch);
        style.down = new NinePatchDrawable(pressed);
        style.over = new NinePatchDrawable(hover);

        // 根据屏幕尺寸选择不同大小的字体
        style.font = getFontForScreenSize();

        style.fontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        style.overFontColor = Color.WHITE;

        return style;
    }

    /**
     * 创建使用skip.png作为贴图的按钮样式
     * @return skip按钮样式
     */
    public static TextButton.TextButtonStyle skipButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        String skipButtonPath = "images/skip.png";
        Texture buttonTexture = ServiceLocator.getResourceService().getAsset(skipButtonPath, Texture.class);
        if (buttonTexture == null) {
            buttonTexture = new Texture(skipButtonPath); // fallback if not preloaded
        }

        TextureRegion region = new TextureRegion(buttonTexture);
        NinePatch patch = new NinePatch(region, 10, 10, 10, 10);

        NinePatch pressed = new NinePatch(region, 10, 10, 10, 10);
        pressed.setColor(new Color(0.9f, 0.9f, 0.9f, 1f));

        NinePatch hover = new NinePatch(region, 10, 10, 10, 10);
        hover.setColor(new Color(1.1f, 1.1f, 1.1f, 1f));

        style.up = new NinePatchDrawable(patch);
        style.down = new NinePatchDrawable(pressed);
        style.over = new NinePatchDrawable(hover);

        // 根据屏幕尺寸选择不同大小的字体
        style.font = getFontForScreenSize();

        style.fontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        style.overFontColor = Color.WHITE;

        return style;
    }

    /**
     * 根据屏幕尺寸选择合适大小的字体文件
     * @return 适合当前屏幕尺寸的字体
     */
    private static com.badlogic.gdx.graphics.g2d.BitmapFont getFontForScreenSize() {
        try {
            float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
            float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
            
            // 根据屏幕分辨率选择字体大小
            String fontPath;
            if (screenWidth >= 2560 || screenHeight >= 1440) {
                // 高分辨率屏幕 (2K/4K)
                fontPath = "flat-earth/skin/fonts/arial_black_32.fnt";
            } else if (screenWidth >= 1920 || screenHeight >= 1080) {
                // 标准分辨率屏幕 (1080p)
                fontPath = "flat-earth/skin/fonts/segoe_ui_18.fnt";
            } else {
                // 低分辨率屏幕 (720p及以下)
                fontPath = "flat-earth/skin/fonts/segoe_ui_18.fnt";
            }
            
            com.badlogic.gdx.graphics.g2d.BitmapFont font = new com.badlogic.gdx.graphics.g2d.BitmapFont(
                com.badlogic.gdx.Gdx.files.internal(fontPath)
            );
            font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
            
            return font;
        } catch (Exception e) {
            // 如果加载失败，使用默认字体
            return skin.getFont("segoe_ui");
        }
    }
}
