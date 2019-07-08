package com.lglab.ivan.lgxeducontroller.games.trivia_versus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.activities.TriviaActivity;
import com.lglab.ivan.lgxeducontroller.games.trivia_versus.fragments.TriviaVersusQuestionFragment;

import java.util.ArrayList;
import java.util.List;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;
import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerManager;
import github.chenupt.springindicator.SpringIndicator;
import github.chenupt.springindicator.viewpager.ScrollerViewPager;

public class TriviaVersusActivity extends TriviaActivity {

    ScrollerViewPager viewPager;
    FloatingActionButton exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quiz);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(TriviaManager.getInstance().getGame().getName());

        viewPager = findViewById(R.id.view_pager);
        SpringIndicator springIndicator = findViewById(R.id.indicator);

        List<ItemEntity> list = new ArrayList<>();
        for (int i = 0; i < TriviaManager.getInstance().getGame().getQuestions().size(); i++) {
            ItemEntityUtil.create(i).setModelView(TriviaVersusQuestionFragment.class).attach(list);
        }
        PagerManager manager = PagerManager.begin().addFragments(list).setTitles(getTitles());

        ModelPagerAdapter adapter = new ModelPagerAdapter(getSupportFragmentManager(), manager);
        viewPager.setAdapter(adapter);
        viewPager.fixScrollSpeed();

        // just set viewPager
        springIndicator.setViewPager(viewPager);

        exitButton = findViewById(R.id.exit_from_quiz_button);
        exitButton.setOnClickListener(view -> exit());
    }

    @Override
    public void exit() {
        Log.d("HEY", "EXIT");

        Intent i = new Intent(this, TriviaVersusResultsActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); //Adds the FLAG_ACTIVITY_NO_HISTORY flag
        startActivity(i);
    }
}



