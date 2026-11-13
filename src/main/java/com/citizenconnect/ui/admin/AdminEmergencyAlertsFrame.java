package com.citizenconnect.ui.admin;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.EmergencyAlertRepository;
import com.citizenconnect.theme.Theme;
import org.bson.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class AdminEmergencyAlertsFrame extends JFrame {

    private final EmergencyAlertRepository repository =
            new EmergencyAlertRepository();

    private final AdminDashboardFrame dashboardFrame;

    private JTable alertTable;
    private DefaultTableModel tableModel;

    private JTextField searchField;
    private JComboBox<String> severityFilter;
    private JComboBox<String> statusFilter;

    private JLabel totalValue;
    private JLabel activeValue;
    private JLabel inactiveValue;
    private JLabel criticalValue;

    private JButton viewButton;
    private JButton editButton;
    private JButton toggleButton;
    private JButton deleteButton;
    private JButton addButton;
    private JButton refreshButton;

    private List<Document> allAlerts = new ArrayList<>();

    private boolean loading = false;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    public AdminEmergencyAlertsFrame() {
        this(null);
    }

    public AdminEmergencyAlertsFrame(AdminDashboardFrame dashboardFrame) {

        if (!UserSession.isAdmin()) {
            throw new SecurityException("Admin access required.");
        }

        this.dashboardFrame = dashboardFrame;

        setTitle("CitizenConnect Pro | Manage Emergency Alerts");
        setSize(1350, 820);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(dashboardFrame);
        setLayout(new BorderLayout());

        getContentPane().setBackground(Theme.BACKGROUND);

        add(createSidebar(), BorderLayout.WEST);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Theme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(25, 28, 25, 28));

        mainPanel.add(createHeader(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 18));
        centerPanel.setOpaque(false);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        summaryPanel.setOpaque(false);

        totalValue = new JLabel("0");
        activeValue = new JLabel("0");
        inactiveValue = new JLabel("0");
        criticalValue = new JLabel("0");

        summaryPanel.add(createSummaryCard("Total Alerts", totalValue));
        summaryPanel.add(createSummaryCard("Active Alerts", activeValue));
        summaryPanel.add(createSummaryCard("Inactive Alerts", inactiveValue));
        summaryPanel.add(createSummaryCard("Critical Alerts", criticalValue));

        centerPanel.add(summaryPanel, BorderLayout.NORTH);
        centerPanel.add(createTablePanel(), BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        configureActions();

        loadAlerts();
    }

    // ================= SIDEBAR =================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBackground(Theme.NAVY);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 15, 20, 15));

        JLabel logo = new JLabel("CitizenConnect");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 21));

        JLabel subtitle = new JLabel("ADMIN PANEL");
        subtitle.setForeground(new Color(205, 216, 230));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setBorder(new EmptyBorder(5, 2, 25, 0));

        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(subtitle);

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

            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            button.setPreferredSize(new Dimension(205, 42));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            button.setForeground(Color.WHITE);

            button.setBackground(
                    item.equals("Emergency Alerts")
                            ? new Color(38, 65, 96)
                            : Theme.NAVY
            );

            button.setBorder(new EmptyBorder(0, 13, 0, 8));
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (!item.equals("Emergency Alerts")) {
                button.addActionListener(e -> navigateToPage(item));
            }

            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(7));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = new JButton("Logout");

        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(38, 65, 96));
        logoutButton.setBorder(new EmptyBorder(0, 13, 0, 8));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        logoutButton.addActionListener(e -> logout());

        sidebar.add(logoutButton);

        return sidebar;
    }

    private void logout() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        UserSession.clearSession();
        dispose();

        try {
            Class<?> loginClass =
                    Class.forName("com.citizenconnect.ui.LoginFrame");

            JFrame loginFrame = (JFrame)
                    loginClass.getDeclaredConstructor().newInstance();

            loginFrame.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Unable to open login page: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= HEADER =================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Manage Emergency Alerts");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Theme.TEXT);

        JLabel description = new JLabel(
                "Create, update and manage public emergency notifications."
        );

        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setForeground(Theme.MUTED_TEXT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(description);

        JButton backButton = createButton("Back to Dashboard", false);
        backButton.addActionListener(e -> openDashboard());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        return header;
    }

    // ================= SUMMARY CARDS =================

    private JPanel createSummaryCard(String title, JLabel valueLabel) {

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(17, 18, 17, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(Theme.MUTED_TEXT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 27));
        valueLabel.setForeground(Theme.NAVY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // ================= TABLE PANEL =================

    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(18, 18, 18, 18)
        ));

        // Search and filters

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel filterPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 9, 0)
        );
        filterPanel.setOpaque(false);

        searchField = new JTextField(19);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(210, 36));

        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(5, 9, 5, 9)
        ));

        severityFilter = new JComboBox<>(
                new String[]{
                        "All Severities",
                        "LOW",
                        "MEDIUM",
                        "HIGH",
                        "CRITICAL"
                }
        );

        statusFilter = new JComboBox<>(
                new String[]{
                        "All Statuses",
                        "ACTIVE",
                        "INACTIVE"
                }
        );

        styleCombo(severityFilter);
        styleCombo(statusFilter);

        filterPanel.add(searchField);
        filterPanel.add(severityFilter);
        filterPanel.add(statusFilter);

        JPanel topActions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 7, 0)
        );
        topActions.setOpaque(false);

        addButton = createButton("+ Add Alert", true);
        refreshButton = createButton("Refresh", false);

        addButton.addActionListener(e -> showAlertForm(null));
        refreshButton.addActionListener(e -> loadAlerts());

        topActions.add(addButton);
        topActions.add(refreshButton);

        topPanel.add(filterPanel, BorderLayout.WEST);
        topPanel.add(topActions, BorderLayout.EAST);

        // Table

        String[] columns = {
                "Alert Title",
                "Alert Type",
                "Location",
                "Severity",
                "Status",
                "Created Date",
                "Alert ID"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        alertTable = new JTable(tableModel);

        alertTable.setRowHeight(34);
        alertTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        alertTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        alertTable.setGridColor(Theme.BORDER);
        alertTable.setShowVerticalLines(false);
        alertTable.setFillsViewportHeight(true);

        alertTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        alertTable.getTableHeader().setBackground(
                new Color(244, 247, 251)
        );

        alertTable.getTableHeader().setForeground(Theme.TEXT);
        alertTable.getTableHeader().setPreferredSize(
                new Dimension(0, 38)
        );

        // Hide Alert ID column from the user.
        alertTable.removeColumn(
                alertTable.getColumnModel().getColumn(6)
        );

        JScrollPane scrollPane = new JScrollPane(alertTable);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(Theme.BORDER)
        );

        // Bottom action buttons

        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 7, 0)
        );
        bottomPanel.setOpaque(false);

        viewButton = createButton("View Details", false);
        editButton = createButton("Edit Alert", false);
        toggleButton = createButton("Activate / Deactivate", false);
        deleteButton = createButton("Delete Alert", false);

        viewButton.setEnabled(false);
        editButton.setEnabled(false);
        toggleButton.setEnabled(false);
        deleteButton.setEnabled(false);

        bottomPanel.add(viewButton);
        bottomPanel.add(editButton);
        bottomPanel.add(toggleButton);
        bottomPanel.add(deleteButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }
           
    private void styleCombo(JComboBox<String> combo) {

        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setPreferredSize(new Dimension(145, 36));
        combo.setBackground(Color.WHITE);
    }

    private JButton createButton(String text, boolean primary) {

        JButton button = new JButton(text);

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));

        button.setForeground(
                primary ? Color.WHITE : Theme.NAVY
        );

        button.setBackground(
                primary ? Theme.NAVY : Color.WHITE
        );

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        primary ? Theme.NAVY : Theme.BORDER
                ),
                new EmptyBorder(8, 13, 8, 13)
        ));

        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    // ================= ACTION LISTENERS =================

    private void configureActions() {

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        filterAlerts();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        filterAlerts();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        filterAlerts();
                    }
                }
        );

        severityFilter.addActionListener(e -> filterAlerts());
        statusFilter.addActionListener(e -> filterAlerts());

        alertTable.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                boolean selected = alertTable.getSelectedRow() >= 0;

                viewButton.setEnabled(selected && !loading);
                editButton.setEnabled(selected && !loading);
                toggleButton.setEnabled(selected && !loading);
                deleteButton.setEnabled(selected && !loading);
            }
        });

        viewButton.addActionListener(e -> viewSelectedAlert());
        editButton.addActionListener(e -> editSelectedAlert());
        toggleButton.addActionListener(e -> toggleSelectedAlert());
        deleteButton.addActionListener(e -> deleteSelectedAlert());
    }

    // ================= LOAD ALERTS =================

    private void loadAlerts() {

        if (loading) {
            return;
        }

        setLoading(true);

        new SwingWorker<List<Document>, Void>() {

            @Override
            protected List<Document> doInBackground() {

                return repository.getAllAlerts(
                        UserSession.getUserId()
                );
            }

            @Override
            protected void done() {

                try {

                    allAlerts = get();

                    updateSummary();
                    filterAlerts();

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                    showError("Loading alerts was interrupted.");

                } catch (ExecutionException ex) {

                    showError(getErrorMessage(ex));

                } finally {

                    setLoading(false);
                }
            }

        }.execute();
    }

    // ================= SUMMARY UPDATE =================

    private void updateSummary() {

        int total = allAlerts.size();
        int active = 0;
        int inactive = 0;
        int critical = 0;

        for (Document alert : allAlerts) {

            String status = getString(alert, "status")
                    .toUpperCase(Locale.ROOT);

            String severity = getString(alert, "severity")
                    .toUpperCase(Locale.ROOT);

            if ("ACTIVE".equals(status)) {
                active++;
            }

            if ("INACTIVE".equals(status)) {
                inactive++;
            }

            if ("CRITICAL".equals(severity)) {
                critical++;
            }
        }

        totalValue.setText(String.valueOf(total));
        activeValue.setText(String.valueOf(active));
        inactiveValue.setText(String.valueOf(inactive));
        criticalValue.setText(String.valueOf(critical));
    }

    // ================= SEARCH AND FILTER =================

    private void filterAlerts() {

        if (tableModel == null) {
            return;
        }

        tableModel.setRowCount(0);
                String search = searchField.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        String selectedSeverity =
                (String) severityFilter.getSelectedItem();

        String selectedStatus =
                (String) statusFilter.getSelectedItem();

        for (Document alert : allAlerts) {

            String title = getString(alert, "title");
            String location = getString(alert, "location");

            String severity = getString(alert, "severity")
                    .toUpperCase(Locale.ROOT);

            String status = getString(alert, "status")
                    .toUpperCase(Locale.ROOT);

            boolean matchesSearch =
                    title.toLowerCase(Locale.ROOT).contains(search)
                            || location.toLowerCase(Locale.ROOT).contains(search);

            boolean matchesSeverity =
                    selectedSeverity == null
                            || selectedSeverity.equals("All Severities")
                            || selectedSeverity.equalsIgnoreCase(severity);

            boolean matchesStatus =
                    selectedStatus == null
                            || selectedStatus.equals("All Statuses")
                            || selectedStatus.equalsIgnoreCase(status);

            if (matchesSearch && matchesSeverity && matchesStatus) {

                Object id = alert.get("_id");

                tableModel.addRow(new Object[]{
                        title,
                        getString(alert, "alertType"),
                        location.isBlank() ? "-" : location,
                        severity,
                        status,
                        formatDate(alert.get("createdAt")),
                        id == null ? "" : id.toString()
                });
            }
        }
    }

    // ================= SELECTED ALERT =================

    private Document getSelectedAlert() {

        int selectedRow = alertTable.getSelectedRow();

        if (selectedRow < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an alert first.",
                    "No Alert Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        int modelRow = alertTable.convertRowIndexToModel(selectedRow);

        String id = String.valueOf(
                tableModel.getValueAt(modelRow, 6)
        );

        for (Document alert : allAlerts) {

            Object alertId = alert.get("_id");

            if (alertId != null && alertId.toString().equals(id)) {
                return alert;
            }
        }

        return null;
    }

    // ================= VIEW DETAILS =================

    private void viewSelectedAlert() {

        Document selected = getSelectedAlert();

        if (selected == null) {
            return;
        }

        String details =
                "Alert Title: " + getString(selected, "title")
                        + "\nAlert Type: " + getString(selected, "alertType")
                        + "\nLocation: " + getString(selected, "location")
                        + "\nSeverity: " + getString(selected, "severity")
                        + "\nStatus: " + getString(selected, "status")
                        + "\nCreated Date: " + formatDate(selected.get("createdAt"))
                        + "\nUpdated Date: " + formatDate(selected.get("updatedAt"))
                        + "\n\nDescription:\n"
                        + getString(selected, "description");

        JTextArea area = new JTextArea(details, 17, 48);

        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "Emergency Alert Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ================= ADD / EDIT ALERT =================

    private void editSelectedAlert() {

        Document selected = getSelectedAlert();

        if (selected != null) {
            showAlertForm(selected);
        }
    }

    private void showAlertForm(Document existingAlert) {

        boolean editing = existingAlert != null;

        JTextField titleField = new JTextField(
                editing ? getString(existingAlert, "title") : "", 25
        );

        JTextField typeField = new JTextField(
                editing ? getString(existingAlert, "alertType") : "", 25
        );

        JTextField locationField = new JTextField(
                editing ? getString(existingAlert, "location") : "", 25
        );

        JTextArea descriptionArea = new JTextArea(
                editing ? getString(existingAlert, "description") : "",
                6,
                25
        );

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JComboBox<String> severityCombo = new JComboBox<>(
                new String[]{"LOW", "MEDIUM", "HIGH", "CRITICAL"}
        );

        if (editing) {

            severityCombo.setSelectedItem(
                    getString(existingAlert, "severity")
                            .toUpperCase(Locale.ROOT)
            );
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(form, gbc, 0, "Alert Title:", titleField);
        addFormRow(form, gbc, 1, "Alert Type:", typeField);
        addFormRow(form, gbc, 2, "Location:", locationField);
        addFormRow(form, gbc, 3, "Severity:", severityCombo);

        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(280, 110));

        addFormRow(form, gbc, 4, "Description:", descriptionScroll);

        while (true) {

            int result = JOptionPane.showConfirmDialog(
                    this,
                    form,
                    editing ? "Edit Emergency Alert" : "Create Emergency Alert",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String title = titleField.getText().trim();
            String type = typeField.getText().trim();
            String location = locationField.getText().trim();
            String severity = (String) severityCombo.getSelectedItem();
            String description = descriptionArea.getText().trim();

            if (title.isEmpty() || type.isEmpty() || description.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Title, Alert Type and Description are required.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );

                continue;
            }

            String alertId = editing
                    ? existingAlert.get("_id").toString()
                    : null;

            final String finalTitle = title;
            final String finalType = type;
            final String finalLocation = location;
            final String finalSeverity = severity;
            final String finalDescription = description;

            setLoading(true);

            new SwingWorker<Boolean, Void>() {

                @Override
                protected Boolean doInBackground() {

                    if (editing) {

                        return repository.updateAlert(
                                alertId,
                                finalTitle,
                                finalType,
                                finalDescription,
                                finalLocation,
                                finalSeverity,
                                UserSession.getUserId()
                        );
                    }

                    repository.createAlert(
                            finalTitle,
                            finalType,
                            finalDescription,
                            finalLocation,
                            finalSeverity,
                            UserSession.getUserId()
                    );

                    return true;
                }

                @Override
                protected void done() {

                    try {

                        if (get()) {

                            JOptionPane.showMessageDialog(
                                    AdminEmergencyAlertsFrame.this,
                                    editing
                                            ? "Emergency alert updated successfully."
                                            : "Emergency alert created successfully.",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                            );

                            loadAlerts();

                        } else {

                            setLoading(false);

                            showError(
                                    "No alert was updated. Please try again."
                            );
                        }

                    } catch (InterruptedException ex) {

                        Thread.currentThread().interrupt();
                        setLoading(false);
                        showError("The operation was interrupted.");

                    } catch (ExecutionException ex) {

                        setLoading(false);
                        showError(getErrorMessage(ex));
                    }
                }

            }.execute();

            return;
        }
    }
        private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            Component component
    ) {

        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(component, gbc);
    }

    // ================= ACTIVATE / DEACTIVATE =================

    private void toggleSelectedAlert() {

        Document selected = getSelectedAlert();

        if (selected == null) {
            return;
        }

        String currentStatus = getString(selected, "status")
                .toUpperCase(Locale.ROOT);

        String newStatus = currentStatus.equals("ACTIVE")
                ? "INACTIVE"
                : "ACTIVE";

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Change this alert status to " + newStatus + "?",
                "Confirm Status Change",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String id = selected.get("_id").toString();

        performBooleanOperation(
                () -> repository.updateAlertStatus(
                        id,
                        newStatus,
                        UserSession.getUserId()
                ),
                "Alert status updated successfully."
        );
    }

    // ================= DELETE ALERT =================

    private void deleteSelectedAlert() {

        Document selected = getSelectedAlert();

        if (selected == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete this alert?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String id = selected.get("_id").toString();

        performBooleanOperation(
                () -> repository.deleteAlert(
                        id,
                        UserSession.getUserId()
                ),
                "Alert deleted successfully."
        );
    }

    // ================= DATABASE OPERATION HELPER =================

    private void performBooleanOperation(
            BooleanOperation operation,
            String successMessage
    ) {

        setLoading(true);

        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() {
                return operation.run();
            }

            @Override
            protected void done() {

                try {

                    if (get()) {

                        JOptionPane.showMessageDialog(
                                AdminEmergencyAlertsFrame.this,
                                successMessage,
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        loadAlerts();

                    } else {

                        setLoading(false);
                        showError("The operation could not be completed.");
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                    setLoading(false);
                    showError("The operation was interrupted.");

                } catch (ExecutionException ex) {

                    setLoading(false);
                    showError(getErrorMessage(ex));
                }
            }

        }.execute();
    }

    // ================= LOADING STATE =================

    private void setLoading(boolean value) {

        loading = value;

        if (viewButton == null) {
            return;
        }

        boolean selected = alertTable.getSelectedRow() >= 0;

        viewButton.setEnabled(!value && selected);
        editButton.setEnabled(!value && selected);
        toggleButton.setEnabled(!value && selected);
        deleteButton.setEnabled(!value && selected);

        addButton.setEnabled(!value);
        refreshButton.setEnabled(!value);
        searchField.setEnabled(!value);
        severityFilter.setEnabled(!value);
        statusFilter.setEnabled(!value);
    }

    // ================= HELPER METHODS =================

    private String getString(Document document, String key) {

        Object value = document.get(key);

        return value == null ? "" : value.toString();
    }

    private String formatDate(Object value) {

        if (value instanceof Date date) {
            return dateFormat.format(date);
        }

        return "-";
    }

    private String getErrorMessage(ExecutionException ex) {

        Throwable cause = ex.getCause();

        if (cause == null) {
            return ex.getMessage();
        }

        return cause.getMessage() == null
                ? cause.getClass().getSimpleName()
                : cause.getMessage();
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Operation Failed",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // ================= NAVIGATION BETWEEN ADMIN PAGES =================

    private void navigateToPage(String page) {

        if (!UserSession.isAdmin()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if ("Dashboard".equals(page)) {
            openDashboard();
            return;
        }

        JFrame targetFrame;

        switch (page) {
            case "Manage Users":
                targetFrame = new AdminUsersFrame(dashboardFrame);
                break;

            case "Manage Services":
                targetFrame = new AdminServicesFrame(dashboardFrame);
                break;

            case "Manage Applications":
                targetFrame = new AdminApplicationsFrame(dashboardFrame);
                break;

            case "Manage Complaints":
                targetFrame = new AdminComplaintsFrame(dashboardFrame);
                break;

            case "Important Information":
                targetFrame = new AdminImportantInformationFrame(dashboardFrame);
                break;

            case "Volunteers & Feedback":
                targetFrame = new AdminVolunteersFeedbackFrame(dashboardFrame);
                break;

            default:
                return;
        }

        targetFrame.setVisible(true);
        dispose();
    }

    // ================= BACK TO DASHBOARD =================

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

        if (dashboardFrame != null && dashboardFrame.isDisplayable()) {

            dashboardFrame.setVisible(true);
            dashboardFrame.setState(JFrame.NORMAL);

        } else {

            new AdminDashboardFrame().setVisible(true);
        }

        dispose();
    }

    @FunctionalInterface
    private interface BooleanOperation {
        boolean run();
    }
}