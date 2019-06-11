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

/**
 * Created by enyason on 10/4/18.
 */

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {


    private List<Quiz> movieList;
    private Context context;

    public QuizAdapter(List<Quiz> list, Context context) {
        this.movieList = list;
        this.context = context;


    }


    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new QuizViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_play_inner_row_new, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final QuizViewHolder holder, final int position) {

        Quiz movie = movieList.get(position);


        holder.textViewTitle.setText(movie.name);
        holder.textViewGenre.setText(movie.category);

        new DownloadImageTask(holder.imageViewMovie).execute("https://i.ytimg.com/vi/ymIhLJ5AKpE/maxresdefault.jpg");

        holder.itemView.setOnClickListener(arg0 -> startQuiz((GoogleDriveActivity) holder.itemView.getContext(), position));
        /*Picasso.with(context).
                load(context.getResources().getString(R.string.image_url) + movie.getPoster())
                .into(holder.imageViewMovie);*/


    }

    private void startQuiz(GoogleDriveActivity activity, final int position) {
        QuizManager.getInstance().startQuiz(movieList.get(position));

        Intent intent = new Intent(activity, QuizActivity.class);
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivity(intent);
    }

    @Override
    public int getItemCount() {

        return movieList.size();

    }


    public class QuizViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewTitle;
        private TextView textViewGenre;
        private ImageView imageViewMovie;


        public QuizViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.quiz_name);
            textViewGenre = itemView.findViewById(R.id.quiz_type);
            imageViewMovie = itemView.findViewById(R.id.image_view_quiz);

        }
    }
}
