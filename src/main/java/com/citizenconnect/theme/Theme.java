
package com.citizenconnect.theme;

import java.awt.Color;
import java.awt.Font;

public final class Theme {

    private Theme() {
        // Prevent object creation
    }

    // Main brand colors
    public static final Color NAVY =
            new Color(16, 39, 67);

    public static final Color PRIMARY =
            new Color(23, 103, 194);

    public static final Color TEAL =
            new Color(13, 152, 127);

    // Background colors
    public static final Color BACKGROUND =
            new Color(244, 247, 251);

    public static final Color LIGHT_BLUE =
            new Color(231, 241, 252);

    public static final Color WHITE = Color.WHITE;

    // Text colors
    public static final Color TEXT =
            new Color(30, 41, 59);

    public static final Color MUTED_TEXT =
            new Color(100, 116, 139);

    // Input and card borders
    public static final Color BORDER =
            new Color(220, 228, 238);

    // Typography
    public static final Font TITLE =
            new Font("Segoe UI", Font.BOLD, 28);

    public static final Font HEADING =
            new Font("Segoe UI", Font.BOLD, 20);

    public static final Font BODY =
            new Font("Segoe UI", Font.PLAIN, 14);

    public static final Font SMALL =
            new Font("Segoe UI", Font.PLAIN, 12);

    public static final Font BUTTON =
            new Font("Segoe UI", Font.BOLD, 14);
}
