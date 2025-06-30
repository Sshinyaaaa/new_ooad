import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class showNotificationsAdmin {
    private final int currentOrganizerId;
    
    public showNotificationsAdmin(int organizerId) {
        this.currentOrganizerId = organizerId;
    }
    
    public void showNotificationsAdmin() {
        JFrame frame = new JFrame("Admin Notifications");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        
        showNotificationsAdminPanel panel = new showNotificationsAdminPanel(currentOrganizerId);
        frame.add(panel);
        
        frame.setVisible(true);
    }

}