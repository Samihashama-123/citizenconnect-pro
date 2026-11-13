
package com.citizenconnect.ui.admin;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.ComplaintRepository;
import com.citizenconnect.theme.Theme;
import com.citizenconnect.theme.RoundedPanel;
import com.citizenconnect.theme.RoundedButton;

import org.bson.Document;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class AdminComplaintsFrame extends JFrame {

    private final ComplaintRepository repository;
    private final AdminDashboardFrame dashboardFrame;

    private JTable complaintTable;
    private DefaultTableModel tableModel;

    private JTextField searchField;
    private JComboBox<String> statusFilter;

    private JLabel totalLabel;
    private JLabel submittedLabel;
    private JLabel reviewLabel;
    private JLabel progressLabel;
    private JLabel resolvedLabel;
    private JLabel rejectedLabel;

    private JButton viewButton;
    private JButton updateButton;
    private JButton refreshButton;

    private List<Document> allComplaints = new ArrayList<>();

    private static final String[] STATUSES = {
        "SUBMITTED",
        "UNDER_REVIEW",
        "IN_PROGRESS",
        "RESOLVED",
        "REJECTED"
    };

    public AdminComplaintsFrame() {
        this(null);
    }

    public AdminComplaintsFrame(AdminDashboardFrame dashboardFrame) {

        if (!UserSession.isAdmin()) {
            throw new SecurityException("Admin access required.");
        }

        this.dashboardFrame = dashboardFrame;
        this.repository = new ComplaintRepository();

        setTitle("CitizenConnect Pro | Manage Complaints");
        setSize(1350, 850);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(dashboardFrame);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BACKGROUND);

        mainPanel.add(createSidebar(), BorderLayout.WEST);
        mainPanel.add(createContent(), BorderLayout.CENTER);

        setContentPane(mainPanel);

        loadComplaints();
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
                    item.equals("Manage Complaints")
                            ? Theme.PRIMARY
                            : Theme.NAVY
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

            if (item.equals("Dashboard")) {
                button.addActionListener(e -> openDashboard());
            }

            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(7));
        }

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

        return sidebar;
    }

    // ================= MAIN CONTENT =================

    private JPanel createContent() {

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(new EmptyBorder(25, 30, 25, 30));

        content.add(createHeader(), BorderLayout.NORTH);
        content.add(createBody(), BorderLayout.CENTER);

        return content;
    }

    // ================= HEADER =================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 22, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Manage Complaints");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(Theme.NAVY);

        JLabel subtitle = new JLabel(
                "Review, manage and resolve citizen complaints"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED_TEXT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(subtitle);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        JButton backButton = new RoundedButton("Back to Dashboard");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backButton.addActionListener(e -> openDashboard());

        refreshButton = new RoundedButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.addActionListener(e -> loadComplaints());

        buttons.add(backButton);
        buttons.add(refreshButton);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(buttons, BorderLayout.EAST);

        return header;
    }

    // ================= BODY =================

    private JPanel createBody() {

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);

        body.add(createSummaryPanel(), BorderLayout.NORTH);
        body.add(createTablePanel(), BorderLayout.CENTER);

        return body;
    }

    // ================= SUMMARY CARDS =================

    private JPanel createSummaryPanel() {

        JPanel panel = new JPanel(new GridLayout(2, 3, 14, 14));
        panel.setOpaque(false);

        totalLabel = createSummaryValue();
        submittedLabel = createSummaryValue();
        reviewLabel = createSummaryValue();
        progressLabel = createSummaryValue();
        resolvedLabel = createSummaryValue();
        rejectedLabel = createSummaryValue();

        panel.add(createSummaryCard("Total Complaints", totalLabel));
        panel.add(createSummaryCard("Submitted", submittedLabel));
        panel.add(createSummaryCard("Under Review", reviewLabel));
        panel.add(createSummaryCard("In Progress", progressLabel));
        panel.add(createSummaryCard("Resolved", resolvedLabel));
        panel.add(createSummaryCard("Rejected", rejectedLabel));

        return panel;
    }

    private JLabel createSummaryValue() {

        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 27));
        label.setForeground(Theme.NAVY);

        return label;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel) {

        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(8, 12));
        card.setBorder(new EmptyBorder(15, 18, 15, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(Theme.MUTED_TEXT);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // ================= TABLE PANEL =================

    private JPanel createTablePanel() {

        RoundedPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel sectionTitle = new JLabel("All Complaints");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        sectionTitle.setForeground(Theme.NAVY);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        searchField = new JTextField(19);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setPreferredSize(new Dimension(210, 34));

        searchField.setToolTipText("Search complaint number or subject");

        statusFilter = new JComboBox<>(new String[]{
            "ALL STATUSES",
            "SUBMITTED",
            "UNDER_REVIEW",
            "IN_PROGRESS",
            "RESOLVED",
            "REJECTED"
        });

        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilter.setPreferredSize(new Dimension(155, 34));

        filterPanel.add(searchField);
        filterPanel.add(statusFilter);

        topPanel.add(sectionTitle, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.EAST);

        String[] columns = {
            "Complaint Number",
            "Subject",
            "Category",
            "Priority",
            "Status",
            "Submitted Date",
            "Complaint ID"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        complaintTable = new JTable(tableModel);

        complaintTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        complaintTable.setForeground(Theme.TEXT);
        complaintTable.setBackground(Color.WHITE);
        complaintTable.setRowHeight(38);
        complaintTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        complaintTable.setSelectionBackground(Theme.LIGHT_BLUE);
        complaintTable.setSelectionForeground(Theme.NAVY);
        complaintTable.setShowGrid(false);
        complaintTable.setIntercellSpacing(new Dimension(0, 0));
        complaintTable.setAutoCreateRowSorter(true);

        JTableHeader tableHeader = complaintTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBackground(Theme.LIGHT_BLUE);
        tableHeader.setForeground(Theme.NAVY);
        tableHeader.setPreferredSize(new Dimension(0, 38));
        tableHeader.setReorderingAllowed(false);

        // Hide Complaint ID column.
        complaintTable.removeColumn(
                complaintTable.getColumnModel().getColumn(6)
        );

        JScrollPane scrollPane = new JScrollPane(complaintTable);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(Theme.BORDER)
        );

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        viewButton = new RoundedButton("View Details");
        updateButton = new RoundedButton("Update Complaint");

        viewButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        updateButton.setFont(new Font("Segoe UI", Font.BOLD, 12));

        viewButton.setEnabled(false);
        updateButton.setEnabled(false);

        viewButton.addActionListener(e -> viewSelectedComplaint());
        updateButton.addActionListener(e -> updateSelectedComplaint());

        bottomPanel.add(viewButton);
        bottomPanel.add(updateButton);

        complaintTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean selected = complaintTable.getSelectedRow() >= 0;
                viewButton.setEnabled(selected);
                updateButton.setEnabled(selected);
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                filterComplaints();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterComplaints();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterComplaints();
            }
        });

        statusFilter.addActionListener(e -> filterComplaints());

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ================= LOAD COMPLAINTS =================

    private void loadComplaints() {

        if (!UserSession.isAdmin()) {
            showMessage(
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        setLoading(true);

        SwingWorker<List<Document>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<Document> doInBackground() {

                return repository.getAllComplaints(
                        UserSession.getUserId()
                );
            }

            @Override
            protected void done() {

                setLoading(false);

                try {

                    allComplaints = get();

                    filterComplaints();
                    updateSummary();

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                    showMessage(
                            "Complaint loading was interrupted.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                } catch (ExecutionException ex) {

                    Throwable cause = ex.getCause();

                    showMessage(
                            cause == null
                                    ? ex.getMessage()
                                    : cause.getMessage(),
                            "Unable to Load Complaints",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    // ================= FILTER COMPLAINTS =================

    private void filterComplaints() {

        if (tableModel == null) {
            return;
        }

        tableModel.setRowCount(0);

        String search = searchField.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        String selectedStatus =
                statusFilter.getSelectedItem().toString();

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd MMM yyyy");

        for (Document complaint : allComplaints) {

            String complaintNumber =
                    getText(complaint, "complaintNumber");

            String subject =
                    getText(complaint, "subject");

            String category =
                    getText(complaint, "category");

            String priority =
                    getText(complaint, "priority");

            String status =
                    getText(complaint, "status");

            boolean matchesSearch =
                    complaintNumber.toLowerCase(Locale.ROOT).contains(search)
                    || subject.toLowerCase(Locale.ROOT).contains(search);

            boolean matchesStatus =
                    selectedStatus.equals("ALL STATUSES")
                    || selectedStatus.equalsIgnoreCase(status);

            if (!matchesSearch || !matchesStatus) {
                continue;
            }

            Object dateValue = complaint.get("submittedAt");
            String date = "-";

            if (dateValue instanceof Date) {
                date = dateFormat.format((Date) dateValue);
            }

            String complaintId = getId(complaint);

            tableModel.addRow(new Object[]{
                complaintNumber,
                subject,
                category,
                priority,
                status,
                date,
                complaintId
            });
        }

        viewButton.setEnabled(false);
        updateButton.setEnabled(false);
    }

    // ================= SUMMARY UPDATE =================

    private void updateSummary() {

        int submitted = 0;
        int underReview = 0;
        int inProgress = 0;
        int resolved = 0;
        int rejected = 0;

        for (Document complaint : allComplaints) {

            String status = getText(complaint, "status");

            switch (status.toUpperCase(Locale.ROOT)) {

                case "SUBMITTED" -> submitted++;

                case "UNDER_REVIEW" -> underReview++;

                case "IN_PROGRESS" -> inProgress++;

                case "RESOLVED" -> resolved++;

                case "REJECTED" -> rejected++;

                default -> {
                }
            }
        }

        totalLabel.setText(String.valueOf(allComplaints.size()));
        submittedLabel.setText(String.valueOf(submitted));
        reviewLabel.setText(String.valueOf(underReview));
        progressLabel.setText(String.valueOf(inProgress));
        resolvedLabel.setText(String.valueOf(resolved));
        rejectedLabel.setText(String.valueOf(rejected));
    }

    // ================= SELECTED COMPLAINT =================

    private Document getSelectedComplaint() {

        int selectedRow = complaintTable.getSelectedRow();

        if (selectedRow < 0) {
            return null;
        }

        int modelRow = complaintTable.convertRowIndexToModel(selectedRow);

        String complaintId = String.valueOf(
                tableModel.getValueAt(modelRow, 6)
        );

        for (Document complaint : allComplaints) {

            if (complaintId.equals(getId(complaint))) {
                return complaint;
            }
        }

        return null;
    }

    // ================= VIEW DETAILS =================

    private void viewSelectedComplaint() {

        Document complaint = getSelectedComplaint();

        if (complaint == null) {
            showMessage(
                    "Please select a complaint first.",
                    "No Complaint Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBackground(Color.WHITE);
        detailsArea.setText(buildComplaintDetails(complaint));
        detailsArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setPreferredSize(new Dimension(620, 520));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Complaint Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String buildComplaintDetails(Document complaint) {

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        StringBuilder details = new StringBuilder();

        details.append("COMPLAINT INFORMATION\n");
        details.append("==============================\n\n");

        details.append("Complaint Number: ")
                .append(getText(complaint, "complaintNumber"))
                .append("\n");

        details.append("Subject: ")
                .append(getText(complaint, "subject"))
                .append("\n");

        details.append("Category: ")
                .append(getText(complaint, "category"))
                .append("\n");

        details.append("Priority: ")
                .append(getText(complaint, "priority"))
                .append("\n");

        details.append("Status: ")
                .append(getText(complaint, "status"))
                .append("\n");

        details.append("Location: ")
                .append(getText(complaint, "location"))
                .append("\n");

        details.append("Submitted: ")
                .append(formatDate(complaint.get("submittedAt"), dateFormat))
                .append("\n");

        details.append("Last Updated: ")
                .append(formatDate(complaint.get("updatedAt"), dateFormat))
                .append("\n\n");

        details.append("DESCRIPTION\n");
        details.append("==============================\n");

        details.append(getText(complaint, "description"))
                .append("\n\n");

        details.append("ADMIN RESPONSE\n");
        details.append("==============================\n");

        details.append(getText(complaint, "adminResponse"))
                .append("\n\n");

        details.append("ATTACHMENTS\n");
        details.append("==============================\n");

        Object attachmentsValue = complaint.get("attachments");

        if (attachmentsValue instanceof List<?> attachments
                && !attachments.isEmpty()) {

            for (Object attachment : attachments) {
                details.append(attachment.toString()).append("\n");
            }

        } else {
            details.append("No attachments available.\n");
        }

        details.append("\nSTATUS HISTORY\n");
        details.append("==============================\n");

        Object historyValue = complaint.get("statusHistory");

        if (historyValue instanceof List<?> history && !history.isEmpty()) {

            for (Object entry : history) {

                if (entry instanceof Document historyEntry) {

                    details.append("Status: ")
                            .append(getText(historyEntry, "status"))
                            .append("\n");

                    details.append("Remarks: ")
                            .append(getText(historyEntry, "remarks"))
                            .append("\n");

                    details.append("Date: ")
                            .append(formatDate(
                                    historyEntry.get("changedAt"),
                                    dateFormat
                            ))
                            .append("\n\n");
                }
            }

        } else {
            details.append("No status history available.\n");
        }

        return details.toString();
    }

    // ================= UPDATE COMPLAINT =================

    private void updateSelectedComplaint() {

        Document complaint = getSelectedComplaint();

        if (complaint == null) {
            showMessage(
                    "Please select a complaint first.",
                    "No Complaint Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String complaintId = getId(complaint);

        JComboBox<String> statusCombo = new JComboBox<>(STATUSES);

        statusCombo.setSelectedItem(
                getText(complaint, "status")
        );

        JTextArea responseArea = new JTextArea(5, 30);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        responseArea.setText(
                "-".equals(getText(complaint, "adminResponse"))
                        ? ""
                        : getText(complaint, "adminResponse")
        );

        JPanel form = new JPanel(new GridBagLayout());
        form.setPreferredSize(new Dimension(440, 210));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Complaint Number:"), gbc);

        gbc.gridx = 1;
        form.add(new JLabel(
                getText(complaint, "complaintNumber")
        ), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("New Status:"), gbc);

        gbc.gridx = 1;
        form.add(statusCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Admin Response:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        form.add(new JScrollPane(responseArea), gbc);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Update Complaint",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String newStatus =
                statusCombo.getSelectedItem().toString();

        String response = responseArea.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to update this complaint?",
                "Confirm Update",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setLoading(true);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() {

                return repository.updateComplaintStatus(
                        complaintId,
                        newStatus,
                        UserSession.getUserId(),
                        response
                );
            }

            @Override
            protected void done() {

                setLoading(false);

                try {

                    if (get()) {

                        showMessage(
                                "Complaint updated successfully.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        loadComplaints();

                    } else {

                        showMessage(
                                "Complaint could not be updated.",
                                "Update Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showMessage(
                            "Complaint update was interrupted.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                } catch (ExecutionException ex) {

                    Throwable cause = ex.getCause();

                    showMessage(
                            cause == null
                                    ? ex.getMessage()
                                    : cause.getMessage(),
                            "Update Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    // ================= DASHBOARD NAVIGATION =================

    private void openDashboard() {

        if (!UserSession.isAdmin()) {
            showMessage(
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (dashboardFrame != null && dashboardFrame.isDisplayable()) {

            dashboardFrame.setVisible(true);
            dashboardFrame.setExtendedState(JFrame.NORMAL);
            dashboardFrame.toFront();
            dashboardFrame.requestFocus();

        } else {

            AdminDashboardFrame newDashboard =
                    new AdminDashboardFrame();

            newDashboard.setVisible(true);
        }

        dispose();
    }

    // ================= HELPERS =================

    private String getText(Document document, String field) {

        Object value = document.get(field);

        if (value == null || value.toString().isBlank()) {
            return "-";
        }

        return value.toString();
    }

    private String getId(Document document) {

        Object id = document.get("_id");

        return id == null ? "" : id.toString();
    }

    private String formatDate(Object value, SimpleDateFormat format) {

        if (value instanceof Date date) {
            return format.format(date);
        }

        return value == null ? "-" : value.toString();
    }

    private void setLoading(boolean loading) {

        if (refreshButton != null) {
            refreshButton.setEnabled(!loading);
        }

        if (viewButton != null) {
            viewButton.setEnabled(!loading);
        }

        if (updateButton != null) {
            updateButton.setEnabled(!loading);
        }

        if (complaintTable != null) {
            complaintTable.setEnabled(!loading);
        }
    }

    private void showMessage(
            String message,
            String title,
            int messageType
    ) {

        JOptionPane.showMessageDialog(
                this,
                message == null ? "An unexpected error occurred." : message,
                title,
                messageType
        );
    }
}
