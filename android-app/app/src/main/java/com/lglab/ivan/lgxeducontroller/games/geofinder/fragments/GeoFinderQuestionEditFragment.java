package com.lglab.ivan.lgxeducontroller.games.geofinder.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.manager.CreatePOIActivity;
import com.lglab.ivan.lgxeducontroller.activities.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.ISaveData;
import com.lglab.ivan.lgxeducontroller.games.geofinder.GeoFinder;
import com.lglab.ivan.lgxeducontroller.games.geofinder.GeoFinderQuestion;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import java.util.ArrayList;
import java.util.List;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;

import static android.app.Activity.RESULT_OK;

public class GeoFinderQuestionEditFragment extends Fragment implements ISaveData {

    private int questionNumber;
    private GeoFinder geofinder;
    private View view;
    private GeoFinderQuestion question;

    private List<POI> poiList;
    private ArrayAdapter<POI> poiStringList;

    private EditText questionEditText;
    private AutoCompleteTextView initialPOITextView, solutionPOITextView;
    private EditText areaEditText;
    private EditText additionalInformationEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ItemEntity<Integer> itemEntity = ItemEntityUtil.getModelData(this);
        questionNumber = itemEntity.getContent();
        geofinder = (GeoFinder) GameManager.getInstance().getGame();
        question = (GeoFinderQuestion) geofinder.getQuestions().get(questionNumber);

        poiStringList = new ArrayAdapter<>(getContext(), android.R.layout.select_dialog_item);
        getPOIStringsFromDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_create_geofinder_question, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        questionEditText = view.findViewById(R.id.questionTextEdit);

        initialPOITextView = view.findViewById(R.id.initialPOITextEdit);
        initialPOITextView.setAdapter(poiStringList);
        initialPOITextView.setOnItemClickListener((parent, view2, position, id) -> {
            POI poi = poiStringList.getItem(position);
            question.initialPOI = poi;
        });
        POIButton(R.id.initialPOIButton, 0);

        solutionPOITextView = view.findViewById(R.id.placeToBeSearchPOITextEdit);
        solutionPOITextView.setAdapter(poiStringList);
        solutionPOITextView.setOnItemClickListener((parent, view2, position, id) -> {
            POI poi = poiStringList.getItem(position);
            question.poi = poi;
        });
        POIButton(R.id.placeToBeSearchPOIButton, 1);

        areaEditText = view.findViewById(R.id.areaOfThePlace);
        additionalInformationEditText = view.findViewById(R.id.informationTextEdit);

        if(question.getQuestion() != null)
            questionEditText.setText(question.getQuestion());

        if (question.initialPOI != null)
            initialPOITextView.setText(question.initialPOI.getName());

        if (question.poi != null)
            solutionPOITextView.setText(question.poi.getName());

        if (question.area != 0.0d)
            areaEditText.setText(String.valueOf(question.area));

        if (question.information != null)
            additionalInformationEditText.setText(question.information);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveData();
    }

    private void getPOIStringsFromDatabase() {
        poiList = new ArrayList<>();
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
                poiList.add(newPOI);
            } catch (Exception e) {
                Log.e("BRUH", e.toString());
            }

        }
        poiCursor.close();
        for (POI poi : poiList) {
            poiStringList.add(poi);
        }
    }

    private void POIButton(int id, int resultCode) {
        view.findViewById(id).setOnClickListener(view -> {
            Intent createPoiIntent = new Intent(getContext(), CreatePOIActivity.class);
            createPoiIntent.putExtra("POI_BUTTON", resultCode);
            if(question != null && question.initialPOI != null)
                createPoiIntent.putExtra("POI", question.initialPOI);
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
            poiList.add(returnedPOI);
            poiStringList.add(returnedPOI);

            if (button == 0) {
                question.initialPOI = returnedPOI;
                initialPOITextView.setText(namePOI);
            } else if (button == 1) {
                question.poi = returnedPOI;
                solutionPOITextView.setText(namePOI);
            }
        }
    }

    private String getTextFromEditText(EditText editText) {
        String toReturn = editText.getText().toString();
        if (toReturn.isEmpty()) {
            return null;
        }
        return toReturn;
    }

    @Override
    public void saveData() {
        String questionS = getTextFromEditText(questionEditText);
        if (questionS == null || questionS.isEmpty()) {
            return;
        }

        if (question.initialPOI == null) {
            question.initialPOI = POIController.EARTH_POI;
        }

        question.setQuestion(questionS);
        try {
            question.area = Double.parseDouble(areaEditText.getText().toString());
        }
        catch(NumberFormatException e) {
            question.area = 0.0d;
        }
        question.information = additionalInformationEditText.getText().toString();
    }
}
