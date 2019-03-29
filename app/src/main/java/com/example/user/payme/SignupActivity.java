package com.example.user.payme;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private static final String PASSWORD_PATTERN =
            "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,16})";
    private Pattern pattern;
    private Matcher matcher;

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

        pattern = Pattern.compile(PASSWORD_PATTERN);

        createAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = nameTxt.getText().toString().trim();
                final String phone = paylahTxt.getText().toString().trim();
                final String email = emailTxt.getText().toString().trim();
                final String pwd = pwdTxt.getText().toString().trim();
                final String confirmPwd = confirmPwdTxt.getText().toString().trim();

                if (validateInputFields(name, phone, email, pwd, confirmPwd)) {
                    createNewUser(name, phone, email, pwd);
                }
            }

        });
    }

    private void createNewUser(String name, String phone, String email, String pwd) {
        auth.createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d("User Creation", "User Account Created.");
                        FirebaseUser user = auth.getCurrentUser();

                        // Save other user details into database
                        User newUser = new User(name, phone, email);
                        System.out.println(newUser);
                        ref.child("users").child(user.getUid()).setValue(newUser);

                        // Send Confirmation Email
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("User Creation", "Email Sent.");
                                    auth.signOut(); // clear the user object
                                    signUpComplete();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(SignupActivity.this, "Account with email already exists.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void signUpComplete() {
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
    }

    private boolean validateInputFields(String name, String phone, String email, String pwd, String confirmPwd) {
        matcher = pattern.matcher(pwd);

        if (name.isEmpty()) {
            nameTxt.requestFocus();
            nameTxt.setError("Name cannot be empty!");
            return false;
        }

        if (phone.isEmpty()) {
            paylahTxt.requestFocus();
            paylahTxt.setError("Number cannot be empty!");
            return false;
        } else if (!phone.matches("^([89]{1})([0-9]{7})")) {
            paylahTxt.requestFocus();
            paylahTxt.setError("Please enter an 8-digit phone number that starts with 8 or 9!");
            return false;
        }

        if (email.isEmpty()) {
            emailTxt.requestFocus();
            emailTxt.setError("Email cannot be empty!");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTxt.requestFocus();
            emailTxt.setError("Please enter a valid email address!");
            return false;
        }

        if (pwd.isEmpty()) {
            pwdTxt.requestFocus();
            pwdTxt.setError("Password cannot be empty!");
            return false;
        } else if (!matcher.matches()) {
            pwdTxt.requestFocus();
            pwdTxt.setError("Password must be 8-16 characters long alphanumeric with special symbols!");
            return false;
        }

        if (confirmPwd.isEmpty()) {
            confirmPwdTxt.requestFocus();
            confirmPwdTxt.setError("Confirm password cannot be empty!");
            return false;
        } else if (!pwd.equals(confirmPwd)) {
            confirmPwdTxt.requestFocus();
            confirmPwdTxt.setError("Passwords do not match!");
            return false;
        }

        return true;

    }
}
