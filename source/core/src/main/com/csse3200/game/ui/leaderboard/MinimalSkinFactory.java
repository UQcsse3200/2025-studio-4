package com.csse3200.game.ui.leaderboard;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.csse3200.game.ui.Theme;

public final class MinimalSkinFactory {
    private MinimalSkinFactory() {}

    /**
     * Creates a Book-style rounded rectangle drawable with optional borders
     * (Similar to SimpleUI.roundRect but adapted for our use case)
     */
    private static TextureRegionDrawable createRoundRect(Color fill, Color border, int r, int borderW) {
        int pad = r + Math.max(borderW, 1);
        int w = pad * 2 + 2;
        int h = pad * 2 + 2;

        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        // Transparent first
        pm.setColor(0, 0, 0, 0);
        pm.fill();

        // 绘制填充（圆角矩形）
        pm.setColor(fill);
        pm.fillRectangle(pad - r, 1, 2 * r + 2, h - 2); // 中间竖条
        pm.fillRectangle(1, pad - r, w - 2, 2 * r + 2); // 中间横条
        pm.fillCircle(pad, pad, r);
        pm.fillCircle(w - pad - 1, pad, r);
        pm.fillCircle(pad, h - pad - 1, r);
        pm.fillCircle(w - pad - 1, h - pad - 1, r);

        // 绘制边框（如果有）
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
        Drawable select  = new TextureRegionDrawable(new TextureRegion(white)).tint((Color)skin.get("selection-c", Color.class));
        
        // 主按钮（绿色）- Book风格无边框圆角
        Drawable btnPrimaryUp   = createRoundRect((Color)skin.get("btn-primary", Color.class), null, Theme.RADIUS, 0);
        Drawable btnPrimaryOver = createRoundRect((Color)skin.get("btn-primary-hover", Color.class), null, Theme.RADIUS, 0);
        Drawable btnPrimaryDown = createRoundRect((Color)skin.get("btn-primary-down", Color.class), null, Theme.RADIUS, 0);
        
        // 暗色按钮 - Book风格无边框圆角
        Drawable btnDarkUp   = createRoundRect((Color)skin.get("btn-dark", Color.class), null, Theme.RADIUS, 0);
        Drawable btnDarkOver = createRoundRect((Color)skin.get("btn-dark-hover", Color.class), null, Theme.RADIUS, 0);
        Drawable btnDarkDown = createRoundRect((Color)skin.get("btn-dark-down", Color.class), null, Theme.RADIUS, 0);

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

        // Window - Book风格奶黄色圆角（无边框）
        Drawable windowBg = createRoundRect((Color)skin.get("panel", Color.class), null, Theme.RADIUS, 0);
        Window.WindowStyle ws = new Window.WindowStyle();
        ws.titleFont = font;
        ws.titleFontColor = (Color)skin.get("text-title", Color.class);
        ws.background = windowBg;
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
