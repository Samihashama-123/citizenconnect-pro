
package com.citizenconnect.ui;

import com.citizenconnect.config.MongoDBConnection;
import com.citizenconnect.config.UserSession;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

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

public class MyComplaintsFrame extends JFrame {

    private static final Color NAVY = new Color(16, 39, 67);
    private static final Color PRIMARY = new Color(23, 103, 194);
    private static final Color TEAL = new Color(13, 152, 127);
    private static final Color BACKGROUND = new Color(244, 247, 251);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(45, 55, 72);
    private static final Color MUTED = new Color(110, 120, 135);
    private static final Color BORDER = new Color(220, 226, 235);

    private JTable complaintTable;
    private DefaultTableModel tableModel;

    private JTextField searchField;
    private JComboBox<String> statusFilter;

    private JLabel totalLabel;
    private JLabel pendingLabel;
    private JLabel resolvedLabel;

    private JButton refreshButton;
    private JButton detailsButton;

    private List<Document> complaintList = new ArrayList<>();

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, hh:mm a");

    public MyComplaintsFrame() {

        if (!UserSession.isLoggedIn() || !UserSession.isCitizen()) {

            JOptionPane.showMessageDialog(
                    null,
                    "Please login as a citizen to view your complaints.",
                    "Access Denied",
                    JOptionPane.WARNING_MESSAGE
            );

            dispose();
            return;
        }

        setTitle("My Complaints - CitizenConnect");
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
        loadComplaints();
    }

    private void buildUI() {

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(25, 30, 25, 30));

        setContentPane(root);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("My Complaints");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "View and track the complaints you have submitted."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        heading.add(title);
        heading.add(Box.createVerticalStrut(7));
        heading.add(subtitle);

        refreshButton = new JButton("Refresh");
        styleButton(refreshButton, PRIMARY);

        refreshButton.addActionListener(e -> loadComplaints());

        header.add(heading, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // Main content
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setOpaque(false);

        // Summary cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setOpaque(false);

        totalLabel = createSummaryValue();
        pendingLabel = createSummaryValue();
        resolvedLabel = createSummaryValue();

        summaryPanel.add(
                createSummaryCard("Total Complaints", totalLabel, PRIMARY)
        );

        summaryPanel.add(
                createSummaryCard("Pending Complaints", pendingLabel,
                        new Color(220, 145, 30))
        );

        summaryPanel.add(
                createSummaryCard("Resolved Complaints", resolvedLabel, TEAL)
        );

        // Search and filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(WHITE);
        filterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        searchField = new JTextField(22);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(240, 38));

        JButton searchButton = new JButton("Search");
        styleButton(searchButton, PRIMARY);

        statusFilter = new JComboBox<>(new String[]{
                "All Statuses",
                "SUBMITTED",
                "UNDER_REVIEW",
                "IN_PROGRESS",
                "RESOLVED",
                "REJECTED"
        });

        statusFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusFilter.setPreferredSize(new Dimension(165, 38));

        searchButton.addActionListener(e -> applyFilters());

        searchField.addActionListener(e -> applyFilters());

