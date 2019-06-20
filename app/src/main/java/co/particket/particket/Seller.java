package co.particket.particket;


public class Seller {
    private String name;
    private String eventID;
    private String sellerUid;
    private String sellerID;
    private  int numberTicket;
    private boolean isActive;
    private double earnPercent;
    private double totalEarn;
    private double toManager;
    public Seller(){}
    public Seller(String eventID, String sellerUid, boolean isActive, double earnPercent, String name, String sellerID){
        this.eventID = eventID;
        this.sellerUid = sellerUid;
        this.numberTicket = 0;
        this.isActive = isActive;
        this.earnPercent = earnPercent;
        this.name=name;
        this.sellerID=sellerID;
        this.totalEarn=0;
        this.toManager=0;
    }

    public String getEventID() { return eventID; }
    public void setEventID(String eventID) { this.eventID = eventID; }
    public String getSellerUid() { return sellerUid; }
    public void setSellerUid(String sellerUid) { this.sellerUid = sellerUid; }
    public int getNumberTicket() { return numberTicket; }
    public void setNumberTicket(int numberTicket) { this.numberTicket = numberTicket; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public double getEarnPercent() { return earnPercent; }
    public void setEarnPercent(double earnPercent) { this.earnPercent = earnPercent; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSellerID() { return sellerID; }
    public void setSellerID(String sellerID) { this.sellerID = sellerID; }
    public double getTotalEarn() { return totalEarn; }
    public void setTotalEarn(double totalEarn) { this.totalEarn = totalEarn; }
    public void addEarn(double profit) { this.totalEarn +=profit; }
    public double getToManager() { return toManager; }
    public void setToManager(double toManager) { this.toManager = toManager; }
    public void addToManager(double profit) {this.toManager+=profit;};
    public  void addMoney(double total){
        double r = (earnPercent / 100) * total;
        this.totalEarn +=r;
        this.toManager += (total -r);
    }
    @Override
    public String toString() {
        return name;
    }
}
