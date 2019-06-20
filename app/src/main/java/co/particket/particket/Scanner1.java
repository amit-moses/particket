package co.particket.particket;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Scanner1 extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    ZXingScannerView scannerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Barcode ticket scanner");
        scannerView = new ZXingScannerView(this);
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            setContentView(R.layout.activity_scanner1);
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        }
        else setContentView(scannerView);


    }

    @Override
    public void handleResult(Result result) {
        long[] pattern = {0,200};
        Vibrator mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        mVibrator.vibrate(pattern,-1);
        Data.searchTicket(this,result.getText());
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }
}

