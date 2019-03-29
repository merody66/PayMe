package com.example.user.payme.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.payme.Adapters.VerticalRecyclerViewAdapter;
import com.example.user.payme.Interfaces.ContactClickListener;
import com.example.user.payme.Interfaces.OnFragmentInteractionListener;
import com.example.user.payme.MainActivity;
import com.example.user.payme.Objects.Contact;
import com.example.user.payme.Objects.User;
import com.example.user.payme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
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
    private Menu mMenu;

    Cursor cursor;
    private Button addMembersBtn;
    private EditText grpNameTxt;
    private TextView membersList;
    private VerticalRecyclerViewAdapter mAdapter;
    private RecyclerView contactsRecyclerView;
    private LinearLayout contactsListContainer;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference ref;

    private ArrayList<Contact> contactsList;
    private ArrayList<Contact> selectedContacts;
    private MaterialSearchBar searchBar;


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
        membersList = view.findViewById(R.id.membersList);
        searchBar = view.findViewById(R.id.searchBar);
        contactsRecyclerView = view.findViewById(R.id.contactList);
        contactsListContainer = view.findViewById(R.id.contactsListContainer);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ref = db.getReference();

        contactsList = new ArrayList<>();
        selectedContacts = new ArrayList<>();
        GetContactsIntoArrayList();

        ContactClickListener listener = new ContactClickListener()
        {
            String msg = "";
            @Override
            public void onContactClick(Contact contact)
            {
                Toast.makeText(getActivity(), "Contact clicked: " + contact.getmName(), Toast.LENGTH_SHORT).show();
                if (!selectedContacts.contains(contact)) {
                    selectedContacts.add(contact);
                    showOption(R.id.done_btn);
                } else {
                    selectedContacts.remove(contact);

                    if (selectedContacts.size() == 0) {
                        membersList.setText(R.string.group_members_msg);
                        hideOption(R.id.done_btn);
                    }
                }

                if (selectedContacts.size() != 0 ) {
                    msg = "Selected "+String.valueOf(selectedContacts.size())+" contact(s)";
                    membersList.setText(msg);
                }
            }
        };

        mAdapter = new VerticalRecyclerViewAdapter(getActivity(), contactsList, listener);
        contactsRecyclerView.setAdapter(mAdapter);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), VERTICAL);
        contactsRecyclerView.addItemDecoration(decoration);


        addMembersBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                contactsListContainer.setVisibility(View.VISIBLE);
                setHasOptionsMenu(true);
            }
        });


        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {  }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchText = searchBar.getText();
                //Log.d("LOG_TAG", getClass().getSimpleName() + " text changed " + searchText);
                mAdapter.getFilter().filter(searchText);
            }

            @Override
            public void afterTextChanged(Editable editable) {  }

        });


        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mMenu = menu;
        inflater.inflate(R.menu.add_group_bar, menu);
        hideOption(R.id.done_btn);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        String grpName = grpNameTxt.getText().toString().trim();
        if (grpName.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Group name cannot be empty!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return false;
        }

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.done_btn:
                addGroup(grpName, selectedContacts);
                contactsListContainer.setVisibility(View.GONE);
                grpNameTxt.setText("");
                grpNameTxt.setFocusable(false);
                item.setVisible(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void hideOption(int id) {
        MenuItem item = mMenu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id) {
        MenuItem item = mMenu.findItem(id);
        setHasOptionsMenu(true);
        item.setVisible(true);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentMessage("AddGroupFragment");
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

        Toast.makeText(getActivity(), grpName + " group added.", Toast.LENGTH_LONG).show();
    }

}
