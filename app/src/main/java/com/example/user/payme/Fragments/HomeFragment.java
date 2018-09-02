package com.example.user.payme.Fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.user.payme.Adapters.PaymentRecyclerViewAdapter;
import com.example.user.payme.Interfaces.OnFragmentInteractionListener;
import com.example.user.payme.MainActivity;
import com.example.user.payme.Objects.Payment;
import com.example.user.payme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class  HomeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private Context parentContext;
    private Typeface fontFace;
    private ArrayList<Payment> pendingPayments;
    private ArrayList<Payment> completedPayments;
    private RecyclerView paymentsList;
    private PaymentRecyclerViewAdapter pdgPaymentsAdapter;
    private PaymentRecyclerViewAdapter comPaymentsAdapter;
    private Button pendingBtn;
    private Button completedBtn;
    private View pending_selected;
    private View completed_selected;

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private FirebaseUser user;
    private String userId;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        savedInstanceState = null;
        super.onCreate(savedInstanceState);

        parentContext = getContext();
        fontFace = ResourcesCompat.getFont(parentContext, R.font.nunito);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ref = db.getReference();
        user = auth.getCurrentUser();
        userId = user.getUid();

        pendingPayments = new ArrayList<>();
        completedPayments = new ArrayList<>();
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
        paymentsList = view.findViewById(R.id.paymentRecyclerView);

        ((MainActivity) getActivity()).setActionBarTitle("Payments");

        // Show pending as selected first
        pending_selected.setVisibility(View.VISIBLE);

        LinearLayoutManager llm = new LinearLayoutManager(parentContext);
        paymentsList.setLayoutManager(llm);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(parentContext,
                llm.getOrientation());
        paymentsList.addItemDecoration(dividerItemDecoration);
        pdgPaymentsAdapter = new PaymentRecyclerViewAdapter(parentContext, pendingPayments);
        paymentsList.setAdapter(pdgPaymentsAdapter);

        // Retrieve the payments for this user from db into the respective array lists
        // and display pending payments
        GetPendingPayments();
        GetCompletedPayments();

        pendingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completed_selected.setVisibility(View.INVISIBLE);
                if (pending_selected.getVisibility() == View.INVISIBLE) {
                    pending_selected.setVisibility(View.VISIBLE);
                }
                ShowPendingPayments();
            }
        });

        completedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pending_selected.setVisibility(View.INVISIBLE);
                if (completed_selected.getVisibility() == View.INVISIBLE) {
                    completed_selected.setVisibility(View.VISIBLE);
                }
                ShowCompletedPayments();
            }
        });

        return view;
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


    private void GetPendingPayments() {
        ref.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("payments")) {  // checks if the user has any payment
                    Query query = ref.child("users").child(userId).child("payments")
                            .orderByChild("mStatus").equalTo("pending");

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot querySnapshot) {
                            if (querySnapshot.exists()) {
                                Iterable<DataSnapshot> payments = querySnapshot.getChildren();
                                for (DataSnapshot payment : payments) {
                                    Payment p = payment.getValue(Payment.class);
                                    if (!pendingPayments.contains(p)) {
                                        pendingPayments.add(p);
                                    } else {  // if payment with same name & date is in arraylist
                                        int index = pendingPayments.indexOf(p);
                                        Payment p2 = pendingPayments.get(index);   // old payment
                                        pendingPayments.remove(index);
                                        updatePayments(p2, p);
                                    }
                                }
                                Collections.sort(pendingPayments, (p1, p2) ->
                                        p1.getmAmount().compareTo(p2.getmAmount()));
                                pdgPaymentsAdapter = new PaymentRecyclerViewAdapter(parentContext, pendingPayments);
                                paymentsList.setAdapter(pdgPaymentsAdapter);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError queryError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }

    private void GetCompletedPayments() {
        ref.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("payments")) {  // checks if the user has any payment
                    Query query = ref.child("users").child(userId).child("payments")
                            .orderByChild("mStatus").equalTo("completed");

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot querySnapshot) {
                            if (querySnapshot.exists()) {
                                Iterable<DataSnapshot> payments = querySnapshot.getChildren();
                                for (DataSnapshot payment : payments) {
                                    Payment p = payment.getValue(Payment.class);
                                    completedPayments.add(p);
                                }
                                Collections.sort(completedPayments, (p1, p2) ->
                                        p1.getmAmount().compareTo(p2.getmAmount()));
                                comPaymentsAdapter = new PaymentRecyclerViewAdapter(parentContext, completedPayments);
                                paymentsList.setAdapter(comPaymentsAdapter);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError queryError) {  }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }

    private void ShowPendingPayments() {
        // Remove all existing payment views first
        paymentsList.removeAllViews();
        paymentsList.setAdapter(pdgPaymentsAdapter);
    }

    private void ShowCompletedPayments() {
        // Remove all existing payment views first
        paymentsList.removeAllViews();
        paymentsList.setAdapter(comPaymentsAdapter);
    }

    private void updatePayments(Payment p1, Payment p2) {
        // p1 is the old payment already in the list, p2 is to update into p1's amount.
        Double prevAmt = p1.getmAmount();
        Double newAmt = p2.getmAmount();
        String receiptIDs = p1.getmReceiptID();
        p1.setmAmount(prevAmt + newAmt);
        p1.setmReceiptID(receiptIDs + "," + p2.getmReceiptID());
        pendingPayments.add(p1);  // add back to arraylist
    }

}
