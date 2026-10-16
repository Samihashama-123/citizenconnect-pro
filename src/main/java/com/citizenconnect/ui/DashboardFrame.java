
package com.citizenconnect.ui;

import com.citizenconnect.theme.Theme;
import com.citizenconnect.theme.RoundedButton;
import com.citizenconnect.theme.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardFrame extends JFrame {

    public enum Role {
        CITIZEN, ADMIN
    }

    private final Role role;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    private final JLabel pageTitle;
    private final JPanel sidebar;

    public DashboardFrame(Role role) {
        this.role = role;

        setTitle("CitizenConnect Pro | " +
                (role == Role.ADMIN ? "Admin Dashboard" : "Citizen Dashboard"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);

        sidebar = createSidebar();

        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(Theme.BACKGROUND);

        pageTitle = new JLabel("Overview");
        pageTitle.setFont(Theme.HEADING);
        pageTitle.setForeground(Theme.NAVY);

        JPanel header = createHeader();

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.BACKGROUND);

        addPage("Overview", createOverviewPage());
        addPage("Public Services", createPlaceholder(
                "Public Services",
                "Explore public services and find the right service for your needs."
        ));
        addPage("My Applications", createPlaceholder(
                "My Applications",
                "View your submitted applications and track their progress."
        ));
        addPage("Applications", createPlaceholder(
                "Applications Management",
                "Review applications and update their processing status."
        ));
        addPage("Complaints", createPlaceholder(
                "Complaints",
                "Submit, review and track complaints."
        ));
        addPage("Emergency Alerts", createPlaceholder(
                "Emergency Alerts",
                "View important public safety notices and emergency information."
        ));
        addPage("Volunteer", createPlaceholder(
                "Volunteer Management",
                "Explore volunteering opportunities and community activities."
        ));
        addPage("Feedback", createPlaceholder(
                "Feedback",
                "Share feedback and help improve public services."
        ));
        addPage("My Profile", createPlaceholder(
                "My Profile",
                "Manage your personal information and account preferences."
        ));
        addPage("Citizens", createPlaceholder(
                "Citizen Management",
                "View and manage citizen records."
        ));
        addPage("Announcements", createPlaceholder(
                "Announcements",
                "Prepare and manage public announcements."
        ));
        addPage("Settings", createPlaceholder(
                "System Settings",
                "Manage dashboard preferences and system options."
        ));

        workspace.add(header, BorderLayout.NORTH);
        workspace.add(contentPanel, BorderLayout.CENTER);

        root.add(sidebar, BorderLayout.WEST);
        root.add(workspace, BorderLayout.CENTER);

        setContentPane(root);
        navigateTo("Overview");
    }

    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.NAVY);
        panel.setPreferredSize(new Dimension(245, 0));
        panel.setBorder(new EmptyBorder(25, 16, 20, 16));

        JLabel brand = new JLabel("<html>CitizenConnect<br> <font size='3'>PRO</font></html>");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 23));
        brand.setForeground(Theme.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel(
                role == Role.ADMIN ? "ADMINISTRATION" : "CITIZEN PORTAL"
        );
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleLabel.setForeground(new Color(177, 198, 224));
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(brand);
        panel.add(Box.createVerticalStrut(8));
        panel.add(roleLabel);
        panel.add(Box.createVerticalStrut(35));

        String[] items = role == Role.ADMIN
                ? new String[]{
                    "Overview", "Applications", "Citizens", "Complaints",
                    "Announcements", "Volunteers", "Feedback", "Settings"
                }
                : new String[]{
                    "Overview", "Public Services", "My Applications",
                    "Complaints", "Emergency Alerts", "Volunteer",
                    "Feedback", "My Profile"
                };

        for (String item : items) {
            JButton navButton = createNavButton(item);
            navButton.addActionListener(e -> navigateTo(item));
            panel.add(navButton);
            panel.add(Box.createVerticalStrut(7));
        }

        panel.add(Box.createVerticalGlue());

        JLabel footer = new JLabel(
                "<html><font color='#B1C6E0'>Civic services<br>made simpler.</font></html>"
        );
        footer.setFont(Theme.SMALL);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(footer);

        return panel;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton("   " + text);
        button.setFont(Theme.BODY);
        button.setForeground(Theme.WHITE);
        button.setBackground(Theme.NAVY);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        button.setPreferredSize(new Dimension(210, 43));
        button.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(37, 66, 101));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(Theme.NAVY);
            }
        });

        return button;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(18, 28, 18, 25)
        ));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        titles.add(pageTitle);
        titles.add(Box.createVerticalStrut(5));

        JLabel subtitle = new JLabel(role == Role.ADMIN
                ? "Manage services, applications and citizen requests."
                : "Your public services, all in one place.");
        subtitle.setFont(Theme.SMALL);
        subtitle.setForeground(Theme.MUTED_TEXT);
        titles.add(subtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 3));
        right.setOpaque(false);

        JLabel date = new JLabel(LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd MMM yyyy")));
        date.setFont(Theme.SMALL);
        date.setForeground(Theme.MUTED_TEXT);

        JButton notification = new JButton("Notifications");
        notification.setFont(Theme.SMALL);
        notification.setForeground(Theme.PRIMARY);
        notification.setBackground(Theme.LIGHT_BLUE);
        notification.setFocusPainted(false);
        notification.setBorder(BorderFactory.createEmptyBorder(10, 13, 10, 13));
        notification.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        notification.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "The notification service is not connected yet.",
                "Notifications",
                JOptionPane.INFORMATION_MESSAGE
        ));

        JButton switchRole = new JButton("Switch Dashboard");
        switchRole.setFont(Theme.SMALL);
        switchRole.setForeground(Theme.TEXT);
        switchRole.setBackground(Theme.WHITE);
        switchRole.setFocusPainted(false);
        switchRole.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        switchRole.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                try {
                    Class<?> appClass = Class.forName("com.citizenconnect.App");
                    java.lang.reflect.Method method =
                            appClass.getDeclaredMethod("main", String[].class);
                    method.invoke(null, (Object) new String[0]);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Could not reopen the dashboard selector.",
                            "Navigation Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            });
        });

        right.add(date);
        right.add(notification);
        right.add(switchRole);

        header.add(titles, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void addPage(String name, JPanel page) {
        contentPanel.add(page, name);
    }

    private void navigateTo(String name) {
        pageTitle.setText(name.equals("Overview") ? "Overview" : name);
        cardLayout.show(contentPanel, name);
    }

    private JPanel createOverviewPage() {
        JPanel page = new JPanel(new BorderLayout(0, 20));
        page.setBackground(Theme.BACKGROUND);
        page.setBorder(new EmptyBorder(24, 28, 26, 28));

        JPanel main = new JPanel();
        main.setOpaque(false);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        main.add(createWelcomeBanner());
        main.add(Box.createVerticalStrut(22));
        main.add(createSectionTitle("Overview at a glance"));
        main.add(Box.createVerticalStrut(13));
        main.add(createStatsPanel());
        main.add(Box.createVerticalStrut(24));

        if (role == Role.ADMIN) {
            main.add(createSectionTitle("Recent Applications"));
            main.add(Box.createVerticalStrut(12));
            main.add(createAdminTable());
            main.add(Box.createVerticalStrut(18));
            main.add(createAttentionPanel());
        } else {
            main.add(createSectionTitle("Recent Activity"));
            main.add(Box.createVerticalStrut(12));
            main.add(createCitizenTable());
            main.add(Box.createVerticalStrut(18));
            main.add(createQuickActions());
        }

        JScrollPane scroll = new JScrollPane(main);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        page.add(scroll, BorderLayout.CENTER);

        return page;
    }

    private JPanel createWelcomeBanner() {
        JPanel banner = new JPanel(new BorderLayout(15, 10));
        banner.setBackground(Theme.NAVY);
        banner.setBorder(new EmptyBorder(25, 26, 25, 26));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(role == Role.ADMIN
                ? "Welcome to the Admin Workspace"
                : "Welcome back, Citizen!");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 23));
        heading.setForeground(Theme.WHITE);

        JLabel description = new JLabel(role == Role.ADMIN
                ? "Monitor service delivery and manage citizen requests."
                : "Access public services and keep track of your applications.");
        description.setFont(Theme.BODY);
        description.setForeground(new Color(220, 232, 246));

        textPanel.add(heading);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(description);

        RoundedButton action = new RoundedButton(
                role == Role.ADMIN ? "Review Applications" : "Explore Services"
        );
        action.setBackground(Theme.TEAL);
        action.setForeground(Theme.WHITE);
        action.setPreferredSize(new Dimension(175, 42));
        action.addActionListener(e ->
                navigateTo(role == Role.ADMIN ? "Applications" : "Public Services")
        );

        banner.add(textPanel, BorderLayout.CENTER);
        banner.add(action, BorderLayout.EAST);
        return banner;
    }

    private JPanel createStatsPanel() {
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));

        String[][] values = role == Role.ADMIN
                ? new String[][]{
                    {"Registered Citizens", "1,284", "Citizen accounts"},
                    {"Total Applications", "356", "All applications"},
                    {"Pending Review", "48", "Awaiting review"},
                    {"Open Complaints", "17", "Need attention"}
                }
                : new String[][]{
                    {"My Applications", "04", "Submitted requests"},
                    {"In Progress", "02", "Currently processing"},
                    {"Completed", "01", "Successfully completed"},
                    {"Open Complaints", "01", "Awaiting response"}
                };

        Color[] accents = {
                Theme.PRIMARY,
                Theme.TEAL,
                new Color(224, 151, 43),
                new Color(211, 83, 91)
        };

        for (int i = 0; i < values.length; i++) {
            stats.add(createStatCard(
                    values[i][0], values[i][1], values[i][2], accents[i]
            ));
        }

        return stats;
    }

    private JPanel createStatCard(
            String title, String value, String detail, Color accent) {

        RoundedPanel card = new RoundedPanel(18);
        card.setLayout(new BorderLayout(8, 7));
        card.setBackground(Theme.WHITE);
        card.setBorder(new EmptyBorder(16, 16, 15, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.SMALL);
        titleLabel.setForeground(Theme.MUTED_TEXT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);

        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detailLabel.setForeground(Theme.MUTED_TEXT);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(valueLabel);
        center.add(Box.createVerticalStrut(3));
        center.add(detailLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCitizenTable() {
        String[] columns = {"Application ID", "Service", "Date", "Status"};
        Object[][] data = {
                {"APP-1024", "Birth Certificate", "12 Oct 2026", "In Progress"},
                {"APP-1018", "Trade License", "08 Oct 2026", "Submitted"},
                {"APP-1007", "Citizen Certificate", "01 Oct 2026", "Completed"}
        };

        return createTablePanel(columns, data);
    }

    private JPanel createAdminTable() {
        String[] columns = {"Application ID", "Citizen", "Service", "Submitted", "Status"};
        Object[][] data = {
                {"APP-2041", "Citizen A", "Birth Certificate", "15 Oct 2026", "Pending"},
                {"APP-2040", "Citizen B", "Trade License", "15 Oct 2026", "In Review"},
                {"APP-2039", "Citizen C", "Citizen Certificate", "14 Oct 2026", "Pending"}
        };

        return createTablePanel(columns, data);
    }

    private JPanel createTablePanel(String[] columns, Object[][] data) {
        RoundedPanel card = new RoundedPanel(16);
        card.setLayout(new BorderLayout());
        card.setBackground(Theme.WHITE);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(Theme.BODY);
        table.setRowHeight(39);
        table.setShowVerticalLines(false);
        table.setGridColor(Theme.BORDER);
        table.setSelectionBackground(Theme.LIGHT_BLUE);
        table.setSelectionForeground(Theme.TEXT);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setForeground(Theme.NAVY);
        table.getTableHeader().setBackground(Theme.LIGHT_BLUE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scrollPane.getViewport().setBackground(Theme.WHITE);

        card.add(scrollPane, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(700, 175));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 195));
        return card;
    }

    private JPanel createQuickActions() {
        RoundedPanel panel = new RoundedPanel(16);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBackground(Theme.WHITE);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Quick Actions");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.NAVY);

        JPanel buttons = new JPanel(new GridLayout(1, 4, 12, 0));
        buttons.setOpaque(false);

        String[] actions = {
                "Public Services", "My Applications", "Complaints", "Feedback"
        };

        for (String action : actions) {
            JButton button = new JButton(action);
            button.setFont(Theme.SMALL);
            button.setForeground(Theme.PRIMARY);
            button.setBackground(Theme.LIGHT_BLUE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(13, 8, 13, 8));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> navigateTo(action));
            buttons.add(button);
        }

        panel.add(title, BorderLayout.NORTH);
        panel.add(buttons, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        return panel;
    }

    private JPanel createAttentionPanel() {
        RoundedPanel panel = new RoundedPanel(16);
        panel.setLayout(new BorderLayout(10, 5));
        panel.setBackground(Theme.WHITE);
        panel.setBorder(new EmptyBorder(17, 18, 17, 18));

        JLabel title = new JLabel("Needs Attention");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.NAVY);

        JLabel description = new JLabel(
                "48 applications are awaiting review. Review pending requests to keep services moving."
        );
        description.setFont(Theme.BODY);
        description.setForeground(Theme.MUTED_TEXT);

        JButton review = new JButton("Open Applications");
        review.setFont(Theme.BUTTON);
        review.setForeground(Theme.WHITE);
        review.setBackground(Theme.PRIMARY);
        review.setFocusPainted(false);
        review.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        review.addActionListener(e -> navigateTo("Applications"));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createVerticalStrut(8));
        text.add(description);

        panel.add(text, BorderLayout.CENTER);
        panel.add(review, BorderLayout.EAST);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        return panel;
    }

    private JPanel createPlaceholder(String title, String description) {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(Theme.BACKGROUND);
        page.setBorder(new EmptyBorder(30, 30, 30, 30));

        RoundedPanel card = new RoundedPanel(20);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.WHITE);
        card.setBorder(new EmptyBorder(35, 35, 35, 35));
        card.setPreferredSize(new Dimension(560, 250));

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.TITLE);
        heading.setForeground(Theme.NAVY);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel detail = new JLabel(
                "<html><div style='text-align:center;width:400px'>"
                        + description + "</div></html>"
        );
        detail.setFont(Theme.BODY);
        detail.setForeground(Theme.MUTED_TEXT);
        detail.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel status = new JLabel("Module UI is ready for implementation.");
        status.setFont(Theme.SMALL);
        status.setForeground(Theme.TEAL);
        status.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(heading);
        card.add(Box.createVerticalStrut(18));
        card.add(detail);
        card.add(Box.createVerticalStrut(20));
        card.add(status);
        card.add(Box.createVerticalGlue());

        page.add(card);
        return page;
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.HEADING);
        label.setForeground(Theme.NAVY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
