
package com.citizenconnect.theme;

import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class RoundedPanel extends JPanel {

    private int radius = 20;

    public RoundedPanel() {
        setOpaque(false);
        setBackground(Theme.WHITE);
    }

    public RoundedPanel(int radius) {
        this();
        this.radius = radius;
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
            0, 0,
            getWidth() - 1,
            getHeight() - 1,
            radius, radius
        );

        g2.dispose();

        super.paintComponent(g);
    }
}
