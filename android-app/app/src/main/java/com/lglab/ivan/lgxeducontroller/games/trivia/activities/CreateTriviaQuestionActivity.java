package com.lglab.ivan.lgxeducontroller.games.trivia.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities_new.manager.CreatePOIActivity;
import com.lglab.ivan.lgxeducontroller.activities_new.manager.enums.QuestionCreateEnum;
import com.lglab.ivan.lgxeducontroller.activities_new.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.Trivia;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaQuestion;
import com.lglab.ivan.lgxeducontroller.games.trivia.fragments.TriviaQuestionEditFragment;
import com.lglab.ivan.lgxeducontroller.games.trivia.fragments.TriviaQuestionFragment;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;
import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerManager;
import github.chenupt.springindicator.SpringIndicator;
import github.chenupt.springindicator.viewpager.ScrollerViewPager;

public class CreateTriviaQuestionActivity extends AppCompatActivity {
    private static final String TAG = CreateTriviaQuestionActivity.class.getSimpleName();

    private ScrollerViewPager viewPager;
    //private FloatingActionButton exitButton;
    private Trivia game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quiz);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        game = (Trivia) GameManager.getInstance().getGame();

        actionBar.setTitle(game.getName());

        viewPager = findViewById(R.id.view_pager);
        SpringIndicator springIndicator = findViewById(R.id.indicator);

        List<ItemEntity> list = new ArrayList<>();
        for (int i = 0; i < game.getQuestions().size(); i++) {
            ItemEntityUtil.create(i).setModelView(TriviaQuestionEditFragment.class).attach(list);
        }
        PagerManager manager = PagerManager.begin().addFragments(list).setTitles(getTitles());

        ModelPagerAdapter adapter = new ModelPagerAdapter(getSupportFragmentManager(), manager);
        viewPager.setAdapter(adapter);
        viewPager.fixScrollSpeed();

        // just set viewPager
        springIndicator.setViewPager(viewPager);

        /*exitButton = findViewById(R.id.exit_from_quiz_button);
        exitButton.setOnClickListener(view -> exit());*/
    }

    private List<String> getTitles() {
        int size = game.getQuestions().size();

        ArrayList<String> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(String.valueOf(i + 1));
        }

        return list;
    }

    @Override
    public void onBackPressed() {
        GameManager.getInstance().endGame();
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
            return onSupportNavigateUp();

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onSupportNavigateUp() {
        new AlertDialog.Builder(this)
                .setTitle("Do you really want to exit from this page?")
                .setMessage("If you continue, you will lose all your progress.")
                .setPositiveButton("Yes", (dialog, id) -> {
                    onBackPressed();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    dialog.cancel();
                }).create()
                .show();
        return true;
    }
}
