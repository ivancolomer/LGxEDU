package com.lglab.ivan.lgxeducontroller.games.geofinder;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.geofinder.activities.GeoFinderActivity;
import com.lglab.ivan.lgxeducontroller.games.geofinder.fragments.GeoFinderQuestionEditFragment;

public class GeoFinderManager extends GameManager {

    private static final Class<?> GAME_ACTIVITY = GeoFinderActivity.class;
    private static final Class<?> TRIVIA_EDIT_FRAGMENT = GeoFinderQuestionEditFragment.class;

    private LatLon[] answers;
    private int[] scores;

    public GeoFinderManager(Game game) {
        super(game);
        answers = new LatLon[game.getQuestions().size()];
        scores = new int[game.getQuestions().size()];
    }

    @Override
    public Class<?> getGameActivity() {
        return GAME_ACTIVITY;
    }

    public boolean hasAnsweredQuestion(int questionId) {
        return answers[questionId] != null;
    }

    public void answerQuestion(int questionId, double lat, double lon) {
        if(answers[questionId] == null) {
            answers[questionId] = new LatLon(lat, lon);
            scoreQuestion(questionId);
        }
    }

    public int getScoreQuestion(int questionId) {
        return scores[questionId];
    }

    public int getTotalScore() {
        int sum = 0;
        for(int score : scores) {
            sum += score;
        }
        return sum;
    }



    private int scoreQuestion(int questionId) {
        GeoFinderQuestion question = ((GeoFinderQuestion)getGame().getQuestions().get(questionId));
        double radiusAnswer = getCircleRadiusFromArea(question.area);
        double distance = calculateDistance(answers[questionId], new LatLon(question.poi.getLatitude(), question.poi.getLongitude()));
        return distance <= (3*radiusAnswer/4) ? 1000 : (int)Math.round(1000 * radiusAnswer / (distance - (radiusAnswer/4)));
    }

    private final static double R = 6371e3;
    private static double calculateDistance(LatLon p1, LatLon p2) {
        double deltaLat = (p2.lat - p1.lat) * Math.PI / 180;
        double deltaLon = (p2.lon - p1.lon) * Math.PI / 180;
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(p1.lat * Math.PI / 180) * Math.cos(p2.lat * Math.PI / 180) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static double getCircleRadiusFromArea(double area) {
        return Math.sqrt(area / Math.PI);
    }

    @Override
    public Class<?> getGameEditFragment() {
        return TRIVIA_EDIT_FRAGMENT;
    }

    static class LatLon {
        private double lat, lon;
        LatLon(double lat, double lon) {
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
