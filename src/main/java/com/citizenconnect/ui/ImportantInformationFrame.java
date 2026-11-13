
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

public class ImportantInformationFrame extends JFrame {

    private final JPanel informationPanel = new JPanel();
    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> categoryFilter =
            new JComboBox<>(new String[]{
                    "All Categories",
                    "General",
                    "Public Service",
                    "Education",
                    "Health",
                    "Government",
                    "Other"
            });

    private final JButton refreshButton = new JButton("Refresh");

    private List<Document> informationList = new ArrayList<>();

    public ImportantInformationFrame() {

        if (!UserSession.isLoggedIn()
                || !UserSession.isCitizen()) {
            throw new IllegalStateException(
                    "Please login as a citizen first."
            );
        }

        setTitle("Important Information - CitizenConnect");
        setSize(1100, 750);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeUI();
        loadInformation();
    }

    private void initializeUI() {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.NAVY);
        header.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(
                heading, BoxLayout.Y_AXIS
        ));

        JLabel title = new JLabel("Important Information");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.WHITE);

        JLabel subtitle = new JLabel(
                "Stay informed about public services and announcements"
        );
        subtitle.setFont(Theme.BODY);
        subtitle.setForeground(Color.WHITE);

        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);

        styleButton(refreshButton, Theme.TEAL);
        refreshButton.addActionListener(e -> loadInformation());

        header.add(heading, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);

        JPanel filterPanel = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 12, 12
        ));
        filterPanel.setBackground(Theme.WHITE);
        filterPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

        searchField.setFont(Theme.BODY);
        searchField.setPreferredSize(new Dimension(250, 36));
        searchField.setToolTipText("Search information");

        categoryFilter.setFont(Theme.BODY);
        categoryFilter.setPreferredSize(new Dimension(180, 36));

        JButton searchButton = new JButton("Search");
        styleButton(searchButton, Theme.PRIMARY);

        searchButton.addActionListener(e -> filterInformation());

        searchField.addActionListener(e -> filterInformation());
        categoryFilter.addActionListener(e -> filterInformation());

        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(searchField);
        filterPanel.add(categoryFilter);
        filterPanel.add(searchButton);

        informationPanel.setLayout(new BoxLayout(
                informationPanel, BoxLayout.Y_AXIS
        ));
        informationPanel.setBackground(Theme.BACKGROUND);
        informationPanel.setBorder(
                new EmptyBorder(20, 25, 20, 25)
        );

        JScrollPane scrollPane = new JScrollPane(
                informationPanel
        );
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

    private void loadInformation() {

        informationPanel.removeAll();
        showLoading();

        refreshButton.setEnabled(false);

        SwingWorker<List<Document>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<Document> doInBackground() {

                MongoDatabase database =
                        MongoDBConnection.getDatabase();

                MongoCollection<Document> collection =
                        database.getCollection(
                                "important_information"
                        );

                List<Document> results = new ArrayList<>();

                for (Document document :
                        collection.find().sort(
                                new Document("publishedAt", -1)
                        )) {
                    results.add(document);
                }

                return results;
            }

            @Override
            protected void done() {

                refreshButton.setEnabled(true);

                try {

                    informationList = get();

                    filterInformation();

                } catch (Exception ex) {

                    informationPanel.removeAll();

                    showEmptyState(
                            "Unable to load information",
                            getErrorMessage(ex)
                    );

                    informationPanel.revalidate();
                    informationPanel.repaint();
                }
            }
        };

        worker.execute();
    }

    private void filterInformation() {

        informationPanel.removeAll();

        String keyword = searchField.getText()
                .trim().toLowerCase();

        String selectedCategory =
                (String) categoryFilter.getSelectedItem();

        List<Document> filtered = new ArrayList<>();

        for (Document document : informationList) {

            String title = getText(document, "title");
            String description =
                    getText(document, "description");

            String category =
                    getText(document, "category");

            boolean matchesSearch =
                    keyword.isEmpty()
                    || title.toLowerCase().contains(keyword)
                    || description.toLowerCase().contains(keyword);

            boolean matchesCategory =
                    selectedCategory == null
                    || selectedCategory.equals("All Categories")
                    || category.equalsIgnoreCase(selectedCategory);

            if (matchesSearch && matchesCategory) {
                filtered.add(document);
            }
        }

        if (filtered.isEmpty()) {

            if (informationList.isEmpty()) {
                showEmptyState(
                        "No Information Available",
                        "There are currently no published announcements."
                );
            } else {
                showEmptyState(
                        "No Matching Information",
                        "Try another search keyword or category."
                );
            }

        } else {

            for (Document document : filtered) {
                informationPanel.add(
                        createInformationCard(document)
                );

                informationPanel.add(
                        Box.createVerticalStrut(14)
                );
            }
        }

        informationPanel.revalidate();
        informationPanel.repaint();
    }

    private JPanel createInformationCard(Document document) {

        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(Theme.WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(18, 20, 18, 20)
        ));

        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 190)
        );

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(
                textPanel, BoxLayout.Y_AXIS
        ));

        String titleText = getText(document, "title");
        String description =
                getText(document, "description");
        String category =
                getText(document, "category");
        String date = formatDate(
                document.get("publishedAt")
        );

        JLabel title = new JLabel(titleText);
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.NAVY);

        JLabel categoryLabel = new JLabel(
                "Category: " + category
        );
        categoryLabel.setFont(Theme.SMALL);
        categoryLabel.setForeground(Theme.TEAL);

        JLabel dateLabel = new JLabel(
                "Published: " + date
        );
        dateLabel.setFont(Theme.SMALL);
        dateLabel.setForeground(Theme.MUTED_TEXT);

        JTextArea descriptionArea =
                new JTextArea(description);

        descriptionArea.setFont(Theme.BODY);
        descriptionArea.setForeground(Theme.TEXT);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setRows(2);

        JButton detailsButton = new JButton("Read Full Information");
        styleButton(detailsButton, Theme.PRIMARY);

        detailsButton.addActionListener(
                e -> showFullInformation(document)
        );

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(categoryLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(dateLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(descriptionArea);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(detailsButton, BorderLayout.SOUTH);

        return card;
    }

    private void showFullInformation(Document document) {

        JDialog dialog = new JDialog(
                this,
                "Information Details",
                true
        );

        dialog.setSize(650, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setBackground(Theme.NAVY);
        header.setBorder(new EmptyBorder(18, 22, 18, 22));
        header.setLayout(new BoxLayout(
                header, BoxLayout.Y_AXIS
        ));

        JLabel title = new JLabel(
                getText(document, "title")
        );
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.WHITE);

        JLabel category = new JLabel(
                "Category: " + getText(document, "category")
        );
        category.setFont(Theme.SMALL);
        category.setForeground(Color.WHITE);

        JLabel date = new JLabel(
                "Published: " +
                        formatDate(document.get("publishedAt"))
        );
        date.setFont(Theme.SMALL);
        date.setForeground(Color.WHITE);

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(category);
        header.add(Box.createVerticalStrut(4));
        header.add(date);

        String fullDescription =
                getText(document, "description");

        Object details = document.get("content");

        if (details != null) {
            fullDescription = details.toString();
        }

        JTextArea content = new JTextArea(fullDescription);
        content.setFont(Theme.BODY);
        content.setForeground(Theme.TEXT);
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setEditable(false);
        content.setBackground(Theme.WHITE);
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
        bottom.setBorder(new EmptyBorder(10, 15, 10, 15));
        bottom.setBackground(Theme.BACKGROUND);
        bottom.add(closeButton);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showLoading() {

        JLabel loading = new JLabel(
                "Loading important information..."
        );

        loading.setFont(Theme.BODY);
        loading.setForeground(Theme.MUTED_TEXT);

        informationPanel.add(loading);
        informationPanel.revalidate();
        informationPanel.repaint();
    }

    private void showEmptyState(
            String titleText,
            String description
    ) {

        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(Theme.WHITE);
        emptyPanel.setBorder(new EmptyBorder(35, 25, 35, 25));
        emptyPanel.setLayout(new BoxLayout(
                emptyPanel, BoxLayout.Y_AXIS
        ));

        JLabel title = new JLabel(titleText);
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.NAVY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel(description);
        message.setFont(Theme.BODY);
        message.setForeground(Theme.MUTED_TEXT);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyPanel.add(title);
        emptyPanel.add(Box.createVerticalStrut(12));
        emptyPanel.add(message);

        emptyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emptyPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 160)
        );

        informationPanel.add(emptyPanel);
    }

    private String getText(Document document, String key) {

        Object value = document.get(key);

        return value == null
                ? "Not available"
                : value.toString();
    }

    private String formatDate(Object value) {

        if (value == null) {
            return "Date not available";
        }

        try {

            Date date;

            if (value instanceof Date d) {
                date = d;
            } else if (value instanceof Number number) {
                date = new Date(number.longValue());
            } else {
                return value.toString();
            }

            return new SimpleDateFormat(
                    "dd MMM yyyy"
            ).format(date);

        } catch (Exception ex) {
            return value.toString();
        }
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
