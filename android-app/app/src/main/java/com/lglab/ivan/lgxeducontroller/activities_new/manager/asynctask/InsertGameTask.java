package com.lglab.ivan.lgxeducontroller.activities_new.manager.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

public class InsertGameTask extends AsyncTask<Void, Integer, Void> {
    public static final String TAG = InsertGameTask.class.getSimpleName();
    private Game game;

    public InsertGameTask(Game game) {
        this.game = game;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Log.i(TAG, "doInBackground: " + game.pack().toString());
            POIsProvider.insertQuiz(game.pack().toString());
        } catch (Exception e) {
            cancel(true);
        }
        return null;
    }
}
