package com.lglab.ivan.lgxeducontroller.games;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.lglab.ivan.lgxeducontroller.games.geofinder.GeoFinder;
import com.lglab.ivan.lgxeducontroller.games.millionaire.Millionaire;
import com.lglab.ivan.lgxeducontroller.games.trivia.Trivia;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class GameManager {
    private static GameManager INSTANCE = null;

    public synchronized static GameManager getInstance() {
        return INSTANCE;
    }

    public static void startGame(Activity activity, Game game) throws IllegalStateException {
        if (INSTANCE != null)
            throw new IllegalStateException("There's already an instance of GameManager active!");

        GameManager gameManager = game.createManager();
        if (gameManager == null)
            throw new IllegalStateException("No GameManager found for that game!");

        INSTANCE = gameManager;
        INSTANCE.startGame(activity);
    }

    public static void editGame(Game game) throws IllegalStateException {
        if (INSTANCE != null)
            throw new IllegalStateException("There's already an instance of GameManager active!");

        GameManager gameManager = game.createManager();
        if (gameManager == null)
            throw new IllegalStateException("No GameManager found for that game!");

        INSTANCE = gameManager;
    }

    public static void endGame() throws IllegalStateException {
        if (INSTANCE == null)
            throw new IllegalStateException("There isn't any instance of GameManager active!");
        INSTANCE = null;
    }

    private static Game newGameByType(GameEnum gameEnum) {
        Game game = null;
        switch (gameEnum) {
            case TRIVIA:
                game = new Trivia();
                break;
            case GEOFINDER:
                game = new GeoFinder();
                break;
            case MILLIONAIRE:
                game = new Millionaire();
                break;
            /*case HANGMAN:
                break;*/
        }
        return game;
    }

    public static Game unpackGame(JSONObject obj) throws JSONException {
        GameEnum gameEnum = GameEnum.findByName(obj.getString("type"));
        if (gameEnum == null)
            throw new JSONException("No type game found");

        return newGameByType(gameEnum).unpack(obj);
    }

    public static Game unpackExternalGame(JSONObject obj, Context context) throws JSONException {
        GameEnum gameEnum = GameEnum.findByName(obj.getString("type"));
        if (gameEnum == null)
            throw new JSONException("No type game found");

        return newGameByType(gameEnum).unpack_external(obj, context);
    }

    public static Game createGame(String name, GameEnum type, Bitmap image, String category, Context context) {
        Game game = newGameByType(type);
        game.setName(name);
        game.setType(type);
        game.setNewImage(image, context);
        game.setCategory(category);
        return game;
    }

    private Game game;
    private boolean[] finishedAnsweringQuestions;

    public GameManager(Game game) {
        this.game = game;
        finishedAnsweringQuestions = new boolean[game.getQuestions().size()];
    }

    private void startGame(Activity activity) {
        Intent intent = new Intent(activity, getGameActivity());
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivity(intent);
    }

    public Game getGame() {
        return game;
    }

    public void disableQuestionFromAnswering(int question) {
        finishedAnsweringQuestions[question] = true;
    }

    public boolean isQuestionDisabled(int question) {
        return finishedAnsweringQuestions[question];
    }

    public abstract Class<?> getGameActivity();

    public abstract Class<?> getGameEditFragment();
}