package com.example.user.payme.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.user.payme.ChooseContactActivity;
import com.example.user.payme.Objects.Contact;
import com.example.user.payme.R;
import com.example.user.payme.ShowActivity;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> implements Filterable {
    private static final String TAG = "ContactAdapter";
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

        TextView name = (TextView) listItem.findViewById(R.id.textView_name);
        name.setText(contact.getmName());

        TextView number = (TextView) listItem.findViewById(R.id.textView_contact);
        number.setText(contact.getmPhoneNumber());

        // TODO pass the correct imagepath to ShowActivity
        // TODO check mContext if is it equals to ChooseContactActivity
        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onItemClick: item: " + name);
                if (mContext instanceof ChooseContactActivity) {
                    Intent intent = new Intent(mContext, ShowActivity.class);
                    intent.putExtra("Contact", contact);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }
        });

        return listItem;

    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<Contact> FilteredContacts = new ArrayList<>();

                // perform your search here using the searchConstraint String.
                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < contactsList.size(); i++) {
                    Contact contact = contactsList.get(i);
                    if (contact.getmName().toLowerCase().startsWith(constraint.toString()))  {
                        FilteredContacts.add(contact);
                    }
                }

                results.count = FilteredContacts.size();
                results.values = FilteredContacts;
                //Log.e("VALUES", results.values.toString());

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count == 0) {
                    notifyDataSetInvalidated();
                } else {
                    contactsList = (ArrayList<Contact>) results.values;
                    notifyDataSetChanged();
                }
            }
        };

        return filter;
    }

    @Override
    public int getCount() {
        return contactsList.size();
    }

}
