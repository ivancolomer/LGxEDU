package com.lglab.ivan.lgxeducontroller.games.trivia.adapters;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.fragments.TriviaResultsFragment;


public class TriviaResultsPagerAdapter extends FragmentStatePagerAdapter {

    private int playersCount;

    public TriviaResultsPagerAdapter(FragmentManager fm, int playersCount) {
        super(fm);
        this.playersCount = playersCount;
    }

    public Fragment getItem(int position) {
        Fragment fragment = new TriviaResultsFragment();

        Bundle args = new Bundle();
        args.putInt("playerId", position);
        fragment.setArguments(args);

        return fragment;
    }

    public int getCount() {
        return playersCount;
    }

    public CharSequence getPageTitle(int position) {
        return ((TriviaManager)GameManager.getInstance()).getPlayerName(position);
    }
}
