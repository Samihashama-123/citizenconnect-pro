
package com.citizenconnect;

import com.citizenconnect.theme.Theme;
import com.citizenconnect.ui.DashboardFrame;

import javax.swing.*;
import java.awt.*;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::showRoleSelector);
    }

    private static void showRoleSelector() {
        JFrame frame = new JFrame("CitizenConnect Pro");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(720, 470);
        frame.setMinimumSize(new Dimension(650, 430));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Theme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(35, 45, 35, 45));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(35, 40, 35, 40)
        ));

        JLabel brand = new JLabel("CitizenConnect Pro");
        brand.setFont(Theme.TITLE);
        brand.setForeground(Theme.NAVY);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "A smarter connection between citizens and public services"
        );
        subtitle.setFont(Theme.BODY);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel choose = new JLabel("Choose a dashboard to continue");
        choose.setFont(Theme.HEADING);
        choose.setForeground(Theme.TEXT);
        choose.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton citizenButton = createButton("Citizen Dashboard");
        JButton adminButton = createButton("Admin Dashboard");

        citizenButton.addActionListener(e -> {
            frame.dispose();
            new DashboardFrame(DashboardFrame.Role.CITIZEN).setVisible(true);
        });

        adminButton.addActionListener(e -> {
            frame.dispose();
            new DashboardFrame(DashboardFrame.Role.ADMIN).setVisible(true);
        });

        card.add(brand);
        card.add(Box.createVerticalStrut(10));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(35));
        card.add(choose);
        card.add(Box.createVerticalStrut(22));
        card.add(citizenButton);
        card.add(Box.createVerticalStrut(12));
        card.add(adminButton);

        root.add(card);
        frame.setContentPane(root);
        frame.setVisible(true);
    }

    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.BUTTON);
        button.setForeground(Theme.WHITE);
        button.setBackground(Theme.PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(360, 46));
        button.setPreferredSize(new Dimension(360, 46));
        return button;
    }
}
