
package com.citizenconnect.ui;

import com.citizenconnect.config.MongoDBConnection;
import com.citizenconnect.config.UserSession;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Date;

public class SubmitComplaintFrame extends JFrame {

    private JTextField subjectField;
    private JTextField applicationNumberField;
    private JComboBox<String> categoryBox;
    private JTextArea descriptionArea;

    private JButton submitButton;
    private JButton clearButton;

    private static final Color NAVY = new Color(16, 39, 67);
    private static final Color PRIMARY = new Color(23, 103, 194);
    private static final Color BACKGROUND = new Color(244, 247, 251);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(45, 55, 72);
    private static final Color MUTED = new Color(110, 120, 135);
    private static final Color BORDER = new Color(220, 226, 235);

    public SubmitComplaintFrame() {

        if (!UserSession.isLoggedIn() || !UserSession.isCitizen()) {

            JOptionPane.showMessageDialog(
                    null,
                    "Please login as a citizen to submit a complaint.",
                    "Access Denied",
                    JOptionPane.WARNING_MESSAGE
            );

            dispose();
            return;
        }

        setTitle("Submit Complaint - CitizenConnect");
        setSize(850, 720);
        setMinimumSize(new Dimension(750, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
    }

    private void buildUI() {

        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(25, 35, 25, 35));

        setContentPane(root);

        // Heading
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Submit a Complaint");
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Tell us about the issue you are experiencing."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        heading.add(title);
        heading.add(Box.createVerticalStrut(8));
        heading.add(subtitle);

        root.add(heading, BorderLayout.NORTH);

        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        subjectField = new JTextField();
        styleTextField(subjectField);

        categoryBox = new JComboBox<>(new String[]{
                "Select a category",
                "Public Service",
                "Staff Behaviour",
                "Service Delay",
                "Corruption",
                "Infrastructure",
                "Education",
                "Healthcare",
                "Other"
        });

        categoryBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        categoryBox.setBackground(WHITE);
        categoryBox.setPreferredSize(new Dimension(400, 40));

        applicationNumberField = new JTextField();
        styleTextField(applicationNumberField);

        descriptionArea = new JTextArea(7, 30);
        descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setForeground(TEXT);
        descriptionArea.setBorder(
                new EmptyBorder(10, 12, 10, 12)
        );

        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        addField(form, gbc, 0, "Complaint Subject", subjectField);

        addField(form, gbc, 2, "Complaint Category", categoryBox);

        addField(
                form,
                gbc,
                4,
                "Related Application Number (Optional)",
                applicationNumberField
        );

        gbc.gridy = 6;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        form.add(createLabel("Complaint Description"), gbc);

        gbc.gridy = 7;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        form.add(descriptionScroll, gbc);

        // Buttons
        clearButton = new JButton("Clear");
        styleButton(clearButton, NAVY);

        submitButton = new JButton("Submit Complaint");
        styleButton(submitButton, PRIMARY);

        clearButton.addActionListener(e -> clearForm());

        submitButton.addActionListener(e -> submitComplaint());

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 5)
        );

        buttonPanel.setOpaque(false);
        buttonPanel.add(clearButton);
        buttonPanel.add(submitButton);

        JPanel center = new JPanel(new BorderLayout(15, 15));
        center.setOpaque(false);

        center.add(form, BorderLayout.CENTER);
        center.add(buttonPanel, BorderLayout.SOUTH);

