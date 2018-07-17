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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class RequestPaymentActivity extends AppCompatActivity {
    private static final String TAG = "RequestPaymentActivity";
    private static final double GST = 1.07;
    private static final double SERVICE_CHARGE = 1.1;

    private ArrayList<UserItem> userItems;
    private ArrayList<UserItem> fbUserItems;

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
        ArrayList<Contact> mContacts = (ArrayList<Contact>) getIntent().getSerializableExtra("Contact");
        HashMap<String, ArrayList<ReceiptItem>> result = (HashMap<String, ArrayList<ReceiptItem>>) getIntent().getSerializableExtra("result");
        ArrayList<ReceiptItem> receiptItems = (ArrayList<ReceiptItem>) getIntent().getSerializableExtra("receiptItemList");
        Receipt receipt = (Receipt) getIntent().getSerializableExtra("receipt");
        Log.d(TAG, "onCreate receipt "+receipt);

        HashMap<String, Double> finalAmount = new HashMap<>();
        double shared = 0;
        if (result.get(null) != null) {
            ArrayList<ReceiptItem> sharedItems = result.remove(null);

            for (ReceiptItem items : sharedItems) {
                Log.d(TAG, "onCreate: shared "+items.getmName() + " price: "+items.getmPrice());
                shared += Double.parseDouble(items.getmPrice());
            }

            shared = shared / mContacts.size();
            finalAmount.put("Shared items", shared);
        }



        userItems = new ArrayList<>();
        fbUserItems = new ArrayList<>();
        double allUsersTotal = 0;

        for (Contact contact : mContacts) {
            String name = contact.getmName();
            double total = 0;

            ArrayList<ReceiptItem> userItem = result.get(name);
            total = 0;
            for (ReceiptItem items : userItem) {
                total += Double.parseDouble(items.getmPrice());
            }


            userItem.add(new ReceiptItem("Shared items", String.valueOf(shared)));
            total = (total + shared) * GST * SERVICE_CHARGE;
            String formattedTotal = String.format("%.2f", total);
            userItems.add(new UserItem(contact, userItem, formattedTotal));

            // TODO get and set current user name
            if (!name.equals("You")) {
                allUsersTotal += total;
                fbUserItems.add(new UserItem(name, contact.getmPhoneNumber(), Double.parseDouble(formattedTotal), false));
            }
        }

        //todo delete this
        // ensure all is saving alright
        for (UserItem card: userItems) {
            Log.d(TAG, "onCreate: card "+card.getmContact().getmName());
            for (ReceiptItem items : card.getReceiptItems()) {
                Log.d(TAG, items.getmName()+ "  "+ items.getmPrice());
            }

            Log.d(TAG, "total "+card.getTotal());
        }

        Button total_button = (Button) findViewById(R.id.total_button);
        String formattedAllUsersTotal = String.format("Requesting: $%.2f", allUsersTotal);
        total_button.setText(formattedAllUsersTotal);

        initRecyclerView();

        total_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo save to firebase
                FirebaseAuth auth;
                FirebaseDatabase db;
                DatabaseReference ref;

                auth = FirebaseAuth.getInstance();
                db = FirebaseDatabase.getInstance();
                ref = db.getReference();

                FirebaseUser currentUser = auth.getCurrentUser();
                String userId = currentUser.getUid();

                Map<String, Object> postValues = new HashMap<>();
                receipt.setmItemList(receiptItems);
                receipt.setPayees(fbUserItems);
                String key = ref.child("users").push().getKey();
                postValues.put("receipts/"+key, receipt);

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
