package com.citizenconnect.ui.admin;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.ImportantInformationRepository;
import com.citizenconnect.theme.Theme;

import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class AdminImportantInformationFrame extends JFrame {

    private final ImportantInformationRepository repository =
            new ImportantInformationRepository();

    private final AdminDashboardFrame dashboardFrame;

    private final DefaultTableModel tableModel;
    private final JTable informationTable;

    private final JTextField searchField;
    private final JComboBox<String> statusFilter;

    private final JLabel totalValue = new JLabel("0");
    private final JLabel publishedValue = new JLabel("0");
    private final JLabel unpublishedValue = new JLabel("0");
    private final JLabel messageLabel = new JLabel(" ");

    private final List<Document> allInformation = new ArrayList<>();

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy");

    public AdminImportantInformationFrame() {
        this(null);
    }

    public AdminImportantInformationFrame(
            AdminDashboardFrame dashboardFrame) {

        if (!UserSession.isAdmin()) {
            throw new SecurityException("Admin access required.");
        }

        this.dashboardFrame = dashboardFrame;

        setTitle("CitizenConnect Pro | Manage Important Information");
        setSize(1400, 850);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(dashboardFrame);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        tableModel = new DefaultTableModel(
                new Object[]{
                        "Title",
                        "Category",
                        "Status",
                        "Published Date",
                        "Information ID"
                }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        informationTable = new JTable(tableModel);

        searchField = new JTextField(20);

        statusFilter = new JComboBox<>(new String[]{
                "All Status",
                "PUBLISHED",
                "UNPUBLISHED"
        });

        buildSidebar();
        buildMainContent();

        loadInformation();
    }

    // =====================================================
    // SIDEBAR
    // =====================================================

    private void buildSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.NAVY);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 15, 20, 15));

        JLabel logo = new JLabel("CitizenConnect Pro");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("ADMIN PANEL");
        subtitle.setForeground(new Color(195, 207, 222));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setBorder(new EmptyBorder(8, 2, 30, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(subtitle);

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
                    item.equals("Important Information")
                            ? new Color(38, 65, 96)
                            : Theme.NAVY
            );

            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(12, 12, 12, 8));
            button.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 45)
            );
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setCursor(Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR
            ));

            // Navigation for all other admin pages.
            if (!item.equals("Important Information")) {
                button.addActionListener(e -> navigateToPage(item));
            }

            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(5));
        }

        add(sidebar, BorderLayout.WEST);
    }

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    private void buildMainContent() {

        JPanel mainPanel = new JPanel(new BorderLayout(0, 18));
        mainPanel.setBackground(Theme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(
                titlePanel, BoxLayout.Y_AXIS
        ));

        JLabel heading = new JLabel("Manage Important Information");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 25));
        heading.setForeground(Theme.TEXT);

        JLabel description = new JLabel(
                "Publish and manage important public information."
        );
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setForeground(Theme.MUTED_TEXT);

        titlePanel.add(heading);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(description);

        JPanel headerButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        headerButtons.setOpaque(false);

        JButton backButton = createButton(
                "Back to Dashboard", false
        );
        JButton refreshButton = createButton("Refresh", false);

        backButton.addActionListener(e -> openDashboard());
        refreshButton.addActionListener(e -> loadInformation());

        headerButtons.add(backButton);
        headerButtons.add(refreshButton);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 18));
        centerPanel.setOpaque(false);

        centerPanel.add(buildSummaryPanel(), BorderLayout.NORTH);
        centerPanel.add(buildTablePanel(), BorderLayout.CENTER);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    // =====================================================
    // SUMMARY CARDS
    // =====================================================

    private JPanel buildSummaryPanel() {

        JPanel panel = new JPanel(
                new GridLayout(1, 3, 15, 0)
        );
        panel.setOpaque(false);

        panel.add(createSummaryCard(
                "Total Information", totalValue
        ));

        panel.add(createSummaryCard(
                "Published", publishedValue
        ));

        panel.add(createSummaryCard(
                "Unpublished", unpublishedValue
        ));

        return panel;
    }

    private JPanel createSummaryCard(
            String title,
            JLabel valueLabel) {

        JPanel card = new JPanel();
        card.setBackground(Theme.WHITE);
        card.setLayout(new BoxLayout(
                card, BoxLayout.Y_AXIS
        ));

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(
                "Segoe UI", Font.PLAIN, 13
        ));
        titleLabel.setForeground(Theme.MUTED_TEXT);

        valueLabel.setFont(new Font(
                "Segoe UI", Font.BOLD, 27
        ));
        valueLabel.setForeground(Theme.NAVY);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);

        return card;
    }

    // =====================================================
    // TABLE PANEL
    // =====================================================

    private JPanel buildTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Theme.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(18, 18, 18, 18)
                        ));

        JPanel filters = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0)
        );
        filters.setOpaque(false);

        searchField.setFont(new Font(
                "Segoe UI", Font.PLAIN, 13
        ));
        searchField.setPreferredSize(
                new Dimension(230, 35)
        );
        searchField.setToolTipText(
                "Search by title or category"
        );

        statusFilter.setFont(new Font(
                "Segoe UI", Font.PLAIN, 13
        ));
        statusFilter.setPreferredSize(
                new Dimension(150, 35)
        );

        JButton searchButton = createButton("Search", false);
        JButton clearButton = createButton("Clear", false);

        searchButton.addActionListener(e -> applyFilters());

        searchField.addActionListener(e -> applyFilters());

        clearButton.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            applyFilters();
        });

        statusFilter.addActionListener(e -> applyFilters());

        filters.add(searchField);
        filters.add(searchButton);
        filters.add(statusFilter);
        filters.add(clearButton);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 7, 0)
        );
        actions.setOpaque(false);

        JButton addButton = createButton(
                "+ Add Information", true
        );
        JButton viewButton = createButton(
                "View Details", false
        );
        JButton editButton = createButton("Edit", false);
        JButton statusButton = createButton(
                "Publish / Unpublish", false
        );
        JButton deleteButton = createButton("Delete", false);

        addButton.addActionListener(e -> addInformation());
        viewButton.addActionListener(e -> viewInformation());
        editButton.addActionListener(e -> editInformation());
        statusButton.addActionListener(
                e -> changeInformationStatus()
        );
        deleteButton.addActionListener(e -> deleteInformation());

        actions.add(addButton);
        actions.add(viewButton);
        actions.add(editButton);
        actions.add(statusButton);
        actions.add(deleteButton);

        JPanel toolbar = new JPanel(new BorderLayout(0, 12));
        toolbar.setOpaque(false);
        toolbar.add(filters, BorderLayout.NORTH);
        toolbar.add(actions, BorderLayout.SOUTH);

        panel.add(toolbar, BorderLayout.NORTH);

        informationTable.setFont(new Font(
                "Segoe UI", Font.PLAIN, 13
        ));
        informationTable.setRowHeight(38);
        informationTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        informationTable.setShowVerticalLines(false);
        informationTable.setGridColor(Theme.BORDER);
        informationTable.setBackground(Theme.WHITE);
        informationTable.setSelectionBackground(
                Theme.LIGHT_BLUE
        );
        informationTable.setSelectionForeground(Theme.TEXT);

        informationTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );
        informationTable.getTableHeader().setBackground(
                Theme.BACKGROUND
        );
        informationTable.getTableHeader().setForeground(
                Theme.TEXT
        );
        informationTable.getTableHeader().setPreferredSize(
                new Dimension(0, 40)
        );

        // Hide the Information ID column.
        informationTable.removeColumn(
                informationTable.getColumnModel().getColumn(4)
        );

        JScrollPane scrollPane = new JScrollPane(
                informationTable
        );
        scrollPane.setBorder(
                BorderFactory.createLineBorder(Theme.BORDER)
        );

        panel.add(scrollPane, BorderLayout.CENTER);

        messageLabel.setFont(new Font(
                "Segoe UI", Font.PLAIN, 12
        ));
        messageLabel.setForeground(Theme.MUTED_TEXT);

        panel.add(messageLabel, BorderLayout.SOUTH);

        return panel;
    }

    // =====================================================
    // BUTTON STYLE
    // =====================================================

    private JButton createButton(
            String text,
            boolean primary) {

        JButton button = new JButton(text);

        button.setFont(new Font(
                "Segoe UI", Font.BOLD, 12
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR
        ));

        if (primary) {
            button.setBackground(Theme.NAVY);
            button.setForeground(Color.WHITE);
            button.setBorder(new EmptyBorder(
                    9, 13, 9, 13
            ));
        } else {
            button.setBackground(Theme.WHITE);
            button.setForeground(Theme.NAVY);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER),
                    new EmptyBorder(8, 12, 8, 12)
            ));
        }

        return button;
    }

    // =====================================================
    // LOAD INFORMATION
    // =====================================================

    private void loadInformation() {

        if (!UserSession.isAdmin()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        messageLabel.setText("Loading information...");

        runDatabaseTask(
                "Unable to load important information.",
                () -> repository.getAllInformation(
                        UserSession.getUserId()
                ),
                result -> {
                    allInformation.clear();
                    allInformation.addAll(result);

                    updateSummary();
                    applyFilters();

                    messageLabel.setText(
                            allInformation.size()
                                    + " information record(s) loaded."
                    );
                }
        );
    }

    // =====================================================
    // UPDATE SUMMARY
    // =====================================================

    private void updateSummary() {

        int published = 0;
        int unpublished = 0;

        for (Document document : allInformation) {

            String status = safeString(
                    document, "status"
            );

            if ("PUBLISHED".equalsIgnoreCase(status)) {
                published++;
            } else if ("UNPUBLISHED".equalsIgnoreCase(status)) {
                unpublished++;
            }
        }

        totalValue.setText(
                String.valueOf(allInformation.size())
        );
        publishedValue.setText(
                String.valueOf(published)
        );
        unpublishedValue.setText(
                String.valueOf(unpublished)
        );
    }

    // =====================================================
    // SEARCH AND FILTER
    // =====================================================

    private void applyFilters() {

        tableModel.setRowCount(0);

        String searchText = searchField.getText()
                .trim()
                .toLowerCase();

        String selectedStatus = String.valueOf(
                statusFilter.getSelectedItem()
        );

        for (Document document : allInformation) {

            String title = safeString(
                    document, "title"
            );

            String category = safeString(
                    document, "category"
            );

            String status = safeString(
                    document, "status"
            );

            boolean matchesSearch =
                    title.toLowerCase().contains(searchText)
                            || category.toLowerCase().contains(searchText);

            boolean matchesStatus =
                    "All Status".equals(selectedStatus)
                            || selectedStatus.equalsIgnoreCase(status);

            if (matchesSearch && matchesStatus) {

                ObjectId objectId = document.getObjectId("_id");

                if (objectId == null) {
                    continue;
                }

                tableModel.addRow(new Object[]{
                        title,
                        category,
                        status,
                        formatDate(document.getDate("publishedAt")),
                        objectId.toString()
                });
            }
        }
    }

    // =====================================================
    // GET SELECTED INFORMATION
    // =====================================================

    private Document getSelectedInformation() {

        int selectedRow = informationTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an information record first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        int modelRow = informationTable.convertRowIndexToModel(
                selectedRow
        );

        String informationId = String.valueOf(
                tableModel.getValueAt(modelRow, 4)
        );

        for (Document document : allInformation) {

            ObjectId id = document.getObjectId("_id");

            if (id != null
                    && id.toString().equals(informationId)) {
                return document;
            }
        }

        JOptionPane.showMessageDialog(
                this,
                "The selected information could not be found.",
                "Not Found",
                JOptionPane.ERROR_MESSAGE
        );

        return null;
    }

    // =====================================================
    // ADD INFORMATION
    // =====================================================

    private void addInformation() {

        String[] values = showInformationForm(null);

        if (values == null) {
            return;
        }

        runDatabaseTask(
                "Unable to publish information.",
                () -> repository.publishInformation(
                        values[0],
                        values[1],
                        values[2],
                        UserSession.getUserId()
                ),
                result -> {
                    JOptionPane.showMessageDialog(
                            this,
                            "Important information published successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    loadInformation();
                }
        );
    }

    // =====================================================
    // EDIT INFORMATION
    // =====================================================

    private void editInformation() {

        Document selected = getSelectedInformation();

        if (selected == null) {
            return;
        }

        String[] values = showInformationForm(selected);

        if (values == null) {
            return;
        }

        ObjectId objectId = selected.getObjectId("_id");

        if (objectId == null) {
            showOperationFailed();
            return;
        }

        String informationId = objectId.toString();

        runDatabaseTask(
                "Unable to update information.",
                () -> repository.updateInformation(
                        informationId,
                        values[0],
                        values[1],
                        values[2],
                        UserSession.getUserId()
                ),
                success -> {
                    if (success) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Information updated successfully.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        loadInformation();
                    } else {
                        showOperationFailed();
                    }
                }
        );
    }
        // =====================================================
    // INFORMATION FORM
    // =====================================================

    private String[] showInformationForm(
            Document existing) {

        JTextField titleField = new JTextField(25);
        JTextField categoryField = new JTextField(25);

        JTextArea descriptionArea = new JTextArea(7, 25);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font(
                "Segoe UI", Font.PLAIN, 13
        ));

        if (existing != null) {
            titleField.setText(
                    safeString(existing, "title")
            );
            categoryField.setText(
                    safeString(existing, "category")
            );
            descriptionArea.setText(
                    safeString(existing, "description")
            );
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 5, 10, 5));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        form.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Category:"), gbc);

        gbc.gridx = 1;
        form.add(categoryField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        form.add(new JScrollPane(descriptionArea), gbc);

        while (true) {

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    form,
                    existing == null
                            ? "Add Important Information"
                            : "Edit Important Information",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (choice != JOptionPane.OK_OPTION) {
                return null;
            }

            String title = titleField.getText().trim();
            String category = categoryField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (title.isEmpty()
                    || category.isEmpty()
                    || description.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please fill in all fields.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );

                continue;
            }

            return new String[]{
                    title,
                    category,
                    description
            };
        }
    }

    // =====================================================
    // VIEW INFORMATION DETAILS
    // =====================================================

    private void viewInformation() {

        Document document = getSelectedInformation();

        if (document == null) {
            return;
        }

        String details =
                "Title: " + safeString(document, "title")
                        + "\n\nCategory: "
                        + safeString(document, "category")
                        + "\n\nStatus: "
                        + safeString(document, "status")
                        + "\n\nPublished Date: "
                        + formatDate(document.getDate("publishedAt"))
                        + "\n\nDescription:\n"
                        + safeString(document, "description");

        JTextArea detailsArea = new JTextArea(details);
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font(
                "Segoe UI", Font.PLAIN, 13
        ));
        detailsArea.setBackground(Theme.WHITE);
        detailsArea.setBorder(new EmptyBorder(
                10, 10, 10, 10
        ));

        JScrollPane scrollPane = new JScrollPane(
                detailsArea
        );
        scrollPane.setPreferredSize(
                new Dimension(500, 350)
        );

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Information Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =====================================================
    // PUBLISH / UNPUBLISH
    // =====================================================

    private void changeInformationStatus() {

        Document selected = getSelectedInformation();

        if (selected == null) {
            return;
        }

        String currentStatus = safeString(
                selected, "status"
        );

        String newStatus;

        if ("PUBLISHED".equalsIgnoreCase(currentStatus)) {
            newStatus = "UNPUBLISHED";
        } else {
            newStatus = "PUBLISHED";
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to change the status to "
                        + newStatus + "?",
                "Confirm Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        ObjectId objectId = selected.getObjectId("_id");

        if (objectId == null) {
            showOperationFailed();
            return;
        }

        String informationId = objectId.toString();

        runDatabaseTask(
                "Unable to change information status.",
                () -> repository.updateInformationStatus(
                        informationId,
                        newStatus,
                        UserSession.getUserId()
                ),
                success -> {
                    if (success) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Information status changed to "
                                        + newStatus + ".",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        loadInformation();
                    } else {
                        showOperationFailed();
                    }
                }
        );
    }

    // =====================================================
    // DELETE INFORMATION
    // =====================================================

    private void deleteInformation() {

        Document selected = getSelectedInformation();

        if (selected == null) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete this information?\n\n"
                        + safeString(selected, "title"),
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        ObjectId objectId = selected.getObjectId("_id");

        if (objectId == null) {
            showOperationFailed();
            return;
        }

        String informationId = objectId.toString();

        runDatabaseTask(
                "Unable to delete information.",
                () -> repository.deleteInformation(
                        informationId,
                        UserSession.getUserId()
                ),
                success -> {
                    if (success) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Information deleted successfully.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        loadInformation();
                    } else {
                        showOperationFailed();
                    }
                }
        );
    }

    // =====================================================
    // NAVIGATION BETWEEN ADMIN PAGES
    // =====================================================

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

            case "Emergency Alerts":
                targetFrame = new AdminEmergencyAlertsFrame(dashboardFrame);
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

    // =====================================================
    // BACK TO DASHBOARD
    // =====================================================

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

        if (dashboardFrame != null
                && dashboardFrame.isDisplayable()) {

            dashboardFrame.setVisible(true);

        } else {
            new AdminDashboardFrame().setVisible(true);
        }

        dispose();
    }
        // =====================================================
    // DATABASE TASK
    // =====================================================

    private <T> void runDatabaseTask(
            String errorMessage,
            Callable<T> task,
            Consumer<T> onSuccess) {

        setCursor(Cursor.getPredefinedCursor(
                Cursor.WAIT_CURSOR
        ));

        SwingWorker<T, Void> worker = new SwingWorker<>() {

            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {

                setCursor(Cursor.getDefaultCursor());

                try {
                    T result = get();
                    onSuccess.accept(result);

                } catch (Exception exception) {

                    Throwable cause = exception.getCause();

                    String details;

                    if (cause != null
                            && cause.getMessage() != null) {
                        details = cause.getMessage();
                    } else if (exception.getMessage() != null) {
                        details = exception.getMessage();
                    } else {
                        details = "Unknown error";
                    }

                    JOptionPane.showMessageDialog(
                            AdminImportantInformationFrame.this,
                            errorMessage + "\n\n" + details,
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private String safeString(
            Document document,
            String key) {

        Object value = document.get(key);

        return value == null ? "" : value.toString();
    }

    private String formatDate(Date date) {

        if (date == null) {
            return "N/A";
        }

        return dateFormat.format(date);
    }

    private void showOperationFailed() {

        JOptionPane.showMessageDialog(
                this,
                "The operation could not be completed. Please try again.",
                "Operation Failed",
                JOptionPane.WARNING_MESSAGE
        );
    }
}