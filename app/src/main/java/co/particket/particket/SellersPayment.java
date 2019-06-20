package co.particket.particket;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SellersPayment extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    EditText etFilter;
    ArrayAdapter<Seller> sellersAdapter;
    ListView lst;
    ArrayList<Seller> listSe;
    Event ev;
    TextView dialogTv1, dialogTv2, dialogTv3, dialogTv4;
    ImageView dialogImg;
    Dialog dialog;
    Button dialogOk, dialogReset;
    Seller slr;
    int pos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellers_payment);
        getSupportActionBar().setTitle("Sellers working details");

        Intent in = getIntent();
        ev = Data.getMyEv().get(in.getIntExtra("pos",0));
        etFilter=findViewById(R.id.et_search);
        listSe = Data.getSellersForEvent(ev.getEventID());
        lst = findViewById(R.id.lst);
        sellersAdapter = new ArrayAdapter<Seller>(this, R.layout.list_status, R.id.text1, listSe) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                slr = (Seller) lst.getAdapter().getItem(position);
                TextView text = view.findViewById(R.id.text1);
                ImageView icon = view.findViewById(R.id.icon);
                int pp = 1;
                if(!slr.isActive()) pp++;
                text.setText(slr.getName());
                int iconResId = getResources().getIdentifier("status" + pp, "drawable", getPackageName());
                icon.setImageResource(iconResId);
                return view;
            }
        };
        lst.setAdapter(sellersAdapter);
        lst.setOnItemClickListener(this);
        etFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                (SellersPayment.this).sellersAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Seller slr = sellersAdapter.getItem(i);
        pos = Data.getMySe().indexOf(slr);
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_seller_payment);

        dialogOk = dialog.findViewById(R.id.btn_ok);
        dialogOk.setOnClickListener(this);
        dialogReset = dialog.findViewById(R.id.btn_reset);


        dialogTv1 = dialog.findViewById(R.id.ticket_sold);
        dialogTv2 = dialog.findViewById(R.id.seller_earn);
        dialogTv3 = dialog.findViewById(R.id.event_name);
        dialogTv4 = dialog.findViewById(R.id.keep);
        dialogImg = dialog.findViewById(R.id.profile_img);
        loadSellerProfileImg(slr.getSellerUid());
        dialogTv1.setText(String.valueOf(slr.getNumberTicket()));
        dialogTv2.setText(getDoubleString(slr.getTotalEarn()));
        dialogTv3.setText(slr.getName() +"\n" + ev.getEventName());
        dialogTv4.setText(getDoubleString(slr.getToManager()));
        dialogReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SellersPayment.this.slr != null){
                    Data.getUd().setRevenueA(Data.getUd().getRevenueA() + Data.getMySe().get(pos).getToManager());
                    Data.getMySe().get(pos).setToManager(0);
                    dialogTv4.setText("0");
                    FirebaseFirestore.getInstance().collection("UserDetails").document(Data.getUd().getUserDetailsID())
                            .set(Data.getUd());
                    FirebaseFirestore.getInstance().collection("Sellers").document(Data.getMySe().get(pos).getSellerID())
                            .set(Data.getMySe().get(pos));
                }
            }
        });
        dialog.show();
    }
    public String getDoubleString(double da){
        if(da%1 ==0) return String.valueOf((int)da);
        else return String.format("%.2f", da);
    }

    /**
     * this function load the profile image of the seller
     * @param uid which is the id of the seller user
     */
    public void loadSellerProfileImg(String uid){
        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference ref = mStorageRef.child(uid);
        try {
            final File localFile = File.createTempFile("Images", "bmp");
            ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener< FileDownloadTask.TaskSnapshot >() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    SellersPayment.this.dialogImg.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if(view == dialogOk) dialog.dismiss();
    }
}
