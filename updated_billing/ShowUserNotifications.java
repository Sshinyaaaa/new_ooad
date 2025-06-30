import javax.swing.*;

public class ShowUserNotifications {
    private final int currentUserId;
    
    public ShowUserNotifications(int userId) {
        this.currentUserId = userId;
    }
    
    public void showNotifications() {
        JFrame frame = new JFrame("My Notifications");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        
        ShowUserNotificationsPanel panel = new ShowUserNotificationsPanel(currentUserId);
        frame.add(panel);
        
        frame.setVisible(true);
    }
}