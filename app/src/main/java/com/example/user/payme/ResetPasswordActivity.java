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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailTxt;
    private Button resetBtn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize widgets
        emailTxt = findViewById(R.id.emailTxtField);
        resetBtn = findViewById(R.id.resetBtn);
        auth = FirebaseAuth.getInstance();

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTxt.getText().toString().trim();
                resetPwd(email);
            }
        });
    }

    private void resetPwd(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Reset Password", "Reset password email sent.");

                            // Show Alert Dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this);
                            builder.setMessage("A reset password email has been sent to your entered email address.")
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
                            Log.e("Reset Password", task.getException().toString());
                        }
                    }
                });
    }
}
