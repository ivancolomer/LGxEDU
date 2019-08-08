package com.lglab.ivan.lgxeducontroller.games.millionaire.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.connection.LGApi;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.millionaire.Millionaire;
import com.lglab.ivan.lgxeducontroller.games.millionaire.MillionaireManager;
import com.lglab.ivan.lgxeducontroller.games.millionaire.MillionaireQuestion;
import com.lglab.ivan.lgxeducontroller.games.millionaire.fragments.MillionaireQuestionFragment;
import com.lglab.ivan.lgxeducontroller.games.millionaire.interfaces.IAnswerListener;
import com.lglab.ivan.lgxeducontroller.utils.CustomScrollerViewPager;

import java.util.ArrayList;
import java.util.List;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;
import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerManager;
import github.chenupt.springindicator.SpringIndicator;

public class MillionaireActivity extends AppCompatActivity implements IAnswerListener {

    private CustomScrollerViewPager viewPager;

    private Millionaire millionaire;
    private MaterialButton buttonNext, buttonBack;
    private int currentQuestion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quiz);

        millionaire = (Millionaire) MillionaireManager.getInstance().getGame();
        ((MillionaireManager)GameManager.getInstance()).setListener(this);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(millionaire.getName());
        }

        viewPager = findViewById(R.id.view_pager);

        SpringIndicator springIndicator = findViewById(R.id.indicator);
        springIndicator.setOnTabClickListener((position) -> false);

        List<ItemEntity> list = new ArrayList<>();
        for (int i = 0; i < millionaire.getQuestions().size(); i++) {
            ItemEntityUtil.create(i).setModelView(MillionaireQuestionFragment.class).attach(list);
        }
        PagerManager manager = PagerManager.begin().addFragments(list).setTitles(getTitles());

        ModelPagerAdapter adapter = new ModelPagerAdapter(getSupportFragmentManager(), manager);
        viewPager.setAdapter(adapter);
        viewPager.fixScrollSpeed();

        springIndicator.setViewPager(viewPager);

        buttonBack = findViewById(R.id.back_button);
        buttonNext = findViewById(R.id.next_button);

        buttonBack.setOnClickListener((v) -> {
            if(buttonBack.isEnabled()) {
                currentQuestion--;
                buttonBack.setEnabled(currentQuestion > 0);
                buttonNext.setEnabled(true);
                buttonNext.setText(!GameManager.getInstance().isQuestionDisabled(currentQuestion) ? "CHECK" : "NEXT");
                viewPager.setCurrentItem(currentQuestion, true);
            }
        });

        buttonNext.setOnClickListener((v) -> {
            if(buttonNext.isEnabled()) {
                if(currentQuestion + 1 >= millionaire.getQuestions().size() && GameManager.getInstance().isQuestionDisabled(currentQuestion)) {
                    exit();
                    return;
                }

                if(GameManager.getInstance().isQuestionDisabled(currentQuestion)) {
                    nextPage();
                    return;
                }

                GameManager.getInstance().disableQuestionFromAnswering(currentQuestion);

                int[] points = ((MillionaireManager)MillionaireManager.getInstance()).getPointsForQuestion(currentQuestion);

                int maxPoints = points[0];
                int answerWithMaxPoints = 0;
                for(int i = 1; i < points.length; i++) {
                    if(points[i] > maxPoints) {
                        answerWithMaxPoints = i;
                        maxPoints = points[i];
                    }
                }

                MillionaireQuestion question = ((MillionaireQuestion)GameManager.getInstance().getGame().getQuestions().get(currentQuestion));

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setOnCancelListener((dialog) -> nextPage());
                builder.setNegativeButton("SKIP", (dialog, id) -> dialog.cancel());

                if(maxPoints == 0 || answerWithMaxPoints != question.correctAnswer - 1) {
                    //OOPS, you weren't right
                    builder.setTitle("Oops! You were wrong!");
                } else {
                    //great, you were right
                    builder.setTitle("Great! You were right!");
                }

                builder.setMessage("The answer is " + question.answers[question.correctAnswer - 1] + ".\n" +
                        "You have " + points[question.correctAnswer - 1] + " points left.\n" +
                        "Going to " + question.poi.getName());
                POIController.getInstance().moveToPOI(question.poi, null);
                LGApi.sendBalloonToPoi(getApplicationContext(), question.poi, question.information);
                builder.create().show();
            }
        });
    }

    private void nextPage() {
        LGApi.cleanBalloon(getApplicationContext());

        if(currentQuestion + 1 < millionaire.getQuestions().size())
            currentQuestion++;

        buttonNext.setEnabled(((MillionaireManager) MillionaireManager.getInstance()).getPointsLeftForQuestion(currentQuestion) == 0);
        buttonNext.setText(!MillionaireManager.getInstance().isQuestionDisabled(currentQuestion) ? "CHECK" : currentQuestion + 1 >= millionaire.getQuestions().size() ? "FINISH" : "NEXT");

        if(millionaire.getQuestions().size() > 1)
            buttonBack.setEnabled(true);

        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
        if(page != null) {
            ((MillionaireQuestionFragment)page).loadSeekBars();
        }

        if(currentQuestion != viewPager.getCurrentItem()) {
            page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + currentQuestion);
            if(page != null) {
                ((MillionaireQuestionFragment)page).loadSeekBars();
            }
        }

        viewPager.setCurrentItem(currentQuestion, true);
    }

    public List<String> getTitles() {
        int size = millionaire.getQuestions().size();

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
        Intent i = new Intent(this, MillionaireResultsActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); //Adds the FLAG_ACTIVITY_NO_HISTORY flag
        startActivity(i);
    }

    @Override
    public void updateAnswer(int questionId) {
        if(questionId == currentQuestion) {
            buttonNext.setEnabled(((MillionaireManager) MillionaireManager.getInstance()).getPointsLeftForQuestion(currentQuestion) == 0);
            buttonNext.setText(!MillionaireManager.getInstance().isQuestionDisabled(currentQuestion) ? "CHECK" : currentQuestion + 1 >= millionaire.getQuestions().size() ? "FINISH" : "NEXT");
        }
    }
}




