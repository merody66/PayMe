package com.example.user.payme.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.user.payme.Interfaces.ContactClickListener;
import com.example.user.payme.Objects.Contact;
import com.example.user.payme.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class VerticalRecyclerViewAdapter extends RecyclerView.Adapter<VerticalRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "VerticalRecyclerView";
    private ArrayList<Contact> mContacts;
    private Context mContext;
    private ContactClickListener contactClickListener;

    public VerticalRecyclerViewAdapter(Context mContext, ArrayList<Contact> mContacts, ContactClickListener contactClickListener) {
        this.mContext = mContext;
        this.mContacts = mContacts;
        this.contactClickListener = contactClickListener;
    }

    public VerticalRecyclerViewAdapter(Context mContext, ArrayList<Contact> mContacts) {
        this.mContext = mContext;
        this.mContacts = mContacts;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = mContacts.get(position);

        String name = contact.getmName();
        String number = contact.getmPhoneNumber();
        int image = contact.getmImageDrawable();

        holder.name.setText(name);
        holder.contact.setText(number);

        // Here I am just highlighting the background
        holder.cardInnerConstraintLayout.setBackgroundColor(contact.getIsSelected() ? mContext.getResources().getColor(R.color.colourSelected): Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name;
        TextView contact;
        ConstraintLayout cardInnerConstraintLayout;

        public ViewHolder (View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.user_image);
            name = itemView.findViewById(R.id.user_name);
            contact = itemView.findViewById(R.id.user_contact);
            cardInnerConstraintLayout = itemView.findViewById(R.id.cardInnerConstraintLayout);

            if (contactClickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get position
                        int position = getAdapterPosition();

                        Contact selectedContact = mContacts.get(position);
                        // Updating isSelected in contact
                        notifyItemChanged(position);
                        selectedContact.toggleSelected();
                        notifyItemChanged(position);

                        contactClickListener.onContactClick(selectedContact);
                    }
                });
            }
        }
    }
}
