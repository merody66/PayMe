package com.example.user.payme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.user.payme.Objects.ReceiptItem;

import java.util.ArrayList;

public class ReceiptArrayAdapter extends ArrayAdapter<ReceiptItem> {
    private final Context context;
    private final ArrayList<ReceiptItem> receiptItems;
    private int lastPosition = -1;

    static class ViewHolder {
        TextView name;
        TextView price;
    }


    public ReceiptArrayAdapter(Context context, ArrayList<ReceiptItem> receiptItems) {
        super(context, R.layout.list_receipt_item, receiptItems);
        this.context = context;
        this.receiptItems = receiptItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get OCR text list
        ReceiptItem textArray = receiptItems.get(position);
        String name = textArray.getmName();
        String price = textArray.getmPrice();

        // create the view result for showing the animation
        final View result;

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_receipt_item, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.nameView);
            holder.price = (TextView) convertView.findViewById(R.id.priceView);

            result = convertView;

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition ) ? R.anim.load_down_anim : R.anim.load_up_anim);
        result.startAnimation(animation);
        lastPosition = position;

        holder.name.setText(name);
        holder.price.setText(price);

        return convertView;
    }
}
