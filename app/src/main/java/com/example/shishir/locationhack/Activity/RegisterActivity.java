package com.example.shishir.locationhack.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shishir.locationhack.Database.LocalDatabase;
import com.example.shishir.locationhack.ExtraClass.EmailValidator;
import com.example.shishir.locationhack.MainActivity;
import com.example.shishir.locationhack.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Date;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

public class RegisterActivity extends AppCompatActivity {
    EditText emailEt, passEt;
    Button registerBtn;
    String emailStr, passStr;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    LocalDatabase localDatabase;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("Registration");
        auth = FirebaseAuth.getInstance();
        findViewById();
    }

    private void findViewById() {

        emailEt = (EditText) findViewById(R.id.emailEt_at_register);
        passEt = (EditText) findViewById(R.id.pass_et_at_register);

        localDatabase = new LocalDatabase(this);
        registerBtn = (Button) findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allInputIsValid()) {
                    registerUser(emailStr, passStr);
                }
            }
        });


    }


    private boolean allInputIsValid() {
        boolean res = false;
        emailStr = emailEt.getText().toString();
        passStr = passEt.getText().toString();
        if (emailStr.length() == 0) {
            myAlertDialogue("Email cannot be empty !");
        } else if (!EmailValidator.emailIsValid(emailStr)) {
            myAlertDialogue("Enter a valid email !");
        } else if (passStr.length() == 0) {
            myAlertDialogue("Password can not be empty !");
        } else {
            res = true;
        }
        return res;

    }

    private void ToastMessage(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void registerUser(String email, String password) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    progressDialog.dismiss();
                    myAlertDialogue("It may be you do not have data pack. Please check !");
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        myAlertDialogue("Your Password is very weak !");
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        myAlertDialogue("Your email is invalid !");
                    } catch (FirebaseAuthUserCollisionException e) {
                        myAlertDialogue("User with this email already exits !");
                    } catch (Exception e) {
                    }

                } else {
                    localDatabase.setLoggedIn(true);
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    finish();
                    //   saveUserToFireBaseDatabase(task.getResult().getUser());
                }
            }
        });
    }


//    private void saveUserToFireBaseDatabase(FirebaseUser user) {
//        if (user != null) {
//            databaseReference = FirebaseDatabase.getInstance().getReference().child("Donors").child(uiID);
//
//            //For Single Value Input.............
//            // databaseReference.child('name').setValue(name).addOnCompletionListener.............................
//
//            HashMap<String, String> map = new HashMap<String, String>();
//            map.put("name", nameStr);
//            map.put("blood", bloodGroupStr);
//            databaseReference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//
//                    if (task.isSuccessful()) {
//
//                    } else {
//                        progressDialog.dismiss();
//                        ToastMessage("Failed to save in FireBase Database !");
//                    }
//                }
//            });
//        }
//
//    }


    private void myAlertDialogue(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

}
