package com.citizenconnect.ui;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.UserRepository;
import org.bson.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Locale;

public class ServiceDetailsFrame extends JFrame {

    private final UserRepository.AuthenticatedUser currentUser;
    private final Document service;

    private static final Color NAVY = new Color(16, 39, 67);
    private static final Color PRIMARY = new Color(23, 103, 194);
    private static final Color TEAL = new Color(13, 152, 127);
    private static final Color BG = new Color(244, 247, 251);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(220, 228, 238);

    public ServiceDetailsFrame(
            UserRepository.AuthenticatedUser user,
            Document service) {

        if (user == null || !"CITIZEN".equals(user.role())) {
            throw new IllegalArgumentException(
                    "Only authenticated citizens can access service details.");
        }

        if (service == null) {
            throw new IllegalArgumentException(
                    "Service information is required.");
        }

        this.currentUser = user;
        this.service = service;

        setTitle("CitizenConnect Pro - Service Details");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    // =========================================
    // SIDEBAR
    // =========================================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(255, 0));

        JPanel menu = new JPanel();
        menu.setBackground(NAVY);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(28, 17, 15, 17));

        JLabel brand = new JLabel("CitizenConnect Pro");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(WHITE);

        JLabel subtitle = new JLabel("SMART PUBLIC SERVICE PLATFORM");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        subtitle.setForeground(new Color(170, 192, 214));

