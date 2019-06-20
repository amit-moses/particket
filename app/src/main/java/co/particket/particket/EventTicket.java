package co.particket.particket;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;

public class EventTicket extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    Spinner spn;
    Button btnScan ,save;
    Button dialogOk, dialogReset;
    TextView dialogTv1, dialogTv2, dialogTv3, dialogTv4;
    TextView tvName, powerd, left, price;
    Event ev;
    public static EditText etBarcode;
    EditText cusName;
    Customer cus;
    ArrayList<String> lst;
    Seller sel;
    Dialog dialog;
    ImageView dProImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_ticket);
        getSupportActionBar().setTitle("Sale ticket");

        Intent in = getIntent();
        ev = Data.getMySa().get(in.getIntExtra("pos",0));
        btnScan = (Button)findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(this);
        spn = (Spinner)findViewById(R.id.sp);
        tvName=findViewById(R.id.event_name);
        powerd = findViewById(R.id.powerd);
        left = findViewById(R.id.tickets);
        price=findViewById(R.id.price);
        save=findViewById(R.id.btn_save);
        save.setOnClickListener(this);
        cusName=findViewById(R.id.et_name);
        tvName.setText(ev.getEventName());
        String str ="";
        if(ev.getDate() != null) {
            String[] kk = ev.getDate().split("/");
            str += kk[2]+"/"+kk[1]+"/"+kk[0] +"\n";
        }
        powerd.setText(str+="powerd by "+ev.getManegerName());
        if(ev.getAvailableTickets() > 0)
            left.setText(String.valueOf(ev.getAvailableTickets()) + " tickets left");
        else{
            left.setVisibility(View.GONE);
            if(ev.getAvailableTickets() != -1) save.setEnabled(false);
        }
        price.setText(getDoubleString(ev.getPrice()));
        etBarcode = (EditText)findViewById(R.id.et_barcode);
        int x = ev.getMaxTicket();
        if(x>ev.getAvailableTickets() && ev.getAvailableTickets() != -1) x = ev.getAvailableTickets();
        lst = new ArrayList<>();
        for (int i = 1; i<= x; i++)
            lst.add(String.valueOf(i) + " Tickets");
        getSeller();
        if(!ev.isSaleActive() || Data.isEventToday(ev) > 0 || (ev.getAvailableTickets()<=0 && ev.getAvailableTickets() !=-1)){
            save.setEnabled(false);
            btnScan.setEnabled(false);
            save.setBackgroundResource(R.drawable.capsule0dis);
            btnScan.setBackgroundResource(R.drawable.capsule0dis);
            etBarcode.setEnabled(false);
            cusName.setEnabled(false);
            spn.setEnabled(false);
            lst.clear();
            lst.add("No tickets available");
        }
        ArrayAdapter<String> lstAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,lst );
        spn.setAdapter(lstAdapter);
        spn.setOnItemSelectedListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if(!ev.getManageUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            getMenuInflater().inflate(R.menu.button_bar2,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.deal){
            dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_seller_payment);

            dialogOk = dialog.findViewById(R.id.btn_ok);
            dialogOk.setOnClickListener(this);
            dialogReset = dialog.findViewById(R.id.btn_reset);
            dialogReset.setOnClickListener(this);
            dProImg = dialog.findViewById(R.id.profile_img);
            loadProfileImg(ev.getManageUid());

            TextView aa1 = dialog.findViewById(R.id.aa1);
            String str1 = aa1.getText().toString();
            TextView aa2 = dialog.findViewById(R.id.aa2);
            aa1.setText(aa2.getText().toString());
            aa2.setText(str1);

            dialogTv1 = dialog.findViewById(R.id.ticket_sold);
            dialogTv4 = dialog.findViewById(R.id.seller_earn);
            dialogTv3 = dialog.findViewById(R.id.event_name);
            dialogTv2 = dialog.findViewById(R.id.keep);

            dialogTv1.setText(String.valueOf(sel.getNumberTicket()));
            dialogTv2.setText(getDoubleString(sel.getTotalEarn()));
            dialogTv3.setText(ev.getEventName() +"\n" + ev.getManegerName());
            dialogTv4.setText(getDoubleString(sel.getToManager()));
            dialog.show();
        }
        return true;
    }
    public String getDoubleString(double da){
        if(da%1 ==0) return String.valueOf((int)da);
        else return String.format("%.2f", da);
    }

    /**
     * this function get the seller item from database
     */
    public void getSeller() {
        if(ev.getManageUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            sel= new Seller(ev.getEventID(),ev.getManageUid(),false,0,ev.getManegerName(),"");
        else{
            FirebaseFirestore.getInstance().collection("Sellers")
                    .whereEqualTo("sellerUid", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .whereEqualTo("eventID", ev.getEventID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for (QueryDocumentSnapshot document : task.getResult())
                        if(document.exists()) sel = document.toObject(Seller.class);
                }
            });
        }
    }
    @Override
    public void onClick(View view) {
        if(view == btnScan) startActivity(new Intent(this, Scanner2.class));
        if(view==save){
            String str="";
            if(cusName.getText().toString().length()==0) str+="Customer name is required +\n";
            if(etBarcode.getText().toString().length()==0) str+="Barcode is required";
            if(str.equals("")) save();
            else Toast.makeText(this, str, Toast.LENGTH_LONG).show();
        }
        if(view==dialogOk) dialog.dismiss();
        if(view==dialogReset){
            Data.getUd().setRevenueB(Data.getUd().getRevenueB() + sel.getTotalEarn());
            sel.setTotalEarn(0);
            dialogTv2.setText(String.valueOf(0));
            FirebaseFirestore.getInstance().collection("UserDetails").document(Data.getUd().getUserDetailsID()).set(Data.getUd());
            FirebaseFirestore.getInstance().collection("Sellers").document(sel.getSellerID()).set(sel);
        }
    }

    /**
     * this function load the profile image of the manger
     * @param uid which is the id of the seller user
     */
    public void loadProfileImg(String uid){
        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference ref = mStorageRef.child(uid);
        try {
            final File localFile = File.createTempFile("Images", "bmp");
            ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener< FileDownloadTask.TaskSnapshot >() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    EventTicket.this.dProImg.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this function save the new ticket to database
     */
    public void save(){
        FirebaseFirestore.getInstance().collection("Customers")
                .whereEqualTo("eventId",ev.getEventID())
                .whereEqualTo("barcode",etBarcode.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult().isEmpty()){
                    String date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
                    (EventTicket.this).cus = new Customer(ev.getEventID(),cusName.getText().toString(),etBarcode.getText().toString(),
                            spn.getSelectedItemPosition() + 1, FirebaseAuth.getInstance().getCurrentUser().getUid(),date,
                            Double.valueOf(price.getText().toString()),sel.getEarnPercent(),ev.isChargeable());
                    FirebaseFirestore.getInstance().collection("Customers").add(cus).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            cus.setCustomerId(documentReference.getId());
                            documentReference.set(cus);
                            Data.getMyCu().add(cus);
                        }
                    });
                    //ev =FirebaseFirestore.getInstance().collection("Events").document(ev.getEventID()).get().getResult().toObject(Event.class);
                    if(ev.getAvailableTickets() != -1) ev.setAvailableTickets(ev.getAvailableTickets()-cus.getQuantity());
                    ev.setTicketSold(ev.getTicketSold()+cus.getQuantity());
                    FirebaseFirestore.getInstance().collection("Events").document(ev.getEventID()).set(ev);
                    if(sel.isActive()){
                        sel.setNumberTicket(sel.getNumberTicket()+cus.getQuantity());
                        sel.addMoney(cus.getTotalPrice());
                        FirebaseFirestore.getInstance().collection("Sellers").document(sel.getSellerID()).set(sel);
                    }

                    finish();
                    onBackPressed();
                }
                else Toast.makeText(EventTicket.this, "This barcode is used", Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        i++;
        price.setText(getDoubleString(i*ev.getPrice()));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

