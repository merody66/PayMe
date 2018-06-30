package com.example.user.payme.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.provider.ContactsContract;
import android.widget.EditText;
import android.widget.ListView;

import com.example.user.payme.Adapters.ContactAdapter;
import com.example.user.payme.MainActivity;
import com.example.user.payme.Objects.Contact;
import com.example.user.payme.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    final int PERMISSION_ALL = 1;
    private ListView listView;
    private ArrayList<Contact> contactsList;
    private ContactAdapter mAdapter;
    private EditText searchContact;
    Cursor cursor;


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

        setHasOptionsMenu(true);

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle("Contacts");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        String[] PERMISSIONS = { Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS };

        if (!hasPermissions(getActivity(), PERMISSIONS)) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        } else {

            listView = view.findViewById(R.id.contactList);
            searchContact = view.findViewById(R.id.searchContact);
            contactsList = new ArrayList<>();

            GetContactsIntoArrayList();
            mAdapter = new ContactAdapter(getActivity(), contactsList);
            listView.setAdapter(mAdapter);

            searchContact.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    // When user changed the Text
                    mAdapter.getFilter().filter(cs);
                }

                @Override
                public void beforeTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                }

                @Override
                public void afterTextChanged(Editable arg0) {
                }
            });
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contacts_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
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
                mAdapter = new ContactAdapter(getActivity(), contactsList);
                listView.setAdapter(mAdapter);

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

    public void GetContactsIntoArrayList(){

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
}
