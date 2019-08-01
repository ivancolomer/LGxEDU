package com.lglab.ivan.lgxeducontroller.games.geofinder.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.lglab.ivan.lgxeducontroller.games.geofinder.fragments.GeoFinderResultsFragment;


public class GeoFinderResultsPagerAdapter extends FragmentStatePagerAdapter {

    public GeoFinderResultsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int position) {
        Fragment fragment = new GeoFinderResultsFragment();
        return fragment;
    }

    public int getCount() {
        return 1;
    }

    public CharSequence getPageTitle(int position) {
        return "";
    }
}
