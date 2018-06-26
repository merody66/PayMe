package com.example.user.payme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ReceiptArrayAdapter extends ArrayAdapter<String[]> {
    private final Context context;
    private final ArrayList<String[]> textList;
    private int lastPosition = -1;

    static class ViewHolder {
        TextView food;
        TextView price;
    }


    public ReceiptArrayAdapter(Context context, ArrayList<String[]> textList) {
        super(context, R.layout.list_receipt_item, textList);
        this.context = context;
        this.textList = textList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get OCR text list
        String[] textArray = textList.get(position);
        String food = textArray[0];
        String price = textArray[1];

        // create the view result for showing the animation
        final View result;

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_receipt_item, parent, false);
            holder = new ViewHolder();
            holder.food = (TextView) convertView.findViewById(R.id.foodView);
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

        holder.food.setText(food);
        holder.price.setText(price);

        return convertView;
    }
}
