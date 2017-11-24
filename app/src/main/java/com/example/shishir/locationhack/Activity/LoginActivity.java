package com.example.shishir.locationhack.Activity;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shishir.locationhack.Database.LocalDatabase;
import com.example.shishir.locationhack.ExtraClass.EmailValidator;
import com.example.shishir.locationhack.ExtraClass.Network;
import com.example.shishir.locationhack.MainActivity;
import com.example.shishir.locationhack.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button registerHerBtn, loginWithEmailBtn, forgotPassBtn;
    EditText emailEt, passEt;
    String emailStr, passStr;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    LocalDatabase localDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        findViewById();

        if (localDatabase.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }

        registerHerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });
    }

    private void findViewById() {
        registerHerBtn = (Button) findViewById(R.id.registerHereBtn);
        emailEt = (EditText) findViewById(R.id.emailEtAtLogin);
        passEt = (EditText) findViewById(R.id.passwordETFAtLogin);
        forgotPassBtn = (Button) findViewById(R.id.forgotPasswordBtnAtFG);
        loginWithEmailBtn = (Button) findViewById(R.id.loginWithEmailBtn);
        loginWithEmailBtn.setOnClickListener(this);
        localDatabase = new LocalDatabase(this);
        forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "I will do it later", Toast.LENGTH_SHORT).show();
                //  new PasswordRecoveryFragment().show(getSupportFragmentManager(), "SS");
            }
        });


    }

    @Override
    public void onClick(View v) {
        emailStr = emailEt.getText().toString();
        passStr = passEt.getText().toString();
        if (emailStr.length() == 0) {
            myAlertDialogue("Email cannot be empty !");
        } else if (!EmailValidator.emailIsValid(emailStr)) {
            myAlertDialogue("Enter a valid email !");
        } else if (passStr.length() == 0) {
            myAlertDialogue("Password cannot be empty !");
        } else {
            if (Network.isNetAvailable(this)) {
                signIn();
            } else {
                Network.showInternetAlertDialog(this);
            }
        }
    }

    private void signIn() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        auth.signInWithEmailAndPassword(emailStr, passStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    localDatabase.setLoggedIn(true);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    progressDialog.dismiss();
                    myAlertDialogue("Either you are not registered or your email and password are incorrect");
                }
            }
        });
    }


    public void myAlertDialogue(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(msg)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }
}
