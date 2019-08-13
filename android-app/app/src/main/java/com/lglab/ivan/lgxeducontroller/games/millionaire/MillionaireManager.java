package com.lglab.ivan.lgxeducontroller.games.millionaire;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.millionaire.activities.MillionaireActivity;
import com.lglab.ivan.lgxeducontroller.games.millionaire.fragments.MillionaireQuestionEditFragment;
import com.lglab.ivan.lgxeducontroller.games.millionaire.interfaces.IAnswerListener;

public class MillionaireManager extends GameManager {

    private static final int STARTING_POINTS = 1000;
    private static final Class<?> GAME_ACTIVITY = MillionaireActivity.class;
    private static final Class<?> TRIVIA_EDIT_FRAGMENT = MillionaireQuestionEditFragment.class;

    private IAnswerListener listener;
    private int[][] pointsAssigned;

    MillionaireManager(Game game) {
        super(game);
        pointsAssigned = new int[game.getQuestions().size()][];
        for(int i = 0; i < pointsAssigned.length; i++) {
            pointsAssigned[i] = new int[MillionaireQuestion.MAX_ANSWERS];
        }
    }

    public void setListener(IAnswerListener listener) {
        this.listener = listener;
    }

    public int[] getPointsForQuestion(int questionId) {
        return pointsAssigned[questionId];
    }

    public int getPointsLeftForQuestion(int questionId) {
        int leftPoints = getPointsCorrectInQuestion(questionId - 1);
        for(int i = 0; i < MillionaireQuestion.MAX_ANSWERS; i++) {
            leftPoints -= pointsAssigned[questionId][i];
        }
        return leftPoints;
    }

    public int getPointsCorrectInQuestion(int questionId) {
        if(questionId < 0)
            return STARTING_POINTS;

        return pointsAssigned[questionId][((MillionaireQuestion)getGame().getQuestions().get(questionId)).correctAnswer - 1];
    }

    public boolean setPoints(int questionId, int answer, int points) {
        int diff = points - pointsAssigned[questionId][answer];

        if(diff == 0)
            return false;

        int pointsLeft = getPointsLeftForQuestion(questionId);

        pointsAssigned[questionId][answer] = diff <= pointsLeft ? points : pointsLeft;

        if(listener != null)
            listener.updateAnswer(questionId);
            return true;
    }

    @Override
    public Class<?> getGameActivity() {
        return GAME_ACTIVITY;
    }

    @Override
    public Class<?> getGameEditFragment() {
        return TRIVIA_EDIT_FRAGMENT;
    }
}
