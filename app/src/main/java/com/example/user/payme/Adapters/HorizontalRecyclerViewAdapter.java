package com.example.user.payme.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.payme.Interfaces.OnImageClickListener;
import com.example.user.payme.Objects.Contact;
import com.example.user.payme.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HorizontalRecyclerViewAdapter extends RecyclerView.Adapter<HorizontalRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "HorizontalRecyclerViewA";
    private ArrayList<Contact> mContacts;
    private Context mContext;
    private OnImageClickListener onImageClickListener;

    public HorizontalRecyclerViewAdapter(Context mContext, ArrayList<Contact> mContacts, OnImageClickListener onImageClickListener) {
        this.mContext = mContext;
        this.mContacts = mContacts;
        this.onImageClickListener = onImageClickListener;
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
        Log.d(TAG, "onBindViewHolder: called.");
        Contact contact = mContacts.get(position);
        String name = contact.getmName();
        // need to check how to use this
        int image = contact.getmImageDrawable();
        Log.d(TAG, "onBindViewHolder: contact image "+image);
        holder.name.setText(name);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on image "+name);
                onImageClickListener.onImageClick(name);
                Toast.makeText(mContext, name, Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name;

        public ViewHolder (View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.user_image);
            name = itemView.findViewById(R.id.user_text);
        }
    }
}
