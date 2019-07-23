package com.lglab.ivan.lgxeducontroller.games.geofinder;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.geofinder.activities.GeoFinderActivity;
import com.lglab.ivan.lgxeducontroller.games.geofinder.fragments.GeoFinderQuestionEditFragment;

public class GeoFinderManager extends GameManager {

    private static final Class<?> GAME_ACTIVITY = GeoFinderActivity.class;
    private static final Class<?> TRIVIA_EDIT_FRAGMENT = GeoFinderQuestionEditFragment.class;

    private LatLon[] answers;

    public GeoFinderManager(Game game) {
        super(game);
        answers = new LatLon[game.getQuestions().size()];
    }

    @Override
    public Class<?> getGameActivity() {
        return GAME_ACTIVITY;
    }

    public boolean hasAnsweredQuestion(int questionId) {
        return answers[questionId] != null;
    }

    public void answerQuestion(int questionId, double lat, double lon) {
        if(answers[questionId] == null)
            answers[questionId] = new LatLon(lat, lon);
    }

    @Override
    public Class<?> getGameEditFragment() {
        return TRIVIA_EDIT_FRAGMENT;
    }

    static class LatLon {
        private double lat, lon;
        public LatLon(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }
    }
}
