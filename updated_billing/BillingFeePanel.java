import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class BillingFeePanel extends JPanel {
    // Constants
    private static final String DB_URL = "jdbc:mysql://localhost:3306/register_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color NAV_BAR_COLOR = new Color(50, 100, 150);
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    
    // Components
    private final DefaultTableModel tableModel;
    private final JTable billingTable;
    private final int currentUserId;
    private final JLabel totalBeforeDiscountLabel;
    private final JLabel discountAmountLabel;
    private final JLabel netPayableLabel;
    
    public BillingFeePanel(int userId) {
        this.currentUserId = userId;
        this.tableModel = createTableModel();
        this.billingTable = createBillingTable();
        this.totalBeforeDiscountLabel = new JLabel("RM 0.00");
        this.discountAmountLabel = new JLabel("RM 0.00");
        this.netPayableLabel = new JLabel("RM 0.00");
        
        initializeUI();
        loadBillingDataFromDatabase();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        setBackground(BACKGROUND_COLOR);
        
        add(createNavBar(), BorderLayout.NORTH);
        add(createMainContentPanel(), BorderLayout.CENTER);
    }

    private String getParticipantUsername(int userId) {
        String username = "Participant"; // default if not found
        try (Connection conn = getConnection()) {
            String sql = "SELECT username FROM users WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
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

    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(NAV_BAR_COLOR);
        navBar.setPreferredSize(new Dimension(getWidth(), 70));
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Billing & Payments");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        String username = getParticipantUsername(currentUserId);
        JLabel userInfoLabel = new JLabel("<html><div style='font-size:12px; margin-top:3px;'>" +
                                        "Logged in as: <b>" + username + "</b> || " +
                                        "Role: <span style='color:#4CAF50;'>Participant</span></div></html>");
        userInfoLabel.setForeground(new Color(220, 220, 220));
        userInfoLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        
        leftPanel.add(titleLabel, BorderLayout.NORTH);
        leftPanel.add(userInfoLabel, BorderLayout.CENTER);
        
        navBar.add(leftPanel, BorderLayout.WEST);
        
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navButtons.setOpaque(false);
        
        navButtons.add(createNavButton("Notifications", e -> showNotifications()));
        navButtons.add(createNavButton("Events", e -> showEvents()));
        navButtons.add(createNavButton("Logout", e -> logout()));
        
        navBar.add(navButtons, BorderLayout.EAST);
        return navBar;
    }

    private DefaultTableModel createTableModel() {
      String[] columns = {"ID", "Event Name", "Date", "Base Fee (RM)", "Additional Services", "Discount", "Status"};
      return new DefaultTableModel(columns, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
              return false;
          }
          
          @Override
          public Class<?> getColumnClass(int columnIndex) {
              if (columnIndex == 3) return Double.class;
              return String.class;
          }
      };
  }

    private JTable createBillingTable() {
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        
        // Hide ID column
        table.removeColumn(table.getColumnModel().getColumn(0));
        
        // Set column widths
        int[] widths = {180, 100, 80, 120, 100, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        
        // Center-align numeric and status columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        
        return table;
    }

    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Add table with scroll pane
        mainPanel.add(new JScrollPane(billingTable), BorderLayout.CENTER);
        
        // Add summary panel at the bottom
        mainPanel.add(createSummaryPanel(), BorderLayout.SOUTH);
        
        return mainPanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Payment Summary"),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        summaryPanel.setBackground(BACKGROUND_COLOR);
        
        // Style for labels
        Font labelFont = new Font("Arial", Font.BOLD, 13);
        Font valueFont = new Font("Arial", Font.PLAIN, 13);
        
        // Total before discount
        JLabel totalLabel = new JLabel("Total Amount Before Discount:");
        totalLabel.setFont(labelFont);
        summaryPanel.add(totalLabel);
        
        totalBeforeDiscountLabel.setFont(valueFont);
        totalBeforeDiscountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        summaryPanel.add(totalBeforeDiscountLabel);
        
        // Discount amount
        JLabel discountLabel = new JLabel("Discount Amount:");
        discountLabel.setFont(labelFont);
        summaryPanel.add(discountLabel);
        
        discountAmountLabel.setFont(valueFont);
        discountAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        summaryPanel.add(discountAmountLabel);
        
        // Net payable
        JLabel netPayableTitleLabel = new JLabel("Net Payable Amount:");
        netPayableTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryPanel.add(netPayableTitleLabel);
        
        netPayableLabel.setFont(new Font("Arial", Font.BOLD, 14));
        netPayableLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        summaryPanel.add(netPayableLabel);
        
        // Payment button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        JButton payButton = new JButton("Make Payment");
        styleButton(payButton, PRIMARY_COLOR);
        payButton.addActionListener(e -> processPayment());
        buttonPanel.add(payButton);
        
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);
        container.add(summaryPanel, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);
        
        return container;
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

    private void loadBillingDataFromDatabase() {
      List<BillingFee> billingFees = new ArrayList<>();
      double totalBeforeDiscount = 0;
      double totalDiscount = 0;
      double totalServices = 0;
      
      try (Connection conn = getConnection()) {
        // Modified query without payment_status
        String sql = "SELECT er.id AS registration_id, e.event_name, e.event_date, e.price, " +
          "GROUP_CONCAT(DISTINCT s.service_name SEPARATOR ', ') AS services, " +
          "SUM(DISTINCT s.price) AS services_total, " +
          "GROUP_CONCAT(DISTINCT d.discount_type SEPARATOR ', ') AS discount_types, " +
          "SUM(DISTINCT CASE WHEN d.discount_type = 'percentage' THEN e.price * d.discount_value / 100 " +
          "ELSE d.discount_value END) AS discount_total " +
          "FROM event_registration er " +
          "JOIN events e ON er.event_id = e.event_id " +
          "LEFT JOIN services s ON e.event_id = s.event_id " +
          "LEFT JOIN discounts d ON e.event_id = d.event_id " +
          "WHERE er.user_id = ? " +  // Removed payment_status condition
          "GROUP BY er.id, e.event_name, e.event_date, e.price";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
          stmt.setInt(1, currentUserId);
          ResultSet rs = stmt.executeQuery();
          
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
          
          while (rs.next()) {
            String services = rs.getString("services");
            double servicesPrice = rs.getDouble("services_total");
            if (rs.wasNull()) {
              servicesPrice = 0;
              services = "None";
            }
            
            double discount = rs.getDouble("discount_total");
            if (rs.wasNull()) {
              discount = 0;
            }
            
            BillingFee fee = new BillingFee(
              rs.getInt("registration_id"),
              rs.getString("event_name"),
              dateFormat.format(rs.getDate("event_date")),
              rs.getDouble("price") + servicesPrice,
              services + " (RM " + String.format("%.2f", servicesPrice) + ")",
              discount,
              "Pending"  // Default status since we don't have payment_status
            );
            
            billingFees.add(fee);
            totalBeforeDiscount += fee.getBaseFee();

            double basePrice = rs.getDouble("price");
            double totalBaseFee = basePrice + servicesPrice;
            double discountfee = totalBaseFee * discount;
            totalDiscount += discountfee;
          }
        }
        
        // Update table
        tableModel.setRowCount(0);
        for (BillingFee fee : billingFees) {
          tableModel.addRow(new Object[]{
            fee.getRegistrationId(),
            fee.getEventName(),
            fee.getEventDate(),
            String.format("%.2f", fee.getBaseFee()),
            fee.getAdditionalServices(),
            String.format("%.2f", fee.getDiscount()),
            fee.getStatus()
          });
        }
        
        // Update summary labels
        totalBeforeDiscountLabel.setText(String.format("RM %.2f", totalBeforeDiscount));
        discountAmountLabel.setText(String.format("RM %.2f", totalDiscount));
        netPayableLabel.setText(String.format("RM %.2f", totalBeforeDiscount - totalDiscount));
          
      } catch (SQLException ex) {
        showError("Error loading billing data: " + ex.getMessage());
        ex.printStackTrace();
      }
    }

    private void processPayment() {
      double netPayable = Double.parseDouble(netPayableLabel.getText().replace("RM ", ""));
    
    if (netPayable <= 0) {
        showError("No payment required");
        return;
    }
    
    // Show "Not Implemented" message
    JOptionPane.showMessageDialog(
        this,
        "Payment not implemented...",
        "Not Implemented",
        JOptionPane.INFORMATION_MESSAGE
    );
  }

    private void showNotifications() {
        new ShowUserNotifications(currentUserId).showNotifications();
    }
    
    private void showEvents() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new BrowseEventsPanel(currentUserId));
            frame.revalidate();
            frame.repaint();
        }
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