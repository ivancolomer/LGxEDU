package com.lglab.ivan.lgxeducontroller.activities_new.play.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.GoogleDriveActivity;
import com.lglab.ivan.lgxeducontroller.activities.QuizActivity;
import com.lglab.ivan.lgxeducontroller.activities_new.play.asynctask.DownloadImageTask;
import com.lglab.ivan.lgxeducontroller.games.quiz.Quiz;
import com.lglab.ivan.lgxeducontroller.games.quiz.QuizManager;

import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {


    private List<Quiz> quizList;
    private Context context;

    public QuizAdapter(List<Quiz> list, Context context) {
        this.quizList = list;
        this.context = context;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new QuizViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_play_inner_row_new, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final QuizViewHolder holder, final int position) {
        Quiz quiz = quizList.get(position);

        holder.textViewTitle.setText(quiz.name);
        holder.textViewGenre.setText("the type of the quiz");

        new DownloadImageTask(holder.imageViewMovie).execute("https://i.ytimg.com/vi/ymIhLJ5AKpE/maxresdefault.jpg");

        holder.itemView.setOnClickListener(arg0 -> startQuiz((GoogleDriveActivity) holder.itemView.getContext(), position));
        /*Picasso.with(context).
                load(context.getResources().getString(R.string.image_url) + movie.getPoster())
                .into(holder.imageViewMovie);*/
    }

    private void startQuiz(GoogleDriveActivity activity, final int position) {
        QuizManager.getInstance().startQuiz(quizList.get(position));

        Intent intent = new Intent(activity, QuizActivity.class);
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    class QuizViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewTitle;
        private TextView textViewGenre;
        private ImageView imageViewMovie;

        QuizViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.quiz_name);
            textViewGenre = itemView.findViewById(R.id.quiz_type);
            imageViewMovie = itemView.findViewById(R.id.image_view_quiz);

        }
    }
}
