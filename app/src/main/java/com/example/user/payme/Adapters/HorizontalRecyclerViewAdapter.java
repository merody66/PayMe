package com.example.user.payme.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

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

    public HorizontalRecyclerViewAdapter(Context mContext, ArrayList<Contact> mContacts) {
        this.mContext = mContext;
        this.mContacts = mContacts;
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
        Contact contact = mContacts.get(position);
        String name = contact.getmName();
        // need to check how to use this
        int image = contact.getmImageDrawable();

        holder.imageBoarder.setBackground(contact.getIsSelected() ?
                ContextCompat.getDrawable(mContext, R.drawable.background_selected) :
                ContextCompat.getDrawable(mContext, R.drawable.background_unselected));

        holder.name.setText(name);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on image "+ name);
                if (onImageClickListener != null) {
                    contact.setSelected(true);
                    for (int i = 0; i < mContacts.size(); i++) {
                        if (i != position) {
                            mContacts.get(i).setSelected(false);
                        }
                    }

                    onImageClickListener.onImageClick(name);
                }
            }
        });
    }

    public boolean isUserSelected(int position) {
        return mContacts.get(position).getIsSelected();
    }

    public void setSelectedStyle(View convertView, boolean isSelected) {
        FrameLayout imageBoarder = convertView.findViewById(R.id.user_image_boarder);

        Drawable boarder = isSelected ?
                ContextCompat.getDrawable(mContext, R.drawable.background_selected) :
                ContextCompat.getDrawable(mContext, R.drawable.background_unselected);

        imageBoarder.setBackground(boarder);
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name;
        FrameLayout imageBoarder;

        public ViewHolder (View itemView) {
            super(itemView);
            imageBoarder = itemView.findViewById(R.id.user_image_boarder);
            image = itemView.findViewById(R.id.user_image);
            name = itemView.findViewById(R.id.user_name);
        }
    }
}
