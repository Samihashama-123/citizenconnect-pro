
package com.citizenconnect.ui;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.ApplicationRepository;
import com.citizenconnect.repository.UserRepository;

import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class ApplyForServiceFrame extends JFrame {

    private final UserRepository.AuthenticatedUser currentUser;
    private final Document service;

    private static final Color NAVY = new Color(16, 39, 67);
    private static final Color PRIMARY = new Color(23, 103, 194);
    private static final Color TEAL = new Color(13, 152, 127);
    private static final Color BG = new Color(244, 247, 251);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(220, 228, 238);

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_TOTAL_SIZE = 10L * 1024 * 1024;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;

    private JTextArea additionalInfoArea;

    private JLabel uploadSummaryLabel;

    private JButton submitButton;

    private final Map<String, Document> uploadedDocuments =
            new LinkedHashMap<>();

    private final Map<String, JLabel> uploadedFileLabels =
            new LinkedHashMap<>();

    private final Map<String, JButton> uploadButtons =
            new LinkedHashMap<>();

    private boolean submitting = false;

    public ApplyForServiceFrame(
            UserRepository.AuthenticatedUser user,
            Document service) {

        if (user == null || !"CITIZEN".equals(user.role())) {
            throw new IllegalArgumentException(
                    "Only authenticated citizens can submit applications.");
        }

        if (service == null || !(service.get("_id") instanceof ObjectId)) {
            throw new IllegalArgumentException(
                    "Valid service information is required.");
        }

        this.currentUser = user;
        this.service = service;

        setTitle("CitizenConnect Pro - Apply for Service");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        setLocationRelativeTo(null);

        loadApplicantProfile();
    }

    // =========================================
    // LOAD USER PROFILE
    // =========================================

    private void loadApplicantProfile() {

        SwingWorker<Document, Void> worker = new SwingWorker<>() {

            @Override
            protected Document doInBackground() {

                UserRepository repository = new UserRepository();

                return repository.getCitizenProfile(currentUser.id());
            }

            @Override
            protected void done() {

                try {

                    Document profile = get();

                    if (profile == null) {
                        return;
                    }

                    phoneField.setText(
                            safe(profile.getString("phone"), ""));

                    addressField.setText(
                            buildApplicantAddress(profile));

                } catch (Exception ex) {

                    System.err.println(
                            "Could not load applicant profile: "
                                    + rootMessage(ex));
                }
            }
        };

        worker.execute();
    }

    private String buildApplicantAddress(Document profile) {

        String detailedAddress =
                safe(profile.getString("detailedAddress"), "");

        String localArea =
                safe(profile.getString("localArea"), "");

        String upazila =
                safe(profile.getString("upazila"), "");

        String district =
                safe(profile.getString("district"), "");

        String division =
                safe(profile.getString("division"), "");

        String postalCode =
                safe(profile.getString("postalCode"), "");

        List<String> parts = new ArrayList<>();

        addAddressPart(parts, detailedAddress);
        addAddressPart(parts, localArea);
        addAddressPart(parts, upazila);
        addAddressPart(parts, district);
        addAddressPart(parts, division);
        addAddressPart(parts, postalCode);

        if (parts.isEmpty()) {
            return safe(profile.getString("address"), "");
        }

        return String.join(", ", parts);
    }

    private void addAddressPart(List<String> parts, String value) {

        if (value != null && !value.isBlank()
                && !parts.contains(value.trim())) {

            parts.add(value.trim());
        }
    }

    // =========================================
    // SIDEBAR
    // =========================================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(255, 0));

        JPanel menu = new JPanel();
        menu.setBackground(NAVY);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(28, 17, 15, 17));

        JLabel brand = new JLabel("CitizenConnect Pro");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(WHITE);

        JLabel subtitle = new JLabel("SMART PUBLIC SERVICE PLATFORM");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        subtitle.setForeground(new Color(170, 192, 214));

        JLabel heading = new JLabel("CITIZEN MENU");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 10));
        heading.setForeground(new Color(145, 170, 195));

        menu.add(brand);
        menu.add(Box.createVerticalStrut(6));
        menu.add(subtitle);
        menu.add(Box.createVerticalStrut(34));
        menu.add(heading);
        menu.add(Box.createVerticalStrut(14));

        addNav(menu, "DB", "Dashboard");
        addNav(menu, "01", "My Profile");
        addNav(menu, "02", "Public Services");
        addNav(menu, "03", "My Applications");
        addNav(menu, "04", "My Complaints");
        addNav(menu, "05", "Important Information");
        addNav(menu, "06", "Emergency Alerts");
        addNav(menu, "07", "Volunteer Registration");
        addNav(menu, "08", "Feedback & Rating");

        sidebar.add(menu, BorderLayout.NORTH);
        sidebar.add(createSidebarFooter(), BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNav(JPanel parent, String icon, String title) {

        boolean selected = "Public Services".equals(title);

        JButton button = roundedButton(
                icon + "     " + title,
                selected ? PRIMARY : NAVY,
                WHITE,
                9
        );

        button.setHorizontalAlignment(SwingConstants.LEFT);

        button.setFont(new Font("Segoe UI",
                selected ? Font.BOLD : Font.PLAIN, 12));

        button.setForeground(
                selected ? WHITE : new Color(197, 211, 226));

        button.setBorder(new EmptyBorder(12, 13, 12, 8));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (!selected) {

            button.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    button.setBackground(new Color(27, 53, 82));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    button.setBackground(NAVY);
                }
            });
        }

        button.addActionListener(e -> {

            switch (title) {

                case "Dashboard":
                    goToDashboard();
                    break;

                case "My Profile":
                    goToProfile();
                    break;

                case "Public Services":
                    goToServices();
                    break;

                default:
                    JOptionPane.showMessageDialog(
                            this,
                            title + " screen will be connected later.",
                            "Screen Integration",
                            JOptionPane.INFORMATION_MESSAGE);
            }
        });

        parent.add(button);
        parent.add(Box.createVerticalStrut(5));
    }

    private JPanel createSidebarFooter() {

        JPanel footer = new JPanel();
        footer.setBackground(NAVY);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(new EmptyBorder(10, 15, 18, 15));

        RoundedPanel profile = new RoundedPanel(
                13, new Color(27, 53, 82));

        profile.setLayout(new BorderLayout(10, 0));
        profile.setBorder(new EmptyBorder(12, 11, 12, 9));
        profile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        String name = safe(currentUser.fullName(), "Citizen");

        JLabel avatar = new JLabel(
                name.substring(0, 1).toUpperCase(),
                SwingConstants.CENTER);

        avatar.setOpaque(true);
        avatar.setBackground(TEAL);
        avatar.setForeground(WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        avatar.setPreferredSize(new Dimension(42, 42));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel userName = new JLabel(name);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userName.setForeground(WHITE);

        JLabel role = new JLabel("Citizen Account");
        role.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        role.setForeground(new Color(185, 204, 222));

        info.add(userName);
        info.add(Box.createVerticalStrut(4));
        info.add(role);

        profile.add(avatar, BorderLayout.WEST);
        profile.add(info, BorderLayout.CENTER);

        JButton logout = roundedButton(
                "Sign Out",
                new Color(37, 62, 88),
                new Color(255, 210, 210),
                10);

        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(e -> logout());

        footer.add(profile);
        footer.add(Box.createVerticalStrut(10));
        footer.add(logout);

        return footer;
    }

    // =========================================
    // MAIN CONTENT
    // =========================================

    private JPanel createMainContent() {

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(27, 30, 35, 30));

        int row = 0;

        addFullWidthSection(content, createTopBar(), row++, 20);
        addFullWidthSection(content, createHeader(), row++, 20);
        addFullWidthSection(content, createServiceSummary(), row++, 18);
        addFullWidthSection(content, createApplicantSection(), row++, 18);
        addFullWidthSection(content, createDocumentSection(), row++, 18);
        addFullWidthSection(content, createAdditionalInfoSection(), row++, 18);
        addFullWidthSection(content, createFeeSection(), row++, 20);
        addFullWidthSection(content, createSubmitSection(), row, 0);

        JScrollPane scroll = new JScrollPane(content);

        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setWheelScrollingEnabled(true);

        scroll.getVerticalScrollBar().setUnitIncrement(22);

        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        outer.add(scroll, BorderLayout.CENTER);

        return outer;
    }

    private void addFullWidthSection(
            JPanel parent,
            Component component,
            int row,
            int bottomGap) {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = row;

        gbc.weightx = 1.0;
        gbc.weighty = 0;

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.insets = new Insets(0, 0, bottomGap, 0);

        parent.add(component, gbc);
    }

    // =========================================
    // TOP BAR
    // =========================================

    private JPanel createTopBar() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel title = new JLabel("Apply for Service");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(NAVY);

        JButton backButton = roundedButton(
                "←  Back to Details", WHITE, PRIMARY, 10);

        backButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 14, 8, 14)));

        backButton.addActionListener(e -> goToServiceDetails());

        panel.add(title, BorderLayout.WEST);
        panel.add(backButton, BorderLayout.EAST);

        return panel;
    }

    // =========================================
    // HEADER
    // =========================================

    private JPanel createHeader() {

        RoundedPanel panel = new RoundedPanel(19, NAVY);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(25, 28, 25, 28));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 135));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Complete Your Application");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(WHITE);

        JLabel subtitle = new JLabel(
                "Provide accurate information and upload the required documents.");

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(220, 233, 245));

        text.add(heading);
        text.add(Box.createVerticalStrut(9));
        text.add(subtitle);

        panel.add(text, BorderLayout.CENTER);

        return panel;
    }

    // =========================================
    // SERVICE SUMMARY
    // =========================================

    private JPanel createServiceSummary() {

        RoundedPanel section = createWhiteCard();

        section.add(createSectionHeading("Selected Service"));
        section.add(Box.createVerticalStrut(13));

        JLabel serviceName = new JLabel(
                safe(service.getString("serviceName"), "Unnamed Service"));

        serviceName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        serviceName.setForeground(NAVY);

        JLabel category = createLabel(
                safe(service.getString("category"), "Public Service"));

        JLabel processing = createLabel(
                "Processing Time: " + safe(
                        service.getString("processingTime"),
                        "Not specified"));

        section.add(serviceName);
        section.add(Box.createVerticalStrut(5));
        section.add(category);
        section.add(Box.createVerticalStrut(8));
        section.add(processing);

        return section;
    }

    // =========================================
    // APPLICANT INFORMATION
    // =========================================

    private JPanel createApplicantSection() {

        RoundedPanel section = createWhiteCard();
        JPanel content = createCardContentPanel();

        JLabel heading = createSectionHeading("Applicant Information");

        addCardContent(content, heading);
        content.add(Box.createVerticalStrut(8));

        JLabel description = createDescriptionLabel(
                "Your saved profile information is loaded automatically. "
                        + "You can update the contact details for this application.");

        addCardContent(content, description);
        content.add(Box.createVerticalStrut(22));

        JPanel grid = new JPanel(new GridLayout(0, 2, 18, 18));
        grid.setOpaque(false);

        nameField = createTextField(
                safe(currentUser.fullName(), ""));

        emailField = createTextField(
                safe(currentUser.email(), ""));

        phoneField = createTextField("");
        addressField = createTextField("");

        grid.add(createFieldGroup("Full Name *", nameField));
        grid.add(createFieldGroup("Email Address *", emailField));

        grid.add(createFieldGroup("Mobile Number *", phoneField));
        grid.add(createFieldGroup("Present Address *", addressField));

        addCardContent(content, grid);

        section.setLayout(new BorderLayout());
        section.add(content, BorderLayout.CENTER);

        return section;
    }

    // =========================================
    // REQUIRED DOCUMENTS
    // =========================================

    private JPanel createDocumentSection() {

        RoundedPanel section = createWhiteCard();
        JPanel content = createCardContentPanel();

        addCardContent(content,
                createSectionHeading("Required Documents"));

        content.add(Box.createVerticalStrut(7));

        addCardContent(content, createDescriptionLabel(
                "Upload documents only where the service specifically requires them."));

        content.add(Box.createVerticalStrut(15));

        List<String> requirements =
                service.getList("requirements", String.class);

        boolean hasRequirements = false;

        if (requirements != null) {

            for (String requirement : requirements) {

                if (requirement == null || requirement.isBlank()) {
                    continue;
                }

                hasRequirements = true;

                if (isDocumentRequirement(requirement)) {
                    addCardContent(content, createUploadRow(requirement));
                } else {
                    addCardContent(content,
                            createInformationRequirementRow(requirement));
                }

                content.add(Box.createVerticalStrut(9));
            }
        }

        if (!hasRequirements) {

            addCardContent(content, createLabel(
                    "No specific requirements have been listed."));
        }

        uploadSummaryLabel = createLabel("No documents uploaded yet.");
        uploadSummaryLabel.setForeground(MUTED);

        content.add(Box.createVerticalStrut(8));
        addCardContent(content, uploadSummaryLabel);

        section.setLayout(new BorderLayout());
        section.add(content, BorderLayout.CENTER);

        return section;
    }

    private boolean isDocumentRequirement(String requirement) {

        String value = requirement.toLowerCase(Locale.ROOT);

        String[] documentKeywords = {
                "document",
                "nid",
                "national id",
                "national identity",
                "birth certificate",
                "birth registration",
                "certificate",
                "passport",
                "photograph",
                "photo",
                "picture",
                "image",
                "scan",
                "copy",
                "utility bill",
                "electricity bill",
                "trade license",
                "tax certificate",
                "tin",
                "attachment",
                "proof of identity",
                "proof of address",
                "proof of income",
                "income certificate",
                "transcript",
                "mark sheet",
                "marksheet"
        };

        for (String keyword : documentKeywords) {

            if (value.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private JPanel createInformationRequirementRow(String requirement) {

        RoundedPanel row = new RoundedPanel(
                11, new Color(235, 243, 252));

        row.setLayout(new BorderLayout(12, 0));
        row.setBorder(new EmptyBorder(12, 14, 12, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel icon = new JLabel("i", SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setBackground(new Color(218, 233, 250));
        icon.setForeground(PRIMARY);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        icon.setPreferredSize(new Dimension(30, 30));

        JLabel text = createLabel(requirement);
        text.setForeground(TEXT);

        row.add(icon, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);

        return row;
    }

    private JPanel createUploadRow(String requirement) {

        RoundedPanel row = new RoundedPanel(11, BG);
        row.setLayout(new BorderLayout(12, 0));
        row.setBorder(new EmptyBorder(11, 14, 11, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JLabel name = new JLabel(requirement);
        name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        name.setForeground(TEXT);

        JLabel fileLabel = createLabel("No file selected");

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        info.add(name);
        info.add(Box.createVerticalStrut(5));
        info.add(fileLabel);

        JButton uploadButton = roundedButton(
                "Choose File", WHITE, PRIMARY, 9);

        uploadButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 12, 7, 12)));

        uploadButton.addActionListener(e -> chooseDocument(requirement));

        row.add(info, BorderLayout.CENTER);
        row.add(uploadButton, BorderLayout.EAST);

        uploadedFileLabels.put(requirement, fileLabel);
        uploadButtons.put(requirement, uploadButton);

        return row;
    }

    // =========================================
    // DOCUMENT UPLOAD
    // =========================================

    private void chooseDocument(String requirement) {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select " + requirement);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try {

            long fileSize = Files.size(file.toPath());

            if (fileSize <= 0) {
                showError("The selected file is empty.");
                return;
            }

            if (fileSize > MAX_FILE_SIZE) {
                showError("Each document must be 5 MB or smaller.");
                return;
            }

            long currentTotal = 0;

            for (Document document : uploadedDocuments.values()) {

                Object size = document.get("fileSize");

                if (size instanceof Number) {
                    currentTotal += ((Number) size).longValue();
                }
            }

            Document previous = uploadedDocuments.get(requirement);

            if (previous != null) {

                Object previousSize = previous.get("fileSize");

                if (previousSize instanceof Number) {
                    currentTotal -= ((Number) previousSize).longValue();
                }
            }

            if (currentTotal + fileSize > MAX_TOTAL_SIZE) {
                showError(
                        "Total uploaded document size cannot exceed 10 MB.");
                return;
            }

            byte[] fileData = Files.readAllBytes(file.toPath());

            String contentType = Files.probeContentType(file.toPath());

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            Document document = new Document()
                    .append("requirement", requirement)
                    .append("fileName", file.getName())
                    .append("contentType", contentType)
                    .append("fileSize", fileSize)
                    .append("data", fileData);

            uploadedDocuments.put(requirement, document);

            uploadedFileLabels.get(requirement).setText(
                    file.getName() + "  ("
                            + formatFileSize(fileSize) + ")");

            uploadedFileLabels.get(requirement).setForeground(TEAL);

            uploadButtons.get(requirement).setText("Change File");

            updateUploadSummary();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not read the selected file.\n\n" + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUploadSummary() {

        long totalSize = 0;

        for (Document document : uploadedDocuments.values()) {

            Object size = document.get("fileSize");

            if (size instanceof Number) {
                totalSize += ((Number) size).longValue();
            }
        }

        uploadSummaryLabel.setText(
                uploadedDocuments.size() + " document(s) selected • "
                        + formatFileSize(totalSize) + " total");
    }

    // =========================================
    // ADDITIONAL INFORMATION
    // =========================================

    private JPanel createAdditionalInfoSection() {

        RoundedPanel section = createWhiteCard();
        JPanel content = createCardContentPanel();

        addCardContent(content,
                createSectionHeading("Additional Information"));

        content.add(Box.createVerticalStrut(7));

        addCardContent(content, createDescriptionLabel(
                "Provide any additional information relevant to your application."));

        content.add(Box.createVerticalStrut(13));

        additionalInfoArea = new JTextArea(5, 20);
        additionalInfoArea.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        additionalInfoArea.setForeground(TEXT);
        additionalInfoArea.setLineWrap(true);
        additionalInfoArea.setWrapStyleWord(true);
        additionalInfoArea.setBorder(new EmptyBorder(12, 13, 12, 13));

        JScrollPane textScroll = new JScrollPane(additionalInfoArea);
        textScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        textScroll.setPreferredSize(new Dimension(500, 120));

        addCardContent(content, textScroll);

        section.setLayout(new BorderLayout());
        section.add(content, BorderLayout.CENTER);

        return section;
    }

    // =========================================
    // FEE
    // =========================================

    private JPanel createFeeSection() {

        RoundedPanel section = new RoundedPanel(
                15, new Color(235, 243, 252));

        section.setLayout(new BorderLayout());
        section.setBorder(new EmptyBorder(17, 20, 17, 20));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Application Fee");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 13));
        heading.setForeground(NAVY);

        JLabel note = new JLabel(
                "Payment is not processed by this submission form.");

        note.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        note.setForeground(MUTED);

        info.add(heading);
        info.add(Box.createVerticalStrut(5));
        info.add(note);

        JLabel feeLabel = new JLabel(
                formatFee(service.get("applicationFee")));

        feeLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        feeLabel.setForeground(PRIMARY);

        section.add(info, BorderLayout.CENTER);
        section.add(feeLabel, BorderLayout.EAST);

        return section;
    }

    // =========================================
    // SUBMIT SECTION
    // =========================================

    private JPanel createSubmitSection() {

        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JLabel note = createLabel(
                "* Required fields must be completed before submission.");

        JButton cancelButton = roundedButton(
                "Cancel", WHITE, MUTED, 10);

        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 16, 10, 16)));

        cancelButton.addActionListener(e -> goToServiceDetails());

        submitButton = roundedButton(
                "Submit Application  →", PRIMARY, WHITE, 10);

        submitButton.setBorder(new EmptyBorder(12, 20, 12, 20));
        submitButton.addActionListener(e -> submitApplication());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        buttons.add(cancelButton);
        buttons.add(submitButton);

        panel.add(note, BorderLayout.WEST);
        panel.add(buttons, BorderLayout.EAST);

        return panel;
    }

    // =========================================
    // SUBMIT APPLICATION TO MONGODB
    // =========================================

    private void submitApplication() {

        if (submitting) {
            return;
        }

        String applicantName = nameField.getText().trim();
        String applicantEmail = emailField.getText().trim();
        String applicantPhone = phoneField.getText().trim();
        String applicantAddress = addressField.getText().trim();

        if (applicantName.isBlank()
                || applicantEmail.isBlank()
                || applicantPhone.isBlank()
                || applicantAddress.isBlank()) {

            showError(
                    "Please complete all required applicant information.");
            return;
        }

        if (!applicantEmail.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            showError("Please enter a valid email address.");
            emailField.requestFocus();
            return;
        }

        if (!applicantPhone.matches("[0-9+() -]{7,20}")) {

            showError("Please enter a valid mobile number.");
            phoneField.requestFocus();
            return;
        }

        if (!allRequiredDocumentsUploaded()) {

            showError(
                    "Please upload every required document before submitting.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to submit this application?\n\n"
                        + "Service: " + service.getString("serviceName")
                        + "\nFee: " + formatFee(service.get("applicationFee")),
                "Confirm Application",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        Object serviceIdObject = service.get("_id");

        if (!(serviceIdObject instanceof ObjectId)) {
            showError("Invalid service ID.");
            return;
        }

        String userId = currentUser.id();
        String serviceId = serviceIdObject.toString();

        Document formData = new Document()
                .append("address", applicantAddress)
                .append(
                        "additionalInformation",
                        additionalInfoArea.getText().trim());

        List<Document> documents =
                new ArrayList<>(uploadedDocuments.values());

        double fee = getServiceFee();

        submitting = true;
        submitButton.setEnabled(false);
        submitButton.setText("Submitting...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {

            @Override
            protected String doInBackground() {

                ApplicationRepository repository =
                        new ApplicationRepository();

                return repository.submitApplication(
                        userId,
                        serviceId,
                        applicantName,
                        applicantEmail,
                        applicantPhone,
                        formData,
                        documents,
                        fee);
            }

            @Override
            protected void done() {

                submitting = false;
                submitButton.setEnabled(true);
                submitButton.setText("Submit Application  →");

                try {

                    String applicationNumber = get();

                    JOptionPane.showMessageDialog(
                            ApplyForServiceFrame.this,
                            "Your application has been submitted successfully!"
                                    + "\n\nApplication Number: "
                                    + applicationNumber
                                    + "\n\nPlease save this application number "
                                    + "for future tracking.",
                            "Application Submitted",
                            JOptionPane.INFORMATION_MESSAGE);

                    goToServices();

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            ApplyForServiceFrame.this,
                            "Application submission failed.\n\n"
                                    + rootMessage(ex),
                            "Submission Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    // =========================================
    // REQUIRED DOCUMENT VALIDATION
    // =========================================

    private boolean allRequiredDocumentsUploaded() {

        List<String> requirements =
                service.getList("requirements", String.class);

        if (requirements == null || requirements.isEmpty()) {
            return true;
        }

        for (String requirement : requirements) {

            if (requirement == null || requirement.isBlank()) {
                continue;
            }

            if (!isDocumentRequirement(requirement)) {
                continue;
            }

            if (!uploadedDocuments.containsKey(requirement)) {
                return false;
            }
        }

        return true;
    }

    private double getServiceFee() {

        Object fee = service.get("applicationFee");

        if (fee instanceof Number) {
            return ((Number) fee).doubleValue();
        }

        return 0;
    }

    // =========================================
    // FORM HELPERS
    // =========================================

    private JTextField createTextField(String value) {

    JTextField field = new JTextField(value);

    field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    field.setForeground(TEXT);
    field.setCaretColor(PRIMARY);
    field.setHorizontalAlignment(JTextField.LEFT);

    field.setBackground(WHITE);
    field.setOpaque(false);

    field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedLineBorder(new Color(174, 193, 216), 18),
            new EmptyBorder(0, 16, 0, 16)
    ));

    field.setPreferredSize(new Dimension(300, 52));
    field.setMinimumSize(new Dimension(100, 52));
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

    return field;
}

    private JPanel createFieldGroup(String title, JTextField field) {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(NAVY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        field.setPreferredSize(new Dimension(300, 50));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(7));
        panel.add(field);

        return panel;
    }

    // Common content layout for all three cards

    private JPanel createCardContentPanel() {

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        return content;
    }

    private void addCardContent(JPanel parent, JComponent component) {

        component.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension preferred = component.getPreferredSize();

        component.setMaximumSize(new Dimension(
                Integer.MAX_VALUE,
                preferred.height));

        parent.add(component);
    }

    private RoundedPanel createWhiteCard() {

        RoundedPanel panel = new RoundedPanel(16, WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(21, 23, 21, 23));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        return panel;
    }

    private JLabel createSectionHeading(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(NAVY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    private JLabel createDescriptionLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(71, 85, 105));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    private JLabel createLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    // =========================================
    // ROUNDED TEXT FIELD
    // =========================================

        // =========================================
    // ROUNDED LINE BORDER
    // =========================================

    private static class RoundedLineBorder
        extends javax.swing.border.AbstractBorder {

    private final Color color;
    private final int radius;

    RoundedLineBorder(Color color, int radius) {
        this.color = color;
        this.radius = radius;
    }

    @Override
    public void paintBorder(
            Component c,
            Graphics g,
            int x,
            int y,
            int width,
            int height) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.6f));

        g2.drawRoundRect(
                x + 1,
                y + 1,
                width - 3,
                height - 3,
                radius,
                radius
        );

        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(2, 2, 2, 2);
    }

    @Override
    public Insets getBorderInsets(
            Component c,
            Insets insets) {

        insets.set(2, 2, 2, 2);
        return insets;
    }
}

    // =========================================
    // BUTTON
    // =========================================

    private JButton roundedButton(
            String text,
            Color background,
            Color foreground,
            int radius) {

        JButton button = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                Color color = getBackground();

                if (!isEnabled()) {
                    color = new Color(190, 198, 208);
                } else if (getModel().isPressed()) {
                    color = color.darker();
                } else if (getModel().isRollover()) {
                    color = color.brighter();
                }

                g2.setColor(color);

                g2.fillRoundRect(
                        0, 0,
                        getWidth(), getHeight(),
                        radius * 2, radius * 2);

                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    // =========================================
    // ROUNDED PANEL
    // =========================================

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(int radius, Color backgroundColor) {

            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(backgroundColor);

            g2.fillRoundRect(
                    0, 0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius, radius);

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =========================================
    // COMMON HELPERS
    // =========================================

    private String formatFee(Object fee) {

        if (fee == null) {
            return "Not specified";
        }

        if (fee instanceof Number) {

            double amount = ((Number) fee).doubleValue();

            if (amount == 0) {
                return "Free";
            }

            return String.format(Locale.US, "৳%.2f", amount);
        }

        return fee.toString();
    }

    private String formatFileSize(long bytes) {

        if (bytes < 1024 * 1024) {
            return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        }

        return String.format(
                Locale.US,
                "%.2f MB",
                bytes / (1024.0 * 1024.0));
    }

    private String safe(String value, String fallback) {

        return value == null || value.isBlank()
                ? fallback
                : value;
    }

    private String rootMessage(Exception exception) {

        Throwable cause = exception;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        String message = cause.getMessage();

        return message == null || message.isBlank()
                ? cause.getClass().getSimpleName()
                : message;
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation",
                JOptionPane.WARNING_MESSAGE);
    }

    // =========================================
    // NAVIGATION
    // =========================================

    private void goToServiceDetails() {

        new ServiceDetailsFrame(currentUser, service).setVisible(true);
        dispose();
    }

    private void goToServices() {

        new PublicServicesFrame(currentUser).setVisible(true);
        dispose();
    }

    private void goToDashboard() {

        new CitizenDashboardFrame(currentUser).setVisible(true);
        dispose();
    }

    private void goToProfile() {

        new UserProfileFrame(currentUser).setVisible(true);
        dispose();
    }

    private void logout() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to sign out?",
                "Confirm Sign Out",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {

            UserSession.clearSession();

            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
