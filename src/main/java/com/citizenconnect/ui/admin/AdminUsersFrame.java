
package com.citizenconnect.ui.admin;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.admin.AdminUserRepository;
import com.citizenconnect.repository.admin.AdminUserRepository.UserSummary;
import com.citizenconnect.theme.Theme;
import com.citizenconnect.theme.RoundedPanel;
import com.citizenconnect.theme.RoundedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminUsersFrame extends JFrame {

    private final AdminUserRepository repository;
    private final AdminDashboardFrame dashboardFrame;

    private JPanel mainContent;
    private JTextField searchField;
    private JTable usersTable;
    private DefaultTableModel tableModel;

    private JLabel totalUsersLabel;
    private JLabel resultLabel;

    private JButton searchButton;
    private JButton refreshButton;
    private JButton viewButton;
    private JButton statusButton;

    private List<UserSummary> currentUsers = new ArrayList<>();

    private static final Color NAVY = new Color(16, 39, 67);

    // Constructor for compatibility
    public AdminUsersFrame() {
        this(null);
    }

    // Constructor with existing Dashboard reference
    public AdminUsersFrame(AdminDashboardFrame dashboardFrame) {

        if (!UserSession.isAdmin()) {
            throw new SecurityException("Admin access required.");
        }

        this.dashboardFrame = dashboardFrame;
        this.repository = new AdminUserRepository();

        setTitle("CitizenConnect Pro | Manage Users");
        setSize(1350, 850);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(dashboardFrame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);

        root.add(createSidebar(), BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Theme.BACKGROUND);
        rightPanel.setBorder(new EmptyBorder(28, 32, 25, 32));

        rightPanel.add(createHeader(), BorderLayout.NORTH);

        mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);

        rightPanel.add(mainContent, BorderLayout.CENTER);
        root.add(rightPanel, BorderLayout.CENTER);

        setContentPane(root);

        loadUsers("");
    }

    // ================= SIDEBAR =================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();

        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 20, 25, 20));

        // BACK TO DASHBOARD BUTTON

        JButton backButton = new JButton("←  Back to Dashboard");

        backButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(NAVY);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setOpaque(true);
        backButton.setHorizontalAlignment(SwingConstants.LEFT);

        backButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        backButton.setBorder(
                new EmptyBorder(12, 5, 12, 5)
        );

        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        backButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42)
        );

        backButton.addActionListener(e -> openDashboard());

        sidebar.add(backButton);
        sidebar.add(Box.createVerticalStrut(20));

        // LOGO

        JLabel logo = new JLabel("CitizenConnect Pro");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel section = new JLabel("ADMINISTRATION");
        section.setFont(new Font("Segoe UI", Font.BOLD, 11));
        section.setForeground(new Color(195, 207, 222));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(section);
        sidebar.add(Box.createVerticalStrut(35));

        String[] items = {
            "Dashboard",
            "Manage Users",
            "Manage Services",
            "Manage Applications",
            "Manage Complaints",
            "Important Information",
            "Emergency Alerts",
            "Volunteers & Feedback"
        };

        for (String item : items) {

            JButton button = new JButton(item);

            button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            button.setForeground(Color.WHITE);

            button.setBackground(
                    item.equals("Manage Users")
                            ? NAVY.brighter()
                            : NAVY
            );

            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);

            button.setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            );

            button.setBorder(
                    new EmptyBorder(12, 14, 12, 10)
            );

            button.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 44)
            );

            button.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (item.equals("Dashboard")) {
                button.addActionListener(e -> openDashboard());
            }

            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(7));
        }

        sidebar.add(Box.createVerticalGlue());

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(75, 96, 119));

        separator.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1)
        );

        sidebar.add(separator);
        sidebar.add(Box.createVerticalStrut(18));

        JLabel role = new JLabel("Administrator");
        role.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        role.setForeground(new Color(210, 220, 232));
        role.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(role);

        return sidebar;
    }

    // ================= DASHBOARD NAVIGATION =================

    private void openDashboard() {

        if (!UserSession.isAdmin()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        // Restore original Dashboard window

        if (dashboardFrame != null && dashboardFrame.isDisplayable()) {

            dashboardFrame.setExtendedState(JFrame.NORMAL);
            dashboardFrame.setVisible(true);
            dashboardFrame.toFront();
            dashboardFrame.requestFocus();

        } else {

            // Fallback when Manage Users was opened independently

            AdminDashboardFrame newDashboard =
                    new AdminDashboardFrame();

            newDashboard.setVisible(true);
        }

        dispose();
    }

    // ================= HEADER =================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 25, 0));

        JPanel titlePanel = new JPanel();

        titlePanel.setOpaque(false);

        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Manage Users");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "View and manage registered citizen accounts"
        );

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED_TEXT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(subtitle);

        refreshButton = new RoundedButton("Refresh");

        refreshButton.addActionListener(
                e -> loadUsers(searchField.getText().trim())
        );

        header.add(titlePanel, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        return header;
    }

    // ================= MAIN CONTENT =================

    private void createMainContent() {

        mainContent.removeAll();

        JPanel body = new JPanel();

        body.setOpaque(false);

        body.setLayout(
                new BoxLayout(body, BoxLayout.Y_AXIS)
        );

        body.add(createSummaryPanel());
        body.add(Box.createVerticalStrut(20));
        body.add(createSearchPanel());
        body.add(Box.createVerticalStrut(18));
        body.add(createTablePanel());
        body.add(Box.createVerticalStrut(15));
        body.add(createActionPanel());

        JScrollPane scrollPane = new JScrollPane(body);

        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainContent.add(scrollPane, BorderLayout.CENTER);

        mainContent.revalidate();
        mainContent.repaint();
    }

    // ================= SUMMARY =================

    private JPanel createSummaryPanel() {

        RoundedPanel panel = new RoundedPanel();

        panel.setLayout(new BorderLayout());

        panel.setBorder(
                new EmptyBorder(20, 23, 20, 23)
        );

        JLabel title = new JLabel("Registered Users");

        title.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        title.setForeground(Theme.MUTED_TEXT);

        totalUsersLabel = new JLabel(
                String.valueOf(currentUsers.size())
        );

        totalUsersLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 30)
        );

        totalUsersLabel.setForeground(NAVY);

        panel.add(title, BorderLayout.NORTH);
        panel.add(totalUsersLabel, BorderLayout.CENTER);

        panel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 105)
        );

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

    // ================= SEARCH =================

    private JPanel createSearchPanel() {

        RoundedPanel panel = new RoundedPanel();

        panel.setLayout(
                new BorderLayout(12, 10)
        );

        panel.setBorder(
                new EmptyBorder(17, 18, 17, 18)
        );

        JLabel label = new JLabel("Search Users");

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        label.setForeground(NAVY);

        searchField = new JTextField();

        searchField.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        searchField.setPreferredSize(
                new Dimension(250, 38)
        );

        searchField.setToolTipText(
                "Search by name or email"
        );

        searchField.addActionListener(
                e -> performSearch()
        );

        searchButton = new RoundedButton("Search");

        searchButton.addActionListener(
                e -> performSearch()
        );

        JPanel controls = new JPanel(
                new BorderLayout(10, 0)
        );

        controls.setOpaque(false);

        controls.add(searchField, BorderLayout.CENTER);
        controls.add(searchButton, BorderLayout.EAST);

        panel.add(label, BorderLayout.NORTH);
        panel.add(controls, BorderLayout.CENTER);

        panel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 115)
        );

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

    private void performSearch() {
        loadUsers(searchField.getText().trim());
    }

    // ================= USER TABLE =================

    private JPanel createTablePanel() {

        RoundedPanel panel = new RoundedPanel();

        panel.setLayout(
                new BorderLayout(10, 14)
        );

        panel.setBorder(
                new EmptyBorder(18, 18, 18, 18)
        );

        JLabel heading = new JLabel("User List");

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 16)
        );

        heading.setForeground(NAVY);

        String[] columns = {
            "Full Name",
            "Email",
            "Phone",
            "Role",
            "Account Status",
            "Registered Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        usersTable = new JTable(tableModel);

        usersTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        usersTable.setForeground(Theme.TEXT);
        usersTable.setBackground(Color.WHITE);
        usersTable.setRowHeight(38);

        usersTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        usersTable.setSelectionBackground(Theme.LIGHT_BLUE);
        usersTable.setSelectionForeground(NAVY);
        usersTable.setShowGrid(false);

        usersTable.setIntercellSpacing(
                new Dimension(0, 0)
        );

        usersTable.setFillsViewportHeight(true);

        JTableHeader header = usersTable.getTableHeader();

        header.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        header.setBackground(Theme.LIGHT_BLUE);
        header.setForeground(NAVY);

        header.setPreferredSize(
                new Dimension(0, 40)
        );

        header.setReorderingAllowed(false);

        JScrollPane tableScroll = new JScrollPane(usersTable);

        tableScroll.setPreferredSize(
                new Dimension(850, 350)
        );

        tableScroll.setBorder(
                BorderFactory.createLineBorder(Theme.BORDER)
        );

        resultLabel = new JLabel(" ");

        resultLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        resultLabel.setForeground(Theme.MUTED_TEXT);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(resultLabel, BorderLayout.SOUTH);

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

    // ================= ACTION BUTTONS =================

    private JPanel createActionPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 5)
        );

        panel.setOpaque(false);

        viewButton = new RoundedButton("View Details");

        statusButton = new RoundedButton(
                "Activate / Deactivate"
        );

        viewButton.addActionListener(
                e -> viewSelectedUser()
        );

        statusButton.addActionListener(
                e -> changeSelectedUserStatus()
        );

        panel.add(viewButton);
        panel.add(statusButton);

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

    // ================= LOAD USERS =================

    private void loadUsers(String searchText) {

        setControlsEnabled(false);

        mainContent.removeAll();

        JLabel loading = new JLabel(
                "Loading users...",
                SwingConstants.CENTER
        );

        loading.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        loading.setForeground(Theme.MUTED_TEXT);

        mainContent.add(loading, BorderLayout.CENTER);

        mainContent.revalidate();
        mainContent.repaint();

        SwingWorker<List<UserSummary>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<UserSummary> doInBackground() {

                return repository.getAllUsers(
                        UserSession.getUserId(),
                        searchText
                );
            }

            @Override
            protected void done() {

                setControlsEnabled(true);

                try {

                    currentUsers = get();

                    createMainContent();
                    updateTable();

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showLoadError(
                            "User loading was interrupted."
                    );

                } catch (Exception ex) {

                    Throwable cause = ex.getCause();

                    String message = cause != null
                            ? cause.getMessage()
                            : ex.getMessage();

                    showLoadError(
                            message == null
                                    ? "Unexpected error occurred."
                                    : message
                    );
                }
            }
        };

        worker.execute();
    }

    // ================= UPDATE TABLE =================

    private void updateTable() {

        tableModel.setRowCount(0);

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd MMM yyyy");

        for (UserSummary user : currentUsers) {

            String date = "-";

            Date createdAt = user.createdAt();

            if (createdAt != null) {
                date = dateFormat.format(createdAt);
            }

            tableModel.addRow(new Object[]{
                safe(user.fullName()),
                safe(user.email()),
                safe(user.phone()),
                safe(user.role()),
                safe(user.accountStatus()),
                date
            });
        }

        totalUsersLabel.setText(
                String.valueOf(currentUsers.size())
        );

        resultLabel.setText(
                currentUsers.size() + " user(s) found"
        );

        if (currentUsers.isEmpty()) {
            resultLabel.setText("No users found.");
        }
    }

    // ================= VIEW DETAILS =================

    private void viewSelectedUser() {

        int selectedRow = usersTable.getSelectedRow();

        if (selectedRow < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a user first.",
                    "No User Selected",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        UserSummary user = currentUsers.get(selectedRow);

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        String createdDate = user.createdAt() == null
                ? "-"
                : dateFormat.format(user.createdAt());

        String details =
                "Full Name: " + safe(user.fullName()) + "\n\n"
                + "Email: " + safe(user.email()) + "\n\n"
                + "Phone: " + safe(user.phone()) + "\n\n"
                + "Role: " + safe(user.role()) + "\n\n"
                + "Account Status: " + safe(user.accountStatus()) + "\n\n"
                + "Registered Date: " + createdDate + "\n\n"
                + "User ID: " + user.id();

        JTextArea textArea = new JTextArea(details);

        textArea.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);

        textArea.setBorder(
                new EmptyBorder(12, 12, 12, 12)
        );

        JOptionPane.showMessageDialog(
                this,
                textArea,
                "User Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ================= CHANGE STATUS =================

    private void changeSelectedUserStatus() {

        int selectedRow = usersTable.getSelectedRow();

        if (selectedRow < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a user first.",
                    "No User Selected",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        UserSummary user = currentUsers.get(selectedRow);

        if (!"CITIZEN".equalsIgnoreCase(user.role())) {

            JOptionPane.showMessageDialog(
                    this,
                    "Only Citizen accounts can be activated or deactivated.",
                    "Action Not Allowed",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String currentStatus = user.accountStatus();

        String newStatus =
                "ACTIVE".equalsIgnoreCase(currentStatus)
                        ? "INACTIVE"
                        : "ACTIVE";

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to change this account status to "
                        + newStatus + "?",
                "Confirm Account Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        setControlsEnabled(false);

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() {

                return repository.updateCitizenStatus(
                        user.id(),
                        newStatus,
                        UserSession.getUserId()
                );
            }

            @Override
            protected void done() {

                setControlsEnabled(true);

                try {

                    boolean updated = get();

                    if (updated) {

                        JOptionPane.showMessageDialog(
                                AdminUsersFrame.this,
                                "Account status updated successfully.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                    } else {

                        JOptionPane.showMessageDialog(
                                AdminUsersFrame.this,
                                "No changes were made. The account may already have this status.",
                                "Information",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }

                    loadUsers(searchField.getText().trim());

                } catch (Exception ex) {

                    Throwable cause = ex.getCause();

                    String message = cause != null
                            ? cause.getMessage()
                            : ex.getMessage();

                    JOptionPane.showMessageDialog(
                            AdminUsersFrame.this,
                            "Unable to update account status.\n"
                                    + message,
                            "Update Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    // ================= HELPERS =================

    private String safe(String value) {

        return value == null || value.isBlank()
                ? "-"
                : value;
    }

    private void setControlsEnabled(boolean enabled) {

        if (searchButton != null) {
            searchButton.setEnabled(enabled);
        }

        if (refreshButton != null) {
            refreshButton.setEnabled(enabled);
        }

        if (viewButton != null) {
            viewButton.setEnabled(enabled);
        }

        if (statusButton != null) {
            statusButton.setEnabled(enabled);
        }
    }

    private void showLoadError(String message) {

        mainContent.removeAll();

        JLabel error = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Unable to load users.<br><br>"
                        + message
                        + "</div></html>",
                SwingConstants.CENTER
        );

        error.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        error.setForeground(NAVY);

        mainContent.add(error, BorderLayout.CENTER);

        mainContent.revalidate();
        mainContent.repaint();

        JOptionPane.showMessageDialog(
                this,
                "Unable to load users.\n" + message,
                "Manage Users Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
