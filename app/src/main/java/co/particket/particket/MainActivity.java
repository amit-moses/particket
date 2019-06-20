package co.particket.particket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.Result;

import java.util.Calendar;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn1, btn2;
    FirebaseUser fb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fb = FirebaseAuth.getInstance().getCurrentUser();
        if(fb == null){
            btn1 = findViewById(R.id.btn_scan);
            btn1.setVisibility(View.VISIBLE);
            btn2 = findViewById(R.id.btn_ba);
            btn2.setVisibility(View.VISIBLE);
            btn1.setOnClickListener(this);
            btn2.setOnClickListener(this);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(fb != null)  Data.loadData(MainActivity.this);
    }

    @Override
    public void onClick(View view) {
        if(view == btn1) {
            SharedPreferences sp = getSharedPreferences("customer",0);
            String str = sp.getString("qr",null);
            if(str!= null) Data.searchTicket(this,str);
            else{
                Intent in = new Intent(this, Scanner1.class);
                startActivity(in);
            }
        }
        if(view == btn2)
            startActivity(new Intent(this,Login.class));
    }
}

