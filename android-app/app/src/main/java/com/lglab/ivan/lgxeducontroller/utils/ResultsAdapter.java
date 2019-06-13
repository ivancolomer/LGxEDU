package com.lglab.ivan.lgxeducontroller.utils;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.trivia.Question;
import com.lglab.ivan.lgxeducontroller.games.trivia.QuizManager;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultsViewHolder> {

    public ResultsAdapter() {

    }

    @Override
    public ResultsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_results, viewGroup, false);
        ResultsViewHolder pvh = new ResultsViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(ResultsViewHolder personViewHolder, int i) {
        Question question = QuizManager.getInstance().getQuiz().questions.get(i);

        personViewHolder.questionName.setText(question.question);
        personViewHolder.questionNumber.setText(String.valueOf(i + 1));
        personViewHolder.questionSolution.setText(question.answers[question.correctAnswer - 1]);

        if (question.selectedAnswer == question.correctAnswer) {
            personViewHolder.rl.setBackgroundColor(Color.parseColor("#5cd65c"));
        } else {
            personViewHolder.rl.setBackgroundColor(Color.parseColor("#ff3333"));
        }
    }

    @Override
    public int getItemCount() {
        return QuizManager.getInstance().getQuiz().questions.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class ResultsViewHolder extends RecyclerView.ViewHolder {
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
