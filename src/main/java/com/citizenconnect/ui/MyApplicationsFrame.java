
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MyApplicationsFrame extends JFrame {

    private final ApplicationRepository repository =
            new ApplicationRepository();

    private DefaultTableModel tableModel;
    private JTable applicationTable;

    private JLabel totalLabel;
    private JLabel pendingLabel;
    private JLabel approvedLabel;

    private JButton refreshButton;
    private JButton detailsButton;

    private boolean loading = false;

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

        // Load MongoDB data in the background.
        loadApplications();
    }

    // =========================================
    // INITIALIZE UI
    // =========================================

    private void initializeUI() {

        JPanel mainPanel = new JPanel(
                new BorderLayout(20, 20)
        );

        mainPanel.setBackground(Theme.BACKGROUND);

        mainPanel.setBorder(
                new EmptyBorder(25, 30, 25, 30)
        );

        // =====================================
        // HEADER
        // =====================================

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

        refreshButton = createButton(
                "Refresh",
                Theme.PRIMARY
        );

        refreshButton.addActionListener(
                e -> loadApplications()
        );

        header.add(titlePanel, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        // =====================================
        // SUMMARY CARDS
        // =====================================

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

        // =====================================
        // APPLICATION TABLE
        // =====================================

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
                    int row,
                    int column
            ) {
                return false;
            }
        };

        applicationTable = new JTable(tableModel);

        applicationTable.setFont(Theme.BODY);
        applicationTable.setRowHeight(40);

        applicationTable.setSelectionBackground(
                Theme.LIGHT_BLUE
        );

        applicationTable.setSelectionForeground(
                Theme.TEXT
        );

        applicationTable.setGridColor(Theme.BORDER);
        applicationTable.setShowVerticalLines(false);

        applicationTable.getTableHeader().setFont(
                Theme.BUTTON
        );

        applicationTable.getTableHeader().setBackground(
                Theme.NAVY
        );

        applicationTable.getTableHeader().setForeground(
                Theme.WHITE
        );

        applicationTable.getTableHeader().setReorderingAllowed(
                false
        );

        JScrollPane scrollPane = new JScrollPane(
                applicationTable
        );

        scrollPane.setBorder(
                BorderFactory.createLineBorder(Theme.BORDER)
        );

        // =====================================
        // VIEW DETAILS BUTTON
        // =====================================

        detailsButton = createButton(
                "View Details",
                Theme.TEAL
        );

        detailsButton.addActionListener(
                e -> showSelectedApplication()
        );

        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        bottomPanel.setOpaque(false);

        bottomPanel.add(detailsButton);

        // =====================================
        // CONTENT PANEL
        // =====================================

        JPanel contentPanel = new JPanel(
                new BorderLayout(15, 20)
        );

        contentPanel.setOpaque(false);

        contentPanel.add(
                summaryPanel,
                BorderLayout.NORTH
        );

        contentPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        contentPanel.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    // =========================================
    // SUMMARY CARD
    // =========================================

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
                        BorderFactory.createLineBorder(
                                Theme.BORDER
                        ),
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

    // =========================================
    // BUTTON
    // =========================================

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
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        return button;
    }

    // =========================================
    // LOAD APPLICATIONS
    // =========================================

    private void loadApplications() {

        if (loading) {
            return;
        }

        loading = true;

        refreshButton.setEnabled(false);
        detailsButton.setEnabled(false);

        refreshButton.setText("Loading...");

        tableModel.setRowCount(0);

        tableModel.addRow(new Object[]{
                "Loading applications...",
                "Please wait",
                "",
                "",
                ""
        });

        totalLabel.setText("...");
        pendingLabel.setText("...");
        approvedLabel.setText("...");

        SwingWorker<ApplicationsData, Void> worker =
                new SwingWorker<>() {

            @Override
            protected ApplicationsData doInBackground()
                    throws Exception {

                String userId = UserSession.getUserId();

                if (userId == null || userId.isBlank()) {

                    throw new IllegalStateException(
                            "Your login session has expired."
                    );
                }

                // Fetch applications from MongoDB.
                List<Document> applications =
                        repository.getApplicationsByUserId(userId);

                List<Object[]> rows = new ArrayList<>();

                int pending = 0;
                int approved = 0;

                SimpleDateFormat dateFormat =
                        new SimpleDateFormat("dd MMM yyyy");

                // Fetch service names in the background too.
                for (Document application : applications) {

                    String applicationNumber =
                            application.getString(
                                    "applicationNumber"
                            );

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
                            application.getString(
                                    "applicationStatus"
                            );

                    String paymentStatus =
                            application.getString(
                                    "paymentStatus"
                            );

                    if (status == null) {
                        status = "UNKNOWN";
                    }

                    if ("SUBMITTED".equalsIgnoreCase(status)
                            || "UNDER_REVIEW".equalsIgnoreCase(status)
                            || "PENDING_DOCUMENTS".equalsIgnoreCase(status)) {

                        pending++;
                    }

                    if ("APPROVED".equalsIgnoreCase(status)) {
                        approved++;
                    }

                    rows.add(new Object[]{
                            applicationNumber == null
                                    ? "N/A"
                                    : applicationNumber,

                            serviceName,

                            submissionDate,

                            status.replace("_", " "),

                            paymentStatus == null
                                    ? "N/A"
                                    : paymentStatus
                    });
                }

                return new ApplicationsData(
                        rows,
                        applications.size(),
                        pending,
                        approved
                );
            }

            @Override
            protected void done() {

                loading = false;

                refreshButton.setEnabled(true);
                detailsButton.setEnabled(true);

                refreshButton.setText("Refresh");

                try {

                    ApplicationsData data = get();

                    tableModel.setRowCount(0);

                    // Display application rows.
                    for (Object[] row : data.rows) {
                        tableModel.addRow(row);
                    }

                    // Update summary cards.
                    totalLabel.setText(
                            String.valueOf(data.total)
                    );

                    pendingLabel.setText(
                            String.valueOf(data.pending)
                    );

                    approvedLabel.setText(
                            String.valueOf(data.approved)
                    );

                    if (data.total == 0) {

                        tableModel.addRow(new Object[]{
                                "No applications found",
                                "",
                                "",
                                "",
                                ""
                        });
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showLoadingError(
                            "Application loading was interrupted."
                    );

                } catch (ExecutionException ex) {

                    Throwable cause = ex.getCause();

                    String message = cause == null
                            ? ex.getMessage()
                            : cause.getMessage();

                    showLoadingError(
                            message == null
                                    ? "Unknown database error."
                                    : message
                    );
                }
            }
        };

        worker.execute();
    }

    // =========================================
    // GET SERVICE NAME
    // =========================================

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

    // =========================================
    // LOADING ERROR
    // =========================================

    private void showLoadingError(String message) {

        tableModel.setRowCount(0);

        totalLabel.setText("0");
        pendingLabel.setText("0");
        approvedLabel.setText("0");

        tableModel.addRow(new Object[]{
                "Unable to load applications",
                "",
                "",
                "",
                ""
        });

        JOptionPane.showMessageDialog(
                this,
                "Unable to load applications:\n" + message,
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // =========================================
    // APPLICATION DETAILS
    // =========================================

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

        if (applicationNumber == null
                || applicationNumber.isBlank()
                || applicationNumber.equals("N/A")
                || applicationNumber.equals("Loading applications...")
                || applicationNumber.equals("No applications found")) {

            return;
        }

        detailsButton.setEnabled(false);

        SwingWorker<Document, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Document doInBackground() {

                // Verify that this application belongs
                // to the logged-in citizen.

                return repository.getApplicationByNumberForUser(
                        applicationNumber,
                        UserSession.getUserId()
                );
            }

            @Override
            protected void done() {

                detailsButton.setEnabled(true);

                try {

                    Document application = get();

                    if (application == null) {

                        JOptionPane.showMessageDialog(
                                MyApplicationsFrame.this,
                                "Application details could not be found.",
                                "Not Found",
                                JOptionPane.ERROR_MESSAGE
                        );

                        return;
                    }

                    ApplicationTrackingFrame trackingFrame =
                            new ApplicationTrackingFrame(
                                    applicationNumber
                            );

                    trackingFrame.setLocationRelativeTo(
                            MyApplicationsFrame.this
                    );

                    trackingFrame.setVisible(true);

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showDetailsError(ex.getMessage());

                } catch (ExecutionException ex) {

                    Throwable cause = ex.getCause();

                    showDetailsError(
                            cause == null
                                    ? ex.getMessage()
                                    : cause.getMessage()
                    );
                }
            }
        };

        worker.execute();
    }

    // =========================================
    // DETAILS ERROR
    // =========================================

    private void showDetailsError(String message) {

        JOptionPane.showMessageDialog(
                this,
                "Unable to open application details:\n"
                        + message,
                "Application Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // =========================================
    // APPLICATION DATA MODEL
    // =========================================

    private static class ApplicationsData {

        private final List<Object[]> rows;

        private final int total;
        private final int pending;
        private final int approved;

        private ApplicationsData(
                List<Object[]> rows,
                int total,
                int pending,
                int approved
        ) {

            this.rows = rows;

            this.total = total;
            this.pending = pending;
            this.approved = approved;
        }
    }
}
