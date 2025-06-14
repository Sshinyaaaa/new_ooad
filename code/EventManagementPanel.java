import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.awt.event.ActionListener;
import java.text.ParseException;

public class EventManagementPanel extends JPanel {
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/register_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    
    // UI Components
    private final DefaultTableModel tableModel;
    private final JTable eventTable;
    private final int currentOrganizerId;
    
    // Constants
    private static final String[] EVENT_CATEGORIES = {"Seminars", "Workshops", "Cultural Events", "Sports Events"};
    private static final String[] COLUMN_NAMES = {"ID", "Event Name", "Date", "Venue", "Price (RM)", "Capacity", "Available", "Category", "Availability"};
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SECONDARY_COLOR = new Color(100, 150, 200);
    private static final Color DANGER_COLOR = new Color(220, 80, 60);
    private static final Color NAV_BAR_COLOR = new Color(50, 100, 150);
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);

    public EventManagementPanel(int organizerId) {
        this.currentOrganizerId = organizerId;
        this.tableModel = createTableModel();
        this.eventTable = createEventTable();
        
        initializePanel();
        loadEventsFromDatabase();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        setBackground(BACKGROUND_COLOR);
        
        add(createNavBar(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
    }

    // UI Component Creation Methods
    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(NAV_BAR_COLOR);
        navBar.setPreferredSize(new Dimension(getWidth(), 60));
        navBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        
        // Title
        JLabel titleLabel = new JLabel("MMU Event Management System");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        navBar.add(titleLabel, BorderLayout.WEST);
        
        // Navigation buttons
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        navButtons.setOpaque(false);
        
        navButtons.add(createNavButton("Notifications", e -> showNotifications()));
        navButtons.add(createNavButton("Billing", e -> showBillingPanel()));
        navButtons.add(createNavButton("Logout", e -> logout()));
        
        navBar.add(navButtons, BorderLayout.EAST);
        return navBar;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        toolbarPanel.setBackground(BACKGROUND_COLOR);
        
        toolbarPanel.add(createActionButton("New Event", PRIMARY_COLOR, this::showNewEventDialog));
        toolbarPanel.add(createActionButton("Update Event", SECONDARY_COLOR, this::showUpdateEventDialog));
        toolbarPanel.add(createActionButton("Remove Event", DANGER_COLOR, this::removeSelectedEvent));
        
        // Table
        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        contentPanel.add(toolbarPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        return contentPanel;
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4 || columnIndex == 5 || columnIndex == 6) {
                    return Double.class;
                }
                return String.class;
            }
        };
    }

    private JTable createEventTable() {
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        
        // Hide ID column
        table.removeColumn(table.getColumnModel().getColumn(0));
        
        // Set column widths
        int[] columnWidths = {180, 100, 150, 80, 70, 70, 120};
        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
        
        // Center-align numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 3; i <= 5; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        return table;
    }

    // Button Creation Methods
    private JButton createNavButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(new Color(80, 140, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 90, 150), 1),
            BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));
        button.addActionListener(listener);
        return button;
    }

    private JButton createActionButton(String text, Color color, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        button.addActionListener(listener);
        return button;
    }

    // Database Operations
    private void loadEventsFromDatabase() {
        try (Connection conn = getConnection()) {
            String sql = "SELECT event_id, event_name, event_date, venue, price, capacity, available_seats, category, is_active FROM events";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                tableModel.setRowCount(0);

                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("event_id"),
                        rs.getString("event_name"),
                        rs.getDate("event_date"),
                        rs.getString("venue"),
                        String.format("%.2f", rs.getDouble("price")),
                        rs.getInt("capacity"),
                        rs.getInt("available_seats"),
                        rs.getString("category"),
                        rs.getBoolean("is_active") ? "Available" : "Closed"
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            showError("Error loading events: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Event Dialog Methods
    private void showNewEventDialog(ActionEvent e) {
        EventDialog dialog = new EventDialog("Add New Event", this::saveNewEvent);
        dialog.setVisible(true);
    }

    private void showUpdateEventDialog(ActionEvent e) {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an event to update");
            return;
        }

        int eventId = (int) tableModel.getValueAt(selectedRow, 0);
        EventDialog dialog = new EventDialog("Update Event", (name, price, date, venue, capacity, category, isActive, reason) -> {
            updateEvent(eventId, name, price, date, venue, capacity, category, isActive, reason);
            loadEventsFromDatabase();
        });
        
        // Populate with existing data
        dialog.populateFields(
            (String) tableModel.getValueAt(selectedRow, 1),
            Double.parseDouble(((String) tableModel.getValueAt(selectedRow, 4)).replace(",", "")),
            ((Date) tableModel.getValueAt(selectedRow, 2)).toString(),
            (String) tableModel.getValueAt(selectedRow, 3),
            (int) tableModel.getValueAt(selectedRow, 5),
            (String) tableModel.getValueAt(selectedRow, 7),
            ((String) tableModel.getValueAt(selectedRow, 8)).equals("Available")
        );
        
        dialog.setVisible(true);
    }

    // Core Business Logic Methods
    private void saveNewEvent(String name, double price, String date, String venue, 
                            int capacity, String category, boolean isActive, String reason) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO events (event_name, price, event_date, venue, capacity, " +
                        "available_seats, category, is_active, organizer_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setDouble(2, price);
                stmt.setString(3, date);
                stmt.setString(4, venue);
                stmt.setInt(5, capacity);
                stmt.setInt(6, capacity);
                stmt.setString(7, category);
                stmt.setBoolean(8, isActive);
                stmt.setInt(9, currentOrganizerId);
                
                if (stmt.executeUpdate() > 0) {
                    showSuccess("Event added successfully!");
                    loadEventsFromDatabase();
                }
            }
        } catch (SQLException ex) {
            showError("Error saving event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateEvent(int eventId, String name, double price, String date, String venue, 
                           int capacity, String category, boolean isActive, String reason) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE events SET " +
                        "event_name = ?, price = ?, event_date = ?, venue = ?, " +
                        "capacity = ?, available_seats = ?, category = ?, " +
                        "is_active = ?, deactive_reason = ? WHERE event_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setDouble(2, price);
                stmt.setString(3, date);
                stmt.setString(4, venue);
                stmt.setInt(5, capacity);
                stmt.setInt(6, capacity);
                stmt.setString(7, category);
                stmt.setBoolean(8, isActive);
                stmt.setString(9, isActive ? null : reason);
                stmt.setInt(10, eventId);
                
                if (stmt.executeUpdate() > 0) {
                    showSuccess("Event updated successfully!");
                } else {
                    showError("No changes were made to the event");
                }
            }
        } catch (SQLException ex) {
            showError("Error updating event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void removeSelectedEvent(ActionEvent e) {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an event to remove");
            return;
        }
        
        String eventName = (String) tableModel.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "WARNING: This will permanently delete '" + eventName + "' and ALL its registrations!\n" +
            "This action cannot be undone.\n\n" +
            "Are you absolutely sure?",
            "Confirm Permanent Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int eventId = (int) tableModel.getValueAt(selectedRow, 0);
            removeEvent(eventId);
        }
    }

    private void removeEvent(int eventId) {
        try (Connection conn = getConnection()) {
            String sql = "DELETE FROM events WHERE event_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, eventId);
                
                if (stmt.executeUpdate() > 0) {
                    showSuccess("Event and all related registrations deleted successfully!");
                    loadEventsFromDatabase();
                } else {
                    showError("No event found with that ID");
                }
            }
        } catch (SQLException ex) {
            showError("Error deleting event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Utility Methods
    private void showNotifications() {
        JOptionPane.showMessageDialog(this, "Notifications will appear here", 
            "Notifications", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showBillingPanel() {
        JOptionPane.showMessageDialog(this, "Billing functionality will be implemented here", 
            "Billing", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to logout?", "Confirm Logout", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
                EventQueue.invokeLater(() -> new LoginRegisterScreen().setVisible(true));
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    // Inner class for Event Dialog
    private class EventDialog extends JDialog {
        private final JTextField nameField = new JTextField(20);
        private final JTextField priceField = new JTextField(20);
        private final JTextField dateField = new JTextField(20);
        private final JTextField venueField = new JTextField(20);
        private final JTextField capacityField = new JTextField(20);
        private final JComboBox<String> categoryCombo = new JComboBox<>(EVENT_CATEGORIES);
        private final JCheckBox activeCheckBox = new JCheckBox("Active Event");
        private final JTextArea reasonField = new JTextArea(3, 30);
        
        private final EventSaveHandler saveHandler;

        interface EventSaveHandler {
            void handle(String name, double price, String date, String venue, 
                        int capacity, String category, boolean isActive, String reason);
        }

        public EventDialog(String title, EventSaveHandler saveHandler) {
            super((Frame)SwingUtilities.getWindowAncestor(EventManagementPanel.this), title, true);
            this.saveHandler = saveHandler;
            
            setSize(500, 550);
            setLocationRelativeTo(EventManagementPanel.this);
            setResizable(false);
            
            initializeComponents();
            setupLayout();
        }

        private void initializeComponents() {
            // Configure components
            nameField.setFont(new Font("Arial", Font.PLAIN, 13));
            priceField.setFont(new Font("Arial", Font.PLAIN, 13));
            priceField.setToolTipText("Leave empty for free event (RM 0.00)");
            dateField.setFont(new Font("Arial", Font.PLAIN, 13));
            dateField.setToolTipText("Enter date as YYYY-MM-DD");
            venueField.setFont(new Font("Arial", Font.PLAIN, 13));
            capacityField.setFont(new Font("Arial", Font.PLAIN, 13));
            categoryCombo.setFont(new Font("Arial", Font.PLAIN, 13));
            activeCheckBox.setFont(new Font("Arial", Font.PLAIN, 13));
            activeCheckBox.setSelected(true);
            reasonField.setFont(new Font("Arial", Font.PLAIN, 13));
            reasonField.setLineWrap(true);
            reasonField.setWrapStyleWord(true);
            reasonField.setVisible(false);
            
            activeCheckBox.addActionListener(e -> {
                reasonField.setVisible(!activeCheckBox.isSelected());
                pack();
            });
        }

        public void populateFields(String name, double price, String date, String venue, 
                                int capacity, String category, boolean isActive) {
            nameField.setText(name);
            priceField.setText(String.format("%.2f", price));
            dateField.setText(date);
            venueField.setText(venue);
            capacityField.setText(String.valueOf(capacity));
            categoryCombo.setSelectedItem(category);
            activeCheckBox.setSelected(isActive);
            reasonField.setVisible(!isActive);
        }

        private void setupLayout() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.setBackground(BACKGROUND_COLOR);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            
            // Add form fields
            addFormField(panel, gbc, "Event Name:", nameField, 0);
            addFormField(panel, gbc, "Price (RM):", priceField, 1);
            addFormField(panel, gbc, "Event Date (YYYY-MM-DD):", dateField, 2);
            addFormField(panel, gbc, "Venue:", venueField, 3);
            addFormField(panel, gbc, "Capacity:", capacityField, 4);
            
            // Category
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 1;
            panel.add(createLabel("Category:"), gbc);
            gbc.gridx = 1;
            panel.add(categoryCombo, gbc);
            
            // Active checkbox
            gbc.gridx = 0;
            gbc.gridy = 6;
            gbc.gridwidth = 2;
            panel.add(activeCheckBox, gbc);
            
            // Reason field (hidden by default)
            JLabel reasonLabel = createLabel("Deactivation Reason:");
            reasonLabel.setVisible(!activeCheckBox.isSelected());
            gbc.gridy = 7;
            panel.add(reasonLabel, gbc);
            
            gbc.gridy = 8;
            panel.add(new JScrollPane(reasonField), gbc);
            
            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
            buttonPanel.setBackground(BACKGROUND_COLOR);
            
            JButton saveButton = createDialogButton("Save", PRIMARY_COLOR, e -> saveEvent());
            JButton cancelButton = createDialogButton("Cancel", new Color(150, 150, 150), e -> dispose());
            
            buttonPanel.add(cancelButton);
            buttonPanel.add(saveButton);
            
            gbc.gridy = 9;
            gbc.gridwidth = 2;
            panel.add(buttonPanel, gbc);
            
            add(panel);
        }

        private void saveEvent() {
            try {
                // Validate date format
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                sdf.parse(dateField.getText());
                
                // Parse inputs
                double price = priceField.getText().trim().isEmpty() ? 0.0 : 
                              Double.parseDouble(priceField.getText());
                int capacity = Integer.parseInt(capacityField.getText());
                
                // Validate deactivation reason if needed
                String reason = activeCheckBox.isSelected() ? null : reasonField.getText().trim();
                if (!activeCheckBox.isSelected() && reason.isEmpty()) {
                    showError("Please enter a deactivation reason");
                    return;
                }
                
                // Call save handler
                saveHandler.handle(
                    nameField.getText(),
                    price,
                    dateField.getText(),
                    venueField.getText(),
                    capacity,
                    (String) categoryCombo.getSelectedItem(),
                    activeCheckBox.isSelected(),
                    reason
                );
                
                dispose();
            } catch (ParseException ex) {
                showError("Please enter date in YYYY-MM-DD format (e.g., 2023-12-31)");
            } catch (NumberFormatException ex) {
                showError("Please enter valid numbers for price and capacity");
            }
        }

        private JLabel createLabel(String text) {
            JLabel label = new JLabel(text);
            label.setFont(new Font("Arial", Font.BOLD, 13));
            return label;
        }

        private JButton createDialogButton(String text, Color color, ActionListener listener) {
            JButton button = new JButton(text);
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
            button.addActionListener(listener);
            return button;
        }

        private void addFormField(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int row) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 1;
            panel.add(createLabel(label), gbc);
            
            gbc.gridx = 1;
            panel.add(field, gbc);
        }
    }
}