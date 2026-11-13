package com.citizenconnect.ui;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.UserRepository;
import com.citizenconnect.theme.Theme;

import org.bson.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class UserProfileFrame extends JFrame {

    private final UserRepository userRepository =
            new UserRepository();

    private final UserRepository.AuthenticatedUser user;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField alternativePhoneField;

    private JTextField dateOfBirthField;
    private JComboBox<String> genderCombo;

    private JComboBox<String> divisionCombo;
    private JTextField districtField;
    private JTextField upazilaField;
    private JTextField localAreaField;
    private JTextField wardField;
    private JTextField detailedAddressField;
    private JTextField postalCodeField;

    private JPasswordField nidField;

    private JTextField emergencyNameField;
    private JTextField emergencyPhoneField;

    private JComboBox<String> languageCombo;

    private JLabel completionLabel;
    private JProgressBar completionBar;
    private JLabel statusLabel;

    private JButton saveButton;

    private static final Color NAVY =
            new Color(16, 39, 67);

    private static final Color PRIMARY =
            new Color(23, 103, 194);

    private static final Color TEAL =
            new Color(13, 152, 127);

    private static final Color BACKGROUND =
            new Color(244, 247, 251);

    private static final Color BORDER =
            new Color(220, 228, 238);

    private static final Color MUTED =
            new Color(100, 116, 139);

    public UserProfileFrame(
            UserRepository.AuthenticatedUser user
    ) {

        if (user == null
                || user.id() == null
                || !"CITIZEN".equals(user.role())) {

            throw new IllegalArgumentException(
                    "A valid citizen session is required."
            );
        }

        this.user = user;

        setTitle("CitizenConnect Pro - My Profile");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1150, 850);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);

        createUI();
        loadProfile();
    }

    // =========================================
    // MAIN UI
    // =========================================

    private void createUI() {

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        root.add(createHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(22, 28, 30, 28));

        content.add(createCompletionCard());
        content.add(Box.createVerticalStrut(20));

        content.add(createPersonalSection());
        content.add(Box.createVerticalStrut(18));

        content.add(createContactSection());
        content.add(Box.createVerticalStrut(18));

        content.add(createAddressSection());
        content.add(Box.createVerticalStrut(18));

        content.add(createIdentitySection());
        content.add(Box.createVerticalStrut(18));

        content.add(createEmergencySection());
        content.add(Box.createVerticalStrut(18));

        content.add(createPreferenceSection());
        content.add(Box.createVerticalStrut(20));

        content.add(createSaveSection());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().setBackground(BACKGROUND);

        root.add(scrollPane, BorderLayout.CENTER);

        setContentPane(root);
    }

    // =========================================
    // HEADER
    // =========================================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(18, 28, 18, 28));

        JButton backButton = new JButton("← Dashboard");
        styleButton(backButton, false);

        backButton.addActionListener(e -> goToDashboard());

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Manage your personal information"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(210, 220, 235));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        header.add(backButton, BorderLayout.WEST);
        header.add(titlePanel, BorderLayout.CENTER);

        return header;
    }

    // =========================================
    // PROFILE COMPLETION
    // =========================================

    private JPanel createCompletionCard() {

        JPanel card = createCard();

        card.setLayout(new BorderLayout(15, 10));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Profile Completion");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Complete your required information."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitle);

        completionLabel = new JLabel("0%");
        completionLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 24)
        );
        completionLabel.setForeground(TEAL);

        completionBar = new JProgressBar(0, 100);
        completionBar.setValue(0);
        completionBar.setStringPainted(false);
        completionBar.setPreferredSize(new Dimension(100, 12));
        completionBar.setForeground(TEAL);
        completionBar.setBackground(new Color(230, 237, 245));
        completionBar.setBorderPainted(false);

        JPanel progressPanel = new JPanel(new BorderLayout(10, 8));
        progressPanel.setOpaque(false);
        progressPanel.add(completionLabel, BorderLayout.EAST);
        progressPanel.add(completionBar, BorderLayout.CENTER);

        card.add(textPanel, BorderLayout.NORTH);
        card.add(progressPanel, BorderLayout.CENTER);

        return card;
    }

    // =========================================
    // PERSONAL INFORMATION
    // =========================================

    private JPanel createPersonalSection() {

        JPanel card = createSection("Personal Information");

        nameField = createTextField();
        dateOfBirthField = createTextField();

        genderCombo = new JComboBox<>(new String[]{
                "Select Gender",
                "Male",
                "Female",
                "Other",
                "Prefer not to say"
        });

        addFormRow(card, "Full Name *", nameField);
        addFormRow(card, "Date of Birth * (YYYY-MM-DD)",
                dateOfBirthField);
        addFormRow(card, "Gender *", genderCombo);

        return card;
    }

    // =========================================
    // CONTACT INFORMATION
    // =========================================

    private JPanel createContactSection() {

        JPanel card = createSection("Contact Information");

        emailField = createTextField();
        emailField.setEditable(false);
        emailField.setBackground(new Color(239, 243, 248));

        phoneField = createTextField();
        alternativePhoneField = createTextField();

        addFormRow(card, "Email Address", emailField);
        addFormRow(card, "Mobile Number *", phoneField);
        addFormRow(card, "Alternative Mobile",
                alternativePhoneField);

        return card;
    }

    // =========================================
    // ADDRESS INFORMATION
    // =========================================

    private JPanel createAddressSection() {

        JPanel card = createSection("Address Information");

        divisionCombo = new JComboBox<>(new String[]{
                "Select Division",
                "Barishal",
                "Chattogram",
                "Dhaka",
                "Khulna",
                "Mymensingh",
                "Rajshahi",
                "Rangpur",
                "Sylhet"
        });

        districtField = createTextField();
        upazilaField = createTextField();
        localAreaField = createTextField();
        wardField = createTextField();
        detailedAddressField = createTextField();
        postalCodeField = createTextField();

        addFormRow(card, "Division *", divisionCombo);
        addFormRow(card, "District *", districtField);
        addFormRow(card, "Upazila / Thana *", upazilaField);
        addFormRow(card, "Union / Municipality / City Corporation",
                localAreaField);
        addFormRow(card, "Ward Number", wardField);
        addFormRow(card, "Village / Road / House Number *",
                detailedAddressField);
        addFormRow(card, "Postal Code", postalCodeField);

        return card;
    }

    // =========================================
    // IDENTITY INFORMATION
    // =========================================

    private JPanel createIdentitySection() {

        JPanel card = createSection("Identity Information");

        nidField = new JPasswordField();
        styleTextField(nidField);

        nidField.setToolTipText(
                "Optional. Your NID number is stored as profile information."
        );

        addFormRow(card, "NID Number (Optional)", nidField);

        JLabel note = new JLabel(
                "NID is optional. No verification is performed by this form."
        );

        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(MUTED);

        card.add(note);

        return card;
    }

    // =========================================
    // EMERGENCY CONTACT
    // =========================================

    private JPanel createEmergencySection() {

        JPanel card = createSection("Emergency Contact");

        emergencyNameField = createTextField();
        emergencyPhoneField = createTextField();

        addFormRow(card, "Emergency Contact Name",
                emergencyNameField);

        addFormRow(card, "Emergency Contact Number",
                emergencyPhoneField);

        return card;
    }

    // =========================================
    // PREFERENCE
    // =========================================

    private JPanel createPreferenceSection() {

        JPanel card = createSection("Preferences");

        languageCombo = new JComboBox<>(new String[]{
                "Bangla",
                "English"
        });

        addFormRow(card, "Preferred Language *",
                languageCombo);

        return card;
    }

    // =========================================
    // SAVE SECTION
    // =========================================

    private JPanel createSaveSection() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(MUTED);

        saveButton = new JButton("Save Profile");
        styleButton(saveButton, true);

        saveButton.addActionListener(e -> saveProfile());

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(saveButton, BorderLayout.EAST);

        return panel;
    }

    // =========================================
    // CARD DESIGN
    // =========================================

    private JPanel createCard() {

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(20, 22, 20, 22)
                )
        );

        return card;
    }

    private JPanel createSection(String titleText) {

        JPanel card = createCard();

        card.setLayout(new GridBagLayout());

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(NAVY);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 15, 0);

        card.add(title, gbc);

        return card;
    }

    // =========================================
    // FORM ROW
    // =========================================

    private void addFormRow(
            JPanel panel,
            String labelText,
            JComponent field
    ) {

        GridBagConstraints gbc = new GridBagConstraints();

        int row = panel.getComponentCount() / 2 + 1;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(51, 65, 85));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.30;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(7, 0, 7, 15);

        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.70;
        gbc.insets = new Insets(7, 0, 7, 0);

        field.setPreferredSize(new Dimension(300, 38));
        panel.add(field, gbc);
    }

    // =========================================
    // TEXT FIELD DESIGN
    // =========================================

    private JTextField createTextField() {

        JTextField field = new JTextField();
        styleTextField(field);

        return field;
    }

    private void styleTextField(JTextField field) {

        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(new Color(30, 41, 59));
        field.setBackground(Color.WHITE);

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(7, 10, 7, 10)
                )
        );
    }

    // =========================================
    // BUTTON DESIGN
    // =========================================

    private void styleButton(
            JButton button,
            boolean primary
    ) {

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setBackground(primary ? PRIMARY : NAVY);
        button.setForeground(Color.WHITE);

        button.setBorder(
                new EmptyBorder(10, 18, 10, 18)
        );
    }

    // =========================================
    // LOAD PROFILE FROM DATABASE
    // =========================================

    private void loadProfile() {

        setFormEnabled(false);
        statusLabel.setText("Loading profile...");

        SwingWorker<Document, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Document doInBackground() {

                return userRepository.getCitizenProfile(
                        user.id()
                );
            }

            @Override
            protected void done() {

                try {

                    Document profile = get();

                    if (profile == null) {

                        JOptionPane.showMessageDialog(
                                UserProfileFrame.this,
                                "Profile information could not be found.",
                                "Profile Error",
                                JOptionPane.ERROR_MESSAGE
                        );

                        statusLabel.setText("Profile not found.");
                        return;
                    }

                    populateFields(profile);
                    updateCompletion();

                    statusLabel.setText("Profile loaded.");

                } catch (Exception ex) {

                    showError(
                            "Could not load profile.",
                            ex
                    );

                } finally {

                    setFormEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // =========================================
    // POPULATE FORM
    // =========================================

    private void populateFields(Document profile) {

        nameField.setText(value(profile, "fullName"));
        emailField.setText(value(profile, "email"));
        phoneField.setText(value(profile, "phone"));

        alternativePhoneField.setText(
                value(profile, "alternativePhone")
        );

        dateOfBirthField.setText(
                value(profile, "dateOfBirth")
        );

        setComboValue(
                genderCombo,
                value(profile, "gender")
        );

        setComboValue(
                divisionCombo,
                value(profile, "division")
        );

        districtField.setText(value(profile, "district"));
        upazilaField.setText(value(profile, "upazila"));
        localAreaField.setText(value(profile, "localArea"));
        wardField.setText(value(profile, "wardNumber"));

        String detailedAddress =
                value(profile, "detailedAddress");

        if (detailedAddress.isBlank()) {
            detailedAddress = value(profile, "address");
        }

        detailedAddressField.setText(detailedAddress);
        postalCodeField.setText(value(profile, "postalCode"));

        nidField.setText(value(profile, "nidNumber"));

        emergencyNameField.setText(
                value(profile, "emergencyContactName")
        );

        emergencyPhoneField.setText(
                value(profile, "emergencyContactNumber")
        );

        setComboValue(
                languageCombo,
                value(profile, "preferredLanguage")
        );
    }

    private String value(
            Document document,
            String key
    ) {

        String result = document.getString(key);

        return result == null ? "" : result;
    }

    private void setComboValue(
            JComboBox<String> combo,
            String value
    ) {

        if (value == null || value.isBlank()) {
            return;
        }

        combo.setSelectedItem(value);
    }

    // =========================================
    // SAVE PROFILE
    // =========================================

    private void saveProfile() {

        if (!validateForm()) {
            return;
        }

        Document profileData = collectProfileData();

        setFormEnabled(false);
        statusLabel.setText("Saving profile...");

        SwingWorker<Void, Void> worker =
                new SwingWorker<>() {

            @Override
            protected Void doInBackground() {

                userRepository.updateCitizenProfile(
                        user.id(),
                        profileData
                );

                return null;
            }

            @Override
            protected void done() {

                try {

                    get();

                    UserRepository.AuthenticatedUser updatedUser =
                            new UserRepository.AuthenticatedUser(
                                    user.id(),
                                    nameField.getText().trim(),
                                    user.email(),
                                    user.role()
                            );

                    UserSession.setCurrentUser(updatedUser);

                    updateCompletion();

                    statusLabel.setText(
                            "Profile saved successfully."
                    );

                    JOptionPane.showMessageDialog(
                            UserProfileFrame.this,
                            "Your profile has been updated successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (Exception ex) {

                    showError(
                            "Could not save profile.",
                            ex
                    );

                } finally {

                    setFormEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // =========================================
    // COLLECT FORM DATA
    // =========================================

    private Document collectProfileData() {

        return new Document()
                .append("fullName", nameField.getText().trim())
                .append("phone", phoneField.getText().trim())
                .append(
                        "alternativePhone",
                        alternativePhoneField.getText().trim()
                )
                .append(
                        "dateOfBirth",
                        dateOfBirthField.getText().trim()
                )
                .append(
                        "gender",
                        selectedValue(genderCombo)
                )
                .append(
                        "division",
                        selectedValue(divisionCombo)
                )
                .append(
                        "district",
                        districtField.getText().trim()
                )
                .append(
                        "upazila",
                        upazilaField.getText().trim()
                )
                .append(
                        "localArea",
                        localAreaField.getText().trim()
                )
                .append(
                        "wardNumber",
                        wardField.getText().trim()
                )
                .append(
                        "detailedAddress",
                        detailedAddressField.getText().trim()
                )
                .append(
                        "postalCode",
                        postalCodeField.getText().trim()
                )
                .append(
                        "nidNumber",
                        new String(nidField.getPassword()).trim()
                )
                .append(
                        "emergencyContactName",
                        emergencyNameField.getText().trim()
                )
                .append(
                        "emergencyContactNumber",
                        emergencyPhoneField.getText().trim()
                )
                .append(
                        "preferredLanguage",
                        selectedValue(languageCombo)
                );
    }

    private String selectedValue(
            JComboBox<String> combo
    ) {

        Object selected = combo.getSelectedItem();

        return selected == null ? "" : selected.toString();
    }

    // =========================================
    // VALIDATION
    // =========================================

    private boolean validateForm() {

        if (nameField.getText().trim().isBlank()
                || phoneField.getText().trim().isBlank()
                || dateOfBirthField.getText().trim().isBlank()
                || districtField.getText().trim().isBlank()
                || upazilaField.getText().trim().isBlank()
                || detailedAddressField.getText().trim().isBlank()) {

            showValidationError(
                    "Please fill in all required fields."
            );

            return false;
        }

        if ("Select Gender".equals(selectedValue(genderCombo))) {

            showValidationError(
                    "Please select your gender."
            );

            return false;
        }

        if ("Select Division".equals(selectedValue(divisionCombo))) {

            showValidationError(
                    "Please select your division."
            );

            return false;
        }

        if (selectedValue(languageCombo).isBlank()) {

            showValidationError(
                    "Please select your preferred language."
            );

            return false;
        }

        try {

            LocalDate date = LocalDate.parse(
                    dateOfBirthField.getText().trim()
            );

            if (date.isAfter(LocalDate.now())) {

                showValidationError(
                        "Date of birth cannot be in the future."
                );

                return false;
            }

        } catch (DateTimeParseException ex) {

            showValidationError(
                    "Date of birth must use YYYY-MM-DD format."
            );

            return false;
        }

        return true;
    }

    private void showValidationError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
    }

    // =========================================
    // PROFILE COMPLETION CALCULATION
    // =========================================

    private void updateCompletion() {

        int completed = 0;

        if (!nameField.getText().trim().isBlank()) {
            completed++;
        }

        if (!emailField.getText().trim().isBlank()) {
            completed++;
        }

        if (!phoneField.getText().trim().isBlank()) {
            completed++;
        }

        if (!dateOfBirthField.getText().trim().isBlank()) {
            completed++;
        }

        if (!"Select Gender".equals(selectedValue(genderCombo))) {
            completed++;
        }

        if (!"Select Division".equals(selectedValue(divisionCombo))) {
            completed++;
        }

        if (!districtField.getText().trim().isBlank()) {
            completed++;
        }

        if (!upazilaField.getText().trim().isBlank()) {
            completed++;
        }

        if (!detailedAddressField.getText().trim().isBlank()) {
            completed++;
        }

        if (!selectedValue(languageCombo).isBlank()) {
            completed++;
        }

        int percentage = completed * 10;

        completionBar.setValue(percentage);
        completionLabel.setText(percentage + "%");
    }

    // =========================================
    // ENABLE / DISABLE FORM
    // =========================================

    private void setFormEnabled(boolean enabled) {

        nameField.setEnabled(enabled);
        phoneField.setEnabled(enabled);
        alternativePhoneField.setEnabled(enabled);
        dateOfBirthField.setEnabled(enabled);
        genderCombo.setEnabled(enabled);

        divisionCombo.setEnabled(enabled);
        districtField.setEnabled(enabled);
        upazilaField.setEnabled(enabled);
        localAreaField.setEnabled(enabled);
        wardField.setEnabled(enabled);
        detailedAddressField.setEnabled(enabled);
        postalCodeField.setEnabled(enabled);

        nidField.setEnabled(enabled);

        emergencyNameField.setEnabled(enabled);
        emergencyPhoneField.setEnabled(enabled);

        languageCombo.setEnabled(enabled);

        saveButton.setEnabled(enabled);
    }

    // =========================================
    // BACK TO DASHBOARD
    // =========================================

    private void goToDashboard() {

        UserRepository.AuthenticatedUser currentUser =
                UserSession.getCurrentUser();

        if (currentUser == null) {
            dispose();
            return;
        }

        new CitizenDashboardFrame(currentUser).setVisible(true);
        dispose();
    }

    // =========================================
    // ERROR HANDLING
    // =========================================

    private void showError(
            String message,
            Exception exception
    ) {

        exception.printStackTrace();

        Throwable cause = exception.getCause();

        String detail = cause != null
                ? cause.getMessage()
                : exception.getMessage();

        JOptionPane.showMessageDialog(
                this,
                message + "\n" + detail,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );

        statusLabel.setText("Something went wrong.");
    }
}