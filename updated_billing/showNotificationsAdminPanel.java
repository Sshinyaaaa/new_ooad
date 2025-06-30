import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.awt.event.ActionListener;

public class showNotificationsAdminPanel extends JPanel {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/register_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    
    private final DefaultTableModel tableModel;
    private final JTable notificationsTable;
    private final int currentOrganizerId;
    
    private static final String[] COLUMN_NAMES = {"ID", "Message", "Type", "Event", "User"};
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    
    public showNotificationsAdminPanel(int organizerId) {
        this.currentOrganizerId = organizerId;
        this.tableModel = createTableModel();
        this.notificationsTable = createNotificationsTable();
        
        initializePanel();
        loadNotificationsFromDatabase();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(BACKGROUND_COLOR);
        
        // Title
        JLabel titleLabel = new JLabel("Your Notifications", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Toolbar - Now only contains the close button
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolbarPanel.setBackground(BACKGROUND_COLOR);
        
        JButton closeButton = createActionButton("Close", new Color(150, 150, 150), e -> closeWindow());
        toolbarPanel.add(closeButton);
        
        // Table
        JScrollPane scrollPane = new JScrollPane(notificationsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(toolbarPanel, BorderLayout.SOUTH);
    }
    
    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
    
    private JTable createNotificationsTable() {
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        
        // Hide ID column
        table.removeColumn(table.getColumnModel().getColumn(0));
        
        // Set column widths
        int[] columnWidths = {300, 100, 150, 150};
        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
        
        // Center-align type column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        // Custom renderer for message column to wrap text
        table.getColumnModel().getColumn(0).setCellRenderer(new TextAreaRenderer());
        
        return table;
    }
    
    private void loadNotificationsFromDatabase() {
        try (Connection conn = getConnection()) {
            String sql = "SELECT n.id, n.notif_message, n.notif_type, " +
                         "er.event_id, e.event_name, " +
                         "u.id as user_id, u.username " +
                         "FROM notifications n " +
                         "LEFT JOIN event_registration er ON n.event_registration_id = er.id " +
                         "LEFT JOIN events e ON er.event_id = e.event_id " +
                         "LEFT JOIN users u ON n.user_id = u.id " +
                         "WHERE n.recipient_type = 'admin' AND n.recipient_admin_id = ? " +
                         "ORDER BY n.id DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, currentOrganizerId);
                ResultSet rs = stmt.executeQuery();
                tableModel.setRowCount(0);
                
                while (rs.next()) {
                    String eventInfo = "N/A";
                    if (rs.getInt("event_id") > 0) {
                        eventInfo = rs.getString("event_name") + " (ID: " + rs.getInt("event_id") + ")";
                    }
                    
                    String userInfo = "N/A";
                    if (rs.getInt("user_id") > 0) {
                        userInfo = rs.getString("username") + " (ID: " + rs.getInt("user_id") + ")";
                    }
                    
                    Object[] row = {
                        rs.getInt("id"),
                        rs.getString("notif_message"),
                        rs.getString("notif_type"),
                        eventInfo,
                        userInfo
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            showError("Error loading notifications: " + ex.getMessage());
            ex.printStackTrace();
        }
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
    
    private void closeWindow() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
    
    private static class TextAreaRenderer extends DefaultTableCellRenderer {
        private final JTextArea textArea;
        
        public TextAreaRenderer() {
            textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            textArea.setFont(getFont());
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                                                     boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                textArea.setText(value.toString());
                
                int lineHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
                int textLength = textArea.getText().length();
                int lines = textLength / 30;
                
                if (lines > 0 && table.getRowHeight(row) < lineHeight * (lines + 1)) {
                    table.setRowHeight(row, lineHeight * (lines + 1));
                }
            }
            
            if (isSelected) {
                textArea.setBackground(table.getSelectionBackground());
                textArea.setForeground(table.getSelectionForeground());
            } else {
                textArea.setBackground(table.getBackground());
                textArea.setForeground(table.getForeground());
            }
            
            return textArea;
        }
    }
}