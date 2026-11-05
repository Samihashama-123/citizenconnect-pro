
package com.citizenconnect.ui;

import com.citizenconnect.repository.UserRepository;
import com.citizenconnect.theme.Theme;
import com.citizenconnect.theme.RoundedButton;
import com.citizenconnect.theme.RoundedTextField;
import com.citizenconnect.theme.RoundedPasswordField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class RegisterFrame extends JFrame {

    private final LoginFrame loginFrame;

    private RoundedTextField nameField;
    private RoundedTextField emailField;
    private RoundedTextField phoneField;
    private RoundedTextField addressField;

    private RoundedPasswordField passwordField;
    private RoundedPasswordField confirmPasswordField;

    private JCheckBox termsCheck;
    private RoundedButton createAccountButton;

    private boolean registrationInProgress = false;

    public RegisterFrame(LoginFrame loginFrame) {

        this.loginFrame = loginFrame;

        setTitle("CitizenConnect Pro - Registration");
        setSize(1100, 700);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        mainPanel.add(createBrandingPanel());
        mainPanel.add(createRegistrationPanel());

        setContentPane(mainPanel);

        // Return to Login when registration window closes
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (RegisterFrame.this.loginFrame != null) {
                    RegisterFrame.this.loginFrame.setVisible(true);
                }
            }
        });
    }

    // =========================================
    // LEFT BRANDING PANEL
    // =========================================

    private JPanel createBrandingPanel() {

        JPanel leftPanel = new JPanel();

        leftPanel.setBackground(Theme.NAVY);

        leftPanel.setLayout(
                new BoxLayout(leftPanel, BoxLayout.Y_AXIS)
        );

        leftPanel.setBorder(
                BorderFactory.createEmptyBorder(55, 45, 45, 40)
        );

        JLabel appName = new JLabel("CitizenConnect Pro");

        appName.setFont(
                new Font("Segoe UI", Font.BOLD, 28)
        );

        appName.setForeground(Theme.WHITE);
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);

        java.net.URL logoUrl = getClass().getResource(
                "/images/citizenconnect-logo.png"
        );

        if (logoUrl != null) {

            ImageIcon originalIcon = new ImageIcon(logoUrl);

            Image scaledLogo = originalIcon.getImage()
                    .getScaledInstance(
                            60, 60, Image.SCALE_SMOOTH
                    );

            appName.setIcon(new ImageIcon(scaledLogo));
            appName.setIconTextGap(15);
        }

        JLabel subtitle = new JLabel(
                "SMART PUBLIC SERVICE PLATFORM"
        );

        subtitle.setFont(Theme.SMALL);
        subtitle.setForeground(new Color(188, 211, 230));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel headline = new JLabel(
                "<html>Join our digital<br>community.</html>"
        );

        headline.setFont(
                new Font("Segoe UI", Font.BOLD, 37)
        );

        headline.setForeground(Theme.WHITE);
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html>Create your citizen account to access<br>"
                + "public services and community support.</html>"
        );

        description.setFont(Theme.BODY);
        description.setForeground(new Color(214, 228, 240));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel featurePanel = new JPanel();

        featurePanel.setOpaque(false);
        featurePanel.setLayout(
                new BoxLayout(featurePanel, BoxLayout.Y_AXIS)
        );

        featurePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        featurePanel.add(createFeatureRow("Secure Registration"));
        featurePanel.add(Box.createVerticalStrut(8));

        featurePanel.add(createFeatureRow("Public Services Access"));
        featurePanel.add(Box.createVerticalStrut(8));

        featurePanel.add(createFeatureRow("Community Connection"));

        JLabel footer = new JLabel(
                "Your Service. Our Commitment."
        );

        footer.setFont(Theme.SMALL);
        footer.setForeground(new Color(185, 208, 227));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(appName);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(subtitle);

        leftPanel.add(Box.createVerticalGlue());

        leftPanel.add(headline);
        leftPanel.add(Box.createVerticalStrut(18));
        leftPanel.add(description);
        leftPanel.add(Box.createVerticalStrut(32));
        leftPanel.add(featurePanel);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(footer);

        return leftPanel;
    }

    // =========================================
    // REUSABLE FEATURE ROW
    // =========================================

    private JPanel createFeatureRow(String text) {

        JPanel row = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );

        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 24)
        );

        JComponent check = new JComponent() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(new Color(64, 214, 177));

                g2.setStroke(new BasicStroke(
                        2.2f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                g2.drawLine(3, 9, 7, 13);
                g2.drawLine(7, 13, 16, 4);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(20, 20);
            }
        };

        JLabel label = new JLabel(text);

        label.setFont(Theme.BODY);
        label.setForeground(new Color(190, 230, 219));

        row.add(check);
        row.add(Box.createHorizontalStrut(10));
        row.add(label);

        return row;
    }

    // =========================================
    // RIGHT REGISTRATION PANEL
    // =========================================

    private JPanel createRegistrationPanel() {

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Theme.WHITE);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Theme.WHITE);

        JPanel form = new JPanel();
        form.setBackground(Theme.WHITE);

        form.setLayout(
                new BoxLayout(form, BoxLayout.Y_AXIS)
        );

        form.setPreferredSize(new Dimension(420, 760));

        JLabel title = new JLabel("Create Account");

        title.setFont(Theme.TITLE);
        title.setForeground(Theme.NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Register to access your citizen services."
        );

        subtitle.setFont(Theme.BODY);
        subtitle.setForeground(Theme.MUTED_TEXT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // =====================================
        // INPUT FIELDS
        // =====================================

        nameField = createTextField();
        emailField = createTextField();
        phoneField = createTextField();
        addressField = createTextField();

        passwordField = createPasswordField();
        confirmPasswordField = createPasswordField();

        // =====================================
        // TERMS CHECKBOX
        // =====================================

        termsCheck = new JCheckBox(
                "I agree to the Terms & Conditions"
        );

        termsCheck.setFont(Theme.SMALL);
        termsCheck.setForeground(Theme.TEXT);
        termsCheck.setBackground(Theme.WHITE);
        termsCheck.setFocusPainted(false);
        termsCheck.setSelected(false);

        JPanel termsPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );

        termsPanel.setBackground(Theme.WHITE);
        termsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        termsPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 32)
        );

        termsPanel.add(termsCheck);

        // =====================================
        // CREATE ACCOUNT BUTTON
        // =====================================

        createAccountButton =
                new RoundedButton("Create Account");

        createAccountButton.setPreferredSize(
                new Dimension(420, 48)
        );

        createAccountButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48)
        );

        createAccountButton.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        createAccountButton.addActionListener(
                e -> validateRegistration()
        );

        getRootPane().setDefaultButton(createAccountButton);

        // =====================================
        // BACK TO LOGIN
        // =====================================

        JLabel loginText = new JLabel(
                "Already have an account?"
        );

        loginText.setFont(Theme.SMALL);
        loginText.setForeground(Theme.MUTED_TEXT);

        JButton loginButton = createLinkButton("Sign In");

        loginButton.addActionListener(
                e -> returnToLogin()
        );

        JPanel loginPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 0, 0)
        );

        loginPanel.setBackground(Theme.WHITE);
        loginPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 35)
        );

        loginPanel.add(loginText);
        loginPanel.add(Box.createHorizontalStrut(5));
        loginPanel.add(loginButton);

        // =====================================
        // ASSEMBLE REGISTRATION FORM
        // =====================================

        form.add(title);
        form.add(Box.createVerticalStrut(7));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(22));

        addField(form, "Full Name", nameField);
        addField(form, "Email Address", emailField);
        addField(form, "Phone Number", phoneField);
        addField(form, "Address", addressField);

        addPasswordRow(form, "Password", passwordField);
        addPasswordRow(
                form,
                "Confirm Password",
                confirmPasswordField
        );

        form.add(Box.createVerticalStrut(6));
        form.add(termsPanel);
        form.add(Box.createVerticalStrut(16));
        form.add(createAccountButton);
        form.add(Box.createVerticalStrut(18));
        form.add(loginPanel);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 15, 20, 15);

        centerPanel.add(form, gbc);

        JScrollPane scrollPane =
                new JScrollPane(centerPanel);

        scrollPane.setBorder(null);
        scrollPane.setBackground(Theme.WHITE);

        scrollPane.getViewport().setBackground(
                Theme.WHITE
        );

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        rightPanel.add(scrollPane, BorderLayout.CENTER);

        return rightPanel;
    }

    // =========================================
    // REUSABLE TEXT FIELD
    // =========================================

    private RoundedTextField createTextField() {

        RoundedTextField field = new RoundedTextField();

        field.setFont(Theme.BODY);
        field.setForeground(Theme.TEXT);
        field.setBackground(Theme.BACKGROUND);

        field.setPreferredSize(new Dimension(420, 44));

        field.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 44)
        );

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        return field;
    }

    // =========================================
    // REUSABLE PASSWORD FIELD
    // =========================================

    private RoundedPasswordField createPasswordField() {

        RoundedPasswordField field =
                new RoundedPasswordField();

        field.setFont(Theme.BODY);
        field.setForeground(Theme.TEXT);
        field.setBackground(Theme.BACKGROUND);

        field.setPreferredSize(new Dimension(420, 44));

        field.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 44)
        );

        return field;
    }

    // =========================================
    // ADD TEXT INPUT TO FORM
    // =========================================

    private void addField(
            JPanel form,
            String title,
            RoundedTextField field
    ) {

        JLabel label = new JLabel(title);

        label.setFont(Theme.BODY);
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(label);
        form.add(Box.createVerticalStrut(6));
        form.add(field);
        form.add(Box.createVerticalStrut(13));
    }

    // =========================================
    // PASSWORD WITH SHOW / HIDE BUTTON
    // =========================================

    private void addPasswordRow(
            JPanel form,
            String title,
            RoundedPasswordField field
    ) {

        JLabel label = new JLabel(title);

        label.setFont(Theme.BODY);
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        char hiddenEcho = field.getEchoChar();

        field.setBorder(
                BorderFactory.createEmptyBorder(8, 15, 8, 65)
        );

        JButton toggleButton = new JButton("Show");

        toggleButton.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        toggleButton.setForeground(Theme.PRIMARY);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setFocusPainted(false);
        toggleButton.setFocusable(false);
        toggleButton.setMargin(new Insets(0, 0, 0, 0));

        toggleButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        toggleButton.addActionListener(e -> {

            boolean currentlyHidden =
                    field.echoCharIsSet();

            field.setEchoChar(
                    currentlyHidden ? (char) 0 : hiddenEcho
            );

            toggleButton.setText(
                    currentlyHidden ? "Hide" : "Show"
            );
        });

        JLayeredPane layer = new JLayeredPane() {

            @Override
            public void doLayout() {

                field.setBounds(
                        0,
                        0,
                        getWidth(),
                        getHeight()
                );

                toggleButton.setBounds(
                        getWidth() - 58,
                        5,
                        50,
                        getHeight() - 10
                );
            }
        };

        layer.add(field, JLayeredPane.DEFAULT_LAYER);
        layer.add(toggleButton, JLayeredPane.PALETTE_LAYER);

        layer.setPreferredSize(new Dimension(420, 44));

        layer.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 44)
        );

        layer.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(label);
        form.add(Box.createVerticalStrut(6));
        form.add(layer);
        form.add(Box.createVerticalStrut(13));
    }

    // =========================================
    // LINK BUTTON
    // =========================================

    private JButton createLinkButton(String text) {

        JButton button = new JButton(text);

        button.setFont(Theme.SMALL);
        button.setForeground(Theme.PRIMARY);

        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(Theme.TEAL);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(Theme.PRIMARY);
            }
        });

        return button;
    }

    // =========================================
    // REGISTRATION + MONGODB VALIDATION
    // =========================================

    private void validateRegistration() {

        if (registrationInProgress) {
            return;
        }

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        char[] password = passwordField.getPassword();
        char[] confirmPassword =
                confirmPasswordField.getPassword();

        try {

            if (name.isEmpty()) {
                showWarning("Please enter your full name.");
                nameField.requestFocusInWindow();
                return;
            }

            if (name.length() < 2) {
                showWarning("Please enter a valid full name.");
                nameField.requestFocusInWindow();
                return;
            }

            if (email.isEmpty()) {
                showWarning("Please enter your email address.");
                emailField.requestFocusInWindow();
                return;
            }

            if (!email.matches(
                    "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            )) {
                showWarning("Please enter a valid email address.");
                emailField.requestFocusInWindow();
                return;
            }

            if (!phone.matches("^01[3-9][0-9]{8}$")) {
                showWarning(
                        "Enter a valid 11-digit Bangladesh mobile number."
                );
                phoneField.requestFocusInWindow();
                return;
            }

            if (address.isEmpty()) {
                showWarning("Please enter your address.");
                addressField.requestFocusInWindow();
                return;
            }

            if (password.length < 8) {
                showWarning(
                        "Password must contain at least 8 characters."
                );
                passwordField.requestFocusInWindow();
                return;
            }

            if (!Arrays.equals(password, confirmPassword)) {
                showWarning("Passwords do not match.");
                confirmPasswordField.requestFocusInWindow();
                return;
            }

            if (!termsCheck.isSelected()) {
                showWarning(
                        "Please accept the Terms & Conditions."
                );
                return;
            }

            // Prevent multiple registration requests
            registrationInProgress = true;

            createAccountButton.setEnabled(false);
            createAccountButton.setText("Creating Account...");

            // Make a separate password copy for background work
            char[] passwordForWorker =
                    Arrays.copyOf(password, password.length);

            // Database operation runs outside Swing UI thread
            SwingWorker<Void, Void> worker =
                    new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {

                    try {

                        UserRepository repository =
                                new UserRepository();

                        repository.registerCitizen(
                                name,
                                email,
                                phone,
                                address,
                                passwordForWorker
                        );

                        return null;

                    } finally {

                        Arrays.fill(passwordForWorker, '\0');
                    }
                }

                @Override
                protected void done() {

                    registrationInProgress = false;

                    createAccountButton.setEnabled(true);
                    createAccountButton.setText("Create Account");

                    try {

                        get();

                        JOptionPane.showMessageDialog(
                                RegisterFrame.this,
                                "Your account has been created successfully!\n"
                                + "You can now sign in.",
                                "Registration Successful",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        passwordField.setText("");
                        confirmPasswordField.setText("");

                        returnToLogin();

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();

                        showWarning(
                                "Registration was interrupted. Please try again."
                        );

                    } catch (ExecutionException e) {

                        Throwable cause = e.getCause();

                        if (cause instanceof UserRepository.DuplicateEmailException) {

                            showWarning(
                                    "An account with this email already exists."
                            );

                            emailField.requestFocusInWindow();

                        } else {

                            JOptionPane.showMessageDialog(
                                    RegisterFrame.this,
                                    "Registration could not be completed.\n"
                                    + "Please check your internet connection "
                                    + "and database configuration.",
                                    "Registration Failed",
                                    JOptionPane.ERROR_MESSAGE
                            );

                            System.err.println(
                                    "Registration error type: "
                                    + cause.getClass().getSimpleName()
                            );
                        }
                    }
                }
            };

            worker.execute();

        } finally {

            Arrays.fill(password, '\0');
            Arrays.fill(confirmPassword, '\0');
        }
    }

    // =========================================
    // WARNING MESSAGE
    // =========================================

    private void showWarning(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Registration Validation",
                JOptionPane.WARNING_MESSAGE
        );
    }

    // =========================================
    // BACK TO LOGIN
    // =========================================

    private void returnToLogin() {
        dispose();
    }
}
