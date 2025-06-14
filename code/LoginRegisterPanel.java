import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import java.awt.image.BufferedImage;

public class LoginRegisterPanel extends JPanel {
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/register_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    // UI Components
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton loginButton = createStyledButton("Login", new Color(70, 130, 180));
    private final JButton registerButton = createStyledButton("Register", new Color(60, 179, 113));

    public LoginRegisterPanel() {
        initializePanel();
        setupLayout();
        setupEventHandlers();
    }

    private void initializePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(240, 240, 240));
    }

    private void setupLayout() {
        // Main form panel
        JPanel formPanel = createFormPanel();
        
        // Center the form vertically
        add(Box.createVerticalGlue());
        add(wrapInContainer(formPanel));
        add(Box.createVerticalGlue());
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(500, 400));
        panel.setMaximumSize(panel.getPreferredSize());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Add components to form
        addLogoAndTitle(panel, gbc);
        addInputFields(panel, gbc);
        addButtons(panel, gbc);

        return panel;
    }

    private void addLogoAndTitle(JPanel panel, GridBagConstraints gbc) {
        JLabel logoLabel = new JLabel(createScaledIcon("photo/mmu.png", 150, 150));
        JLabel titleLabel = new JLabel("Campus Event Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(createLogoPanel(logoLabel), gbc);

        gbc.gridy = 1;
        panel.add(titleLabel, gbc);
    }

    private JPanel createLogoPanel(JLabel logoLabel) {
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(240, 240, 240));
        logoPanel.add(logoLabel);
        return logoPanel;
    }

    private void addInputFields(JPanel panel, GridBagConstraints gbc) {
        // Username
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(createInputLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(configureInputField(usernameField), gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(createInputLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(configureInputField(passwordField), gbc);
    }

    private JLabel createInputLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        return label;
    }

    private JComponent configureInputField(JComponent field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(200, 25));
        return field;
    }

    private void addButtons(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(createButtonPanel(), gbc);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(new Color(240, 240, 240));
        panel.add(loginButton);
        panel.add(registerButton);
        return panel;
    }

    private JPanel wrapInContainer(JPanel content) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(240, 240, 240));
        container.add(content, BorderLayout.CENTER);
        return container;
    }

    private void setupEventHandlers() {
        loginButton.addActionListener(e -> loginUser());
        registerButton.addActionListener(e -> registerUser());
    }

    // Database operations
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    private void loginUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        if (validateInputs(username, password)) return;
        
        try (Connection conn = getConnection()) {
            if (!checkEOCredentials(conn, username, password)) {
                checkParticipantCredentials(conn, username, password);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private boolean checkEOCredentials(Connection conn, String username, String password) throws SQLException {
        String query = "SELECT id FROM admin WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    openEventOrganizerApplication(rs.getInt("id"));
                    return true;
                }
                return false;
            }
        }
    }

    private void checkParticipantCredentials(Connection conn, String username, String password) throws SQLException {
        String query = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    openParticipantApplication(rs.getInt("id"));
                } else {
                    showError("Invalid username or password");
                }
            }
        }
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        if (validateInputs(username, password)) return;
        
        try (Connection conn = getConnection()) {
            if (usernameExists(conn, username)) {
                showError("Username already exists");
                return;
            }
            
            int userId = createNewUser(conn, username, password);
            if (userId > 0) {
                openParticipantApplication(userId);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private int createNewUser(Connection conn, String username, String password) throws SQLException {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            if (stmt.executeUpdate() > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        return keys.getInt(1);
                    }
                }
            }
            showError("Registration failed");
            return -1;
        }
    }

    // Navigation methods
    private void openEventOrganizerApplication(int organizerId) {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.dispose();
        new EventManagementSystem(organizerId).setVisible(true);
    }

    private void openParticipantApplication(int userId) {
        SwingUtilities.invokeLater(() -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (topFrame != null) topFrame.dispose();
            
            SwingUtilities.invokeLater(() -> {
                try {
                    new BrowseEvents(userId).setVisible(true);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Failed to open event browser", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        });
    }

    // Utility methods
    private ImageIcon createScaledIcon(String path, int width, int height) {
        try {
            ImageIcon icon = new ImageIcon(path);
            return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return new ImageIcon(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        }
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private boolean validateInputs(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return true;
        }
        return false;
    }

    private boolean usernameExists(Connection conn, String username) throws SQLException {
        String query = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void handleDatabaseError(SQLException e) {
        showError("Database error: " + e.getMessage());
        e.printStackTrace();
    }
}