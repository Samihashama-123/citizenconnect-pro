package com.citizenconnect.ui;

import com.citizenconnect.repository.UserRepository;
import com.citizenconnect.theme.Theme;
import com.citizenconnect.theme.RoundedButton;
import com.citizenconnect.theme.RoundedTextField;
import com.citizenconnect.theme.RoundedPasswordField;
import com.citizenconnect.config.UserSession;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class LoginFrame extends JFrame {

    private static final String EMAIL_HINT = "Enter your email address";

    private static final String PASSWORD_HINT = "Enter your password";

    private RoundedTextField emailField;
    private RoundedPasswordField passwordField;

    private JCheckBox rememberMe;
    private JButton showHideButton;
    private RoundedButton loginButton;

    private char passwordEchoChar;
    private boolean passwordVisible = false;
    private boolean passwordPlaceholder = true;
    private boolean loginInProgress = false;

    public LoginFrame() {

        setTitle("CitizenConnect Pro - Login");
        setSize(1100, 700);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        mainPanel.add(createBrandingPanel());
        mainPanel.add(createLoginPanel());

        setContentPane(mainPanel);
    }

    // =========================================
    // LEFT BRANDING PANEL
    // =========================================

    private JPanel createBrandingPanel() {

        JPanel leftPanel = new JPanel();

        leftPanel.setBackground(Theme.NAVY);

        leftPanel.setLayout(
                new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        leftPanel.setBorder(
                BorderFactory.createEmptyBorder(55, 45, 45, 40));

        JLabel appName = new JLabel("CitizenConnect Pro");

        appName.setFont(
                new Font("Segoe UI", Font.BOLD, 28));

        appName.setForeground(Theme.WHITE);
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);

        java.net.URL logoUrl = getClass().getResource(
                "/images/citizenconnect-logo.png");

        if (logoUrl != null) {

            ImageIcon originalIcon = new ImageIcon(logoUrl);

            Image scaledLogo = originalIcon.getImage()
                    .getScaledInstance(
                            60, 60, Image.SCALE_SMOOTH);

            appName.setIcon(new ImageIcon(scaledLogo));
            appName.setIconTextGap(15);

        } else {
            System.err.println("CitizenConnect logo not found.");
        }

        JLabel subtitle = new JLabel(
                "SMART PUBLIC SERVICE PLATFORM");

        subtitle.setFont(Theme.SMALL);
        subtitle.setForeground(new Color(188, 211, 230));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel headline = new JLabel(
                "<html>Public services,<br>simplified.</html>");

        headline.setFont(
                new Font("Segoe UI", Font.BOLD, 37));

        headline.setForeground(Theme.WHITE);
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html>A smarter way to connect citizens with<br>"
                        + "essential services and community support.</html>");

        description.setFont(Theme.BODY);
        description.setForeground(new Color(214, 228, 240));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel featurePanel = new JPanel();

        featurePanel.setOpaque(false);
        featurePanel.setLayout(
                new BoxLayout(featurePanel, BoxLayout.Y_AXIS));

        featurePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        featurePanel.add(createFeatureLabel("Secure & Reliable"));
        featurePanel.add(Box.createVerticalStrut(8));

        featurePanel.add(createFeatureLabel(
                "Essential Services in One Place"));

        featurePanel.add(Box.createVerticalStrut(8));

        featurePanel.add(createFeatureLabel(
                "Connecting Communities"));

        JLabel footer = new JLabel(
                "Your Service. Our Commitment.");

        footer.setFont(Theme.SMALL);
        footer.setForeground(new Color(185, 208, 227));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(appName);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(subtitle);

        leftPanel.add(Box.createVerticalGlue());

        leftPanel.add(headline);
        leftPanel.add(Box.createVerticalStrut(16));
        leftPanel.add(description);
        leftPanel.add(Box.createVerticalStrut(32));
        leftPanel.add(featurePanel);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(footer);

        return leftPanel;
    }

    // =========================================
    // FEATURE LABEL
    // =========================================

    private JPanel createFeatureLabel(String text) {

        JPanel item = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0));

        item.setOpaque(false);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        item.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 24));

        JComponent checkIcon = new JComponent() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(64, 214, 177));

                g2.setStroke(new BasicStroke(
                        2.2f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));

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

        item.add(checkIcon);
        item.add(Box.createHorizontalStrut(10));
        item.add(label);

        return item;
    }

    // =========================================
    // RIGHT LOGIN PANEL
    // =========================================

    private JPanel createLoginPanel() {

        JPanel rightPanel = new JPanel(new GridBagLayout());

        rightPanel.setBackground(Theme.WHITE);

        rightPanel.setBorder(
                BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JPanel formPanel = new JPanel();

        formPanel.setLayout(
                new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        formPanel.setBackground(Theme.WHITE);
        formPanel.setPreferredSize(new Dimension(420, 500));

        JLabel welcomeTitle = new JLabel("Welcome Back!");

        welcomeTitle.setFont(Theme.TITLE);
        welcomeTitle.setForeground(Theme.NAVY);
        welcomeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcomeSubtitle = new JLabel(
                "Sign in to access your citizen services.");

        welcomeSubtitle.setFont(Theme.BODY);
        welcomeSubtitle.setForeground(Theme.MUTED_TEXT);
        welcomeSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // =====================================
        // EMAIL FIELD
        // =====================================

        JLabel emailLabel = createFieldLabel("Email Address");

        emailField = new RoundedTextField();

        emailField.setBackground(Theme.BACKGROUND);

        emailField.setPreferredSize(
                new Dimension(420, 48));

        emailField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48));

        emailField.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        setupEmailPlaceholder();

        // =====================================
        // PASSWORD FIELD
        // =====================================

        JLabel passwordLabel = createFieldLabel("Password");

        passwordField = new RoundedPasswordField();

        passwordField.setBackground(Theme.BACKGROUND);

        passwordEchoChar = passwordField.getEchoChar();

        passwordField.setBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 65));

        setupPasswordPlaceholder();

        showHideButton = new JButton("Show");

        showHideButton.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        showHideButton.setForeground(Theme.PRIMARY);
        showHideButton.setContentAreaFilled(false);
        showHideButton.setBorderPainted(false);
        showHideButton.setFocusPainted(false);
        showHideButton.setFocusable(false);
        showHideButton.setMargin(new Insets(0, 0, 0, 0));

        showHideButton.setToolTipText("Show password");

        showHideButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        showHideButton.addActionListener(
                e -> togglePassword());

        JLayeredPane passwordLayer = new JLayeredPane() {

            @Override
            public void doLayout() {

                passwordField.setBounds(
                        0, 0, getWidth(), getHeight());

                showHideButton.setBounds(
                        getWidth() - 58,
                        5,
                        50,
                        getHeight() - 10);
            }
        };

        passwordLayer.add(
                passwordField,
                JLayeredPane.DEFAULT_LAYER);

        passwordLayer.add(
                showHideButton,
                JLayeredPane.PALETTE_LAYER);

        passwordLayer.setPreferredSize(
                new Dimension(420, 48));

        passwordLayer.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48));

        passwordLayer.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        // =====================================
        // REMEMBER ME / FORGOT PASSWORD
        // =====================================

        JPanel optionsPanel = new JPanel(new BorderLayout());

        optionsPanel.setBackground(Theme.WHITE);

        optionsPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 36));

        optionsPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        rememberMe = new JCheckBox("Remember me");

        rememberMe.setFont(Theme.SMALL);
        rememberMe.setForeground(Theme.TEXT);
        rememberMe.setBackground(Theme.WHITE);
        rememberMe.setFocusPainted(false);
        rememberMe.setSelected(false);

        rememberMe.setToolTipText(
                "Remember your email address on this device");

        loadRememberedEmail();

        rememberMe.addActionListener(e -> {

            if (!rememberMe.isSelected()) {
                clearRememberedEmail();
            }
        });

        JButton forgotPassword = createLinkButton("Forgot password?");

        forgotPassword.addActionListener(e -> {

            ForgotPasswordFrame forgotFrame =
                    new ForgotPasswordFrame(this);

            forgotFrame.setVisible(true);
            this.setVisible(false);
        });

        optionsPanel.add(
                rememberMe,
                BorderLayout.WEST);

        optionsPanel.add(
                forgotPassword,
                BorderLayout.EAST);

        // =====================================
        // SIGN IN BUTTON
        // =====================================

        loginButton = new RoundedButton("Sign In");

        loginButton.setPreferredSize(
                new Dimension(420, 50));

        loginButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 50));

        loginButton.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        loginButton.addActionListener(
                e -> validateLogin());

        getRootPane().setDefaultButton(loginButton);

        // =====================================
        // REGISTRATION LINK
        // =====================================

        JLabel accountText = new JLabel(
                "Don't have an account?");

        accountText.setFont(Theme.SMALL);
        accountText.setForeground(Theme.MUTED_TEXT);

        JButton registerButton = createLinkButton("Register here");

        registerButton.setMargin(
                new Insets(0, 0, 0, 0));

        registerButton.addActionListener(e -> {

            RegisterFrame registerFrame = new RegisterFrame(this);

            registerFrame.setVisible(true);
            this.setVisible(false);
        });

        JPanel registerPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 0, 0));

        registerPanel.setBackground(Theme.WHITE);
        registerPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        registerPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 35));

        registerPanel.add(accountText);
        registerPanel.add(Box.createHorizontalStrut(5));
        registerPanel.add(registerButton);

        // =====================================
        // ASSEMBLE FORM
        // =====================================

        formPanel.add(welcomeTitle);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(welcomeSubtitle);
        formPanel.add(Box.createVerticalStrut(35));

        formPanel.add(emailLabel);
        formPanel.add(Box.createVerticalStrut(9));
        formPanel.add(emailField);
        formPanel.add(Box.createVerticalStrut(22));

        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(9));
        formPanel.add(passwordLayer);
        formPanel.add(Box.createVerticalStrut(17));

        formPanel.add(optionsPanel);
        formPanel.add(Box.createVerticalStrut(24));

        formPanel.add(loginButton);
        formPanel.add(Box.createVerticalStrut(22));
        formPanel.add(registerPanel);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        rightPanel.add(formPanel, gbc);

        return rightPanel;
    }

    // =========================================
    // FIELD LABEL
    // =========================================

    private JLabel createFieldLabel(String text) {

        JLabel label = new JLabel(text);

        label.setFont(Theme.BODY);
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
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

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(
                new MouseAdapter() {

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
    // EMAIL PLACEHOLDER
    // =========================================

    private void setupEmailPlaceholder() {

        emailField.setText(EMAIL_HINT);
        emailField.setForeground(Theme.MUTED_TEXT);

        emailField.addFocusListener(
                new FocusAdapter() {

                    @Override
                    public void focusGained(FocusEvent e) {

                        if (emailField.getText().equals(EMAIL_HINT)) {

                            emailField.setText("");
                            emailField.setForeground(Theme.TEXT);
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e) {

                        if (emailField.getText().trim().isEmpty()) {

                            emailField.setText(EMAIL_HINT);
                            emailField.setForeground(Theme.MUTED_TEXT);
                        }
                    }
                });
    }

    // =========================================
    // PASSWORD PLACEHOLDER
    // =========================================

    private void setupPasswordPlaceholder() {

        passwordPlaceholder = true;

        passwordField.setEchoChar((char) 0);
        passwordField.setText(PASSWORD_HINT);
        passwordField.setForeground(Theme.MUTED_TEXT);

        passwordField.addFocusListener(
                new FocusAdapter() {

                    @Override
                    public void focusGained(FocusEvent e) {

                        if (passwordPlaceholder) {

                            passwordPlaceholder = false;
                            passwordField.setText("");
                            passwordField.setForeground(Theme.TEXT);

                            passwordField.setEchoChar(
                                    passwordVisible
                                            ? (char) 0
                                            : passwordEchoChar);
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e) {

                        if (passwordField.getPassword().length == 0) {
                            restorePasswordPlaceholder();
                        }
                    }
                });
    }

    private void restorePasswordPlaceholder() {

        passwordPlaceholder = true;

        passwordVisible = false;

        passwordField.setEchoChar((char) 0);
        passwordField.setText(PASSWORD_HINT);
        passwordField.setForeground(Theme.MUTED_TEXT);

        if (showHideButton != null) {
            showHideButton.setText("Show");
            showHideButton.setToolTipText("Show password");
        }
    }

    // =========================================
    // SHOW / HIDE PASSWORD
    // =========================================

    private void togglePassword() {

        if (passwordPlaceholder) {
            passwordField.requestFocusInWindow();
            return;
        }

        passwordVisible = !passwordVisible;

        if (passwordVisible) {

            passwordField.setEchoChar((char) 0);
            showHideButton.setText("Hide");
            showHideButton.setToolTipText("Hide password");

        } else {

            passwordField.setEchoChar(passwordEchoChar);
            showHideButton.setText("Show");
            showHideButton.setToolTipText("Show password");
        }

        passwordField.repaint();
    }

    // =========================================
    // MONGODB LOGIN AUTHENTICATION
    // =========================================

    private void validateLogin() {

        if (loginInProgress) {
            return;
        }

        String email = emailField.getText().trim();
        char[] password = passwordField.getPassword();

        try {

            if (email.isEmpty() || email.equals(EMAIL_HINT)) {

                showWarning("Please enter your email address.");
                emailField.requestFocusInWindow();
                return;
            }

            if (!email.matches(
                    "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

                showWarning("Please enter a valid email address.");
                emailField.requestFocusInWindow();
                return;
            }

            if (passwordPlaceholder || password.length == 0) {

                showWarning("Please enter your password.");
                passwordField.requestFocusInWindow();
                return;
            }

            loginInProgress = true;

            loginButton.setEnabled(false);
            loginButton.setText("Signing In...");

            char[] passwordForWorker =
                    Arrays.copyOf(password, password.length);

            SwingWorker<UserRepository.AuthenticatedUser, Void> worker =
                    new SwingWorker<UserRepository.AuthenticatedUser, Void>() {

                @Override
                protected UserRepository.AuthenticatedUser doInBackground()
                        throws Exception {

                    try {

                        UserRepository repository = new UserRepository();

                        return repository.authenticate(
                                email,
                                passwordForWorker);

                    } finally {

                        Arrays.fill(passwordForWorker, '\0');
                    }
                }

                @Override
                protected void done() {

                    loginInProgress = false;

                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");

                    try {

                        UserRepository.AuthenticatedUser user = get();

                        if (user == null) {

                            showWarning("Invalid email or password.");

                            passwordField.setText("");
                            restorePasswordPlaceholder();

                            return;
                        }

                        handleSuccessfulLogin(user);

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();

                        showWarning(
                                "Login was interrupted. Please try again.");

                    } catch (ExecutionException e) {

                        Throwable cause = e.getCause();

                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "Unable to sign in right now.\n"
                                        + "Please check your internet connection "
                                        + "and database configuration.",
                                "Login Error",
                                JOptionPane.ERROR_MESSAGE);

                        if (cause != null) {
                            System.err.println(
                                    "Login error type: "
                                            + cause.getClass()
                                            .getSimpleName());
                        }
                    }
                }
            };

            worker.execute();

        } finally {

            Arrays.fill(password, '\0');
        }
    }

    // =========================================
    // SUCCESSFUL LOGIN / ROLE VERIFICATION
    // =========================================

    private void handleSuccessfulLogin(
            UserRepository.AuthenticatedUser user) {

        if (user == null || user.id() == null) {

            showWarning(
                    "Unable to retrieve your account information. Please try again.");

            return;
        }

        String role = user.role();

        if (role == null) {

            showWarning("Your account role is not configured.");
            return;
        }

        // Save authenticated user information
        UserSession.setCurrentUser(user);

        switch (role) {

            case "CITIZEN" -> {

                saveRememberedEmail(emailField.getText());

                JOptionPane.showMessageDialog(
                        this,
                        "Login Successful!\n\n"
                                + "Welcome, " + user.fullName() + "!\n"
                                + "Account Role: CITIZEN",
                        "Citizen Authentication",
                        JOptionPane.INFORMATION_MESSAGE);

                // Open Citizen Dashboard
                CitizenDashboardFrame dashboard =
                        new CitizenDashboardFrame(user);

                dashboard.setVisible(true);

                // Close Login window
                dispose();
            }

            case "ADMIN" -> {

                saveRememberedEmail(emailField.getText());

                JOptionPane.showMessageDialog(
                        this,
                        "Login Successful!\n\n"
                                + "Welcome, " + user.fullName() + "!\n"
                                + "Account Role: ADMIN\n\n"
                                + "Admin Dashboard integration is pending.",
                        "Admin Authentication",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            default -> {

                UserSession.clearSession();

                showWarning(
                        "Your account does not have permission "
                                + "to access this application.");
            }
        }
    }

    // =========================================
    // REMEMBER ME FUNCTIONALITY
    // =========================================

    private void loadRememberedEmail() {

        Preferences preferences =
                Preferences.userNodeForPackage(LoginFrame.class);

        String savedEmail =
                preferences.get("rememberedEmail", "");

        if (!savedEmail.isBlank()) {

            emailField.setText(savedEmail);
            emailField.setForeground(Theme.TEXT);

            rememberMe.setSelected(true);
        }
    }

    private void saveRememberedEmail(String email) {

        Preferences preferences =
                Preferences.userNodeForPackage(LoginFrame.class);

        if (rememberMe.isSelected()) {

            preferences.put("rememberedEmail", email.trim());

        } else {

            preferences.remove("rememberedEmail");
        }

        try {

            preferences.flush();

        } catch (BackingStoreException e) {

            System.err.println(
                    "Unable to save Remember Me preference: "
                            + e.getMessage());
        }
    }

    private void clearRememberedEmail() {

        Preferences preferences =
                Preferences.userNodeForPackage(LoginFrame.class);

        preferences.remove("rememberedEmail");

        try {

            preferences.flush();

        } catch (BackingStoreException e) {

            System.err.println(
                    "Unable to clear Remember Me preference: "
                            + e.getMessage());
        }
    }

    // =========================================
    // WARNING MESSAGE
    // =========================================

    private void showWarning(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
    }

    // =========================================
    // MAIN METHOD
    // =========================================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}