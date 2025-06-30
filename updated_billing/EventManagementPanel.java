import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
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

    private String getOrganizerUsername(int organizerId) {
        String username = "Organizer"; // default if not found
        try (Connection conn = getConnection()) {
            String sql = "SELECT username FROM admin WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, organizerId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    username = rs.getString("username");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return username;
    }
    
    // UI Component Creation Methods
    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(NAV_BAR_COLOR);
        navBar.setPreferredSize(new Dimension(getWidth(), 70)); // Slightly reduced height
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Main container for left-aligned content
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        
        // Main title
        JLabel titleLabel = new JLabel("MMU Event Management System");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        // User info text
        String username = getOrganizerUsername(currentOrganizerId);
        JLabel userInfoLabel = new JLabel("<html><div style='font-size:12px; margin-top:3px;'>" +
                                        "Logged in as: <b>" + username + "</b> || " +
                                        "Role: <span style='color:#4CAF50;'>Organizer</span></div></html>");
        userInfoLabel.setForeground(new Color(220, 220, 220));
        userInfoLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        
        leftPanel.add(titleLabel, BorderLayout.NORTH);
        leftPanel.add(userInfoLabel, BorderLayout.CENTER);
        
        navBar.add(leftPanel, BorderLayout.WEST);
        
        // Navigation buttons
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navButtons.setOpaque(false);
        
        navButtons.add(createNavButton("Notifications", e -> showNotifications()));
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
        // Create the dialog but don't show it yet
        EventDialog dialog = new EventDialog("Add New Event", 
            (name, price, date, venue, capacity, category, isActive, reason, services, discounts) -> {
                saveNewEvent(name, price, date, venue, capacity, category, isActive, reason, services, discounts);
            }, 
            false);
        
        // Show the type selection dialog first
        dialog.showEventTypeDialog();
    }

    private void showUpdateEventDialog(ActionEvent e) {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an event to update");
            return;
        }

        int eventId = (int) tableModel.getValueAt(selectedRow, 0);
        double currentPrice = Double.parseDouble(((String) tableModel.getValueAt(selectedRow, 4)).replace(",", ""));
        
        // Create dialog but don't show it yet
        EventDialog dialog = new EventDialog("Update Event", 
            (name, newPrice, date, venue, capacity, category, isActive, reason, services, discounts) -> {
                updateEvent(eventId, name, newPrice, date, venue, capacity, category, isActive, reason, services, discounts);
                loadEventsFromDatabase();
            },
            true);
        
        // FIRST populate the fields
        dialog.populateFields(
            (String) tableModel.getValueAt(selectedRow, 1),
            currentPrice,
            ((Date) tableModel.getValueAt(selectedRow, 2)).toString(),
            (String) tableModel.getValueAt(selectedRow, 3),
            (int) tableModel.getValueAt(selectedRow, 5),
            (String) tableModel.getValueAt(selectedRow, 7),
            ((String) tableModel.getValueAt(selectedRow, 8)).equals("Available")
        );
        
        // THEN load services and discounts
        loadEventServices(eventId, dialog);
        loadEventDiscounts(eventId, dialog);
        
        // FINALLY setup and show the dialog
        dialog.setupMainDialog();
    }

    private void loadEventDiscounts(int eventId, EventDialog dialog) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT discount_type, discount_value FROM discounts WHERE event_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, eventId);
                ResultSet rs = stmt.executeQuery();
                
                List<Discount> discounts = new ArrayList<>();
                while (rs.next()) {
                    discounts.add(new Discount(
                        rs.getString("discount_type"),
                        rs.getDouble("discount_value")
                    ));
                }
                
                // Populate discounts in the dialog
                dialog.setDiscounts(discounts);
            }
        } catch (SQLException ex) {
            showError("Error loading discounts: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadEventServices(int eventId, EventDialog dialog) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT service_name, price FROM services WHERE event_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, eventId);
                ResultSet rs = stmt.executeQuery();
                
                List<Service> services = new ArrayList<>();
                while (rs.next()) {
                    services.add(new Service(
                        rs.getString("service_name"),
                        rs.getDouble("price")
                    ));
                }
                
                // Populate services in the dialog
                dialog.setServices(services);
            }
        } catch (SQLException ex) {
            showError("Error loading services: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Core Business Logic Methods
    private void saveNewEvent(String name, double price, String date, String venue, 
        int capacity, String category, boolean isActive, String reason,
        List<Service> additionalServices, List<Discount> discounts) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Handle empty date
                String eventDate = date;
                if (date == null || date.trim().isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, 1);
                    eventDate = sdf.format(cal.getTime());
                }
                
                // 1. Insert the main event
                String eventSql = "INSERT INTO events (event_name, price, event_date, venue, capacity, " +
                                "available_seats, category, is_active, organizer_id) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                int eventId;
                try (PreparedStatement stmt = conn.prepareStatement(eventSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, name);
                    stmt.setDouble(2, price);
                    stmt.setString(3, eventDate);
                    stmt.setString(4, venue);
                    stmt.setInt(5, capacity);
                    stmt.setInt(6, capacity);
                    stmt.setString(7, category);
                    stmt.setBoolean(8, isActive);
                    stmt.setInt(9, currentOrganizerId);
                    
                    stmt.executeUpdate();
                    
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            eventId = rs.getInt(1);
                        } else {
                            throw new SQLException("Failed to get event ID");
                        }
                    }
                }
                
                // 2. Insert additional services directly linked to event
                if (!additionalServices.isEmpty()) {
                    String serviceSql = "INSERT INTO services (event_id, service_name, price) " +
                                    "VALUES (?, ?, ?)";
                    
                    try (PreparedStatement stmt = conn.prepareStatement(serviceSql)) {
                        for (Service service : additionalServices) {
                            stmt.setInt(1, eventId);
                            stmt.setString(2, service.getName());
                            stmt.setDouble(3, service.getPrice());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }
                
                // 3. Insert discounts directly linked to event
                if (!discounts.isEmpty()) {
                    String discountSql = "INSERT INTO discounts (event_id, discount_type, discount_value) " +
                                    "VALUES (?, ?, ?)";
                    
                    try (PreparedStatement stmt = conn.prepareStatement(discountSql)) {
                        for (Discount discount : discounts) {
                            stmt.setInt(1, eventId);
                            stmt.setString(2, discount.getType());
                            stmt.setDouble(3, discount.getValue());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }
                
                conn.commit();
                showSuccess("Event added successfully!");
                loadEventsFromDatabase();
                
            } catch (SQLException ex) {
                conn.rollback();
                showError("Error saving event: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            showError("Connection Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateEvent(int eventId, String name, double price, String date, String venue, 
   int capacity, String category, boolean isActive, String reason,
   List<Service> additionalServices, List<Discount> discounts) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try {
                // 1. Get current event data and registrations
                String oldName = "";
                double oldPrice = 0;
                String oldDate = "";
                String oldVenue = "";
                int oldCapacity = 0;
                String oldCategory = "";
                List<Registration> registrations = new ArrayList<>();
                
                String selectSql = "SELECT e.event_name, e.price, e.event_date, e.venue, " +
                                "e.capacity, e.category, e.is_active, " +
                                "er.id as registration_id, er.user_id " +
                                "FROM events e " +
                                "LEFT JOIN event_registration er ON e.event_id = er.event_id " +
                                "WHERE e.event_id = ?";
                
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, eventId);
                    ResultSet rs = selectStmt.executeQuery();
                    if (rs.next()) {
                        oldName = rs.getString("event_name");
                        oldPrice = rs.getDouble("price");
                        oldDate = rs.getString("event_date");
                        oldVenue = rs.getString("venue");
                        oldCapacity = rs.getInt("capacity");
                        oldCategory = rs.getString("category");
                        boolean wasActive = rs.getBoolean("is_active");
                        
                        do {
                            if (rs.getInt("registration_id") > 0) {
                                registrations.add(new Registration(
                                    rs.getInt("registration_id"),
                                    rs.getInt("user_id")
                                ));
                            }
                        } while (rs.next());
                    }
                }

                // 2. Update the event
                String updateSql = "UPDATE events SET " +
                            "event_name = ?, price = ?, event_date = ?, venue = ?, " +
                            "capacity = ?, available_seats = ?, category = ?, " +
                            "is_active = ?, deactive_reason = ? WHERE event_id = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
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
                    
                    stmt.executeUpdate();
                }

                // 3. Handle services - first delete all existing, then insert new ones
                List<Service> oldServices = new ArrayList<>();
                String selectServicesSql = "SELECT service_name, price FROM services WHERE event_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(selectServicesSql)) {
                    stmt.setInt(1, eventId);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        oldServices.add(new Service(rs.getString("service_name"), rs.getDouble("price")));
                    }
                }
                
                String deleteServicesSql = "DELETE FROM services WHERE event_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteServicesSql)) {
                    stmt.setInt(1, eventId);
                    stmt.executeUpdate();
                }
                
                if (!additionalServices.isEmpty()) {
                    String insertServiceSql = "INSERT INTO services (event_id, service_name, price) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertServiceSql)) {
                        for (Service service : additionalServices) {
                            stmt.setInt(1, eventId);
                            stmt.setString(2, service.getName());
                            stmt.setDouble(3, service.getPrice());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                // 4. Handle discounts - first delete all existing, then insert new ones
                List<Discount> oldDiscounts = new ArrayList<>();
                String selectDiscountsSql = "SELECT discount_type, discount_value FROM discounts WHERE event_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(selectDiscountsSql)) {
                    stmt.setInt(1, eventId);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        oldDiscounts.add(new Discount(rs.getString("discount_type"), rs.getDouble("discount_value")));
                    }
                }
                
                String deleteDiscountsSql = "DELETE FROM discounts WHERE event_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteDiscountsSql)) {
                    stmt.setInt(1, eventId);
                    stmt.executeUpdate();
                }
                
                if (!discounts.isEmpty()) {
                    String insertDiscountSql = "INSERT INTO discounts (event_id, discount_type, discount_value) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertDiscountSql)) {
                        for (Discount discount : discounts) {
                            stmt.setInt(1, eventId);
                            stmt.setString(2, discount.getType());
                            stmt.setDouble(3, discount.getValue());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                // 5. Create notifications for changes
                if (!registrations.isEmpty()) {
                    // Check for name change
                    if (!name.equals(oldName)) {
                        String message = String.format("Event name changed from '%s' to '%s'", oldName, name);
                        createChangeNotifications(conn, registrations, "event_name_change", message);
                    }
                    
                    // Check for price change
                    if (Math.abs(price - oldPrice) > 0.01) {
                        String message = String.format("Event price changed from RM%.2f to RM%.2f", oldPrice, price);
                        createChangeNotifications(conn, registrations, "event_price_change", message);
                    }
                    
                    // Check for date change
                    if (!date.equals(oldDate)) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            java.util.Date oldDateObj = sdf.parse(oldDate);
                            java.util.Date newDateObj = sdf.parse(date);
                            
                            Calendar oldCal = Calendar.getInstance();
                            oldCal.setTime(oldDateObj);
                            
                            Calendar newCal = Calendar.getInstance();
                            newCal.setTime(newDateObj);
                            
                            // Only notify if day, month or year changed (ignore time changes)
                            if (oldCal.get(Calendar.DAY_OF_MONTH) != newCal.get(Calendar.DAY_OF_MONTH) ||
                                oldCal.get(Calendar.MONTH) != newCal.get(Calendar.MONTH) ||
                                oldCal.get(Calendar.YEAR) != newCal.get(Calendar.YEAR)) {
                                
                                String message = String.format("Event date changed from %s to %s", oldDate, date);
                                createChangeNotifications(conn, registrations, "event_date_change", message);
                            }
                        } catch (ParseException e) {
                            // If date parsing fails, just skip the notification
                            e.printStackTrace();
                        }
                    }
                    
                    // Check for venue change
                    if (!venue.equals(oldVenue)) {
                        String message = String.format("Event venue changed from '%s' to '%s'", oldVenue, venue);
                        createChangeNotifications(conn, registrations, "event_venue_change", message);
                    }
                    
                    // Check for capacity change
                    if (capacity != oldCapacity) {
                        String message = String.format("Event capacity changed from %d to %d", oldCapacity, capacity);
                        createChangeNotifications(conn, registrations, "event_capacity_change", message);
                    }
                    
                    // Check for category change
                    if (!category.equals(oldCategory)) {
                        String message = String.format("Event category changed from '%s' to '%s'", oldCategory, category);
                        createChangeNotifications(conn, registrations, "event_category_change", message);
                    }
                    
                    // Check for services changes
                    if (!servicesEqual(oldServices, additionalServices)) {
                        String message = "Additional services for the event have been updated";
                        createChangeNotifications(conn, registrations, "event_services_change", message);
                    }
                    
                    // Check for discounts changes
                    if (!discountsEqual(oldDiscounts, discounts)) {
                        String message = "Discounts for the event have been updated";
                        createChangeNotifications(conn, registrations, "event_discounts_change", message);
                    }
                }
                
                conn.commit(); // Commit transaction
                showSuccess("Event updated successfully!");
                loadEventsFromDatabase();
                
            } catch (SQLException ex) {
                conn.rollback();
                showError("Error updating event: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            showError("Connection Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean servicesEqual(List<Service> list1, List<Service> list2) {
        if (list1.size() != list2.size()) return false;
        
        for (Service s1 : list1) {
            boolean found = false;
            for (Service s2 : list2) {
                if (s1.getName().equals(s2.getName()) && 
                    Math.abs(s1.getPrice() - s2.getPrice()) < 0.01) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private boolean discountsEqual(List<Discount> list1, List<Discount> list2) {
        if (list1.size() != list2.size()) return false;
        
        for (Discount d1 : list1) {
            boolean found = false;
            for (Discount d2 : list2) {
                if (d1.getType().equals(d2.getType()) && 
                    Math.abs(d1.getValue() - d2.getValue()) < 0.0001) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private void createChangeNotifications(Connection conn, List<Registration> registrations, 
                                    String notifType, String message) throws SQLException {
        String sql = "INSERT INTO notifications " +
                    "(notif_message, notif_type, recipient_type, " +
                    "event_registration_id, user_id, recipient_user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Registration reg : registrations) {
                stmt.setString(1, message);
                stmt.setString(2, notifType);
                stmt.setString(3, "user");
                stmt.setInt(4, reg.registrationId);
                stmt.setInt(5, reg.userId);
                stmt.setInt(6, reg.userId);
                stmt.addBatch();
            }
            stmt.executeBatch();
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
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // 1. First get all registration data including user IDs
                String eventName = "";
                List<Registration> registrations = new ArrayList<>();
                
                String selectSql = "SELECT e.event_name, er.id as registration_id, er.user_id " +
                            "FROM events e " +
                            "LEFT JOIN event_registration er ON e.event_id = er.event_id " +
                            "WHERE e.event_id = ?";
                
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, eventId);
                    ResultSet rs = selectStmt.executeQuery();
                    
                    boolean eventExists = false;
                    while (rs.next()) {
                        eventExists = true;
                        eventName = rs.getString("event_name");
                        if (rs.getInt("registration_id") > 0) {
                            registrations.add(new Registration(
                                rs.getInt("registration_id"),
                                rs.getInt("user_id")
                            ));
                        }
                    }
                    
                    if (!eventExists) {
                        showError("No event found with ID: " + eventId);
                        return;
                    }
                }
                
                // 2. Create participant notifications FIRST (while registrations still exist)
                if (!registrations.isEmpty()) {
                    String participantNotifSql = "INSERT INTO notifications " +
                                            "(notif_message, notif_type, recipient_type, " +
                                            "event_registration_id, user_id, recipient_user_id) " +
                                            "VALUES (?, ?, ?, ?, ?, ?)";
                    
                    try (PreparedStatement notifStmt = conn.prepareStatement(participantNotifSql)) {
                        for (Registration reg : registrations) {
                            System.out.println("Creating notification for user_id: " + reg.userId); // Debug
                            notifStmt.setString(1, "Event '" + eventName + "' has been cancelled");
                            notifStmt.setString(2, "cancellation");
                            notifStmt.setString(3, "user");
                            notifStmt.setInt(4, reg.registrationId);
                            notifStmt.setInt(5, reg.userId);
                            notifStmt.setInt(6, reg.userId);
                            notifStmt.addBatch();
                        }
                        notifStmt.executeBatch();
                    }
                }
                
                // 3. THEN delete the registrations
                if (!registrations.isEmpty()) {
                    String deleteRegSql = "DELETE FROM event_registration WHERE event_id = ?";
                    try (PreparedStatement deleteRegStmt = conn.prepareStatement(deleteRegSql)) {
                        deleteRegStmt.setInt(1, eventId);
                        deleteRegStmt.executeUpdate();
                    }
                }
                
                // 4. Create admin notification
                String adminNotifSql = "INSERT INTO notifications " +
                                    "(notif_message, notif_type, recipient_type, " +
                                    "recipient_admin_id, event_registration_id, user_id) " +
                                    "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement adminNotifStmt = conn.prepareStatement(adminNotifSql)) {
                    Registration firstReg = registrations.isEmpty() ? null : registrations.get(0);
                    
                    adminNotifStmt.setString(1, "You cancelled event '" + eventName + "'");
                    adminNotifStmt.setString(2, "cancellation");
                    adminNotifStmt.setString(3, "admin");
                    adminNotifStmt.setInt(4, currentOrganizerId);
                    
                    if (firstReg != null) {
                        System.out.println("Admin notification with user_id: " + firstReg.userId); // Debug
                        adminNotifStmt.setInt(5, firstReg.registrationId);
                        adminNotifStmt.setInt(6, firstReg.userId);
                    } else {
                        adminNotifStmt.setNull(5, Types.INTEGER);
                        adminNotifStmt.setNull(6, Types.INTEGER);
                    }
                    adminNotifStmt.executeUpdate();
                }
                
                // 5. Finally delete the event
                String deleteEventSql = "DELETE FROM events WHERE event_id = ?";
                try (PreparedStatement deleteEventStmt = conn.prepareStatement(deleteEventSql)) {
                    deleteEventStmt.setInt(1, eventId);
                    int rowsAffected = deleteEventStmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        conn.commit();
                        showSuccess("Event deleted successfully");
                        loadEventsFromDatabase();
                    } else {
                        conn.rollback();
                        showError("Failed to delete event");
                    }
                }
            } catch (SQLException ex) {
                try { conn.rollback(); } catch (SQLException e) {}
                showError("Database Error: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException e) {}
            }
        } catch (SQLException ex) {
            showError("Connection Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static class Registration {
        int registrationId;
        int userId;
        
        public Registration(int registrationId, int userId) {
            this.registrationId = registrationId;
            this.userId = userId;
        }
    }

    private static class Discount {
        private final String type;
        private final double value; // decimal (e.g. 0.1 for 10%)
        
        public Discount(String type, double value) {
            this.type = type;
            this.value = value;
        }
        
        public String getType() { return type; }
        public double getValue() { return value; }
    }
    
    // Utility Methods
    private void showNotifications() {
        new showNotificationsAdmin(currentOrganizerId).showNotificationsAdmin();
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
        private final JPanel additionalServicesPanel = new JPanel();
        private final JComboBox<String> discountCombo = new JComboBox<>(new String[]{"None", "Early Bird (10%)", "Group Registration (15%)", "Student Discount (20%)"});
        private final JPanel discountsPanel = new JPanel();
        private List<Discount> discounts = new ArrayList<>();
        private final EventSaveHandler saveHandler;
        private boolean isPaidEvent = false;
        private List<Service> additionalServices = new ArrayList<>();
        private final boolean isUpdateMode;

        interface EventSaveHandler {
            void handle(String name, double price, String date, String venue, 
                        int capacity, String category, boolean isActive, String reason,
                        List<Service> additionalServices, List<Discount> discounts);  // Changed parameter type
        }

        public void setDiscounts(List<Discount> discounts) {
            this.discounts = new ArrayList<>(discounts);
            refreshDiscountsList();
        }

        public void setServices(List<Service> services) {
            this.additionalServices = new ArrayList<>(services);
            refreshServicesList();
        }

        public void setPaidStatus(boolean isPaid) {
            this.isPaidEvent = isPaid;
            
            // Enable/disable price field
            priceField.setEnabled(isPaid);
            if (!isPaid) {
                priceField.setText("0.00");
            }
            
            // Enable/disable additional services components
            additionalServicesPanel.setEnabled(isPaid);
            setPanelEnabled(additionalServicesPanel, isPaid);
            additionalServicesPanel.setBorder(BorderFactory.createTitledBorder(
                isPaid ? "Additional Services" : "Additional Services (Not available for free events)"));
            
            // Enable/disable discounts components
            discountsPanel.setEnabled(isPaid);
            setPanelEnabled(discountsPanel, isPaid);
            discountsPanel.setBorder(BorderFactory.createTitledBorder(
                isPaid ? "Available Discounts" : "Discounts (Not available for free events)"));
            
            // Repack the dialog to adjust size
            pack();
        }

        private void setPanelEnabled(JPanel panel, boolean enabled) {
            panel.setEnabled(enabled);
            for (Component component : panel.getComponents()) {
                component.setEnabled(enabled);
                component.setForeground(enabled ? Color.BLACK : Color.GRAY);
                if (component instanceof JPanel) {
                    setPanelEnabled((JPanel) component, enabled);
                }
            }
        }

         public EventDialog(String title, EventSaveHandler saveHandler, boolean isUpdateMode) {
            super((Frame)SwingUtilities.getWindowAncestor(EventManagementPanel.this), title, true);
            this.saveHandler = saveHandler;
            this.isUpdateMode = isUpdateMode;
            
            // Initialize components only - don't show anything yet
            initializeComponents();
            
            // Don't show any dialogs here - let the calling code control the flow
        }

        private void showEventTypeDialog() {
            JDialog typeDialog = new JDialog(this, "Event Type", true);
            typeDialog.setLayout(new BorderLayout());
            typeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            
            JPanel typePanel = new JPanel(new BorderLayout(10, 10));
            typePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel titleLabel = new JLabel("Select Event Type", JLabel.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
            JButton freeBtn = createTypeButton("FREE EVENT", new Color(76, 175, 80));
            JButton paidBtn = createTypeButton("PAID EVENT", new Color(33, 150, 243));
            
            buttonPanel.add(freeBtn);
            buttonPanel.add(paidBtn);
            
            typePanel.add(titleLabel, BorderLayout.NORTH);
            typePanel.add(buttonPanel, BorderLayout.CENTER);
            
            typeDialog.add(typePanel);
            typeDialog.pack();
            typeDialog.setLocationRelativeTo(EventManagementPanel.this);
            
            freeBtn.addActionListener(e -> {
                isPaidEvent = false;
                typeDialog.dispose();
                setupMainDialog(); // Now show the main dialog
            });
            
            paidBtn.addActionListener(e -> {
                isPaidEvent = true;
                typeDialog.dispose();
                setupMainDialog(); // Now show the main dialog
            });
            
            typeDialog.setVisible(true);
        }

        public void setupMainDialog() {
            setupLayout();
            setPreferredSize(new Dimension(600, 800)); // Just a starting size
            pack(); // This will size the dialog based on its content
            setMinimumSize(new Dimension(500, 500)); // Set a reasonable minimum
            setResizable(true);
            setLocationRelativeTo(EventManagementPanel.this);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            
            // Clear any existing data if this is a new dialog
            if (!isUpdateMode) {
                nameField.setText("");
                priceField.setText("0.00");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 1);
                dateField.setText(sdf.format(cal.getTime()));
                venueField.setText("");
                capacityField.setText("");
                categoryCombo.setSelectedIndex(0);
                activeCheckBox.setSelected(true);
                reasonField.setText("");
                additionalServices.clear();
                discounts.clear();
                refreshServicesList();
                refreshDiscountsList();
            }
            
            setVisible(true);
        }

        private JButton createTypeButton(String text, Color color) {
            JButton button = new JButton(text);
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            return button;
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

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 1);
            dateField.setText(sdf.format(cal.getTime()));
            
            activeCheckBox.addActionListener(e -> {
                reasonField.setVisible(!activeCheckBox.isSelected());
                pack();
            });

            additionalServicesPanel.setLayout(new BoxLayout(additionalServicesPanel, BoxLayout.Y_AXIS));
            additionalServicesPanel.setBorder(BorderFactory.createTitledBorder("Additional Services"));
            discountsPanel.setLayout(new BoxLayout(discountsPanel, BoxLayout.Y_AXIS));
            discountsPanel.setBorder(BorderFactory.createTitledBorder("Available Discounts"));
            
            // Discount combo setup
            discountCombo.setFont(new Font("Arial", Font.PLAIN, 13));
            additionalServicesPanel.setVisible(true);
            discountsPanel.setVisible(true);
            discountCombo.setVisible(true);
            
            // Add service button
            JButton addServiceBtn = new JButton("Add Service");
            addServiceBtn.addActionListener(e -> showAddServiceDialog());
            addServiceBtn.setVisible(isPaidEvent);

            // Add discount button
            JButton addDiscountBtn = new JButton("Add Discount");
            addDiscountBtn.addActionListener(e -> showAddDiscountDialog());
            addDiscountBtn.setVisible(isPaidEvent);
        }

        private void showAddDiscountDialog() {
            JDialog discountDialog = new JDialog(this, "Add Discount", true);
            discountDialog.setLayout(new BorderLayout(10, 10));
            discountDialog.setSize(400, 200); // Reduced height
            discountDialog.setLocationRelativeTo(this);
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            JComboBox<String> discountTypeCombo = new JComboBox<>(new String[]{
                "Early Bird", 
                "Group Registration", 
                "Student Discount"
            });
            
            JTextField discountValueField = new JTextField(20);
            
            addFormField(formPanel, gbc, "Discount Type:", discountTypeCombo, 0);
            addFormField(formPanel, gbc, "Discount Value (%):", discountValueField, 1);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancelBtn = new JButton("Cancel");
            JButton saveBtn = new JButton("Save");
            
            cancelBtn.addActionListener(e -> discountDialog.dispose());
            saveBtn.addActionListener(e -> {
                try {
                    String type = (String) discountTypeCombo.getSelectedItem();
                    double value = Double.parseDouble(discountValueField.getText());
                    
                    if (value <= 0 || value >= 100) {
                        showError("Discount value must be between 0 and 100");
                        return;
                    }
                    
                    Discount discount = new Discount(type, value/100); // No description
                    discounts.add(discount);
                    refreshDiscountsList();
                    discountDialog.dispose();
                } catch (NumberFormatException ex) {
                    showError("Please enter a valid discount percentage");
                }
            });
            
            buttonPanel.add(cancelBtn);
            buttonPanel.add(saveBtn);
            
            discountDialog.add(formPanel, BorderLayout.CENTER);
            discountDialog.add(buttonPanel, BorderLayout.SOUTH);
            discountDialog.setVisible(true);
        }


        private void showAddServiceDialog() {
            JDialog serviceDialog = new JDialog(this, "Add Additional Service", true);
            serviceDialog.setLayout(new BorderLayout(10, 10));
            serviceDialog.setSize(400, 200); // Reduced height
            serviceDialog.setLocationRelativeTo(this);
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            JTextField serviceNameField = new JTextField(20);
            JTextField servicePriceField = new JTextField(20);
            
            addFormField(formPanel, gbc, "Service Name:", serviceNameField, 0);
            addFormField(formPanel, gbc, "Price (RM):", servicePriceField, 1);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancelBtn = new JButton("Cancel");
            JButton saveBtn = new JButton("Save");
            
            cancelBtn.addActionListener(e -> serviceDialog.dispose());
            saveBtn.addActionListener(e -> {
                try {
                    String name = serviceNameField.getText().trim();
                    double price = Double.parseDouble(servicePriceField.getText());
                    
                    if (name.isEmpty()) {
                        showError("Service name cannot be empty");
                        return;
                    }
                    
                    Service service = new Service(name, price); // No description
                    additionalServices.add(service);
                    refreshServicesList();
                    serviceDialog.dispose();
                } catch (NumberFormatException ex) {
                    showError("Please enter a valid price");
                }
            });
            
            buttonPanel.add(cancelBtn);
            buttonPanel.add(saveBtn);
            
            serviceDialog.add(formPanel, BorderLayout.CENTER);
            serviceDialog.add(buttonPanel, BorderLayout.SOUTH);
            serviceDialog.setVisible(true);
        }

        private void refreshServicesList() {
            additionalServicesPanel.removeAll();
            
            if (additionalServices.isEmpty()) {
                additionalServicesPanel.add(new JLabel("No additional services added"));
            } else {
                for (Service service : additionalServices) {
                    JPanel servicePanel = new JPanel(new BorderLayout());
                    servicePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    
                    JLabel serviceLabel = new JLabel(String.format(
                        "<html><b>%s</b> - RM %.2f</html>",
                        service.getName(),
                        service.getPrice()
                    ));
                    
                    JButton removeBtn = new JButton("Remove");
                    removeBtn.addActionListener(e -> {
                        additionalServices.remove(service);
                        refreshServicesList();
                        pack(); // Adjust dialog size after removal
                    });
                    
                    servicePanel.add(serviceLabel, BorderLayout.CENTER);
                    servicePanel.add(removeBtn, BorderLayout.EAST);
                    additionalServicesPanel.add(servicePanel);
                }
            }
            
            additionalServicesPanel.revalidate();
            additionalServicesPanel.repaint();
        }

        private void refreshDiscountsList() {
            discountsPanel.removeAll();
            
            if (discounts.isEmpty()) {
                discountsPanel.add(new JLabel("No discounts added"));
            } else {
                for (Discount discount : discounts) {
                    JPanel discountPanel = new JPanel(new BorderLayout());
                    discountPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    
                    JLabel discountLabel = new JLabel(String.format(
                        "<html><b>%s</b> - %.1f%% off</html>",
                        discount.getType(),
                        discount.getValue() * 100
                    ));
                    
                    JButton removeBtn = new JButton("Remove");
                    removeBtn.addActionListener(e -> {
                        discounts.remove(discount);
                        refreshDiscountsList();
                        pack(); // Adjust dialog size after removal
                    });
                    
                    discountPanel.add(discountLabel, BorderLayout.CENTER);
                    discountPanel.add(removeBtn, BorderLayout.EAST);
                    discountsPanel.add(discountPanel);
                }
            }
            
            discountsPanel.revalidate();
            discountsPanel.repaint();
        }

        public void populateFields(String name, double price, String date, String venue, 
        int capacity, String category, boolean isActive) {
            nameField.setText(name);
            priceField.setText(String.format("%.2f", price));
            dateField.setText(date);
            venueField.setText(venue);
            capacityField.setText(String.valueOf(capacity));
            categoryCombo.setSelectedItem(category);
            
            // Determine if this is a paid event based on price
            setPaidStatus(price > 0);
            
            if (isUpdateMode) {
                activeCheckBox.setSelected(isActive);
                reasonField.setVisible(!isActive);
            }
            
            // Ensure UI is updated
            revalidate();
            repaint();
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
            
            // Paid event components (always visible but disabled for free events)
            gbc.gridy = 9;
            panel.add(createLabel("Additional Services:"), gbc);
            
            gbc.gridy = 10;
            panel.add(additionalServicesPanel, gbc);
            
            JButton addServiceBtn = new JButton("Add Service");
            addServiceBtn.addActionListener(e -> {
                if (isPaidEvent) showAddServiceDialog();
            });
            addServiceBtn.setEnabled(isPaidEvent);
            gbc.gridy = 11;
            panel.add(addServiceBtn, gbc);
            
            // Discounts
            gbc.gridy = 12;
            panel.add(createLabel("Available Discounts:"), gbc);
            
            gbc.gridy = 13;
            panel.add(discountsPanel, gbc);
            
            JButton addDiscountBtn = new JButton("Add Discount");
            addDiscountBtn.addActionListener(e -> {
                if (isPaidEvent) showAddDiscountDialog();
            });
            addDiscountBtn.setEnabled(isPaidEvent);
            gbc.gridy = 14;
            panel.add(addDiscountBtn, gbc);
            
            // Buttons - fixed position at the bottom
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
            buttonPanel.setBackground(BACKGROUND_COLOR);
            
            JButton saveButton = createDialogButton("Save", PRIMARY_COLOR, e -> {
                saveEvent();
                dispose();
            });
            
            JButton cancelButton = createDialogButton("Cancel", new Color(150, 150, 150), e -> {
                dispose();
            });
            
            buttonPanel.add(cancelButton);
            buttonPanel.add(saveButton);
            
            // Add the button panel at a fixed position
            gbc.gridy = 15;
            gbc.gridwidth = 2;
            gbc.weighty = 1.0; // Push buttons to bottom
            gbc.anchor = GridBagConstraints.SOUTH;
            panel.add(buttonPanel, gbc);
            
            add(panel);
            
            // Set initial state
            setPaidStatus(isPaidEvent);
        }

        private void saveEvent() {
            try {
                // Validate inputs
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                java.util.Date utilDate = sdf.parse(dateField.getText());
                java.sql.Date selectedDate = new java.sql.Date(utilDate.getTime());
                
                // Get today's date without time component
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                java.sql.Date todayDate = new java.sql.Date(today.getTimeInMillis());
                
                // Compare dates
                if (selectedDate.before(todayDate)) {
                    showError("Event date cannot be earlier than today");
                    return;
                }
                
                double price = priceField.getText().trim().isEmpty() ? 0.0 : 
                            Double.parseDouble(priceField.getText());
                int capacity = Integer.parseInt(capacityField.getText());
                
                // Automatically determine if event is paid based on price
                isPaidEvent = price > 0;
                
                String reason = activeCheckBox.isSelected() ? null : reasonField.getText().trim();
                if (!activeCheckBox.isSelected() && reason.isEmpty()) {
                    showError("Please enter a deactivation reason");
                    return;
                }
                
                // Call save handler with additional parameters
                saveHandler.handle(
                    nameField.getText(),
                    price,
                    dateField.getText(),  // Keep as String for consistency
                    venueField.getText(),
                    capacity,
                    (String) categoryCombo.getSelectedItem(),
                    activeCheckBox.isSelected(),
                    reason,
                    isPaidEvent ? additionalServices : new ArrayList<>(),
                    isPaidEvent ? discounts : new ArrayList<>()
                );
                
                // Only dispose if everything was successful
                dispose();
            } catch (ParseException ex) {
                showError("Please enter date in YYYY-MM-DD format");
            } catch (NumberFormatException ex) {
                showError("Please enter valid numbers for price and capacity");
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
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