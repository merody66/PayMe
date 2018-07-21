package com.example.user.payme.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.provider.ContactsContract;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.payme.Adapters.HorizontalRecyclerViewAdapter;
import com.example.user.payme.Adapters.VerticalRecyclerViewAdapter;
import com.example.user.payme.ChooseContactActivity;
import com.example.user.payme.Interfaces.ContactClickListener;
import com.example.user.payme.MainActivity;
import com.example.user.payme.Objects.Contact;
import com.example.user.payme.R;
import com.example.user.payme.ShowActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment implements ContactClickListener{
    private static final String TAG = "ContactsFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    final int PERMISSION_ALL = 1;
    private ArrayList<Contact> contactsList;
    private LinearLayout groupContainer;
    private EditText searchContact;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private FirebaseUser currentUser;
    private String userId;
    Cursor cursor;
    HashMap<String, ArrayList<Contact>> groupList = new HashMap<>();

    private ArrayList<Contact> mContacts = new ArrayList<>();

    // Empty public constructor, required by the system
    public ContactsFragment() { }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        contactsList = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ref = db.getReference();
        currentUser = auth.getCurrentUser();
        userId = currentUser.getUid();
        setHasOptionsMenu(true);

        // Set title bar
        if (getActivity().getClass().equals(MainActivity.class)) {
            ((MainActivity) getActivity())
                    .setActionBarTitle("Contacts");
        } else {
            ((ChooseContactActivity) getActivity()).setActionBarTitle("Choose contacts");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        searchContact = view.findViewById(R.id.searchContact);
        groupContainer = view.findViewById(R.id.groupContainer);

        String[] PERMISSIONS = { Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS };

        if (!hasPermissions(getActivity(), PERMISSIONS)) {  // if permission is not granted
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        } else {  // if granted
            GetContactsIntoArrayList();
            GetGroupList();
        }


        searchContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                // contactAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity() instanceof ChooseContactActivity) {
            inflater.inflate(R.menu.choose_contact_bar, menu);
        } else {
            inflater.inflate(R.menu.contacts_bar, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (getActivity() instanceof ChooseContactActivity) {
            switch (item.getItemId()) {
                case R.id.next:
                    nextShowActvity();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } else {
            switch (item.getItemId()) {
                case R.id.add_friend:
                    addFriend();
                    return true;
                case R.id.add_group:
                    addGroup();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL: {
                Log.d("PERMISSION", "Permission Granted.");
                GetContactsIntoArrayList();
                GetGroupList();
                return;
            }
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void GetContactsIntoArrayList() {

        cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Contact contact = new Contact(0, name, phoneNumber);
            if (!contactsList.contains(contact)) {  // to avoid adding duplicates
                contactsList.add(contact);
            }
        }

        Collections.sort(contactsList, (o1, o2) -> o1.getmName().compareTo(o2.getmName()));
        cursor.close();
    }


    public void GetGroupList() {
        ref.child("users").child(userId).child("groupList")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Loops through every group
                        for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                            ArrayList<Contact> contacts = new ArrayList<>();
                            // Loops through every contact in each group
                            for (DataSnapshot contactsSnapshop : groupSnapshot.getChildren()) {
                                Contact c = contactsSnapshop.getValue(Contact.class);
                                contacts.add(c);
                            }
                            groupList.put(groupSnapshot.getKey(), contacts);
                        }

                        Typeface fontFace = ResourcesCompat.getFont(getContext(), R.font.nunito);
                        Iterator iterator = groupList.entrySet().iterator();
                        while(iterator.hasNext()) {
                            Map.Entry pair = (Map.Entry) iterator.next();
                            String groupName = pair.getKey().toString();
                            ArrayList<Contact> contacts = (ArrayList<Contact>) pair.getValue();
                            LinearLayout layout = new LinearLayout(getContext());  // create a layout for every group
                            layout.setLayoutParams(new LinearLayout.LayoutParams(
                                    // int width, int height
                                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                            ));
                            layout.setPadding(50, 50, 50, 0);
                            layout.setOrientation(LinearLayout.VERTICAL);
                            TextView nameTextView = new TextView(getContext());
                            nameTextView.setText(groupName);
                            nameTextView.setTypeface(fontFace);
                            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayout.HORIZONTAL, false);
                            HorizontalRecyclerViewAdapter adapter = new HorizontalRecyclerViewAdapter(getContext(), contacts);
                            RecyclerView recyclerView = new RecyclerView(getContext());
                            recyclerView.setLayoutParams(new RecyclerView.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(layoutManager);
                            View separator = new View(getContext());
                            separator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,3));
                            separator.setBackgroundColor(Color.BLACK);
                            layout.addView(nameTextView);
                            layout.addView(recyclerView);
                            layout.addView(separator);
                            groupContainer.addView(layout);

                            // On click listener for select group for add receipt
                            layout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getContext(), groupName + " group clicked.", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onClick: group pass it to showactivity "+groupName);
                                    Log.d(TAG, "onClick: list of contacts "+contacts.get(0));
                                    Intent intent = new Intent(getActivity().getBaseContext(), ShowActivity.class);
                                    intent.putExtra("Contacts", contacts);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                }
                            });
                        }
                        LinearLayout layout = new LinearLayout(getContext());
                        layout.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        layout.setPadding(50, 50, 50, 0);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayout.VERTICAL, false);
                        VerticalRecyclerViewAdapter adapter = new VerticalRecyclerViewAdapter(getContext(), contactsList);
                        if (getActivity() instanceof ChooseContactActivity) {
                            adapter = new VerticalRecyclerViewAdapter(getContext(), contactsList, ContactsFragment.this::onContactClick);
                        }

                        RecyclerView recyclerView = new RecyclerView(getContext());
                        recyclerView.setLayoutParams(new RecyclerView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        recyclerView.setAdapter(adapter);
                        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), VERTICAL);
                        recyclerView.addItemDecoration(decoration);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setNestedScrollingEnabled(false);
                        TextView contactsTextView = new TextView(getContext());
                        contactsTextView.setText("Contacts");
                        contactsTextView.setTypeface(fontFace);
                        contactsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
                        layout.addView(contactsTextView);
                        layout.addView(recyclerView);
                        groupContainer.addView(layout);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {  }
                });
    }



    private void nextShowActvity() {
        Log.d(TAG, "onContactClick: pass it to showactivity ");
        Intent intent = new Intent(getActivity().getBaseContext(), ShowActivity.class);
        intent.putExtra("Contacts", mContacts);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void addFriend() {
        Fragment friendFragment = new AddFriendFragment();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, friendFragment);
        // previous state will be added to the backstack, allowing you to go back with the back button.
        // must be done before commit.
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void addGroup() {
        Fragment groupFragment = new AddGroupFragment();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, groupFragment);
        // previous state will be added to the backstack, allowing you to go back with the back button.
        // must be done before commit.
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onContactClick(Contact contact) {
        if (getActivity() instanceof ChooseContactActivity) {
            mContacts.add(contact);
        }
    }
}
