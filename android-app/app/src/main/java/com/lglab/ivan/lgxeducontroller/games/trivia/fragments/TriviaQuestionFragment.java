package com.lglab.ivan.lgxeducontroller.games.trivia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaQuestion;
import com.lglab.ivan.lgxeducontroller.interfaces.IDraggableListener;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;
import com.lglab.ivan.lgxeducontroller.utils.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;

public class TriviaQuestionFragment extends Fragment implements IDraggableListener {

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

    private TextView textView;
    private RecyclerView initial_recyclerview;
    private RecyclerView[] questions_recyclerviews;
    private TextView[] answerViews;


    private TriviaQuestion question;
    private int questionNumber;

    private boolean sendInitialPOIOnCreate = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ItemEntity<Integer> itemEntity = ItemEntityUtil.getModelData(this);
        questionNumber = itemEntity.getContent();
        question = (TriviaQuestion) GameManager.getInstance().getGame().getQuestions().get(questionNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initial_recyclerview = getView().findViewById(R.id.question_0_rv);
        initial_recyclerview.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));

        questions_recyclerviews = new RecyclerView[TriviaQuestion.MAX_ANSWERS];
        questions_recyclerviews[0] = getView().findViewById(R.id.question_1_rv);
        questions_recyclerviews[0].setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        questions_recyclerviews[1] = getView().findViewById(R.id.question_2_rv);
        questions_recyclerviews[1].setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        questions_recyclerviews[2] = getView().findViewById(R.id.question_3_rv);
        questions_recyclerviews[2].setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        questions_recyclerviews[3] = getView().findViewById(R.id.question_4_rv);
        questions_recyclerviews[3].setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));

        textView = view.findViewById(R.id.question_title);
        textView.setText(question.getQuestion());

        answerViews = new TextView[TriviaQuestion.MAX_ANSWERS];
        answerViews[0] = getView().findViewById(R.id.question_name_1);
        answerViews[1] = getView().findViewById(R.id.question_name_2);
        answerViews[2] = getView().findViewById(R.id.question_name_3);
        answerViews[3] = getView().findViewById(R.id.question_name_4);
        for (int i = 0; i < question.answers.length; i++) {
            answerViews[i].setText(question.answers[i]);
        }

        if (sendInitialPOIOnCreate) {
            sendInitialPOIOnCreate = false;
            sendInitialPoi();
        }

        List<Integer> players = new ArrayList<>();
        for(int i = 0; i < GameManager.getInstance().getPlayersCount(); i++) {
            players.add(i);
        }
        ListAdapter topListAdapter = new ListAdapter(players, this);
        initial_recyclerview.setAdapter(topListAdapter);
        initial_recyclerview.setOnDragListener(topListAdapter.getDragInstance());

        for(int i = 0; i < questions_recyclerviews.length; i++) {
            ListAdapter adapter = new ListAdapter(new ArrayList<>(), this);
            questions_recyclerviews[i].setAdapter(adapter);
            questions_recyclerviews[i].setOnDragListener(adapter.getDragInstance());
            answerViews[i].setOnDragListener(adapter.getDragInstance());
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
        /*view.findViewById(R.id.answerCard1 + i).setOnClickListener(v -> {
            if (!hasClicked) {
                hasClicked = true;

                boolean hadAlreadyClicked = ((TriviaManager) GameManager.getInstance()).hasAnsweredQuestion(0, questionNumber);
                if (!hadAlreadyClicked)
                    ((TriviaManager) GameManager.getInstance()).answerQuestion(0, questionNumber, i + 1);

                view.findViewById(R.id.answerCard1 + question.correctAnswer - 1).setBackgroundColor(Color.parseColor("#388E3C"));
                answerViews[question.correctAnswer - 1].setTextColor(Color.parseColor("#000000"));

                if (i != question.correctAnswer - 1) {
                    v.setBackgroundColor(Color.parseColor("#C62828"));
                    answerViews[i].setTextColor(Color.parseColor("#000000"));
                }

                if (!hadAlreadyClicked) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());

                    if (!((TriviaManager) GameManager.getInstance()).isCorrectAnswer(0, questionNumber)) {
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

                    if (!((TriviaManager) GameManager.getInstance()).isCorrectAnswer(0, questionNumber)) {

                        /*final Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            //Do something after 15sec
                            if(activeAlertDialog.isShowing())
                                activeAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                        }, 15000);*//*

                        activeAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                            POIController.getInstance().moveToPOI(question.pois[question.correctAnswer - 1], true);
                            activeAlertDialog.setMessage("And now going to " + question.pois[question.correctAnswer - 1].getName());
                            v1.setEnabled(false);
                        });
                    }
                }
            }
        });

        if (((TriviaManager) GameManager.getInstance()).hasAnsweredQuestion(0, questionNumber) && ((TriviaManager) GameManager.getInstance()).getAnswerIdOfQuestion(0, questionNumber) == i + 1) {
            view.findViewById(R.id.answerCard1 + i).performClick();
        }*/
    }

    @Override
    public void draggedViewOnRecyclerView(int playerId, int answer) {
        ((TriviaManager)GameManager.getInstance()).answerQuestion(playerId, questionNumber, answer);
    }
}
