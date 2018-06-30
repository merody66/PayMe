package com.example.user.payme.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.payme.Adapters.ContactAdapter;
import com.example.user.payme.MainActivity;
import com.example.user.payme.Objects.Contact;
import com.example.user.payme.Objects.User;
import com.example.user.payme.R;
import com.example.user.payme.SignupActivity;
import com.google.android.gms.vision.text.Line;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.grpc.netty.shaded.io.netty.util.internal.SocketUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddGroupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddGroupFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Button addMembersBtn;
    private EditText grpNameTxt;
    private EditText searchContact;
    private TextView membersList;
    private ListView contactsListView;
    private LinearLayout groupListContainer;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;

    private ArrayList<Contact> contactsList;
    private ArrayList<Contact> selectedContacts;
    private ContactAdapter mAdapter;
    Cursor cursor;

    public AddGroupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddGroupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddGroupFragment newInstance(String param1, String param2) {
        AddGroupFragment fragment = new AddGroupFragment();
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

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle("Add Group");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_group, container, false);

        // Initialize widgets
        addMembersBtn = view.findViewById(R.id.addMembersBtn);
        grpNameTxt = view.findViewById(R.id.grpNameTxtField);
        searchContact = view.findViewById(R.id.searchContact);
        membersList = view.findViewById(R.id.membersList);
        contactsListView = view.findViewById(R.id.contactList);
        groupListContainer = view.findViewById(R.id.groupListContainer);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ref = db.getReference();

        contactsList = new ArrayList<>();
        selectedContacts = new ArrayList<>();

        GetContactsIntoArrayList();
        mAdapter = new ContactAdapter(getActivity(), contactsList);
        contactsListView.setAdapter(mAdapter);

        addMembersBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                groupListContainer.setVisibility(View.VISIBLE);
                setHasOptionsMenu(true);
            }
        });

        searchContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                mAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence cs, int arg1, int arg2, int arg3) { }

            @Override
            public void afterTextChanged(Editable arg0) { }
        });

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            String msg = "";

            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                Contact c = (Contact) adapter.getItemAtPosition(position);
                if (!selectedContacts.contains(c)) {
                    selectedContacts.add(c);
                    //System.out.println(c.getmName());
                    msg += c.getmName() + ",";
                }
                membersList.setText(msg);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_group_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        String grpName = grpNameTxt.getText().toString().trim();

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.done_btn:
                addGroup(grpName, selectedContacts);
                FragmentManager manager = getFragmentManager();
                manager.popBackStack();
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

    private void addGroup(String grpName, ArrayList<Contact> contacts) {
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();
        System.out.println(userId);

        ref.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User updatedUser = dataSnapshot.getValue(User.class);
                updatedUser.addGroup(grpName, contacts);
                Map<String, Object> postValues = new HashMap<String,Object>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    postValues.put(snapshot.getKey(), snapshot.getValue());
                }
                postValues.put("groupList", updatedUser.getGroupList());
                ref.child("users").child(userId).updateChildren(postValues);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });

        Toast.makeText(getActivity(), grpName + " group added.", Toast.LENGTH_SHORT).show();
    }

}
