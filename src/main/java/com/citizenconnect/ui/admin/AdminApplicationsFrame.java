
package com.citizenconnect.ui.admin;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.ApplicationRepository;
import com.citizenconnect.repository.ServiceRepository;
import com.citizenconnect.theme.Theme;
import com.citizenconnect.theme.RoundedPanel;
import com.citizenconnect.theme.RoundedButton;

import org.bson.Document;
import org.bson.types.ObjectId;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AdminApplicationsFrame extends JFrame {

    private final ApplicationRepository applicationRepository;
    private final ServiceRepository serviceRepository;
    private final AdminDashboardFrame dashboardFrame;

    private JTable applicationsTable;
    private DefaultTableModel tableModel;

    private JTextField searchField;
    private JComboBox<String> statusFilter;

    private JLabel totalLabel;
    private JLabel submittedLabel;
    private JLabel reviewLabel;
    private JLabel approvedLabel;
    private JLabel rejectedLabel;
    private JLabel loadingLabel;

    private JButton viewButton;
    private JButton updateButton;
    private JButton refreshButton;

    private List<Document> allApplications = new ArrayList<>();
    private Map<String, String> serviceNames = new HashMap<>();

    private static final String[] STATUSES = {
        "SUBMITTED",
        "UNDER_REVIEW",
        "PENDING_DOCUMENTS",
        "APPROVED",
        "REJECTED"
    };

    private static final String[] TABLE_COLUMNS = {
        "Application Number",
        "Applicant Name",
        "Service",
        "Submission Date",
        "Status",
        "Application ID"
    };

    public AdminApplicationsFrame() {
        this(null);
    }

    public AdminApplicationsFrame(AdminDashboardFrame dashboardFrame) {

        if (!UserSession.isAdmin()) {
            throw new SecurityException("Admin access required.");
        }

        this.dashboardFrame = dashboardFrame;

        applicationRepository = new ApplicationRepository();
        serviceRepository = new ServiceRepository();

        setTitle("CitizenConnect Pro | Manage Applications");
        setSize(1350, 850);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(dashboardFrame);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BACKGROUND);

        mainPanel.add(createSidebar(), BorderLayout.WEST);
        mainPanel.add(createContent(), BorderLayout.CENTER);

        setContentPane(mainPanel);

        loadApplications();
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

        JLabel sectionLabel = new JLabel("ADMINISTRATION");
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sectionLabel.setForeground(new Color(195, 207, 222));

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
                    item.equals("Manage Applications")
                            ? Theme.PRIMARY
                            : Theme.NAVY
            );

            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
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

        sidebar.add(separator);
        sidebar.add(Box.createVerticalStrut(18));

        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(210, 220, 232));

        sidebar.add(roleLabel);

        return sidebar;
    }

    // ================= MAIN CONTENT =================

    private JPanel createContent() {

        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(new EmptyBorder(28, 30, 25, 30));

        content.add(createHeader(), BorderLayout.NORTH);
        content.add(createApplicationArea(), BorderLayout.CENTER);

        return content;
    }

    // ================= HEADER =================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Manage Applications");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(Theme.TEXT);

        JLabel subtitle = new JLabel(
                "Review citizen applications and manage their status."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED_TEXT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);

        RoundedButton backButton = new RoundedButton("← Dashboard");
        backButton.addActionListener(e -> openDashboard());

        refreshButton = new RoundedButton("Refresh");
        refreshButton.addActionListener(e -> loadApplications());

        actionPanel.add(backButton);
        actionPanel.add(refreshButton);

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(actionPanel, BorderLayout.EAST);

        return header;
    }

    // ================= APPLICATION AREA =================

    private JPanel createApplicationArea() {

        JPanel area = new JPanel(new BorderLayout(0, 18));
        area.setOpaque(false);

        area.add(createSummaryPanel(), BorderLayout.NORTH);
        area.add(createTablePanel(), BorderLayout.CENTER);

        return area;
    }

    // ================= SUMMARY CARDS =================

    private JPanel createSummaryPanel() {

        JPanel panel = new JPanel(new GridLayout(1, 5, 12, 0));
        panel.setOpaque(false);

        totalLabel = new JLabel("0");
        submittedLabel = new JLabel("0");
        reviewLabel = new JLabel("0");
        approvedLabel = new JLabel("0");
        rejectedLabel = new JLabel("0");

        panel.add(createSummaryCard("Total Applications", totalLabel));
        panel.add(createSummaryCard("Submitted", submittedLabel));
        panel.add(createSummaryCard("Under Review", reviewLabel));
        panel.add(createSummaryCard("Approved", approvedLabel));
        panel.add(createSummaryCard("Rejected", rejectedLabel));

        return panel;
    }

    private JPanel createSummaryCard(String heading, JLabel valueLabel) {

        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.WHITE);
        card.setBorder(new EmptyBorder(18, 16, 18, 16));

        JLabel title = new JLabel(heading);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        title.setForeground(Theme.MUTED_TEXT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 25));
        valueLabel.setForeground(Theme.NAVY);

        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);

        return card;
    }

    // ================= TABLE PANEL =================

    private JPanel createTablePanel() {

        RoundedPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout(0, 15));
        panel.setBackground(Theme.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel tableTitle = new JLabel("Citizen Applications");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(Theme.TEXT);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filters.setOpaque(false);

        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setToolTipText("Search application or applicant");

        statusFilter = new JComboBox<>(new String[]{
            "All Statuses",
            "SUBMITTED",
            "UNDER_REVIEW",
            "PENDING_DOCUMENTS",
            "APPROVED",
            "REJECTED"
        });

        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        filters.add(searchField);
        filters.add(statusFilter);

        topPanel.add(tableTitle, BorderLayout.WEST);
        topPanel.add(filters, BorderLayout.EAST);

        tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        applicationsTable = new JTable(tableModel);
        applicationsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        applicationsTable.setRowHeight(38);
        applicationsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        applicationsTable.setShowVerticalLines(false);
        applicationsTable.setGridColor(Theme.BORDER);
        applicationsTable.setSelectionBackground(Theme.LIGHT_BLUE);
        applicationsTable.setSelectionForeground(Theme.TEXT);
        applicationsTable.setAutoCreateRowSorter(true);

        JTableHeader tableHeader = applicationsTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBackground(Theme.BACKGROUND);
        tableHeader.setForeground(Theme.TEXT);
        tableHeader.setPreferredSize(new Dimension(0, 42));

        // Hide the Application ID column.
        applicationsTable.removeColumn(
                applicationsTable.getColumnModel().getColumn(5)
        );

        JScrollPane scrollPane = new JScrollPane(applicationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scrollPane.getViewport().setBackground(Theme.WHITE);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        loadingLabel = new JLabel(" ");
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loadingLabel.setForeground(Theme.MUTED_TEXT);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        viewButton = new RoundedButton("View Details");
        updateButton = new RoundedButton("Update Status");

        viewButton.addActionListener(e -> viewSelectedApplication());
        updateButton.addActionListener(e -> updateSelectedApplication());

        viewButton.setEnabled(false);
        updateButton.setEnabled(false);

        buttons.add(viewButton);
        buttons.add(updateButton);

        bottomPanel.add(loadingLabel, BorderLayout.WEST);
        bottomPanel.add(buttons, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        applicationsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean selected = applicationsTable.getSelectedRow() >= 0;
                viewButton.setEnabled(selected);
                updateButton.setEnabled(selected);
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                filterApplications();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterApplications();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterApplications();
            }
        });

        statusFilter.addActionListener(e -> filterApplications());

        return panel;
    }

    // ================= LOAD APPLICATIONS =================

    private void loadApplications() {

        if (!UserSession.isAdmin()) {
            showMessage(
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        setLoading(true);
        loadingLabel.setText("Loading applications...");

        String adminId = UserSession.getUserId();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            private List<Document> applications;
            private Map<String, String> loadedServices;

            @Override
            protected Void doInBackground() {

                applications = applicationRepository.getAllApplications();

                loadedServices = new HashMap<>();

                List<Document> services =
                        serviceRepository.getAllServices(adminId);

                for (Document service : services) {

                    ObjectId id = service.getObjectId("_id");

                    if (id != null) {
                        loadedServices.put(
                                id.toHexString(),
                                getText(service, "serviceName")
                        );
                    }
                }

                return null;
            }

            @Override
            protected void done() {

                try {
                    get();

                    allApplications = applications;
                    serviceNames = loadedServices;

                    filterApplications();
                    updateSummary();

                    loadingLabel.setText(
                            allApplications.size() + " application(s) found."
                    );

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showMessage(
                            "Loading was interrupted.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                } catch (ExecutionException ex) {

                    Throwable cause = ex.getCause();

                    showMessage(
                            cause == null
                                    ? ex.getMessage()
                                    : cause.getMessage(),
                            "Could Not Load Applications",
                            JOptionPane.ERROR_MESSAGE
                    );

                    loadingLabel.setText("Failed to load applications.");

                } finally {
                    setLoading(false);
                }
            }
        };

        worker.execute();
    }

    // ================= FILTER APPLICATIONS =================

    private void filterApplications() {

        if (tableModel == null) {
            return;
        }

        tableModel.setRowCount(0);

        String search = searchField.getText()
                .trim()
                .toLowerCase();

        String selectedStatus =
                (String) statusFilter.getSelectedItem();

        for (Document application : allApplications) {

            String applicationNumber =
                    getText(application, "applicationNumber");

            String applicantName =
                    getText(application, "applicantName");

            String applicantEmail =
                    getText(application, "applicantEmail");

            String serviceName =
                    getServiceName(application);

            String status =
                    getText(application, "applicationStatus");

            boolean matchesSearch =
                    search.isEmpty()
                    || applicationNumber.toLowerCase().contains(search)
                    || applicantName.toLowerCase().contains(search)
                    || applicantEmail.toLowerCase().contains(search)
                    || serviceName.toLowerCase().contains(search);

            boolean matchesStatus =
                    selectedStatus == null
                    || selectedStatus.equals("All Statuses")
                    || selectedStatus.equalsIgnoreCase(status);

            if (matchesSearch && matchesStatus) {

                Date submittedAt = application.getDate("submittedAt");

                String dateText = submittedAt == null
                        ? "N/A"
                        : new SimpleDateFormat("dd MMM yyyy")
                                .format(submittedAt);

                tableModel.addRow(new Object[]{
                    applicationNumber,
                    applicantName,
                    serviceName,
                    dateText,
                    status.replace("_", " "),
                    getId(application)
                });
            }
        }

    }

    // ================= UPDATE SUMMARY =================

    private void updateSummary() {

        int submitted = 0;
        int underReview = 0;
        int approved = 0;
        int rejected = 0;

        for (Document application : allApplications) {

            String status =
                    getText(application, "applicationStatus");

            switch (status.toUpperCase()) {

                case "SUBMITTED":
                    submitted++;
                    break;

                case "UNDER_REVIEW":
                case "PENDING_DOCUMENTS":
                    underReview++;
                    break;

                case "APPROVED":
                    approved++;
                    break;

                case "REJECTED":
                    rejected++;
                    break;

                default:
                    break;
            }
        }

        totalLabel.setText(String.valueOf(allApplications.size()));
        submittedLabel.setText(String.valueOf(submitted));
        reviewLabel.setText(String.valueOf(underReview));
        approvedLabel.setText(String.valueOf(approved));
        rejectedLabel.setText(String.valueOf(rejected));
    }

    // ================= SELECTED APPLICATION =================

    private Document getSelectedApplication() {

        int selectedRow = applicationsTable.getSelectedRow();

        if (selectedRow < 0) {
            return null;
        }

        int modelRow =
                applicationsTable.convertRowIndexToModel(selectedRow);

        String applicationId =
                String.valueOf(tableModel.getValueAt(modelRow, 5));

        for (Document application : allApplications) {

            if (getId(application).equals(applicationId)) {
                return application;
            }
        }

        return null;
    }

    // ================= VIEW DETAILS =================

    private void viewSelectedApplication() {

        Document application = getSelectedApplication();

        if (application == null) {
            showMessage(
                    "Please select an application first.",
                    "No Application Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        StringBuilder details = new StringBuilder();

        details.append("APPLICATION DETAILS\n");
        details.append("==============================\n\n");

        details.append("Application Number: ")
                .append(getText(application, "applicationNumber"))
                .append("\n");

        details.append("Applicant Name: ")
                .append(getText(application, "applicantName"))
                .append("\n");

        details.append("Email: ")
                .append(getText(application, "applicantEmail"))
                .append("\n");

        details.append("Phone: ")
                .append(getText(application, "applicantPhone"))
                .append("\n");

        details.append("Service: ")
                .append(getServiceName(application))
                .append("\n");

        details.append("Application Status: ")
                .append(getText(application, "applicationStatus"))
                .append("\n");

        details.append("Payment Status: ")
                .append(getText(application, "paymentStatus"))
                .append("\n");

        details.append("Application Fee: ")
                .append(getText(application, "feeAmount"))
                .append("\n");

        details.append("Submitted At: ")
                .append(formatDate(application.getDate("submittedAt")))
                .append("\n");

        details.append("Last Updated: ")
                .append(formatDate(application.getDate("updatedAt")))
                .append("\n\n");

        details.append("ADMIN REMARKS\n");
        details.append("------------------------------\n");
        details.append(getText(application, "adminRemarks"));
        details.append("\n\n");

        details.append("SUBMITTED FORM DATA\n");
        details.append("------------------------------\n");

        Document formData = application.get("formData", Document.class);

        if (formData != null && !formData.isEmpty()) {

            for (String key : formData.keySet()) {
                details.append(key)
                        .append(": ")
                        .append(String.valueOf(formData.get(key)))
                        .append("\n");
            }

        } else {
            details.append("No additional form data.\n");
        }

        details.append("\nUPLOADED DOCUMENTS\n");
        details.append("------------------------------\n");

        List<Document> documents =
                application.getList("documents", Document.class);

        if (documents != null && !documents.isEmpty()) {

            for (Document document : documents) {
                details.append(document.toJson()).append("\n");
            }

        } else {
            details.append("No documents uploaded.\n");
        }

        details.append("\nSTATUS HISTORY\n");
        details.append("------------------------------\n");

        List<Document> history =
                application.getList("statusHistory", Document.class);

        if (history != null && !history.isEmpty()) {

            for (Document entry : history) {

                details.append("Status: ")
                        .append(getText(entry, "status"))
                        .append("\n");

                details.append("Remarks: ")
                        .append(getText(entry, "remarks"))
                        .append("\n");

                details.append("Date: ")
                        .append(formatDate(entry.getDate("changedAt")))
                        .append("\n\n");
            }

        } else {
            details.append("No status history available.\n");
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretPosition(0);
        textArea.setBackground(Theme.WHITE);
        textArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(650, 550));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Application Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ================= UPDATE APPLICATION STATUS =================

    private void updateSelectedApplication() {

        Document application = getSelectedApplication();

        if (application == null) {
            showMessage(
                    "Please select an application first.",
                    "No Application Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String applicationId = getId(application);

        JComboBox<String> statusCombo =
                new JComboBox<>(STATUSES);

        statusCombo.setSelectedItem(
                getText(application, "applicationStatus")
        );

        JTextArea remarksArea = new JTextArea(5, 30);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        remarksArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        remarksArea.setText(
                getText(application, "adminRemarks")
        );

        JPanel form = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Application Status:"), gbc);

        gbc.gridx = 1;
        form.add(statusCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Admin Remarks:"), gbc);

        gbc.gridx = 1;
        form.add(new JScrollPane(remarksArea), gbc);

        int choice = JOptionPane.showConfirmDialog(
                this,
                form,
                "Update Application Status",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String newStatus =
                (String) statusCombo.getSelectedItem();

        String remarks = remarksArea.getText().trim();

        if (newStatus == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Update this application's status to "
                        + newStatus.replace("_", " ") + "?",
                "Confirm Status Update",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setLoading(true);
        loadingLabel.setText("Updating application...");

        String adminId = UserSession.getUserId();

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() {

                return applicationRepository.updateApplicationStatus(
                        applicationId,
                        newStatus,
                        adminId,
                        remarks
                );
            }

            @Override
            protected void done() {

                try {

                    boolean updated = get();

                    if (updated) {

                        showMessage(
                                "Application status updated successfully.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        loadApplications();

                    } else {

                        showMessage(
                                "Application could not be updated.",
                                "Update Failed",
                                JOptionPane.ERROR_MESSAGE
                        );

                        setLoading(false);
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                    setLoading(false);

                    showMessage(
                            "Update was interrupted.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                } catch (ExecutionException ex) {

                    setLoading(false);

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

    // ================= BACK TO DASHBOARD =================

    private void openDashboard() {

        if (!UserSession.isAdmin()) {
            showMessage(
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (dashboardFrame != null
                && dashboardFrame.isDisplayable()) {

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

    // ================= HELPER METHODS =================

    private String getServiceName(Document application) {

        Object serviceId = application.get("serviceId");

        if (serviceId == null) {
            return "Unknown Service";
        }

        String id = serviceId instanceof ObjectId
                ? ((ObjectId) serviceId).toHexString()
                : serviceId.toString();

        return serviceNames.getOrDefault(id, "Unknown Service");
    }

    private String getId(Document document) {

        Object id = document.get("_id");

        if (id instanceof ObjectId) {
            return ((ObjectId) id).toHexString();
        }

        return id == null ? "" : id.toString();
    }

    private String getText(Document document, String field) {

        if (document == null) {
            return "";
        }

        Object value = document.get(field);

        return value == null ? "" : value.toString();
    }

    private String formatDate(Date date) {

        if (date == null) {
            return "N/A";
        }

        return new SimpleDateFormat("dd MMM yyyy, hh:mm a")
                .format(date);
    }

    private void setLoading(boolean loading) {

        if (refreshButton != null) {
            refreshButton.setEnabled(!loading);
        }

        if (viewButton != null) {
            viewButton.setEnabled(
                    !loading && applicationsTable.getSelectedRow() >= 0
            );
        }

        if (updateButton != null) {
            updateButton.setEnabled(
                    !loading && applicationsTable.getSelectedRow() >= 0
            );
        }
    }

    private void showMessage(
            String message,
            String title,
            int messageType
    ) {

        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                messageType
        );
    }
}

