import javax.swing.*;

public class EventManagementSystem extends JFrame {
    private final int organizerId; // final because it won't change after constructor

    public EventManagementSystem(int organizerId) {
        this.organizerId = organizerId; // Store the ID that was passed in
        
        setTitle("Event Management System - Organizer ID: " + organizerId);
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Pass the SAME organizerId to the panel
        EventManagementPanel panel = new EventManagementPanel(organizerId); 
        add(panel);
    }
}