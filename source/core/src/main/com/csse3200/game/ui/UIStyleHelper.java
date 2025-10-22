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

    /** Path to the main menu button background texture */
    private static final String BUTTON_PATH = "images/Main_Menu_Button_Background.png";

    /**
     * Creates an orange-themed button style for main menu buttons.
     * 
     * <p>This method creates a button style using the main menu button background
     * texture with NinePatch scaling. It includes different visual states for
     * normal, pressed, and hover interactions.</p>
     * 
     * @return TextButton.TextButtonStyle with orange theme and NinePatch background
     */
    public static TextButton.TextButtonStyle orangeButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        Texture buttonTexture = ServiceLocator.getResourceService().getAsset(BUTTON_PATH, Texture.class);
        if (buttonTexture == null) {
            buttonTexture = new Texture(BUTTON_PATH); // Fallback if not preloaded
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
     * Creates a button style using continue.png texture.
     * 
     * <p>This method creates a button style specifically for continue buttons,
     * using the continue.png texture with responsive font sizing based on
     * screen resolution.</p>
     * 
     * @return TextButton.TextButtonStyle for continue buttons
     */
    public static TextButton.TextButtonStyle continueButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        String continueButtonPath = "images/continue.png";
        Texture buttonTexture = ServiceLocator.getResourceService().getAsset(continueButtonPath, Texture.class);
        if (buttonTexture == null) {
            buttonTexture = new Texture(continueButtonPath); // Fallback if not preloaded
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

        // Select font size based on screen dimensions
        style.font = getFontForScreenSize();

        style.fontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        style.overFontColor = Color.WHITE;

        return style;
    }

    /**
     * Creates a button style using skip.png texture.
     * 
     * <p>This method creates a button style specifically for skip buttons,
     * using the skip.png texture with responsive font sizing based on
     * screen resolution.</p>
     * 
     * @return TextButton.TextButtonStyle for skip buttons
     */
    public static TextButton.TextButtonStyle skipButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        String skipButtonPath = "images/skip.png";
        Texture buttonTexture = ServiceLocator.getResourceService().getAsset(skipButtonPath, Texture.class);
        if (buttonTexture == null) {
            buttonTexture = new Texture(skipButtonPath); // Fallback if not preloaded
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

        // Select font size based on screen dimensions
        style.font = getFontForScreenSize();

        style.fontColor = Color.WHITE;
        style.downFontColor = Color.LIGHT_GRAY;
        style.overFontColor = Color.WHITE;

        return style;
    }

    /**
     * Selects an appropriate font size based on screen dimensions.
     * 
     * <p>This method analyzes the current screen resolution and selects
     * the most suitable font size for optimal readability. It supports
     * high-resolution displays (2K/4K), standard resolution (1080p),
     * and lower resolution screens (720p and below).</p>
     * 
     * @return BitmapFont appropriate for the current screen size
     */
    private static com.badlogic.gdx.graphics.g2d.BitmapFont getFontForScreenSize() {
        try {
            float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
            float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();

            // Select font size based on screen resolution
            String fontPath;
            if (screenWidth >= 2560 || screenHeight >= 1440) {
                // High resolution screens (2K/4K)
                fontPath = "flat-earth/skin/fonts/arial_black_32.fnt";
            } else if (screenWidth >= 1920 || screenHeight >= 1080) {
                // Standard resolution screens (1080p)
                fontPath = "flat-earth/skin/fonts/segoe_ui_18.fnt";
            } else {
                // Low resolution screens (720p and below)
                fontPath = "flat-earth/skin/fonts/segoe_ui_18.fnt";
            }

            com.badlogic.gdx.graphics.g2d.BitmapFont font = new com.badlogic.gdx.graphics.g2d.BitmapFont(
                com.badlogic.gdx.Gdx.files.internal(fontPath)
            );
            font.setColor(com.badlogic.gdx.graphics.Color.WHITE);

            return font;
        } catch (Exception e) {
            // If loading fails, use default font
            return skin.getFont("segoe_ui");
        }
    }

    /**
     * Creates a button style for the main game menu.
     *
     * The button uses smooth rounded corners and slightly different colors
     * for normal, hover, and pressed states. This keeps a consistent dark theme
     * without needing image assets.
     *
     * @return a custom {@link TextButton.TextButtonStyle} with rounded backgrounds
     */
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

    /**
     * Builds a simple rounded rectangle texture with the given color.
     *
     * This is used to draw button backgrounds directly with code instead of images.
     * You can control how round the corners are with the {@code radius} value.
     *
     * @param color  color to fill the texture with
     * @param radius how round the corners should be
     * @param size   width and height of the square texture (in pixels)
     * @return a new {@link Texture} with the rounded shape
     */
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
