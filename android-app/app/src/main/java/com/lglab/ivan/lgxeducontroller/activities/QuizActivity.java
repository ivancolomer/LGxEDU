package com.lglab.ivan.lgxeducontroller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.fragments.ExitFromQuizFragment;
import com.lglab.ivan.lgxeducontroller.fragments.QuestionFragment;
import com.lglab.ivan.lgxeducontroller.games.quiz.QuizManager;

import java.util.ArrayList;
import java.util.List;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;
import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerManager;
import github.chenupt.springindicator.SpringIndicator;
import github.chenupt.springindicator.viewpager.ScrollerViewPager;

public class QuizActivity extends AppCompatActivity {

    ScrollerViewPager viewPager;
    FloatingActionButton exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quiz);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(QuizManager.getInstance().getQuiz().name);

        viewPager = findViewById(R.id.view_pager);
        SpringIndicator springIndicator = findViewById(R.id.indicator);

        List<ItemEntity> list = new ArrayList<>();
        for (int i = 0; i < QuizManager.getInstance().getQuiz().questions.size(); i++) {
            ItemEntityUtil.create(i).setModelView(QuestionFragment.class).attach(list);
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

    private List<String> getTitles() {
        int size = QuizManager.getInstance().getQuiz().questions.size();

        ArrayList<String> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(String.valueOf(i + 1));
        }

        return list;
    }

    @Override
    public boolean onSupportNavigateUp() {
        DialogFragment dialog = new ExitFromQuizFragment();
        dialog.show(this.getSupportFragmentManager(), "dialog");
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
            return onSupportNavigateUp();

        return super.onKeyDown(keyCode, event);
    }

    public void showFloatingExitButton() {
        exitButton.setVisibility(View.VISIBLE);
    }

    public void exit() {
        Log.d("HEY", "EXIT");

        Intent i = new Intent(this, ResultsActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); //Adds the FLAG_ACTIVITY_NO_HISTORY flag
        startActivity(i);
    }
}