        root.add(center, BorderLayout.CENTER);
    }

    private void addField(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            JComponent field
    ) {

        gbc.gridy = row;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(createLabel(label), gbc);

        gbc.gridy = row + 1;

        field.setFont(new Font("SansSerif", Font.PLAIN, 14));

        if (field instanceof JTextField) {
            field.setPreferredSize(new Dimension(400, 40));
        }

        panel.add(field, gbc);
    }

    private JLabel createLabel(String text) {

        JLabel label = new JLabel(text);

        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(NAVY);

        return label;
    }

    private void styleTextField(JTextField field) {

        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setPreferredSize(new Dimension(400, 40));

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleButton(JButton button, Color background) {

        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(WHITE);
        button.setBackground(background);

        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 22, 12, 22));

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.setOpaque(true);
    }

    private void submitComplaint() {

        String subject = subjectField.getText().trim();

        String category =
                (String) categoryBox.getSelectedItem();

        String applicationNumber =
                applicationNumberField.getText().trim();

        String description =
                descriptionArea.getText().trim();

        String userId = UserSession.getUserId();

        // Subject validation
        if (subject.length() < 5 || subject.length() > 100) {

            showError(
                    "Subject must contain between 5 and 100 characters."
            );

            subjectField.requestFocus();
            return;
        }

        // Category validation
        if (category == null ||
                category.equals("Select a category")) {

            showError("Please select a complaint category.");

            categoryBox.requestFocus();
            return;
        }

        // Description validation
        if (description.length() < 20 ||
                description.length() > 2000) {

            showError(
                    "Description must contain between 20 and 2000 characters."
            );

            descriptionArea.requestFocus();
            return;
        }

        // User ID validation
        if (userId == null || !ObjectId.isValid(userId)) {

            showError(
                    "Invalid citizen account. Please login again."
            );

            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to submit this complaint?",
                "Confirm Complaint",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        setFormEnabled(false);

        submitButton.setText("Submitting...");

        SwingWorker<String, Void> worker =
                new SwingWorker<>() {

            private String errorMessage;

            @Override
            protected String doInBackground() {

                try {

                    ObjectId citizenId = new ObjectId(userId);

                    // Verify optional application number
                    if (!applicationNumber.isEmpty()) {

                        MongoCollection<Document> applications =
                                MongoDBConnection.getDatabase()
                                        .getCollection("applications");

                        Document application = applications.find(
                                Filters.and(
                                        Filters.eq(
                                                "applicationNumber",
                                                applicationNumber
                                        ),
                                        Filters.eq(
                                                "userId",
                                                citizenId
                                        )
                                )
                        ).first();

                        if (application == null) {

                            errorMessage =
                                    "Application not found or it does not belong to your account.";

                            return null;
                        }
                    }

                    // Generate complaint number
                    ObjectId complaintId = new ObjectId();

                    String complaintNumber =
                            "CMP-" + complaintId.toHexString()
                                    .substring(0, 8)
                                    .toUpperCase();

                    Date now = new Date();

                    // Complaint document
                    Document complaint = new Document()
                            .append("_id", complaintId)
                            .append("complaintNumber", complaintNumber)
                            .append("userId", citizenId)
                            .append("subject", subject)
                            .append("category", category)
                            .append("description", description)
                            .append(
                                    "applicationNumber",
                                    applicationNumber.isEmpty()
                                            ? null
                                            : applicationNumber
                            )
                            .append("status", "SUBMITTED")
                            .append("submittedAt", now)
                            .append("updatedAt", now);

                    // Insert into MongoDB
                    MongoCollection<Document> complaints =
                            MongoDBConnection.getDatabase()
                                    .getCollection("complaints");

                    complaints.insertOne(complaint);

                    return complaintNumber;

                } catch (Exception ex) {

                    errorMessage = ex.getMessage();

                    return null;
                }
            }

            @Override
            protected void done() {

                setFormEnabled(true);

                submitButton.setText("Submit Complaint");

                try {

                    String complaintNumber = get();

                    if (complaintNumber == null) {

                        showError(
                                errorMessage == null
                                        ? "Complaint submission failed."
                                        : errorMessage
                        );

                        return;
                    }

                    JOptionPane.showMessageDialog(
                            SubmitComplaintFrame.this,
                            "Your complaint has been submitted successfully!"
                                    + "\n\nComplaint Number: "
                                    + complaintNumber
                                    + "\nStatus: SUBMITTED",
                            "Complaint Submitted",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    clearForm();

                } catch (Exception ex) {

                    showError(
                            "Could not complete complaint submission: "
                                    + ex.getMessage()
                    );
                }
            }
        };

        worker.execute();
    }

    private void setFormEnabled(boolean enabled) {

        subjectField.setEnabled(enabled);
        categoryBox.setEnabled(enabled);
        applicationNumberField.setEnabled(enabled);
        descriptionArea.setEnabled(enabled);

        clearButton.setEnabled(enabled);
        submitButton.setEnabled(enabled);
    }

    private void clearForm() {

        subjectField.setText("");

        categoryBox.setSelectedIndex(0);

        applicationNumberField.setText("");

        descriptionArea.setText("");

        subjectField.requestFocus();
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Complaint Submission",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
