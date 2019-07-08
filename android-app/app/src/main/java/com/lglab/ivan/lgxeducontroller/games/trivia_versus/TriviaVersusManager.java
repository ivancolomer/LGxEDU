package com.lglab.ivan.lgxeducontroller.games.trivia_versus;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;

import java.util.List;

public class TriviaVersusManager extends TriviaManager {

    private List<Integer> selectedAnswers;

    private Player[] players;

    public TriviaVersusManager(Game game) {
        super(game);
    }


    private static class Player {
        private String name;

        Player(String name) {
            this.name = name;
        }
    }
}
