package com.example.user.payme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.user.payme.Fragments.ContactsFragment;
import com.example.user.payme.Interfaces.OnFragmentInteractionListener;

public class ChooseContactActivity extends AppCompatActivity implements OnFragmentInteractionListener {
    private static final String TAG = "ChooseContactActivity";
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final FragmentManager fragmentManager = getSupportFragmentManager();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);

        fragment = new ContactsFragment();

        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container_choose, fragment);
        transaction.commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);

        //todo fix nav bar fragments at other view beside mainactivity
        navigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Intent intent = new Intent(ChooseContactActivity.this, MainActivity.class);
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
                });
    }

    @Override
    public void onFragmentMessage(String TAG) {
        // retrieve fragment tag here
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
