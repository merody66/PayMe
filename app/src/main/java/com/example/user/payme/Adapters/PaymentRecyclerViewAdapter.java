package com.example.user.payme.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.payme.Fragments.SettlePaymentFragment;
import com.example.user.payme.Objects.Payment;
import com.example.user.payme.R;

import java.util.ArrayList;

public class PaymentRecyclerViewAdapter extends RecyclerView.Adapter<PaymentRecyclerViewAdapter.PaymentViewHolder> {

    private static final String TAG = "ReceiptRecyclerViewAdapter";
    private Context mContext;
    private ArrayList<Payment> mPayments;

    public PaymentRecyclerViewAdapter(Context mContext, ArrayList<Payment> mPayments) {
        this.mContext = mContext;
        this.mPayments = mPayments;
    }


    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_payment_item, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment p = mPayments.get(position);

        String name = p.getmName();
        String number = p.getmNumber();
        String date = p.getmDate();
        Double amount = p.getmAmount();
        String receiptIDs = p.getmReceiptID();
        //System.out.println(date + ", " + name + ", $" + amount);

        holder.payment_name.setText(name);
        holder.payment_date.setText(date);
        holder.payment_amount.setText("$" + String.format("%.2f", amount));
        if (amount < 0.0) {  // negative amount, means the user owed somebody
            holder.payment_amount.setTextColor(Color.RED);
        } else {
            holder.payment_amount.setTextColor(Color.GREEN);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new SettlePaymentFragment();
                Bundle bundle = new Bundle();
                bundle.putString("NAME", name);
                bundle.putString("NUMBER", number);
                bundle.putString("DATE", date);
                bundle.putDouble("TOTAL_AMOUNT", amount);
                bundle.putString("PAYMENT_STATUS", p.getmStatus());
                bundle.putString("RECEIPT_IDS", receiptIDs);
                fragment.setArguments(bundle);
                goToPaymentPage(fragment);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mPayments.size();
    }


    public class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView payment_name;
        TextView payment_date;
        TextView payment_amount;

        public PaymentViewHolder (View itemView) {
            super(itemView);
            payment_name = itemView.findViewById(R.id.payment_name);
            payment_date = itemView.findViewById(R.id.payment_date);
            payment_amount = itemView.findViewById(R.id.payment_amount);
        }
    }

    private void goToPaymentPage(Fragment fragment) {
        FragmentManager fm = ((FragmentActivity) mContext).getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        // previous state will be added to the backstack, allowing you to go back with the back button.
        // must be done before commit.
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
