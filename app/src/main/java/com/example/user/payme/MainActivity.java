package com.example.user.payme;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.user.payme.Fragments.AccountSettingsFragment;
import com.example.user.payme.Fragments.ContactsFragment;
import com.example.user.payme.Fragments.HistoryFragment;
import com.example.user.payme.Fragments.HomeFragment;
import com.example.user.payme.Interfaces.OnFragmentInteractionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private FirebaseAuth auth;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final FragmentManager fragmentManager = getSupportFragmentManager();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        // By default, show home fragment first
        fragment = new HomeFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);

        navigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_home:
                                fragment = new HomeFragment();
                                break;
                            case R.id.navigation_history:
                                fragment = new HistoryFragment();
                                break;
                            case R.id.navigation_addNewReceipt:
                                Intent intent = new Intent(MainActivity.this, AddReceiptActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.navigation_contacts:
                                fragment = new ContactsFragment();
                                break;
                            case R.id.navigation_account:
                                fragment = new AccountSettingsFragment();
                                break;
                        }
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment_container, fragment);
                        transaction.commit();
                        return true;
                    }
                });
    };

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        System.out.println(currentUser);
        if (currentUser == null) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
    }

    public void onFragmentMessage(String TAG) {
        // retrieve fragment tag here
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

}
