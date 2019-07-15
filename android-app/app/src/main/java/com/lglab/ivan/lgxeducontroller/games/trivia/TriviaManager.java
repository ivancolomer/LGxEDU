package com.lglab.ivan.lgxeducontroller.games.trivia;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.activities.TriviaActivity;
import com.lglab.ivan.lgxeducontroller.games.trivia.fragments.TriviaQuestionEditFragment;

import java.util.ArrayList;
import java.util.List;

public class TriviaManager extends GameManager {

    private static final Class<?> GAME_ACTIVITY = TriviaActivity.class;
    private static final Class<?> TRIVIA_EDIT_FRAGMENT = TriviaQuestionEditFragment.class;

    private Player[] players;

    public TriviaManager(Game game) {
        super(game);
    }

    @Override
    public void setPlayers(String[] names) {
        players = new Player[names.length];
        for(int i = 0; i < names.length; i++) {
            players[i] = new Player(getGame());
            players[i].setName(names[i]);
        }
    }

    @Override
    public int getPlayersCount() {
        return players.length;
    }

    @Override
    public Class<?> getGameActivity() {
        return GAME_ACTIVITY;
    }

    @Override
    public Class<?> getGameEditFragment() {
        return TRIVIA_EDIT_FRAGMENT;
    }

    public boolean hasAnsweredAllQuestions() {
        for (Player player : players) {
            if (!player.hasAnsweredAllQuestions())
                return false;
        }
        return true;
    }

    public boolean hasAnsweredQuestion(int player, int i) {
        return players[player].selectedAnswers.get(i) != 0;
    }

    public void answerQuestion(int player, int i, int selectedAnswer) {
        players[player].selectedAnswers.set(i, selectedAnswer);
    }

    public int getAnswerIdOfQuestion(int player, int i) {
        return players[player].selectedAnswers.get(i);
    }

    public int[] correctAnsweredQuestionsCount() {
        int[] total = new int[players.length];
        for(int i = 0; i < players.length; i++) {
            total[i] = players[i].correctAnsweredQuestionsCount();
        }
        return total;
    }

    public boolean isCorrectAnswer(int playerId, Integer index) {
        return players[playerId].isCorrectAnswer(index);
    }

    public String getPlayerName(int playerId) {
        return players[playerId].getName();
    }

    public static class Player {
        private String name;
        private Game game;
        private List<Integer> selectedAnswers;

        Player(Game game) {
            this.name = "";
            this.game = game;
            selectedAnswers = new ArrayList<>();
            for (int i = 0; i < game.getQuestions().size(); i++)
                selectedAnswers.add(0);
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean hasAnsweredAllQuestions() {
            for (int answer : selectedAnswers) {
                if (answer == 0) return false;
            }
            return true;
        }

        public int correctAnsweredQuestionsCount() {
            int total = 0;
            for (int i = 0; i < game.getQuestions().size(); i++) {
                total += isCorrectAnswer(i) ? 1 : 0;
            }
            return total;
        }

        public boolean isCorrectAnswer(Integer index) {
            return selectedAnswers.get(index) == ((TriviaQuestion) game.getQuestions().get(index)).correctAnswer;
        }
    }
}
