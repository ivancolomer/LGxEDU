package com.lglab.ivan.lgxeducontroller.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.lglab.ivan.lgxeducontroller.games.quiz.Quiz;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

public class InsertQuizTask extends AsyncTask<Void, Integer, Void> {
    public static final String TAG = InsertQuizTask.class.getSimpleName();
    private Quiz quiz;

    public InsertQuizTask(Quiz quiz) {
        this.quiz = quiz;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Log.i(TAG, "doInBackground: " + quiz.pack().toString());
            POIsProvider.insertQuiz(quiz.pack().toString());
        } catch (Exception e) {
            cancel(true);
        }
        return null;
    }
}
