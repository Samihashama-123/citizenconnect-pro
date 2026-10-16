
package com.citizenconnect.ui;

import com.citizenconnect.theme.Theme;
import com.citizenconnect.theme.RoundedButton;
import com.citizenconnect.theme.RoundedTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ForgotPasswordFrame extends JFrame {

    private static final String EMAIL_PATTERN =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final LoginFrame loginFrame;
    private RoundedTextField emailField;

    public ForgotPasswordFrame(LoginFrame loginFrame) {

        this.loginFrame = loginFrame;

        setTitle("CitizenConnect Pro - Forgot Password");
        setSize(1100, 700);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(loginFrame);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        mainPanel.add(createBrandingPanel());
        mainPanel.add(createForgotPasswordPanel());

        setContentPane(mainPanel);

        // Restore Login when this window is closed.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (ForgotPasswordFrame.this.loginFrame != null) {
                    ForgotPasswordFrame.this.loginFrame.setVisible(true);
                }
            }
        });
    }

    // =========================================
    // LEFT BRANDING PANEL
    // =========================================

    private JPanel createBrandingPanel() {

        JPanel leftPanel = new JPanel();

        leftPanel.setBackground(Theme.NAVY);
        leftPanel.setLayout(
                new BoxLayout(leftPanel, BoxLayout.Y_AXIS)
        );

        leftPanel.setBorder(
                BorderFactory.createEmptyBorder(55, 45, 45, 40)
        );

        // Application name and logo
        JLabel appName = new JLabel("CitizenConnect Pro");

        appName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        appName.setForeground(Theme.WHITE);
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);

        java.net.URL logoUrl = getClass().getResource(
                "/images/citizenconnect-logo.png"
        );

        if (logoUrl != null) {

            ImageIcon originalIcon = new ImageIcon(logoUrl);

            Image scaledLogo = originalIcon.getImage()
                    .getScaledInstance(
                            60, 60, Image.SCALE_SMOOTH
                    );

            appName.setIcon(new ImageIcon(scaledLogo));
            appName.setIconTextGap(15);

        } else {
            System.err.println("CitizenConnect logo not found.");
        }

        JLabel subtitle = new JLabel(
                "SMART PUBLIC SERVICE PLATFORM"
        );

        subtitle.setFont(Theme.SMALL);
        subtitle.setForeground(new Color(188, 211, 230));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel headline = new JLabel(
                "<html>Account recovery,<br>made simple.</html>"
        );

        headline.setFont(new Font("Segoe UI", Font.BOLD, 37));
        headline.setForeground(Theme.WHITE);
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html>Follow the password recovery process<br>"
                + "to regain access to your citizen services.</html>"
        );

        description.setFont(Theme.BODY);
        description.setForeground(new Color(214, 228, 240));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel featurePanel = new JPanel();

        featurePanel.setOpaque(false);
        featurePanel.setLayout(
                new BoxLayout(featurePanel, BoxLayout.Y_AXIS)
        );
        featurePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        featurePanel.add(createFeatureRow("Simple Email Validation"));
        featurePanel.add(Box.createVerticalStrut(8));
        featurePanel.add(createFeatureRow("Secure Recovery - Planned"));
        featurePanel.add(Box.createVerticalStrut(8));
        featurePanel.add(createFeatureRow("Citizen Services Access"));

        JLabel footer = new JLabel(
                "Your Service. Our Commitment."
        );

        footer.setFont(Theme.SMALL);
        footer.setForeground(new Color(185, 208, 227));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(appName);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(subtitle);

        leftPanel.add(Box.createVerticalGlue());

        leftPanel.add(headline);
        leftPanel.add(Box.createVerticalStrut(16));
        leftPanel.add(description);
        leftPanel.add(Box.createVerticalStrut(32));
        leftPanel.add(featurePanel);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(footer);

        return leftPanel;
    }

    // =========================================
    // BRANDING FEATURE ROW
    // =========================================

    private JPanel createFeatureRow(String text) {

        JPanel row = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );

        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 24)
        );

        JComponent checkIcon = new JComponent() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(new Color(64, 214, 177));

                g2.setStroke(new BasicStroke(
                        2.2f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                g2.drawLine(3, 9, 7, 13);
                g2.drawLine(7, 13, 16, 4);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(20, 20);
            }
        };

        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY);
        label.setForeground(new Color(190, 230, 219));

        row.add(checkIcon);
        row.add(Box.createHorizontalStrut(10));
        row.add(label);

        return row;
    }

    // =========================================
    // RIGHT FORGOT PASSWORD PANEL
    // =========================================

    private JPanel createForgotPasswordPanel() {

        JPanel rightPanel = new JPanel(new GridBagLayout());

        rightPanel.setBackground(Theme.WHITE);
        rightPanel.setBorder(
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        );

        JPanel formPanel = new JPanel();

        formPanel.setLayout(
                new BoxLayout(formPanel, BoxLayout.Y_AXIS)
        );

        formPanel.setBackground(Theme.WHITE);
        formPanel.setPreferredSize(new Dimension(420, 350));

        // Heading
        JLabel title = new JLabel("Forgot Password?");

        title.setFont(Theme.TITLE);
        title.setForeground(Theme.NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "<html>Enter your email address to begin<br>"
                + "the password recovery process.</html>"
        );

        subtitle.setFont(Theme.BODY);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Email label
        JLabel emailLabel = new JLabel("Email Address");

        emailLabel.setFont(Theme.BODY);
        emailLabel.setForeground(Theme.TEXT);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Email field
        emailField = new RoundedTextField();

        emailField.setFont(Theme.BODY);
        emailField.setForeground(Theme.TEXT);
        emailField.setBackground(Theme.BACKGROUND);

        emailField.setPreferredSize(new Dimension(420, 48));
        emailField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48)
        );

        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.setToolTipText("Enter your email address");

        // Request reset button
        RoundedButton requestButton =
                new RoundedButton("Request Reset");

        requestButton.setPreferredSize(new Dimension(420, 50));
        requestButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 50)
        );

        requestButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        requestButton.addActionListener(
                e -> validateResetRequest()
        );

        // Back to Login link
        JButton backButton = createLinkButton("Back to Login");

        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        backButton.addActionListener(e -> dispose());

        JPanel backPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 0, 0)
        );

        backPanel.setBackground(Theme.WHITE);
        backPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        backPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 35)
        );

        backPanel.add(backButton);

        // Assemble form
        formPanel.add(title);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(subtitle);
        formPanel.add(Box.createVerticalStrut(35));

        formPanel.add(emailLabel);
        formPanel.add(Box.createVerticalStrut(9));
        formPanel.add(emailField);
        formPanel.add(Box.createVerticalStrut(24));

        formPanel.add(requestButton);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(backPanel);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        rightPanel.add(formPanel, gbc);

        // Pressing Enter submits the email form.
        getRootPane().setDefaultButton(requestButton);

        return rightPanel;
    }

    // =========================================
    // LINK BUTTON
    // =========================================

    private JButton createLinkButton(String text) {

        JButton button = new JButton(text);

        button.setFont(Theme.SMALL);
        button.setForeground(Theme.PRIMARY);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(Theme.TEAL);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(Theme.PRIMARY);
            }
        });

        return button;
    }

    // =========================================
    // EMAIL VALIDATION
    // =========================================

    private void validateResetRequest() {

        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showWarning("Please enter your email address.");
            emailField.requestFocusInWindow();
            return;
        }

        if (!email.matches(EMAIL_PATTERN)) {
            showWarning("Please enter a valid email address.");
            emailField.requestFocusInWindow();
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Email validation passed.\n\n"
                + "Password reset service is not connected yet.\n"
                + "No reset email has been sent.",
                "Password Reset Unavailable",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =========================================
    // VALIDATION MESSAGE
    // =========================================

    private void showWarning(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
    }
}