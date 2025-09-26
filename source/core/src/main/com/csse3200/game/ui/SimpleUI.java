package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class SimpleUI {
    private static BitmapFont BASE_FONT;
    private static TextureRegionDrawable SOLID_CACHE_DARK;

    private SimpleUI() {}

    public static BitmapFont font() {
        if (BASE_FONT == null) {
            BASE_FONT = new BitmapFont();
        }
        return BASE_FONT;
    }

    public static TextureRegionDrawable solid(Color c) {
        Pixmap pm = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(new TextureRegion(t));
    }

    /* ---------------------------
     * Drawable with rounded corners (can have borders)
     *R: Corner radius; BorderW: Border width (0 means no border)
     * --------------------------- */
    public static TextureRegionDrawable roundRect(Color fill, Color border, int r, int borderW) {
        int pad = r + Math.max(borderW, 1);
        int w = pad * 2 + 2;
        int h = pad * 2 + 2;

        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        // Transparent first
        pm.setColor(0, 0, 0, 0);
        pm.fill();

        pm.setColor(fill);
        pm.fillRectangle(pad - r, 1, 2 * r + 2, h - 2); // 中间竖条
        pm.fillRectangle(1, pad - r, w - 2, 2 * r + 2); // 中间横条
        pm.fillCircle(pad, pad, r);
        pm.fillCircle(w - pad - 1, pad, r);
        pm.fillCircle(pad, h - pad - 1, r);
        pm.fillCircle(w - pad - 1, h - pad - 1, r);

        if (borderW > 0 && border != null) {
            pm.setColor(border);
            for (int i = 0; i < borderW; i++) {
                int rr = r + i;
                pm.drawRectangle(pad - rr, pad - rr, (w - 2 * (pad - rr)), (h - 2 * (pad - rr)));
                pm.drawCircle(pad, pad, rr);
                pm.drawCircle(w - pad - 1, pad, rr);
                pm.drawCircle(pad, h - pad - 1, rr);
                pm.drawCircle(w - pad - 1, h - pad - 1, rr);
            }
        }

        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(new TextureRegion(t));
    }

    /* ---------------------------
     * Label Style
     * --------------------------- */
    public static Label.LabelStyle label() {
        return new Label.LabelStyle(font(), Color.BLACK);
    }

    public static Label.LabelStyle muted() {
        return new Label.LabelStyle(font(), Color.valueOf("9aa7b3"));
    }

    public static Label.LabelStyle title() {
        return new Label.LabelStyle(font(), Color.valueOf("d7e3ff"));
    }

    /* ---------------------------
     * Button style
     * --------------------------- */
    public static TextButton.TextButtonStyle buttonStyle() {
        TextButton.TextButtonStyle st = new TextButton.TextButtonStyle();
        st.font = font();
        st.fontColor = Color.WHITE;
        st.up   = solid(Color.valueOf("1f2630"));
        st.over = solid(Color.valueOf("293241"));
        st.down = solid(Color.valueOf("15202b"));
        return st;
    }

    public static TextButton.TextButtonStyle darkButton() {
        return buttonStyle();
    }

    public static TextButton.TextButtonStyle primaryButton() {
        TextButton.TextButtonStyle st = new TextButton.TextButtonStyle();
        st.font = font();
        st.fontColor = Color.WHITE;
        st.up   = solid(Color.valueOf("2aa35a"));
        st.over = solid(Color.valueOf("34b56a"));
        st.down = solid(Color.valueOf("1e7d45"));
        return st;
    }

    /* ---------------------------
     * Window style (pop ups/dialog boxes)
     * --------------------------- */
    public static Window.WindowStyle windowStyle() {
        Window.WindowStyle ws = new Window.WindowStyle();
        ws.titleFont = font();
        ws.titleFontColor = Color.WHITE;
        ws.background = solid(new Color(1f, 0.95f, 0.8f, 0.9f)); // RGBA，奶黄色半透明

        return ws;
    }

    public static ScrollPane.ScrollPaneStyle scrollStyle() {
        ScrollPane.ScrollPaneStyle sp = new ScrollPane.ScrollPaneStyle();
        sp.background = null;
        sp.hScroll = null;
        sp.vScroll = null;
        sp.hScrollKnob = null;
        sp.vScrollKnob = null;
        return sp;
    }

    public static void dispose() {
        if (BASE_FONT != null) {
            BASE_FONT.dispose();
            BASE_FONT = null;
        }
    }
}
