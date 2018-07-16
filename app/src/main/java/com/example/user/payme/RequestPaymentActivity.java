package com.example.user.payme;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import com.example.user.payme.Adapters.UserItemAdapter;
import com.example.user.payme.Objects.UserItem;
import com.example.user.payme.Objects.Contact;
import com.example.user.payme.Objects.ReceiptItem;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestPaymentActivity extends AppCompatActivity {
    private static final String TAG = "RequestPaymentActivity";
    private static final double GST = 1.07;
    private static final double SERVICE_CHARGE = 1.1;

    private ArrayList<UserItem> userItems;

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
        double allUsersTotal = 0;

        for (Contact contact : mContacts) {
            String name = contact.getmName();
            double total = 0;

            ArrayList<ReceiptItem> userItem = result.get(name);
            total = 0;
            for (ReceiptItem items : userItem) {
//                Log.d(TAG, "get item "+items.getmName() + " price: "+items.getmPrice());
                total += Double.parseDouble(items.getmPrice());
            }


            userItem.add(new ReceiptItem("Shared items", String.valueOf(shared)));
            total = (total + shared) * GST * SERVICE_CHARGE;
            String formattedTotal = String.format("%.2f", total);
            userItems.add(new UserItem(contact, userItem, formattedTotal));

            allUsersTotal += total;
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

        Button mButton = (Button) findViewById(R.id.total_button);
        String formattedAllUsersTotal = String.format("Total: $%.2f", allUsersTotal);
        mButton.setText(formattedAllUsersTotal);

        initRecyclerView();
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
    }
}
