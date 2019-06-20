package co.particket.particket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity implements View.OnClickListener {
    EditText etUsername, etPassword1, etPassword2, firstName, lastName;
    FirebaseAuth firebaseAuth;
    Button btnReg, btnBack, btnNext;
    LinearLayout a1,a2;
    UserDetails ud;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("Register");
        etUsername = (EditText)findViewById(R.id.et_email);
        etPassword1 = (EditText)findViewById(R.id.et_password1);
        etPassword2 = (EditText) findViewById(R.id.et_password2);
        btnNext = (Button)findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
        etUsername.requestFocus();


        btnReg = (Button) findViewById(R.id.btn_reg);
        btnBack = (Button) findViewById(R.id.btn_back);
        firstName = (EditText) findViewById(R.id.et_firstname);
        lastName = (EditText) findViewById(R.id.et_lastname);
        a1 = (LinearLayout)findViewById(R.id.a1);
        a2 = (LinearLayout)findViewById(R.id.a2);
        btnReg.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    /**
     * this function register the user to the app and save the details to firebase
     */

    public void register(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(etUsername.getText().toString(),etPassword1.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    ud = new  UserDetails(user.getUid(),user.getEmail(),firstName.getText().toString(),lastName.getText().toString(),null);
                    FirebaseFirestore.getInstance().collection("UserDetails").add(ud).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            ud.setUserDetailsID(documentReference.getId());
                            documentReference.set(ud);
                        }
                    });
                    Data.loadData(Register.this);
                    pd.dismiss();
                    Register.this.btnReg.setEnabled(true);
                }
                else Toast.makeText(Register.this, "this Email is used or wrong", Toast.LENGTH_LONG).show();
                btnReg.setEnabled(true);
                pd.dismiss();

            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view==btnNext){
            boolean reg = true;
            String str = "";
            if(etUsername.getText().toString().length() == 0)
                str += "wrong Email" + "\n";
            if(!etPassword1.getText().toString().equals(etPassword2.getText().toString()))
                str += "passwords don't match" + "\n";
            if (etPassword1.getText().length() < 6 || etPassword2.getText().length() < 6)
                str += "passwords too short (=>6)";

            if (str.equals("")){
                a1.setVisibility(View.GONE);
                a2.setVisibility(View.VISIBLE);
                firstName.requestFocus();
            }
            else Toast.makeText(this, str, Toast.LENGTH_LONG).show();
        }
        if(view == btnBack){
            a1.setVisibility(View.VISIBLE);
            a2.setVisibility(View.GONE);
            etUsername.requestFocus();

        }
        if(view == btnReg){
            btnReg.setEnabled(false);
            String str="";
            if(firstName.getText().toString().length() == 0)
                str += "Please insert first name" + "\n";
            if(lastName.getText().toString().length() == 0)
                str += "Please insert last name";

            if (str.equals("")) register();
            else {
                btnReg.setEnabled(true);
                Toast.makeText(this, str, Toast.LENGTH_LONG);
            }
        }
    }
}

