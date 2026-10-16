
package com.citizenconnect.theme;

import javax.swing.JTextField;
import javax.swing.BorderFactory;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;

public class RoundedTextField extends JTextField {

    private final int radius = 18;

    public RoundedTextField() {
        setOpaque(false);
        setFont(Theme.BODY);
        setBackground(Theme.BACKGROUND);

        setBorder(
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(getBackground());
        g2.fillRoundRect(
            1, 1,
            getWidth() - 3,
            getHeight() - 3,
            radius, radius
        );

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(Theme.BORDER);
        g2.setStroke(new BasicStroke(1f));

        g2.drawRoundRect(
            1, 1,
            getWidth() - 3,
            getHeight() - 3,
            radius, radius
        );

        g2.dispose();
    }
}
