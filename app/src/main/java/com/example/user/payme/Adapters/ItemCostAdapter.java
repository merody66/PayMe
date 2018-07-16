package com.example.user.payme.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.user.payme.Objects.ReceiptItem;
import com.example.user.payme.R;

import java.util.ArrayList;

public class ItemCostAdapter extends RecyclerView.Adapter<ItemCostAdapter.ViewHolder>{
    private static final String TAG = "ItemCostAdapter";
    private Context mContext;
    private ArrayList<ReceiptItem> receiptItems;

    public ItemCostAdapter(Context mContext, ArrayList<ReceiptItem> receiptItems) {
        this.mContext = mContext;
        this.receiptItems = receiptItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_listitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReceiptItem receiptItem = receiptItems.get(position);
        String item = receiptItem.getmName();
        String price = receiptItem.getmPrice();

        holder.item.setText(item);
        holder.price.setText(price);
    }

    @Override
    public int getItemCount() {
        return receiptItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView item;
        TextView price;
        public ViewHolder(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item);
            price = itemView.findViewById(R.id.price);
        }
    }
}
