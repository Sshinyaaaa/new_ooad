import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class LoginRegisterScreen extends JFrame {
    private final LoginRegisterPanel loginRegisterPanel;

    public LoginRegisterScreen() {
        initializeFrame();
        JPanel mainPanel = createMainPanel();
        loginRegisterPanel = new LoginRegisterPanel();
        mainPanel.add(loginRegisterPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void initializeFrame() {
        setTitle("Event Management System - Login/Register");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new java.awt.Color(240, 240, 240));
        return panel;
    }
}