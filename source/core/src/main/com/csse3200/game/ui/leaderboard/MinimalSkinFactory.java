package com.csse3200.game.ui.leaderboard;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.csse3200.game.ui.Theme;

/** 纯代码生成一个最小可用的 Skin，使用系统统一的 Theme 配色。*/
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
        skin.add("default", font, BitmapFont.class); // 添加 "default" 别名以兼容不同调用

        // 使用 Theme 中定义的颜色
        skin.add("text", Theme.ROW_FG);
        skin.add("text-muted", Theme.ROW_MUTED);
        skin.add("text-header", Theme.HEADER_FG);
        skin.add("text-title", Theme.TITLE_FG);
        skin.add("bg", Theme.TABLE_BG);
        skin.add("panel", Theme.WINDOW_BG);
        skin.add("title-bg", Theme.TITLE_BG);
        skin.add("row-alt", Theme.ROW_ALT_BG);
        skin.add("row-hover", Theme.ROW_HOVER_BG);
        skin.add("selection-c", Theme.ROW_ME_BG);
        
        // 按钮颜色
        skin.add("btn-primary", Theme.BTN_PRIMARY_BG);
        skin.add("btn-primary-hover", Theme.BTN_PRIMARY_HV);
        skin.add("btn-primary-down", Theme.BTN_PRIMARY_DN);
        skin.add("btn-dark", Theme.BTN_DARK_BG);
        skin.add("btn-dark-hover", Theme.BTN_DARK_HV);
        skin.add("btn-dark-down", Theme.BTN_DARK_DN);

        // 可复用的纯色背景
        Drawable bg      = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("bg", Color.class));
        Drawable panel   = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("panel", Color.class));
        Drawable select  = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("selection-c", Color.class));
        
        // 主按钮（绿色）
        Drawable btnPrimaryUp   = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("btn-primary", Color.class));
        Drawable btnPrimaryOver = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("btn-primary-hover", Color.class));
        Drawable btnPrimaryDown = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("btn-primary-down", Color.class));
        
        // 暗色按钮
        Drawable btnDarkUp   = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("btn-dark", Color.class));
        Drawable btnDarkOver = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("btn-dark-hover", Color.class));
        Drawable btnDarkDown = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("btn-dark-down", Color.class));

        // Label - 默认样式
        Label.LabelStyle label = new Label.LabelStyle();
        label.font = font;
        label.fontColor = (Color)skin.get("text", Color.class);
        skin.add("default", label);

        // Label - 标题样式
        Label.LabelStyle title = new Label.LabelStyle();
        title.font = font;
        title.fontColor = (Color)skin.get("text-title", Color.class);
        skin.add("title", title);
        
        // Label - 表头样式
        Label.LabelStyle header = new Label.LabelStyle();
        header.font = font;
        header.fontColor = (Color)skin.get("text-header", Color.class);
        skin.add("header", header);

        // TextButton - 默认样式（主按钮）
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.up = btnPrimaryUp;
        tbs.over = btnPrimaryOver;
        tbs.down = btnPrimaryDown;
        tbs.checked = btnPrimaryDown;
        tbs.font = font;
        tbs.fontColor = Color.WHITE;
        skin.add("default", tbs);
        
        // TextButton - 暗色样式
        TextButton.TextButtonStyle tbsDark = new TextButton.TextButtonStyle();
        tbsDark.up = btnDarkUp;
        tbsDark.over = btnDarkOver;
        tbsDark.down = btnDarkDown;
        tbsDark.checked = btnDarkDown;
        tbsDark.font = font;
        tbsDark.fontColor = Color.WHITE;
        skin.add("dark", tbsDark);

        // Window - 使用完全透明的背景显示游戏场景
        Window.WindowStyle ws = new Window.WindowStyle();
        ws.titleFont = font;
        ws.titleFontColor = (Color)skin.get("text-title", Color.class);
        ws.background = panel;
        // 完全透明，显示背后的游戏背景
        ws.stageBackground = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0, 0, 0, 0f));
        skin.add("default", ws);

        // ScrollPane
        ScrollPane.ScrollPaneStyle sps = new ScrollPane.ScrollPaneStyle();
        sps.background = bg;
        skin.add("default", sps);

        // Table row 选中背景（给排行榜高亮"自己"用）
        skin.add("selection", select, Drawable.class);

        return skin;
    }
}
