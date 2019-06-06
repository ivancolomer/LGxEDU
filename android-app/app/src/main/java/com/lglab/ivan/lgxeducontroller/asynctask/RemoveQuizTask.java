package com.lglab.ivan.lgxeducontroller.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.lglab.ivan.lgxeducontroller.games.quiz.Quiz;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

public class RemoveQuizTask extends AsyncTask<Void, Integer, Void> {
    public static final String TAG = RemoveQuizTask.class.getSimpleName();
    private Quiz quiz;

    public RemoveQuizTask(Quiz quiz) {
        this.quiz = quiz;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Log.i(TAG, "doInBackground: deleting " + quiz.name);
            POIsProvider.removeQuizById((int) quiz.id);
        } catch (Exception e) {
            cancel(true);
        }
        return null;
    }
}
