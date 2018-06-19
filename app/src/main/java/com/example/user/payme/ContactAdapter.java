package com.example.user.payme;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.payme.Objects.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private Context mContext;
    private List<Contact> contactsList = new ArrayList<>();

    public ContactAdapter(Context context, ArrayList<Contact> list) {
        super(context, 0, list);
        this.mContext = context;
        this.contactsList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);


        Contact contact = contactsList.get(position);

        //ImageView image = (ImageView) listItem.findViewById(R.id.profile_image);
        //image.setImageResource(contact.getmImageDrawable());

        TextView name = (TextView) listItem.findViewById(R.id.textView_name);
        name.setText(contact.getmName());

        TextView number = (TextView) listItem.findViewById(R.id.textView_contact);
        number.setText(contact.getmPhoneNumber());

        return listItem;

    }
}
