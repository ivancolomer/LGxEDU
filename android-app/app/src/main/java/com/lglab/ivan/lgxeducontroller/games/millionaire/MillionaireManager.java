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
    private int currentLeftPoints;

    public MillionaireManager(Game game) {
        super(game);
        pointsAssigned = new int[game.getQuestions().size()][];
        for(int i = 0; i < pointsAssigned.length; i++) {
            pointsAssigned[i] = new int[MillionaireQuestion.MAX_ANSWERS];
        }
        currentLeftPoints = STARTING_POINTS;
    }

    public void setListener(IAnswerListener listener) {
        this.listener = listener;
    }

    public int[] getPointsForQuestion(int questionId) {
        return pointsAssigned[questionId];
    }

    public int getPointsLeftForQuestion(int questionId) {
        int leftPoints = questionId == 0 ? STARTING_POINTS : pointsAssigned[questionId - 1][((MillionaireQuestion)getGame().getQuestions().get(questionId)).correctAnswer - 1];
        for(int i = 0; i < MillionaireQuestion.MAX_ANSWERS; i++) {
            leftPoints -= pointsAssigned[questionId][i];
        }
        return leftPoints;
    }

    public boolean setPoints(int questionId, int answer, int points) {
        int diff = points - pointsAssigned[questionId][answer];
        int pointsLeft = getPointsLeftForQuestion(questionId);
        if(diff != 0) {
            if (diff <= pointsLeft) {
                pointsAssigned[questionId][answer] = points;
                if(listener != null)
                    listener.updateAnswer(questionId);
                return true;
            }

            pointsAssigned[questionId][answer] = pointsLeft;
            if(listener != null)
                listener.updateAnswer(questionId);
            return true;
        }

        return false;
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
