// com.csse3200.game.ui.Theme.java
package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;

public final class Theme {
    private Theme() {}

    // Book style - Warm cream color tone
    public static final Color WINDOW_BG      = new Color(1f, 0.97f, 0.88f, 0.98f);  // Cream panel
    public static final Color WINDOW_BORDER  = Color.valueOf("8b7355");  // Brown-gray border

    public static final Color TITLE_BG       = Color.valueOf("6d5d4b");  // Dark brown title bar
    public static final Color TITLE_FG       = Color.valueOf("ffffff");  // Pure white

    public static final Color TABLE_BG       = new Color(1f, 0.95f, 0.8f, 0.95f);  // Cream background
    public static final Color TABLE_BORDER   = Color.valueOf("d4c4a8");  // Light brown border

    public static final Color HEADER_FG      = Color.valueOf("5a4a3a");  // Dark brown header text
    public static final Color ROW_FG         = Color.valueOf("2c2416");  // Dark brown text
    public static final Color ROW_MUTED      = Color.valueOf("8b7355");  // Brown-gray secondary text
    public static final Color ROW_ALT_BG     = Color.valueOf("f5ead5");  // Light gold alternating row
    public static final Color ROW_HOVER_BG   = Color.valueOf("ffe8a8");  // Light gold hover
    public static final Color ROW_ME_BG      = Color.valueOf("d4edda");  // Light green (your rank)
    public static final Color BADGE_GOLD     = Color.valueOf("d4af37");  // Gold
    public static final Color BADGE_SILVER   = Color.valueOf("c0c0c0");  // Silver
    public static final Color BADGE_BRONZE   = Color.valueOf("cd7f32");  // Bronze

    // Book style buttons (consistent with SimpleUI)
    public static final Color BTN_PRIMARY_BG = Color.valueOf("2aa35a");  // Book green
    public static final Color BTN_PRIMARY_HV = Color.valueOf("34b56a");  // Book green hover
    public static final Color BTN_PRIMARY_DN = Color.valueOf("1e7d45");  // Book green pressed

    public static final Color BTN_DARK_BG    = Color.valueOf("1f2630");  // Book dark
    public static final Color BTN_DARK_HV    = Color.valueOf("293241");  // Book dark hover
    public static final Color BTN_DARK_DN    = Color.valueOf("15202b");  // Book dark pressed

    public static final int  PAD        = 12;  // Book style spacious padding
    public static final int  PAD_SM     = 6;
    public static final int  RADIUS     = 8;   // Book style rounded corners
    public static final int  RADIUS_SM  = 4;
    public static final int  FONT_LG    = 20;
    public static final int  FONT_MD    = 16;
    public static final int  FONT_SM    = 14;
}
