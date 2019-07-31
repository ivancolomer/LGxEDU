package com.lglab.ivan.lgxeducontroller.games.trivia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.connection.LGApi;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaQuestion;
import com.lglab.ivan.lgxeducontroller.games.trivia.fragments.TriviaQuestionFragment;
import com.lglab.ivan.lgxeducontroller.games.trivia.interfaces.IAnswerListener;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;
import com.lglab.ivan.lgxeducontroller.utils.CustomScrollerViewPager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;
import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerManager;
import github.chenupt.springindicator.SpringIndicator;

public class TriviaActivity extends AppCompatActivity implements IAnswerListener {

    private CustomScrollerViewPager viewPager;
    //private FloatingActionButton exitButton;
    private Game trivia;
    private MaterialButton buttonNext, buttonBack;
    private int currentQuestion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_quiz);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(TriviaManager.getInstance().getGame().getName());
        }

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

        //exitButton = findViewById(R.id.exit_from_quiz_button);
        buttonBack = findViewById(R.id.back_button);
        buttonNext = findViewById(R.id.next_button);

        //exitButton.setOnClickListener(view -> exit());

        buttonBack.setOnClickListener((v) -> {
            if(buttonBack.isEnabled()) {
                currentQuestion--;
                buttonBack.setEnabled(currentQuestion > 0);
                buttonNext.setEnabled(true);
                buttonNext.setText(!TriviaManager.getInstance().isQuestionDisabled(currentQuestion) ? "CHECK" : "NEXT");
                viewPager.setCurrentItem(currentQuestion, true);
            }
        });

        buttonNext.setOnClickListener((v) -> {
            if(buttonNext.isEnabled()) {
                if(currentQuestion + 1 >= trivia.getQuestions().size() && GameManager.getInstance().isQuestionDisabled(currentQuestion)) {
                    //exitButton.show();
                    exit();
                    return;
                }

                if(!GameManager.getInstance().isQuestionDisabled(currentQuestion)) {
                    GameManager.getInstance().disableQuestionFromAnswering(currentQuestion);
                    showAlertAnswer();
                }
                else {
                    nextPage();
                }
            }
        });
    }

    private void showAlertAnswer() {
        final TriviaQuestion question = ((TriviaQuestion)trivia.getQuestions().get(currentQuestion));
        final List<Integer> wrongAnswers = new ArrayList<>(((TriviaManager) TriviaManager.getInstance()).getWrongAnswers(currentQuestion));

        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
        if(page != null) {
            ((TriviaQuestionFragment)page).checkDraggables();
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setOnCancelListener((dialog) -> nextPage());
        builder.setNegativeButton("SKIP", (dialog, id) -> dialog.cancel());
        builder.setTitle("Watching wrong answers");

        if(wrongAnswers.size() == 0) {
            builder.setTitle("Great! You were all totally right!");
            builder.setMessage("Going to " + question.pois[question.correctAnswer - 1].getName());
            POIController.getInstance().moveToPOI(question.pois[question.correctAnswer - 1], null);
            testEricsAPI(question.pois[question.correctAnswer - 1], question.information);
            builder.create().show();
        }
        else {
            builder.setTitle("Oops! Some of you have chosen a wrong answer!");
            builder.setMessage("Going to " + question.pois[wrongAnswers.get(0)].getName());
            builder.setPositiveButton(wrongAnswers.size() > 1 ? "SHOW NEXT WRONG ANSWER" : "SHOW CORRECT ANSWER", (dialog, id) -> {
                //We override this later in order to prevent alertdialog from closing after clicking this button

            });

            POIController.getInstance().moveToPOI(question.pois[wrongAnswers.get(0)], null);

            final AlertDialog activeAlertDialog = builder.create();
            final boolean[] visitedWrongAnswers = new boolean[wrongAnswers.size()];
            visitedWrongAnswers[0] = true;

            activeAlertDialog.show();

            activeAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                int nonVisitedAnswer = -1;
                for(int i = 0; i < visitedWrongAnswers.length; i++) {
                    if(!visitedWrongAnswers[i]) {
                        nonVisitedAnswer = i;
                        break;
                    }
                }

                if(nonVisitedAnswer == -1) {
                    POIController.getInstance().moveToPOI(question.pois[question.correctAnswer - 1], null);
                    testEricsAPI(question.pois[question.correctAnswer - 1], question.information);
                    activeAlertDialog.setMessage("And now going to " + question.pois[question.correctAnswer - 1].getName());
                    v1.setEnabled(false);
                } else {
                    POIController.getInstance().moveToPOI(question.pois[wrongAnswers.get(nonVisitedAnswer)], null);
                    activeAlertDialog.setMessage("And now going to " + question.pois[wrongAnswers.get(nonVisitedAnswer)].getName());
                    visitedWrongAnswers[nonVisitedAnswer] = true;
                    if(nonVisitedAnswer == visitedWrongAnswers.length - 1) {
                        ((TextView)v1).setText("SHOW CORRECT ANSWER");
                    }
                }
            });
        }
    }

    private static final String SERVER_IP = "192.168.86.145";
    private static final int PORT = 8112;

    private void testEricsAPI(POI poi, String information) {
        //LGApi.sendJsonRequest(getApplicationContext(), Request.Method.DELETE, "http://" + SERVER_IP + ":" + PORT + "/kml/builder/deleteTag/Placemark/12345", (response) -> Log.d("LGAPI", response), null);

        LGApi.sendJsonRequest(getApplicationContext(), Request.Method.POST, "http://" + SERVER_IP + ":" + PORT + "/kml/builder/addplacemark", (response) -> Log.d("LGAPI", "ADDED PLACEMARK SUCCESS: " + response), new HashMap<String, String>() {{
            put("id", "12345");
            put("name", "");
            put("longitude", String.valueOf(poi.getLongitude()));
            put("latitude", String.valueOf(poi.getLatitude()));
            put("range", "0");
            put("description", "<![CDATA[\n" +
                    "  <head>\n" +
                    "    <!-- Required meta tags -->\n" +
                    "    <meta charset=\"utf-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n" +
                    "\n" +
                    "    <!-- Bootstrap CSS -->\n" +
                    "    <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">\n" +
                    "\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "    <div class=\"p-lg-5\" align=\"center\">\n" +
                    "\n" +
                    "        <h1>" + poi.getName() + "</h1>\n" +
                    "        <hr></hr>\n" +
                    "        <h2>" + information + "</h2>\n" +
                    "\n" +
                    "    </div>\n" +
                    "\n" +
                    "    <script src=\"https://code.jquery.com/jquery-3.2.1.slim.min.js\" integrity=\"sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN\" crossorigin=\"anonymous\"></script>\n" +
                    "    <script src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js\" integrity=\"sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q\" crossorigin=\"anonymous\"></script>\n" +
                    "    <script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js\" integrity=\"sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl\" crossorigin=\"anonymous\"></script>\n" +
                    "  </body>\n" +
                    "]]>");
            put("icon", "afasfas");
        }});
        LGApi.sendJsonRequest(getApplicationContext(), Request.Method.GET, "http://" + SERVER_IP + ":" + PORT + "/kml/manage/balloon/12345/1", (response) -> Log.d("LGAPI", response), null);
    }

    private void nextPage() {
        LGApi.sendJsonRequest(getApplicationContext(), Request.Method.GET, "http://" + SERVER_IP + ":" + PORT + "/kml/manage/clean", (response) -> Log.d("LGAPI", "CLEAN SUCCESS: " + response), null);
        LGApi.sendJsonRequest(getApplicationContext(), Request.Method.POST, "http://" + SERVER_IP + ":" + PORT + "/kml/manage/new?name=IvanKML", (response) -> Log.d("LGAPI", response), null);
        if(currentQuestion + 1 < trivia.getQuestions().size())
            currentQuestion++;
        buttonNext.setEnabled(((TriviaManager) TriviaManager.getInstance()).allPlayersHasAnswerQuestion(currentQuestion));
        buttonNext.setText(!TriviaManager.getInstance().isQuestionDisabled(currentQuestion) ? "CHECK" : currentQuestion + 1 >= trivia.getQuestions().size() ? "FINISH" : "NEXT");
        buttonBack.setEnabled(true);
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
        if(page != null) {
            ((TriviaQuestionFragment)page).checkDraggables();
        }

        viewPager.setCurrentItem(currentQuestion, true);
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
            buttonNext.setText(!TriviaManager.getInstance().isQuestionDisabled(currentQuestion) ? "CHECK" : currentQuestion + 1 >= trivia.getQuestions().size() ? "FINISH" : "NEXT");
        }
    }
}



