
package com.citizenconnect.ui.admin;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.admin.AdminDashboardRepository;
import com.citizenconnect.repository.admin.AdminDashboardRepository.DashboardStats;
import com.citizenconnect.theme.Theme;
import com.citizenconnect.theme.RoundedPanel;
import com.citizenconnect.theme.RoundedButton;

import org.bson.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdminDashboardFrame extends JFrame {

    private final AdminDashboardRepository repository;

    private JPanel dashboardBodyPanel;
    private JButton refreshButton;

    private record DashboardData(
            DashboardStats stats,
            List<Document> applications,
            List<Document> complaints
    ) {}

    public AdminDashboardFrame() {

        if (!UserSession.isAdmin()) {
            throw new SecurityException("Admin access required.");
        }

        repository = new AdminDashboardRepository();

        setTitle("CitizenConnect Pro | Admin Dashboard");
        setSize(1350, 850);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BACKGROUND);

        mainPanel.add(createSidebar(), BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Theme.BACKGROUND);
        rightPanel.setBorder(new EmptyBorder(28, 32, 25, 32));

        rightPanel.add(createHeader(), BorderLayout.NORTH);

        dashboardBodyPanel = new JPanel(new BorderLayout());
        dashboardBodyPanel.setOpaque(false);

        rightPanel.add(dashboardBodyPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);

        loadDashboardData();
    }

    // ================= SIDEBAR =================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.NAVY);
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 25, 20));

        JLabel logo = new JLabel("CitizenConnect Pro");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionLabel = new JLabel("ADMINISTRATION");
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sectionLabel.setForeground(new Color(195, 207, 222));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(sectionLabel);
        sidebar.add(Box.createVerticalStrut(35));

        String[] menuItems = {
            "Dashboard",
            "Manage Users",
            "Manage Services",
            "Manage Applications",
            "Manage Complaints",
            "Important Information",
            "Emergency Alerts",
            "Volunteers & Feedback"
        };

        for (String item : menuItems) {

            JButton button = new JButton(item);

            button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            button.setForeground(Color.WHITE);

            button.setBackground(
                    item.equals("Dashboard") ? Theme.PRIMARY : Theme.NAVY
            );

            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            );

            button.setBorder(new EmptyBorder(12, 14, 12, 10));
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (item.equals("Manage Users")) {
                button.addActionListener(e -> openManageUsers());
            }

            if (item.equals("Manage Services")) {
                button.addActionListener(e -> openManageServices());
            }

            if (item.equals("Manage Applications")) {
                button.addActionListener(e -> openManageApplications());
            }

            if (item.equals("Manage Complaints")) {
                button.addActionListener(e -> openManageComplaints());
            }

            if (item.equals("Important Information")) {
                button.addActionListener(e -> openImportantInformation());
            }

            if (item.equals("Emergency Alerts")) {
                button.addActionListener(e -> openEmergencyAlerts());
            }

            if (item.equals("Volunteers & Feedback")) {
                button.addActionListener(e -> openVolunteersFeedback());
            }

            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(7));
        }

        // Push Administrator and Logout to the bottom.
        sidebar.add(Box.createVerticalGlue());

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(75, 96, 119));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        sidebar.add(separator);
        sidebar.add(Box.createVerticalStrut(18));

        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(210, 220, 232));
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(roleLabel);
        sidebar.add(Box.createVerticalStrut(12));

        // ================= LOGOUT BUTTON =================

        JButton logoutButton = new JButton("Sign Out");

        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(38, 65, 96));
        logoutButton.setHorizontalAlignment(SwingConstants.LEFT);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setOpaque(true);
        logoutButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        logoutButton.setBorder(new EmptyBorder(12, 14, 12, 10));
        logoutButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 44)
        );
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoutButton.addActionListener(e -> logout());

        sidebar.add(logoutButton);

        return sidebar;
    }

    // ================= LOGOUT =================

    private void logout() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to sign out?",
                "Confirm Sign Out",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // Clear the current admin session.
        UserSession.clearSession();

        // Close the Admin Dashboard.
        dispose();

        // Open Login Frame.
        try {

            Class<?> loginClass =
                    Class.forName("com.citizenconnect.ui.LoginFrame");

            JFrame loginFrame = (JFrame)
                    loginClass.getDeclaredConstructor().newInstance();

            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    null,
                    "Unable to open Login page:\n" + ex.getMessage(),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= MANAGE USERS NAVIGATION =================

    private void openManageUsers() {

        if (!UserSession.isAdmin()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        AdminUsersFrame usersFrame = new AdminUsersFrame(this);
        usersFrame.setVisible(true);

        this.setVisible(false);
    }

    // ================= MANAGE SERVICES NAVIGATION =================

    private void openManageServices() {

        if (!UserSession.isAdmin()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        AdminServicesFrame servicesFrame = new AdminServicesFrame(this);
        servicesFrame.setVisible(true);

        this.setVisible(false);
    }

    // ================= MANAGE APPLICATIONS NAVIGATION =================

    private void openManageApplications() {

        if (!UserSession.isAdmin()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        AdminApplicationsFrame applicationsFrame =
                new AdminApplicationsFrame(this);

        applicationsFrame.setVisible(true);

        this.setVisible(false);
    }

    // ================= MANAGE COMPLAINTS NAVIGATION =================

    private void openManageComplaints() {

        if (!UserSession.isAdmin()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        AdminComplaintsFrame complaintsFrame =
                new AdminComplaintsFrame(this);

        complaintsFrame.setVisible(true);

        this.setVisible(false);
    }

    // ================= IMPORTANT INFORMATION NAVIGATION =================

    private void openImportantInformation() {

        if (!UserSession.isAdmin()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        AdminImportantInformationFrame informationFrame =
                new AdminImportantInformationFrame(this);

        informationFrame.setVisible(true);

        this.setVisible(false);
    }

    // ================= EMERGENCY ALERTS NAVIGATION =================

    private void openEmergencyAlerts() {

        if (!UserSession.isAdmin()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        AdminEmergencyAlertsFrame alertsFrame =
                new AdminEmergencyAlertsFrame(this);

        alertsFrame.setVisible(true);

        this.setVisible(false);
    }

    // ================= VOLUNTEERS & FEEDBACK NAVIGATION =================

    private void openVolunteersFeedback() {

        if (!UserSession.isAdmin()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        AdminVolunteersFeedbackFrame frame =
                new AdminVolunteersFeedbackFrame(this);

        frame.setVisible(true);

        this.setVisible(false);
    }

    // ================= HEADER =================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(Theme.NAVY);

        String name = UserSession.getUserName();

        if (name == null || name.isBlank()) {
            name = "Administrator";
        }

        JLabel welcome = new JLabel("Welcome back, " + name);
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcome.setForeground(Theme.MUTED_TEXT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(welcome);

        refreshButton = new RoundedButton("Refresh Data");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        refreshButton.addActionListener(e -> loadDashboardData());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        return header;
    }

    // ================= LOAD DASHBOARD DATA =================

    private void loadDashboardData() {

        if (refreshButton != null) {
            refreshButton.setEnabled(false);
        }

        dashboardBodyPanel.removeAll();

        JLabel loadingLabel = new JLabel(
                "Loading dashboard data...",
                SwingConstants.CENTER
        );

        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadingLabel.setForeground(Theme.MUTED_TEXT);

        dashboardBodyPanel.add(loadingLabel, BorderLayout.CENTER);
        dashboardBodyPanel.revalidate();
        dashboardBodyPanel.repaint();

        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {

            @Override
            protected DashboardData doInBackground() {

                String adminId = UserSession.getUserId();

                DashboardStats stats =
                        repository.getDashboardStats(adminId);

                List<Document> applications =
                        repository.getRecentApplications(adminId);

                List<Document> complaints =
                        repository.getRecentComplaints(adminId);

                return new DashboardData(
                        stats,
                        applications,
                        complaints
                );
            }

            @Override
            protected void done() {

                if (refreshButton != null) {
                    refreshButton.setEnabled(true);
                }

                try {

                    DashboardData data = get();
                    showDashboard(data);

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                    showError("Dashboard loading was interrupted.");

                } catch (Exception ex) {

                    Throwable cause = ex.getCause();

                    String message = cause != null
                            ? cause.getMessage()
                            : ex.getMessage();

                    showError(
                            message == null
                                    ? "An unexpected error occurred."
                                    : message
                    );
                }
            }
        };

        worker.execute();
    }

    // ================= DISPLAY DASHBOARD =================

    private void showDashboard(DashboardData data) {

        dashboardBodyPanel.removeAll();

        JPanel dashboardContent = new JPanel();
        dashboardContent.setOpaque(false);

        dashboardContent.setLayout(
                new BoxLayout(dashboardContent, BoxLayout.Y_AXIS)
        );

        DashboardStats stats = data.stats();

        JPanel statisticsPanel = new JPanel(
                new GridLayout(0, 3, 16, 16)
        );

        statisticsPanel.setOpaque(false);

        statisticsPanel.add(
                createStatCard("Total Citizens", stats.totalCitizens())
        );

        statisticsPanel.add(
                createStatCard("Total Services", stats.totalServices())
        );

        statisticsPanel.add(
                createStatCard("Total Applications", stats.totalApplications())
        );

        statisticsPanel.add(
                createStatCard("Pending Applications", stats.pendingApplications())
        );

        statisticsPanel.add(
                createStatCard("Total Complaints", stats.totalComplaints())
        );

        statisticsPanel.add(
                createStatCard("Unresolved Complaints", stats.unresolvedComplaints())
        );

        statisticsPanel.add(
                createStatCard("Volunteer Registrations", stats.totalVolunteers())
        );

        JLabel recentTitle = new JLabel("Recent Activities");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        recentTitle.setForeground(Theme.NAVY);
        recentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel activitiesPanel = new JPanel(
                new GridLayout(1, 2, 16, 0)
        );

        activitiesPanel.setOpaque(false);

        activitiesPanel.add(
                createActivityPanel(
                        "Recent Applications",
                        data.applications(),
                        true
                )
        );

        activitiesPanel.add(
                createActivityPanel(
                        "Recent Complaints",
                        data.complaints(),
                        false
                )
        );

        statisticsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        activitiesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        dashboardContent.add(statisticsPanel);
        dashboardContent.add(Box.createVerticalStrut(28));
        dashboardContent.add(recentTitle);
        dashboardContent.add(Box.createVerticalStrut(14));
        dashboardContent.add(activitiesPanel);
        dashboardContent.add(Box.createVerticalStrut(15));

        JScrollPane scrollPane = new JScrollPane(dashboardContent);

        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        dashboardBodyPanel.add(scrollPane, BorderLayout.CENTER);

        dashboardBodyPanel.revalidate();
        dashboardBodyPanel.repaint();
    }

    // ================= STATISTICS CARD =================

    private JPanel createStatCard(String title, long value) {

        RoundedPanel card = new RoundedPanel();

        card.setLayout(new BorderLayout(8, 15));
        card.setBorder(new EmptyBorder(20, 21, 20, 21));
        card.setPreferredSize(new Dimension(220, 115));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(Theme.MUTED_TEXT);

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(Theme.NAVY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // ================= ACTIVITY TABLE =================

    private JPanel createActivityPanel(
            String title,
            List<Document> records,
            boolean isApplication
    ) {

        RoundedPanel panel = new RoundedPanel();

        panel.setLayout(new BorderLayout(10, 14));
        panel.setBorder(new EmptyBorder(17, 17, 17, 17));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        heading.setForeground(Theme.NAVY);

        String[] columns = {
            "Reference",
            "Name / Subject",
            "Status",
            "Date"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd MMM yyyy");

        for (Document document : records) {

            String reference;
            String nameOrSubject;
            String status;

            if (isApplication) {

                reference = getText(document, "applicationNumber");
                nameOrSubject = getText(document, "applicantName");
                status = getText(document, "applicationStatus");

            } else {

                reference = getText(document, "complaintNumber");
                nameOrSubject = getText(document, "subject");
                status = getText(document, "status");
            }

            Object dateValue = document.get("submittedAt");

            String date = "-";

            if (dateValue instanceof Date) {
                date = dateFormat.format((Date) dateValue);
            } else if (dateValue != null) {
                date = dateValue.toString();
            }

            model.addRow(new Object[]{
                reference,
                nameOrSubject,
                status,
                date
            });
        }

        if (records.isEmpty()) {
            model.addRow(new Object[]{
                "-",
                "No records available",
                "-",
                "-"
            });
        }

        JTable table = new JTable(model);

        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setForeground(Theme.TEXT);
        table.setBackground(Color.WHITE);
        table.setRowHeight(34);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(Theme.LIGHT_BLUE);
        table.setSelectionForeground(Theme.NAVY);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader tableHeader = table.getTableHeader();

        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tableHeader.setBackground(Theme.LIGHT_BLUE);
        tableHeader.setForeground(Theme.NAVY);
        tableHeader.setPreferredSize(new Dimension(0, 36));
        tableHeader.setReorderingAllowed(false);

        JScrollPane tableScrollPane = new JScrollPane(table);

        tableScrollPane.setPreferredSize(new Dimension(480, 235));
        tableScrollPane.setBorder(
                BorderFactory.createLineBorder(Theme.BORDER)
        );

        panel.add(heading, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ================= DOCUMENT FIELD HELPER =================

    private String getText(Document document, String field) {

        Object value = document.get(field);

        if (value == null || value.toString().isBlank()) {
            return "-";
        }

        return value.toString();
    }

    // ================= ERROR DISPLAY =================

    private void showError(String message) {

        dashboardBodyPanel.removeAll();

        JPanel errorPanel = new JPanel(new GridBagLayout());
        errorPanel.setOpaque(false);

        JLabel errorLabel = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Unable to load dashboard data.<br><br>"
                        + message
                        + "</div></html>"
        );

        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        errorLabel.setForeground(Theme.NAVY);

        errorPanel.add(errorLabel);

        dashboardBodyPanel.add(errorPanel, BorderLayout.CENTER);

        dashboardBodyPanel.revalidate();
        dashboardBodyPanel.repaint();

        JOptionPane.showMessageDialog(
                this,
                "Unable to load dashboard data.\n" + message,
                "Dashboard Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
