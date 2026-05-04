package com.university.fms.util;

import java.awt.*;

/**
 * Central UI styling constants.
 * Changing these propagates to the entire application.
 */
public final class UIConstants {
    private UIConstants() {}

    // Colors
    public static final Color PRIMARY     = new Color(30, 90, 160);    // Deep blue
    public static final Color PRIMARY_DARK= new Color(20, 60, 120);
    public static final Color ACCENT      = new Color(0, 160, 130);    // Teal
    public static final Color DANGER      = new Color(210, 50, 50);
    public static final Color SUCCESS     = new Color(34, 150, 80);
    public static final Color WARNING     = new Color(220, 140, 0);
    public static final Color BG_DARK     = new Color(25, 35, 55);
    public static final Color BG_PANEL    = new Color(245, 247, 252);
    public static final Color BG_WHITE    = Color.WHITE;
    public static final Color TEXT_MAIN   = new Color(30, 40, 60);
    public static final Color TEXT_MUTED  = new Color(110, 120, 140);
    public static final Color BORDER      = new Color(210, 215, 230);
    public static final Color ROW_ALT     = new Color(240, 244, 255);
    public static final Color PENDING_CLR = new Color(255, 243, 205);
    public static final Color APPROVED_CLR= new Color(212, 237, 218);
    public static final Color REJECTED_CLR= new Color(248, 215, 218);

    // Fonts
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_INPUT   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BTN     = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);

    // Sizes
    public static final int SIDEBAR_WIDTH = 220;
    public static final int TOPBAR_HEIGHT = 55;
    public static final Dimension BTN_SIZE = new Dimension(140, 36);
    public static final int FIELD_HEIGHT   = 32;
    public static final int GAP            = 10;
    public static final int PADDING        = 20;
}
