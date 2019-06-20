package co.particket.particket;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Setting extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    EditText etNa, etLa, etPa1, etPa2;
    Button setNaShow, setPasShow,setBackShow, btnSaveNa, btnSavePa, btnSaveBack;
    ImageView setBackImg;
    LinearLayout linName, linPass, linBack;
    DrawerLayout dwl;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    ProgressDialog pd;
    Uri uriProfile;
    Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        dwl = (DrawerLayout)findViewById(R.id.drawerLayout);
        navigationView = (NavigationView)findViewById(R.id.nav);
        toggle = new ActionBarDrawerToggle(this, dwl ,R.string.open, R.string.close);
        dwl.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(this);

        etNa=findViewById(R.id.et_firstname);
        etLa=findViewById(R.id.et_lastname);
        etPa1=findViewById(R.id.et_pass1);
        etPa2=findViewById(R.id.et_pass2);
        setNaShow=findViewById(R.id.setname);
        setPasShow=findViewById(R.id.setpass);
        setBackShow=findViewById(R.id.setback);
        btnSaveNa=findViewById(R.id.btnsetname);
        btnSavePa=findViewById(R.id.btnsetpass);
        btnSaveBack=findViewById(R.id.btnsetback);
        setBackImg=findViewById(R.id.selectpic);
        linName=findViewById(R.id.a1);
        linPass=findViewById(R.id.a2);
        linBack=findViewById(R.id.a3);
        setNaShow.setOnClickListener(this);
        setPasShow.setOnClickListener(this);
        setBackShow.setOnClickListener(this);
        btnSaveNa.setOnClickListener(this);
        btnSavePa.setOnClickListener(this);
        btnSaveBack.setOnClickListener(this);

        setBackImg.setOnClickListener(this);

        etNa.setText(Data.getUd().getFirstName());
        etLa.setText(Data.getUd().getLastName());

        loadProfileImg();

        //setBackImg.setImageURI(Uri.fromFile(getFileStreamPath("background.png")));

    }

    /**
     * this function update the new name of the user to database
     */
    public void updateName(){
        UserDetails ud = Data.getUd();
        ud.setFirstName(etNa.getText().toString());
        ud.setLastName(etLa.getText().toString());
        FirebaseFirestore.getInstance().collection("UserDetails")
                .document(ud.getUserDetailsID()).set(ud);
        for(int i=0; i<Data.getMyEv().size(); i++){
            Event ev = Data.getMyEv().get(i);
            ev.setManegerName(ud.toString());
            FirebaseFirestore.getInstance().collection("Events")
                    .document(ev.getEventID()).set(ev);
        }
        FirebaseFirestore.getInstance().collection("Sellers")
                .whereEqualTo("sellerUid",ud.getUserUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult())
                        if(document.exists()){
                            Seller slr = document.toObject(Seller.class);
                            slr.setName(Data.getUd().toString());
                            FirebaseFirestore.getInstance().collection("Sellers").document(document.getId()).set(slr);
                        }
                }
            }
        });
        etNa.setText(ud.getFirstName());
        etLa.setText(ud.getLastName());
        shut();
    }

    /**
     * this function update the new password of the user
     */
    public void updatePassword(){
        FirebaseAuth.getInstance().getCurrentUser().updatePassword(etPa1.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                    Toast.makeText(Setting.this, "Password changed successfully", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(Setting.this, "Password changed error", Toast.LENGTH_LONG).show();
            }
        });
        etPa1.setText("");
        etPa2.setText("");
        shut();
    }

    /**
     * this function check if the name is ok
     * @return true if the name is ok else false
     */
    public boolean checkName(){
        if(etNa.getText().toString().length()==0 || etLa.getText().toString().length()==0) {
            Toast.makeText(Setting.this, "please insert name and last name", Toast.LENGTH_LONG).show();
            return false;
        }
        if(etNa.getText().toString().equals(Data.getUd().getFirstName()) && etLa.getText().toString().equals(Data.getUd().getLastName())) {
            Toast.makeText(Setting.this, "The name hasn't changed", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * this function check if the name is ok
     * @return true if the name is ok else false
     */
    public boolean checkPass(){
        String str="";
        String pass1=etPa1.getText().toString();
        String pass2=etPa2.getText().toString();
        if(!pass1.equals(pass2)) str+="Passwords don't match";
        if(pass1.length()<6 || pass2.length()<6) str+="\n Password should be 6+ chars";
        if(str.length()==0) return true;
        else{
            Toast.makeText(Setting.this, str, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * this function close all the layouts
     */
    public void shut(){
        linName.setVisibility(View.GONE);
        linPass.setVisibility(View.GONE);
        linBack.setVisibility(View.GONE);
    }
    @Override
    public void onClick(View view) {
        pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();

        if(view==btnSaveBack) saveProfileImg();
        if(view==btnSaveNa)
            if(checkName()) updateName();
        if(view==btnSavePa)
            if(checkPass()) updatePassword();
        if(view==setNaShow){
            if(linName.getVisibility() == View.VISIBLE)shut();
            else{
                etNa.requestFocus();
                linName.setVisibility(View.VISIBLE);
                linPass.setVisibility(View.GONE);
                linBack.setVisibility(View.GONE);
            }
        }
        if(view==setBackImg) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(Setting.this, "No permission", Toast.LENGTH_LONG).show();
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            else{
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        }
        if(view==setPasShow){
            if(linPass.getVisibility() == View.VISIBLE) shut();
            else{
                etPa1.requestFocus();
                linName.setVisibility(View.GONE);
                linPass.setVisibility(View.VISIBLE);
                linBack.setVisibility(View.GONE);
            }
        }
        if(view==setBackShow){
            if(linBack.getVisibility() == View.VISIBLE) shut();
            else{
                etPa1.requestFocus();
                linName.setVisibility(View.GONE);
                linPass.setVisibility(View.GONE);
                linBack.setVisibility(View.VISIBLE);
            }
        }
        pd.dismiss();
    }

    /**
     * this function save the chosen profile image
     */
    public void saveProfileImg() {
       if(uriProfile!= null) Data.getUd().setProfileImg(uriProfile);
       if(bmp != null) Data.setProfileImg(bmp);
       shut();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                uriProfile = data.getData();
                try {
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfile);
                    setBackImg.setImageBitmap(bmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * this function load the chosen profile img from external host to the ImageView and Bitmap
     */
    public void loadProfileImg(){
        Bitmap icon = Data.getProfileImg();
        if(icon!=null)
            setBackImg.setImageBitmap(icon);
        else{
            int iconResId = getResources().getIdentifier("profile", "drawable", getPackageName());
            setBackImg.setImageResource(iconResId);
        }
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
        if(id == R.id.menu_d) startActivity(new Intent(this, MyCustomers.class)); //my customers
        if(id == R.id.menu_e) dwl.closeDrawers(); //setting
        if(id == R.id.menu_f){
            FirebaseAuth fa = FirebaseAuth.getInstance();
            fa.signOut();
            Data.rest();
            startActivity(new Intent(this, MainActivity.class));
        }//log out
        return  true;
    }
}
