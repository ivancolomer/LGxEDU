package com.lglab.ivan.lgxeducontroller.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

public class RemoveGameTask extends AsyncTask<Void, Integer, Void> {
    public static final String TAG = RemoveGameTask.class.getSimpleName();
    private Game game;

    public RemoveGameTask(Game game) {
        this.game = game;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Log.i(TAG, "doInBackground: deleting " + game.getName());
            POIsProvider.removeQuizById((int) game.getId());
        } catch (Exception e) {
            cancel(true);
        }
        return null;
    }
}
