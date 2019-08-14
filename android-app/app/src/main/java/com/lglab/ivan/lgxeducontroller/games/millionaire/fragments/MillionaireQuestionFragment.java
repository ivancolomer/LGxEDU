package com.lglab.ivan.lgxeducontroller.games.millionaire.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.millionaire.MillionaireManager;
import com.lglab.ivan.lgxeducontroller.games.millionaire.MillionaireQuestion;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;

import abak.tr.com.boxedverticalseekbar.BoxedVertical;
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
    //private RelativeLayout[] relativeLayouts;
    private BoxedVertical[] charts;
    private com.google.android.material.textview.MaterialTextView[] textAnswers;

    private MillionaireQuestion question;
    private MillionaireManager manager;
    private int questionNumber;

    private boolean sendInitialPOIOnCreate = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ItemEntity<Integer> itemEntity = ItemEntityUtil.getModelData(this);
        questionNumber = itemEntity.getContent();
        question = (MillionaireQuestion) GameManager.getInstance().getGame().getQuestions().get(questionNumber);
        manager = (MillionaireManager) GameManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment__millionaire_question, container, false);
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(questionNumber == 0) {
            if(getView() != null) {
                getView().findViewById(R.id.extra_tip_first_page).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.extra_tip_first_page).setOnClickListener((v) -> new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Help")
                        .setMessage("Move up/down the bar below each answer to assign your points.")
                        .setNegativeButton(R.string.close, (dialog, id) -> dialog.cancel())
                        .create()
                        .show());
            }
        }

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
                ((RelativeLayout.LayoutParams)getView().findViewById(R.id.extra_tip_first_page).getLayoutParams()).removeRule(RelativeLayout.END_OF);
                ((RelativeLayout.LayoutParams)getView().findViewById(R.id.extra_tip_first_page).getLayoutParams()).addRule(RelativeLayout.END_OF, imageView.getId());
            }
        }
        else {
            textView.setText(question.getQuestion());
        }

        //relativeLayouts = new RelativeLayout[MillionaireQuestion.MAX_ANSWERS];
        charts = new BoxedVertical[MillionaireQuestion.MAX_ANSWERS];
        textAnswers = new MaterialTextView[MillionaireQuestion.MAX_ANSWERS];

        for(int i = 0; i < MillionaireQuestion.MAX_ANSWERS; i++) {
            //relativeLayouts[i] = view.findViewById(R.id.answer1_millionaire_layout + i * 3);
            charts[i] = view.findViewById(R.id.ChartProgressBar_millionaire_1 + i);
            textAnswers[i] = view.findViewById(R.id.answerText1_millionaire + i);
        }

        for(int i = 0; i < MillionaireQuestion.MAX_ANSWERS; i++) {
            textAnswers[i].setText(question.answers[i]);

            charts[i].setOnBoxedPointsChangeListener(new BoxedVertical.OnValuesChangeListener() {
                @Override
                public void onPointsChanged(BoxedVertical boxedPoints, int points) {
                    if(!manager.isQuestionDisabled(questionNumber)) {
                        int i = 0;
                        for(BoxedVertical boxedVertical : charts) {
                            if(boxedVertical == boxedPoints)
                                break;
                            i++;
                        }

                        if(manager.setPoints(questionNumber, i, points))
                            loadSeekBars();
                    }
                }

                @Override
                public void onStartTrackingTouch(BoxedVertical boxedPoints) { }
                @Override
                public void onStopTrackingTouch(BoxedVertical boxedPoints) { }
            });
        }

        loadSeekBars();

        if (sendInitialPOIOnCreate) {
            sendInitialPOIOnCreate = false;
            sendInitialPoi();
        }
    }

    public void updatePointsInformation(int pointsLeft) {
        ((TextView)getView().findViewById(R.id.points_information)).setText("Points left: " + pointsLeft);
    }

    public void loadSeekBars() {
        int[] points = manager.getPointsForQuestion(questionNumber);
        int leftPoints = manager.getPointsLeftForQuestion(questionNumber);

        for(int i = 0; i < MillionaireQuestion.MAX_ANSWERS; i++) {
            if(points[i] + leftPoints == 0) {
                charts[i].setMax(1);
                charts[i].setEnabled(false);
            } else {
                charts[i].setMax(points[i] + leftPoints);
                charts[i].setEnabled(!manager.isQuestionDisabled(questionNumber));
            }

            charts[i].setValue(points[i]);
        }

        updatePointsInformation(leftPoints);
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
