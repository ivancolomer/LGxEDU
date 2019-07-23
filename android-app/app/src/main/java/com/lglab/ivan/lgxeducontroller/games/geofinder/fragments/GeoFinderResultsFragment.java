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
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;

public class GeoFinderResultsFragment extends Fragment {

    private int playerId = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments() != null) {
            playerId = getArguments().getInt("playerId");
        }

        View view = inflater.inflate(R.layout.activity_results_page, container, false);

        RecyclerView rv = view.findViewById(R.id.my_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        rv.setLayoutManager(llm);

        ((TextView) view.findViewById(R.id.textViewScore)).setText("You have scored " + ((TriviaManager) GameManager.getInstance()).correctAnsweredQuestionsCount()[playerId] + " out of " + GameManager.getInstance().getGame().getQuestions().size() + "!");

        return view;
    }
}


