package com.lglab.ivan.lgxeducontroller.games.millionaire.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.lglab.ivan.lgxeducontroller.games.millionaire.fragments.MillionaireResultsFragment;

public class MillionaireResultsPagerAdapter extends FragmentStatePagerAdapter {

    public MillionaireResultsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int position) {
        Fragment fragment = new MillionaireResultsFragment();
        return fragment;
    }

    public int getCount() {
        return 1;
    }

    public CharSequence getPageTitle(int position) {
        return "";
    }
}
