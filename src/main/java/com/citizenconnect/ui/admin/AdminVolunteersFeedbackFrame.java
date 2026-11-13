
package com.citizenconnect.ui.admin;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.VolunteerRepository;
import com.citizenconnect.repository.FeedbackRepository;
import com.citizenconnect.theme.Theme;

import org.bson.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminVolunteersFeedbackFrame extends JFrame {

    private final AdminDashboardFrame dashboardFrame;

    private final VolunteerRepository volunteerRepository =
            new VolunteerRepository();

    private final FeedbackRepository feedbackRepository =
            new FeedbackRepository();

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM yyyy, hh:mm a");

    private JTabbedPane tabbedPane;

    public AdminVolunteersFeedbackFrame() {
        this(null);
    }

    public AdminVolunteersFeedbackFrame(
            AdminDashboardFrame dashboardFrame
    ) {
        this.dashboardFrame = dashboardFrame;

        if (!UserSession.isAdmin()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            dispose();
            return;
        }

        setTitle("CitizenConnect Pro | Volunteers & Feedback");
        setSize(1250, 780);
        setMinimumSize(new Dimension(1050, 650));
        setLocationRelativeTo(dashboardFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
    }

    // =========================================
    // MAIN UI
    // =========================================

    private void buildUI() {

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);

        root.add(createHeader(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Theme.WHITE);
        tabbedPane.setForeground(Theme.NAVY);

        tabbedPane.addTab(
                "Manage Volunteers",
                new ManagementTab(true)
        );

        tabbedPane.addTab(
                "Manage Feedback",
                new ManagementTab(false)
        );

        root.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(root);
    }

    // =========================================
    // HEADER
    // =========================================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.WHITE);
        header.setBorder(new EmptyBorder(18, 25, 18, 25));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(
                titlePanel,
                BoxLayout.Y_AXIS
        ));

        JLabel title = new JLabel("Volunteers & Feedback");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Theme.NAVY);

        JLabel subtitle = new JLabel(
                "Manage volunteer applications and citizen feedback"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED_TEXT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton backButton = createButton(
                "← Back to Dashboard"
        );

        backButton.addActionListener(e -> openDashboard());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        return header;
    }

    // =========================================
    // MANAGEMENT TAB
    // =========================================

    private class ManagementTab extends JPanel {

        private final boolean volunteerTab;

        private JTable table;
        private DefaultTableModel tableModel;
        private TableRowSorter<DefaultTableModel> sorter;

        private JTextField searchField;
        private JComboBox<String> statusFilter;

        private JLabel totalValue;
        private JLabel firstStatusValue;
        private JLabel secondStatusValue;
        private JLabel thirdStatusValue;

        private JButton viewButton;
        private JButton updateButton;
        private JButton refreshButton;

        private List<Document> allRecords = new ArrayList<>();

        ManagementTab(boolean volunteerTab) {

            this.volunteerTab = volunteerTab;

            setLayout(new BorderLayout(15, 15));
            setBackground(Theme.BACKGROUND);
            setBorder(new EmptyBorder(20, 22, 20, 22));

            add(createSummaryPanel(), BorderLayout.NORTH);
            add(createTablePanel(), BorderLayout.CENTER);

            loadRecords();
        }

        // =====================================
        // SUMMARY CARDS
        // =====================================

        private JPanel createSummaryPanel() {

            JPanel panel = new JPanel(
                    new GridLayout(1, 4, 15, 0)
            );
            panel.setOpaque(false);

            totalValue = new JLabel("0");
            firstStatusValue = new JLabel("0");
            secondStatusValue = new JLabel("0");
            thirdStatusValue = new JLabel("0");

            if (volunteerTab) {

                panel.add(createSummaryCard(
                        "Total Volunteers",
                        totalValue
                ));

                panel.add(createSummaryCard(
                        "Pending",
                        firstStatusValue
                ));

                panel.add(createSummaryCard(
                        "Approved",
                        secondStatusValue
                ));

                panel.add(createSummaryCard(
                        "Rejected",
                        thirdStatusValue
                ));

            } else {

                panel.add(createSummaryCard(
                        "Total Feedback",
                        totalValue
                ));

                panel.add(createSummaryCard(
                        "New",
                        firstStatusValue
                ));

                panel.add(createSummaryCard(
                        "Reviewed",
                        secondStatusValue
                ));

                panel.add(createSummaryCard(
                        "Resolved",
                        thirdStatusValue
                ));
            }

            return panel;
        }

        private JPanel createSummaryCard(
                String heading,
                JLabel valueLabel
        ) {

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(
                    card,
                    BoxLayout.Y_AXIS
            ));

            card.setBackground(Theme.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER),
                    new EmptyBorder(16, 18, 16, 18)
            ));

            JLabel headingLabel = new JLabel(heading);
            headingLabel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );
            headingLabel.setForeground(Theme.MUTED_TEXT);

            valueLabel.setFont(
                    new Font("Segoe UI", Font.BOLD, 25)
            );
            valueLabel.setForeground(Theme.NAVY);

            card.add(headingLabel);
            card.add(Box.createVerticalStrut(8));
            card.add(valueLabel);

            return card;
        }

        // =====================================
        // TABLE PANEL
        // =====================================

        private JPanel createTablePanel() {

            JPanel panel = new JPanel(
                    new BorderLayout(12, 12)
            );

            panel.setBackground(Theme.WHITE);
            panel.setBorder(new EmptyBorder(18, 18, 18, 18));

            JPanel topPanel = new JPanel(
                    new BorderLayout(10, 10)
            );
            topPanel.setOpaque(false);

            searchField = new JTextField();
            searchField.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );
            searchField.setPreferredSize(
                    new Dimension(250, 38)
            );

            searchField.setToolTipText(
                    volunteerTab
                            ? "Search name, email or phone"
                            : "Search subject, category or message"
            );

            statusFilter = new JComboBox<>(
                    volunteerTab
                            ? new String[]{
                                    "All Statuses",
                                    "PENDING",
                                    "APPROVED",
                                    "REJECTED",
                                    "INACTIVE"
                            }
                            : new String[]{
                                    "All Statuses",
                                    "NEW",
                                    "REVIEWED",
                                    "RESOLVED"
                            }
            );

            statusFilter.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );

            JPanel filterPanel = new JPanel(
                    new FlowLayout(FlowLayout.LEFT, 8, 0)
            );
            filterPanel.setOpaque(false);

            filterPanel.add(new JLabel("Search:"));
            filterPanel.add(searchField);
            filterPanel.add(new JLabel("Status:"));
            filterPanel.add(statusFilter);

            refreshButton = createButton("Refresh");

            topPanel.add(filterPanel, BorderLayout.CENTER);
            topPanel.add(refreshButton, BorderLayout.EAST);

            String[] columns = volunteerTab
                    ? new String[]{
                            "Full Name",
                            "Email",
                            "Phone",
                            "Skills",
                            "Status",
                            "Registered Date",
                            "Record ID"
                    }
                    : new String[]{
                            "Subject",
                            "Category",
                            "Rating",
                            "Message",
                            "Status",
                            "Submitted Date",
                            "Record ID"
                    };

            tableModel = new DefaultTableModel(columns, 0) {

                @Override
                public boolean isCellEditable(
                        int row,
                        int column
                ) {
                    return false;
                }
            };

            table = new JTable(tableModel);

            table.setFont(
                    new Font("Segoe UI", Font.PLAIN, 12)
            );
            table.setRowHeight(34);
            table.setSelectionMode(
                    ListSelectionModel.SINGLE_SELECTION
            );
            table.setShowGrid(true);
            table.setGridColor(Theme.BORDER);
            table.setBackground(Theme.WHITE);
            table.setSelectionBackground(Theme.LIGHT_BLUE);
            table.setSelectionForeground(Theme.TEXT);

            table.getTableHeader().setFont(
                    new Font("Segoe UI", Font.BOLD, 12)
            );
            table.getTableHeader().setBackground(Theme.NAVY);
            table.getTableHeader().setForeground(Theme.WHITE);
            table.getTableHeader().setPreferredSize(
                    new Dimension(0, 38)
            );

            sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);

            // Hide database ID column.
            table.removeColumn(
                    table.getColumnModel().getColumn(6)
            );

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(
                    BorderFactory.createLineBorder(Theme.BORDER)
            );

            JPanel bottomPanel = new JPanel(
                    new FlowLayout(FlowLayout.RIGHT, 10, 0)
            );
            bottomPanel.setOpaque(false);

            viewButton = createButton("View Details");

            updateButton = createButton(
                    volunteerTab
                            ? "Update Volunteer Status"
                            : "Update Feedback Status"
            );

            viewButton.setEnabled(false);
            updateButton.setEnabled(false);

            bottomPanel.add(viewButton);
            bottomPanel.add(updateButton);

            table.getSelectionModel().addListSelectionListener(e -> {

                if (!e.getValueIsAdjusting()) {

                    boolean selected =
                            table.getSelectedRow() >= 0;

                    viewButton.setEnabled(selected);
                    updateButton.setEnabled(selected);
                }
            });

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

            statusFilter.addActionListener(
                    e -> applyFilters()
            );

            refreshButton.addActionListener(
                    e -> loadRecords()
            );

            viewButton.addActionListener(
                    e -> showSelectedDetails()
            );

            updateButton.addActionListener(
                    e -> updateSelectedStatus()
            );

            panel.add(topPanel, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(bottomPanel, BorderLayout.SOUTH);

            return panel;
        }

        // =====================================
        // LOAD DATABASE RECORDS
        // =====================================

        private void loadRecords() {

            if (!UserSession.isAdmin()) {
                showError("Admin access required.");
                return;
            }

            setControlsEnabled(false);

            SwingWorker<List<Document>, Void> worker =
                    new SwingWorker<>() {

                @Override
                protected List<Document> doInBackground() {

                    String adminId = UserSession.getUserId();

                    if (volunteerTab) {
                        return volunteerRepository
                                .getAllVolunteers(adminId);
                    }

                    return feedbackRepository
                            .getAllFeedback(adminId);
                }

                @Override
                protected void done() {

                    try {

                        allRecords = get();

                        // Update summary and table together.
                        updateSummary();
                        populateTable();

                    } catch (Exception ex) {

                        showError(
                                "Could not load "
                                        + (volunteerTab
                                        ? "volunteers"
                                        : "feedback")
                                        + ".\n\n"
                                        + getRootMessage(ex)
                        );

                    } finally {

                        setControlsEnabled(true);
                    }
                }
            };

            worker.execute();
        }

        // =====================================
        // UPDATE SUMMARY
        // =====================================

        private void updateSummary() {

            int total = allRecords.size();

            int first = 0;
            int second = 0;
            int third = 0;

            for (Document record : allRecords) {

                String status = getString(
                        record,
                        "status"
                ).toUpperCase(Locale.ROOT);

                if (volunteerTab) {

                    if ("PENDING".equals(status)) {
                        first++;
                    } else if ("APPROVED".equals(status)) {
                        second++;
                    } else if ("REJECTED".equals(status)) {
                        third++;
                    }

                } else {

                    if ("NEW".equals(status)) {
                        first++;
                    } else if ("REVIEWED".equals(status)) {
                        second++;
                    } else if ("RESOLVED".equals(status)) {
                        third++;
                    }
                }
            }

            totalValue.setText(String.valueOf(total));
            firstStatusValue.setText(String.valueOf(first));
            secondStatusValue.setText(String.valueOf(second));
            thirdStatusValue.setText(String.valueOf(third));
        }

        // =====================================
        // POPULATE TABLE
        // =====================================

        private void populateTable() {

            tableModel.setRowCount(0);

            for (Document record : allRecords) {

                String id = getId(record);

                if (volunteerTab) {

                    tableModel.addRow(new Object[]{
                            getString(record, "fullName"),
                            getString(record, "email"),
                            getString(record, "phone"),
                            getSkills(record),
                            getString(record, "status"),
                            formatDate(
                                    record.getDate("registeredAt")
                            ),
                            id
                    });

                } else {

                    tableModel.addRow(new Object[]{
                            getString(record, "subject"),
                            getString(record, "category"),
                            getString(record, "rating"),
                            getShortMessage(
                                    getString(record, "message")
                            ),
                            getString(record, "status"),
                            formatDate(
                                    record.getDate("submittedAt")
                            ),
                            id
                    });
                }
            }

            applyFilters();
        }

        // =====================================
        // SEARCH AND STATUS FILTER
        // =====================================

        private void applyFilters() {

            if (sorter == null) {
                return;
            }

            String search = searchField.getText()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            String selectedStatus =
                    (String) statusFilter.getSelectedItem();

            List<RowFilter<DefaultTableModel, Integer>> filters =
                    new ArrayList<>();

            if (!search.isEmpty()) {

                filters.add(new RowFilter<>() {

                    @Override
                    public boolean include(
                            Entry<? extends DefaultTableModel,
                                    ? extends Integer> entry
                    ) {

                        for (int i = 0; i < 6; i++) {

                            String value = String.valueOf(
                                    entry.getValue(i)
                            ).toLowerCase(Locale.ROOT);

                            if (value.contains(search)) {
                                return true;
                            }
                        }

                        return false;
                    }
                });
            }

            if (selectedStatus != null &&
                    !"All Statuses".equals(selectedStatus)) {

                filters.add(
                        RowFilter.regexFilter(
                                "^" + selectedStatus + "$",
                                4
                        )
                );
            }

            sorter.setRowFilter(
                    filters.isEmpty()
                            ? null
                            : RowFilter.andFilter(filters)
            );
        }

        // =====================================
        // VIEW DETAILS
        // =====================================

        private void showSelectedDetails() {

            Document record = getSelectedRecord();

            if (record == null) {
                showError("Please select a record first.");
                return;
            }

            StringBuilder details = new StringBuilder();

            if (volunteerTab) {

                details.append("VOLUNTEER DETAILS\n\n");

                details.append("Name: ")
                        .append(getString(record, "fullName"))
                        .append("\n\n");

                details.append("Email: ")
                        .append(getString(record, "email"))
                        .append("\n\n");

                details.append("Phone: ")
                        .append(getString(record, "phone"))
                        .append("\n\n");

                details.append("Skills: ")
                        .append(getSkills(record))
                        .append("\n\n");

                details.append("Availability: ")
                        .append(getString(record, "availability"))
                        .append("\n\n");

                details.append("Address: ")
                        .append(getString(record, "address"))
                        .append("\n\n");

                details.append("Status: ")
                        .append(getString(record, "status"))
                        .append("\n\n");

                details.append("Registered: ")
                        .append(formatDate(
                                record.getDate("registeredAt")
                        ));

            } else {

                details.append("FEEDBACK DETAILS\n\n");

                details.append("Subject: ")
                        .append(getString(record, "subject"))
                        .append("\n\n");

                details.append("Category: ")
                        .append(getString(record, "category"))
                        .append("\n\n");

                details.append("Rating: ")
                        .append(getString(record, "rating"))
                        .append(" / 5\n\n");

                details.append("Message:\n")
                        .append(getString(record, "message"))
                        .append("\n\n");

                details.append("Status: ")
                        .append(getString(record, "status"))
                        .append("\n\n");

                details.append("Submitted: ")
                        .append(formatDate(
                                record.getDate("submittedAt")
                        ));
            }

            JTextArea textArea = new JTextArea(
                    details.toString()
            );

            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );
            textArea.setBackground(Theme.WHITE);
            textArea.setBorder(
                    new EmptyBorder(12, 12, 12, 12)
            );

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(
                    new Dimension(470, 400)
            );

            JOptionPane.showMessageDialog(
                    this,
                    scrollPane,
                    volunteerTab
                            ? "Volunteer Details"
                            : "Feedback Details",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        // =====================================
        // UPDATE STATUS
        // =====================================

        private void updateSelectedStatus() {

            Document record = getSelectedRecord();

            if (record == null) {
                showError("Please select a record first.");
                return;
            }

            String currentStatus = getString(
                    record,
                    "status"
            );

            String[] options = volunteerTab
                    ? new String[]{
                            "PENDING",
                            "APPROVED",
                            "REJECTED",
                            "INACTIVE"
                    }
                    : new String[]{
                            "NEW",
                            "REVIEWED",
                            "RESOLVED"
                    };

            JComboBox<String> statusCombo =
                    new JComboBox<>(options);

            statusCombo.setSelectedItem(currentStatus);

            JPanel form = new JPanel(
                    new GridLayout(2, 1, 5, 8)
            );

            form.add(new JLabel("Select new status:"));
            form.add(statusCombo);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    form,
                    volunteerTab
                            ? "Update Volunteer Status"
                            : "Update Feedback Status",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String newStatus =
                    (String) statusCombo.getSelectedItem();

            if (newStatus == null ||
                    newStatus.equals(currentStatus)) {
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Change status from "
                            + currentStatus
                            + " to "
                            + newStatus
                            + "?",
                    "Confirm Status Update",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            String recordId = getId(record);
            String adminId = UserSession.getUserId();

            setControlsEnabled(false);

            SwingWorker<Boolean, Void> worker =
                    new SwingWorker<>() {

                @Override
                protected Boolean doInBackground() {

                    if (volunteerTab) {

                        return volunteerRepository
                                .updateVolunteerStatus(
                                        recordId,
                                        newStatus,
                                        adminId
                                );
                    }

                    return feedbackRepository
                            .updateFeedbackStatus(
                                    recordId,
                                    newStatus,
                                    adminId
                            );
                }

                @Override
                protected void done() {

                    try {

                        boolean updated = get();

                        if (updated) {

                            JOptionPane.showMessageDialog(
                                    ManagementTab.this,
                                    "Status updated successfully.",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                            );

                            loadRecords();

                        } else {

                            showError(
                                    "No record was updated. Please refresh."
                            );
                        }

                    } catch (Exception ex) {

                        showError(
                                "Status update failed.\n\n"
                                        + getRootMessage(ex)
                        );

                    } finally {

                        setControlsEnabled(true);
                    }
                }
            };

            worker.execute();
        }

        // =====================================
        // GET SELECTED RECORD
        // =====================================

        private Document getSelectedRecord() {

            int selectedViewRow = table.getSelectedRow();

            if (selectedViewRow < 0) {
                return null;
            }

            int modelRow =
                    table.convertRowIndexToModel(selectedViewRow);

            String selectedId = String.valueOf(
                    tableModel.getValueAt(modelRow, 6)
            );

            for (Document record : allRecords) {

                if (selectedId.equals(getId(record))) {
                    return record;
                }
            }

            return null;
        }

        // =====================================
        // ENABLE / DISABLE CONTROLS
        // =====================================

        private void setControlsEnabled(boolean enabled) {

            if (refreshButton != null) {
                refreshButton.setEnabled(enabled);
            }

            if (searchField != null) {
                searchField.setEnabled(enabled);
            }

            if (statusFilter != null) {
                statusFilter.setEnabled(enabled);
            }

            if (table != null) {
                table.setEnabled(enabled);
            }

            if (viewButton != null) {
                viewButton.setEnabled(
                        enabled && table.getSelectedRow() >= 0
                );
            }

            if (updateButton != null) {
                updateButton.setEnabled(
                        enabled && table.getSelectedRow() >= 0
                );
            }
        }
    }

    // =========================================
    // SHARED HELPERS
    // =========================================

    private JButton createButton(String text) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        button.setForeground(Theme.WHITE);
        button.setBackground(Theme.NAVY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setPreferredSize(
                new Dimension(155, 38)
        );

        return button;
    }

    private String getString(
            Document document,
            String key
    ) {

        Object value = document.get(key);

        return value == null
                ? ""
                : String.valueOf(value);
    }

    private String getId(Document document) {

        Object id = document.get("_id");

        return id == null
                ? ""
                : id.toString();
    }

    private String getSkills(Document document) {

        Object skills = document.get("skills");

        if (skills instanceof List<?>) {

            return String.join(
                    ", ",
                    ((List<?>) skills).stream()
                            .map(String::valueOf)
                            .toList()
            );
        }

        return skills == null
                ? ""
                : skills.toString();
    }

    private String getShortMessage(String message) {

        if (message == null) {
            return "";
        }

        if (message.length() > 55) {
            return message.substring(0, 52) + "...";
        }

        return message;
    }

    private String formatDate(java.util.Date date) {

        if (date == null) {
            return "N/A";
        }

        return DATE_FORMAT.format(date);
    }

    private String getRootMessage(Exception exception) {

        Throwable cause = exception;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        String message = cause.getMessage();

        return message == null
                ? cause.getClass().getSimpleName()
                : message;
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // =========================================
    // BACK TO DASHBOARD
    // =========================================

    private void openDashboard() {

        if (dashboardFrame != null &&
                dashboardFrame.isDisplayable()) {

            dashboardFrame.setVisible(true);
            dashboardFrame.setState(JFrame.NORMAL);

        } else {

            AdminDashboardFrame dashboard =
                    new AdminDashboardFrame();

            dashboard.setVisible(true);
        }

        dispose();
    }
}
