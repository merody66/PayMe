package com.example.user.payme;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.payme.Objects.User;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private EditText emailTxt;
    private EditText pwdTxt;
    private TextView forgotPwd;
    private TextView googleTxt;
    private Button loginBtn;
    private Button signUpBtn;
    private boolean dialogOn;

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;

    private SignInButton signInGoogleBtn;
    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize widgets
        emailTxt = findViewById(R.id.emailTxtField);
        pwdTxt = findViewById(R.id.pwdTxtField);
        forgotPwd = findViewById(R.id.forgotPwdLink);
        loginBtn = findViewById(R.id.loginBtn);
        signUpBtn = findViewById(R.id.signUpBtn);
        signInGoogleBtn = findViewById(R.id.sign_in_button);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ref = db.getReference();

        googleTxt = (TextView) signInGoogleBtn.getChildAt(0);
        googleTxt.setText("Sign in with Google Account");

        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(LoginActivity.this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                .build();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTxt.getText().toString().trim();
                String password = pwdTxt.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    showInfoMessage("Email or password cannot be empty!");
                } else {
                    signInWithEmailAndPassword(email, password);
                }
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUp = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(signUp);
            }
        });

        forgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resetPwd = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(resetPwd);
            }
        });

        signInGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // When we get here in an automanager activity the error is likely not
        // resolvable - meaning Google Sign In and other Google APIs will be
        // unavailable.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(LoginActivity.this, "Unable to connect to Google account service.", Toast.LENGTH_SHORT).show();
    }

    private void signInWithEmailAndPassword(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user.isEmailVerified()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        showInfoMessage("Your email address is not verified! \n" +
                                "Please go to your email inbox to verify your PayMe account.");
                    }
                } else {  // if Firebase auth cannot sign in using credentials provided
                    Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showInfoMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google sign-in was successful, proceed to authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google sign-in failed
                Toast.makeText(LoginActivity.this, "Sign in with Google failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("firebaseAuthWithGoogle", "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            String userId = user.getUid();
                            String name = user.getDisplayName();
                            String email = user.getEmail();

                            openDialog(userId, name, email);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }

                    }
                });
    }

    private void openDialog(String userId, String name, String email) {
        // Get google_enter_number.xml view
        LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
        View view = inflater.inflate(R.layout.google_enter_number, null);

        final EditText userInput = (EditText) view.findViewById(R.id.editTextDialogUserInput);

        final AlertDialog d = new AlertDialog.Builder(LoginActivity.this)
                .setView(view)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(android.R.string.ok, null) // Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button okBtn = d.getButton(AlertDialog.BUTTON_POSITIVE);
                Button cancelBtn = d.getButton(AlertDialog.BUTTON_NEGATIVE);
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String phone = userInput.getText().toString().trim();

                        if (phone.isEmpty()) {
                            userInput.requestFocus();
                            userInput.setError("Please enter a phone number to complete sign-in.");
                        } else if (!phone.matches("^([89]{1})([0-9]{7})")) {
                            userInput.requestFocus();
                            userInput.setError("Please enter an 8-digit phone number that starts with 8 or 9!");
                        } else {  // After all preconditions are met

                            // Save user details into Firebase
                            User newUser = new User(name, phone, email);
                            ref.child("users").child(userId).setValue(newUser);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        auth.getCurrentUser().delete();
                        mGoogleApiClient.clearDefaultAccountAndReconnect();
                        d.cancel();
                    }
                });
            }
        });

        d.show();
        dialogOn = true;
    }

}
