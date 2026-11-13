
package com.citizenconnect.ui;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.ServiceRepository;
import com.citizenconnect.repository.UserRepository;
import org.bson.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PublicServicesFrame extends JFrame {

    private final UserRepository.AuthenticatedUser currentUser;

    private static final Color NAVY = new Color(16, 39, 67);
    private static final Color PRIMARY = new Color(23, 103, 194);
    private static final Color TEAL = new Color(13, 152, 127);
    private static final Color BG = new Color(244, 247, 251);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(220, 228, 238);

    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JPanel servicesPanel;
    private JLabel resultLabel;
    private JButton refreshButton;
    private JScrollPane mainScroll;

    private List<Document> allServices = new ArrayList<>();
    private boolean loading;

    public PublicServicesFrame(UserRepository.AuthenticatedUser user) {

        if (user == null || !"CITIZEN".equals(user.role())) {
            throw new IllegalArgumentException(
                    "Only authenticated citizens can access public services.");
        }

        currentUser = user;

        setTitle("CitizenConnect Pro - Public Services");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        setLocationRelativeTo(null);

        loadServices();
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

        addNav(menu, "DB", "Dashboard", false);
        addNav(menu, "01", "My Profile", false);
        addNav(menu, "02", "Public Services", true);
        addNav(menu, "03", "My Applications", false);
        addNav(menu, "04", "My Complaints", false);
        addNav(menu, "05", "Important Information", false);
        addNav(menu, "06", "Emergency Alerts", false);
        addNav(menu, "07", "Volunteer Registration", false);
        addNav(menu, "08", "Feedback & Rating", false);

        sidebar.add(menu, BorderLayout.NORTH);
        sidebar.add(createSidebarFooter(), BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNav(
            JPanel parent,
            String icon,
            String title,
            boolean selected) {

        JButton button = roundedButton(
                icon + "     " + title,
                selected ? PRIMARY : NAVY,
                selected ? WHITE : new Color(197, 211, 226),
                9
        );

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font(
                "Segoe UI",
                selected ? Font.BOLD : Font.PLAIN,
                12
        ));

        button.setBorder(new EmptyBorder(12, 13, 12, 8));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        button.setPreferredSize(new Dimension(220, 43));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

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
                    break;

                default:
                    JOptionPane.showMessageDialog(
                            this,
                            title + " screen will be connected later.",
                            "Screen Integration",
                            JOptionPane.INFORMATION_MESSAGE
                    );
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

        RoundedPanel profile = new RoundedPanel(
                13, new Color(27, 53, 82));

        profile.setLayout(new BorderLayout(10, 0));
        profile.setBorder(new EmptyBorder(12, 11, 12, 9));
        profile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        String name = safe(currentUser.fullName(), "Citizen");

        JLabel avatar = new JLabel(
                name.substring(0, 1).toUpperCase(),
                SwingConstants.CENTER
        );

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
                "Sign Out",
                new Color(37, 62, 88),
                new Color(255, 210, 210),
                10
        );

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
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(createTopBar());
        content.add(Box.createVerticalStrut(22));

        content.add(createWelcomeBanner());
        content.add(Box.createVerticalStrut(23));

        content.add(createSearchSection());
        content.add(Box.createVerticalStrut(25));

        content.add(createResultHeader());
        content.add(Box.createVerticalStrut(15));

        servicesPanel = new JPanel(new GridLayout(0, 3, 17, 17));
        servicesPanel.setOpaque(false);
        servicesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        setServicesPanelHeight(1);

        content.add(servicesPanel);

        mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.setBackground(BG);
        mainScroll.getViewport().setBackground(BG);
        mainScroll.setWheelScrollingEnabled(true);
        mainScroll.getVerticalScrollBar().setUnitIncrement(24);
        mainScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Ensure the content always uses the viewport width.
        mainScroll.getViewport().addComponentListener(
                new java.awt.event.ComponentAdapter() {

                    @Override
                    public void componentResized(
                            java.awt.event.ComponentEvent e) {

                        int width = mainScroll.getViewport().getWidth();

                        if (width > 0) {
                            content.setPreferredSize(
                                    new Dimension(width, content.getPreferredSize().height));

                            content.revalidate();
                        }
                    }
                }
        );

        outer.add(mainScroll, BorderLayout.CENTER);

        return outer;
    }

    private void setServicesPanelHeight(int rows) {

        int cardHeight = 265;
        int gap = 17;

        int totalHeight =
                rows * cardHeight + Math.max(0, rows - 1) * gap;

        servicesPanel.setPreferredSize(
                new Dimension(900, totalHeight));

        servicesPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, totalHeight));
    }

    // =========================================
    // TOP BAR
    // =========================================

    private JPanel createTopBar() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Public Services");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(NAVY);

        refreshButton = roundedButton(
                "↻  Refresh", WHITE, PRIMARY, 11);

        refreshButton.setBorder(new EmptyBorder(10, 17, 10, 17));

        refreshButton.addActionListener(e -> loadServices());

        panel.add(title, BorderLayout.WEST);
        panel.add(refreshButton, BorderLayout.EAST);

        return panel;
    }

    // =========================================
    // WELCOME BANNER
    // =========================================

    private JPanel createWelcomeBanner() {

        RoundedPanel banner = new RoundedPanel(
                19, new Color(19, 65, 105));

        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(27, 30, 27, 30));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Find the Services You Need");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 25));
        heading.setForeground(WHITE);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "Explore public services, review requirements and learn how to apply.");

        description.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        description.setForeground(new Color(220, 233, 245));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(heading);
        text.add(Box.createVerticalStrut(10));
        text.add(description);

        banner.add(text, BorderLayout.CENTER);

        return banner;
    }

    // =========================================
    // SEARCH AND FILTER
    // =========================================

    private JPanel createSearchSection() {

        RoundedPanel card = new RoundedPanel(16, WHITE);

        card.setLayout(new BorderLayout(12, 13));
        card.setBorder(new EmptyBorder(19, 21, 19, 21));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Search and Filter Services");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(NAVY);

        JPanel controls = new JPanel(new GridLayout(1, 2, 14, 0));
        controls.setOpaque(false);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setForeground(TEXT);
        searchField.setBorder(new RoundedBorder(BORDER, 10, 1));
        searchField.setMargin(new Insets(10, 13, 10, 13));
        searchField.setToolTipText(
                "Search by service name or description");

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        applyFilters();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        applyFilters();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        applyFilters();
                    }
                }
        );

        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryCombo.setBackground(WHITE);
        categoryCombo.setBorder(new RoundedBorder(BORDER, 10, 1));
        categoryCombo.addActionListener(e -> applyFilters());

        controls.add(searchField);
        controls.add(categoryCombo);

        card.add(heading, BorderLayout.NORTH);
        card.add(controls, BorderLayout.CENTER);

        return card;
    }

    // =========================================
    // RESULT HEADER
    // =========================================

    private JPanel createResultHeader() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Available Services");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 21));
        heading.setForeground(NAVY);

        resultLabel = new JLabel("Loading services...");
        resultLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resultLabel.setForeground(MUTED);

        panel.add(heading, BorderLayout.WEST);
        panel.add(resultLabel, BorderLayout.EAST);

        return panel;
    }

    // =========================================
    // LOAD SERVICES FROM MONGODB
    // =========================================

    private void loadServices() {

        if (loading) {
            return;
        }

        loading = true;

        refreshButton.setEnabled(false);
        resultLabel.setText("Loading...");

        servicesPanel.removeAll();
        servicesPanel.setLayout(new BorderLayout());
        servicesPanel.setPreferredSize(new Dimension(900, 220));
        servicesPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 220));

        JLabel loadingLabel = new JLabel(
                "Loading services from database...",
                SwingConstants.CENTER);

        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loadingLabel.setForeground(MUTED);

        servicesPanel.add(loadingLabel, BorderLayout.CENTER);
        servicesPanel.revalidate();
        servicesPanel.repaint();

        SwingWorker<List<Document>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<Document> doInBackground() {
                        return new ServiceRepository().getActiveServices();
                    }

                    @Override
                    protected void done() {

                        loading = false;
                        refreshButton.setEnabled(true);

                        try {

                            allServices = get();

                            updateCategories();
                            applyFilters();

                        } catch (Exception ex) {

                            showEmptyState(
                                    "Unable to Load Services",
                                    "Please check your database connection and try again."
                            );

                            resultLabel.setText("Loading failed");

                            JOptionPane.showMessageDialog(
                                    PublicServicesFrame.this,
                                    "Could not load public services.\n\n"
                                            + rootMessage(ex),
                                    "Database Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                };

        worker.execute();
    }

    // =========================================
    // CATEGORY FILTER
    // =========================================

    private void updateCategories() {

        String previous = categoryCombo.getSelectedItem() == null
                ? "All Categories"
                : categoryCombo.getSelectedItem().toString();

        categoryCombo.removeAllItems();
        categoryCombo.addItem("All Categories");

        List<String> categories = new ArrayList<>();

        for (Document service : allServices) {

            String category = service.getString("category");

            if (category != null && !category.isBlank()
                    && !categories.contains(category.trim())) {

                categories.add(category.trim());
            }
        }

        categories.sort(String.CASE_INSENSITIVE_ORDER);

        for (String category : categories) {
            categoryCombo.addItem(category);
        }

        categoryCombo.setSelectedItem(previous);

        if (categoryCombo.getSelectedIndex() == -1) {
            categoryCombo.setSelectedIndex(0);
        }
    }

    // =========================================
    // SEARCH FILTER
    // =========================================

    private void applyFilters() {

        if (servicesPanel == null || searchField == null
                || categoryCombo == null) {
            return;
        }

        String search = searchField.getText()
                .trim().toLowerCase(Locale.ROOT);

        String selectedCategory =
                categoryCombo.getSelectedItem() == null
                        ? "All Categories"
                        : categoryCombo.getSelectedItem().toString();

        List<Document> filtered = new ArrayList<>();

        for (Document service : allServices) {

            String name = safe(
                    service.getString("serviceName"), "");

            String description = safe(
                    service.getString("description"), "");

            String category = safe(
                    service.getString("category"), "");

            boolean matchesSearch =
                    name.toLowerCase(Locale.ROOT).contains(search)
                            || description.toLowerCase(Locale.ROOT)
                            .contains(search);

            boolean matchesCategory =
                    selectedCategory.equals("All Categories")
                            || category.equalsIgnoreCase(selectedCategory);

            if (matchesSearch && matchesCategory) {
                filtered.add(service);
            }
        }

        displayServices(filtered);
    }

    // =========================================
    // DISPLAY SERVICES
    // =========================================

    private void displayServices(List<Document> services) {

        servicesPanel.removeAll();

        resultLabel.setText(
                services.size() + " service(s) found");

        if (services.isEmpty()) {

            showEmptyState(
                    allServices.isEmpty()
                            ? "No Public Services Available"
                            : "No Matching Services Found",

                    allServices.isEmpty()
                            ? "There are currently no active services."
                            : "Try another search keyword or category."
            );

            return;
        }

        int rows = (int) Math.ceil(services.size() / 3.0);

        servicesPanel.setLayout(new GridLayout(0, 3, 17, 17));
        setServicesPanelHeight(rows);

        for (Document service : services) {
            servicesPanel.add(createServiceCard(service));
        }

        servicesPanel.revalidate();
        servicesPanel.repaint();

        mainScroll.getViewport().revalidate();
    }

    // =========================================
    // SERVICE CARD
    // =========================================

    private JPanel createServiceCard(Document service) {

        RoundedPanel card = new RoundedPanel(18, WHITE);

        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(19, 19, 18, 19));

        card.setPreferredSize(new Dimension(300, 265));
        card.setMinimumSize(new Dimension(220, 265));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 265));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel category = new JLabel(
                safe(service.getString("category"),
                        "Public Service").toUpperCase(Locale.ROOT));

        category.setFont(new Font("Segoe UI", Font.BOLD, 9));
        category.setForeground(TEAL);
        category.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel(
                "<html><div style='width:230px;'>"
                        + escapeHtml(safe(
                                service.getString("serviceName"),
                                "Unnamed Service"))
                        + "</div></html>");

        name.setFont(new Font("Segoe UI", Font.BOLD, 16));
        name.setForeground(NAVY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html><div style='width:230px;'>"
                        + escapeHtml(shortDescription(
                                service.getString("description")))
                        + "</div></html>");

        description.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        description.setForeground(MUTED);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(category);
        top.add(Box.createVerticalStrut(11));
        top.add(name);
        top.add(Box.createVerticalStrut(9));
        top.add(description);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        JLabel fee = new JLabel(
                "Fee: " + formatFee(service.get("applicationFee")));

        fee.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fee.setForeground(NAVY);
        fee.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel processing = new JLabel(
                "Processing: " + safe(
                        service.getString("processingTime"),
                        "Not specified"));

        processing.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        processing.setForeground(MUTED);
        processing.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton viewButton = roundedButton(
                "View Details  →", PRIMARY, WHITE, 11);

        viewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 40));

        viewButton.addActionListener(e -> showServiceDetails(service));

        bottom.add(fee);
        bottom.add(Box.createVerticalStrut(5));
        bottom.add(processing);
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(viewButton);

        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private void showServiceDetails(Document service) {

        if (service == null) {
            return;
        }

        new ServiceDetailsFrame(currentUser, service).setVisible(true);
        dispose();
    }

    // =========================================
    // EMPTY STATE
    // =========================================

    private void showEmptyState(
            String heading,
            String description) {

        servicesPanel.removeAll();
        servicesPanel.setLayout(new BorderLayout());

        servicesPanel.setPreferredSize(new Dimension(900, 230));
        servicesPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 230));

        JPanel empty = new JPanel();
        empty.setOpaque(false);
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        empty.setBorder(new EmptyBorder(45, 20, 45, 20));

        JLabel icon = new JLabel("◇", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 42));
        icon.setForeground(new Color(155, 170, 188));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(heading);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(NAVY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel(description);
        message.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        message.setForeground(MUTED);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        empty.add(icon);
        empty.add(Box.createVerticalStrut(12));
        empty.add(title);
        empty.add(Box.createVerticalStrut(8));
        empty.add(message);

        servicesPanel.add(empty, BorderLayout.CENTER);

        servicesPanel.revalidate();
        servicesPanel.repaint();
    }

    // =========================================
    // ROUNDED BUTTON
    // =========================================

    private JButton roundedButton(
            String text,
            Color background,
            Color foreground,
            int radius) {

        JButton button = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                Color color = isEnabled()
                        ? getBackground()
                        : new Color(190, 198, 208);

                if (getModel().isPressed()) {
                    color = color.darker();
                } else if (getModel().isRollover()) {
                    color = color.brighter();
                }

                g2.setColor(color);

                g2.fillRoundRect(
                        0, 0,
                        getWidth(), getHeight(),
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

    // =========================================
    // COMMON HELPERS
    // =========================================

    private String shortDescription(String description) {

        if (description == null || description.isBlank()) {
            return "No description available.";
        }

        String value = description.trim();

        if (value.length() > 115) {
            value = value.substring(0, 112) + "...";
        }

        return value;
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

    private String escapeHtml(String value) {

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String safe(String value, String fallback) {

        return value == null || value.isBlank()
                ? fallback
                : value;
    }

    private String rootMessage(Exception exception) {

        Throwable cause = exception;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        String message = cause.getMessage();

        return message == null || message.isBlank()
                ? cause.getClass().getSimpleName()
                : message;
    }

    // =========================================
    // NAVIGATION
    // =========================================

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
                    0, 0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius, radius);

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =========================================
    // ROUNDED BORDER
    // =========================================

    private static class RoundedBorder
            extends javax.swing.border.AbstractBorder {

        private final Color color;
        private final int radius;
        private final int thickness;

        RoundedBorder(Color color, int radius, int thickness) {

            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(
                Component c,
                Graphics g,
                int x,
                int y,
                int width,
                int height) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));

            g2.drawRoundRect(
                    x, y,
                    width - 1,
                    height - 1,
                    radius, radius);

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 12, 8, 12);
        }
    }
}
