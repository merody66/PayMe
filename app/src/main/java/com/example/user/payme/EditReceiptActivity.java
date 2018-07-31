package com.example.user.payme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.user.payme.Objects.Contact;
import com.example.user.payme.Objects.Receipt;
import com.example.user.payme.Objects.ReceiptItem;

import java.util.ArrayList;
import java.util.Locale;

// https://www.androidtutorialpoint.com/basics/dynamically-add-and-remove-views-in-android/
public class EditReceiptActivity extends AppCompatActivity {
    private static final String TAG = "EditReceiptActivity";
    private static final int OFFSET = 1;

    private LinearLayout parentLinearLayout;
    private ScrollView scrollView;
    private EditText mItemName;
    private EditText mItemPrice;
    private EditText mEtShopname;
    private EditText mEtDate;
    private ArrayList<ReceiptItem> receiptItems = new ArrayList<>();
    private ArrayList<Contact> contacts;
    private String shopname;
    private String date;
    private String gstAmt;
    private String serviceChargeAmt;
    private String subtotalAmt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_receipt);
        parentLinearLayout = (LinearLayout) findViewById(R.id.parent_linear_layout);
        scrollView = (ScrollView) findViewById(R.id.edit_scroll_view);

        receiptItems = (ArrayList<ReceiptItem>) getIntent().getSerializableExtra("itemList");
        contacts = (ArrayList<Contact>) getIntent().getSerializableExtra("Contacts");
        shopname = getIntent().getStringExtra("shopname");
        date = getIntent().getStringExtra("date");

        View firstRow = parentLinearLayout.getChildAt(0);
        mEtShopname = firstRow.findViewById(R.id.editShopname);
        mEtDate = firstRow.findViewById(R.id.editDate);

        if (!shopname.isEmpty()) {
            mEtShopname.setText(shopname);
        }

        if (!date.isEmpty()) {
            mEtDate.setText(date);
        }

        Log.d(TAG, "onCreate: AFTER INTENT");

        if (!receiptItems.isEmpty()) {
            for (ReceiptItem item : receiptItems) {
                String name = item.getmName();
                String price = item.getmPrice();

                setAddView(name, price);
            }

            scrollDown();
        } else {
            setAddView("", "");
        }
    }

    // As it takes around 200ms to add element and update,
    // the scrolling action will only be activated 200ms later.
    private void scrollDown() {
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 200);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_group_bar, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done_btn:
                boolean isEmpty = getAllFieldValue();

                if (receiptItems == null || receiptItems.isEmpty() || isEmpty) {
                    Log.d(TAG, "onOptionsItemSelected: INSIDE");
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditReceiptActivity.this);
                    builder.setMessage("Item cannot be empty!")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return false;
                }

                Intent intent = new Intent(EditReceiptActivity.this, ShowActivity.class);
                Receipt receipt = new Receipt(shopname, date, gstAmt, serviceChargeAmt, subtotalAmt, receiptItems);
                intent.putExtra("from_activity", "EditReceiptActivity");
                intent.putExtra("receipt", receipt);
                intent.putExtra("Contacts",contacts);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void setAddView(String name, String price) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.edit_item_row, null);
        // Add the new row before the add field button.
        parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount() - OFFSET);

        mItemName = rowView.findViewById(R.id.etItemName);
        mItemPrice = rowView.findViewById(R.id.etItemPrice);

        mItemName.setText(name);
        mItemPrice.setText(price);
    }

    public void onAddField(View v) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.edit_item_row, null);
        // Add the new row before the add field button.
        parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount() - OFFSET);
        Log.d(TAG, "onAddField: AFTER ADDING FIELD");
        scrollDown();
    }

    public void onDelete(View v) {
        Log.d(TAG, "onDelete: view "+v);
        parentLinearLayout.removeView((View) v.getParent());
    }

    /**
     * Delete all existing records and row, and add the buttons and a row back.
     * @param v child view that is not used
     */
    public void onClear(View v) {
        receiptItems = new ArrayList<>();

        final int childCount = parentLinearLayout.getChildCount();
        receiptItems = new ArrayList<>();

        Log.d(TAG, "onClear: CHILD COUNT FINAL"+childCount);

        View firstRow = parentLinearLayout.getChildAt(0);
        mEtShopname = firstRow.findViewById(R.id.editShopname);
        mEtDate = firstRow.findViewById(R.id.editDate);

        mEtShopname.setText("");
        mEtDate.setText("");

        for (int i = childCount - 2; i >= 1; i--) {
            Log.d(TAG, "onClear: CHILD COUNT LOOP"+parentLinearLayout.getChildCount());

            View rowView = parentLinearLayout.getChildAt(i);
            mItemName = rowView.findViewById(R.id.etItemName);
            mItemPrice = rowView.findViewById(R.id.etItemPrice);

            String name = mItemName.getText().toString().trim();
            String price = mItemPrice.getText().toString().trim();
//
            Log.d(TAG, "onClear: name "+name+" price "+price);

            parentLinearLayout.removeView(rowView);

        }


//        parentLinearLayout.removeAllViews();
//
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View rowView = inflater.inflate(R.layout.edit_buttons, null);
//        parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount() - OFFSET);

        onAddField(v);
    }

    public boolean getAllFieldValue() {
        final int childCount = parentLinearLayout.getChildCount();
        receiptItems = new ArrayList<>();
        double subtotal = 0;

        View firstRow = parentLinearLayout.getChildAt(0);
        mEtShopname = firstRow.findViewById(R.id.editShopname);
        mEtDate = firstRow.findViewById(R.id.editDate);

        shopname = mEtShopname.getText().toString().trim();
        date = mEtDate.getText().toString().trim();

        if (shopname.isEmpty()) {
            mEtShopname.setError("Required!");
            return true;
        }

        if (date.isEmpty()) {
            mEtDate.setError("Required!");
            return true;
        }

        Log.d(TAG, "getAllFieldValue: shopname "+shopname);
        Log.d(TAG, "getAllFieldValue: date "+ date);

        for (int i = 1; i < childCount - OFFSET; i++) {
            View v = parentLinearLayout.getChildAt(i);
            mItemName = v.findViewById(R.id.etItemName);
            mItemPrice = v.findViewById(R.id.etItemPrice);

            String name = mItemName.getText().toString().trim();
            String price = mItemPrice.getText().toString().trim();

            if (name.isEmpty()) {
                mItemName.setError("Required!");
                return true;
            }

            if (price.isEmpty()) {
                mItemPrice.setError("Required!");
                return true;
            }

            double priceDouble = Double.parseDouble(price);
            subtotal += priceDouble;
            String formattedPrice = String.format(Locale.ENGLISH, "%.2f", priceDouble);

            Log.d(TAG, "getAllFieldValue: name "+name+" price "+formattedPrice);

            receiptItems.add(new ReceiptItem(name, formattedPrice));
        }
        subtotalAmt = String.format(Locale.ENGLISH, "%.2f", subtotal);
        serviceChargeAmt = String.format(Locale.ENGLISH, "%.2f", subtotal * 0.1);
        gstAmt = String.format(Locale.ENGLISH, "%.2f", subtotal * 0.07);

        return false;
    }
}
