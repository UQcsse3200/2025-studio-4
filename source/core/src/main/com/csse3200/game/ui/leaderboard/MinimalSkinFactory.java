package com.csse3200.game.ui.leaderboard;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;

/** 纯代码生成一个最小可用的 Skin，不依赖 uiskin.json/atlas。*/
public final class MinimalSkinFactory {
    private MinimalSkinFactory() {}

    public static Skin create() {
        Skin skin = new Skin();

        // 1x1 白色纹理，后面可以 tint 出各种颜色
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture white = new Texture(pm);
        pm.dispose();
        skin.add("white", white, Texture.class);

        // 字体（使用默认 BitmapFont）
        BitmapFont font = new BitmapFont();
        skin.add("default-font", font, BitmapFont.class);

        // 常用颜色
        skin.add("text", Color.WHITE);
        skin.add("bg", new Color(0x111111ff));
        skin.add("panel", new Color(0x1e1e1eff));
        skin.add("accent", new Color(0x3fa7ffff)); // 青色
        skin.add("accent-d", new Color(0x2b7aaaff));
        skin.add("selection-c", new Color(0x2b7aaa66)); // 半透明选中

        // 可复用的纯色背景
        Drawable bg      = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("bg", Color.class));
        Drawable panel   = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("panel", Color.class));
        Drawable up      = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("accent", Color.class));
        Drawable down    = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("accent-d", Color.class));
        Drawable select  = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("selection-c", Color.class));

        // Label
        Label.LabelStyle label = new Label.LabelStyle();
        label.font = font;
        label.fontColor = (Color)skin.get("text", Color.class);
        skin.add("default", label);

        // 标题 Label
        Label.LabelStyle title = new Label.LabelStyle();
        title.font = font;
        title.fontColor = (Color)skin.get("accent", Color.class);
        skin.add("title", title);

        // TextButton
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.up = up;
        tbs.down = down;
        tbs.checked = down;
        tbs.font = font;
        tbs.fontColor = Color.BLACK;
        skin.add("default", tbs);

        // Window
        Window.WindowStyle ws = new Window.WindowStyle();
        ws.titleFont = font;
        ws.titleFontColor = (Color)skin.get("text", Color.class);
        ws.background = panel;
        skin.add("default", ws);

        // ScrollPane
        ScrollPane.ScrollPaneStyle sps = new ScrollPane.ScrollPaneStyle();
        sps.background = bg;
        skin.add("default", sps);

        // Table row 选中背景（给排行榜高亮“自己”用）
        skin.add("selection", select, Drawable.class);

        return skin;
    }
}
