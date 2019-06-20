package co.particket.particket;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class Login extends AppCompatActivity implements View.OnClickListener {
    EditText etEmail, etPassword;
    Button btnLogin, btnReg;
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");
        sp = getSharedPreferences("save",0);
        etEmail = (EditText)findViewById(R.id.et_username);
        etEmail.setText(sp.getString("email",""));
        etPassword = (EditText)findViewById(R.id.et_password);
        btnLogin = (Button)findViewById(R.id.btn_login);
        btnReg = (Button) findViewById(R.id.reg);
        btnLogin.setOnClickListener(this);
        btnReg.setOnClickListener(this);
        if(etEmail.getText().toString().length() == 0) etEmail.requestFocus();
        else etPassword.requestFocus();
    }

    @Override
    public void onClick(View view) {
        if(view==btnLogin) {
            btnLogin.setEnabled(false);
            login();
        }
        if(view == btnReg) startActivity(new Intent(this,Register.class));
    }

    /**
     * this function log in to the app
     */
    public void login()
    {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();
        FirebaseAuth fa = FirebaseAuth.getInstance();
        fa.signInWithEmailAndPassword(etEmail.getText().toString(),etPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Login.this.btnLogin.setEnabled(true);
                    SharedPreferences.Editor editor = Login.this.sp.edit();
                    editor.putString("email", etEmail.getText().toString());
                    editor.commit();
                    Data.loadData(Login.this);
                }
                else Toast.makeText(Login.this, "Email or Password is wrong", Toast.LENGTH_LONG).show();
                btnLogin.setEnabled(true);
                pd.dismiss();
            }
        });
    }
}
