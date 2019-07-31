package com.lglab.ivan.lgxeducontroller.games.geofinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.connection.LGCommand;
import com.lglab.ivan.lgxeducontroller.connection.LGConnectionManager;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.geofinder.GeoFinder;
import com.lglab.ivan.lgxeducontroller.games.geofinder.GeoFinderManager;
import com.lglab.ivan.lgxeducontroller.games.geofinder.GeoFinderQuestion;
import com.lglab.ivan.lgxeducontroller.games.geofinder.fragments.GeoFinderQuestionFragment;
import com.lglab.ivan.lgxeducontroller.utils.CustomScrollerViewPager;

import java.util.ArrayList;
import java.util.List;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;
import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerManager;
import github.chenupt.springindicator.SpringIndicator;

public class GeoFinderActivity extends AppCompatActivity {

    private CustomScrollerViewPager viewPager;

    private GeoFinder geofinder;
    private MaterialButton buttonNext, buttonBack;
    private int currentQuestion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quiz);

        geofinder = (GeoFinder)GeoFinderManager.getInstance().getGame();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(geofinder.getName());
        }

        viewPager = findViewById(R.id.view_pager);

        SpringIndicator springIndicator = findViewById(R.id.indicator);
        springIndicator.setOnTabClickListener((position) -> false);

        List<ItemEntity> list = new ArrayList<>();
        for (int i = 0; i < geofinder.getQuestions().size(); i++) {
            ItemEntityUtil.create(i).setModelView(GeoFinderQuestionFragment.class).attach(list);
        }
        PagerManager manager = PagerManager.begin().addFragments(list).setTitles(getTitles());

        ModelPagerAdapter adapter = new ModelPagerAdapter(getSupportFragmentManager(), manager);
        viewPager.setAdapter(adapter);
        viewPager.fixScrollSpeed();

        springIndicator.setViewPager(viewPager);

        buttonBack = findViewById(R.id.back_button);
        buttonNext = findViewById(R.id.next_button);
        buttonNext.setEnabled(true);

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
                if(currentQuestion + 1 >= geofinder.getQuestions().size() && GameManager.getInstance().isQuestionDisabled(currentQuestion)) {
                    exit();
                    return;
                }

                if(GameManager.getInstance().isQuestionDisabled(currentQuestion)) {
                    nextPage();
                    return;
                }

                GameManager.getInstance().disableQuestionFromAnswering(currentQuestion);

                final AlertDialog loading_dialog = new MaterialAlertDialogBuilder(this)
                .setView(R.layout.progress)
                .setTitle("Getting position from the LiquidGalaxy")
                .setMessage("")
                .setOnCancelListener((dialog) -> nextPage())
                .setNegativeButton("SKIP", (dialog, id) -> dialog.cancel())
                .setPositiveButton("SHOW ANSWER", (dialog, id) -> { })
                .create();

                loading_dialog.setCancelable(false);
                loading_dialog.setCanceledOnTouchOutside(false);
                loading_dialog.show();

                loading_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
                loading_dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);

                LGConnectionManager.getInstance().addCommandToLG(new LGCommand("echo $(/home/lg/bin/lg-locate)", LGCommand.CRITICAL_MESSAGE, (response -> {
                    String[] responseSplitted = response.split(" ", 3);
                    Log.d("Test", response);
                    ((GeoFinderManager)GeoFinderManager.getInstance()).answerQuestion(currentQuestion, Double.parseDouble(responseSplitted[0]), Double.parseDouble(responseSplitted[1]));
                    int score = ((GeoFinderManager)GeoFinderManager.getInstance()).getScoreQuestion(currentQuestion);
                    loading_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((view) -> {
                        POIController.getInstance().moveToPOI(((GeoFinderQuestion)(GeoFinderManager.getInstance().getGame().getQuestions().get(currentQuestion))).poi, null);
                        view.setEnabled(false);
                        loading_dialog.setCancelable(true);
                        loading_dialog.setCanceledOnTouchOutside(true);
                    });
                    loading_dialog.setMessage("You have scored " + score + " out of 1000 points!");
                    loading_dialog.findViewById(R.id.loader).setVisibility(View.GONE);
                    loading_dialog.findViewById(R.id.loading_msg).setVisibility(View.GONE);
                    loading_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                    loading_dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                })));
            }
        });
    }

    private void nextPage() {
        if(currentQuestion + 1 < geofinder.getQuestions().size())
            currentQuestion++;
        buttonNext.setEnabled(true);
        buttonNext.setText(!GeoFinderManager.getInstance().isQuestionDisabled(currentQuestion) ? "CHECK" : currentQuestion + 1 >= geofinder.getQuestions().size() ? "FINISH" : "NEXT");
        buttonBack.setEnabled(true);
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
        if(page != null) {
            //((TriviaQuestionFragment)page).checkDraggables();
        }

        viewPager.setCurrentItem(currentQuestion, true);
    }

    public List<String> getTitles() {
        int size = geofinder.getQuestions().size();

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
        Intent i = new Intent(this, GeoFinderResultsActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); //Adds the FLAG_ACTIVITY_NO_HISTORY flag
        startActivity(i);
    }
}



