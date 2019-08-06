package com.lglab.ivan.lgxeducontroller.games.millionaire.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

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
        return inflater.inflate(R.layout.fragment_geofinder_question, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(questionNumber == 0) {
            if(getView() != null)
                getView().findViewById(R.id.extra_tip_first_page).setVisibility(View.VISIBLE);
        }

        textView = view.findViewById(R.id.question_title);
        textView.setText(question.getQuestion());

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
}
