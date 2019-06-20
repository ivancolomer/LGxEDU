package com.lglab.ivan.lgxeducontroller.games.trivia.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities_new.manager.CreatePOIActivity;
import com.lglab.ivan.lgxeducontroller.activities_new.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.Trivia;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaQuestion;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import java.util.HashMap;
import java.util.Map;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;

import static android.app.Activity.RESULT_OK;

public class TriviaQuestionEditFragment extends Fragment {

    private int questionNumber;
    private Trivia trivia;
    private View view;
    private TriviaQuestion question;

    private HashMap<Long, POI> poiList;
    private ArrayAdapter<POI> poiStringList;
    private EditText questionEditText;
    private RadioGroup correctAnswerRadioButton;
    private EditText[] textAnswers;

    private AutoCompleteTextView textQuestionPOI;
    private AutoCompleteTextView[] answersPOITextEdit;

    private EditText additionalInformation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ItemEntity<Integer> itemEntity = ItemEntityUtil.getModelData(this);
        questionNumber = itemEntity.getContent();
        trivia = (Trivia) GameManager.getInstance().getGame();
        question = (TriviaQuestion)trivia.getQuestions().get(questionNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_create_question, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        poiStringList = new ArrayAdapter<>(getContext(), android.R.layout.select_dialog_item);
        getPOIStringsFromDatabase();

        questionEditText = view.findViewById(R.id.questionTextEdit);
        correctAnswerRadioButton = view.findViewById(R.id.radio_group_correct_answer);
        textQuestionPOI = view.findViewById(R.id.questionPOITextEdit);
        questionPOIText();
        POIButton(R.id.addQuestionPOIButton, 0); //0

        textAnswers = new EditText[TriviaQuestion.MAX_ANSWERS];
        answersPOITextEdit = new AutoCompleteTextView[TriviaQuestion.MAX_ANSWERS];

        for(int i = 0; i < TriviaQuestion.MAX_ANSWERS; i++) {
            textAnswers[i] = view.findViewById(R.id.answer1TextEdit + 2*i);
            answersPOITextEdit[i] = view.findViewById(R.id.answer1POITextEdit + 2*i);
            answerPOIText(i, answersPOITextEdit[i]);
            POIButton(R.id.addAnswer1POIButton + i, i+1);
        }

        additionalInformation = view.findViewById(R.id.informationTextEdit);

            questionEditText.setText(question.getQuestion());

            if(question.correctAnswer > 0)
                ((RadioButton) correctAnswerRadioButton.getChildAt(question.correctAnswer - 1)).setChecked(true);

            for(int i = 0; i < TriviaQuestion.MAX_ANSWERS; i++) {
                if (question.answers[0] != null) {
                    textAnswers[i].setText(question.answers[0]);
                }
            }

            if(question.initialPOI != null)
                textQuestionPOI.setText(question.initialPOI.getName());

            for(int i = 0; i < TriviaQuestion.MAX_ANSWERS; i++) {
                if (question.pois[i] != null) {
                    answersPOITextEdit[i].setText(question.pois[i].getName());
                }
            }

            if(question.information != null)
                additionalInformation.setText(question.information);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        String questionS = getTextFromEditText(questionEditText);
        if(questionS == null || questionS.isEmpty()) {
            Toast.makeText(getContext(), "A text must be filled in the Question textbox", Toast.LENGTH_SHORT).show();
            return;
        }

        int idSelectedRadioButton = correctAnswerRadioButton.getCheckedRadioButtonId();
        if (idSelectedRadioButton == -1) {
            Toast.makeText(getContext(), getString(R.string.correct_answer), Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton pressedRadioButton = view.findViewById(idSelectedRadioButton);
        int correctAnswer = Integer.parseInt(pressedRadioButton.getText().toString());

        String[] answers = new String[TriviaQuestion.MAX_ANSWERS];

        for(int i = 0; i < TriviaQuestion.MAX_ANSWERS; i++) {
            String text = getTextFromEditText(textAnswers[i]);
            if(text == null || text.isEmpty()) {
                Toast.makeText(getContext(), "Answer " + (i + 1) + " POI", Toast.LENGTH_SHORT).show();
                return;
            }
            answers[i] = text;
        }

        for (int i = 0; i < TriviaQuestion.MAX_ANSWERS; i++) {
            if (this.question.pois[i] == null) {
                Toast.makeText(getContext(), "POI of the answer " + (i + 1) + " is not selected, please select one", Toast.LENGTH_SHORT).show();
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

            /*if (type == QuestionCreateEnum.NEW)
                quiz.getQuestions().add(question);
            else
                quiz.getQuestions().set(this.index, question);*/

            /*try {
                POIsProvider.updateGameById((int) quiz.getId(), quiz.pack().toString());
                //Log.d("save", quiz.pack().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
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
        view.findViewById(id).setOnClickListener(view -> {
            Intent createPoiIntent = new Intent(getContext(), CreatePOIActivity.class);
            createPoiIntent.putExtra("POI_BUTTON", resultCode);
            startActivityForResult(createPoiIntent, 0);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        AutoCompleteTextView textPOI = view.findViewById(R.id.questionPOITextEdit);
        textPOI.setAdapter(poiStringList);

        textPOI.setOnItemClickListener((parent, view, position, id) -> question.initialPOI = poiStringList.getItem(position));
    }
}
