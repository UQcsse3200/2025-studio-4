// com.csse3200.game.ui.Theme.java
package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;

public final class Theme {
    private Theme() {}

    // Book风格 - 奶黄色温暖色调
    public static final Color WINDOW_BG      = new Color(1f, 0.97f, 0.88f, 0.98f);  // 奶黄色面板
    public static final Color WINDOW_BORDER  = Color.valueOf("8b7355");  // 棕灰色边框

    public static final Color TITLE_BG       = Color.valueOf("6d5d4b");  // 深棕标题栏
    public static final Color TITLE_FG       = Color.valueOf("ffffff");  // 纯白

    public static final Color TABLE_BG       = new Color(1f, 0.95f, 0.8f, 0.95f);  // 奶黄色背景
    public static final Color TABLE_BORDER   = Color.valueOf("d4c4a8");  // 浅棕边框

    public static final Color HEADER_FG      = Color.valueOf("5a4a3a");  // 深棕表头文字
    public static final Color ROW_FG         = Color.valueOf("2c2416");  // 深褐色文字
    public static final Color ROW_MUTED      = Color.valueOf("8b7355");  // 棕灰色次要文字
    public static final Color ROW_ALT_BG     = Color.valueOf("f5ead5");  // 浅金色交替行
    public static final Color ROW_HOVER_BG   = Color.valueOf("ffe8a8");  // 浅金色悬停
    public static final Color ROW_ME_BG      = Color.valueOf("d4edda");  // 淡绿色（你的排名）
    public static final Color BADGE_GOLD     = Color.valueOf("d4af37");  // 金色
    public static final Color BADGE_SILVER   = Color.valueOf("c0c0c0");  // 银色
    public static final Color BADGE_BRONZE   = Color.valueOf("cd7f32");  // 铜色

    // Book风格按钮（与SimpleUI保持一致）
    public static final Color BTN_PRIMARY_BG = Color.valueOf("2aa35a");  // Book的绿色
    public static final Color BTN_PRIMARY_HV = Color.valueOf("34b56a");  // Book的绿色悬停
    public static final Color BTN_PRIMARY_DN = Color.valueOf("1e7d45");  // Book的绿色按下

    public static final Color BTN_DARK_BG    = Color.valueOf("1f2630");  // Book的深色
    public static final Color BTN_DARK_HV    = Color.valueOf("293241");  // Book的深色悬停
    public static final Color BTN_DARK_DN    = Color.valueOf("15202b");  // Book的深色按下

    public static final int  PAD        = 12;  // Book风格更宽松的间距
    public static final int  PAD_SM     = 6;
    public static final int  RADIUS     = 8;   // Book风格圆角
    public static final int  RADIUS_SM  = 4;
    public static final int  FONT_LG    = 20;
    public static final int  FONT_MD    = 16;
    public static final int  FONT_SM    = 14;
}
