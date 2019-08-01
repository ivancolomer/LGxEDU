package com.lglab.ivan.lgxeducontroller.games.geofinder.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.geofinder.adapters.GeoFinderResultsPagerAdapter;

public class GeoFinderResultsActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    PagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_results);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        tabLayout = findViewById(R.id.tabLayoutResults);
        tabLayout.setVisibility(View.GONE);
        viewPager = findViewById(R.id.pagerResults);
        adapter = new GeoFinderResultsPagerAdapter(getSupportFragmentManager());

        //Adding adapter to pager
        viewPager.setAdapter(adapter);

        /*tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });*/
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        GameManager.endGame();
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
            return onSupportNavigateUp();

        return super.onKeyDown(keyCode, event);
    }

}


