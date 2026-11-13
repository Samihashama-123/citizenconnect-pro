
package com.citizenconnect.ui;

import com.citizenconnect.config.MongoDBConnection;
import com.citizenconnect.config.UserSession;
import com.citizenconnect.theme.Theme;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmergencyAlertsFrame extends JFrame {

    private final JPanel alertsPanel = new JPanel();
    private final JTextField searchField = new JTextField(18);

    private final JComboBox<String> priorityFilter =
            new JComboBox<>(new String[]{
                    "ALL PRIORITIES",
                    "CRITICAL",
                    "HIGH",
                    "MEDIUM",
                    "LOW"
            });

    private final JButton refreshButton = new JButton("Refresh");

    private List<Document> alerts = new ArrayList<>();

    public EmergencyAlertsFrame() {

        if (!UserSession.isLoggedIn()
                || !UserSession.isCitizen()) {
            throw new IllegalStateException(
                    "Please login as a citizen first."
            );
        }

        setTitle("Emergency Alerts - CitizenConnect");
        setSize(1100, 750);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeUI();
        loadAlerts();
    }

    private void initializeUI() {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BACKGROUND);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.NAVY);
        header.setBorder(new EmptyBorder(24, 30, 24, 30));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(
                heading, BoxLayout.Y_AXIS
        ));

        JLabel title = new JLabel("Emergency Alerts");
        title.setFont(Theme.TITLE);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Important emergency announcements and public safety updates"
        );
        subtitle.setFont(Theme.BODY);
        subtitle.setForeground(Color.WHITE);

        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);

        styleButton(refreshButton, Theme.TEAL);
        refreshButton.addActionListener(e -> loadAlerts());

        header.add(heading, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        // Filter section
        JPanel filterPanel = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 12, 12
        ));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

        searchField.setFont(Theme.BODY);
        searchField.setPreferredSize(new Dimension(230, 36));
        searchField.setToolTipText("Search emergency alerts");

        priorityFilter.setFont(Theme.BODY);
        priorityFilter.setPreferredSize(new Dimension(170, 36));

        JButton searchButton = new JButton("Search");
        styleButton(searchButton, Theme.PRIMARY);

        searchButton.addActionListener(e -> filterAlerts());
        searchField.addActionListener(e -> filterAlerts());
        priorityFilter.addActionListener(e -> filterAlerts());

        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(searchField);
        filterPanel.add(priorityFilter);
        filterPanel.add(searchButton);

        // Alert list
        alertsPanel.setLayout(new BoxLayout(
                alertsPanel, BoxLayout.Y_AXIS
        ));
        alertsPanel.setBackground(Theme.BACKGROUND);
        alertsPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JScrollPane scrollPane = new JScrollPane(alertsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Theme.BACKGROUND);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void loadAlerts() {

        alertsPanel.removeAll();
        showLoading();

        refreshButton.setEnabled(false);

        SwingWorker<List<Document>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<Document> doInBackground() {

                MongoDatabase database =
                        MongoDBConnection.getDatabase();

                MongoCollection<Document> collection =
                        database.getCollection("emergency_alerts");

                List<Document> results = new ArrayList<>();

                for (Document document : collection.find()
                        .sort(new Document("publishedAt", -1))) {
                    results.add(document);
                }

                return results;
            }

            @Override
            protected void done() {

                refreshButton.setEnabled(true);

                try {
                    alerts = get();
                    filterAlerts();

                } catch (Exception ex) {

                    alertsPanel.removeAll();

                    showEmptyState(
                            "Unable to load emergency alerts",
                            getErrorMessage(ex)
                    );

                    alertsPanel.revalidate();
                    alertsPanel.repaint();
                }
            }
        };

        worker.execute();
    }

    private void filterAlerts() {

        alertsPanel.removeAll();

        String keyword = searchField.getText()
                .trim().toLowerCase(Locale.ROOT);

        String selectedPriority =
                (String) priorityFilter.getSelectedItem();

        List<Document> filtered = new ArrayList<>();

        for (Document alert : alerts) {

            String title = getFirstText(
                    alert, "title", "alertTitle"
            );

            String description = getFirstText(
                    alert, "description", "message"
            );

            String priority = getPriority(alert);

            boolean matchesSearch =
                    keyword.isEmpty()
                    || title.toLowerCase(Locale.ROOT)
                            .contains(keyword)
                    || description.toLowerCase(Locale.ROOT)
                            .contains(keyword);

            boolean matchesPriority =
                    selectedPriority == null
                    || selectedPriority.equals("ALL PRIORITIES")
                    || priority.equalsIgnoreCase(selectedPriority);

            if (matchesSearch && matchesPriority) {
                filtered.add(alert);
            }
        }

        if (filtered.isEmpty()) {

            if (alerts.isEmpty()) {
                showEmptyState(
                        "No Emergency Alerts",
                        "There are currently no emergency announcements."
                );
            } else {
                showEmptyState(
                        "No Matching Alerts",
                        "Try another keyword or priority filter."
                );
            }

        } else {

            for (Document alert : filtered) {
                alertsPanel.add(createAlertCard(alert));
                alertsPanel.add(Box.createVerticalStrut(14));
            }
        }

        alertsPanel.revalidate();
        alertsPanel.repaint();
    }

    private JPanel createAlertCard(Document alert) {

        String titleText = getFirstText(
                alert, "title", "alertTitle"
        );

        String description = getFirstText(
                alert, "description", "message"
        );

        String priority = getPriority(alert);

        String date = formatDate(
                getFirstValue(
                        alert,
                        "publishedAt",
                        "createdAt",
                        "issuedAt"
                )
        );

        String status = getFirstText(
                alert, "status"
        );

        JPanel card = new JPanel(new BorderLayout(12, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(18, 20, 18, 20)
        ));

        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 210)
        );

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel(titleText);
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.NAVY);

        JLabel priorityBadge = new JLabel(
                "  " + priority + "  "
        );
        priorityBadge.setOpaque(true);
        priorityBadge.setBackground(getPriorityColor(priority));
        priorityBadge.setForeground(Color.WHITE);
        priorityBadge.setFont(Theme.BUTTON);
        priorityBadge.setBorder(new EmptyBorder(6, 8, 6, 8));

        top.add(title, BorderLayout.CENTER);
        top.add(priorityBadge, BorderLayout.EAST);

        JTextArea descriptionArea = new JTextArea(description);
        descriptionArea.setFont(Theme.BODY);
        descriptionArea.setForeground(Theme.TEXT);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setRows(2);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JLabel dateLabel = new JLabel("Date: " + date);
        dateLabel.setFont(Theme.SMALL);
        dateLabel.setForeground(Theme.MUTED_TEXT);

        JLabel statusLabel = new JLabel(
                "Status: " + status
        );
        statusLabel.setFont(Theme.SMALL);
        statusLabel.setForeground(Theme.MUTED_TEXT);

        JButton detailsButton =
                new JButton("View Full Alert");

        styleButton(detailsButton, Theme.PRIMARY);
        detailsButton.addActionListener(
                e -> showAlertDetails(alert)
        );

        JPanel metadata = new JPanel();
        metadata.setOpaque(false);
        metadata.setLayout(new BoxLayout(
                metadata, BoxLayout.Y_AXIS
        ));

        metadata.add(dateLabel);
        metadata.add(Box.createVerticalStrut(4));
        metadata.add(statusLabel);

        bottom.add(metadata, BorderLayout.WEST);
        bottom.add(detailsButton, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(descriptionArea, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private void showAlertDetails(Document alert) {

        JDialog dialog = new JDialog(
                this,
                "Emergency Alert Details",
                true
        );

        dialog.setSize(650, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setBackground(Theme.NAVY);
        header.setBorder(new EmptyBorder(20, 22, 20, 22));
        header.setLayout(new BoxLayout(
                header, BoxLayout.Y_AXIS
        ));

        JLabel title = new JLabel(
                getFirstText(alert, "title", "alertTitle")
        );
        title.setFont(Theme.HEADING);
        title.setForeground(Color.WHITE);

        JLabel priority = new JLabel(
                "Priority: " + getPriority(alert)
        );
        priority.setFont(Theme.BODY);
        priority.setForeground(Color.WHITE);

        JLabel date = new JLabel(
                "Published: " + formatDate(
                        getFirstValue(
                                alert,
                                "publishedAt",
                                "createdAt",
                                "issuedAt"
                        )
                )
        );
        date.setFont(Theme.SMALL);
        date.setForeground(Color.WHITE);

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(priority);
        header.add(Box.createVerticalStrut(5));
        header.add(date);

        String description = getFirstText(
                alert, "description", "message"
        );

        Object details = getFirstValue(
                alert, "content", "details"
        );

        if (details != null) {
            description = details.toString();
        }

        JTextArea content = new JTextArea(description);
        content.setFont(Theme.BODY);
        content.setForeground(Theme.TEXT);
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setEditable(false);
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);

        JButton closeButton = new JButton("Close");
        styleButton(closeButton, Theme.TEAL);
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel(new FlowLayout(
                FlowLayout.RIGHT
        ));
        bottom.setBackground(Theme.BACKGROUND);
        bottom.setBorder(new EmptyBorder(10, 15, 10, 15));
        bottom.add(closeButton);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private Color getPriorityColor(String priority) {

        return switch (priority.toUpperCase(Locale.ROOT)) {
            case "CRITICAL" -> new Color(170, 25, 35);
            case "HIGH" -> new Color(210, 75, 45);
            case "MEDIUM" -> new Color(205, 135, 25);
            case "LOW" -> Theme.TEAL;
            default -> Theme.MUTED_TEXT;
        };
    }

    private String getPriority(Document alert) {

        return getFirstText(
                alert, "severity", "priority"
        ).toUpperCase(Locale.ROOT);
    }

    private Object getFirstValue(
            Document document,
            String... keys
    ) {

        for (String key : keys) {
            Object value = document.get(key);

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private String getFirstText(
            Document document,
            String... keys
    ) {

        Object value = getFirstValue(document, keys);

        return value == null
                ? "Not available"
                : value.toString();
    }

    private String formatDate(Object value) {

        if (value == null) {
            return "Date not available";
        }

        try {

            if (value instanceof Date date) {
                return new SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a"
                ).format(date);
            }

            return value.toString();

        } catch (Exception ex) {
            return value.toString();
        }
    }

    private void showLoading() {

        JLabel loading = new JLabel(
                "Loading emergency alerts..."
        );

        loading.setFont(Theme.BODY);
        loading.setForeground(Theme.MUTED_TEXT);

        alertsPanel.add(loading);
        alertsPanel.revalidate();
        alertsPanel.repaint();
    }

    private void showEmptyState(
            String titleText,
            String description
    ) {

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(35, 25, 35, 25));
        panel.setLayout(new BoxLayout(
                panel, BoxLayout.Y_AXIS
        ));

        JLabel title = new JLabel(titleText);
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.NAVY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel(description);
        message.setFont(Theme.BODY);
        message.setForeground(Theme.MUTED_TEXT);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(12));
        panel.add(message);

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 160)
        );

        alertsPanel.add(panel);
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

    private void styleButton(
            JButton button,
            Color background
    ) {

        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFont(Theme.BUTTON);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}