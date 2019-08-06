package com.lglab.ivan.lgxeducontroller.games.millionaire;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.millionaire.fragments.MillionaireQuestionEditFragment;
import com.lglab.ivan.lgxeducontroller.games.millionaire.activities.MillionaireActivity;

public class MillionaireManager extends GameManager {

    private static final Class<?> GAME_ACTIVITY = MillionaireActivity.class;
    private static final Class<?> TRIVIA_EDIT_FRAGMENT = MillionaireQuestionEditFragment.class;

    private int[][] pointsAssigned;
    private int currentLeftPoints;

    public MillionaireManager(Game game) {
        super(game);
        pointsAssigned = new int[game.getQuestions().size()][];
        for(int i = 0; i < pointsAssigned.length; i++) {
            pointsAssigned[i] = new int[MillionaireQuestion.MAX_ANSWERS];
        }
        currentLeftPoints = 1000;
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
