
package com.citizenconnect.ui.admin;

import com.citizenconnect.config.UserSession;
import com.citizenconnect.repository.ServiceRepository;
import com.citizenconnect.theme.Theme;
import com.citizenconnect.theme.RoundedPanel;
import com.citizenconnect.theme.RoundedButton;

import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AdminServicesFrame extends JFrame {

    private final ServiceRepository repository;
    private final AdminDashboardFrame dashboardFrame;

    private JTable servicesTable;
    private DefaultTableModel tableModel;

    private JTextField searchField;
    private JLabel countLabel;
    private JLabel loadingLabel;

    private JButton addButton;
    private JButton editButton;
    private JButton statusButton;
    private JButton deleteButton;
    private JButton refreshButton;

    private List<Document> allServices = new ArrayList<>();

    private static final String[] CATEGORIES = {
        "Health",
        "Education",
        "Legal",
        "Government",
        "Public Safety",
        "Other"
    };

    private record ServiceFormData(
            String serviceName,
            String category,
            String description,
            List<String> requirements,
            double applicationFee,
            String processingTime,
            String officeName,
            String applicationInstructions
    ) {}

    public AdminServicesFrame() {
        this(null);
    }

    public AdminServicesFrame(AdminDashboardFrame dashboardFrame) {

        if (!UserSession.isAdmin()) {
            throw new SecurityException("Admin access required.");
        }

        this.dashboardFrame = dashboardFrame;
        this.repository = new ServiceRepository();

        setTitle("CitizenConnect Pro | Manage Services");
        setSize(1350, 850);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(dashboardFrame);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BACKGROUND);

        mainPanel.add(createSidebar(), BorderLayout.WEST);
        mainPanel.add(createContent(), BorderLayout.CENTER);

        setContentPane(mainPanel);

        loadServices();
    }

    // ================= SIDEBAR =================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.NAVY);
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 25, 20));

        JLabel logo = new JLabel("CitizenConnect Pro");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionLabel = new JLabel("ADMINISTRATION");
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sectionLabel.setForeground(new Color(195, 207, 222));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(sectionLabel);
        sidebar.add(Box.createVerticalStrut(35));

        String[] menuItems = {
            "Dashboard",
            "Manage Users",
            "Manage Services",
            "Manage Applications",
            "Manage Complaints",
            "Important Information",
            "Emergency Alerts",
            "Volunteers & Feedback"
        };

        for (String item : menuItems) {

            JButton button = new JButton(item);

            button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            button.setForeground(Color.WHITE);

            button.setBackground(
                    item.equals("Manage Services")
                            ? Theme.PRIMARY
                            : Theme.NAVY
            );

            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setBorder(new EmptyBorder(12, 14, 12, 10));
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (item.equals("Dashboard")) {
                button.addActionListener(e -> openDashboard());
            }

            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(7));
        }

        sidebar.add(Box.createVerticalGlue());

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(75, 96, 119));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        sidebar.add(separator);
        sidebar.add(Box.createVerticalStrut(18));

        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(210, 220, 232));
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(roleLabel);

        return sidebar;
    }

    // ================= MAIN CONTENT =================

    private JPanel createContent() {

        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(new EmptyBorder(30, 32, 25, 32));

        content.add(createHeader(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 18));
        centerPanel.setOpaque(false);

        centerPanel.add(createToolbar(), BorderLayout.NORTH);
        centerPanel.add(createTablePanel(), BorderLayout.CENTER);

        content.add(centerPanel, BorderLayout.CENTER);

        return content;
    }

    // ================= HEADER =================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Manage Services");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(Theme.NAVY);

        JLabel subtitle = new JLabel(
                "Create, update and manage public services"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED_TEXT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(subtitle);

        JButton backButton = createButton(
                "← Back to Dashboard",
                Theme.NAVY
        );

        backButton.addActionListener(e -> openDashboard());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        return header;
    }

    // ================= TOOLBAR =================

    private JPanel createToolbar() {

        RoundedPanel toolbar = new RoundedPanel();
        toolbar.setLayout(new BorderLayout(12, 12));
        toolbar.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(260, 39));

        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.BORDER),
                        new EmptyBorder(8, 11, 8, 11)
                )
        );

        searchField.putClientProperty(
                "JTextField.placeholderText",
                "Search service name or category..."
        );

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                filterServices();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterServices();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterServices();
            }
        });

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(Theme.TEXT);

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        actionPanel.setOpaque(false);

        addButton = createButton("+ Add Service", Theme.NAVY);
        editButton = createButton("Edit Selected", Theme.NAVY);
        statusButton = createButton("Activate / Deactivate", Theme.NAVY);
        deleteButton = createButton("Delete", Theme.NAVY);
        refreshButton = createButton("Refresh", Theme.NAVY);

        addButton.addActionListener(e -> addService());
        editButton.addActionListener(e -> editService());
        statusButton.addActionListener(e -> changeServiceStatus());
        deleteButton.addActionListener(e -> deleteService());
        refreshButton.addActionListener(e -> loadServices());

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(statusButton);
        actionPanel.add(deleteButton);
        actionPanel.add(refreshButton);

        JPanel topPanel = new JPanel(new BorderLayout(10, 12));
        topPanel.setOpaque(false);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        countLabel = new JLabel("Services: 0");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countLabel.setForeground(Theme.MUTED_TEXT);

        toolbar.add(topPanel, BorderLayout.CENTER);
        toolbar.add(countLabel, BorderLayout.SOUTH);

        return toolbar;
    }

    // ================= TABLE PANEL =================

    private JPanel createTablePanel() {

        RoundedPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel heading = new JLabel("Service Records");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(Theme.NAVY);

        String[] columns = {
            "Service Name",
            "Category",
            "Application Fee",
            "Processing Time",
            "Office",
            "Status",
            "Created Date",
            "Service ID"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        servicesTable = new JTable(tableModel);

        servicesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        servicesTable.setForeground(Theme.TEXT);
        servicesTable.setBackground(Color.WHITE);
        servicesTable.setRowHeight(39);
        servicesTable.setFillsViewportHeight(true);
        servicesTable.setShowGrid(false);
        servicesTable.setIntercellSpacing(new Dimension(0, 0));
        servicesTable.setSelectionBackground(Theme.LIGHT_BLUE);
        servicesTable.setSelectionForeground(Theme.NAVY);
        servicesTable.setAutoCreateRowSorter(true);

        JTableHeader tableHeader = servicesTable.getTableHeader();

        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBackground(Theme.LIGHT_BLUE);
        tableHeader.setForeground(Theme.NAVY);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setReorderingAllowed(false);

        // Keep the MongoDB ID in the model but hide its visible column.
        servicesTable.removeColumn(
                servicesTable.getColumnModel().getColumn(7)
        );

        JScrollPane scrollPane = new JScrollPane(servicesTable);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(Theme.BORDER)
        );

        loadingLabel = new JLabel(
                "Loading services...",
                SwingConstants.CENTER
        );

        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loadingLabel.setForeground(Theme.MUTED_TEXT);
        loadingLabel.setVisible(false);

        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.setOpaque(false);

        tableArea.add(scrollPane, BorderLayout.CENTER);
        tableArea.add(loadingLabel, BorderLayout.SOUTH);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(tableArea, BorderLayout.CENTER);

        return panel;
    }

    // ================= BUTTON HELPER =================

    private JButton createButton(String text, Color background) {

        JButton button = new RoundedButton(text);

        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    // ================= LOAD SERVICES =================

    private void loadServices() {

        if (!UserSession.isAdmin()) {
            showMessage(
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        setActionsEnabled(false);

        loadingLabel.setText("Loading services...");
        loadingLabel.setVisible(true);

        SwingWorker<List<Document>, Void> worker =
                new SwingWorker<>() {

            @Override
            protected List<Document> doInBackground() {
                return repository.getAllServices(
                        UserSession.getUserId()
                );
            }

            @Override
            protected void done() {

                try {

                    List<Document> result = get();

                    allServices = result == null
                            ? new ArrayList<>()
                            : new ArrayList<>(result);

                    filterServices();

                    loadingLabel.setVisible(false);
                    setActionsEnabled(true);

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    loadingLabel.setVisible(false);
                    setActionsEnabled(true);

                    showMessage(
                            "Service loading was interrupted.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                } catch (ExecutionException ex) {

                    loadingLabel.setVisible(false);
                    setActionsEnabled(true);

                    showRepositoryError(
                            "Unable to load services.",
                            ex
                    );
                }
            }
        };

        worker.execute();
    }

    // ================= SEARCH / FILTER =================

    private void filterServices() {

        if (tableModel == null) {
            return;
        }

        tableModel.setRowCount(0);

        String keyword = searchField == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd MMM yyyy");

        int visibleCount = 0;

        for (Document service : allServices) {

            String name = getText(service, "serviceName");
            String category = getText(service, "category");
            String status = getText(service, "status");

            String searchable = (
                    name + " " + category + " " + status
            ).toLowerCase();

            if (!searchable.contains(keyword)) {
                continue;
            }

            Object feeValue = service.get("applicationFee");

            String fee = String.format(
                    "%.2f",
                    toDouble(feeValue)
            );

            Object createdValue = service.get("createdAt");

            String createdDate = "-";

            if (createdValue instanceof Date) {
                createdDate = dateFormat.format(
                        (Date) createdValue
                );
            }

            // Store the exact MongoDB ID in the hidden model column.
            tableModel.addRow(new Object[]{
                name,
                category,
                fee,
                getText(service, "processingTime"),
                getText(service, "officeName"),
                status,
                createdDate,
                getServiceId(service)
            });

            visibleCount++;
        }

        countLabel.setText(
                "Showing " + visibleCount
                        + " of " + allServices.size()
                        + " services"
        );
    }

    // ================= ADD SERVICE =================

    private void addService() {

        ServiceFormData formData = showServiceForm(null);

        if (formData == null) {
            return;
        }

        runServiceOperation(
                "Adding service...",
                () -> repository.addService(
                        formData.serviceName(),
                        formData.category(),
                        formData.description(),
                        formData.requirements(),
                        formData.applicationFee(),
                        formData.processingTime(),
                        formData.officeName(),
                        formData.applicationInstructions(),
                        UserSession.getUserId()
                ),
                "Service added successfully."
        );
    }

    // ================= EDIT SERVICE =================

    private void editService() {

        Document selected = getSelectedService();

        if (selected == null) {
            showMessage(
                    "Please select a service to edit.",
                    "No Service Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        ServiceFormData formData = showServiceForm(selected);

        if (formData == null) {
            return;
        }

        String serviceId = getServiceId(selected);

        if (serviceId == null) {
            showMessage(
                    "Unable to identify this service.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        runServiceOperation(
                "Updating service...",
                () -> repository.updateService(
                        serviceId,
                        formData.serviceName(),
                        formData.category(),
                        formData.description(),
                        formData.requirements(),
                        formData.applicationFee(),
                        formData.processingTime(),
                        formData.officeName(),
                        formData.applicationInstructions(),
                        UserSession.getUserId()
                ),
                "Service updated successfully."
        );
    }

    // ================= SERVICE FORM =================

    private ServiceFormData showServiceForm(Document existing) {

        boolean editing = existing != null;

        JTextField nameField = new JTextField(25);

        JComboBox<String> categoryBox =
                new JComboBox<>(CATEGORIES);

        JTextArea descriptionArea = new JTextArea(3, 25);
        JTextArea requirementsArea = new JTextArea(4, 25);

        JTextField feeField = new JTextField(25);
        JTextField processingField = new JTextField(25);
        JTextField officeField = new JTextField(25);

        JTextArea instructionsArea = new JTextArea(4, 25);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        requirementsArea.setLineWrap(true);
        requirementsArea.setWrapStyleWord(true);

        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);

        // Load existing data when editing.
        if (editing) {

            nameField.setText(
                    getFormText(existing, "serviceName")
            );

            String category = getFormText(existing, "category");

            if (!category.isBlank()) {

                boolean found = false;

                for (int i = 0; i < categoryBox.getItemCount(); i++) {

                    if (categoryBox.getItemAt(i)
                            .equalsIgnoreCase(category)) {

                        categoryBox.setSelectedIndex(i);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    categoryBox.addItem(category);
                    categoryBox.setSelectedItem(category);
                }
            }

            descriptionArea.setText(
                    getFormText(existing, "description")
            );

            Object requirementsValue = existing.get("requirements");

            if (requirementsValue instanceof List<?>) {

                List<?> requirements = (List<?>) requirementsValue;

                StringBuilder text = new StringBuilder();

                for (Object requirement : requirements) {

                    if (text.length() > 0) {
                        text.append("\n");
                    }

                    text.append(requirement);
                }

                requirementsArea.setText(text.toString());
            }

            feeField.setText(
                    String.valueOf(
                            toDouble(existing.get("applicationFee"))
                    )
            );

            processingField.setText(
                    getFormText(existing, "processingTime")
            );

            officeField.setText(
                    getFormText(existing, "officeName")
            );

            instructionsArea.setText(
                    getFormText(existing, "applicationInstructions")
            );
        }

        JPanel form = new JPanel(new GridBagLayout());

        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int row = 0;

        addFormRow(form, gbc, row++, "Service Name *", nameField);
        addFormRow(form, gbc, row++, "Category *", categoryBox);

        addFormRow(
                form, gbc, row++,
                "Description *",
                new JScrollPane(descriptionArea)
        );

        addFormRow(
                form, gbc, row++,
                "Requirements (one per line)",
                new JScrollPane(requirementsArea)
        );

        addFormRow(form, gbc, row++, "Application Fee *", feeField);
        addFormRow(form, gbc, row++, "Processing Time", processingField);
        addFormRow(form, gbc, row++, "Office Name", officeField);

        addFormRow(
                form, gbc, row++,
                "Application Instructions",
                new JScrollPane(instructionsArea)
        );

        JScrollPane formScroll = new JScrollPane(form);

        formScroll.setPreferredSize(new Dimension(570, 530));
        formScroll.setBorder(null);

        // Keep the same form open when validation fails.
        while (true) {

            int result = JOptionPane.showConfirmDialog(
                    this,
                    formScroll,
                    editing ? "Edit Service" : "Add New Service",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            String serviceName = nameField.getText().trim();

            String category =
                    (String) categoryBox.getSelectedItem();

            String description =
                    descriptionArea.getText().trim();

            String feeText = feeField.getText().trim();

            if (serviceName.isBlank()
                    || category == null
                    || category.isBlank()
                    || description.isBlank()
                    || feeText.isBlank()) {

                showMessage(
                        "Please fill in Service Name, Category, Description and Application Fee.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );

                continue;
            }

            double fee;

            try {

                fee = Double.parseDouble(feeText);

                if (!Double.isFinite(fee) || fee < 0) {
                    throw new NumberFormatException();
                }

            } catch (NumberFormatException ex) {

                showMessage(
                        "Application Fee must be a valid non-negative number.",
                        "Invalid Fee",
                        JOptionPane.WARNING_MESSAGE
                );

                feeField.requestFocusInWindow();
                continue;
            }

            List<String> requirements = new ArrayList<>();

            for (String line : requirementsArea.getText().split("\\R")) {

                String requirement = line.trim();

                if (!requirement.isBlank()) {
                    requirements.add(requirement);
                }
            }

            return new ServiceFormData(
                    serviceName,
                    category,
                    description,
                    requirements,
                    fee,
                    processingField.getText().trim(),
                    officeField.getText().trim(),
                    instructionsArea.getText().trim()
            );
        }
    }

    // ================= FORM ROW =================

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            JComponent component
    ) {

        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.30;

        JLabel label = new JLabel(labelText);

        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Theme.TEXT);

        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.70;

        if (component instanceof JScrollPane) {
            component.setPreferredSize(new Dimension(340, 75));
        } else {
            component.setPreferredSize(new Dimension(340, 34));
        }

        component.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        panel.add(component, gbc);
    }

    // ================= ACTIVATE / DEACTIVATE =================

    private void changeServiceStatus() {

        Document selected = getSelectedService();

        if (selected == null) {
            showMessage(
                    "Please select a service first.",
                    "No Service Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String currentStatus = getText(selected, "status");

        String newStatus = currentStatus.equalsIgnoreCase("ACTIVE")
                ? "INACTIVE"
                : "ACTIVE";

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to change this service status to "
                        + newStatus + "?",
                "Confirm Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String serviceId = getServiceId(selected);

        if (serviceId == null) {
            showMessage(
                    "Invalid service ID.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        runServiceOperation(
                "Updating service status...",
                () -> repository.updateServiceStatus(
                        serviceId,
                        newStatus,
                        UserSession.getUserId()
                ),
                "Service status changed to " + newStatus + "."
        );
    }

    // ================= DELETE SERVICE =================

    private void deleteService() {

        Document selected = getSelectedService();

        if (selected == null) {
            showMessage(
                    "Please select a service to delete.",
                    "No Service Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String serviceName = getText(selected, "serviceName");

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this service?\n\n"
                        + serviceName
                        + "\n\nThis action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String serviceId = getServiceId(selected);

        if (serviceId == null) {
            showMessage(
                    "Invalid service ID.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        runServiceOperation(
                "Deleting service...",
                () -> repository.deleteService(
                        serviceId,
                        UserSession.getUserId()
                ),
                "Service deleted successfully."
        );
    }

    // ================= ASYNC OPERATION =================

    @FunctionalInterface
    private interface ServiceOperation {
        Object execute() throws Exception;
    }

    private void runServiceOperation(
            String loadingText,
            ServiceOperation operation,
            String successMessage
    ) {

        setActionsEnabled(false);

        loadingLabel.setText(loadingText);
        loadingLabel.setVisible(true);

        SwingWorker<Object, Void> worker = new SwingWorker<>() {

            @Override
            protected Object doInBackground() throws Exception {
                return operation.execute();
            }

            @Override
            protected void done() {

                loadingLabel.setVisible(false);
                setActionsEnabled(true);

                try {

                    Object result = get();

                    if (result instanceof Boolean
                            && !((Boolean) result)) {

                        showMessage(
                                "No service record was changed. Please refresh and try again.",
                                "Operation Not Completed",
                                JOptionPane.WARNING_MESSAGE
                        );

                    } else {

                        showMessage(
                                successMessage,
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        loadServices();
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showMessage(
                            "The operation was interrupted.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                } catch (ExecutionException ex) {

                    showRepositoryError(
                            "Unable to complete the operation.",
                            ex
                    );
                }
            }
        };

        worker.execute();
    }

    // ================= SELECTED SERVICE =================

    private Document getSelectedService() {

        int selectedRow = servicesTable.getSelectedRow();

        if (selectedRow < 0) {
            return null;
        }

        // Convert the sorted view row to the original model row.
        int modelRow = servicesTable.convertRowIndexToModel(selectedRow);

        // Read the hidden MongoDB ID from the model.
        Object idValue = tableModel.getValueAt(modelRow, 7);

        if (idValue == null) {
            return null;
        }

        String selectedId = idValue.toString();

        for (Document service : allServices) {

            String serviceId = getServiceId(service);

            if (selectedId.equals(serviceId)) {
                return service;
            }
        }

        return null;
    }

    // ================= SERVICE ID =================

    private String getServiceId(Document service) {

        if (service == null) {
            return null;
        }

        Object id = service.get("_id");

        if (id instanceof ObjectId) {
            return id.toString();
        }

        return id == null ? null : id.toString();
    }

    // ================= ENABLE / DISABLE ACTIONS =================

    private void setActionsEnabled(boolean enabled) {

        if (addButton != null) {
            addButton.setEnabled(enabled);
        }

        if (editButton != null) {
            editButton.setEnabled(enabled);
        }

        if (statusButton != null) {
            statusButton.setEnabled(enabled);
        }

        if (deleteButton != null) {
            deleteButton.setEnabled(enabled);
        }

        if (refreshButton != null) {
            refreshButton.setEnabled(enabled);
        }
    }

    // ================= BACK TO DASHBOARD =================

    private void openDashboard() {

        if (!UserSession.isAdmin()) {

            showMessage(
                    "Admin access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        if (dashboardFrame != null
                && dashboardFrame.isDisplayable()) {

            dashboardFrame.setVisible(true);
            dashboardFrame.setExtendedState(JFrame.NORMAL);
            dashboardFrame.toFront();
            dashboardFrame.requestFocus();

        } else {

            AdminDashboardFrame newDashboard =
                    new AdminDashboardFrame();

            newDashboard.setVisible(true);
        }

        dispose();
    }

    // ================= HELPERS =================

    // For displaying values in the table.
    private String getText(Document document, String field) {

        Object value = document.get(field);

        if (value == null || value.toString().isBlank()) {
            return "-";
        }

        return value.toString();
    }

    // For loading values into editable form fields.
    // Missing values should be empty, not "-".
    private String getFormText(Document document, String field) {

        Object value = document.get(field);

        if (value == null) {
            return "";
        }

        return value.toString();
    }

    private double toDouble(Object value) {

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value == null) {
            return 0.0;
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private void showMessage(
            String message,
            String title,
            int messageType
    ) {

        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                messageType
        );
    }

    // ================= ERROR HANDLING =================

    private void showRepositoryError(
            String title,
            Exception ex
    ) {

        Throwable cause = ex;

        while (cause.getCause() != null
                && cause.getCause() != cause) {

            cause = cause.getCause();
        }

        String message = cause.getMessage();

        if (message == null || message.isBlank()) {
            message = "An unexpected error occurred.";
        }

        showMessage(
                title + "\n\n" + message,
                "Service Management Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
