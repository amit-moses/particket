package co.particket.particket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MyEventInfo extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    Event ev;
    TextView ticketSold, numberSellers, arriveCus, yourEarn, sellersEarn, salePer, evName;
    Switch active1, active2;
    int pos =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_event_info);

        Intent in = getIntent();
        pos = in.getIntExtra("pos",0);
        ev=Data.getMyEv().get(pos);
        evName = findViewById(R.id.event_name);
        getSupportActionBar().setTitle(ev.getEventName());
        if(ev.getDate() != null){
            String aa[] = ev.getDate().split("/");
            evName.setText(aa[2]+"/"+aa[1]+"/"+aa[0]);
        }
        else evName.setVisibility(View.GONE);

        ticketSold = findViewById(R.id.ticket_sold);
        arriveCus = findViewById(R.id.arrive_customers);
        numberSellers = findViewById(R.id.number_sellers);

        yourEarn = findViewById(R.id.your_earn);
        sellersEarn = findViewById(R.id.sellers_earn);
        salePer = findViewById(R.id.sold_percent);



        active1 = findViewById(R.id.sale_active);
        active1.setChecked(ev.isSaleActive());
        active2 = findViewById(R.id.ticketing_active);
        active2.setChecked(ev.isTicketing());

        active1.setOnCheckedChangeListener(this);
        active2.setOnCheckedChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.button_bar1,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.edit){
            Intent intent = new Intent(this,EditMyEvent.class);
            intent.putExtra("pos",pos);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.deal){
            Intent intent = new Intent(this,SellersPayment.class);
            intent.putExtra("pos",pos);
            startActivity(intent);
        }
        return true;
    }

    /**
     * this function load the details of the chosen event
     * @param evId which is the id of event
     * @param sold the number of ticket were sold
     */
    public void loadA(String evId, int sold){
        int numberSellers1 =0;
        double arr2[] = new double[]{0,0,0};
        ArrayList<Customer> lst1 = Data.getMyCu();
        for(int i =0; i<lst1.size(); i++) {
            if(lst1.get(i).getEventId().equals(evId)){
                Customer cus = lst1.get(i);
                arr2[0]+= (((100-cus.getPercentToSeller())/100)*cus.getTotalPrice());
                arr2[1]+= (((cus.getPercentToSeller())/100)*cus.getTotalPrice());

            }
        }
        ArrayList<Seller> lst2 = Data.getMySe();
        for(int i =0; i<lst2.size(); i++) {
            if(lst2.get(i).getEventID().equals(evId))
                numberSellers1++;
        }
        ev.getTicketSold();
        arr2[2] = (100*ev.getTicketSold());
        arr2[2] /= (ev.getAvailableTickets() + ev.getTicketSold());

        ticketSold.setText(String.valueOf(ev.getTicketSold()));
        arriveCus.setText(String.valueOf(ev.getNumberEnters()));
        numberSellers.setText(String.valueOf(numberSellers1));

        yourEarn.setText(getDoubleString(arr2[0]));
        sellersEarn.setText(getDoubleString(arr2[1]));
        if(ev.getAvailableTickets() != -1) salePer.setText(getDoubleString(arr2[2]) +" %");
        else salePer.setText("unlimited");
    }
    public String getDoubleString(double da){
        if(da%1 ==0) return String.valueOf((int)da);
        else return String.format("%.2f", da);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadA(ev.getEventID(),ev.getTicketSold());
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton == active1) {
            ev.setSaleActive(b);
            FirebaseFirestore.getInstance().collection("Events").document(ev.getEventID()).set(ev);
        }
        if(compoundButton == active2) {
            ev.setTicketing(b);
            FirebaseFirestore.getInstance().collection("Events").document(ev.getEventID()).set(ev);
        }
    }
}

