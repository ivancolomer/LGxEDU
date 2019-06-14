package com.lglab.ivan.lgxeducontroller.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
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
import com.lglab.ivan.lgxeducontroller.fragments.ExitFromQuizFragment;
import com.lglab.ivan.lgxeducontroller.games.trivia.Trivia;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaQuestion;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;
import com.lglab.ivan.lgxeducontroller.utils.Exceptions.MissingInformationException;

import org.json.JSONException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CreateQuestionActivity extends AppCompatActivity {
    private static final String TAG = CreateQuestionActivity.class.getSimpleName();
    private int index;
    private UpdateNew type;


    public enum UpdateNew {UPDATE, NEW;}

    private Context context;
    private HashMap<Long, POI> poiList;
    private ArrayAdapter<POI> poiStringList;
    private EditText questionEditText;
    private RadioGroup correctAnswerRadioButton;
    private EditText textAnswer1;
    private EditText textAnswer2;
    private EditText textAnswer3;
    private EditText textAnswer4;

    private AutoCompleteTextView textQuestionPOI;
    private AutoCompleteTextView answer1POITextEdit;
    private AutoCompleteTextView answer2POITextEdit;
    private AutoCompleteTextView answer3POITextEdit;
    private AutoCompleteTextView answer4POITextEdit;

    private EditText additionalInformation;

    private Trivia quiz;
    private TriviaQuestion question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question);
        quiz = getIntent().getParcelableExtra("game");
        index = getIntent().getIntExtra("index", -1);
        type = (UpdateNew) getIntent().getSerializableExtra("type");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (type == UpdateNew.UPDATE) actionBar.setTitle(R.string.update_question);
        else actionBar.setTitle(R.string.create_question);
        context = CreateQuestionActivity.this;


        poiStringList = new ArrayAdapter<>(context, android.R.layout.select_dialog_item);

        getPOIStringsFromDatabase();

        questionEditText = findViewById(R.id.questionTextEdit);
        correctAnswerRadioButton = findViewById(R.id.radio_group_correct_answer);

        textAnswer1 = findViewById(R.id.answer1TextEdit);
        textAnswer2 = findViewById(R.id.answer2TextEdit);
        textAnswer3 = findViewById(R.id.answer3TextEdit);
        textAnswer4 = findViewById(R.id.answer4TextEdit);


        textQuestionPOI = findViewById(R.id.questionPOITextEdit);
        answer1POITextEdit = findViewById(R.id.answer1POITextEdit);
        answer2POITextEdit = findViewById(R.id.answer2POITextEdit);
        answer3POITextEdit = findViewById(R.id.answer3POITextEdit);
        answer4POITextEdit = findViewById(R.id.answer4POITextEdit);

        //Autocomplete text field listeners
        answerPOIText(0, answer1POITextEdit);
        answerPOIText(1, answer2POITextEdit);
        answerPOIText(2, answer3POITextEdit);
        answerPOIText(3, answer4POITextEdit);
        questionPOIText();

        //POIs Buttons listeners
        POIButton(R.id.addQuestionPOIButton, 0);
        POIButton(R.id.addAnswer1POIButton, 1);
        POIButton(R.id.addAnswer2POIButton, 2);
        POIButton(R.id.addAnswer3POIButton, 3);
        POIButton(R.id.addAnswer4POIButton, 4);

        additionalInformation = findViewById(R.id.informationTextEdit);

        if (type == UpdateNew.NEW) {
            question = new TriviaQuestion();
        } else {
            question = (TriviaQuestion)quiz.getQuestions().get(index);
            questionEditText.setText(question.getQuestion());
            ((RadioButton) correctAnswerRadioButton.getChildAt(question.correctAnswer - 1)).setChecked(true);
            textAnswer1.setText(question.answers[0]);
            textAnswer2.setText(question.answers[1]);
            textAnswer3.setText(question.answers[2]);
            textAnswer4.setText(question.answers[3]);

            textQuestionPOI.setText(question.initialPOI.getName());
            answer1POITextEdit.setText(question.pois[0].getName());
            answer2POITextEdit.setText(question.pois[1].getName());
            answer3POITextEdit.setText(question.pois[2].getName());
            answer4POITextEdit.setText(question.pois[3].getName());

            additionalInformation.setText(question.information);
        }

        //finish buttons listeners
        acceptButton();
        cancelButton();
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
        for (Map.Entry pair : poiList.entrySet()) {
            POI temp = (POI) pair.getValue();
            poiStringList.add(temp);
        }
    }

    private void POIButton(int id, int button) {
        findViewById(id).setOnClickListener(view -> {
            Intent createPoiIntent = new Intent(context, CreateItemActivity_Copy.class);
            createPoiIntent.putExtra("CREATION_TYPE", "POI");
            createPoiIntent.putExtra("Button", button);
            startActivity(createPoiIntent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            POI returnedPOI = data.getParcelableExtra("poi");
            String namePOI = returnedPOI.getName();
            poiList.put(returnedPOI.getId(), returnedPOI);
            poiStringList.add(returnedPOI);


            switch (requestCode) {
                case 0:
                    question.initialPOI = returnedPOI;
                    textQuestionPOI.setText(namePOI);

                case 1:
                    question.pois[0] = returnedPOI;
                    answer1POITextEdit.setText(namePOI);
                    break;

                case 2:
                    question.pois[1] = returnedPOI;
                    answer2POITextEdit.setText(namePOI);
                    break;

                case 3:
                    question.pois[2] = returnedPOI;
                    answer3POITextEdit.setText(namePOI);
                    break;

                case 4:
                    question.pois[3] = returnedPOI;
                    answer4POITextEdit.setText(namePOI);
                    break;

                case 6:
                    //Code for intent from Manager (get the quiz Trivia and if editing only one question or the whole quiz Boolean)
                    break;
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

    private void acceptButton() {
        findViewById(R.id.accept_button).setOnClickListener(view -> {
            try {
                //Id question (need Intend from Game Manager)
                //If edit question, id := question id. Else add new id := last quiz id + 1


                //TriviaQuestion stuff
                String questionS = getTextFromEditText(questionEditText, getString(R.string.question_text_edit));

                //Correct Answer
                int idSelectedRadioButton = correctAnswerRadioButton.getCheckedRadioButtonId();
                if (idSelectedRadioButton == -1) {
                    throw new MissingInformationException(getString(R.string.correct_answer));
                }
                RadioButton pressedRadioButton = (RadioButton) findViewById(correctAnswerRadioButton.getCheckedRadioButtonId());
                int correctAnswer = Integer.parseInt(pressedRadioButton.getText().toString());

                //Answers
                String[] answers = {getTextFromEditText(textAnswer1, getString(R.string.answer_1)),
                        getTextFromEditText(textAnswer2, getString(R.string.answer_2)),
                        getTextFromEditText(textAnswer3, getString(R.string.answer_3)),
                        getTextFromEditText(textAnswer4, getString(R.string.answer_4))};

                for (int i = 0; i < this.question.pois.length; i++) {
                    if (this.question.pois[i] == null)
                        throw new MissingInformationException("Answer " + i + 1 + " POI");
                }

                String information = additionalInformation.getText().toString();
                question.setQuestion(questionS);
                question.answers = answers;
                question.correctAnswer = correctAnswer;
                question.information = information;

                if (type == UpdateNew.NEW) {
                    //question.id = quiz.questions.size();
                    quiz.getQuestions().add(question);
                } else {
                    //question.id = quiz.questions.get(this.index).id;
                    quiz.getQuestions().set(this.index, question);
                }
                Log.i(TAG, "acceptButton: " + question + " " + correctAnswer + " " + Arrays.toString(answers) + " " + information + " " + Arrays.toString(question.pois) + " " + question.initialPOI);
                Log.i(TAG, "acceptButton: " + quiz);

                try {
                    POIsProvider.updateQuizById((int) quiz.getId(), quiz.pack().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                finish();

            } catch (MissingInformationException e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelButton() {
        findViewById(R.id.cancel_button).setOnClickListener(view -> {
            DialogFragment dialog = new ExitFromQuizFragment();
            dialog.show(this.getSupportFragmentManager(), "dialog");
        });

    }

    private String getTextFromEditText(EditText editText, String whichTextEdit) throws MissingInformationException {
        String toReturn = editText.getText().toString();
        if (toReturn.isEmpty())
            throw new MissingInformationException(whichTextEdit);
        return toReturn;
    }

    private void questionPOIText() {
        AutoCompleteTextView textPOI = findViewById(R.id.questionPOITextEdit);
        textPOI.setAdapter(poiStringList);

        textPOI.setOnItemClickListener((parent, view, position, id) -> {
            question.initialPOI = poiStringList.getItem(position);
        });
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

    @Override
    public String toString() {
        return "CreateQuestionActivity{" +
                "index=" + index +
                ", type=" + type +
                ", context=" + context +
                ", poiList=" + poiList +
                ", poiStringList=" + poiStringList +
                ", questionEditText=" + questionEditText +
                ", correctAnswerRadioButton=" + correctAnswerRadioButton +
                ", textAnswer1=" + textAnswer1 +
                ", textAnswer2=" + textAnswer2 +
                ", textAnswer3=" + textAnswer3 +
                ", textAnswer4=" + textAnswer4 +
                ", textQuestionPOI=" + textQuestionPOI +
                ", answer1POITextEdit=" + answer1POITextEdit +
                ", answer2POITextEdit=" + answer2POITextEdit +
                ", answer3POITextEdit=" + answer3POITextEdit +
                ", answer4POITextEdit=" + answer4POITextEdit +
                ", additionalInformation=" + additionalInformation +
                ", quiz=" + quiz +
                ", question=" + question +
                '}';
    }
}