        JLabel heading = new JLabel("CITIZEN MENU");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 10));
        heading.setForeground(new Color(145, 170, 195));

        menu.add(brand);
        menu.add(Box.createVerticalStrut(6));
        menu.add(subtitle);
        menu.add(Box.createVerticalStrut(34));
        menu.add(heading);
        menu.add(Box.createVerticalStrut(14));

        addNav(menu, "DB", "Dashboard");
        addNav(menu, "01", "My Profile");
        addNav(menu, "02", "Public Services");
        addNav(menu, "03", "My Applications");
        addNav(menu, "04", "My Complaints");
        addNav(menu, "05", "Important Information");
        addNav(menu, "06", "Emergency Alerts");
        addNav(menu, "07", "Volunteer Registration");
        addNav(menu, "08", "Feedback & Rating");

        sidebar.add(menu, BorderLayout.NORTH);
        sidebar.add(createSidebarFooter(), BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNav(JPanel parent, String icon, String title) {

        boolean selected = "Public Services".equals(title);

        JButton button = new JButton(icon + "     " + title);

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Segoe UI",
                selected ? Font.BOLD : Font.PLAIN, 12));
        button.setForeground(selected ? WHITE : new Color(197, 211, 226));
        button.setBackground(selected ? PRIMARY : NAVY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(12, 13, 12, 8));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (!selected) {
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    button.setBackground(new Color(27, 53, 82));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    button.setBackground(NAVY);
                }
            });
        }

        button.addActionListener(e -> {
            switch (title) {
                case "Dashboard":
                    goToDashboard();
                    break;

                case "My Profile":
                    goToProfile();
                    break;

                case "Public Services":
                    goToServices();
                    break;

                default:
                    JOptionPane.showMessageDialog(
                            this,
                            title + " screen will be connected later.",
                            "Screen Integration",
                            JOptionPane.INFORMATION_MESSAGE);
            }
        });

        parent.add(button);
        parent.add(Box.createVerticalStrut(5));
    }

    private JPanel createSidebarFooter() {

        JPanel footer = new JPanel();
        footer.setBackground(NAVY);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(new EmptyBorder(10, 15, 18, 15));

        String name = safe(currentUser.fullName(), "Citizen");

        RoundedPanel profile = new RoundedPanel(13, new Color(27, 53, 82));
        profile.setLayout(new BorderLayout(10, 0));
        profile.setBorder(new EmptyBorder(12, 11, 12, 9));
        profile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel avatar = new JLabel(
                name.substring(0, 1).toUpperCase(),
                SwingConstants.CENTER);

        avatar.setOpaque(true);
        avatar.setBackground(TEAL);
        avatar.setForeground(WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        avatar.setPreferredSize(new Dimension(42, 42));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel userName = new JLabel(name);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userName.setForeground(WHITE);

        JLabel role = new JLabel("Citizen Account");
        role.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        role.setForeground(new Color(185, 204, 222));

        info.add(userName);
        info.add(Box.createVerticalStrut(4));
        info.add(role);

        profile.add(avatar, BorderLayout.WEST);
        profile.add(info, BorderLayout.CENTER);

        JButton logout = roundedButton(
                "Sign Out", new Color(37, 62, 88),
                new Color(255, 210, 210), 10);

        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(e -> logout());

        footer.add(profile);
        footer.add(Box.createVerticalStrut(10));
        footer.add(logout);

        return footer;
    }

    // =========================================
    // MAIN CONTENT
    // =========================================

    private JPanel createMainContent() {

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(27, 30, 35, 30));

        content.add(createTopBar());
        content.add(Box.createVerticalStrut(22));
        content.add(createHeaderCard());
        content.add(Box.createVerticalStrut(22));
        content.add(createInformationSection());
        content.add(Box.createVerticalStrut(17));
        content.add(createRequirementsSection());
        content.add(Box.createVerticalStrut(17));
        content.add(createInstructionsSection());
        content.add(Box.createVerticalStrut(20));
        content.add(createActionSection());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setWheelScrollingEnabled(true);
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    // =========================================
    // TOP BAR
    // =========================================

    private JPanel createTopBar() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Service Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(NAVY);

        JButton backButton = roundedButton(
                "←  Back to Services", WHITE, PRIMARY, 10);

        backButton.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER, 10),
                new EmptyBorder(8, 14, 8, 14)));

        backButton.addActionListener(e -> goToServices());

        panel.add(title, BorderLayout.WEST);
        panel.add(backButton, BorderLayout.EAST);

        return panel;
    }

    // =========================================
    // SERVICE HEADER
    // =========================================

    private JPanel createHeaderCard() {

        RoundedPanel card = new RoundedPanel(20, NAVY);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(28, 30, 28, 30));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel category = new JLabel(
                safe(service.getString("category"),
                        "Public Service").toUpperCase(Locale.ROOT));

        category.setFont(new Font("Segoe UI", Font.BOLD, 10));
        category.setForeground(new Color(126, 224, 210));
        category.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel(
                "<html><div style='width:850px;'>"
                        + escapeHtml(safe(
                                service.getString("serviceName"),
                                "Unnamed Service"))
                        + "</div></html>");

        name.setFont(new Font("Segoe UI", Font.BOLD, 27));
        name.setForeground(WHITE);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Public Service Information and Application Guide");

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(220, 233, 245));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(category);
        text.add(Box.createVerticalStrut(10));
        text.add(name);
        text.add(Box.createVerticalStrut(9));
        text.add(subtitle);

        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // =========================================
    // SERVICE INFORMATION
    // =========================================

    private JPanel createInformationSection() {

        RoundedPanel section = createWhiteCard();

        section.add(createSectionHeading("Service Information"));
        section.add(Box.createVerticalStrut(17));

        JLabel descriptionHeading = createLabel("DESCRIPTION");
        section.add(descriptionHeading);
        section.add(Box.createVerticalStrut(7));

        JTextArea description = createReadOnlyTextArea(
                safe(service.getString("description"),
                        "No description available."));

        section.add(description);
        section.add(Box.createVerticalStrut(18));

        JPanel detailsGrid = new JPanel(new GridLayout(1, 3, 13, 0));
        detailsGrid.setOpaque(false);
        detailsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailsGrid.add(createDetailBox(
                "APPLICATION FEE",
                formatFee(service.get("applicationFee"))));

        detailsGrid.add(createDetailBox(
                "PROCESSING TIME",
                safe(service.getString("processingTime"),
                        "Not specified")));

        detailsGrid.add(createDetailBox(
                "RESPONSIBLE OFFICE",
                safe(service.getString("officeName"),
                        "Not specified")));

        section.add(detailsGrid);
        return section;
    }

    private JPanel createDetailBox(String title, String value) {

        RoundedPanel box = new RoundedPanel(12, BG);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel heading = createLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 9));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(
                "<html><div style='width:190px;'>"
                        + escapeHtml(value) + "</div></html>");

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valueLabel.setForeground(NAVY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.add(heading);
        box.add(Box.createVerticalStrut(9));
        box.add(valueLabel);

        return box;
    }

    // =========================================
    // REQUIREMENTS
    // =========================================

    private JPanel createRequirementsSection() {

        RoundedPanel section = createWhiteCard();

        section.add(createSectionHeading(
                "Required Documents and Information"));
        section.add(Box.createVerticalStrut(13));

        List<String> requirements =
                service.getList("requirements", String.class);

        if (requirements == null || requirements.isEmpty()) {

            section.add(createLabel(
                    "No specific requirements have been listed."));

        } else {

            for (int i = 0; i < requirements.size(); i++) {

                JPanel row = new JPanel(new BorderLayout(12, 0));
                row.setOpaque(false);
                row.setBorder(new EmptyBorder(6, 0, 6, 0));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel number = new JLabel(
                        String.valueOf(i + 1), SwingConstants.CENTER);

                number.setOpaque(true);
                number.setBackground(new Color(231, 241, 252));
                number.setForeground(PRIMARY);
                number.setFont(new Font("Segoe UI", Font.BOLD, 12));
                number.setPreferredSize(new Dimension(34, 34));

                JLabel requirement = createLabel(requirements.get(i));
                requirement.setForeground(TEXT);

                row.add(number, BorderLayout.WEST);
                row.add(requirement, BorderLayout.CENTER);

                section.add(row);
            }
        }

        return section;
    }

    // =========================================
    // APPLICATION INSTRUCTIONS
    // =========================================

    private JPanel createInstructionsSection() {

        RoundedPanel section = createWhiteCard();

        section.add(createSectionHeading("Application Instructions"));
        section.add(Box.createVerticalStrut(13));

        JTextArea instructions = createReadOnlyTextArea(
                safe(service.getString("applicationInstructions"),
                        "Application instructions have not been provided yet."));

        section.add(instructions);
        return section;
    }

    // =========================================
    // APPLY SECTION
    // =========================================

    private JPanel createActionSection() {

        RoundedPanel panel = new RoundedPanel(17, WHITE);
        panel.setLayout(new BorderLayout(15, 0));
        panel.setBorder(new EmptyBorder(20, 22, 20, 22));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Ready to Apply?");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Review the requirements before starting your application.");

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);

        text.add(heading);
        text.add(Box.createVerticalStrut(6));
        text.add(subtitle);

        JButton applyButton = roundedButton(
                "Apply for Service  →", PRIMARY, WHITE, 11);

        applyButton.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Open Apply For Service Page
        applyButton.addActionListener(e -> {
            new ApplyForServiceFrame(currentUser, service).setVisible(true);
            dispose();
        });

        panel.add(text, BorderLayout.CENTER);
        panel.add(applyButton, BorderLayout.EAST);

        return panel;
    }

    // =========================================
    // UI HELPERS
    // =========================================

    private RoundedPanel createWhiteCard() {

        RoundedPanel panel = new RoundedPanel(17, WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(21, 23, 21, 23));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, Integer.MAX_VALUE));

        return panel;
    }

    private JLabel createSectionHeading(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(NAVY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    private JLabel createLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    private JTextArea createReadOnlyTextArea(String text) {

        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        area.setForeground(TEXT);
        area.setBackground(WHITE);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setBorder(null);
        area.setRows(Math.max(2, Math.min(4, text.length() / 85 + 1)));
        area.setAlignmentX(Component.LEFT_ALIGNMENT);

        return area;
    }

    private JButton roundedButton(
            String text, Color background, Color foreground, int radius) {

        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                Color color = getBackground();

                if (getModel().isPressed()) {
                    color = color.darker();
                } else if (getModel().isRollover()) {
                    color = color.brighter();
                }

                g2.setColor(color);
                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(),
                        radius * 2, radius * 2);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    private String formatFee(Object fee) {

        if (fee == null) {
            return "Not specified";
        }

        if (fee instanceof Number) {

            double amount = ((Number) fee).doubleValue();

            if (amount == 0) {
                return "Free";
            }

            return String.format(Locale.US, "৳%.2f", amount);
        }

        return fee.toString();
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // =========================================
    // NAVIGATION
    // =========================================

    private void goToServices() {
        new PublicServicesFrame(currentUser).setVisible(true);
        dispose();
    }

    private void goToDashboard() {
        new CitizenDashboardFrame(currentUser).setVisible(true);
        dispose();
    }

    private void goToProfile() {
        new UserProfileFrame(currentUser).setVisible(true);
        dispose();
    }

    private void logout() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to sign out?",
                "Confirm Sign Out",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            UserSession.clearSession();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    // =========================================
    // ROUNDED PANEL
    // =========================================

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(backgroundColor);
            g2.fillRoundRect(
                    0, 0, getWidth() - 1, getHeight() - 1,
                    radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedLineBorder
            extends javax.swing.border.AbstractBorder {

        private final Color color;
        private final int radius;

        RoundedLineBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(
                Component c, Graphics g,
                int x, int y, int width, int height) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);
            g2.drawRoundRect(
                    x, y, width - 1, height - 1,
                    radius * 2, radius * 2);

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 12, 8, 12);
        }
    }
}