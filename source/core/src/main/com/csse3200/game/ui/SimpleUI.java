package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class SimpleUI {
    private static BitmapFont font;

    public static BitmapFont font() {
        if (font == null) font = new BitmapFont();
        return font;
    }

    private static TextureRegionDrawable solid(Color c) {
        Pixmap pm = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(t);
    }

    public static TextButton.TextButtonStyle buttonStyle() {
        TextButton.TextButtonStyle st = new TextButton.TextButtonStyle();
        st.font = font();
        st.fontColor = Color.WHITE;
        st.up   = solid(Color.valueOf("2b2f36"));
        st.over = solid(Color.valueOf("39414d"));
        st.down = solid(Color.valueOf("1f2329"));
        return st;
    }

    public static Window.WindowStyle windowStyle() {
        Window.WindowStyle ws = new Window.WindowStyle();
        ws.titleFont = font();
        ws.titleFontColor = Color.WHITE;
        ws.background = solid(Color.valueOf("101215"));
        return ws;
    }

    public static Label.LabelStyle label() {
        return new Label.LabelStyle(font(), Color.WHITE);
    }

    public static Label.LabelStyle muted() {
        return new Label.LabelStyle(font(), Color.LIGHT_GRAY);
    }
}

