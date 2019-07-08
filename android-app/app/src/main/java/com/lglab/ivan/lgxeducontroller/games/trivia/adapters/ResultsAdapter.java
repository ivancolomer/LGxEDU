package com.lglab.ivan.lgxeducontroller.games.trivia.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaQuestion;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultsViewHolder> {

    public ResultsAdapter() {

    }

    @Override
    public ResultsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_results, viewGroup, false);
        return new ResultsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ResultsViewHolder personViewHolder, int i) {
        TriviaQuestion question = (TriviaQuestion) GameManager.getInstance().getGame().getQuestions().get(i);

        personViewHolder.questionName.setText(question.getQuestion());
        personViewHolder.questionNumber.setText(String.valueOf(i + 1));
        personViewHolder.questionSolution.setText(question.answers[question.correctAnswer - 1]);

        if (((TriviaManager) GameManager.getInstance()).isCorrectAnswer(i)) {
            personViewHolder.rl.setBackgroundColor(Color.parseColor("#388E3C"));
        } else {
            personViewHolder.rl.setBackgroundColor(Color.parseColor("#C62828"));
        }
    }

    @Override
    public int getItemCount() {
        return GameManager.getInstance().getGame().getQuestions().size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    static class ResultsViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView questionNumber;
        TextView questionName;
        TextView questionSolution;
        DynamicSquareLayout rl;

        ResultsViewHolder(View itemView) {
            super(itemView);

            cv = itemView.findViewById(R.id.cv);
            questionName = itemView.findViewById(R.id.question_name);
            questionNumber = itemView.findViewById(R.id.question_number);
            questionSolution = itemView.findViewById(R.id.question_solution);
            rl = itemView.findViewById(R.id.number_question);
        }
    }
}
