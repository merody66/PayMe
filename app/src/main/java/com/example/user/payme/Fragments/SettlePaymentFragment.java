package com.example.user.payme.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SettlePaymentFragment extends Fragment {

    private static final String NAME = "NAME";
    private static final String TOTAL_AMOUNT = "TOTAL_AMOUNT";
    private static final String PAYMENT_STATUS = "PAYMENT_STATUS";
    private static final String DATE = "DATE";
    private static final String RECEIPT_IDS = "RECEIPT_IDS";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private String arg_name;
    private String arg_date;
    private Double arg_totalAmt;
    private String arg_status;
    private String[] arg_receiptIDs;
    private LinearLayout itemDetailsLayout;
    private TextView paymentDetails;
    private TextView nettAmount;
    private TextView date;
    private Button payBtn;

    final int REQUEST_CODE = 1;
    final String mock_client_token = "eyJ2ZXJzaW9uIjoyLCJhdXRob3JpemF0aW9uRmluZ2VycHJpbnQiOiI4MzMzYmQzOTI2NTRmZDUzNmMyNjQ0MzliNTc0NmVkZGZkZWI0OWEzNDNjODA3MzU5Yjk4OTc2MjRjNThkMTA1fGNyZWF0ZWRfYXQ9MjAxOC0wNy0yNlQwMDo0MTo0Mi45ODkzNjMwNzcrMDAwMFx1MDAyNm1lcmNoYW50X2lkPTM0OHBrOWNnZjNiZ3l3MmJcdTAwMjZwdWJsaWNfa2V5PTJuMjQ3ZHY4OWJxOXZtcHIiLCJjb25maWdVcmwiOiJodHRwczovL2FwaS5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tOjQ0My9tZXJjaGFudHMvMzQ4cGs5Y2dmM2JneXcyYi9jbGllbnRfYXBpL3YxL2NvbmZpZ3VyYXRpb24iLCJjaGFsbGVuZ2VzIjpbXSwiZW52aXJvbm1lbnQiOiJzYW5kYm94IiwiY2xpZW50QXBpVXJsIjoiaHR0cHM6Ly9hcGkuc2FuZGJveC5icmFpbnRyZWVnYXRld2F5LmNvbTo0NDMvbWVyY2hhbnRzLzM0OHBrOWNnZjNiZ3l3MmIvY2xpZW50X2FwaSIsImFzc2V0c1VybCI6Imh0dHBzOi8vYXNzZXRzLmJyYWludHJlZWdhdGV3YXkuY29tIiwiYXV0aFVybCI6Imh0dHBzOi8vYXV0aC52ZW5tby5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tIiwiYW5hbHl0aWNzIjp7InVybCI6Imh0dHBzOi8vb3JpZ2luLWFuYWx5dGljcy1zYW5kLnNhbmRib3guYnJhaW50cmVlLWFwaS5jb20vMzQ4cGs5Y2dmM2JneXcyYiJ9LCJ0aHJlZURTZWN1cmVFbmFibGVkIjp0cnVlLCJwYXlwYWxFbmFibGVkIjp0cnVlLCJwYXlwYWwiOnsiZGlzcGxheU5hbWUiOiJBY21lIFdpZGdldHMsIEx0ZC4gKFNhbmRib3gpIiwiY2xpZW50SWQiOm51bGwsInByaXZhY3lVcmwiOiJodHRwOi8vZXhhbXBsZS5jb20vcHAiLCJ1c2VyQWdyZWVtZW50VXJsIjoiaHR0cDovL2V4YW1wbGUuY29tL3RvcyIsImJhc2VVcmwiOiJodHRwczovL2Fzc2V0cy5icmFpbnRyZWVnYXRld2F5LmNvbSIsImFzc2V0c1VybCI6Imh0dHBzOi8vY2hlY2tvdXQucGF5cGFsLmNvbSIsImRpcmVjdEJhc2VVcmwiOm51bGwsImFsbG93SHR0cCI6dHJ1ZSwiZW52aXJvbm1lbnROb05ldHdvcmsiOnRydWUsImVudmlyb25tZW50Ijoib2ZmbGluZSIsInVudmV0dGVkTWVyY2hhbnQiOmZhbHNlLCJicmFpbnRyZWVDbGllbnRJZCI6Im1hc3RlcmNsaWVudDMiLCJiaWxsaW5nQWdyZWVtZW50c0VuYWJsZWQiOnRydWUsIm1lcmNoYW50QWNjb3VudElkIjoiYWNtZXdpZGdldHNsdGRzYW5kYm94IiwiY3VycmVuY3lJc29Db2RlIjoiVVNEIn0sIm1lcmNoYW50SWQiOiIzNDhwazljZ2YzYmd5dzJiIiwidmVubW8iOiJvZmYifQ==";
    final String send_payment_details = "http://127.0.0.1/";
    String amount;
    HashMap<String, String> paramHash;

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private FirebaseUser currentUser;
    private String userId;

    public SettlePaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            arg_name = getArguments().getString(NAME);
            arg_totalAmt = getArguments().getDouble(TOTAL_AMOUNT);
            arg_status = getArguments().getString(PAYMENT_STATUS);
            arg_date = getArguments().getString(DATE);
            arg_receiptIDs = getArguments().getString(RECEIPT_IDS).split(",");
        }

        paramHash = new HashMap<>();
        ((MainActivity) getActivity()).setActionBarTitle("Settle Payment");

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ref = db.getReference();
        currentUser = auth.getCurrentUser();
        userId = currentUser.getUid();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settle_payment, container, false);

        // Initialize widgets
        paymentDetails = view.findViewById(R.id.paymentDetails);
        nettAmount = view.findViewById(R.id.nettAmount);
        date = view.findViewById(R.id.date);
        itemDetailsLayout = view.findViewById(R.id.itemDetailsLayout);
        payBtn = view.findViewById(R.id.payBtn);

        paymentDetails.setText(arg_name + " & You");
        nettAmount.setText("$ " + arg_totalAmt.toString());
        date.setText(arg_date);

        if (arg_totalAmt > 0.0 || arg_status.equals("completed")) {
            // Remove the payment button if the nett amt is positive (people owe you),
            // or when the payment status is already completed.
            payBtn.setVisibility(View.GONE);
        }

        for (String id : arg_receiptIDs) {
            processReceipt(id);
        }

        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBraintreeSubmit();
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentMessage("SettlePaymentFragment");
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

    private void processReceipt(String id) {
        Context context = getContext();
        Typeface fontFace = ResourcesCompat.getFont(context, R.font.nunito);

        LinearLayout receipt = new LinearLayout(context);
        receipt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        receipt.setOrientation(LinearLayout.VERTICAL);
        TextView receiptID = new TextView(context);

        if (arg_totalAmt > 0.0) {  // search from own receipts (owed amount)
            receiptID.setText(arg_name + "'s items for this payment:");
            receiptID.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            receiptID.setTypeface(fontFace, Typeface.BOLD);
            receipt.addView(receiptID);

            ref.child("users").child(userId).child("receipts").child(id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    long numPayees = snapshot.child("payees").getChildrenCount() + 1;  // num of payees
                    Iterable<DataSnapshot> iterable = snapshot.child("mItemList").getChildren();
                    for (DataSnapshot ss : iterable) {
                        if (ss.hasChild("mBelongsTo")) {
                            if (ss.child("mBelongsTo").getValue().toString().equals(arg_name)) {
                                String itemName = ss.child("mName").getValue().toString();
                                String amt = ss.child("mPrice").getValue().toString();
                                setAddView(receipt, itemName, amt);
                            }
                        } else {  // shared item
                            double sharedPrice = 0.0;
                            String itemName = ss.child("mName").getValue().toString();
                            itemName += " (shared)";
                            String amt = ss.child("mPrice").getValue().toString();
                            sharedPrice = Double.parseDouble(amt) / numPayees;
                            setAddView(receipt, itemName, String.format("%.2f", sharedPrice));
                        }
                    }

                    itemDetailsLayout.addView(receipt);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {  }
            });
        } else {
            // to-do (owe amount)
            receiptID.setText("Your items for this payment:");
            receiptID.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            receiptID.setTypeface(fontFace, Typeface.BOLD);
            receipt.addView(receiptID);
        }
    }

    private void setAddView(LinearLayout layout, String itemName, String amt) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View receiptView = inflater.inflate(R.layout.settlepayment_receipt_item, null);
        layout.addView(receiptView);

        TextView mItemName = receiptView.findViewById(R.id.item_name);
        TextView mItemAmt = receiptView.findViewById(R.id.item_amt);

        mItemName.setText(itemName);
        mItemAmt.setText(amt);

    }

    public void onBraintreeSubmit() {
        DropInRequest dropInRequest = new DropInRequest().clientToken(mock_client_token);
        startActivityForResult(dropInRequest.getIntent(getContext()), REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                String stringNonce = nonce.getNonce();
                Log.d("PAYMENT NONCE", "Result: " + stringNonce);

                // Send payment price with the nonce
                // use the result to update your UI and send the payment method nonce to your server
                if (arg_totalAmt < 0.0)
                    amount = nettAmount.getText().toString().substring(2);
                else
                    amount = nettAmount.getText().toString().substring(1);

                paramHash.put("amount", amount);
                paramHash.put("nonce", stringNonce);
                sendPaymentDetails();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user cancelled action
                Log.d("ERROR", "User cancelled payment action.");
            } else {
                // Handle exceptions from Braintree's DropInRequest here
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.d("ERROR", "Error: " + error.toString());
            }
        }
    }

    private void sendPaymentDetails() {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, send_payment_details,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.contains("Successful")) {
                        Toast.makeText(getContext(), "Payment successful.", Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(getContext(), "Payment failed", Toast.LENGTH_LONG).show();
                    Log.d("SEND PAYMENT DETAILS ", "Final Response: " + response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("SEND PAYMENT DETAILS", "Volley Error : " + error.toString());
                    // Need to create a backend server to generate client token and process HttpRequests,
                    // so for now, this is the temporary solution.
                    Toast.makeText(getContext(), "Payment Successful.", Toast.LENGTH_LONG).show();
                    updatePaymentOnSuccess();
                }
            }) {
            @Override
            protected Map<String, String> getParams() {
                if (paramHash == null)
                    return null;
                Map<String, String> params = new HashMap<>();
                for (String key : paramHash.keySet()) {
                    params.put(key, paramHash.get(key));
                    Log.d("mylog", "Key : " + key + " Value : " + paramHash.get(key));
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(stringRequest);
    }

    private void updatePaymentOnSuccess() {
        Query query = ref.child("users").child(userId).child("payments")
                .orderByChild("mName").equalTo(arg_name);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot querySnapshot) {
                if (querySnapshot.exists()) {
                    Iterable<DataSnapshot> payments = querySnapshot.getChildren();
                    for (DataSnapshot payment : payments) {
                         Payment p = payment.getValue(Payment.class);
                         p.setmStatus("completed");
                         //System.out.println(payment.getRef().toString());
                         payment.getRef().setValue(p);
                    }
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    fm.popBackStack();
                }
            }

            @Override
            public void onCancelled(DatabaseError queryError) {  }
        });
    }

}