        statusFilter.addActionListener(e -> applyFilters());

        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(searchField);
        filterPanel.add(searchButton);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilter);

        JPanel topContent = new JPanel(new BorderLayout(12, 12));
        topContent.setOpaque(false);

        topContent.add(summaryPanel, BorderLayout.NORTH);
        topContent.add(filterPanel, BorderLayout.SOUTH);

        content.add(topContent, BorderLayout.NORTH);

        // Table
        String[] columns = {
                "Complaint Number",
                "Subject",
                "Category",
                "Submitted Date",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        complaintTable = new JTable(tableModel);
        complaintTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        complaintTable.setRowHeight(38);
        complaintTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        complaintTable.setShowGrid(true);
        complaintTable.setGridColor(BORDER);
        complaintTable.setSelectionBackground(
                new Color(225, 238, 252)
        );

        complaintTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        complaintTable.getTableHeader().setBackground(NAVY);
        complaintTable.getTableHeader().setForeground(WHITE);
        complaintTable.getTableHeader().setPreferredSize(
                new Dimension(100, 40)
        );

        complaintTable.getColumnModel()
                .getColumn(0).setPreferredWidth(140);

        complaintTable.getColumnModel()
                .getColumn(1).setPreferredWidth(240);

        complaintTable.getColumnModel()
                .getColumn(2).setPreferredWidth(140);

        complaintTable.getColumnModel()
                .getColumn(3).setPreferredWidth(180);

        complaintTable.getColumnModel()
                .getColumn(4).setPreferredWidth(140);

        JScrollPane scrollPane = new JScrollPane(complaintTable);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        content.add(scrollPane, BorderLayout.CENTER);

        // Bottom buttons
        detailsButton = new JButton("View Complaint Details");
        styleButton(detailsButton, TEAL);

        detailsButton.addActionListener(e -> showSelectedDetails());

        complaintTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                detailsButton.setEnabled(
                        complaintTable.getSelectedRow() >= 0
                );
            }
        });

        detailsButton.setEnabled(false);

        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        bottomPanel.setOpaque(false);
        bottomPanel.add(detailsButton);

        content.add(bottomPanel, BorderLayout.SOUTH);

        root.add(content, BorderLayout.CENTER);
    }

    private JLabel createSummaryValue() {

        JLabel label = new JLabel("0");
        label.setFont(new Font("SansSerif", Font.BOLD, 27));
        label.setForeground(NAVY);

        return label;
    }

    private JPanel createSummaryCard(
            String heading,
            JLabel value,
            Color accent
    ) {

        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 4, 0, 0, accent
                ),
                new EmptyBorder(15, 18, 15, 18)
        ));

        JLabel title = new JLabel(heading);
        title.setFont(new Font("SansSerif", Font.PLAIN, 13));
        title.setForeground(MUTED);

        card.add(title, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);

        return card;
    }

    private void styleButton(JButton button, Color color) {

        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 18, 11, 18));
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setOpaque(true);
    }

    private void loadComplaints() {

        String userId = UserSession.getUserId();

        if (userId == null || !ObjectId.isValid(userId)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid citizen session. Please login again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        refreshButton.setEnabled(false);
        refreshButton.setText("Loading...");

        SwingWorker<List<Document>, Void> worker =
                new SwingWorker<>() {

            private String errorMessage;

            @Override
            protected List<Document> doInBackground() {

                try {

                    MongoCollection<Document> collection =
                            MongoDBConnection.getDatabase()
                                    .getCollection("complaints");

                    return collection.find(
                            Filters.eq(
                                    "userId",
                                    new ObjectId(userId)
                            )
                    ).sort(
                            Sorts.descending("submittedAt")
                    ).into(new ArrayList<>());

                } catch (Exception ex) {

                    errorMessage = ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {

                refreshButton.setEnabled(true);
                refreshButton.setText("Refresh");

                try {

                    List<Document> results = get();

                    if (results == null) {

                        JOptionPane.showMessageDialog(
                                MyComplaintsFrame.this,
                                "Could not load complaints:\n"
                                        + errorMessage,
                                "Database Error",
                                JOptionPane.ERROR_MESSAGE
                        );

                        return;
                    }

                    complaintList = results;

                    updateSummary();
                    applyFilters();

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            MyComplaintsFrame.this,
                            "Error loading complaints:\n"
                                    + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void updateSummary() {

        int total = complaintList.size();
        int pending = 0;
        int resolved = 0;

        for (Document complaint : complaintList) {

            String status = complaint.getString("status");

            if (status == null) {
                status = "";
            }

            if (status.equalsIgnoreCase("RESOLVED")) {
                resolved++;
            } else if (
                    !status.equalsIgnoreCase("REJECTED")
            ) {
                pending++;
            }
        }

        totalLabel.setText(String.valueOf(total));
        pendingLabel.setText(String.valueOf(pending));
        resolvedLabel.setText(String.valueOf(resolved));
    }

    private void applyFilters() {

        if (tableModel == null) {
            return;
        }

        tableModel.setRowCount(0);

        String search = searchField.getText()
                .trim()
                .toLowerCase();

        String selectedStatus =
                (String) statusFilter.getSelectedItem();

        for (Document complaint : complaintList) {

            String number = getString(
                    complaint, "complaintNumber"
            );

            String subject = getString(
                    complaint, "subject"
            );

            String category = getString(
                    complaint, "category"
            );

            String status = getString(
                    complaint, "status"
            );

            if (selectedStatus != null &&
                    !selectedStatus.equals("All Statuses") &&
                    !selectedStatus.equalsIgnoreCase(status)) {

                continue;
            }

            String searchableText =
                    (number + " " + subject + " " + category)
                            .toLowerCase();

            if (!searchableText.contains(search)) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    number,
                    subject,
                    category,
                    formatDate(complaint.getDate("submittedAt")),
                    status
            });
        }
    }

    private void showSelectedDetails() {

        int selectedRow = complaintTable.getSelectedRow();

        if (selectedRow < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a complaint first.",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        String complaintNumber = String.valueOf(
                tableModel.getValueAt(selectedRow, 0)
        );

        Document selectedComplaint = null;

        for (Document complaint : complaintList) {

            if (complaintNumber.equals(
                    getString(complaint, "complaintNumber")
            )) {

                selectedComplaint = complaint;
                break;
            }
        }

        if (selectedComplaint == null) {
            return;
        }

        showDetailsDialog(selectedComplaint);
    }

    private void showDetailsDialog(Document complaint) {

        JDialog dialog = new JDialog(
                this,
                "Complaint Details",
                true
        );

        dialog.setSize(600, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(15, 15));

        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(WHITE);
        main.setBorder(new EmptyBorder(22, 25, 22, 25));

        JLabel title = new JLabel("Complaint Information");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(NAVY);

        JPanel details = new JPanel(new GridLayout(0, 1, 5, 9));
        details.setBackground(WHITE);

        addDetail(details, "Complaint Number",
                getString(complaint, "complaintNumber"));

        addDetail(details, "Subject",
                getString(complaint, "subject"));

        addDetail(details, "Category",
                getString(complaint, "category"));

        addDetail(details, "Status",
                getString(complaint, "status"));

        addDetail(details, "Related Application",
                getString(complaint, "applicationNumber"));

        addDetail(details, "Submitted Date",
                formatDate(complaint.getDate("submittedAt")));

        addDetail(details, "Last Updated",
                formatDate(complaint.getDate("updatedAt")));

        JLabel descriptionTitle = new JLabel("Description");
        descriptionTitle.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        descriptionTitle.setForeground(NAVY);

        JTextArea description = new JTextArea(
                getString(complaint, "description")
        );

        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );
        description.setBackground(BACKGROUND);
        description.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel descriptionPanel = new JPanel(
                new BorderLayout(5, 5)
        );

        descriptionPanel.setBackground(WHITE);
        descriptionPanel.add(descriptionTitle, BorderLayout.NORTH);
        descriptionPanel.add(
                new JScrollPane(description),
                BorderLayout.CENTER
        );

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(details, BorderLayout.CENTER);

        main.add(top, BorderLayout.NORTH);
        main.add(descriptionPanel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        styleButton(closeButton, NAVY);
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        bottom.setBackground(WHITE);
        bottom.add(closeButton);

        dialog.add(main, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addDetail(
            JPanel panel,
            String label,
            String value
    ) {

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(WHITE);

        JLabel name = new JLabel(label + ":");
        name.setFont(new Font("SansSerif", Font.BOLD, 13));
        name.setForeground(MUTED);

        JLabel data = new JLabel(value);
        data.setFont(new Font("SansSerif", Font.PLAIN, 13));
        data.setForeground(TEXT);

        row.add(name, BorderLayout.WEST);
        row.add(data, BorderLayout.CENTER);

        panel.add(row);
    }

    private String getString(Document document, String field) {

        Object value = document.get(field);

        if (value == null) {
            return "Not available";
        }

        return value.toString();
    }

    private String formatDate(Date date) {

        if (date == null) {
            return "Not available";
        }

        return dateFormat.format(date);
    }
}
