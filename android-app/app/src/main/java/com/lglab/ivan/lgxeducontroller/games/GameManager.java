package com.lglab.ivan.lgxeducontroller.games;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.lglab.ivan.lgxeducontroller.games.trivia.Trivia;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.activities.TriviaActivity;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class GameManager {
    private static GameManager INSTANCE = null;

    public synchronized static GameManager getInstance() {
        return INSTANCE;
    }

    public static void startGame(Activity activity, Game game) throws IllegalStateException {
        if(INSTANCE != null)
            throw new IllegalStateException("There's already an instance of GameManager active!");

        GameManager gameManager = getGameManagerByGame(game);
        if(gameManager == null)
            throw new IllegalStateException("No GameManager found for that game!");

        INSTANCE = gameManager;
        INSTANCE.startGame(activity);
    }

    public static void endGame() throws IllegalStateException {
        if(INSTANCE == null)
            throw new IllegalStateException("There isn't any instance of GameManager active!");
        INSTANCE = null;
    }

    private static GameManager getGameManagerByGame(Game game) {
        switch(game.getType()) {
            case TRIVIA:
                return new TriviaManager(game);
            case VERSUS_TRIVIA:
                break;
            case MILLIONAIRE:
                break;
            case FIND_LOCATION:
                break;
            case HANGMAN:
                break;
        }

        return null;
    }

    public static Game unpackGame(JSONObject obj) throws JSONException {
        GameEnum gameEnum = GameEnum.findByName(obj.getString("type"));
        if(gameEnum == null)
            throw new JSONException("No type game found");

        Game game = null;
        switch(gameEnum) {
            case TRIVIA:
                game = new Trivia().unpack(obj);
            case VERSUS_TRIVIA:
                break;
            case MILLIONAIRE:
                break;
            case FIND_LOCATION:
                break;
            case HANGMAN:
                break;
        }
        return game;
    }

    private Game game;
    private boolean hasStarted = false;

    public GameManager(Game game) {
        this.game = game;
    }

    private void startGame(Activity activity) {
        setHasStarted();

        Intent intent = new Intent(activity, getGameActivity());
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivity(intent);
    }

    public Game getGame() {
        return game;
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public void setHasStarted() {
        this.hasStarted = true;
    }

    public abstract Class<?> getGameActivity();
}