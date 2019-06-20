package co.particket.particket;

public class Event {
    private String eventID;
    private String eventName;
    private String manageUid;
    private String date;
    private int availableTickets;
    private int ticketSold;
    private double price;
    private int maxTicket;
    private boolean saleActive;
    private boolean ticketing;
    private String manegerName;
    private int numberEnters;
    private boolean chargeable;
    public Event(){}
    public Event(String eventName, String manageUid, String date, int availableTickets, int maxTicket, Double price, boolean salActive, boolean ticketing, String manegerName, boolean isChargeable){
        this.eventName = eventName;
        this.manageUid = manageUid;
        this.date = date;
        this.availableTickets = availableTickets;
        this.ticketSold = 0;
        this.maxTicket = maxTicket;
        this.price = price;
        this.saleActive = salActive;
        this.ticketing = ticketing;
        this.manegerName=manegerName;
        this.numberEnters=0;
        this.chargeable = isChargeable;
    }

    public String getEventName(){return eventName;}
    public void setEventName(String a){this.eventName = a;}
    public String getManageUid(){return manageUid;}
    public void setManageUid(String a){this.manageUid = a;}
    public String getDate(){return date;}
    public void setDate(String a){this.date = a;}
    public int getAvailableTickets(){return availableTickets;}
    public void setAvailableTickets(int a){this.availableTickets = a;}
    public int getTicketSold(){return ticketSold;}
    public void setTicketSold(int a){this.ticketSold = a;}
    public double getPrice(){return price;}
    public void setPrice(double a){this.price = a;}
    public String getEventID(){return eventID;}
    public void setEventID(String a){this.eventID = a;}
    public int getMaxTicket(){return maxTicket;}
    public void setMaxTicket(int a){this.maxTicket = a;}
    public boolean isSaleActive() { return saleActive; }
    public void setSaleActive(boolean saleActive) { this.saleActive = saleActive; }
    public boolean isTicketing() { return ticketing; }
    public void setTicketing(boolean ticketing) { this.ticketing = ticketing; }
    public String getManegerName() { return manegerName; }
    public void setManegerName(String manegerName) { this.manegerName = manegerName; }
    public int getNumberEnters() { return numberEnters; }
    public void addEnter(int x) { this.numberEnters+=x; }
    public boolean isChargeable() { return chargeable; }
    public void setChargeable(boolean chargeable) { this.chargeable = chargeable; }

    @Override
    public String toString() {return eventName;}


}
