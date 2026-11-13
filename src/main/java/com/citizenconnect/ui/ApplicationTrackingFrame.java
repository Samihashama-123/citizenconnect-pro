
package com.citizenconnect.ui;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.ApplicationRepository;
import com.citizenconnect.theme.Theme;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class ApplicationTrackingFrame extends JFrame {

    private final ApplicationRepository repository =
            new ApplicationRepository();

    private final String applicationNumber;

    private final JPanel contentPanel = new JPanel();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public ApplicationTrackingFrame(String applicationNumber) {

        if (!UserSession.isLoggedIn() || !UserSession.isCitizen()) {
            throw new IllegalStateException(
                    "Please login as a citizen first."
            );
        }

        if (applicationNumber == null ||
                applicationNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Application number is required."
            );
        }

        this.applicationNumber = applicationNumber.trim();

        setTitle("Application Tracking - CitizenConnect");
        setSize(1000, 750);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeUI();
        loadApplication();
    }

    private void initializeUI() {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.NAVY);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JLabel title = new JLabel("Application Tracking");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.WHITE);

        JLabel subtitle = new JLabel(
                "View your application details and progress"
        );
        subtitle.setFont(Theme.BODY);
        subtitle.setForeground(Color.WHITE);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton, Theme.TEAL);
        refreshButton.addActionListener(e -> loadApplication());

        header.add(heading, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        contentPanel.setLayout(new BoxLayout(
                contentPanel, BoxLayout.Y_AXIS
        ));
        contentPanel.setBackground(Theme.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Theme.BACKGROUND);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void loadApplication() {

        contentPanel.removeAll();

        JLabel loading = new JLabel("Loading application details...");
        loading.setFont(Theme.BODY);
        contentPanel.add(loading);

        contentPanel.revalidate();
        contentPanel.repaint();

        SwingWorker<Document, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Document doInBackground() {

                return repository.getApplicationByNumberForUser(
                        applicationNumber,
                        UserSession.getUserId()
                );
            }

            @Override
            protected void done() {

                try {
                    Document application = get();

                    contentPanel.removeAll();

                    if (application == null) {
                        showMessage(
                                "Application not found",
                                "No application was found with this number."
                        );
                    } else {
                        displayApplication(application);
                    }

                } catch (Exception ex) {

                    contentPanel.removeAll();

                    showMessage(
                            "Unable to load application",
                            getErrorMessage(ex)
                    );
                }

                contentPanel.revalidate();
                contentPanel.repaint();
            }
        };

        worker.execute();
    }

    private void displayApplication(Document app) {

        String serviceName = getServiceName(app);

        addSectionTitle("Application Overview");

        addInfoCard("Application Number",
                getText(app, "applicationNumber"));

        addInfoCard("Service Name", serviceName);

        addInfoCard("Applicant Name",
                getText(app, "applicantName"));

        addInfoCard("Applicant Email",
                getText(app, "applicantEmail"));

        addInfoCard("Applicant Phone",
                getText(app, "applicantPhone"));

        addInfoCard("Submission Date",
                formatDate(app.get("submittedAt")));

        addSectionTitle("Application Status");

        String status = getText(app, "applicationStatus");
        String paymentStatus = getText(app, "paymentStatus");

        addStatusCard(status);
        addInfoCard("Payment Status", paymentStatus);

        addSectionTitle("Application Progress");

        addProgress(status);

        addSectionTitle("Application Details");

        Document formData = app.get("formData", Document.class);

        if (formData != null && !formData.isEmpty()) {

            for (String key : formData.keySet()) {
                Object value = formData.get(key);

                addInfoCard(
                        formatLabel(key),
                        value == null ? "Not provided" : value.toString()
                );
            }

        } else {
            addInfoCard(
                    "Additional Information",
                    "No additional application details available."
            );
        }

        addSectionTitle("Status History");

        displayStatusHistory(app);

        revalidate();
        repaint();
    }

    private String getServiceName(Document app) {

        try {

            Object serviceId = app.get("serviceId");

            if (serviceId == null) {
                return "Service information unavailable";
            }

            MongoDatabase database =
                    com.citizenconnect.config.MongoDBConnection.getDatabase();

            MongoCollection<Document> services =
                    database.getCollection("services");

            Document service = services.find(
                    new Document("_id", serviceId)
            ).first();

            if (service != null) {
                return getText(service, "serviceName");
            }

        } catch (Exception ignored) {
            // Do not expose database internals in the UI.
        }

        return "Service information unavailable";
    }

    private void displayStatusHistory(Document app) {

        List<Document> history = app.getList(
                "statusHistory",
                Document.class
        );

        if (history == null || history.isEmpty()) {

            addInfoCard(
                    "History",
                    "No status updates are available yet."
            );

            return;
        }

        for (Document entry : history) {

            String status = getText(entry, "status");

            String date = formatDate(
                    entry.get("updatedAt")
            );

            String note = getText(entry, "remarks");

            JPanel card = createCard();

            JLabel statusLabel = new JLabel(status);
            statusLabel.setFont(Theme.HEADING);
            statusLabel.setForeground(Theme.PRIMARY);

            JLabel dateLabel = new JLabel(date);
            dateLabel.setFont(Theme.SMALL);
            dateLabel.setForeground(Theme.MUTED_TEXT);

            JLabel noteLabel = new JLabel(note);
            noteLabel.setFont(Theme.BODY);
            noteLabel.setForeground(Theme.TEXT);

            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.add(statusLabel);
            card.add(Box.createVerticalStrut(5));
            card.add(dateLabel);
            card.add(Box.createVerticalStrut(5));
            card.add(noteLabel);

            contentPanel.add(card);
            contentPanel.add(Box.createVerticalStrut(10));
        }
    }

    private void addProgress(String status) {

        String[] steps = {
                "SUBMITTED",
                "UNDER_REVIEW",
                "APPROVED"
        };

        int currentStep = switch (status) {
            case "SUBMITTED" -> 0;
            case "UNDER_REVIEW", "PENDING_DOCUMENTS" -> 1;
            case "APPROVED" -> 2;
            default -> -1;
        };

        JPanel progressPanel = createCard();
        progressPanel.setLayout(new GridLayout(1, 3, 10, 10));

        for (int i = 0; i < steps.length; i++) {

            JLabel step = new JLabel(
                    "<html><center>" +
                    (i <= currentStep && currentStep >= 0 ? "✓ " : "○ ") +
                    steps[i].replace("_", " ") +
                    "</center></html>",
                    SwingConstants.CENTER
            );

            step.setOpaque(true);
            step.setFont(Theme.SMALL);
            step.setBorder(new EmptyBorder(12, 5, 12, 5));

            if (i <= currentStep && currentStep >= 0) {
                step.setBackground(Theme.TEAL);
                step.setForeground(Color.WHITE);
            } else {
                step.setBackground(Theme.LIGHT_BLUE);
                step.setForeground(Theme.TEXT);
            }

            progressPanel.add(step);
        }

        if ("REJECTED".equals(status)) {
            JLabel rejected = new JLabel(
                    "Application rejected",
                    SwingConstants.CENTER
            );
            rejected.setForeground(Color.RED);
            rejected.setFont(Theme.HEADING);
            progressPanel.add(rejected);
        }

        if ("PENDING_DOCUMENTS".equals(status)) {
            JLabel pending = new JLabel(
                    "Additional documents are required.",
                    SwingConstants.CENTER
            );
            pending.setForeground(Theme.TEXT);
            progressPanel.add(pending);
        }

        contentPanel.add(progressPanel);
        contentPanel.add(Box.createVerticalStrut(15));
    }

    private void addSectionTitle(String text) {

        JLabel label = new JLabel(text);
        label.setFont(Theme.HEADING);
        label.setForeground(Theme.NAVY);

        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(label);
        contentPanel.add(Box.createVerticalStrut(10));
    }

    private void addInfoCard(String label, String value) {

        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(label);
        name.setFont(Theme.SMALL);
        name.setForeground(Theme.MUTED_TEXT);

        JLabel data = new JLabel(
                "<html>" + escapeHtml(value) + "</html>"
        );
        data.setFont(Theme.BODY);
        data.setForeground(Theme.TEXT);

        card.add(name);
        card.add(Box.createVerticalStrut(5));
        card.add(data);

        contentPanel.add(card);
        contentPanel.add(Box.createVerticalStrut(10));
    }

    private void addStatusCard(String status) {

        JPanel card = createCard();
        card.setLayout(new BorderLayout());

        JLabel label = new JLabel(
                "Current Status: " + status.replace("_", " ")
        );

        label.setFont(Theme.HEADING);
        label.setForeground(getStatusColor(status));

        card.add(label, BorderLayout.CENTER);

        contentPanel.add(card);
        contentPanel.add(Box.createVerticalStrut(10));
    }

    private JPanel createCard() {

        JPanel panel = new JPanel();
        panel.setBackground(Theme.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(15, 18, 15, 18)
        ));

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, 180
        ));

        return panel;
    }

    private void showMessage(String title, String message) {

        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.NAVY);

        JLabel description = new JLabel(
                "<html>" + escapeHtml(message) + "</html>"
        );
        description.setFont(Theme.BODY);
        description.setForeground(Theme.MUTED_TEXT);

        card.add(heading);
        card.add(Box.createVerticalStrut(10));
        card.add(description);

        contentPanel.add(card);
    }

    private Color getStatusColor(String status) {

        return switch (status) {
            case "APPROVED" -> Theme.TEAL;
            case "REJECTED" -> new Color(190, 45, 45);
            case "PENDING_DOCUMENTS" -> new Color(190, 120, 20);
            case "UNDER_REVIEW" -> Theme.PRIMARY;
            default -> Theme.MUTED_TEXT;
        };
    }

    private String getText(Document document, String key) {

        Object value = document.get(key);

        if (value == null) {
            return "Not available";
        }

        return value.toString();
    }

    private String formatDate(Object value) {

        if (value == null) {
            return "Not available";
        }

        try {

            Instant instant;

            if (value instanceof Date date) {
                instant = date.toInstant();
            } else if (value instanceof Instant i) {
                instant = i;
            } else {
                return value.toString();
            }

            return LocalDateTime.ofInstant(
                    instant,
                    ZoneId.systemDefault()
            ).format(dateFormatter);

        } catch (Exception ex) {
            return value.toString();
        }
    }

    private String formatLabel(String key) {

        String formatted = key.replaceAll(
                "([a-z])([A-Z])", "$1 $2"
        );

        formatted = formatted.replace("_", " ");

        return formatted.substring(0, 1).toUpperCase()
                + formatted.substring(1);
    }

    private String escapeHtml(String text) {

        if (text == null) {
            return "";
        }

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String getErrorMessage(Exception ex) {

        Throwable cause = ex;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        return cause.getMessage() == null
                ? "An unexpected error occurred."
                : cause.getMessage();
    }

    private void styleButton(JButton button, Color color) {

        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(Theme.BUTTON);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
