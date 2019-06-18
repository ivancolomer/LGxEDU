package com.lglab.ivan.lgxeducontroller.activities_new.manager.asynctasks;

import android.os.AsyncTask;
import android.util.Log;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

public class UpdateGameTask extends AsyncTask<Void, Integer, Void> {
    public static final String TAG = UpdateGameTask.class.getSimpleName();
    private Game game;

    public UpdateGameTask(Game game) {
        this.game = game;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Log.i(TAG, "doInBackground: " + game.pack().toString());
            POIsProvider.updateGameById((int) game.getId(), game.pack().toString());
        } catch (Exception e) {
            cancel(true);
        }
        return null;
    }
}
