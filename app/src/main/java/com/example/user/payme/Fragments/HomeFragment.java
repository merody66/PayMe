package com.example.user.payme.Fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.util.Locale;


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
    private LinearLayout paymentsList;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize widgets
        paymentsList = view.findViewById(R.id.paymentsList);
        pendingBtn = view.findViewById(R.id.pendingBtn);
        completedBtn = view.findViewById(R.id.completedBtn);
        pending_selected = view.findViewById(R.id.pending_selected);
        completed_selected = view.findViewById(R.id.completed_selected);

        ((MainActivity) getActivity()).setActionBarTitle("Payments");

        // Show pending as selected first
        pending_selected.setVisibility(View.VISIBLE);

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
        pendingPayments = new ArrayList<>();
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
                                    pendingPayments.add(p);
                                }
                                ShowPendingPayments();
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

    private void GetCompletedPayments() {
        completedPayments = new ArrayList<>();
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
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError queryError) {  }
                    });
                } else {
                    showInfoMessage();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }

    private void ShowPendingPayments() {
        // Remove all existing payment views first
        paymentsList.removeAllViews();
        for (Payment p : pendingPayments) {
            addToTable(p);
        }
    }

    private void ShowCompletedPayments() {
        // Remove all existing payment views first
        paymentsList.removeAllViews();
        for (Payment p : completedPayments) {
            addToTable(p);
        }
    }

    private void addToTable(Payment p) {
        // Initializing main layout, Text Views and params
        RelativeLayout layout = new RelativeLayout(parentContext);
        int height = dpToPx(50);  // row height
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, height);
        RelativeLayout.LayoutParams name_lp = new RelativeLayout.LayoutParams(
                400, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams owe_lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams owed_lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams date_lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams divider_lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, dpToPx(2));
        TextView nameTextView = new TextView(parentContext);
        TextView oweAmtTextView = new TextView(parentContext);
        TextView owedAmtTextView = new TextView(parentContext);
        TextView dateTextView = new TextView(parentContext);
        TextView receiptIDs = new TextView(parentContext);

        // Styling the TextViews
        nameTextView.setTypeface(fontFace);
        oweAmtTextView.setTypeface(fontFace);
        owedAmtTextView.setTypeface(fontFace);
        dateTextView.setTypeface(fontFace);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        oweAmtTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        owedAmtTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        receiptIDs.setVisibility(View.GONE);

        // Adding rules to the LayoutParams
        name_lp.addRule(RelativeLayout.ALIGN_PARENT_START);
        name_lp.addRule(RelativeLayout.CENTER_VERTICAL);
        name_lp.setMarginStart(dpToPx(10));  // margin_start 10dp
        owe_lp.addRule(RelativeLayout.ALIGN_PARENT_START);
        owe_lp.addRule(RelativeLayout.CENTER_VERTICAL);
        owe_lp.setMarginStart(dpToPx(135));   // margin_start 135dp
        owed_lp.addRule(RelativeLayout.ALIGN_PARENT_END);
        owed_lp.addRule(RelativeLayout.CENTER_VERTICAL);
        owed_lp.setMarginEnd(dpToPx(125));   // margin_end 125dp
        date_lp.addRule(RelativeLayout.ALIGN_PARENT_END);
        date_lp.addRule(RelativeLayout.CENTER_VERTICAL);
        date_lp.setMarginEnd(dpToPx(20));   // margin_end 20dp
        divider_lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        divider_lp.addRule(RelativeLayout.ALIGN_PARENT_START);

        // Divider
        int[] attr = {android.R.attr.listDivider};
        TypedArray ta = parentContext.getApplicationContext().obtainStyledAttributes(attr);
        Drawable dividerDrawable = ta.getDrawable(0);
        View divider = new View(parentContext);
        divider.setBackground(dividerDrawable);

        if (getView() != null) {
            RelativeLayout relativeLayout = getView().findViewWithTag(p.getmDate() + "," + p.getmName());
            if (relativeLayout == null) {  // if unable to find payment in existing rows
                nameTextView.setText(p.getmName());
                dateTextView.setText(p.getmDate());
                receiptIDs.setText(p.getmReceiptID());

                if (p.getmType().equals("owe")) {
                    oweAmtTextView.setText("$" + p.getmAmount());
                    owedAmtTextView.setText("-");
                } else {
                    oweAmtTextView.setText("-");
                    owedAmtTextView.setText("$" + p.getmAmount());
                }

                layout.setTag(p.getmDate() + "," + p.getmName());
                layout.addView(nameTextView, name_lp);
                layout.addView(oweAmtTextView, owe_lp);
                layout.addView(owedAmtTextView, owed_lp);
                layout.addView(dateTextView, date_lp);
                layout.addView(receiptIDs);
                layout.addView(divider, divider_lp);
                paymentsList.addView(layout, rlp);
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment fragment = new SettlePaymentFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("NAME", ((TextView) layout.getChildAt(0)).getText().toString());
                        bundle.putString("AMOUNT_OWE", ((TextView) layout.getChildAt(1)).getText().toString().substring(1));
                        bundle.putString("AMOUNT_OWED", ((TextView) layout.getChildAt(2)).getText().toString().substring(1));
                        bundle.putString("PAYMENT_STATUS", p.getmStatus());
                        bundle.putString("DATE", p.getmDate());
                        bundle.putString("RECEIPT_IDS", ((TextView) layout.getChildAt(4)).getText().toString());
                        fragment.setArguments(bundle);
                        goToPaymentPage(fragment);
                    }
                });
            } else {  // otherwise, just update the information in the existing view
                if (p.getmType().equals("owe")) {
                    oweAmtTextView = (TextView) relativeLayout.getChildAt(1);
                    String previousAmt = oweAmtTextView.getText().toString();
                    relativeLayout.removeViewAt(1);

                    if (previousAmt.equals("-")) {
                        oweAmtTextView.setText("$" + p.getmAmount());
                    } else {
                        Double prev = Double.parseDouble(previousAmt.substring(1));  // remove the '$'
                        Double total = prev + p.getmAmount();
                        oweAmtTextView.setText("$" + String.format(Locale.ENGLISH,"%.2f", total));
                    }
                    relativeLayout.addView(oweAmtTextView, 1);
                } else {
                    owedAmtTextView = (TextView) relativeLayout.getChildAt(2);
                    String previousAmt = owedAmtTextView.getText().toString();
                    relativeLayout.removeViewAt(2);

                    if (previousAmt.equals("-")) {
                        owedAmtTextView.setText("$" + p.getmAmount());
                    } else {
                        Double prev = Double.parseDouble(previousAmt.substring(1));  // remove the '$'
                        Double total = prev + p.getmAmount();
                        owedAmtTextView.setText("$" + String.format(Locale.ENGLISH, "%.2f", total));
                    }
                    relativeLayout.addView(owedAmtTextView, 2);
                }

                receiptIDs = (TextView) relativeLayout.getChildAt(4);
                relativeLayout.removeViewAt(4);
                receiptIDs.setText(receiptIDs.getText().toString() + "," + p.getmReceiptID());
                relativeLayout.addView(receiptIDs, 4);
            }
        }
    }

    private int dpToPx(int dp) {
        float density = parentContext.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void goToPaymentPage(Fragment fragment) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        // previous state will be added to the backstack, allowing you to go back with the back button.
        // must be done before commit.
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showInfoMessage() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(100, 50, 100, 0);
        TextView messageTextView = new TextView(parentContext);
        messageTextView.setLayoutParams(params);
        //messageTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        messageTextView.setTypeface(fontFace, Typeface.ITALIC);
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        messageTextView.setText(R.string.home_info_msg);
        paymentsList.addView(messageTextView);
    }

}
