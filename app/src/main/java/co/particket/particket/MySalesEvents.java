package co.particket.particket;

import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MySalesEvents extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener, View.OnClickListener {
    DrawerLayout dwl;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    ArrayAdapter<Event> eventAdapter;
    ListView lst;
    EditText etFilter;
    Button btn1, btn2, btn3;
    ArrayList<Event> sort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_sales_events);
        getSupportActionBar().setTitle("Shared events");
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

        btn1 = findViewById(R.id.a1);
        btn1.setOnClickListener(this);
        btn2 = findViewById(R.id.a2);
        btn2.setOnClickListener(this);
        btn3 = findViewById(R.id.a3);
        btn3.setOnClickListener(this);
        sort = new ArrayList<>(Data.getMySa());
        setEventAdapter();
        lst.setOnItemClickListener(this);

        etFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                (MySalesEvents.this).eventAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    @Override
    protected void onStart() {
        eventAdapter.notifyDataSetChanged();
        super.onStart();
    }
    @Override
    protected void onResume() {
        eventAdapter.notifyDataSetChanged();
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
        if(id == R.id.menu_b) dwl.closeDrawers();//sale ticket
        if(id == R.id.menu_c) startActivity(new Intent(this, Ticketing.class));//ticketing events
        if(id == R.id.menu_d) startActivity(new Intent(this, MyCustomers.class)); //my customers
        if(id == R.id.menu_e) startActivity(new Intent(this, Setting.class));//setting
        if(id == R.id.menu_f){
            FirebaseAuth fa = FirebaseAuth.getInstance();
            fa.signOut();
            Data.rest();
            startActivity(new Intent(this, MainActivity.class));
        }//log out
        return  true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent in = new Intent(this,EventTicket.class);
        in.putExtra("pos",Data.getMySa().indexOf(eventAdapter.getItem(i)));
        startActivity(in);
    }

    /**
     * this function set the adapter after sorting
     */
    public void setEventAdapter(){
        etFilter.setText("");
        eventAdapter = new ArrayAdapter<Event>(this, R.layout.list_status, R.id.text1, sort) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Event eve = (Event) lst.getAdapter().getItem(position);
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
        lst.setAdapter(eventAdapter);
    }

    @Override
    public void onClick(View view) {

        if(view==btn1) sort1();
        if(view==btn2) sort2();
        if(view==btn3) sort3();
    }

    /**
     * this function presents all the events
     */
    public void sort1(){
        btn1.setBackgroundResource(getResources().getIdentifier("capsule1", "drawable", getPackageName()));
        btn2.setBackgroundResource(getResources().getIdentifier("capsule0", "drawable", getPackageName()));
        btn3.setBackgroundResource(getResources().getIdentifier("capsule0", "drawable", getPackageName()));
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.show();
        sort.clear();
        sort = new ArrayList<>(Data.getMySa());
        setEventAdapter();
        eventAdapter.notifyDataSetChanged();
        pd.dismiss();
    }

    /**
     * this function presents the events in the future or today
     */
    public void sort2(){
        btn1.setBackgroundResource(getResources().getIdentifier("capsule0", "drawable", getPackageName()));
        btn2.setBackgroundResource(getResources().getIdentifier("capsule1", "drawable", getPackageName()));
        btn3.setBackgroundResource(getResources().getIdentifier("capsule0", "drawable", getPackageName()));
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();
        sort.clear();
        sort = new ArrayList<>(Data.getSortList(true));
        setEventAdapter();
        eventAdapter.notifyDataSetChanged();
        pd.dismiss();
    }

    /**
     * this function presents the events in the past
     */
    public void sort3(){
        btn1.setBackgroundResource(getResources().getIdentifier("capsule0", "drawable", getPackageName()));
        btn2.setBackgroundResource(getResources().getIdentifier("capsule0", "drawable", getPackageName()));
        btn3.setBackgroundResource(getResources().getIdentifier("capsule1", "drawable", getPackageName()));
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.show();
        sort.clear();
        sort = new ArrayList<>(Data.getSortList(false));
        setEventAdapter();
        eventAdapter.notifyDataSetChanged();
        pd.dismiss();
    }
}

