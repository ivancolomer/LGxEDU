package com.lglab.ivan.lgxeducontroller.games.trivia.fragments;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities_new.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.activities.TriviaActivity;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaQuestion;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;

public class TriviaQuestionFragment extends Fragment {
    private static final POI EARTH_POI = new POI()
            .setLongitude(10.52668d)
            .setLatitude(40.085941d)
            .setAltitude(0.0d)
            .setHeading(0.0d)
            .setTilt(0.0d)
            .setRange(10000000.0d)
            .setAltitudeMode("relativeToSeaFloor");
    private static final POI EUROPE_POI = new POI()
            .setLongitude(9.0629d)
            .setLatitude(47.77d)
            .setAltitude(0.0d)
            .setHeading(0.0d)
            .setTilt(0.0d)
            .setRange(3000000.0d)
            .setAltitudeMode("relativeToSeaFloor");

    AlertDialog activeAlertDialog;
    private View view;
    private int questionNumber;
    private TriviaQuestion question;
    private TextView textView;
    private TextView[] answerViews;
    private boolean sendInitialPOIOnCreate = false;
    private boolean hasClicked = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ItemEntity<Integer> itemEntity = ItemEntityUtil.getModelData(this);
        questionNumber = itemEntity.getContent();
        question = (TriviaQuestion) GameManager.getInstance().getGame().getQuestions().get(questionNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_question, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hasClicked = false;

        textView = view.findViewById(R.id.question_title);
        textView.setText(question.getQuestion());

        answerViews = new TextView[TriviaQuestion.MAX_ANSWERS];
        answerViews[0] = getView().findViewById(R.id.answerText1);
        answerViews[1] = getView().findViewById(R.id.answerText2);
        answerViews[2] = getView().findViewById(R.id.answerText3);
        answerViews[3] = getView().findViewById(R.id.answerText4);
        for (int i = 0; i < question.answers.length; i++) {
            answerViews[i].setText(question.answers[i]);
        }

        for (int i = 0; i < answerViews.length; i++) {
            setClickListener(i);
        }

        if (sendInitialPOIOnCreate == true) {
            sendInitialPOIOnCreate = false;
            sendInitialPoi();
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            if (question == null)
                sendInitialPOIOnCreate = true;
            else
                sendInitialPoi();
        }
    }

    private void sendInitialPoi() {
        long poiId = question.initialPOI.getId();
        if (poiId == -1)
            POIController.getInstance().moveToPOI(EARTH_POI, true);
        else if (poiId == -2)
            POIController.getInstance().moveToPOI(EUROPE_POI, true);
        else
            POIController.getInstance().moveToPOI(question.initialPOI, true);
    }

    public void setClickListener(final int i) {
        view.findViewById(R.id.answerCard1 + i).setOnClickListener(v -> {
            if (!hasClicked) {
                hasClicked = true;

                boolean hadAlreadyClicked = ((TriviaManager) GameManager.getInstance()).hasAnsweredQuestion(questionNumber);
                if(!hadAlreadyClicked)
                    ((TriviaManager) GameManager.getInstance()).answerQuestion(questionNumber, i + 1);

                view.findViewById(R.id.answerCard1 + question.correctAnswer - 1).setBackgroundColor(Color.parseColor("#5cd65c"));
                answerViews[question.correctAnswer - 1].setTextColor(Color.parseColor("#000000"));

                if (i != question.correctAnswer - 1) {
                    v.setBackgroundColor(Color.parseColor("#ff3333"));
                    answerViews[i].setTextColor(Color.parseColor("#000000"));
                }

                if (!hadAlreadyClicked) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    if (!((TriviaManager) GameManager.getInstance()).isCorrectAnswer(questionNumber)) {
                        builder.setTitle("Oops! You've chosen a wrong answer!");
                        builder.setMessage("Going to " + question.pois[i].getName());
                        builder.setPositiveButton("SHOW CORRECT ANSWER", (dialog, id) -> {
                            //We override this later in order to prevent alertdialog from closing after clicking this button
                        });
                        POIController.getInstance().moveToPOI(question.pois[i], true);
                    } else {
                        builder.setTitle("Great! You're totally right!");
                        builder.setMessage("Going to " + question.pois[question.correctAnswer - 1].getName());
                        POIController.getInstance().moveToPOI(question.pois[question.correctAnswer - 1], true);
                    }

                    builder.setOnCancelListener(dialog -> checkQuizProgress());
                    builder.setNegativeButton("SKIP", (dialog, id) -> dialog.cancel());

                    activeAlertDialog = builder.create();
                    activeAlertDialog.show();

                    if (!((TriviaManager) GameManager.getInstance()).isCorrectAnswer(i)) {

                        /*final Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            //Do something after 15sec
                            if(activeAlertDialog.isShowing())
                                activeAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                        }, 15000);*/

                        activeAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                            POIController.getInstance().moveToPOI(question.pois[question.correctAnswer - 1], true);
                            activeAlertDialog.setMessage("And now going to " + question.pois[question.correctAnswer - 1].getName());
                            v1.setEnabled(false);
                        });
                    }
                }
            }
        });

        if (((TriviaManager) GameManager.getInstance()).hasAnsweredQuestion(questionNumber) && ((TriviaManager) GameManager.getInstance()).getAnswerIdOfQuestion(questionNumber) == i + 1) {
            view.findViewById(R.id.answerCard1 + i).performClick();
        }
    }

    private void checkQuizProgress() {
        if (((TriviaManager) GameManager.getInstance()).hasAnsweredAllQuestions()) {
            ((TriviaActivity) getActivity()).showFloatingExitButton();
        }
    }
}
