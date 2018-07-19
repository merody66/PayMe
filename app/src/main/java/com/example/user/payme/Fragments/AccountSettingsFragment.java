package com.example.user.payme.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.payme.AddReceiptActivity;
import com.example.user.payme.LoginActivity;
import com.example.user.payme.MainActivity;
import com.example.user.payme.Objects.User;
import com.example.user.payme.R;
import com.example.user.payme.SignupActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountSettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountSettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private EditText nameTxt;
    private EditText emailTxt;
    private EditText paylahTxt;
    private Switch notifyToggle;
    private TextView logoutLbl;
    private TextView chngPwdLbl;
    private Button saveBtn;
    private String userId;
    private int nonEmptyFields;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private FirebaseUser currentUser;

    public AccountSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountSettingsFragment newInstance(String param1, String param2) {
        AccountSettingsFragment fragment = new AccountSettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        ((MainActivity) getActivity())
                .setActionBarTitle("Account Settings");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);

        // Initialize widgets
        nameTxt = view.findViewById(R.id.nameTxtField);
        emailTxt = view.findViewById(R.id.emailTxtField);
        chngPwdLbl = view.findViewById(R.id.chngPwdLbl);
        paylahTxt = view.findViewById(R.id.paylahTxtField);
        notifyToggle = view.findViewById(R.id.notifyToggle);
        saveBtn = view.findViewById(R.id.saveBtn);
        logoutLbl = view.findViewById(R.id.logoutLbl);


        chngPwdLbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        notifyToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if (isChecked) {
                   setNotificationSetting(true);
                   Toast.makeText(getContext(), "Notifications turned on.", Toast.LENGTH_SHORT).show();
               } else {
                   setNotificationSetting(false);
                   Toast.makeText(getContext(), "Notifications turned off.", Toast.LENGTH_SHORT).show();
               }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameTxt.getText().toString().trim();
                String email = emailTxt.getText().toString().trim();
                String paylahNumber = paylahTxt.getText().toString().trim();

                // Checks if the input fields are empty before saving changes
                checkEmptyInput(nameTxt, name);
                checkEmptyInput(emailTxt, email);
                checkEmptyInput(paylahTxt, paylahNumber);
                System.out.println(nonEmptyFields);
                if (nonEmptyFields == 3) {
                    saveAccountInfo(name, email, paylahNumber);
                }
                nonEmptyFields = 0;
            }
        });

        logoutLbl.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Show Alert Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Log out of PayMe?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                sendToLogin();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ref = db.getReference();

        currentUser = auth.getCurrentUser();
        userId = currentUser.getUid();
        loadAccountDetails();
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }

    private void loadAccountDetails() {
        ref.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                nameTxt.setText(user.getName());
                emailTxt.setText(user.getEmail());
                paylahTxt.setText(user.getNumber());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });

    }

    private void setNotificationSetting(boolean bool) {
        ref.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> postValues = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    postValues.put(snapshot.getKey(), snapshot.getValue());
                }
                postValues.put("notificationSetting", bool);
                ref.child("users").child(userId).updateChildren(postValues);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }

    private void sendToLogin() {
        GoogleSignInClient googleSignInClient ;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(getContext(), gso);
        googleSignInClient.signOut().addOnCompleteListener(getActivity(),
                new OnCompleteListener<Void>() {  // Google auth sign out
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().signOut(); // Firebase auth sign out
                        Intent loginIntent = new Intent(getContext(), LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginIntent);
                    }
                });
    }

    private void checkEmptyInput(EditText et, String input) {
        if (input.isEmpty()) {
            String errorMsg;
            if (et == nameTxt) {
                errorMsg = "<font color='red'><i>Name cannot be empty!</i></font>";
                et.setText(Html.fromHtml(errorMsg));
            } else if (et == emailTxt) {
                errorMsg = "<font color='red'><i>Email cannot be empty!</i></font>";
                et.setText(Html.fromHtml(errorMsg));
            } else {  // paylah number
                errorMsg = "<font color='red'><i>Number cannot be empty!</i></font>";
                et.setText(Html.fromHtml(errorMsg));
            }
        } else {
            // if not empty and is not error message
            if (!input.contains("cannot be empty")) {
                nonEmptyFields++;
            }
        }
    }

    private void saveAccountInfo(String name, String email, String paylahNumber) {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (paylahNumber.matches("^([89]{1})([0-9]{7})")) {
                Toast.makeText(getContext(), "Saving account details...", Toast.LENGTH_SHORT).show();
                ref.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> postValues = new HashMap<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            postValues.put(snapshot.getKey(), snapshot.getValue());
                        }
                        postValues.put("name", name);
                        postValues.put("email", email);
                        postValues.put("number", paylahNumber);
                        ref.child("users").child(userId).updateChildren(postValues);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {  }
                });

            } else {  // invalid phone number (starting with 8 or 9 and 8 digits long)
                Toast.makeText(getContext(), "Invalid phone number.", Toast.LENGTH_SHORT).show();
            }
        } else {  // invalid email address
            Toast.makeText(getContext(), "Invalid email address.", Toast.LENGTH_SHORT).show();
        }

        }

}



