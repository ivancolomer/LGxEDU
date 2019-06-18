package com.lglab.ivan.lgxeducontroller.activities_new.manager.asynctasks;

import android.os.AsyncTask;
import android.util.Log;

import com.lglab.ivan.lgxeducontroller.games.Category;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

public class RemoveCategoryTask extends AsyncTask<Void, Integer, Void> {
    public static final String TAG = RemoveCategoryTask.class.getSimpleName();
    private Category category;

    public RemoveCategoryTask(Category category) {
        this.category = category;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Log.i(TAG, "doInBackground: deleting " + category.getTitle());
            POIsProvider.removeCategoryGameById((int) category.getId());
        } catch (Exception e) {
            cancel(true);
        }
        return null;
    }
}
