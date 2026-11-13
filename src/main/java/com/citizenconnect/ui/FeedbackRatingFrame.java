
package com.citizenconnect.ui;

import com.citizenconnect.config.MongoDBConnection;
import com.citizenconnect.config.UserSession;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Date;

public class FeedbackRatingFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color NAVY = new Color(16, 39, 67);
    private static final Color BLUE = new Color(23, 103, 194);
    private static final Color TEAL = new Color(13, 152, 127);
    private static final Color BACKGROUND = new Color(244, 247, 251);
    private static final Color TEXT = new Color(45, 55, 72);
    private static final Color MUTED = new Color(110, 120, 135);
    private static final Color BORDER = new Color(220, 228, 238);

    private JComboBox<Integer> ratingCombo;
    private JComboBox<String> categoryCombo;
    private JTextArea feedbackArea;
    private JButton submitButton;
    private JLabel ratingDescription;

    public FeedbackRatingFrame() {
        setTitle("CitizenConnect - Feedback & Rating");
        setSize(850, 720);
        setMinimumSize(new Dimension(700, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        if (!UserSession.isLoggedIn() || !UserSession.isCitizen()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please login with a citizen account to submit feedback.",
                    "Access Denied",
                    JOptionPane.WARNING_MESSAGE
            );
            dispose();
            return;
        }

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Feedback & Rating");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Your feedback helps us improve CitizenConnect services."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(220, 230, 242));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(47, 68, 94));
        closeButton.setFocusPainted(false);
        closeButton.setBorder(new EmptyBorder(10, 18, 10, 18));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());

        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainContent() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BACKGROUND);
        outer.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(28, 32, 28, 32)
        ));

        JLabel welcome = new JLabel("We value your opinion!");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 21));
        welcome.setForeground(NAVY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel instruction = new JLabel(
                "Please rate your experience and share your suggestions."
        );
        instruction.setFont(new Font("SansSerif", Font.PLAIN, 13));
        instruction.setForeground(MUTED);
        instruction.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(welcome);
        card.add(Box.createVerticalStrut(8));
        card.add(instruction);
        card.add(Box.createVerticalStrut(27));

        card.add(createSectionLabel("Rate Your Experience"));
        card.add(Box.createVerticalStrut(10));

        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        ratingPanel.setOpaque(false);
        ratingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        ratingCombo = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        ratingCombo.setFont(new Font("SansSerif", Font.BOLD, 15));
        ratingCombo.setPreferredSize(new Dimension(100, 40));
        ratingCombo.setBackground(Color.WHITE);
        ratingCombo.setFocusable(false);

        ratingDescription = new JLabel("Excellent");
        ratingDescription.setFont(new Font("SansSerif", Font.BOLD, 14));
        ratingDescription.setForeground(TEAL);

        ratingCombo.addActionListener(e -> updateRatingDescription());

        ratingPanel.add(ratingCombo);
        ratingPanel.add(ratingDescription);

        card.add(ratingPanel);
        card.add(Box.createVerticalStrut(22));

        card.add(createSectionLabel("Feedback Category"));
        card.add(Box.createVerticalStrut(9));

        categoryCombo = new JComboBox<>(new String[]{
                "General Feedback",
                "Website / Application",
                "Public Services",
                "User Experience",
                "Suggestions",
                "Other"
        });
        categoryCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        categoryCombo.setPreferredSize(new Dimension(300, 42));
        categoryCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        categoryCombo.setBackground(Color.WHITE);
        categoryCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(categoryCombo);
        card.add(Box.createVerticalStrut(22));

        card.add(createSectionLabel("Your Comments"));
        card.add(Box.createVerticalStrut(9));

        feedbackArea = new JTextArea(6, 30);
        feedbackArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        feedbackArea.setForeground(TEXT);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setBorder(new EmptyBorder(12, 12, 12, 12));
        feedbackArea.setBackground(new Color(250, 252, 255));

        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setPreferredSize(new Dimension(500, 145));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));

        card.add(scrollPane);
        card.add(Box.createVerticalStrut(8));

        JLabel helper = new JLabel(
                "Please write at least 10 characters (maximum 2000)."
        );
        helper.setFont(new Font("SansSerif", Font.PLAIN, 11));
        helper.setForeground(MUTED);
        helper.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(helper);
        card.add(Box.createVerticalStrut(25));

        submitButton = new JButton("Submit Feedback");
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitButton.setForeground(Color.WHITE);
        submitButton.setBackground(BLUE);
        submitButton.setFocusPainted(false);
        submitButton.setBorder(new EmptyBorder(13, 25, 13, 25));
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitButton.addActionListener(e -> submitFeedback());

        card.add(submitButton);

        JLabel privacyNote = new JLabel(
                "Your feedback will be associated with your citizen account."
        );
        privacyNote.setFont(new Font("SansSerif", Font.PLAIN, 11));
        privacyNote.setForeground(MUTED);
        privacyNote.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(Box.createVerticalStrut(15));
        card.add(privacyNote);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);

        outer.add(card, gbc);

        JScrollPane pageScroll = new JScrollPane(outer);
        pageScroll.setBorder(null);
        pageScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(pageScroll, BorderLayout.CENTER);
        wrapper.setBackground(BACKGROUND);

        return wrapper;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(NAVY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void updateRatingDescription() {
        int rating = (Integer) ratingCombo.getSelectedItem();

        String description;
        Color color;

        switch (rating) {
            case 5:
                description = "Excellent";
                color = TEAL;
                break;
            case 4:
                description = "Very Good";
                color = new Color(23, 140, 100);
                break;
            case 3:
                description = "Good";
                color = new Color(190, 140, 20);
                break;
            case 2:
                description = "Fair";
                color = new Color(210, 110, 30);
                break;
            default:
                description = "Poor";
                color = new Color(200, 65, 65);
                break;
        }

        ratingDescription.setText(description);
        ratingDescription.setForeground(color);
    }

    private void submitFeedback() {
        String comments = feedbackArea.getText().trim();
        Integer rating = (Integer) ratingCombo.getSelectedItem();
        String category = (String) categoryCombo.getSelectedItem();

        if (rating == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a rating.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (comments.length() < 10) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please write at least 10 characters in your feedback.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE
            );
            feedbackArea.requestFocus();
            return;
        }

        if (comments.length() > 2000) {
            JOptionPane.showMessageDialog(
                    this,
                    "Feedback cannot exceed 2000 characters.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String userId = UserSession.getUserId();

        if (userId == null || !ObjectId.isValid(userId)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Your login session is invalid. Please login again.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to submit your feedback?",
                "Confirm Submission",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        setFormEnabled(false);
        submitButton.setText("Submitting...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() {
                MongoCollection<Document> collection =
                        MongoDBConnection.getDatabase()
                                .getCollection("feedback");

                ObjectId feedbackId = new ObjectId();

                Document feedback = new Document("_id", feedbackId)
                        .append("feedbackNumber",
                                "FDB-" + feedbackId.toHexString()
                                        .substring(0, 8).toUpperCase())
                        .append("userId", new ObjectId(userId))
                        .append("rating", rating)
                        .append("category", category)
                        .append("comments", comments)
                        .append("submittedAt", new Date());

                collection.insertOne(feedback);

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    JOptionPane.showMessageDialog(
                            FeedbackRatingFrame.this,
                            "Thank you! Your feedback has been submitted successfully.",
                            "Submission Successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    feedbackArea.setText("");
                    ratingCombo.setSelectedItem(5);
                    categoryCombo.setSelectedIndex(0);
                    updateRatingDescription();

                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    String message = cause != null
                            ? cause.getMessage()
                            : ex.getMessage();

                    JOptionPane.showMessageDialog(
                            FeedbackRatingFrame.this,
                            "Could not submit feedback.\n" + message,
                            "Submission Failed",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {
                    setFormEnabled(true);
                    submitButton.setText("Submit Feedback");
                }
            }
        };

        worker.execute();
    }

    private void setFormEnabled(boolean enabled) {
        ratingCombo.setEnabled(enabled);
        categoryCombo.setEnabled(enabled);
        feedbackArea.setEnabled(enabled);
        submitButton.setEnabled(enabled);
    }
}
