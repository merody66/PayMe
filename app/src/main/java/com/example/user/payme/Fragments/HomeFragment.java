package com.example.user.payme.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.user.payme.Interfaces.OnFragmentInteractionListener;
import com.example.user.payme.MainActivity;
import com.example.user.payme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class  HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private TableLayout tableLayout;
    private Button pendingBtn;
    private Button completedBtn;
    private View pending_selected;
    private View completed_selected;
    private boolean pendingSelected;

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private FirebaseUser currentUser;
    private String userId;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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

        ((MainActivity) getActivity()).setActionBarTitle("Payments");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize widgets
        pendingBtn = view.findViewById(R.id.pendingBtn);
        completedBtn = view.findViewById(R.id.completedBtn);
        pending_selected = view.findViewById(R.id.pending_selected);
        completed_selected = view.findViewById(R.id.completed_selected);

        // Show contacts first
        pending_selected.setVisibility(View.VISIBLE);
        pendingSelected = true;


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
        ShowPendingPayments();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentMessage("HomeFragment");
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


    private void ShowPendingPayments() {
        ref.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("payments")) {  // checks if the user has any payment
                    Iterable<DataSnapshot> payments = snapshot.child("payments").getChildren();
                    for (DataSnapshot p : payments) {
                        if (p.getKey().equals("pending")) {
                            // write code for pending payments
                            Iterable<DataSnapshot> pendingPayments = p.getChildren();
                            for (DataSnapshot o : pendingPayments) {
                                Iterable<DataSnapshot> oweSnapshot = o.getChildren();
                                System.out.println(o.getKey() + " - children count: " + o.getChildrenCount());
                                switch(o.getKey()) {
                                    case "owe": {
                                        getOwePayments(oweSnapshot);
                                    }
                                    break;
                                    case "owed": {

                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }

    private void getOwePayments(Iterable<DataSnapshot> snapshot) {
        String mAmount, mName, mNumber, mReceiptID;
        mAmount = mName = mNumber = mReceiptID = "";

        for (DataSnapshot payments : snapshot) {
            Iterable<DataSnapshot> paymentItems = payments.getChildren();
            for (DataSnapshot paymentInfo : paymentItems) {
                switch (paymentInfo.getKey()) {
                    case "mAmount":
                        mAmount = paymentInfo.getValue().toString();
                        break;
                    case "mName":
                        mName = paymentInfo.getValue().toString();
                        break;
                    case "mNumber":
                        mNumber = paymentInfo.getValue().toString();
                        break;
                    case "mReceiptID":
                        mReceiptID = paymentInfo.getValue().toString();
                        break;
                }
            }
            addToTable(mName, mAmount, false);
        }


    }

    private void addToTable(String name, String amount, Boolean isOwed) {
        System.out.println(name + ", $" + amount);

        TextView nameTextView = new TextView(getContext());
        TextView oweAmtTextView = new TextView(getContext());
        TextView owedAmtTextView = new TextView(getContext());

        if (!isOwed) {
            nameTextView.setText(name);
            oweAmtTextView.setText("$" + amount);
            owedAmtTextView.setText("-");
        } else {

        }

        //tableLayout.addView(row, rowParams);


    }

    private void getOwedPayments(Iterable<DataSnapshot> snapshot) {

    }

    private void ShowCompletedPayments() {

    }

}
