package com.example.user.payme;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.payme.Objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignupActivity extends AppCompatActivity {

    private EditText nameTxt;
    private EditText paylahTxt;
    private EditText emailTxt;
    private EditText pwdTxt;
    private EditText confirmPwdTxt;
    private Button createAccBtn;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize widgets
        nameTxt = findViewById(R.id.nameTxtField);
        paylahTxt = findViewById(R.id.paylahTxtField);
        emailTxt = findViewById(R.id.emailTxtField);
        pwdTxt = findViewById(R.id.pwdTxtField);
        confirmPwdTxt = findViewById(R.id.pwdTxtField2);
        createAccBtn = findViewById(R.id.createAccBtn);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ref = db.getReference();

        createAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewUser();
            }

        });
    }

    private void createNewUser() {
        final String name = nameTxt.getText().toString().trim();
        final String phone = paylahTxt.getText().toString().trim();
        final String email = emailTxt.getText().toString().trim();
        final String pwd = pwdTxt.getText().toString().trim();
        final String confirmPwd = confirmPwdTxt.getText().toString().trim();

        auth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("UserCreation", "User Account Created.");
                            FirebaseUser user = auth.getCurrentUser();

                            // Save other user details into database
                            User newUser = new User(name, phone, email);
                            ref.child("users").child(user.getUid()).setValue(newUser);

//                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                                    .setDisplayName(name)
//                                    .build();
//
//                            user.updateProfile(profileUpdates)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {
//                                                Log.d("User Creation", "User profile updated.");
//                                            }
//                                        }
//                                    });


                            // Send Confirmation Email
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("User Creation", "Email sent.");
                                    }
                                }
                            });

                            // Show Alert Dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                            builder.setMessage("A confirmation email has been sent to your registered email address.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            finish();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {
                            Toast.makeText(SignupActivity.this, "Unable to create account.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
