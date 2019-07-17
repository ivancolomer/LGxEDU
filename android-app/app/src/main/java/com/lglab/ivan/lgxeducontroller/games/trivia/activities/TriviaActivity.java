package com.lglab.ivan.lgxeducontroller.games.trivia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.fragments.TriviaQuestionFragment;
import com.lglab.ivan.lgxeducontroller.interfaces.IAnswerListener;
import com.lglab.ivan.lgxeducontroller.utils.CustomScrollerViewPager;

import java.util.ArrayList;
import java.util.List;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;
import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerManager;
import github.chenupt.springindicator.SpringIndicator;

public class TriviaActivity extends AppCompatActivity implements IAnswerListener {

    private CustomScrollerViewPager viewPager;
    private FloatingActionButton exitButton;
    private Game trivia;
    private MaterialButton buttonNext, buttonBack;
    private int currentQuestion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_quiz);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(TriviaManager.getInstance().getGame().getName());

        trivia = TriviaManager.getInstance().getGame();
        ((TriviaManager)GameManager.getInstance()).setListener(this);

        viewPager = findViewById(R.id.view_pager);

        SpringIndicator springIndicator = findViewById(R.id.indicator);
        springIndicator.setOnTabClickListener((position) -> false);

        List<ItemEntity> list = new ArrayList<>();
        for (int i = 0; i < trivia.getQuestions().size(); i++) {
            ItemEntityUtil.create(i).setModelView(TriviaQuestionFragment.class).attach(list);
        }
        PagerManager manager = PagerManager.begin().addFragments(list).setTitles(getTitles());

        ModelPagerAdapter adapter = new ModelPagerAdapter(getSupportFragmentManager(), manager);
        viewPager.setAdapter(adapter);
        viewPager.fixScrollSpeed();

        // just set viewPager
        springIndicator.setViewPager(viewPager);

        exitButton = findViewById(R.id.exit_from_quiz_button);
        buttonBack = findViewById(R.id.back_button);
        buttonNext = findViewById(R.id.next_button);

        exitButton.setOnClickListener(view -> exit());

        buttonBack.setOnClickListener((v) -> {
            if(buttonBack.isEnabled()) {
                currentQuestion--;
                viewPager.setCurrentItem(currentQuestion, true);
                buttonBack.setEnabled(currentQuestion > 0);
            }
        });

        buttonNext.setOnClickListener((v) -> {
            if(buttonNext.isEnabled()) {

                if(currentQuestion + 1 >= trivia.getQuestions().size()) {
                    //GAME FINISHED
                    //Show dialogfragment asking if they want to exit!!!
                    exitButton.show();
                    return;
                }

                currentQuestion++;
                viewPager.setCurrentItem(currentQuestion, true);
                buttonNext.setEnabled(false);
            }
        });
    }

    public List<String> getTitles() {
        int size = trivia.getQuestions().size();

        ArrayList<String> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(String.valueOf(i + 1));
        }

        return list;
    }

    @Override
    public boolean onSupportNavigateUp() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Do you really want to exit from this page?")
                .setMessage("If you continue, you will lose all your progress.")
                .setPositiveButton("Yes", (dialog, id) -> onBackPressed())
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel())
                .create()
                .show();
        return true;
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

    public void exit() {
        Log.d("HEY", "EXIT");

        Intent i = new Intent(this, TriviaResultsActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); //Adds the FLAG_ACTIVITY_NO_HISTORY flag
        startActivity(i);
    }

    @Override
    public void updateAnswer(int playerId, int question, int answer) {
        if(question == currentQuestion) {
            buttonNext.setEnabled(((TriviaManager) TriviaManager.getInstance()).allPlayersHasAnswerQuestion(currentQuestion));
        }
    }
}



