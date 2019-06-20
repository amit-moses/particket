package co.particket.particket;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyCustomers extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener, View.OnClickListener, AdapterView.OnItemSelectedListener {
    DrawerLayout dwl;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    ArrayAdapter<Customer> customerArrayAdapter;
    ListView lst;
    EditText etFilter;
    Dialog dialog;
    Button dialogOkAdd, dialogCancel, btnScan, btn1, btn2, btn3;
    Spinner dialogSpiner;
    Event ev;
    ArrayList<String> list;
    TextView dialogPrice;
    Customer cus;
    ArrayList<Customer> sort;
    ListView lstv;
    ArrayAdapter<Event> eventAdapter;
    boolean selectCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_customers);
        getSupportActionBar().setTitle("My customers");

        DrawerLayout d = findViewById(R.id.drawerLayout);
        NavigationView n = findViewById(R.id.nav);

        etFilter = findViewById(R.id.et_search);
        dwl = (DrawerLayout)findViewById(R.id.drawerLayout);
        navigationView = (NavigationView)findViewById(R.id.nav);
        toggle = new ActionBarDrawerToggle(this, dwl ,R.string.open, R.string.close);
        dwl.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(this);
        lst = findViewById(R.id.lst);
        btnScan=findViewById(R.id.btn_scan);
        btn1 = findViewById(R.id.a1);
        btn2 = findViewById(R.id.a2);
        btn3 = findViewById(R.id.a3);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        sort = new ArrayList<>(Data.getMyCu());
        setCustomersAdapter();
        selectCustomer = true;
        lst.setOnItemClickListener(this);
        etFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                (MyCustomers.this).customerArrayAdapter.getFilter().filter(charSequence);
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * this function update adapter after sorting
     */
    public void setCustomersAdapter() {
        etFilter.setText("");
        customerArrayAdapter = new ArrayAdapter<Customer>(this, R.layout.list_status, R.id.text1, sort) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Customer customer = (Customer) lst.getAdapter().getItem(position);
                TextView text = view.findViewById(R.id.text1);
                ImageView icon = view.findViewById(R.id.icon);
                text.setText(customer.toString());

                if(customer.isChargeable()){
                    int iconResId = getResources().getIdentifier("charge", "drawable", getPackageName());
                    icon.setImageResource(iconResId);
                }

                return view;
            }
        };
        lst.setAdapter(customerArrayAdapter);
    }

    @Override
    protected void onStart() {
        customerArrayAdapter.notifyDataSetChanged();
        super.onStart();
    }
    @Override
    protected void onResume() {
        customerArrayAdapter.notifyDataSetChanged();
        Intent in = getIntent();
        if(in.getStringExtra("result") != null) searchByBarcode(in.getStringExtra("result"));
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){
            TextView tv1 = findViewById(R.id.emailHeader);
            tv1.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            TextView tv2 = findViewById(R.id.nameHeader);
            tv2.setText(Data.getUd().getFirstName() + " " + Data.getUd().getLastName());
            Bitmap icon = Data.getProfileImg();
            ImageView proImg = findViewById(R.id.profile_img);
            if(icon!=null)
                proImg.setImageBitmap(icon);
            else{
                int iconResId = getResources().getIdentifier("profile", "drawable", getPackageName());
                proImg.setImageResource(iconResId);
            }
            return true;
        }
        super.onOptionsItemSelected(item);
        return  true;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if(id == R.id.menu_a) startActivity(new Intent(this, MyEvents.class)); //my events
        if(id == R.id.menu_b) startActivity(new Intent(this, MySalesEvents.class));//sale ticket
        if(id == R.id.menu_c) startActivity(new Intent(this, Ticketing.class));//ticketing events
        if(id == R.id.menu_d) dwl.closeDrawers(); //my customers
        if(id == R.id.menu_e) startActivity(new Intent(this, Setting.class));//setting
        if(id == R.id.menu_f){
            FirebaseAuth fa = FirebaseAuth.getInstance();
            fa.signOut();
            Data.rest();
            startActivity(new Intent(this, MainActivity.class));
        }//log out
        return  true;
    }

    /**
     * this function open dialog to charge the ticket
     */
    public void dialogShow(){
        if(cus.isChargeable()){
            dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_charge);
            dialogOkAdd = dialog.findViewById(R.id.btn_ok);
            dialogCancel = dialog.findViewById(R.id.btn_cancel);
            dialogSpiner = dialog.findViewById(R.id.sp);
            dialogPrice = dialog.findViewById(R.id.price);
            TextView tv =dialog.findViewById(R.id.text);
            int ff =0;
            while (ff<Data.getMySa().size() && !Data.getMySa().get(ff).getEventID().equals(cus.getEventId())) ff++;
            if(ff<Data.getMySa().size()){
                ev = Data.getMySa().get(ff);
                tv.setText(cus.toString()+'\n'+ ev.getEventName());
                int x = ev.getMaxTicket();
                if(x>ev.getAvailableTickets() && ev.getAvailableTickets() != -1) x = ev.getAvailableTickets();
                list = new ArrayList<>();
                for (int i = 1; i<= x; i++)
                    list.add(String.valueOf(i) + " Tickets");
                if(!ev.isSaleActive() || (ev.getAvailableTickets()<=0 && ev.getAvailableTickets() !=-1) || Data.isEventToday(ev)>0) {
                    list.clear();
                    dialogPrice.setVisibility(View.INVISIBLE);
                    list.add("No tickets available");
                    dialogSpiner.setEnabled(false);
                    dialogSpiner.setBackgroundResource(getResources().getIdentifier("capsule0dis", "drawable", getPackageName()));
                    dialogOkAdd.setEnabled(false);
                    dialogOkAdd.setBackgroundResource(getResources().getIdentifier("capsule0dis", "drawable", getPackageName()));
                }
                ArrayAdapter<String> lstAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,list );
                dialogSpiner.setAdapter(lstAdapter);
                dialogSpiner.setOnItemSelectedListener(this);
                dialogOkAdd.setOnClickListener(this);
                dialogCancel.setOnClickListener(this);
                dialog.show();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(selectCustomer){
            cus=customerArrayAdapter.getItem(i);
            dialogShow();
        }
        else{
            dialog.dismiss();
            selectCustomer = true;
            sort2(eventAdapter.getItem(i).getEventID());
        }
    }

    /**
     * this function update the customer after the charge to the database
     */
    public void update(){
        final double price1 = (dialogSpiner.getSelectedItemPosition()+1)*ev.getPrice();
        cus.setTotalPrice(cus.getTotalPrice()+price1);
        cus.setQuantity(cus.getQuantity()+dialogSpiner.getSelectedItemPosition()+1);
        FirebaseFirestore.getInstance().collection("Customers").document(cus.getCustomerId()).set(cus);
        customerArrayAdapter.notifyDataSetChanged();
        if(ev.getAvailableTickets() != -1) ev.setAvailableTickets(ev.getAvailableTickets()-dialogSpiner.getSelectedItemPosition()-1);
        ev.setTicketSold(ev.getTicketSold()+dialogSpiner.getSelectedItemPosition()+1);
        FirebaseFirestore.getInstance().collection("Events").document(ev.getEventID()).set(ev);
        String strUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(!ev.getManageUid().equals(strUid)) {
            FirebaseFirestore.getInstance().collection("Sellers")
                    .whereEqualTo("sellerUid",strUid)
                    .whereEqualTo("eventID",ev.getEventID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        Seller slr = task.getResult().toObjects(Seller.class).get(0);
                        if(slr!=null){
                            Toast.makeText(MyCustomers.this, slr.getName(), Toast.LENGTH_LONG).show();
                            slr.setNumberTicket(slr.getNumberTicket()+dialogSpiner.getSelectedItemPosition()+1);
                            slr.addMoney(price1);
                            FirebaseFirestore.getInstance().collection("Sellers").document(slr.getSellerID()).set(slr);
                        }
                    }
                }
            });
        }

    }
    @Override
    public void onClick(View view) {
        if(view == dialogOkAdd) {
            dialog.dismiss();
            update();
        }
        if(view == dialogCancel) dialog.dismiss();
        if(view == btnScan) startActivity(new Intent(this, Scanner3.class));
        if(view == btn1) sort1();
        if(view == btn2) openSelectEventDialog();
        if(view == btn3) sort3();
    }

    /**
     * this function open dialog event picker to sort the customers by event
     */
    public void openSelectEventDialog(){
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_select_event);
        lstv = dialog.findViewById(R.id.lst);
        eventAdapter = new ArrayAdapter<Event>(this, R.layout.list_status, R.id.text1, Data.getMySa()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Event eve = (Event) lstv.getAdapter().getItem(position);
                TextView text = view.findViewById(R.id.text1);
                ImageView icon = view.findViewById(R.id.icon);
                text.setText(eve.getEventName());
                int pp = 2;
                if(Data.isEventToday(eve) <=0){
                    if(eve.isSaleActive())pp = 1;
                    else pp = 3;
                }
                if(pp!=3){
                    int iconResId = getResources().getIdentifier("status" + pp, "drawable", getPackageName());
                    icon.setImageResource(iconResId);
                }
                return view;
            }
        };
        lstv.setAdapter(eventAdapter);
        lstv.setOnItemClickListener(this);
        EditText etFil = dialog.findViewById(R.id.et_search);
        etFil.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                (MyCustomers.this).eventAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        selectCustomer = false;
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                MyCustomers.this.selectCustomer = true;
            }
        });
        dialog.show();
    }

    /**
     * this function find specific customer after scan barcode
     * @param bar whic is the barcode of the customer
     */
    public void searchByBarcode(String bar){
        int ff =0;
        while (ff<Data.getMyCu().size() && !Data.getMyCu().get(ff).getBarcode().equals(bar)) ff++;
        if(ff<Data.getMyCu().size()){
            cus = Data.getMyCu().get(ff);
            if(cus.isChargeable()) dialogShow();
            else Toast.makeText(this, "This ticket is not chargeable", Toast.LENGTH_LONG).show();
        }
        else Toast.makeText(this, "No customer has been found", Toast.LENGTH_LONG).show();
    }
    public String getDoubleString(double da){
        if(da%1 ==0) return String.valueOf((int)da);
        else return String.format("%.2f", da);
    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        i++;
        dialogPrice.setText("Total price: "+getDoubleString(i*ev.getPrice()));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * this function presents all the customers
     */
    public void sort1(){
        btn1.setBackgroundResource(getResources().getIdentifier("capsule1", "drawable", getPackageName()));
        btn2.setBackgroundResource(getResources().getIdentifier("capsule0", "drawable", getPackageName()));
        btn3.setBackgroundResource(getResources().getIdentifier("capsule0", "drawable", getPackageName()));
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.show();
        sort.clear();
        sort = new ArrayList<>(Data.getMyCu());
        setCustomersAdapter();
        customerArrayAdapter.notifyDataSetChanged();
        pd.dismiss();
    }

    /**
     * this function sort the customers by specific event
     * @param evID which is the id of the chosen event
     */
    public void sort2(String evID){
        btn1.setBackgroundResource(getResources().getIdentifier("capsule0", "drawable", getPackageName()));
        btn2.setBackgroundResource(getResources().getIdentifier("capsule1", "drawable", getPackageName()));
        btn3.setBackgroundResource(getResources().getIdentifier("capsule0", "drawable", getPackageName()));
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();
        sort.clear();
        sort = new ArrayList<>(Data.getSortList1(evID));
        setCustomersAdapter();
        customerArrayAdapter.notifyDataSetChanged();
        pd.dismiss();
    }

    /**
     * this function sort the customers by ability ti charge their ticket
     */
    public void sort3(){
        btn1.setBackgroundResource(getResources().getIdentifier("capsule0", "drawable", getPackageName()));
        btn2.setBackgroundResource(getResources().getIdentifier("capsule0", "drawable", getPackageName()));
        btn3.setBackgroundResource(getResources().getIdentifier("capsule1", "drawable", getPackageName()));
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.show();
        sort.clear();
        sort = new ArrayList<>(Data.getSortList2());
        setCustomersAdapter();
        customerArrayAdapter.notifyDataSetChanged();
        pd.dismiss();

    }
}
