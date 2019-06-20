package co.particket.particket;

import android.Manifest;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.Result;

import java.util.Stack;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerTicketing extends AppCompatActivity implements ZXingScannerView.ResultHandler, View.OnClickListener {
    ZXingScannerView scannerView;
    LinearLayout lnr;
    Event ev;
    TextView tv1, tv2, tv3;
    Stack<Customer> stackCustomers;
    Dialog dialog;
    Button ok, cancel;
    boolean br;
    Customer cus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_ticketing);
        getSupportActionBar().hide();
        Intent in = getIntent();
        ev = Data.getMyTi().get(in.getIntExtra("pos",0));
        lnr = findViewById(R.id.scan);
        tv1 = findViewById(R.id.event_name);
        tv1.setText(ev.getEventName());
        tv2 = findViewById(R.id.event_date);
        tv3 = findViewById(R.id.last);
        if(ev.getDate() != null){
            String str[] = ev.getDate().split("/");
            tv2.setText(str[2]+"/"+str[1]+"/"+str[0]);
        }
        else tv2.setVisibility(View.GONE);
        scannerView = new ZXingScannerView(this);
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            lnr.setVisibility(View.GONE);
            LinearLayout lll = findViewById(R.id.camera_off);
            lll.setVisibility(View.VISIBLE);
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        }
        else if(ev.isTicketing()){
            lnr.removeAllViews();
            lnr.addView(scannerView, RadioGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        }
        stackCustomers = new Stack<Customer>();
        cancel = findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(this);

    }

    @Override
    public void handleResult(Result result) {
        long[] pattern = {0,200};
        Vibrator mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        mVibrator.vibrate(pattern,-1);
        scannerView.stopCamera();
        ticketing(result.getText().toString());

    }

    /**
     * this function update the customer and reduce the number of enters
     * @param result which is the barcode of the customer. after the scan
     */
    public void ticketing(String result){
        Query query = FirebaseFirestore.getInstance().collection("Customers")
                .whereEqualTo("eventId",ev.getEventID())
                .whereEqualTo("barcode",result);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult().isEmpty())
                    showDialog1(-1, "Enter is not verified", "This customer does'nt exist");
                else if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        if(document.exists()){
                            cus = document.toObject(Customer.class);
                            //Toast.makeText(ScannerTicketing.this, document.getId(), Toast.LENGTH_LONG).show();
                            if(cus.getQuantity()>0) {
                                stackCustomers.push(document.toObject(Customer.class));
                                tv3.setText("Cancel: " + cus.getCustomerName() + " ticketing");
                                cus.setNumberEnters(cus.getNumberEnters() + 1);
                                cus.setQuantity(cus.getQuantity() - 1);
                                FirebaseFirestore.getInstance().collection("Events").document(ev.getEventID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful())
                                            ScannerTicketing.this.ev = task.getResult().toObject(Event.class);
                                    }
                                });
                                ev.addEnter(1);
                                FirebaseFirestore.getInstance().collection("Customers").document(document.getId()).set(cus);
                                FirebaseFirestore.getInstance().collection("Events").document(ev.getEventID()).set(ev);
                                showDialog1(1, "Enter is verified", cus.getCustomerName() + " is verified \n (" + String.valueOf(cus.getNumberEnters()) + "/" + String.valueOf(cus.getQuantity() + cus.getNumberEnters()) + ")");
                            }
                            else showDialog1(-1,"Enter is not verified", cus.getCustomerName()+" is not verified \n This ticket has used ("+String.valueOf(cus.getNumberEnters())+"/"+String.valueOf(cus.getQuantity()+cus.getNumberEnters())+")");
                        }
                    }
                }
            }
        });
    }

    /**
     * this function presents dialog of enter validation
     * @param pos 0 no icon / 1 validated icon / -1 error icon.
     * @param str1 which is the title of the message
     * @param str2 which is the text of the message
     */
    public void showDialog1(Integer pos,String str1, String str2){
        dialog = new Dialog(ScannerTicketing.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_ticketing);
        TextView title = dialog.findViewById(R.id.title);
        title.setText(str1);
        ImageView img = dialog.findViewById(R.id.img);
        br = false;
        if(pos == 0) {
            img.setVisibility(View.GONE);
            br = true;
        }
        if(pos == 1) {
            img.setImageResource(getResources().getIdentifier("check", "drawable", getPackageName()));
            title.setBackgroundColor(Color.parseColor("#5FB404"));
        }
        if(pos == -1) {
            img.setImageResource(getResources().getIdentifier("error", "drawable", getPackageName()));
            title.setBackgroundColor(Color.parseColor("#FE2E2E"));
        }
        TextView text = dialog.findViewById(R.id.text);
        text.setText(str2);
        ok = dialog.findViewById(R.id.btn_ok);
        ok.setOnClickListener(this);
        dialog.show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(ev.isTicketing()) scannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ev.isTicketing()){
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        }
    }

    @Override
    public void onClick(View view) {
        if(view==ok){
            dialog.dismiss();
            if(!stackCustomers.isEmpty() && br){
                FirebaseFirestore.getInstance().collection("Customers").document(stackCustomers.peek().getCustomerId()).set(stackCustomers.pop());
                ev.addEnter(-1);
                FirebaseFirestore.getInstance().collection("Events").document(ev.getEventID()).set(ev);
                br=false;
                if(!stackCustomers.isEmpty()) tv3.setText("Cancel: "+stackCustomers.peek().getCustomerName()+ " ticketing");
                else tv3.setText("Cancel last action");
            }
            else {
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            }
        }
        if(view==cancel) {
            if(!stackCustomers.isEmpty()) showDialog1(0,"Cancel last action", "Do you sure to cancel the ticketing of "+stackCustomers.peek().getCustomerName()+"?");
            else Toast.makeText(this, "No action has done", Toast.LENGTH_LONG).show();
        }
    }
}


