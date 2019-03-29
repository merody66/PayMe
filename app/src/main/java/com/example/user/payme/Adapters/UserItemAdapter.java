package com.example.user.payme.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.user.payme.Objects.UserItem;
import com.example.user.payme.Objects.Contact;
import com.example.user.payme.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserItemAdapter extends RecyclerView.Adapter<UserItemAdapter.ViewHolder> {
    private static final String TAG = "UserItemAdapter";
    private ArrayList<UserItem> mUserItems;
    private Context mContext;

    public UserItemAdapter(Context mContext, ArrayList<UserItem> mUserItems) {
        this.mContext = mContext;
        this.mUserItems = mUserItems;

        //todo delete this
        // ensure all is saving alright
        for (UserItem card : mUserItems) {
            Log.d(TAG, ": card "+card.getmContact().getmName());
//            for (ReceiptItem items : card.getReceiptItems()) {
//                Log.d(TAG, items.getmName()+ "  "+ items.getmPrice());
//            }

            Log.d(TAG, "total "+card.getTotal());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called.");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_listusertotal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        UserItem userItem = mUserItems.get(position);
        Contact contact = userItem.getmContact();
        String total = userItem.getTotal();

        Log.d(TAG, "onBindViewHolder: name "+contact.getmName());

        holder.userText.setText(contact.getmName());
        holder.totalAmt.setText(total);
    }

    @Override
    public int getItemCount() {
        return mUserItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userImage;
        TextView userText;
        TextView totalAmt;
        public ViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.user_image);
            userText = itemView.findViewById(R.id.user_text);
            totalAmt = itemView.findViewById(R.id.totalAmt);
        }
    }
}
