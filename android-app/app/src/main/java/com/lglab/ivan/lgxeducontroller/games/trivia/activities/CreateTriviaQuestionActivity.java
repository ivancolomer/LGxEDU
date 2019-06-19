package com.lglab.ivan.lgxeducontroller.games.trivia.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import com.lglab.ivan.lgxeducontroller.games.trivia.Trivia;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaQuestion;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import org.json.JSONException;
import java.util.HashMap;
import java.util.Map;

public class CreateTriviaQuestionActivity extends AppCompatActivity {
    private static final String TAG = CreateTriviaQuestionActivity.class.getSimpleName();
    private int index;
    private QuestionCreateEnum type;

    private Context context;
    private HashMap<Long, POI> poiList;
    private ArrayAdapter<POI> poiStringList;
    private EditText questionEditText;
    private RadioGroup correctAnswerRadioButton;
    private EditText[] textAnswers;

    private AutoCompleteTextView textQuestionPOI;
    private AutoCompleteTextView[] answersPOITextEdit;

    private EditText additionalInformation;

    private Trivia quiz;
    private TriviaQuestion question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_question);

        quiz = getIntent().getParcelableExtra("game");
        index = getIntent().getIntExtra("index", -1);
        type = (QuestionCreateEnum) getIntent().getSerializableExtra("type");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (type == QuestionCreateEnum.UPDATE)
            actionBar.setTitle(R.string.update_question);
        else
            actionBar.setTitle(R.string.create_question);

        context = CreateTriviaQuestionActivity.this;

        poiStringList = new ArrayAdapter<>(context, android.R.layout.select_dialog_item);
        getPOIStringsFromDatabase();

        questionEditText = findViewById(R.id.questionTextEdit);
        correctAnswerRadioButton = findViewById(R.id.radio_group_correct_answer);
        textQuestionPOI = findViewById(R.id.questionPOITextEdit);
        questionPOIText();
        POIButton(R.id.addQuestionPOIButton, 0); //0

        textAnswers = new EditText[TriviaQuestion.MAX_ANSWERS];
        answersPOITextEdit = new AutoCompleteTextView[TriviaQuestion.MAX_ANSWERS];

        for(int i = 0; i < TriviaQuestion.MAX_ANSWERS; i++) {
            textAnswers[i] = findViewById(R.id.answer1TextEdit + 2*i);
            answersPOITextEdit[i] = findViewById(R.id.answer1POITextEdit + 2*i);
            answerPOIText(i, answersPOITextEdit[i]);
            POIButton(R.id.addAnswer1POIButton + i, i+1);
        }

        additionalInformation = findViewById(R.id.informationTextEdit);

        if (type == QuestionCreateEnum.NEW) {
            question = new TriviaQuestion();
        } else {
            question = (TriviaQuestion)quiz.getQuestions().get(index);
            questionEditText.setText(question.getQuestion());
            ((RadioButton) correctAnswerRadioButton.getChildAt(question.correctAnswer - 1)).setChecked(true);
            for(int i = 0; i < TriviaQuestion.MAX_ANSWERS; i++)
                textAnswers[i].setText(question.answers[0]);

            textQuestionPOI.setText(question.initialPOI.getName());

            for(int i = 0; i < TriviaQuestion.MAX_ANSWERS; i++)
                answersPOITextEdit[i].setText(question.pois[i].getName());

            additionalInformation.setText(question.information);
        }

        findViewById(R.id.accept_button).setOnClickListener(view -> {
            String questionS = getTextFromEditText(questionEditText);
            if(questionS == null || questionS.isEmpty()) {
                Toast.makeText(context, "A text must be filled in the Question textbox", Toast.LENGTH_SHORT).show();
                return;
            }

            int idSelectedRadioButton = correctAnswerRadioButton.getCheckedRadioButtonId();
            if (idSelectedRadioButton == -1) {
                Toast.makeText(context, getString(R.string.correct_answer), Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton pressedRadioButton = findViewById(idSelectedRadioButton);
            int correctAnswer = Integer.parseInt(pressedRadioButton.getText().toString());

            String[] answers = new String[TriviaQuestion.MAX_ANSWERS];

            for(int i = 0; i < TriviaQuestion.MAX_ANSWERS; i++) {
                String text = getTextFromEditText(textAnswers[i]);
                if(text == null || text.isEmpty()) {
                    Toast.makeText(context, "Answer " + (i + 1) + " POI", Toast.LENGTH_SHORT).show();
                    return;
                }
                answers[i] = text;
            }

            for (int i = 0; i < TriviaQuestion.MAX_ANSWERS; i++) {
                if (this.question.pois[i] == null) {
                    Toast.makeText(context, "POI of the answer " + (i + 1) + " is not selected, please select one", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if(question.initialPOI == null) {
                question.initialPOI = POIController.EARTH_POI;
            }


            question.setQuestion(questionS);
            question.answers = answers;
            question.correctAnswer = correctAnswer;
            question.information = additionalInformation.getText().toString();

            if (type == QuestionCreateEnum.NEW)
                quiz.getQuestions().add(question);
            else
                quiz.getQuestions().set(this.index, question);

            try {
                POIsProvider.updateGameById((int) quiz.getId(), quiz.pack().toString());
                //Log.d("save", quiz.pack().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            finish();
        });

        findViewById(R.id.cancel_button).setOnClickListener(view -> {
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
        });
    }

    private void getPOIStringsFromDatabase() {
        poiList = new HashMap<>();
        Cursor poiCursor = POIsProvider.getAllPOIs();

        while (poiCursor.moveToNext()) {
            long poiID = poiCursor.getLong(poiCursor.getColumnIndexOrThrow("_id"));
            String name = poiCursor.getString(poiCursor.getColumnIndexOrThrow("Name"));
            String visitedPlace = poiCursor.getString(poiCursor.getColumnIndexOrThrow("Visited_Place"));
            double longitude = poiCursor.getDouble(poiCursor.getColumnIndexOrThrow("Longitude"));
            double altitude = poiCursor.getDouble(poiCursor.getColumnIndexOrThrow("Altitude"));
            double latitude = poiCursor.getDouble(poiCursor.getColumnIndexOrThrow("Latitude"));
            double heading = poiCursor.getDouble(poiCursor.getColumnIndexOrThrow("Longitude"));
            double tilt = poiCursor.getDouble(poiCursor.getColumnIndexOrThrow("Tilt"));
            double range = poiCursor.getDouble(poiCursor.getColumnIndexOrThrow("Range"));
            String altitudeMode = poiCursor.getString(poiCursor.getColumnIndexOrThrow("Altitude_Mode"));
            boolean hidden = poiCursor.getInt(poiCursor.getColumnIndexOrThrow("Hide")) == 1;
            int categoryID = poiCursor.getInt(poiCursor.getColumnIndexOrThrow("Category"));

            try {
                POI newPOI = new POI(poiID, name, visitedPlace, longitude, latitude, altitude, heading, tilt, range, altitudeMode, hidden, categoryID);
                poiList.put(poiID, newPOI);
            } catch (Exception e) {
                Log.e("BRUH", e.toString());
            }

        }
        poiCursor.close();
        for (Map.Entry pair : poiList.entrySet()) {
            POI temp = (POI) pair.getValue();
            poiStringList.add(temp);
        }
    }

    private void POIButton(int id, int resultCode) {
        findViewById(id).setOnClickListener(view -> {
            Intent createPoiIntent = new Intent(context, CreatePOIActivity.class);
            createPoiIntent.putExtra("POI_BUTTON", resultCode);
            startActivityForResult(createPoiIntent, 0);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            POI returnedPOI = data.getParcelableExtra("POI");
            int button = data.getIntExtra("button", -1);
            String namePOI = returnedPOI.getName();
            poiList.put(returnedPOI.getId(), returnedPOI);
            poiStringList.add(returnedPOI);

            if(button == 0) {
                question.initialPOI = returnedPOI;
                textQuestionPOI.setText(namePOI);
            }
            else if(button >= 1 && button <= 4) {
                question.pois[button - 1] = returnedPOI;
                answersPOITextEdit[button - 1].setText(namePOI);
            }
            else if(button == 6) {
                //Code for intent from Manager (get the quiz Trivia and if editing only one question or the whole quiz Boolean)

            }
        }
    }

    private void answerPOIText(int pos, AutoCompleteTextView textPOI) {
        textPOI.setAdapter(poiStringList);

        textPOI.setOnItemClickListener((parent, view, position, id) -> {
            POI poi = poiStringList.getItem(position);
            question.pois[pos] = poi;
        });
    }

    private String getTextFromEditText(EditText editText) {
        String toReturn = editText.getText().toString();
        if (toReturn.isEmpty()) {
            return null;
        }
        return toReturn;
    }

    private void questionPOIText() {
        AutoCompleteTextView textPOI = findViewById(R.id.questionPOITextEdit);
        textPOI.setAdapter(poiStringList);

        textPOI.setOnItemClickListener((parent, view, position, id) -> question.initialPOI = poiStringList.getItem(position));
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
            return onSupportNavigateUp();

        return super.onKeyDown(keyCode, event);
    }
}
