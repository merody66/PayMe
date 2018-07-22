package com.example.user.payme;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.user.payme.Adapters.UserItemAdapter;
import com.example.user.payme.Objects.Contact;
import com.example.user.payme.Objects.Receipt;
import com.example.user.payme.Objects.ReceiptItem;
import com.example.user.payme.Objects.UserItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class RequestPaymentActivity extends AppCompatActivity {
    private static final String TAG = "RequestPaymentActivity";
    private static final double GST = 1.07;
    private static final double SERVICE_CHARGE = 1.1;

    private ArrayList<UserItem> userItems;
    private ArrayList<UserItem> fbUserItems;
    private double allUsersTotal;
    ArrayList<ReceiptItem> updatedSharedItems;

    private ArrayList<Contact> mContacts;
    private HashMap<String, ArrayList<ReceiptItem>> mResult;
    private ArrayList<ReceiptItem> mReceiptItems;
    private Receipt mReceipt;
    private double mShared;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(RequestPaymentActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_history:
                    return true;
                case R.id.navigation_addNewReceipt:
                    return true;
                case R.id.navigation_contacts:
                    return true;
                case R.id.navigation_account:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_payment);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_addNewReceipt);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Get extra from intent
        mContacts = (ArrayList<Contact>) getIntent().getSerializableExtra("Contact");
        mResult = (HashMap<String, ArrayList<ReceiptItem>>) getIntent().getSerializableExtra("result");
        mReceiptItems = (ArrayList<ReceiptItem>) getIntent().getSerializableExtra("receiptItemList");
        mReceipt = (Receipt) getIntent().getSerializableExtra("receipt");
        Log.d(TAG, "onCreate mReceipt "+ mReceipt);

        mShared = calcIndivSharedAmt();
        setIndivItemsInfo();

        Button total_button = (Button) findViewById(R.id.total_button);
        String formattedAllUsersTotal = String.format(Locale.ENGLISH, "Requesting: $%.2f", allUsersTotal);
        total_button.setText(formattedAllUsersTotal);

        initRecyclerView();

        total_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth;
                FirebaseDatabase db;
                DatabaseReference ref;

                auth = FirebaseAuth.getInstance();
                db = FirebaseDatabase.getInstance();
                ref = db.getReference();

                FirebaseUser currentUser = auth.getCurrentUser();
                String userId = currentUser.getUid();

                Map<String, Object> postValues = new HashMap<>();
                mReceipt.setmItemList(mReceiptItems);
                mReceipt.setPayees(fbUserItems);
                String key = ref.child("users").push().getKey();
                postValues.put("receipts/"+key, mReceipt);

                ref.child("users").child(userId).updateChildren(postValues)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RequestPaymentActivity.this);
                                builder.setMessage("Requested Successfully")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                startActivity(new Intent(RequestPaymentActivity.this, MainActivity.class));
                                                // End the activity
                                                finish();
                                            }
                                        });

                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RequestPaymentActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }

    private double calcIndivSharedAmt() {
        double shared = 0;
        updatedSharedItems = new ArrayList<>();
        int contactsSize = mContacts.size();
        if (mResult.get(null) != null) {
            ArrayList<ReceiptItem> sharedItems = mResult.remove(null);
            for (ReceiptItem items : sharedItems) {
                String name = items.getmName();
                String price = items.getmPrice();
                price = price.replace("$", "");
                price = price.replace(",", "");
                double parsedPrice = 0;
                try {
                    parsedPrice = Double.parseDouble(price);
                } catch (NumberFormatException e ){
                    Log.d(TAG, "calcIndivSharedAmt: WRONG NUMBER FORMAT");
                }

                updatedSharedItems.add(new ReceiptItem(name, String.valueOf(parsedPrice/contactsSize)));
                shared += parsedPrice;
            }

            shared = shared / contactsSize;
        }

        return shared;
    }

    /**
     * Get the individual UserItem and in another format for firebase
     * This also calculate the total requesting amount.
     */
    private void setIndivItemsInfo() {
        userItems = new ArrayList<>();
        fbUserItems = new ArrayList<>();
        allUsersTotal = 0;

        for (Contact contact : mContacts) {
            String name = contact.getmName();
            String phoneNumber = contact.getmPhoneNumber();
            double total = 0;

            ArrayList<ReceiptItem> userItem = mResult.get(name);

            // Adding all the shared item to calculate later
            ArrayList<ReceiptItem> userIncSharedItems = new ArrayList<>();
            userIncSharedItems.addAll(updatedSharedItems);

            if (userItem != null) {
                userIncSharedItems.addAll(userItem);
            } else {
                userItem = new ArrayList<>();
            }

            for (ReceiptItem items : userIncSharedItems) {
                total += Double.parseDouble(items.getmPrice());
            }

            userItem.add(new ReceiptItem("Shared items", String.valueOf(mShared)));
            total = total * GST * SERVICE_CHARGE;
            String formattedTotal = String.format(Locale.ENGLISH, "$%.2f", total);
            String withoutSignTotal = String.format(Locale.ENGLISH, "%.2f", total);
            userItems.add(new UserItem(contact, userItem, formattedTotal));

            // TODO get and set current user name
            if (!name.equals("You")) {
                allUsersTotal += total;
                if (total != 0) {
                    fbUserItems.add(new UserItem(name, phoneNumber, Double.parseDouble(withoutSignTotal), false));
                }
            }
        }
    }

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: started");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.cardRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        //todo delete this
        // ensure all is saving alright
        for (UserItem card : userItems) {
            Log.d(TAG, "initRecyclerView: card "+card.getmContact().getmName());
            for (ReceiptItem items : card.getReceiptItems()) {
                Log.d(TAG, items.getmName()+ "  "+ items.getmPrice());
            }

            Log.d(TAG, "total "+card.getTotal());
        }
        UserItemAdapter adapter = new UserItemAdapter(this, userItems);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        recyclerView.addItemDecoration(decoration);
    }
}
