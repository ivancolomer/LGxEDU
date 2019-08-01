package com.lglab.ivan.lgxeducontroller.games.geofinder.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.geofinder.GeoFinderManager;
import com.lglab.ivan.lgxeducontroller.games.geofinder.adapters.ResultsAdapter;

public class GeoFinderResultsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.activity_results_page, container, false);

        RecyclerView rv = view.findViewById(R.id.my_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        rv.setLayoutManager(llm);
        ResultsAdapter adapter = new ResultsAdapter();
        rv.setAdapter(adapter);

        ((TextView) view.findViewById(R.id.textViewScore)).setText("You have scored " + ((GeoFinderManager) GameManager.getInstance()).getTotalScore() + " out of " + GameManager.getInstance().getGame().getQuestions().size() * 1000 + "!");

        return view;
    }
}


