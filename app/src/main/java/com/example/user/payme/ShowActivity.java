package com.example.user.payme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ShowActivity extends AppCompatActivity implements OnImageClickListener {
    private static final String TAG = "ShowActivity";
    private static final int readPermissionID = 103;
    private ArrayList<String[]> menuList = new ArrayList<>();
    private ListView mListView;
    private RecyclerView mRecyclerView;
    private HorizontalRecyclerViewAdapter contactAdapter;
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
    private ArrayList<ReceiptItem> mUpdatedItemList;

    private ReceiptArrayAdapter adapter;
    private Receipt receipt;

    private ArrayList<Contact> mContacts = new ArrayList<>();


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent = new Intent(ShowActivity.this, MainActivity.class);
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(intent);
                    break;
                case R.id.navigation_history:
                    intent.putExtra("startFragment", MainActivity.REQUEST_HISTORY_FRAGMENT);
                    break;
                case R.id.navigation_addNewReceipt:
                    return true;
                case R.id.navigation_contacts:
                    intent.putExtra("startFragment", MainActivity.REQUEST_CONTACTS_FRAGMENT);
                    break;
                case R.id.navigation_account:
                    intent.putExtra("startFragment", MainActivity.REQUEST_ACCOUNT_SETTING_FRAGMENT);
                    break;
            }
            startActivity(intent);
            finish();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

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

        // initialise all basic values
        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
        mDate = sdf.format(new Date());
        tvDate.setText(mDate);

        mSubtotalAmt = "";
        mServiceChargeAmt = "";
        mGstAmt = "";
        mUpdatedItemList = new ArrayList<>();

        // Get contacts from intent
        ArrayList<Contact> contacts = (ArrayList<Contact>) getIntent().getSerializableExtra("Contacts");
        mContacts.addAll(contacts);

        // Check if coming from ChooseContactActivity or EditReceiptActivity
        if (getIntent().getStringExtra("from_activity").equals("ChooseContactActivity")) {
            // TODO retrieve current logged in user
            mContacts.add(0, new Contact(0, "You", "99999999"));
            resetContactSelected();

            // Data taken from shared preferences
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            mEditor = mSharedPreferences.edit();
            String imagePath = mSharedPreferences.getString("imagePath", "default nothing");
            Log.d(TAG, "onCreate: from sp "+imagePath);
            myBitmap = new Image(this, imagePath).getmBitmap();

            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(ShowActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        readPermissionID);
                return;
            }

            initRecyclerView();

            Log.d(TAG, "onCreate: started ProcessOCR");
            new ProcessOcrAsync().execute();
        } else {
            Log.d(TAG, "onCreate: CAME BACK FROM EDITRECEIPT");
            resetContactSelected();
            receipt = (Receipt) getIntent().getSerializableExtra("receipt");
            mShopname = receipt.getmShopname();
            mSubtotalAmt = receipt.getmSubtotalAmt();
            mServiceChargeAmt = receipt.getmServiceChargeAmt();
            mGstAmt = receipt.getmGstAmt();
            mDate = receipt.getmDate();
            mUpdatedItemList = receipt.getmItemList();

            runOnUiThread(new Runnable() {
                public void run() {
                    initReceipt();
                    initRecyclerView();

                }
            });


        }
    }

    private void resetContactSelected(){
        for (int i = 1; i < mContacts.size(); i++) {
            mContacts.get(i).setSelected(false);
        }
        mContacts.get(0).setSelected(true);
    }

    // todo can do the same for receiptadapter
    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: started");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);
        contactAdapter = new HorizontalRecyclerViewAdapter(this, mContacts, this);
        mRecyclerView.setAdapter(contactAdapter);
    }

    private void initReceipt() {
        tvShopname.setText(mShopname);
        tvDate.setText(mDate);
        tvSubtotalAmt.setText(mSubtotalAmt);
        tvServiceChargeAmt.setText(mServiceChargeAmt);
        tvGstAmt.setText(mGstAmt);

        adapter = new ReceiptArrayAdapter(this, mUpdatedItemList);

        // Set user as the first user.
        adapter.setCurrentChooseUser("You");
        mListView.setAdapter(adapter);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.show_activity_bar, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_redirect_button:
                sendUserToEdit();
            case R.id.action_back:
                // User chose the "Settings" item, show the app settings UI...
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void sendUserToEdit(){
        Intent intent = new Intent(ShowActivity.this, EditReceiptActivity.class);
        intent.putExtra("shopname", mShopname);
        intent.putExtra("date", mDate);
        intent.putExtra("itemList", mUpdatedItemList);
        intent.putExtra("Contacts", mContacts);
        startActivity(intent);
        finish();
    }

    private ArrayList<Text> findPrice(SparseArray<TextBlock> items) {
        ArrayList<Text> priceArray = new ArrayList<>();
        Pattern datePattern = Pattern.compile("(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\\d\\d");

        int leastTop = Integer.MAX_VALUE;

        for (int i = 0; i < items.size(); i++) {
            TextBlock item = items.valueAt(i);
            List<? extends Text> textComponents = item.getComponents();
            for (Text currentText : textComponents) {
                Log.d(TAG, "run: item "+ currentText.getValue());
                Matcher dateMatcher = datePattern.matcher(currentText.getValue());
                int currentTop = currentText.getBoundingBox().top;

                // Find shopname which is located at the top row
                if (currentTop < leastTop) {
                    leastTop = currentTop;
                    mShopname = currentText.getValue();
                }

                // Find and add all price format [Text]
                if (currentText.getValue().matches("\\$?[o0-9]{1,2}[.,]+[o0-9]{1,2}")) {
                    Log.d(TAG, "run: found "+currentText.getValue());
                    priceArray.add(currentText);
                } else if (dateMatcher.find()) {
                    mDate = dateMatcher.group(0);
                }
            }
        }

        return priceArray;
    }

    private ArrayList<ReceiptItem> findLeftItem(SparseArray<TextBlock> items, ArrayList<Text> priceArray) {
        ArrayList<ReceiptItem> mItemList = new ArrayList<>();

        String[] menuArray = new String[2];
        ReceiptItem receiptItem;

        ArrayList<String> foundItem = new ArrayList<>();
        for (int y = 0; y < priceArray.size(); y++) {
            Text priceItem = priceArray.get(y);
            float priceLeft = priceItem.getBoundingBox().left;
            float priceBottom = priceItem.getBoundingBox().bottom;

//                    Log.d(TAG, "run: price "+ priceItem.getValue()+" bounding box: "+priceItem.getBoundingBox().toString());
            for (int i = 0; i < items.size(); i++) {
                TextBlock item = items.valueAt(i);
                List<? extends Text> textComponents = item.getComponents();

                for (Text currentText : textComponents) {
                    float itemLeft = currentText.getBoundingBox().left;
                    float itemBottom = currentText.getBoundingBox().bottom;

//                    Log.d(TAG, "run: item "+ currentText.getValue()+" bounding box "+ currentText.getBoundingBox().toString());
                    if (!currentText.getValue().matches("\\$?[o0-9]{1,2}[.,]+[o0-9]{1,2}") && (priceLeft - itemLeft) > 0) { //skip all price item and those in roughly around the same column
                        if (priceBottom / 1.035 <= itemBottom && itemBottom <= priceBottom * 1.035) { // priceBottom <= itemBottom <= priceBottom*1.035
                            if (!foundItem.contains(currentText.getValue())) { // Skip those item that have already been found before
                                Log.d(TAG, "run again: " + currentText.getValue() + "     " + priceItem.getValue() + "\n");
                                menuArray[0] = currentText.getValue();
                                menuArray[1] = priceItem.getValue();
                                menuList.add(menuArray);
                                menuArray = new String[2];

                                receiptItem = new ReceiptItem(currentText.getValue(), priceItem.getValue());
                                mItemList.add(receiptItem);
                                foundItem.add(currentText.getValue());
                                break;
                            }
                        }
                    }

                }
            }
        }

        return mItemList;
    }

    private void ProcessOCR () {
        Log.d(TAG, "ProcessOCR: within");
        
        Context context = getApplicationContext();
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

        // Check if there is item
        // todo else what happen
        if (items.size() != 0) {
            // get the first line in the item.
            Log.d(TAG, "ProcessOCR: finding xx.xx");
            priceArray = findPrice(items);

            Log.d(TAG, "ProcessOCR: finding matching item on the left");
            ArrayList<ReceiptItem> mItemList = findLeftItem(items, priceArray);

            Log.d(TAG, "ProcessOCR: finding those 3 values");
            mUpdatedItemList = new ArrayList<>();
            mUpdatedItemList.addAll(mItemList);
            boolean foundBreak = false;

            // Combined pattern to search for subtotel, gst and service charge.
            Pattern breakPattern = Pattern.compile("(sub ?to?ta?l)|((GST).*)|(service ?charge.*)", Pattern.CASE_INSENSITIVE);
            double subtotal = 0;

            for (int i = 0; i < mItemList.size(); i++) {
                String mName = mItemList.get(i).getmName();

                if (breakPattern.matcher(mName).find()) {
                    foundBreak = true;
                }

                if (!foundBreak) {
                    String price = mItemList.get(i).getmPrice();
                    price = price.replace("$", "");
                    price = price.replace(",", "");

                    try {
                        subtotal += Double.parseDouble(price);
                    } catch (NumberFormatException e ){
                        Log.d(TAG, "ProcessOCR: WRONG NUMBER FORMAT");
                    }
                } else {
                    mUpdatedItemList.remove(mItemList.get(i));
                }
            }

            Log.d(TAG, "ProcessOCR: set all the information and adapter");

            // To fit more receipts with any arrangement, we are doing the calculation
            mSubtotalAmt = String.format(Locale.ENGLISH, "%.2f", subtotal);
            mServiceChargeAmt = String.format(Locale.ENGLISH, "%.2f", subtotal * 0.1);
            mGstAmt = String.format(Locale.ENGLISH, "%.2f", subtotal * 0.07);

            receipt = new Receipt(mShopname, mDate, mGstAmt, mServiceChargeAmt, mSubtotalAmt, mUpdatedItemList);
        }

        
    }

    private class ProcessOcrAsync extends AsyncTask<Bitmap , Void, String> {

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            Log.d(TAG, "doInBackground: START ASYNCTASK");
            ProcessOCR();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (mUpdatedItemList.isEmpty()){
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowActivity.this);
                builder.setMessage("Unable to detect any item/price text, please enter your own item details")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                sendUserToEdit();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            } else {
                initReceipt();

                Log.d(TAG, "ProcessOCR: after setadapter");
            }
        }
    }

    @Override
    public void onImageClick(String name) {
        boolean isUserSelected;
        View userView;
        for (int i = 0; i < contactAdapter.getItemCount(); i++) {
            userView = mRecyclerView.getChildAt(i);
            isUserSelected = contactAdapter.isUserSelected(i);
            contactAdapter.setSelectedStyle(userView, isUserSelected);
        }

        adapter.setCurrentChooseUser(name);
        boolean isChildEnabled;
        View childView;

        for (int i = 0; i < mListView.getChildCount(); i++) {
            childView = mListView.getChildAt(i);
            isChildEnabled = adapter.isItemEnabled(i);
            childView.setEnabled(isChildEnabled);
            adapter.setSelectedStyle(childView, isChildEnabled);
        }
    }

    @Override
    public ArrayList<String> getItemIsEnabled() {
        return null;
    }
}
