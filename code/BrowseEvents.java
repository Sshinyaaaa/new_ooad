import javax.swing.*;
import java.awt.*;

public class BrowseEvents extends JFrame {
    private final int currentUserId; // Final since it shouldn't change
    
    public BrowseEvents(int userId) {
        this.currentUserId = userId; // Gets REAL ID from login/register
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("MMU Event Management System - Browse Events");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        // Main content
        setLayout(new BorderLayout());
        add(new BrowseEventsPanel(currentUserId), BorderLayout.CENTER);
        
        // Set icon if available (silent fail if not)
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/app_icon.png"));
            if (icon.getImage() != null) {
                setIconImage(icon.getImage());
            }
        } catch (Exception ignored) {}
    }

}