package com.lglab.ivan.lgxeducontroller.games.trivia.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaQuestion;
import com.lglab.ivan.lgxeducontroller.games.trivia.adapters.ListAdapter;
import com.lglab.ivan.lgxeducontroller.games.trivia.interfaces.IDraggableListener;
import com.lglab.ivan.lgxeducontroller.games.utils.MultiplayerManagerGame;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;

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
    private androidx.appcompat.widget.AppCompatTextView[] answerViews;


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

        if(questionNumber == 0) {
            getView().findViewById(R.id.drag_drop_info).setVisibility(View.VISIBLE);
        }

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
        if(question.getQuestion().startsWith("%flag_mode% ")) {
            textView.setText("Where is this flag from ");
            AppCompatImageView imageView = view.findViewById(R.id.flag_image);
            try {
                imageView.setImageDrawable(AppCompatResources.getDrawable(getContext(), getContext().getResources().getIdentifier(question.getQuestion().substring(12), "drawable", getContext().getPackageName())));
            } catch(Exception ignored) {
                imageView.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.ic_close_black_24dp));
                imageView.setSupportImageTintList(ColorStateList.valueOf(getContext().getResources().getColor(R.color.red)));

            }
            imageView.setVisibility(View.VISIBLE);
            if(questionNumber == 0) {
                ((RelativeLayout.LayoutParams)getView().findViewById(R.id.drag_drop_info).getLayoutParams()).removeRule(RelativeLayout.END_OF);
                ((RelativeLayout.LayoutParams)getView().findViewById(R.id.drag_drop_info).getLayoutParams()).addRule(RelativeLayout.END_OF, imageView.getId());
            }
        }
        else {
            textView.setText(question.getQuestion());
        }

        answerViews = new androidx.appcompat.widget.AppCompatTextView[TriviaQuestion.MAX_ANSWERS];
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

        for(int playerId = 0; playerId < ((MultiplayerManagerGame)GameManager.getInstance()).getPlayersCount(); playerId++)
            if(((TriviaManager)TriviaManager.getInstance()).getAnswerFromPlayer(playerId, questionNumber) == 0)
                players.add(playerId);

        ListAdapter topListAdapter = new ListAdapter(players, this, 0, (ViewGroup)initial_recyclerview.getParent().getParent());
        initial_recyclerview.setAdapter(topListAdapter);
        initial_recyclerview.setOnDragListener(topListAdapter.getDragInstance());

        for(int i = 0; i < questions_recyclerviews.length; i++) {
            players = new ArrayList<>();
            for(int playerId = 0; playerId < ((MultiplayerManagerGame)GameManager.getInstance()).getPlayersCount(); playerId++)
                if(((TriviaManager)TriviaManager.getInstance()).getAnswerFromPlayer(playerId, questionNumber) == i + 1)
                    players.add(playerId);
            ListAdapter adapter = new ListAdapter(players, this, i + 1, (ViewGroup)questions_recyclerviews[i].getParent().getParent().getParent());
            questions_recyclerviews[i].setAdapter(adapter);
            questions_recyclerviews[i].setOnDragListener(adapter.getDragInstance());
            answerViews[i].setOnDragListener(adapter.getDragInstance());
        }

        checkDraggables();
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

    public void checkDraggables() {
        if((TriviaManager.getInstance()).isQuestionDisabled(questionNumber)) {
            if(questions_recyclerviews != null) {
                initial_recyclerview.setOnDragListener(null);
                ((ListAdapter)initial_recyclerview.getAdapter()).isDisabled = true;
                for(int i = 0; i < questions_recyclerviews.length; i++) {
                    questions_recyclerviews[i].setOnDragListener(null);
                    ((ListAdapter)questions_recyclerviews[i].getAdapter()).isDisabled = true;
                }
                for (int i = 0; i < question.answers.length; i++) {

                    //Rotation Button
                    final AppCompatImageView imageView = new AppCompatImageView(getContext());
                    imageView.setImageDrawable(AppCompatResources.getDrawable(getContext(), question.correctAnswer == i + 1 ? R.drawable.ic_check_box_black_24dp : R.drawable.ic_cancel_black_24dp));

                    RelativeLayout.LayoutParams paramsRotate = new RelativeLayout.LayoutParams(getContext().getResources().getDimensionPixelSize(R.dimen._24sdp), getContext().getResources().getDimensionPixelSize(R.dimen._24sdp));
                    paramsRotate.addRule(RelativeLayout.ALIGN_PARENT_START);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imageView.setSupportImageTintList(ColorStateList.valueOf(question.correctAnswer == i + 1 ? Color.parseColor("#388E3C") : Color.parseColor("#C62828")));
                    imageView.setLayoutParams(paramsRotate);

                    ((ViewGroup)questions_recyclerviews[i].getParent()).addView(imageView);
                }
            }
        }
    }

    private void sendInitialPoi() {
        long poiId = question.initialPOI.getId();
        if (poiId == -1)
            POIController.getInstance().moveToPOI(EARTH_POI, null);
        else if (poiId == -2)
            POIController.getInstance().moveToPOI(EUROPE_POI, null);
        else
            POIController.getInstance().moveToPOI(question.initialPOI, null);
    }

    @Override
    public void draggedViewOnRecyclerView(int playerId, int answer) {
        if(!(TriviaManager.getInstance()).isQuestionDisabled(questionNumber))
            ((TriviaManager)GameManager.getInstance()).answerQuestion(playerId, questionNumber, answer);
    }
}
