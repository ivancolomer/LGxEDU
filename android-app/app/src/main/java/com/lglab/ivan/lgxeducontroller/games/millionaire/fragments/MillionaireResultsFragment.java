package com.lglab.ivan.lgxeducontroller.games.millionaire.fragments;

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
import com.lglab.ivan.lgxeducontroller.games.millionaire.MillionaireManager;
import com.lglab.ivan.lgxeducontroller.games.millionaire.adapters.ResultsAdapter;

public class MillionaireResultsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_results_page, container, false);

        RecyclerView rv = view.findViewById(R.id.my_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        rv.setLayoutManager(llm);
        ResultsAdapter adapter = new ResultsAdapter();
        rv.setAdapter(adapter);

        int pointsLeftLastQuestion = ((MillionaireManager) GameManager.getInstance()).getPointsCorrectInQuestion(GameManager.getInstance().getGame().getQuestions().size() - 1);

        if(pointsLeftLastQuestion > 0) {
            ((TextView) view.findViewById(R.id.textViewScore)).setText("Congratulations!!! You have passed the game with " + pointsLeftLastQuestion + " points!");
        } else {
            int lastQuestionWithPositivePoints = 0;
            int pointsLeft = 0;

            for(int i = 0; i < GameManager.getInstance().getGame().getQuestions().size(); i++) {
                if(((MillionaireManager) GameManager.getInstance()).getPointsCorrectInQuestion(i) == 0)
                    break;
                pointsLeft = ((MillionaireManager) GameManager.getInstance()).getPointsCorrectInQuestion(i);
                lastQuestionWithPositivePoints = i + 1;
            }

            if(lastQuestionWithPositivePoints == 0) {
                ((TextView) view.findViewById(R.id.textViewScore)).setText("Oops! You haven't passed the first question!");
            } else {
                ((TextView) view.findViewById(R.id.textViewScore)).setText("Oops! You haven't passed the game :(\nYou have reached the question " + lastQuestionWithPositivePoints + " with " + pointsLeft + " points!");
            }
        }



        return view;
    }
}


