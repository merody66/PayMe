package com.example.user.payme.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.user.payme.Objects.ReceiptItem;
import com.example.user.payme.R;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.util.stream.Collectors.groupingBy;

public class ReceiptArrayAdapter extends ArrayAdapter<ReceiptItem> {
    private static final String TAG = "ReceiptArrayAdapter";
    private final Context context;
    private final ArrayList<ReceiptItem> receiptItems;
    private int lastPosition = -1;
    private String currentChooseUser;

    static class ViewHolder {
        TextView name;
        TextView price;
    }

    public ReceiptArrayAdapter(Context context, ArrayList<ReceiptItem> receiptItems) {
        super(context, R.layout.list_receipt_item, receiptItems);
        this.context = context;
        this.receiptItems = receiptItems;
//        this.getItemIsEnabledListener = getItemIsEnabledListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get OCR text list
        ReceiptItem textArray = receiptItems.get(position);
        String name = textArray.getmName();
        String price = textArray.getmPrice();
        Boolean isShared = textArray.getmIsShared();

        // create the view result for showing the animation
        final View result;

//        ViewHolder holder;
//
//        if (convertView == null) {
//            LayoutInflater inflater = (LayoutInflater) context
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = inflater.inflate(R.layout.list_receipt_item, parent, false);
//            holder = new ViewHolder();
//            holder.name = (TextView) convertView.findViewById(R.id.nameView);
//            holder.price = (TextView) convertView.findViewById(R.id.priceView);
//
//            result = convertView;
//
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//            result = convertView;
//        }
//
//        Animation animation = AnimationUtils.loadAnimation(context,
//                (position > lastPosition ) ? R.anim.load_down_anim : R.anim.load_up_anim);
//        result.startAnimation(animation);
//        lastPosition = position;
//
//        holder.name.setText(name);
//        holder.price.setText(price);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(R.layout.list_receipt_item, parent, false);
        TextView foodView = (TextView) convertView.findViewById(R.id.nameView);
        TextView priceView = (TextView) convertView.findViewById(R.id.priceView);
        CircleImageView sharedButton = (CircleImageView) convertView.findViewById(R.id.sharedButton);

        foodView.setText(name);
        priceView.setText(String.valueOf(price));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // toggle image
                boolean updatedIsShared = textArray.toggleIsShared();
                if (updatedIsShared) {
                    textArray.setmBelongsTo(null);
                    sharedButton.setImageResource(R.mipmap.shared_button);
                } else {
                    textArray.setmBelongsTo(currentChooseUser);
                    sharedButton.setImageResource(R.mipmap.tick_button);
                }

            }
        });

        return convertView;
    }

    public void setCurrentChooseUser (String name) {
        this.currentChooseUser = name;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    // Overriding isEnabled will have an unwanted side effect of removing the divider between items.
    // Hence renaming method to isItemEnabled
    public boolean isItemEnabled(int position) {
        String name = receiptItems.get(position).getmBelongsTo();
        // Clickable only if no one have chose it before or it belongs to the user.
        return name == null || name == currentChooseUser;
    }

    public void setSelectedStyle(View convertView, boolean isEnabled) {
        TextView foodView = (TextView) convertView.findViewById(R.id.nameView);
        TextView priceView = (TextView) convertView.findViewById(R.id.priceView);
        CircleImageView sharedButton = (CircleImageView) convertView.findViewById(R.id.sharedButton);
        float opacity = isEnabled ? 1f : 0.5f;

        foodView.setAlpha(opacity);
        priceView.setAlpha(opacity);
        sharedButton.setAlpha(opacity);
    }

    private ArrayList<ReceiptItem> myGetOrDefault(HashMap<String, ArrayList<ReceiptItem>> map, String key, ArrayList<ReceiptItem> defaultValue) {
        Object value = map.get(key);
        return (value == null) ? defaultValue : (ArrayList<ReceiptItem>) value;
    }

    /***
     *
     * @return HashMap<String, ArrayList<ReceiptItem>>
     *     String refers to the name of the user
     *     It contains a list of item belongs to the user
     */
    public HashMap<String, ArrayList<ReceiptItem>> calculateAmount() {
        HashMap<String, ArrayList<ReceiptItem>> result = new HashMap<>();
        ArrayList<ReceiptItem> userReceiptItems = new ArrayList<>();
        for (ReceiptItem item : receiptItems) {
            String name = item.getmBelongsTo();
            userReceiptItems = myGetOrDefault(result, name, new ArrayList<>());
            userReceiptItems.add(item);
            result.put(name, userReceiptItems);
        }

        Log.d(TAG, "calculateAmount: result "+result);

        return result;
    }
}
