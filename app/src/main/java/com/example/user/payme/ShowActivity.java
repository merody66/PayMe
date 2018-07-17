package com.example.user.payme;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.payme.Adapters.HorizontalRecyclerViewAdapter;
import com.example.user.payme.Adapters.ReceiptArrayAdapter;
import com.example.user.payme.Interfaces.OnImageClickListener;
import com.example.user.payme.Objects.Contact;
import com.example.user.payme.Objects.Image;
import com.example.user.payme.Objects.Receipt;
import com.example.user.payme.Objects.ReceiptItem;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ShowActivity extends AppCompatActivity implements OnImageClickListener {
    private static final String TAG = "ShowActivity";
    private static final int readPermissionID = 103;
    private ArrayList<String[]> menuList = new ArrayList<>();
    private ListView mListView;
    private TextView tvShopname;
    private TextView tvDate;
    private TextView tvGstAmt;
    private TextView tvServiceChargeAmt;
    private TextView tvSubtotalAmt;
    private Button mDone_button;
    private String mShopname;
    private String mDate;
    private String mGstAmt;
    private String mServiceChargeAmt;
    private String mSubtotalAmt;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private Bitmap myBitmap;
    private ArrayList<ReceiptItem> updatedItemList;

    private ReceiptArrayAdapter adapter;
    private Receipt receipt;

    // todo change to listview or something
//    private CircleImageView profile_image_contact1;
//    private TextView mContact1Text;

    private ArrayList<Contact> mContacts = new ArrayList<>();


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(ShowActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_history:
                    return true;
                case R.id.navigation_addNewReceipt:
                    return true;
                case R.id.navigation_contacts:
                    return true;
                case R.id.navigation_account:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        // TODO redo the toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarTop);
        setSupportActionBar(myToolbar);
        myToolbar.setTitleTextColor(Color.WHITE);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_addNewReceipt);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Log.d(TAG, "onCreate: Started.");
        mListView = (ListView) findViewById(R.id.listView);
        tvShopname = (TextView) findViewById(R.id.shopname);
        tvDate = (TextView) findViewById(R.id.date);
        tvGstAmt = (TextView) findViewById(R.id.gstAmt);
        tvServiceChargeAmt = (TextView) findViewById(R.id.serviceChargeAmt);
        tvSubtotalAmt = (TextView) findViewById(R.id.subtotalAmt);
        mDone_button = (Button) findViewById(R.id.done_button);

        // Data taken from previous ChooseContactActivity
        Contact mContact = (Contact) getIntent().getSerializableExtra("Contact");
        // TODO retrieve current logged in user
        mContacts.add(new Contact(0, "You", "99999999"));
//        mContact.setmName("Hui Chun");
        mContacts.add(mContact);

        // Data taken from shared preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();
        String imagePath = mSharedPreferences.getString("imagePath", "default nothing");
        Log.d(TAG, "onCreate: from sp "+imagePath);
        Context mContext = getApplicationContext();
        myBitmap = new Image(mContext, imagePath).getmBitmap();


        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ShowActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    readPermissionID);
            return;
        }

        Log.d(TAG, "onCreate: started ProcessOCR");
        ProcessOCR();

    }

    // todo can do the same for receiptadapter
    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: started");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        HorizontalRecyclerViewAdapter adapter = new HorizontalRecyclerViewAdapter(this, mContacts, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.back_appbar, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private ArrayList<Text> findPrice(SparseArray<TextBlock> items) {
        Log.d(TAG, "ProcessOCR: finding xx.xx");
        ArrayList<Text> priceArray = new ArrayList<>();
        Pattern datePattern = Pattern.compile("(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\\d\\d");

        runOnUiThread(new Runnable() {
            public void run() {
                for (int i = 0; i < items.size(); i++) {
                    TextBlock item = items.valueAt(i);
//                Log.d(TAG, "run: individual item " + item.getValue());

                    List<? extends Text> textComponents = item.getComponents();
                    for (Text currentText : textComponents) {
                        Matcher dateMatcher = datePattern.matcher(currentText.getValue());

                        // Find and add all price format [Text]
                        if (currentText.getValue().matches("[o0-9]{1,2}.[o0-9]{2}")) {
                        Log.d(TAG, "ProcessOCR: found a price match "+currentText.getValue());
                            priceArray.add(currentText);
                        } else if (dateMatcher.find()) {
//                        Log.d(TAG, "run: time " + currentText.getValue());
                            mDate = dateMatcher.group(0);
                        }
                    }
                }
            }
        });

        return priceArray;
    }

    private ArrayList<ReceiptItem> findLeftItem(SparseArray<TextBlock> items, ArrayList<Text> priceArray) {
        ArrayList<ReceiptItem> mItemList = new ArrayList<>();

        runOnUiThread(new Runnable() {
                public void run() {
                String[] menuArray = new String[2];
                ReceiptItem receiptItem;
                for (int y = 0; y < priceArray.size(); y++) {
                    Text priceItem = priceArray.get(y);
                    float priceLeft = priceItem.getBoundingBox().left;
                    float priceBottom = priceItem.getBoundingBox().bottom;

                    for (int i = 0; i < items.size(); i++) {
                        TextBlock item = items.valueAt(i);
                        List<? extends Text> textComponents = item.getComponents();

                        for (Text currentText : textComponents) {
                            float itemLeft = currentText.getBoundingBox().left;
                            float itemBottom = currentText.getBoundingBox().bottom;


                            if (priceItem != currentText && (priceLeft - itemLeft) > 0) { //skip itself and those in roughly around the same column
                                if (priceBottom <= itemBottom && itemBottom <= priceBottom * 1.035) { // priceBottom <= itemBottom <= priceBottom*1.035
                                    Log.d(TAG, "run again: " + currentText.getValue() + "     " + priceItem.getValue() + "\n");
                                    menuArray[0] = currentText.getValue();
                                    menuArray[1] = priceItem.getValue();
                                    menuList.add(menuArray);
                                    menuArray = new String[2];

                                    receiptItem = new ReceiptItem(currentText.getValue(), priceItem.getValue());
                                    mItemList.add(receiptItem);
                                    break;
                                }
                            }

                        }
                    }
                }
            }
        });
        return mItemList;
    }

    private void ProcessOCR () {
        Log.d(TAG, "ProcessOCR: within");
        
        Context context = getApplicationContext();
        // TODO: REMOVE SAMPLE IMAGE LATER
//        String imagePath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/PayMe/sample_bakkutteh.jpg";
//        Bitmap myBitmap = rotateImage(imagePath);
        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        // Create the TextRecognizer
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        // Check if the TextRecognizer is operational.
        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, "Error: low storage", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Error: low storage");
            }
        }

        Log.d(TAG, "ProcessOCR: start detction");
        SparseArray<TextBlock> items = textRecognizer.detect(frame);
        ArrayList<Text> priceArray;
        final Text[] resturantText = new Text[1];
        String dateText = "";

        // Check if there is item
        // todo else what happen
        if (items.size() != 0) {
            // get the first line in the item.
            resturantText[0] = items.valueAt(0).getComponents().get(0);

            Log.d(TAG, "ProcessOCR: finding xx.xx");
            priceArray = findPrice(items);

            Log.d(TAG, "ProcessOCR: finding matching item on the left");
            ArrayList<ReceiptItem> mItemList = findLeftItem(items, priceArray);

            Log.d(TAG, "ProcessOCR: finding those 3 values");
            updatedItemList = new ArrayList<>();
            boolean foundGST = false;
            Pattern gstPattern = Pattern.compile("(GST).+", Pattern.CASE_INSENSITIVE);
            Pattern serviceChargePattern = Pattern.compile("(service charge).+", Pattern.CASE_INSENSITIVE);

            for (int i = 0; i < mItemList.size(); i++) {
                String mName = mItemList.get(i).getmName();

                // TODO to a non hardcoded way
                switch (mName) {
                    case "SUBTTL":
                        mSubtotalAmt = mItemList.get(i).getmPrice();
                        tvSubtotalAmt.setText(mSubtotalAmt);
                        continue;
                }

                if (gstPattern.matcher(mName).find()) {
                    foundGST = true;
                    mGstAmt = mItemList.get(i).getmPrice();
                    tvGstAmt.setText(mGstAmt);
                    continue;
                } else if (serviceChargePattern.matcher(mName).find()) {
                    mServiceChargeAmt = mItemList.get(i).getmPrice();
                    tvServiceChargeAmt.setText(mServiceChargeAmt);
                    continue;
                }

                if (!foundGST ) {
                    updatedItemList.add(mItemList.get(i));
                }
            }

            Log.d(TAG, "ProcessOCR: set all the information and adapter");
            mShopname = resturantText[0].getValue().replaceAll("\\s+","").toUpperCase();
            tvShopname.setText(mShopname);
            mDate = dateText;
            tvDate.setText(mDate);

            // TODO REMOVE HARDCODED VALUE
            mShopname = "YA HUA BAK KUT TEH";
            tvShopname.setText(mShopname);
            mDate = "17/05/2018";
            tvDate.setText(mDate);
            mSubtotalAmt = "33.40";
            tvSubtotalAmt.setText(mSubtotalAmt);
            mServiceChargeAmt = "3.34";
            tvServiceChargeAmt.setText(mServiceChargeAmt);
            mGstAmt = "2.57";
            tvGstAmt.setText(mGstAmt);
            updatedItemList = new ArrayList<>();
            updatedItemList.add(new ReceiptItem("3 RICE", "2.40"));
            updatedItemList.add(new ReceiptItem("1 CHINESE TEA (GLASS)", "1.80"));
            updatedItemList.add(new ReceiptItem("1 COKE", "1.80"));
            updatedItemList.add(new ReceiptItem("1 PRIME CUT RIBS", "11.50"));
            updatedItemList.add(new ReceiptItem("1 RIBS", "8.50"));
            updatedItemList.add(new ReceiptItem("2 WET TOWEL", "0.60"));
            updatedItemList.add(new ReceiptItem("1 FRAGRANT FRIED CHICKEN WI", "6.80"));


            receipt = new Receipt(mShopname, mDate, mGstAmt, mServiceChargeAmt, mSubtotalAmt, updatedItemList);

            initRecyclerView();

            adapter = new ReceiptArrayAdapter(this, updatedItemList);
            // Set user as the first user.
            adapter.setCurrentChooseUser("You");
            mListView.setAdapter(adapter);
            Log.d(TAG, "ProcessOCR: after setadapter");

            mDone_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ShowActivity.this, RequestPaymentActivity.class);
                    HashMap<String, ArrayList<ReceiptItem>> result = adapter.calculateAmount();
                    intent.putExtra("Contact", mContacts);
                    intent.putExtra("result", result);
                    intent.putExtra("receiptItemList", adapter.getReceiptItems());
                    intent.putExtra("receipt",receipt);
                    startActivity(intent);
                }
            });
        } else {

        }
    }

    @Override
    public void onImageClick(String name) {
        Log.d(TAG, "onImageClick: inside "+name);
        adapter.setCurrentChooseUser(name);
//        mListView.getChildAt(0).setEnabled(false);
        boolean isChildEnabled;
        View childView;
        for (int i = 0; i < updatedItemList.size(); i++) {
            childView = mListView.getChildAt(i);
            isChildEnabled = adapter.isItemEnabled(i);
            childView.setEnabled(isChildEnabled);
            adapter.setSelectedStyle(childView, isChildEnabled);
            Log.d(TAG, "onImageClick: ischildenabled "+isChildEnabled);
        }
    }

    @Override
    public ArrayList<String> getItemIsEnabled() {
        return null;
    }
}
