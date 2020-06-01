package com.example.watson;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.net.Uri;

public class MainActivity extends AppCompatActivity implements ChartFragment.OnFragmentInteractionListener , ClassifierFragment.OnFragmentInteractionListener , DetailsFragment.OnFragmentInteractionListener, SuggestionsFragment.OnFragmentInteractionListener{


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new ClassifierFragment();
                    break;
                case R.id.navigation_chart:
                    fragment = new ChartFragment();
                    break;
                case R.id.navigation_detail:
                    fragment = new DetailsFragment();
                    break;
                case R.id.navigation_suggestion:
                    fragment = new SuggestionsFragment();
                    break;
            }
            if(fragment == null) return false;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();

            return true;
        }
    };

    public void onFragmentInteraction(Uri uri){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, new ClassifierFragment()).commit();
        BottomNavigationView navView = findViewById(R.id.nav_view);

        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
