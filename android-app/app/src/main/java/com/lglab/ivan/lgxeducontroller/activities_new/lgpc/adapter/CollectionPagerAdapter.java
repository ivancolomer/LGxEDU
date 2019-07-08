package com.lglab.ivan.lgxeducontroller.activities_new.lgpc.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.lglab.ivan.lgxeducontroller.activities_new.lgpc.fragment.SearchFragment;
import com.lglab.ivan.lgxeducontroller.legacy.POISFragment;
import com.lglab.ivan.lgxeducontroller.legacy.TourUserFragment;

public class CollectionPagerAdapter extends FragmentStatePagerAdapter {

    private static final int SEARCH = 0;
    private static final int PAGE_TOURS = 1;

    public CollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case SEARCH:
                if (POISFragment.getTourState()) {
                    POISFragment.resetTourSettings();
                }
                return new SearchFragment();
            case PAGE_TOURS:
                return new TourUserFragment();
            default:
                return getItem(SEARCH);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case SEARCH:
                return "SEARCH";
            case PAGE_TOURS:
                return "TOURS";
            default:
                return "PAGE" + (position - 1);
        }
    }
}
