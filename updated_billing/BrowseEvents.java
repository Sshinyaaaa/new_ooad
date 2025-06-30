import javax.swing.*;
import java.awt.*;

public class BrowseEvents extends JFrame {
    private final int currentUserId; 
    
    public BrowseEvents(int userId) {
        this.currentUserId = userId;
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());
        add(new BrowseEventsPanel(currentUserId), BorderLayout.CENTER);
        
    }

}