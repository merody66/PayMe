package com.example.user.payme.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.user.payme.Objects.Receipt;
import com.example.user.payme.R;

import java.util.ArrayList;

public class ReceiptRecyclerViewAdapter extends RecyclerView.Adapter<ReceiptRecyclerViewAdapter.ReceiptViewHolder> {

    private static final String TAG = "ReceiptRecyclerViewAdapter";
    private Context mContext;
    private ArrayList<Receipt> mReceipts;

    public ReceiptRecyclerViewAdapter(Context mContext, ArrayList<Receipt> mReceipts) {
        this.mContext = mContext;
        this.mReceipts = mReceipts;
    }


    @NonNull
    @Override
    public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_receipt_item, parent, false);
        return new ReceiptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptViewHolder holder, int position) {
        Receipt receipt = mReceipts.get(position);

        String shopName = receipt.getmShopname();
        String subtotalAmt = receipt.getmSubtotalAmt();
        String receiptDate = receipt.getmDate();
        System.out.println(receiptDate + " --> " + shopName);

        holder.receipt_name.setText(shopName);
        holder.receipt_subtotal.setText("$" + subtotalAmt);
        holder.receipt_date.setText(receiptDate);
    }

    @Override
    public int getItemCount() {
        return mReceipts.size();
    }


    public class ReceiptViewHolder extends RecyclerView.ViewHolder {
        TextView receipt_name;
        TextView receipt_subtotal;
        TextView receipt_date;

        public ReceiptViewHolder (View itemView) {
            super(itemView);
            receipt_name = itemView.findViewById(R.id.receipt_name);
            receipt_subtotal = itemView.findViewById(R.id.receipt_subtotal);
            receipt_date = itemView.findViewById(R.id.receipt_date);
        }
    }

}
