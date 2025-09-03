package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * 轻量 UI 工具：提供（1）简单的 BitmapFont 缩放；（2）圆角矩形背景（可带边框）。
 * 不依赖外部 skin/ttf。
 */
public final class UiKit {
    private UiKit() {}

    /**
     * 生成一个缩放后的 BitmapFont（不依赖 TTF）。
     * @param size  目标字号（像素级，内部按 16px 基准等比缩放）
     * @param color 字色（你也可以在 LabelStyle 里设置 fontColor）
     */
    public static BitmapFont font(int size, Color color) {
        BitmapFont f = new BitmapFont();           // 默认位图字体
        float scale = Math.max(1f, size / 16f);    // 以 16 为基准等比缩放
        f.getData().setScale(scale);
        f.setColor(color);
        return f;
    }

    /**
     * 生成圆角矩形背景（可选边框），返回 Drawable，可直接 Table#setBackground(drawable)。
     * 不使用 NinePatch，避免构造参数问题。
     *
     * @param fillColor    填充色（含透明度）
     * @param borderColor  边框色；为 null 或 borderWidth<=0 则不绘制边框
     * @param cornerRadius 圆角半径（像素）
     * @param borderWidth  边框像素宽度
     */
    public static Drawable roundRect(Color fillColor,
                                     Color borderColor,
                                     int cornerRadius,
                                     int borderWidth) {
        // 为了作为“背景”可伸缩，做一块小贴图即可（大小由圆角+边框决定）
        int pad = Math.max(1, cornerRadius + Math.max(0, borderWidth));
        int w = pad * 2 + 1;
        int h = w;

        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        // setBlending 是实例方法
        pm.setBlending(Pixmap.Blending.SourceOver);

        // 先画填充的圆角矩形（坐标固定从 0,0 开始，避免未使用参数的警告）
        fillRoundRect(pm, w, h, cornerRadius, fillColor);

        // 再画边框
        if (borderColor != null && borderWidth > 0) {
            strokeRoundRect(pm, w, h, cornerRadius, borderWidth, borderColor);
        }

        Texture texture = new Texture(pm);
        pm.dispose();

        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    // ----------------- 私有绘制工具 -----------------

    private static void fillRoundRect(Pixmap p, int w, int h, int r, Color color) {
        p.setColor(color);
        int right = w - 1;
        int top = h - 1;

        // 中间矩形
        p.fillRectangle(r, 0, w - 2 * r, h);
        // 左右竖条
        p.fillRectangle(0, r, r, h - 2 * r);
        p.fillRectangle(right - r + 1, r, r, h - 2 * r);

        // 四个圆角
        p.fillCircle(r, r, r);
        p.fillCircle(right - r, r, r);
        p.fillCircle(r, top - r, r);
        p.fillCircle(right - r, top - r, r);
    }

    private static void strokeRoundRect(Pixmap p, int w, int h,
                                        int r, int bw, Color color) {
        p.setColor(color);
        int right = w - 1;
        int top = h - 1;

        for (int i = 0; i < bw; i++) {
            // 四边
            p.drawRectangle(r + i, i, w - 2 * (r + i), 1);          // 下
            p.drawRectangle(r + i, top - i, w - 2 * (r + i), 1);    // 上
            p.drawRectangle(i, r + i, 1, h - 2 * (r + i));          // 左
            p.drawRectangle(right - i, r + i, 1, h - 2 * (r + i));  // 右

            // 四角
            p.drawCircle(r, r, r - i);
            p.drawCircle(right - r, r, r - i);
            p.drawCircle(r, top - r, r - i);
            p.drawCircle(right - r, top - r, r - i);
        }
    }
}
