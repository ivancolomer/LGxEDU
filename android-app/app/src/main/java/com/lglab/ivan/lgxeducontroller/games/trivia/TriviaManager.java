package com.lglab.ivan.lgxeducontroller.games.trivia;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.multiplayer.ChoosePlayersActivity;
import com.lglab.ivan.lgxeducontroller.games.trivia.activities.TriviaActivity;
import com.lglab.ivan.lgxeducontroller.games.trivia.fragments.TriviaQuestionEditFragment;
import com.lglab.ivan.lgxeducontroller.interfaces.IAnswerListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TriviaManager extends GameManager {

    private static final Class<?> GAME_ACTIVITY = TriviaActivity.class;
    private static final Class<?> TRIVIA_EDIT_FRAGMENT = TriviaQuestionEditFragment.class;

    private Player[] players;
    private IAnswerListener listener;

    public TriviaManager(Game game) {
        super(game);
    }

    public void setListener(IAnswerListener listener) {
        this.listener = listener;
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
    public String[] getPlayerNames() {
        String[] names = new String[players.length];
        for(int i = 0; i < players.length; i++) {
            names[i] = ChoosePlayersActivity.getPlayerSubName(i, players[i].getName());
        }
        return names;
    }

    @Override
    public Class<?> getGameActivity() {
        return GAME_ACTIVITY;
    }

    @Override
    public Class<?> getGameEditFragment() {
        return TRIVIA_EDIT_FRAGMENT;
    }

    public void answerQuestion(int player, int question, int selectedAnswer) {
        players[player].selectedAnswers.set(question, selectedAnswer);
        if(listener != null)
            listener.updateAnswer(player, question, selectedAnswer);
    }

    public int getAnswerFromPlayer(int playerId, int questionId) {
        return players[playerId].selectedAnswers.get(questionId);
    }

    public boolean allPlayersHasAnswerQuestion(int i) {
        for (Player player : players) {
            if (!player.hasAnswer(i))
                return false;
        }
        return true;
    }

    public int[] correctAnsweredQuestionsCount() {
        int[] total = new int[players.length];
        for(int i = 0; i < players.length; i++) {
            total[i] = players[i].getCorrectAnswersCount();
        }
        return total;
    }

    public boolean isAnswerCorrect(int playerId, Integer index) {
        return players[playerId].isAnswerCorrect(index);
    }

    public Set<Integer> getWrongAnswers(int questionId) {
        HashSet<Integer> set = new HashSet<>();
        for(Player player : players)
            if(!player.isAnswerCorrect(questionId))
                set.add(player.selectedAnswers.get(questionId) - 1);
        return set;
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


        private int getCorrectAnswersCount() {
            int total = 0;
            for (int i = 0; i < game.getQuestions().size(); i++) {
                total += isAnswerCorrect(i) ? 1 : 0;
            }
            return total;
        }

        private boolean hasAnswer(int index) {
            return selectedAnswers.get(index) != 0;
        }

        private boolean isAnswerCorrect(int index) {
            return selectedAnswers.get(index) == ((TriviaQuestion)game.getQuestions().get(index)).correctAnswer;
        }
    }
}
