
package com.citizenconnect.theme;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;


public class RoundedButton extends JButton {

    private final int radius = 18;

    public RoundedButton(String text) {
        super(text);

        setFont(Theme.BUTTON);
        setForeground(Color.WHITE);
        setBackground(Theme.PRIMARY);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        setCursor(
            new Cursor(Cursor.HAND_CURSOR)
        );
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        Color background = getBackground();

        if (getModel().isPressed()) {
            background = background.darker();
        } else if (getModel().isRollover()) {
            background = background.brighter();
        }

        g2.setColor(background);
        g2.fillRoundRect(
            0, 0,
            getWidth() - 1,
            getHeight() - 1,
            radius, radius
        );

        g2.dispose();

        super.paintComponent(g);
    }
}
