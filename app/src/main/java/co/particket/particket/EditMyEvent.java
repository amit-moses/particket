package co.particket.particket;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

public class EditMyEvent extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    EditText eventName, avTicket, maxTicket, price, dialogEmail, dialogPercents;
    Button btnDate, btnAdd, btnSave, dialogOkAdd,dialogOkEdit, dialogCancel;
    Switch swDialog;
    ArrayAdapter<Seller> sellers;
    ArrayList<Seller> userLst;
    ListView lst;
    Dialog dialog;
    Event ev;
    int pos = 0;
    CheckBox ch3;
    Intent in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_event);
        getSupportActionBar().setTitle("Event edit");
        in = getIntent();
        ev = Data.getMyEv().get(in.getIntExtra("pos",0));

        eventName = findViewById(R.id.et1);
        avTicket = findViewById(R.id.et2);
        maxTicket = findViewById(R.id.et3);
        price = findViewById(R.id.et4);
        lst = findViewById(R.id.lstSellers);
        btnDate = findViewById(R.id.btn_pickdate);
        btnAdd = findViewById(R.id.btn_addseller);
        btnSave = findViewById(R.id.btn_save);
        ch3 = findViewById(R.id.checkbox3);
        ch3.setChecked(ev.isChargeable());
        btnSave.setOnClickListener(this);
        btnAdd.setOnClickListener(this);

        userLst = Data.getSellersForEvent(ev.getEventID());
        //sellers = new ArrayAdapter<Seller>(this, android.R.layout.simple_list_item_1, android.R.id.text1, userLst);
        sellers = new ArrayAdapter<Seller>(this, R.layout.list_status, R.id.text1, userLst) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Seller slr = (Seller) lst.getAdapter().getItem(position);
                TextView text = view.findViewById(R.id.text1);
                ImageView icon = view.findViewById(R.id.icon);
                text.setText(slr.getName());
                int pp = 1;
                if(!slr.isActive()) pp++;
                int iconResId = getResources().getIdentifier("status" + pp, "drawable", getPackageName());
                icon.setImageResource(iconResId);
                return view;
            }
        };
        lst.setAdapter(sellers);
        sellers.notifyDataSetChanged();
        lst.setOnItemClickListener(this);

        if(ev.getAvailableTickets() != -1) avTicket.setText(String.valueOf(ev.getAvailableTickets()));
        else {
            avTicket.setBackgroundResource(R.drawable.capsule0dis);
            avTicket.setEnabled(false);
        }
        if(ev.getDate() == null)
            btnDate.setText("UNLIMITED DATE");
        else{
            String str[] = ev.getDate().split("/");
            btnDate.setText(str[2]+"/"+str[1]+"/"+str[0]);
        }

        eventName.setText(ev.getEventName());
        maxTicket.setText(String.valueOf(ev.getMaxTicket()));
        price.setText(String.valueOf(ev.getPrice()));
        btnDate.setBackgroundResource(R.drawable.capsule3dis);
        btnDate.setEnabled(false);

    }

    /**
     * this function update the details of the seller on the database
     */
    public void update(){
        userLst.get(pos).setActive(swDialog.isChecked());
        userLst.get(pos).setEarnPercent(Double.valueOf(dialogPercents.getText().toString()));
        if(userLst.get(pos).getSellerID() != null)
            FirebaseFirestore.getInstance().collection("Sellers")
                    .document(userLst.get(pos).getSellerID()).set(userLst.get(pos));
        pos = 0;
        dialog.dismiss();
        sellers.notifyDataSetChanged();
    }

    /**
     * this function update the details of the event in the app
     * if new seller was added the id will be null and new seller will save on firebase
     */
    public void save(){
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();
        if(ev.isChargeable() != ch3.isChecked()) setChargeable(ch3.isChecked());
        if(isPropriety()){
            ev.setChargeable(ch3.isChecked());
            ev.setEventName(eventName.getText().toString());
            ev.setPrice(Double.valueOf(price.getText().toString()));
            ev.setMaxTicket(Integer.valueOf(maxTicket.getText().toString()));
            if(ev.getAvailableTickets()!=-1) ev.setAvailableTickets(Integer.valueOf(avTicket.getText().toString()));
            FirebaseFirestore.getInstance().collection("Events").document(ev.getEventID()).set(ev);
            for(int i=0; i<userLst.size(); i++){
                if(userLst.get(i).getSellerID() == null)
                    addSellerToDataBase(userLst.get(i));
            }
            pd.dismiss();
            finish();
            onBackPressed();
        }
        else pd.dismiss();
    }

    /**
     * this function set event as chargeable and it will update all customers of this event
     * @param set which is the ability to charge the ticket
     */
    public void setChargeable(boolean set){
        for(int i=0; i<Data.getMyCu().size(); i++) {
            Customer cus1 = Data.getMyCu().get(i);
            if (cus1.getEventId().equals(ev.getEventID())){
                cus1.setChargeable(set);
                FirebaseFirestore.getInstance().collection("Customers").document(cus1.getCustomerId()).set(cus1);
            }
        }
    }

    /**
     * this function add new seller to the database
     * @param slr which is the new seller
     */
    public void addSellerToDataBase(final Seller slr){
        FirebaseFirestore.getInstance().collection("Sellers").add(slr).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                slr.setSellerID(documentReference.getId());
                documentReference.set(slr);
                Data.getMySe().add(slr);
            }
        });
    }

    /**
     * this function check if the values that have put are correct
     * @return true if the vaues are ok else false
     */
    public boolean isPropriety(){
        String str ="";
        if(eventName.getText().toString().length()==0) str+="Event name is required" + "\n";
        if(avTicket.getText().toString().length()==0 && ev.getAvailableTickets()!=-1) str+="Available tickets is required for limited tickets" + "\n";
        if(maxTicket.getText().toString().length()==0) str+= "Max purchase is required +\n";
        if (price.getText().toString().length()==0) str+="Ticket price is required \n";

        if(str.equals("")) return true;
        else{
            Toast.makeText(this, str, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * this function add the seller to the list of sellers
     * @param email wich is the email of the seller
     */
    public void addSeller(String email){
        CollectionReference clr = FirebaseFirestore.getInstance().collection("UserDetails");
        Query query = clr.whereEqualTo("email",email);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        UserDetails ud =document.toObject(UserDetails.class);
                        if(!isExist(ud)){
                            userLst.add(new Seller(ev.getEventID(),ud.getUserUid(),swDialog.isChecked(),Double.valueOf(dialogPercents.getText().toString()),ud.getFirstName()+" "+ud.getLastName(),null));
                            sellers.notifyDataSetChanged();
                        }
                        else Toast.makeText(EditMyEvent.this, "This user is already seller!", Toast.LENGTH_LONG).show();
                    }
                }
                if(task.getResult().isEmpty()) Toast.makeText(EditMyEvent.this, "This user is not exist", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * this function check if the seller is already exist
     * @param um which is the new user
     * @return true if the seller is exist ok else false
     */
    public boolean isExist(UserDetails um){
        for(int i = 0; i<userLst.size(); i++)
            if (userLst.get(i).getSellerUid().equals(um.getUserUid())) return true;
        return false;
    }

    /**
     * this function open the dialog to add new seller
     */
    public void dialogShow(){
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_add_seller);
        dialogOkAdd = dialog.findViewById(R.id.btn_ok);
        dialogCancel = dialog.findViewById(R.id.btn_cancel);
        dialogPercents = dialog.findViewById(R.id.et_earnings);
        swDialog = dialog.findViewById(R.id.active);
        swDialog.setChecked(true);
        dialogEmail = dialog.findViewById(R.id.et_email);
        dialogOkAdd.setOnClickListener(this);
        dialogCancel.setOnClickListener(this);
        dialog.show();
    }
    @Override
    public void onClick(View view) {

        if (view == btnSave) save();
        if (view == btnAdd) dialogShow();
        if (view == dialogOkEdit) update();
        if (view == dialogCancel) dialog.dismiss();
        if (view == dialogOkAdd) {
            String str = "";
            if (dialogEmail.getText().toString().length() == 0)
                str = "Please insert Email \n";
            else if (dialogEmail.getText().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                str = "This is your Email, you are already seller! + \n";
            if (dialogPercents.getText().toString().length() == 0)
                str += "Please insert sell earnings percents";
            else if (Double.valueOf(dialogPercents.getText().toString()) > 100 || Double.valueOf(dialogPercents.getText().toString()) < 0)
                str += "earnings percents should be 0≤ ? ≤100";
            if (str.equals("")) {
                addSeller(dialogEmail.getText().toString());
                dialog.dismiss();
            } else Toast.makeText(this, str, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        pos = i;
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_add_seller);
        dialogOkEdit = dialog.findViewById(R.id.btn_ok);
        dialogCancel = dialog.findViewById(R.id.btn_cancel);
        dialogOkEdit.setText("SAVE");
        dialogPercents = dialog.findViewById(R.id.et_earnings);
        dialogPercents.setText(String.valueOf(userLst.get(i).getEarnPercent()));

        swDialog = dialog.findViewById(R.id.active);
        swDialog.setChecked(userLst.get(i).isActive());

        TextView head = dialog.findViewById(R.id.head_line);
        TextView tv = dialog.findViewById(R.id.text);
        tv.setVisibility(View.INVISIBLE);
        head.setText("Edit seller");
        dialogEmail = dialog.findViewById(R.id.et_email);
        dialogEmail.setText(userLst.get(i).getName());
        dialogEmail.setBackgroundResource(R.drawable.capsule0dis);
        dialogEmail.setEnabled(false);

        dialogOkEdit.setOnClickListener(this);
        dialogCancel.setOnClickListener(this);
        dialog.show();

    }
}

