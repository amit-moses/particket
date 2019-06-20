package co.particket.particket;

public class Customer {
    private String eventId;
    private String customerName;
    private String barcode;
    private int quantity;
    private String sellerUid;
    private double totalPrice;
    private String date;
    private double percentToSeller;
    private int numberEnters;
    private String customerId;
    private boolean chargeable;
    public Customer(){}
    public Customer(String eventId, String customerName,String barcode, int quantity, String sellerUid, String date, double totalPrice, double percentToSeller, boolean isChargeable){
        this.date=date;
        this.eventId=eventId;
        this.customerName=customerName;
        this.barcode=barcode;
        this.quantity=quantity;
        this.sellerUid=sellerUid;
        this.totalPrice=totalPrice;
        this.percentToSeller=percentToSeller;
        this.numberEnters=0;
        this.chargeable = isChargeable;
    }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getSellerUid() { return sellerUid; }
    public void setSellerUid(String sellerUid) { this.sellerUid = sellerUid; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getPercentToSeller() {return percentToSeller; }
    public void setPercentToSeller(double percentToSeller) { this.percentToSeller = percentToSeller; }

    public int getNumberEnters() { return numberEnters; }
    public void setNumberEnters(int numberEnters) { this.numberEnters = numberEnters; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public boolean isChargeable() { return chargeable; }
    public void setChargeable(boolean chargeable) { this.chargeable = chargeable; }

    @Override
    public String toString() { return customerName +" ("+numberEnters+"/"+(quantity+numberEnters)+")"; }
}
