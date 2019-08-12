package com.lglab.ivan.lgxeducontroller.utils;

import android.database.Cursor;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.Trivia;
import com.lglab.ivan.lgxeducontroller.games.utils.multiplayer.ChoosePlayersActivity;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

public class AssistantHandler implements IAssistantHandler {

    private FragmentActivity fragmentActivity;

    enum Result {
        OK, GAME_NOT_FOUND, POI_NOT_FOUND, PATH_NOT_FOUND, NOT_IN_ACTIVITY, IS_MULTIPLAYER_GAME
    }

    AssistantHandler(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
    }

    private void getGames(Map<String, Game> games) {
        Cursor game_cursor = POIsProvider.getAllGames();
        while (game_cursor.moveToNext()) {
            long gameId = game_cursor.getLong(game_cursor.getColumnIndexOrThrow("_id"));
            String questData = game_cursor.getString(game_cursor.getColumnIndexOrThrow("Data"));
            try {
                Game newGame = GameManager.unpackGame(new JSONObject(questData));
                newGame.setId(gameId);
                games.put(newGame.getName(), newGame);
            } catch (JSONException e) {
                Log.e("WEBSERVER", e.toString());
            }
        }
        game_cursor.close();
    }

    @Override
    public Result handleNewResponse(NanoHTTPD.Method method, String[] uri, Map<String, List<String>> params) {
        if(uri[1].equals("play")) {
            if(uri.length > 2) {
                if(uri[2].equals("game")) {
                    if(uri.length > 3) {
                        Map<String, Game> games = new HashMap<>();
                        getGames(games);
                        ExtractedResult result = FuzzySearch.extractOne(uri[3], games.keySet());
                        Log.d("WEBSERVER", result.toString());
                        if(result.getScore() >= 80) {
                            Game game = games.get(result.getString());
                            GameManager.startGame(fragmentActivity, game);
                            if(game instanceof Trivia)
                                return Result.IS_MULTIPLAYER_GAME;
                            return Result.OK;
                        }
                    }
                    return Result.GAME_NOT_FOUND;
                }
            }
        } else if(uri[1].equals("player")) {
            if(uri.length > 2) {
                if (fragmentActivity instanceof ChoosePlayersActivity) {
                    ChoosePlayersActivity activity = (ChoosePlayersActivity) fragmentActivity;
                    final AssistantHandler.Result[] result = {Result.OK};

                    BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable( fragmentActivity, () -> {
                        // Execute your activity code here
                        if (activity.addNewPlayerName(uri[2])) {
                            result[0] = Result.IS_MULTIPLAYER_GAME;
                        } else {
                            result[0] = Result.OK;
                        }
                    });

                    actionRunnable.startOnUiAndWait();



                    return result[0];
                }
                return Result.PATH_NOT_FOUND;
            }

            return (fragmentActivity instanceof ChoosePlayersActivity) ? Result.IS_MULTIPLAYER_GAME : Result.PATH_NOT_FOUND;
        }
        return Result.PATH_NOT_FOUND;
    }

    @Override
    public void onServerCreated(String ip, String port) {
        fragmentActivity.runOnUiThread(() -> {
            AlertDialog alert = new MaterialAlertDialogBuilder(fragmentActivity)
                    .setTitle("Google Assistant Configuration")
                    .setMessage("Configure the Google Assistant with the following url:\n" + ip + ":" + port)
                    .setPositiveButton(fragmentActivity.getString(R.string.close), (dialog, id) -> dialog.dismiss())
                    .create();

            alert.setCanceledOnTouchOutside(false);
            alert.setCancelable(false);

            alert.show();
        });
    }
}
