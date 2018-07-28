package com.example.user.payme;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.user.payme.Objects.ReceiptItem;

import java.util.ArrayList;
import java.util.Locale;

public class EditReceiptActivity extends AppCompatActivity {
    private static final String TAG = "EditReceiptActivity";

    private LinearLayout parentLinearLayout;
    private EditText mItemName;
    private EditText mItemPrice;
    private ArrayList<ReceiptItem> receiptItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_receipt);
        parentLinearLayout = (LinearLayout) findViewById(R.id.parent_linear_layout);
    }

    public void onAddField(View v) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.edit_item_row, null);
        // Add the new row before the add field button.
        parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount() - 2);
    }

    public void onDelete(View v) {
        parentLinearLayout.removeView((View) v.getParent());
    }

    public void getAllFieldValue(View view) {
        final int childCount = parentLinearLayout.getChildCount();
        receiptItems = new ArrayList<>();

        for (int i = 0; i < childCount - 2; i++) {
            View v = parentLinearLayout.getChildAt(i);
            mItemName = v.findViewById(R.id.etItemName);
            mItemPrice = v.findViewById(R.id.etItemPrice);

            if (TextUtils.isEmpty(mItemName.getText())) {
                mItemName.setError("Required!");
                return;
            }

            if (TextUtils.isEmpty(mItemPrice.getText())) {
                mItemPrice.setError("Required!");
                return;
            }

            String name = mItemName.getText().toString().trim();
            double price = Double.parseDouble(mItemPrice.getText().toString().trim());
            String formattedPrice = String.format(Locale.ENGLISH, "%.2f", price);

            Log.d(TAG, "getAllFieldValue: name "+name+" price "+formattedPrice);

            receiptItems.add(new ReceiptItem(name, formattedPrice));
        }
    }
}
