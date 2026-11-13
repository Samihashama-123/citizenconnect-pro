
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

public class VolunteerRegistrationFrame extends JFrame {

    private static final Color NAVY = new Color(16, 39, 67);
    private static final Color PRIMARY = new Color(23, 103, 194);
    private static final Color TEAL = new Color(13, 152, 127);
    private static final Color BACKGROUND = new Color(244, 247, 251);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(45, 55, 72);
    private static final Color MUTED = new Color(110, 120, 135);
    private static final Color BORDER = new Color(220, 226, 235);

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;

    private JComboBox<String> areaBox;
    private JComboBox<String> availabilityBox;

    private JTextArea skillsArea;
    private JTextArea motivationArea;

    private JButton submitButton;
    private JButton clearButton;

    public VolunteerRegistrationFrame() {

        if (!UserSession.isLoggedIn() || !UserSession.isCitizen()) {

            JOptionPane.showMessageDialog(
                    null,
                    "Please login as a citizen to register as a volunteer.",
                    "Access Denied",
                    JOptionPane.WARNING_MESSAGE
            );

            dispose();
            return;
        }

        setTitle("Volunteer Registration - CitizenConnect");
        setSize(900, 800);
        setMinimumSize(new Dimension(750, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
    }

    private void buildUI() {

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(25, 35, 25, 35));

        setContentPane(root);

        // Header
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Become a Volunteer");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Join CitizenConnect and make a positive difference in your community."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        heading.add(title);
        heading.add(Box.createVerticalStrut(8));
        heading.add(subtitle);

        root.add(heading, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(22, 28, 22, 28));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 0, 7, 0);

        nameField = new JTextField(UserSession.getUserName() == null
                ? ""
                : UserSession.getUserName());

        emailField = new JTextField(UserSession.getUserEmail() == null
                ? ""
                : UserSession.getUserEmail());

        phoneField = new JTextField();
        addressField = new JTextField();

        styleTextField(nameField);
        styleTextField(emailField);
        styleTextField(phoneField);
        styleTextField(addressField);

        nameField.setEditable(false);
        emailField.setEditable(false);

        nameField.setBackground(new Color(240, 244, 249));
        emailField.setBackground(new Color(240, 244, 249));

        areaBox = new JComboBox<>(new String[]{
                "Select preferred area",
                "Education",
                "Healthcare",
                "Environment",
                "Disaster Management",
                "Community Development",
                "Public Awareness",
                "Other"
        });

        availabilityBox = new JComboBox<>(new String[]{
                "Select availability",
                "Weekdays",
                "Weekends",
                "Both Weekdays and Weekends",
                "Flexible"
        });

        styleComboBox(areaBox);
        styleComboBox(availabilityBox);

        skillsArea = createTextArea(3);
        motivationArea = createTextArea(4);

        addField(form, gbc, 0, "Full Name", nameField);
        addField(form, gbc, 2, "Email Address", emailField);
        addField(form, gbc, 4, "Phone Number", phoneField);
        addField(form, gbc, 6, "Address", addressField);
        addField(form, gbc, 8, "Preferred Volunteer Area", areaBox);
        addField(form, gbc, 10, "Availability", availabilityBox);

        addTextAreaField(
                form,
                gbc,
                12,
                "Skills and Experience",
                skillsArea
        );

        addTextAreaField(
                form,
                gbc,
                14,
                "Why do you want to become a volunteer?",
                motivationArea
        );

        // Buttons
        clearButton = new JButton("Clear");
        styleButton(clearButton, NAVY);

        submitButton = new JButton("Register as Volunteer");
        styleButton(submitButton, TEAL);

        clearButton.addActionListener(e -> clearForm());

        submitButton.addActionListener(e -> submitRegistration());

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 5)
        );

        buttonPanel.setOpaque(false);
        buttonPanel.add(clearButton);
        buttonPanel.add(submitButton);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);

        center.add(form, BorderLayout.CENTER);
        center.add(buttonPanel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(center);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND);

        root.add(scrollPane, BorderLayout.CENTER);
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

        field.setPreferredSize(new Dimension(400, 40));

        panel.add(field, gbc);
    }

    private void addTextAreaField(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            JTextArea area
    ) {

        gbc.gridy = row;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(createLabel(label), gbc);

        gbc.gridy = row + 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(400, 85));
        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        panel.add(scroll, gbc);

        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
    }

    private JLabel createLabel(String text) {

        JLabel label = new JLabel(text);

        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(NAVY);

        return label;
    }

    private JTextArea createTextArea(int rows) {

        JTextArea area = new JTextArea(rows, 30);

        area.setFont(new Font("SansSerif", Font.PLAIN, 14));
        area.setForeground(TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(10, 12, 10, 12));

        return area;
    }

    private void styleTextField(JTextField field) {

        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(TEXT);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleComboBox(JComboBox<String> combo) {

        combo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        combo.setBackground(WHITE);
        combo.setPreferredSize(new Dimension(400, 40));
    }

    private void styleButton(JButton button, Color color) {

        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 20, 12, 20));

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.setOpaque(true);
    }

    private void submitRegistration() {

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        String area = (String) areaBox.getSelectedItem();

        String availability =
                (String) availabilityBox.getSelectedItem();

        String skills = skillsArea.getText().trim();

        String motivation = motivationArea.getText().trim();

        String userId = UserSession.getUserId();

        // Name validation
        if (name.isBlank()) {
            showError("Your name is missing from your account.");
            return;
        }

        // Email validation
        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {

            showError("Please provide a valid email address.");
            return;
        }

        // Phone validation
        if (!phone.matches("[0-9+() -]{7,20}")) {

            showError("Please enter a valid phone number.");
            phoneField.requestFocus();
            return;
        }

        // Address validation
        if (address.length() < 5 || address.length() > 250) {

            showError(
                    "Address must contain between 5 and 250 characters."
            );

            addressField.requestFocus();
            return;
        }

        // Area validation
        if (area == null ||
                area.equals("Select preferred area")) {

            showError("Please select your preferred volunteer area.");
            return;
        }

        // Availability validation
        if (availability == null ||
                availability.equals("Select availability")) {

            showError("Please select your availability.");
            return;
        }

        // Skills validation
        if (skills.length() < 5 || skills.length() > 1000) {

            showError(
                    "Please describe your skills in 5 to 1000 characters."
            );

            skillsArea.requestFocus();
            return;
        }

        // Motivation validation
        if (motivation.length() < 20 ||
                motivation.length() > 1000) {

            showError(
                    "Motivation must contain between 20 and 1000 characters."
            );

            motivationArea.requestFocus();
            return;
        }

        // Session validation
        if (userId == null || !ObjectId.isValid(userId)) {

            showError("Invalid citizen session. Please login again.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to register as a volunteer?",
                "Confirm Registration",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        setFormEnabled(false);

        submitButton.setText("Registering...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {

            private String errorMessage;

            @Override
            protected String doInBackground() {

                try {

                    ObjectId citizenId = new ObjectId(userId);

                    MongoCollection<Document> collection =
                            MongoDBConnection.getDatabase()
                                    .getCollection("volunteers");

                    // Prevent duplicate registration by the same citizen
                    Document existing = collection.find(
                            Filters.and(
                                    Filters.eq("userId", citizenId),
                                    Filters.ne("status", "REJECTED")
                            )
                    ).first();

                    if (existing != null) {

                        errorMessage =
                                "You have already submitted a volunteer registration.";

                        return null;
                    }

                    ObjectId registrationId = new ObjectId();

                    Date now = new Date();

                    Document volunteer = new Document()
                            .append("_id", registrationId)
                            .append("volunteerNumber",
                                    "VOL-" + registrationId.toHexString()
                                            .substring(0, 8).toUpperCase())
                            .append("userId", citizenId)
                            .append("fullName", name)
                            .append("email", email)
                            .append("phone", phone)
                            .append("address", address)
                            .append("preferredArea", area)
                            .append("availability", availability)
                            .append("skills", skills)
                            .append("motivation", motivation)
                            .append("status", "PENDING")
                            .append("submittedAt", now)
                            .append("updatedAt", now);

                    collection.insertOne(volunteer);

                    return volunteer.getString("volunteerNumber");

                } catch (Exception ex) {

                    errorMessage = ex.getMessage();

                    return null;
                }
            }

            @Override
            protected void done() {

                setFormEnabled(true);

                submitButton.setText("Register as Volunteer");

                try {

                    String volunteerNumber = get();

                    if (volunteerNumber == null) {

                        showError(
                                errorMessage == null
                                        ? "Volunteer registration failed."
                                        : errorMessage
                        );

                        return;
                    }

                    JOptionPane.showMessageDialog(
                            VolunteerRegistrationFrame.this,
                            "Your volunteer registration was submitted successfully!"
                                    + "\n\nRegistration Number: "
                                    + volunteerNumber
                                    + "\nStatus: PENDING",
                            "Registration Successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    clearForm();

                } catch (Exception ex) {

                    showError(
                            "Could not complete registration: "
                                    + ex.getMessage()
                    );
                }
            }
        };

        worker.execute();
    }

    private void setFormEnabled(boolean enabled) {

        phoneField.setEnabled(enabled);
        addressField.setEnabled(enabled);

        areaBox.setEnabled(enabled);
        availabilityBox.setEnabled(enabled);

        skillsArea.setEnabled(enabled);
        motivationArea.setEnabled(enabled);

        clearButton.setEnabled(enabled);
        submitButton.setEnabled(enabled);
    }

    private void clearForm() {

        phoneField.setText("");
        addressField.setText("");

        areaBox.setSelectedIndex(0);
        availabilityBox.setSelectedIndex(0);

        skillsArea.setText("");
        motivationArea.setText("");
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Volunteer Registration",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
