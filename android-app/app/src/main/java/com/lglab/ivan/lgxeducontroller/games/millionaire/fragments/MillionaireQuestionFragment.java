package com.lglab.ivan.lgxeducontroller.games.millionaire.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.google.android.material.textview.MaterialTextView;
import com.hadiidbouk.charts.ChartProgressBar;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.millionaire.MillionaireQuestion;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;

public class MillionaireQuestionFragment extends Fragment {

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

    private AppCompatTextView textView;
    private RelativeLayout[] relativeLayouts;
    private ChartProgressBar[] charts;
    private com.google.android.material.textview.MaterialTextView[] textAnswers;

    private MillionaireQuestion question;
    private int questionNumber;

    private boolean sendInitialPOIOnCreate = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ItemEntity<Integer> itemEntity = ItemEntityUtil.getModelData(this);
        questionNumber = itemEntity.getContent();
        question = (MillionaireQuestion) GameManager.getInstance().getGame().getQuestions().get(questionNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment__millionaire_question, container, false);
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(questionNumber == 0) {
            if(getView() != null)
                getView().findViewById(R.id.extra_tip_first_page).setVisibility(View.VISIBLE);
        }

        textView = view.findViewById(R.id.question_title);
        textView.setText(question.getQuestion());

        /*
        Log.d("DEBUG", String.valueOf(R.id.answer1_millionaire_layout));
        Log.d("DEBUG", String.valueOf(R.id.answer2_millionaire_layout));
        Log.d("DEBUG", String.valueOf(R.id.answerText1_millionaire));
        Log.d("DEBUG", String.valueOf(R.id.answerText2_millionaire));
        Log.d("DEBUG", String.valueOf(R.id.ChartProgressBar_millionaire_1));
        Log.d("DEBUG", String.valueOf(R.id.ChartProgressBar_millionaire_2));*/

        relativeLayouts = new RelativeLayout[MillionaireQuestion.MAX_ANSWERS];
        charts = new ChartProgressBar[MillionaireQuestion.MAX_ANSWERS];
        textAnswers = new MaterialTextView[MillionaireQuestion.MAX_ANSWERS];

        for(int i = 0; i < MillionaireQuestion.MAX_ANSWERS; i++) {
            relativeLayouts[i] = view.findViewById(R.id.answer1_millionaire_layout + i * 3);
            //charts[i] = view.findViewById(R.id.ChartProgressBar_millionaire_1 + i);
            textAnswers[i] = view.findViewById(R.id.answerText1_millionaire + i);
        }

        for(int i = 0; i < MillionaireQuestion.MAX_ANSWERS; i++) {
            relativeLayouts[i].setOnTouchListener(new TouchListener());

            /*charts[i].setDataList(Collections.singletonList(new BarData("Sep", 3.4f, "3.4€")));
            charts[i].build();*/

            //charts[i].selectBar(0);
            //charts[i].disableBar(0);
        }

        if (sendInitialPOIOnCreate) {
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
            POIController.getInstance().moveToPOI(EARTH_POI, null);
        else if (poiId == -2)
            POIController.getInstance().moveToPOI(EUROPE_POI, null);
        else
            POIController.getInstance().moveToPOI(question.initialPOI, null);
    }

    private static class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            final int Y = 100 - Math.min(100, Math.max(0, (int) motionEvent.getY() * 100 / view.getHeight()));
            Log.d("DEBUG", "||" + Y);

            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();
            }

            return true;
        }
    }
}
