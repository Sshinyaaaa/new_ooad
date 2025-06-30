public class BillingFee {
    private final int registrationId;
    private final String eventName;
    private final String eventDate;
    private final double baseFee;
    private final String additionalServices;
    private final double discount;
    private final String status;
    
    public BillingFee(int registrationId, String eventName, String eventDate, 
                     double baseFee, String additionalServices, double discount, String status) {
        this.registrationId = registrationId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.baseFee = baseFee;
        this.additionalServices = additionalServices;
        this.discount = discount;
        this.status = status;
    }
    
    public int getRegistrationId() { return registrationId; }
    public String getEventName() { return eventName; }
    public String getEventDate() { return eventDate; }
    public double getBaseFee() { return baseFee; }
    public String getAdditionalServices() { return additionalServices; }
    public double getDiscount() { return discount; }
    public String getStatus() { return status; }
}