import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class BrowseEventsPanel extends JPanel {
    // Constants
    private static final String DB_URL = "jdbc:mysql://localhost:3306/register_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color NAV_BAR_COLOR = new Color(50, 100, 150);
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    
    // Components
    private final DefaultTableModel tableModel;
    private final JTable eventTable;
    private final int currentUserId;
    
    public BrowseEventsPanel(int userId) {
        this.currentUserId = userId;
        this.tableModel = createTableModel();
        this.eventTable = createEventTable();
        
        initializeUI();
        loadEventsFromDatabase();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        setBackground(BACKGROUND_COLOR);
        
        add(createNavBar(), BorderLayout.NORTH);
        add(new JScrollPane(eventTable), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    // UI Component Creation
    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(NAV_BAR_COLOR);
        navBar.setPreferredSize(new Dimension(getWidth(), 60));
        navBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        
        JLabel titleLabel = new JLabel("Browse");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        navBar.add(titleLabel, BorderLayout.WEST);
        
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        navButtons.setOpaque(false);
        
        navButtons.add(createNavButton("Notifications", e -> showNotifications()));
        navButtons.add(createNavButton("Billing", e -> showBilling()));
        navButtons.add(createNavButton("Logout", e -> logout()));
        
        navBar.add(navButtons, BorderLayout.EAST);
        return navBar;
    }

    private DefaultTableModel createTableModel() {
        String[] columns = {"ID", "Event Name", "Date", "Venue", "Price (RM)", "Available", "Category"};
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Double.class;
                if (columnIndex == 5) return Integer.class;
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
        int[] widths = {180, 100, 150, 80, 70, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        
        // Center-align numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        return table;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        
        JButton registerButton = new JButton("Register for Selected Event");
        styleButton(registerButton, PRIMARY_COLOR);
        registerButton.addActionListener(e -> handleRegistration());
        
        panel.add(registerButton);
        return panel;
    }

    // Button Styling
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

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
    }

    // Event Handling
    private void handleRegistration() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Please select an event first");
            return;
        }
        
        int eventId = (int) tableModel.getValueAt(selectedRow, 0);
        String eventName = (String) tableModel.getValueAt(selectedRow, 1);
        double price = (double) tableModel.getValueAt(selectedRow, 4);
        
        if (price == 0) {
            registerForFreeEvent(eventId, eventName);
        } else {
            handlePaidEventRegistration(eventId, price, eventName);
        }
    }

    // Database Operations
    private void loadEventsFromDatabase() {
        try (Connection conn = getConnection()) {
            String sql = "SELECT event_id, event_name, event_date, venue, price, available_seats, category " +
                         "FROM events WHERE is_active = TRUE AND available_seats > 0";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                tableModel.setRowCount(0);
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("event_id"),
                        rs.getString("event_name"),
                        dateFormat.format(rs.getDate("event_date")),
                        rs.getString("venue"),
                        rs.getDouble("price"),
                        rs.getInt("available_seats"),
                        rs.getString("category"),
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            showError("Error loading events: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void registerForFreeEvent(int eventId, String eventName) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                if (isAlreadyRegistered(conn, eventId)) {
                    showError("You are already registered for this event");
                    return;
                }

                int availableSeats = getAvailableSeats(conn, eventId);
                if (availableSeats <= 0) {
                    showError("No available seats left for this event");
                    return;
                }

                int registrationId = createRegistration(conn, eventId);
                int newSeatCount = updateAvailableSeats(conn, eventId);

                if (newSeatCount == 0) {
                    deactivateEvent(conn, eventId);
                }

                createOrganizerNotification(conn, getOrganizerIdForEvent(conn, eventId), 
                                         registrationId, eventName);

                conn.commit();
                showSuccess("Registration successful!");
                loadEventsFromDatabase();
            } catch (SQLException ex) {
                conn.rollback();
                showError("Registration failed: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean isAlreadyRegistered(Connection conn, int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM event_registration WHERE event_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            stmt.setInt(2, currentUserId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private int getAvailableSeats(Connection conn, int eventId) throws SQLException {
        String sql = "SELECT available_seats FROM events WHERE event_id = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int createRegistration(Connection conn, int eventId) throws SQLException {
        String sql = "INSERT INTO event_registration (event_id, user_id, registration_date) " +
                    "VALUES (?, ?, CURRENT_DATE)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, eventId);
            stmt.setInt(2, currentUserId);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Could not get registration ID");
        }
    }

    private int updateAvailableSeats(Connection conn, int eventId) throws SQLException {
        String updateSql = "UPDATE events SET available_seats = available_seats - 1 WHERE event_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setInt(1, eventId);
            stmt.executeUpdate();
        }
        
        String checkSql = "SELECT available_seats FROM events WHERE event_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("available_seats");
            }
            throw new SQLException("Could not get updated seat count");
        }
    }

    private void deactivateEvent(Connection conn, int eventId) throws SQLException {
        String sql = "UPDATE events SET is_active = FALSE WHERE event_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            stmt.executeUpdate();
        }
    }

    private int getOrganizerIdForEvent(Connection conn, int eventId) throws SQLException {
        String sql = "SELECT organizer_id FROM events WHERE event_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("organizer_id") : -1;
        }
    }

    private void createOrganizerNotification(Connection conn, int organizerId, 
                                          int registrationId, String eventName) throws SQLException {
        String message = String.format(
            "New registration for your event: %s (Registration ID: %d)", 
            eventName, registrationId);
        
        String sql = "INSERT INTO notifications " +
                    "(user_id, notif_message, notif_type, recipient_type, recipient_admin_id) " +
                    "VALUES (?, ?, ?, 'admin', ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            stmt.setString(2, message);
            stmt.setString(3, "registration");
            stmt.setInt(4, organizerId);
            stmt.executeUpdate();
        }
    }

    private void handlePaidEventRegistration(int eventId, double price, String eventName) {
        String message = String.format(
            "This is a paid event (RM %.2f).\n\n" +
            "Payment functionality would be implemented here.\n" +
            "You would be redirected to a payment gateway.", price);
        
        JOptionPane.showMessageDialog(this, message, 
            "Payment Required for: " + eventName, JOptionPane.INFORMATION_MESSAGE);
    }

    // Utility Methods
    private void showNotifications() {
        JOptionPane.showMessageDialog(this, 
            "Notification functionality would be implemented here", 
            "Notifications", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showBilling() {
        JOptionPane.showMessageDialog(this, 
            "Billing and payment history would be shown here", 
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
}