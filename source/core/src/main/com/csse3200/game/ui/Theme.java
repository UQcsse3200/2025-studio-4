// com.csse3200.game.ui.Theme.java
package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;

public final class Theme {
    private Theme() {}

    // 你的游戏是清新的草地卡通风，这里用偏“绿色系 + 暗底”的配色
    public static final Color WINDOW_BG      = Color.valueOf("0c1116"); // 窗体底色
    public static final Color WINDOW_BORDER  = Color.valueOf("2c313a");

    public static final Color TITLE_BG       = Color.valueOf("1a2129");
    public static final Color TITLE_FG       = Color.valueOf("d7e3ff");

    public static final Color TABLE_BG       = Color.valueOf("141a20");
    public static final Color TABLE_BORDER   = Color.valueOf("2b323c");

    public static final Color HEADER_FG      = Color.valueOf("9fc1ff");
    public static final Color ROW_FG         = Color.valueOf("e6edf3");
    public static final Color ROW_MUTED      = Color.valueOf("9aa7b3");
    public static final Color ROW_ALT_BG     = Color.valueOf("0f151b");
    public static final Color ROW_HOVER_BG   = Color.valueOf("1a2330");
    public static final Color ROW_ME_BG      = Color.valueOf("11331b"); // 高亮“自己”
    public static final Color BADGE_GOLD     = Color.valueOf("ffd363");
    public static final Color BADGE_SILVER   = Color.valueOf("dfe2e7");
    public static final Color BADGE_BRONZE   = Color.valueOf("f0b48a");

    public static final Color BTN_PRIMARY_BG = Color.valueOf("2aa35a");
    public static final Color BTN_PRIMARY_HV = Color.valueOf("34b56a");
    public static final Color BTN_PRIMARY_DN = Color.valueOf("1e7d45");

    public static final Color BTN_DARK_BG    = Color.valueOf("1f2630");
    public static final Color BTN_DARK_HV    = Color.valueOf("293241");
    public static final Color BTN_DARK_DN    = Color.valueOf("15202b");

    // 尺寸（统一扩大）
    public static final int  PAD        = 14;
    public static final int  PAD_SM     = 8;
    public static final int  RADIUS     = 12;
    public static final int  RADIUS_SM  = 10;
    public static final int  FONT_LG    = 20;
    public static final int  FONT_MD    = 16;
    public static final int  FONT_SM    = 14;
}
