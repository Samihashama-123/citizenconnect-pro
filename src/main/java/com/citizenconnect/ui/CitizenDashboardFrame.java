
package com.citizenconnect.ui;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.ApplicationRepository;
import com.citizenconnect.repository.ComplaintRepository;
import com.citizenconnect.repository.EmergencyAlertRepository;
import com.citizenconnect.repository.ServiceRepository;
import com.citizenconnect.repository.UserRepository;

import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CitizenDashboardFrame extends JFrame {

    private final UserRepository.AuthenticatedUser currentUser;

    private final Color NAVY = new Color(16, 39, 67);
    private final Color NAVY_LIGHT = new Color(27, 53, 82);
    private final Color PRIMARY = new Color(23, 103, 194);
    private final Color TEAL = new Color(13, 152, 127);
    private final Color BACKGROUND = new Color(244, 247, 251);
    private final Color WHITE = Color.WHITE;
    private final Color MUTED = new Color(100, 116, 139);
    private final Color BORDER = new Color(220, 228, 238);

    private JLabel totalApplicationsValue;
    private JLabel pendingApplicationsValue;
    private JLabel totalComplaintsValue;
    private JLabel activeServicesValue;
    private JLabel activeAlertsValue;
    private JLabel lastUpdatedLabel;

    private JPanel recentApplicationsHolder;
    private JPanel recentComplaintsHolder;
    private JPanel alertsHolder;

    private JButton refreshButton;
    private boolean loading;

    private JLabel profileCompletionLabel;
    private JProgressBar profileCompletionBar;
    private JLabel profileCompletionMessage;
    private JButton completeProfileButton;

    public CitizenDashboardFrame(UserRepository.AuthenticatedUser user) {

        if (user == null || !"CITIZEN".equals(user.role())) {
            throw new IllegalArgumentException(
                    "Only authenticated citizens can access this dashboard.");
        }

        currentUser = user;

        setTitle("CitizenConnect Pro - Citizen Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        setLocationRelativeTo(null);

        loadDashboardData();
        loadProfileCompletion();
    }

    // ==================== SIDEBAR ====================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(255, 0));

        JPanel menu = new JPanel();
        menu.setBackground(NAVY);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(28, 18, 15, 18));

        JLabel brand = new JLabel("CitizenConnect Pro");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("SMART PUBLIC SERVICE PLATFORM");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        subtitle.setForeground(new Color(170, 192, 214));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        menu.add(brand);
        menu.add(Box.createVerticalStrut(6));
        menu.add(subtitle);
        menu.add(Box.createVerticalStrut(35));

        JLabel menuHeading = new JLabel("CITIZEN MENU");
        menuHeading.setFont(new Font("Segoe UI", Font.BOLD, 10));
        menuHeading.setForeground(new Color(145, 170, 195));
        menuHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        menu.add(menuHeading);
        menu.add(Box.createVerticalStrut(14));

        addNav(menu, "DB", "Dashboard", true);
        addNav(menu, "01", "My Profile", false);
        addNav(menu, "02", "Public Services", false);
        addNav(menu, "03", "My Applications", false);
        addNav(menu, "04", "Submit Complaint", false);
        addNav(menu, "05", "My Complaints", false);
        addNav(menu, "06", "Important Information", false);
        addNav(menu, "07", "Emergency Alerts", false);
        addNav(menu, "08", "Volunteer Registration", false);
        addNav(menu, "09", "Feedback & Rating", false);

        sidebar.add(menu, BorderLayout.NORTH);
        sidebar.add(createSidebarFooter(), BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createSidebarFooter() {

        JPanel footer = new JPanel();
        footer.setBackground(NAVY);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(new EmptyBorder(10, 15, 18, 15));

        JPanel profile = new JPanel(new BorderLayout(10, 0));
        profile.setBackground(NAVY_LIGHT);
        profile.setBorder(new EmptyBorder(12, 11, 12, 9));
        profile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        profile.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel avatar = new JLabel(getInitial(), SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(TEAL);
        avatar.setForeground(WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        avatar.setPreferredSize(new Dimension(42, 42));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(safe(currentUser.fullName(), "Citizen"));
        name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        name.setForeground(WHITE);

        JLabel role = new JLabel("Citizen Account");
        role.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        role.setForeground(new Color(185, 204, 222));

        info.add(name);
        info.add(Box.createVerticalStrut(4));
        info.add(role);

        profile.add(avatar, BorderLayout.WEST);
        profile.add(info, BorderLayout.CENTER);

        JButton logout = new JButton("Sign Out");
        logout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logout.setForeground(new Color(255, 210, 210));
        logout.setBackground(new Color(37, 62, 88));
        logout.setFocusPainted(false);
        logout.setBorderPainted(false);
        logout.setOpaque(true);
        logout.setBorder(new EmptyBorder(12, 14, 12, 14));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> logout());

        footer.add(profile);
        footer.add(Box.createVerticalStrut(10));
        footer.add(logout);

        return footer;
    }

    private void addNav(JPanel parent, String icon, String title,
                        boolean selected) {

        JButton button = new JButton(icon + "     " + title);

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Segoe UI",
                selected ? Font.BOLD : Font.PLAIN, 12));

        button.setForeground(selected
                ? WHITE : new Color(197, 211, 226));

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
                    button.setBackground(NAVY_LIGHT);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    button.setBackground(NAVY);
                }
            });

            button.addActionListener(e -> {

                switch (title) {

                    case "My Profile":
                        openProfile();
                        break;

                    case "Public Services":
                        openPublicServices();
                        break;

                    case "My Applications":
                        openMyApplications();
                        break;

                    case "Submit Complaint":
                        openSubmitComplaint();
                        break;

                    case "My Complaints":
                        openMyComplaints();
                        break;

                    case "Important Information":
                        openImportantInformation();
                        break;

                    case "Emergency Alerts":
                        openEmergencyAlerts();
                        break;

                    case "Volunteer Registration":
                        openVolunteerRegistration();
                        break;

                    case "Feedback & Rating":
                        openFeedbackRating();
                        break;

                    default:
                        showNavigationNotice(title);
                        break;
                }
            });
        }

        parent.add(button);
        parent.add(Box.createVerticalStrut(5));
    }

    private String getInitial() {

        String name = currentUser.fullName();

        if (name == null || name.isBlank()) {
            return "C";
        }

        return name.trim().substring(0, 1)
                .toUpperCase(Locale.ROOT);
    }

    // ==================== MAIN CONTENT ====================

    private JPanel createMainContent() {

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(25, 30, 28, 30));

        addFullWidth(content, createTopBar());

        content.add(Box.createVerticalStrut(20));

        addFullWidth(content, createWelcomeBanner());

        content.add(Box.createVerticalStrut(15));
        addFullWidth(content, createProfileCompletionCard());

        content.add(Box.createVerticalStrut(25));

        addFullWidth(content, sectionTitle(
                "Dashboard Overview",
                "A quick summary of your public service activities"));

        content.add(Box.createVerticalStrut(13));

        addFullWidth(content, createStatistics());

        content.add(Box.createVerticalStrut(25));

        addFullWidth(content, sectionTitle(
                "Quick Access",
                "Access the most frequently used citizen services"));

        content.add(Box.createVerticalStrut(13));

        addFullWidth(content, createQuickAccess());

        content.add(Box.createVerticalStrut(25));

        addFullWidth(content, sectionTitle(
                "Your Recent Activity",
                "Your latest applications and complaints"));

        content.add(Box.createVerticalStrut(13));

        addFullWidth(content, createRecentActivity());

        content.add(Box.createVerticalStrut(25));

        addFullWidth(content, sectionTitle(
                "Public Updates",
                "Important information and active emergency announcements"));

        content.add(Box.createVerticalStrut(13));

        addFullWidth(content, createPublicUpdates());

        content.add(Box.createVerticalStrut(18));

        addFullWidth(content, createFooter());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setBackground(BACKGROUND);
        scroll.getViewport().setBackground(BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        outer.add(scroll, BorderLayout.CENTER);

        return outer;
    }

    private void addFullWidth(JPanel parent, JComponent component) {

        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, Integer.MAX_VALUE));

        parent.add(component);
    }

    // ==================== PROFILE COMPLETION ====================

    private JPanel createProfileCompletionCard() {

        JPanel card = new SoftCard();
        card.setLayout(new BorderLayout(15, 5));
        card.setBorder(new EmptyBorder(17, 20, 17, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Complete Your Profile");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        heading.setForeground(NAVY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        profileCompletionMessage = new JLabel(
                "Loading your profile information...");
        profileCompletionMessage.setFont(
                new Font("Segoe UI", Font.PLAIN, 11));
        profileCompletionMessage.setForeground(MUTED);
        profileCompletionMessage.setAlignmentX(Component.LEFT_ALIGNMENT);

        profileCompletionBar = new JProgressBar(0, 100);
        profileCompletionBar.setValue(0);
        profileCompletionBar.setStringPainted(false);
        profileCompletionBar.setForeground(TEAL);
        profileCompletionBar.setBackground(new Color(230, 237, 245));
        profileCompletionBar.setBorderPainted(false);
        profileCompletionBar.setPreferredSize(new Dimension(300, 9));
        profileCompletionBar.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 9));
        profileCompletionBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(heading);
        left.add(Box.createVerticalStrut(6));
        left.add(profileCompletionMessage);
        left.add(Box.createVerticalStrut(12));
        left.add(profileCompletionBar);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        profileCompletionLabel = new JLabel("0%");
        profileCompletionLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 24));
        profileCompletionLabel.setForeground(TEAL);
        profileCompletionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        completeProfileButton = new JButton("Complete Profile");
        completeProfileButton.setFont(
                new Font("Segoe UI", Font.BOLD, 11));
        completeProfileButton.setForeground(WHITE);
        completeProfileButton.setBackground(PRIMARY);
        completeProfileButton.setFocusPainted(false);
        completeProfileButton.setBorderPainted(false);
        completeProfileButton.setOpaque(true);
        completeProfileButton.setBorder(
                new EmptyBorder(9, 15, 9, 15));
        completeProfileButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        completeProfileButton.addActionListener(e -> openProfile());

        right.add(profileCompletionLabel);
        right.add(Box.createVerticalStrut(7));
        right.add(completeProfileButton);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    private void loadProfileCompletion() {

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {

            @Override
            protected Integer doInBackground() {

                UserRepository repository = new UserRepository();

                Document profile = repository.getCitizenProfile(
                        currentUser.id());

                if (profile == null) {
                    return 0;
                }

                int completed = 0;

                String fullName = profile.getString("fullName");
                String email = profile.getString("email");
                String phone = profile.getString("phone");
                String dateOfBirth = profile.getString("dateOfBirth");
                String gender = profile.getString("gender");
                String division = profile.getString("division");
                String district = profile.getString("district");
                String upazila = profile.getString("upazila");
                String detailedAddress =
                        profile.getString("detailedAddress");
                String preferredLanguage =
                        profile.getString("preferredLanguage");

                if (hasValue(fullName)) completed++;
                if (hasValue(email)) completed++;
                if (hasValue(phone)) completed++;
                if (hasValue(dateOfBirth)) completed++;
                if (hasValue(gender)) completed++;
                if (hasValue(division)) completed++;
                if (hasValue(district)) completed++;
                if (hasValue(upazila)) completed++;

                if (hasValue(detailedAddress)
                        || hasValue(profile.getString("address"))) {
                    completed++;
                }

                if (hasValue(preferredLanguage)) completed++;

                return completed * 10;
            }

            @Override
            protected void done() {

                try {

                    int percentage = get();
                    updateProfileCompletion(percentage);

                } catch (Exception ex) {

                    profileCompletionMessage.setText(
                            "Unable to load profile completion.");
                }
            }
        };

        worker.execute();
    }

    private boolean hasValue(String value) {

        return value != null && !value.isBlank();
    }

    private void updateProfileCompletion(int percentage) {

        profileCompletionBar.setValue(percentage);
        profileCompletionLabel.setText(percentage + "%");

        if (percentage >= 100) {

            profileCompletionMessage.setText(
                    "Great! Your profile is complete.");

            profileCompletionLabel.setForeground(TEAL);
            profileCompletionBar.setForeground(TEAL);
            completeProfileButton.setText("View Profile");

        } else {

            int remaining = 100 - percentage;

            profileCompletionMessage.setText(
                    "Complete your information to improve your profile. "
                            + remaining + "% remaining.");

            profileCompletionLabel.setForeground(PRIMARY);
            profileCompletionBar.setForeground(PRIMARY);
            completeProfileButton.setText("Complete Profile");
        }

        profileCompletionBar.repaint();
    }

    // ==================== TOP BAR ====================

    private JPanel createTopBar() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(NAVY);

        JPanel right = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        lastUpdatedLabel = new JLabel("Loading dashboard...");
        lastUpdatedLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 11));
        lastUpdatedLabel.setForeground(MUTED);

        refreshButton = new JButton("Refresh");
        refreshButton.setFont(
                new Font("Segoe UI", Font.BOLD, 11));
        refreshButton.setForeground(PRIMARY);
        refreshButton.setBackground(WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 13, 8, 13)));

        refreshButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        refreshButton.addActionListener(e -> {
            loadDashboardData();
            loadProfileCompletion();
        });

        right.add(lastUpdatedLabel);
        right.add(refreshButton);

        panel.add(title, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    // ==================== WELCOME BANNER ====================

    private JPanel createWelcomeBanner() {

        JPanel banner = new JPanel(new BorderLayout()) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setPaint(new GradientPaint(
                        0, 0,
                        new Color(16, 43, 75),
                        getWidth(), getHeight(),
                        new Color(27, 91, 132)));

                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(getWidth() - 170, -65, 220, 220);
                g2.fillOval(getWidth() - 110, 75, 110, 110);

                g2.dispose();

                super.paintComponent(g);
            }
        };

        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(25, 29, 25, 25));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("WELCOME BACK");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 10));
        eyebrow.setForeground(new Color(126, 224, 210));

        JLabel name = new JLabel(
                safe(currentUser.fullName(), "Citizen"));

        name.setFont(new Font("Segoe UI", Font.BOLD, 28));
        name.setForeground(WHITE);

        JLabel description = new JLabel(
                "Your public services, applications and community support in one place.");

        description.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        description.setForeground(new Color(220, 233, 245));

        text.add(eyebrow);
        text.add(Box.createVerticalStrut(7));
        text.add(name);
        text.add(Box.createVerticalStrut(6));
        text.add(description);

        banner.add(text, BorderLayout.CENTER);

        return banner;
    }

    // ==================== SECTION TITLE ====================

    private JPanel sectionTitle(String title, String subtitle) {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(NAVY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel detail = new JLabel(subtitle);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detail.setForeground(MUTED);
        detail.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(4));
        panel.add(detail);

        return panel;
    }

    // ==================== STATISTICS ====================

    private JPanel createStatistics() {

        JPanel panel = new AdaptiveGridPanel(
                5, 3, 1, 112, 210, 13);

        panel.setOpaque(false);

        totalApplicationsValue = new JLabel("—");
        pendingApplicationsValue = new JLabel("—");
        totalComplaintsValue = new JLabel("—");
        activeServicesValue = new JLabel("—");
        activeAlertsValue = new JLabel("—");

        panel.add(statCard(
                "Total Applications",
                totalApplicationsValue,
                "All your submissions",
                PRIMARY,
                "A"));

        panel.add(statCard(
                "Pending Applications",
                pendingApplicationsValue,
                "Awaiting a decision",
                new Color(207, 143, 47),
                "P"));

        panel.add(statCard(
                "My Complaints",
                totalComplaintsValue,
                "Submitted by you",
                new Color(145, 91, 180),
                "C"));

        panel.add(statCard(
                "Available Services",
                activeServicesValue,
                "Currently active",
                TEAL,
                "S"));

        panel.add(statCard(
                "Active Alerts",
                activeAlertsValue,
                "Public announcements",
                new Color(194, 76, 76),
                "!"));

        return panel;
    }

    private JPanel statCard(
            String title,
            JLabel value,
            String description,
            Color accent,
            String symbol) {

        JPanel card = new SoftCard();
        card.setLayout(new BorderLayout(8, 0));
        card.setBorder(new EmptyBorder(14, 14, 13, 12));
        card.setPreferredSize(new Dimension(210, 104));

        JLabel icon = new JLabel(symbol, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        icon.setForeground(accent);
        icon.setOpaque(true);

        icon.setBackground(new Color(
                accent.getRed(),
                accent.getGreen(),
                accent.getBlue(),
                22));

        icon.setPreferredSize(new Dimension(36, 36));

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 10));
        heading.setForeground(MUTED);

        value.setFont(new Font("Segoe UI", Font.BOLD, 25));
        value.setForeground(NAVY);

        JLabel detail = new JLabel(description);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        detail.setForeground(MUTED);

        labels.add(heading);
        labels.add(Box.createVerticalStrut(5));
        labels.add(value);
        labels.add(Box.createVerticalStrut(2));
        labels.add(detail);

        card.add(icon, BorderLayout.WEST);
        card.add(labels, BorderLayout.CENTER);

        return card;
    }

    // ==================== QUICK ACCESS ====================

    private JPanel createQuickAccess() {

        JPanel panel = new AdaptiveGridPanel(
                4, 2, 1, 140, 250, 13);

        panel.setOpaque(false);

        panel.add(quickCard(
                "Public Services",
                "Explore available services",
                "Explore Services",
                PRIMARY));

        panel.add(quickCard(
                "My Applications",
                "View and track submissions",
                "View Applications",
                TEAL));

        panel.add(quickCard(
                "My Complaints",
                "Submit and track complaints",
                "View Complaints",
                new Color(194, 135, 48)));

        panel.add(quickCard(
                "Emergency Alerts",
                "Check important public alerts",
                "View Alerts",
                new Color(194, 76, 76)));

        return panel;
    }

    private JPanel quickCard(
            String title,
            String description,
            String actionText,
            Color accent) {

        JPanel card = new SoftCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 13, 16));
        card.setPreferredSize(new Dimension(250, 126));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(NAVY);

        JLabel detail = new JLabel(
                "<html><div style='width:190px;'>"
                        + description + "</div></html>");

        detail.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        detail.setForeground(MUTED);

        top.add(heading);
        top.add(Box.createVerticalStrut(6));
        top.add(detail);

        JButton action = new JButton(actionText);
        action.setFont(new Font("Segoe UI", Font.BOLD, 10));
        action.setForeground(accent);
        action.setBackground(new Color(245, 248, 252));
        action.setFocusPainted(false);
        action.setBorderPainted(false);
        action.setBorder(new EmptyBorder(9, 10, 9, 10));
        action.setHorizontalAlignment(SwingConstants.CENTER);

        action.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        action.addActionListener(e -> {

            switch (title) {

                case "Public Services":
                    openPublicServices();
                    break;

                case "My Applications":
                    openMyApplications();
                    break;

                case "My Complaints":
                    openMyComplaints();
                    break;

                case "Emergency Alerts":
                    openEmergencyAlerts();
                    break;

                default:
                    showNavigationNotice(title);
                    break;
            }
        });

        card.add(top, BorderLayout.CENTER);
        card.add(action, BorderLayout.SOUTH);

        return card;
    }

    // ==================== RECENT ACTIVITY ====================

    private JPanel createRecentActivity() {

        JPanel panel = new AdaptiveGridPanel(
                2, 2, 1, 205, 360, 13);

        panel.setOpaque(false);

        recentApplicationsHolder = new JPanel(new BorderLayout());
        recentApplicationsHolder.setOpaque(false);

        recentComplaintsHolder = new JPanel(new BorderLayout());
        recentComplaintsHolder.setOpaque(false);

        panel.add(dataPanel(
                "Recent Applications",
                recentApplicationsHolder));

        panel.add(dataPanel(
                "Recent Complaints",
                recentComplaintsHolder));

        showEmpty(
                recentApplicationsHolder,
                "You have not submitted any applications yet.");

        showEmpty(
                recentComplaintsHolder,
                "You have not submitted any complaints yet.");

        return panel;
    }

    private JPanel dataPanel(String title, JPanel body) {

        JPanel card = new SoftCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(15, 16, 14, 16));
        card.setPreferredSize(new Dimension(360, 190));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(NAVY);

        card.add(heading, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    // ==================== PUBLIC UPDATES ====================

    private JPanel createPublicUpdates() {

        JPanel panel = new AdaptiveGridPanel(
                2, 2, 1, 175, 360, 13);

        panel.setOpaque(false);

        alertsHolder = new JPanel();
        alertsHolder.setOpaque(false);
        alertsHolder.setLayout(
                new BoxLayout(alertsHolder, BoxLayout.Y_AXIS));

        JPanel information = new JPanel(
                new BorderLayout(0, 8));

        information.setBackground(WHITE);
        information.setPreferredSize(new Dimension(360, 175));
        information.setBorder(
                new EmptyBorder(17, 18, 15, 18));

        JLabel heading = new JLabel("Important Information");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(NAVY);

        JLabel description = new JLabel(
                "<html>Read public service notices, "
                        + "citizen guidance and important announcements.</html>");

        description.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        description.setForeground(MUTED);

        JButton openInfo = simpleActionButton(
                "Explore Information");

        openInfo.addActionListener(
                e -> openImportantInformation());

        information.add(heading, BorderLayout.NORTH);
        information.add(description, BorderLayout.CENTER);
        information.add(openInfo, BorderLayout.SOUTH);

        JPanel alerts = new JPanel(new BorderLayout(0, 8));
        alerts.setBackground(WHITE);
        alerts.setPreferredSize(new Dimension(360, 175));
        alerts.setBorder(new EmptyBorder(17, 18, 15, 18));

        JLabel alertHeading = new JLabel("Emergency Alerts");
        alertHeading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        alertHeading.setForeground(NAVY);

        JScrollPane alertScroll = new JScrollPane(alertsHolder);
        alertScroll.setBorder(null);
        alertScroll.setPreferredSize(new Dimension(200, 65));
        alertScroll.getViewport().setBackground(WHITE);

        JButton openAlerts = simpleActionButton("View All Alerts");

        openAlerts.addActionListener(
                e -> openEmergencyAlerts());

        alerts.add(alertHeading, BorderLayout.NORTH);
        alerts.add(alertScroll, BorderLayout.CENTER);
        alerts.add(openAlerts, BorderLayout.SOUTH);

        panel.add(wrapWhite(information));
        panel.add(wrapWhite(alerts));

        showEmpty(alertsHolder, "No active emergency alerts.");

        return panel;
    }

    private JPanel wrapWhite(JPanel inner) {

        JPanel wrapper = new SoftCard();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(inner, BorderLayout.CENTER);

        return wrapper;
    }

    private JButton simpleActionButton(String text) {

        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 10));
        button.setForeground(PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    // ==================== FOOTER ====================

    private JPanel createFooter() {

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 2, 0, 2));

        JLabel copyright = new JLabel(
                "© CitizenConnect Pro  |  Smart Public Service Platform");

        copyright.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        copyright.setForeground(MUTED);

        JLabel support = new JLabel(
                "Help & Support  |  Contact Information");

        support.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        support.setForeground(PRIMARY);

        footer.add(copyright, BorderLayout.WEST);
        footer.add(support, BorderLayout.EAST);

        return footer;
    }

    // ==================== MONGODB DATA LOADING ====================

    private void loadDashboardData() {

        if (loading) {
            return;
        }

        loading = true;

        if (refreshButton != null) {
            refreshButton.setEnabled(false);
        }

        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Updating...");
        }

        SwingWorker<DashboardData, Void> worker =
                new SwingWorker<>() {

            @Override
            protected DashboardData doInBackground() {

                String userId = currentUser.id();

                if (userId == null || !ObjectId.isValid(userId)) {
                    throw new IllegalStateException(
                            "Your account session is invalid. Please sign in again.");
                }

                ApplicationRepository applicationRepository =
                        new ApplicationRepository();

                ComplaintRepository complaintRepository =
                        new ComplaintRepository();

                ServiceRepository serviceRepository =
                        new ServiceRepository();

                EmergencyAlertRepository alertRepository =
                        new EmergencyAlertRepository();

                List<Document> applications =
                        applicationRepository.getApplicationsByUserId(userId);

                List<Document> complaints =
                        complaintRepository.getComplaintsByUserId(userId);

                List<Document> services =
                        serviceRepository.getActiveServices();

                List<Document> alerts =
                        alertRepository.getActiveAlerts();

                long pending = applications.stream()
                        .filter(d -> {

                            String status =
                                    d.getString("applicationStatus");

                            return status != null
                                    && !status.equalsIgnoreCase("APPROVED")
                                    && !status.equalsIgnoreCase("REJECTED");

                        }).count();

                return new DashboardData(
                        applications,
                        complaints,
                        services,
                        alerts,
                        pending);
            }

            @Override
            protected void done() {

                loading = false;

                if (refreshButton != null) {
                    refreshButton.setEnabled(true);
                }

                try {

                    DashboardData data = get();

                    updateDashboard(data);

                    if (lastUpdatedLabel != null) {
                        lastUpdatedLabel.setText(
                                "Updated "
                                        + new SimpleDateFormat("hh:mm a")
                                        .format(new Date()));
                    }

                } catch (Exception ex) {

                    if (lastUpdatedLabel != null) {
                        lastUpdatedLabel.setText("Could not refresh");
                    }

                    JOptionPane.showMessageDialog(
                            CitizenDashboardFrame.this,
                            "Dashboard data could not be loaded from MongoDB.\n"
                                    + "Please check your connection and try again.\n\n"
                                    + rootMessage(ex),
                            "Dashboard Loading Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    // ==================== UPDATE DASHBOARD ====================

    private void updateDashboard(DashboardData data) {

        totalApplicationsValue.setText(
                String.valueOf(data.applications.size()));

        pendingApplicationsValue.setText(
                String.valueOf(data.pendingApplications));

        totalComplaintsValue.setText(
                String.valueOf(data.complaints.size()));

        activeServicesValue.setText(
                String.valueOf(data.services.size()));

        activeAlertsValue.setText(
                String.valueOf(data.alerts.size()));

        updateApplications(data.applications, data.services);
        updateComplaints(data.complaints);
        updateAlerts(data.alerts);
    }

    // ==================== APPLICATIONS ====================

    private void updateApplications(
            List<Document> applications,
            List<Document> services) {

        recentApplicationsHolder.removeAll();

        if (applications.isEmpty()) {

            showEmpty(
                    recentApplicationsHolder,
                    "You have not submitted any applications yet.");

            return;
        }

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(
                new BoxLayout(rows, BoxLayout.Y_AXIS));

        int limit = Math.min(4, applications.size());

        for (int i = 0; i < limit; i++) {

            Document app = applications.get(i);

            String appNumber = safe(
                    app.getString("applicationNumber"),
                    "Application");

            String status = safe(
                    app.getString("applicationStatus"),
                    "Unknown");

            Date date = app.getDate("submittedAt");

            String serviceName = findServiceName(app, services);

            rows.add(activityRow(
                    appNumber,
                    serviceName,
                    status,
                    formatDate(date),
                    statusColor(status)));

            if (i < limit - 1) {
                rows.add(new JSeparator());
            }
        }

        recentApplicationsHolder.add(rows, BorderLayout.CENTER);
        recentApplicationsHolder.revalidate();
        recentApplicationsHolder.repaint();
    }

    private String findServiceName(
            Document application,
            List<Document> services) {

        Object serviceId = application.get("serviceId");

        if (serviceId == null) {
            return "Public service";
        }

        String id = serviceId.toString();

        for (Document service : services) {

            ObjectId objectId = service.getObjectId("_id");

            if (objectId != null
                    && objectId.toString().equals(id)) {

                return safe(
                        service.getString("serviceName"),
                        "Public service");
            }
        }

        return "Public service";
    }

    // ==================== COMPLAINTS ====================

    private void updateComplaints(List<Document> complaints) {

        recentComplaintsHolder.removeAll();

        if (complaints.isEmpty()) {

            showEmpty(
                    recentComplaintsHolder,
                    "You have not submitted any complaints yet.");

            return;
        }

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(
                new BoxLayout(rows, BoxLayout.Y_AXIS));

        int limit = Math.min(4, complaints.size());

        for (int i = 0; i < limit; i++) {

            Document complaint = complaints.get(i);

            String number = safe(
                    complaint.getString("complaintNumber"),
                    "Complaint");

            String subject = safe(
                    complaint.getString("subject"),
                    "Citizen complaint");

            String status = safe(
                    complaint.getString("status"),
                    "Unknown");

            Date date = complaint.getDate("submittedAt");

            rows.add(activityRow(
                    number,
                    subject,
                    status,
                    formatDate(date),
                    statusColor(status)));

            if (i < limit - 1) {
                rows.add(new JSeparator());
            }
        }

        recentComplaintsHolder.add(rows, BorderLayout.CENTER);
        recentComplaintsHolder.revalidate();
        recentComplaintsHolder.repaint();
    }

    private JPanel activityRow(
            String reference,
            String detail,
            String status,
            String date,
            Color statusColor) {

        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(7, 2, 7, 2));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(
                new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel ref = new JLabel(reference);
        ref.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ref.setForeground(NAVY);

        JLabel desc = new JLabel(detail + "  |  " + date);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        desc.setForeground(MUTED);

        text.add(ref);
        text.add(Box.createVerticalStrut(3));
        text.add(desc);

        JLabel badge = new JLabel(status.replace('_', ' '));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        badge.setForeground(statusColor);
        badge.setBorder(new EmptyBorder(4, 6, 4, 6));

        row.add(text, BorderLayout.CENTER);
        row.add(badge, BorderLayout.EAST);

        return row;
    }

    // ==================== EMERGENCY ALERTS ====================

    private void updateAlerts(List<Document> alerts) {

        alertsHolder.removeAll();

        if (alerts.isEmpty()) {

            showEmpty(
                    alertsHolder,
                    "No active emergency alerts.");

            return;
        }

        int limit = Math.min(3, alerts.size());

        for (int i = 0; i < limit; i++) {

            Document alert = alerts.get(i);

            String title = safe(
                    alert.getString("title"),
                    "Emergency announcement");

            String severity = safe(
                    alert.getString("severity"),
                    "NOTICE");

            Date date = alert.getDate("createdAt");

            JPanel item = new JPanel();
            item.setOpaque(false);
            item.setLayout(
                    new BoxLayout(item, BoxLayout.Y_AXIS));

            item.setBorder(
                    new EmptyBorder(5, 0, 6, 0));

            JLabel alertTitle = new JLabel("!  " + title);
            alertTitle.setFont(
                    new Font("Segoe UI", Font.BOLD, 10));

            alertTitle.setForeground(
                    "CRITICAL".equalsIgnoreCase(severity)
                            || "HIGH".equalsIgnoreCase(severity)
                            ? new Color(183, 59, 59)
                            : NAVY);

            JLabel meta = new JLabel(
                    severity + "  |  " + formatDate(date));

            meta.setFont(
                    new Font("Segoe UI", Font.PLAIN, 9));

            meta.setForeground(MUTED);

            item.add(alertTitle);
            item.add(Box.createVerticalStrut(3));
            item.add(meta);

            alertsHolder.add(item);

            if (i < limit - 1) {
                alertsHolder.add(new JSeparator());
            }
        }

        alertsHolder.revalidate();
        alertsHolder.repaint();
    }

    // ==================== EMPTY STATE ====================

    private void showEmpty(JPanel parent, String message) {

        parent.removeAll();

        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        emptyPanel.setLayout(
                new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));

        JLabel symbol = new JLabel("—");
        symbol.setFont(new Font("Segoe UI", Font.BOLD, 20));
        symbol.setForeground(new Color(155, 170, 188));
        symbol.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel empty = new JLabel(
                "<html><div style='width:260px;'>"
                        + message
                        + "</div></html>");

        empty.setFont(
                new Font("Segoe UI", Font.PLAIN, 11));

        empty.setForeground(MUTED);
        empty.setAlignmentX(Component.LEFT_ALIGNMENT);

        emptyPanel.add(symbol);
        emptyPanel.add(Box.createVerticalStrut(5));
        emptyPanel.add(empty);

        parent.add(emptyPanel, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();
    }

    // ==================== STATUS COLORS ====================

    private Color statusColor(String status) {

        if (status == null) {
            return MUTED;
        }

        String value = status.toUpperCase(Locale.ROOT);

        if (value.equals("APPROVED")
                || value.equals("RESOLVED")) {
            return TEAL;
        }

        if (value.equals("REJECTED")) {
            return new Color(190, 60, 60);
        }

        if (value.equals("IN_PROGRESS")
                || value.equals("UNDER_REVIEW")) {
            return new Color(190, 125, 35);
        }

        return PRIMARY;
    }

    // ==================== HELPER METHODS ====================

    private String formatDate(Date date) {

        if (date == null) {
            return "Date unavailable";
        }

        return new SimpleDateFormat("dd MMM yyyy")
                .format(date);
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

    // ==================== NAVIGATION ====================

    // ==================== JOTY'S CITIZEN SCREENS ====================

    private void openMyApplications() {
        new MyApplicationsFrame().setVisible(true);
    }

    private void openSubmitComplaint() {
        new SubmitComplaintFrame().setVisible(true);
    }

    private void openMyComplaints() {
        new MyComplaintsFrame().setVisible(true);
    }

    private void openImportantInformation() {
        new ImportantInformationFrame().setVisible(true);
    }

    private void openEmergencyAlerts() {
        new EmergencyAlertsFrame().setVisible(true);
    }

    private void openVolunteerRegistration() {
        new VolunteerRegistrationFrame().setVisible(true);
    }

    private void openFeedbackRating() {
        new FeedbackRatingFrame().setVisible(true);
    }

    // ==================== PUBLIC SERVICES ====================

    private void openPublicServices() {

        UserRepository.AuthenticatedUser user =
                UserSession.getCurrentUser();

        if (user == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Your session has expired. Please sign in again.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        PublicServicesFrame publicServicesFrame =
                new PublicServicesFrame(user);

        publicServicesFrame.setVisible(true);

        // Keep the dashboard open behind the Public Services window.
    }

    // ==================== PROFILE ====================

    private void openProfile() {

        UserRepository.AuthenticatedUser user =
                UserSession.getCurrentUser();

        if (user == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Your session has expired. Please sign in again.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        UserProfileFrame profileFrame = new UserProfileFrame(user);
        profileFrame.setVisible(true);

        // Keep the dashboard open behind the Profile window.
    }

    private void showNavigationNotice(String pageName) {

        JOptionPane.showMessageDialog(
                this,
                pageName + " is not available from this dashboard yet.",
                "Navigation",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================== LOGOUT ====================

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

    // ==================== RESPONSIVE GRID ====================

    private class AdaptiveGridPanel extends JPanel {

        private final int wideColumns;
        private final int mediumColumns;
        private final int narrowColumns;
        private final int rowHeight;
        private final int minimumCardWidth;
        private final int gap;

        private int currentColumns;

        AdaptiveGridPanel(
                int wideColumns,
                int mediumColumns,
                int narrowColumns,
                int rowHeight,
                int minimumCardWidth,
                int gap) {

            this.wideColumns = wideColumns;
            this.mediumColumns = mediumColumns;
            this.narrowColumns = narrowColumns;
            this.rowHeight = rowHeight;
            this.minimumCardWidth = minimumCardWidth;
            this.gap = gap;

            currentColumns = wideColumns;

            setOpaque(false);
            setLayout(
                    new GridLayout(0, currentColumns, gap, gap));

            addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(ComponentEvent e) {
                    updateColumns();
                }
            });
        }

        private void updateColumns() {

            int width = getWidth();
            int columns;

            if (width >= wideColumns * minimumCardWidth
                    + (wideColumns - 1) * gap) {

                columns = wideColumns;

            } else if (width >= mediumColumns * minimumCardWidth
                    + (mediumColumns - 1) * gap) {

                columns = mediumColumns;

            } else {

                columns = narrowColumns;
            }

            if (columns != currentColumns) {

                currentColumns = columns;

                setLayout(
                        new GridLayout(0, currentColumns, gap, gap));

                revalidate();

                if (getParent() != null) {
                    getParent().revalidate();
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {

            int rows = (int) Math.ceil(
                    (double) getComponentCount()
                            / Math.max(1, currentColumns));

            return new Dimension(
                    0,
                    Math.max(
                            rowHeight,
                            rows * rowHeight
                                    + Math.max(0, rows - 1) * gap));
        }
    }

    // ==================== CUSTOM CARD ====================

    private class SoftCard extends JPanel {

        SoftCard() {

            setOpaque(false);
            setBorder(new EmptyBorder(2, 2, 5, 2));
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(30, 50, 75, 15));

            g2.fill(new RoundRectangle2D.Float(
                    1, 3, w - 3, h - 5, 16, 16));

            g2.setColor(WHITE);

            g2.fill(new RoundRectangle2D.Float(
                    1, 1, w - 3, h - 6, 16, 16));

            g2.setColor(BORDER);

            g2.draw(new RoundRectangle2D.Float(
                    1, 1, w - 3, h - 6, 16, 16));

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // ==================== DASHBOARD DATA MODEL ====================

    private static class DashboardData {

        private final List<Document> applications;
        private final List<Document> complaints;
        private final List<Document> services;
        private final List<Document> alerts;
        private final long pendingApplications;

        private DashboardData(
                List<Document> applications,
                List<Document> complaints,
                List<Document> services,
                List<Document> alerts,
                long pendingApplications) {

            this.applications = applications;
            this.complaints = complaints;
            this.services = services;
            this.alerts = alerts;
            this.pendingApplications = pendingApplications;
        }
    }
}
