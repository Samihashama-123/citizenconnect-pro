
package com.citizenconnect.ui;

import com.citizenconnect.config.MongoDBConnection;
import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.ApplicationRepository;
import com.citizenconnect.theme.Theme;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MyApplicationsFrame extends JFrame {

    private final ApplicationRepository repository =
            new ApplicationRepository();

    private DefaultTableModel tableModel;
    private JTable applicationTable;

    private JLabel totalLabel;
    private JLabel pendingLabel;
    private JLabel approvedLabel;

    public MyApplicationsFrame() {

        if (!UserSession.isLoggedIn()
                || !UserSession.isCitizen()) {
            throw new IllegalStateException(
                    "Please login as a citizen first."
            );
        }

        setTitle("My Applications - CitizenConnect");
        setSize(1100, 700);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeUI();
        loadApplications();
    }

    private void initializeUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(Theme.BACKGROUND);
        mainPanel.setBorder(
                new EmptyBorder(25, 30, 25, 30)
        );

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("My Applications");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.NAVY);

        JLabel subtitle = new JLabel(
                "View and track all your submitted applications."
        );
        subtitle.setFont(Theme.BODY);
        subtitle.setForeground(Theme.MUTED_TEXT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(subtitle);

        JButton refreshButton = createButton(
                "Refresh", Theme.PRIMARY
        );

        refreshButton.addActionListener(
                e -> loadApplications()
        );

        header.add(titlePanel, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        // Summary cards
        JPanel summaryPanel = new JPanel(
                new GridLayout(1, 3, 15, 0)
        );
        summaryPanel.setOpaque(false);

        totalLabel = createSummaryCard(
                summaryPanel,
                "Total Applications",
                Theme.PRIMARY
        );

        pendingLabel = createSummaryCard(
                summaryPanel,
                "Pending Applications",
                new Color(217, 145, 35)
        );

        approvedLabel = createSummaryCard(
                summaryPanel,
                "Approved Applications",
                Theme.TEAL
        );

        // Applications table
        String[] columns = {
                "Application Number",
                "Service",
                "Submission Date",
                "Application Status",
                "Payment Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(
                    int row, int column
            ) {
                return false;
            }
        };

        applicationTable = new JTable(tableModel);
        applicationTable.setFont(Theme.BODY);
        applicationTable.setRowHeight(40);
        applicationTable.setSelectionBackground(Theme.LIGHT_BLUE);
        applicationTable.setSelectionForeground(Theme.TEXT);
        applicationTable.setGridColor(Theme.BORDER);
        applicationTable.setShowVerticalLines(false);

        applicationTable.getTableHeader().setFont(Theme.BUTTON);
        applicationTable.getTableHeader().setBackground(Theme.NAVY);
        applicationTable.getTableHeader().setForeground(Theme.WHITE);
        applicationTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(
                applicationTable
        );

        scrollPane.setBorder(
                BorderFactory.createLineBorder(Theme.BORDER)
        );

        // View details button
        JButton detailsButton = createButton(
                "View Details", Theme.TEAL
        );

        detailsButton.addActionListener(
                e -> showSelectedApplication()
        );

        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        bottomPanel.setOpaque(false);
        bottomPanel.add(detailsButton);

        JPanel contentPanel = new JPanel(
                new BorderLayout(15, 20)
        );
        contentPanel.setOpaque(false);

        contentPanel.add(
                summaryPanel, BorderLayout.NORTH
        );

        contentPanel.add(
                scrollPane, BorderLayout.CENTER
        );

        contentPanel.add(
                bottomPanel, BorderLayout.SOUTH
        );

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JLabel createSummaryCard(
            JPanel parent,
            String heading,
            Color accent
    ) {

        JPanel card = new JPanel();
        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBackground(Theme.WHITE);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.BORDER),
                        new EmptyBorder(18, 20, 18, 20)
                )
        );

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(Theme.SMALL);
        headingLabel.setForeground(Theme.MUTED_TEXT);

        JLabel valueLabel = new JLabel("0");
        valueLabel.setFont(Theme.TITLE);
        valueLabel.setForeground(accent);

        card.add(headingLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);

        parent.add(card);

        return valueLabel;
    }

    private JButton createButton(
            String text,
            Color background
    ) {

        JButton button = new JButton(text);
        button.setFont(Theme.BUTTON);
        button.setBackground(background);
        button.setForeground(Theme.WHITE);
        button.setFocusPainted(false);
        button.setBorder(
                new EmptyBorder(10, 20, 10, 20)
        );
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    private void loadApplications() {

        try {

            String userId = UserSession.getUserId();

            List<Document> applications =
                    repository.getApplicationsByUserId(userId);

            tableModel.setRowCount(0);

            int pending = 0;
            int approved = 0;

            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("dd MMM yyyy");

            for (Document application : applications) {

                String applicationNumber =
                        application.getString("applicationNumber");

                ObjectId serviceId =
                        application.getObjectId("serviceId");

                String serviceName =
                        getServiceName(serviceId);

                Date submittedAt =
                        application.getDate("submittedAt");

                String submissionDate =
                        submittedAt == null
                                ? "N/A"
                                : dateFormat.format(submittedAt);

                String status =
                        application.getString("applicationStatus");

                String paymentStatus =
                        application.getString("paymentStatus");

                if (status == null) {
                    status = "UNKNOWN";
                }

                if ("SUBMITTED".equals(status)
                        || "UNDER_REVIEW".equals(status)
                        || "PENDING_DOCUMENTS".equals(status)) {
                    pending++;
                }

                if ("APPROVED".equals(status)) {
                    approved++;
                }

                tableModel.addRow(new Object[]{
                        applicationNumber,
                        serviceName,
                        submissionDate,
                        status.replace("_", " "),
                        paymentStatus == null
                                ? "N/A"
                                : paymentStatus
                });
            }

            totalLabel.setText(
                    String.valueOf(applications.size())
            );

            pendingLabel.setText(
                    String.valueOf(pending)
            );

            approvedLabel.setText(
                    String.valueOf(approved)
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load applications:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    private String getServiceName(ObjectId serviceId) {

        if (serviceId == null) {
            return "Unknown Service";
        }

        MongoCollection<Document> services =
                MongoDBConnection.getDatabase()
                        .getCollection("services");

        Document service = services.find(
                new Document("_id", serviceId)
        ).first();

        if (service == null) {
            return "Unknown Service";
        }

        String serviceName =
                service.getString("serviceName");

        return serviceName == null
                ? "Unknown Service"
                : serviceName;
    }

    private void showSelectedApplication() {

        int selectedRow =
                applicationTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an application first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String applicationNumber =
                String.valueOf(
                        tableModel.getValueAt(selectedRow, 0)
                );

        Document application =
                repository.getApplicationByNumberForUser(
                        applicationNumber,
                        UserSession.getUserId()
                );

        if (application == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Application details could not be found.",
                    "Not Found",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String remarks =
                application.getString("adminRemarks");

        if (remarks == null || remarks.isBlank()) {
            remarks = "No remarks available";
        }

        String details = """
                Application Number: %s

                Applicant Name: %s

                Applicant Email: %s

                Applicant Phone: %s

                Application Status: %s

                Payment Status: %s

                Application Fee: %s

                Admin Remarks: %s
                """.formatted(
                application.getString("applicationNumber"),
                application.getString("applicantName"),
                application.getString("applicantEmail"),
                application.getString("applicantPhone"),
                application.getString("applicationStatus"),
                application.getString("paymentStatus"),
                application.get("feeAmount"),
                remarks
        );

        JTextArea textArea = new JTextArea(details);
        textArea.setEditable(false);
        textArea.setFont(Theme.BODY);
        textArea.setBackground(Theme.BACKGROUND);
        textArea.setBorder(
                new EmptyBorder(15, 15, 15, 15)
        );

        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(480, 350));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Application Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
