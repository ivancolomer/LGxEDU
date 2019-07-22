package com.lglab.ivan.lgxeducontroller.games.trivia;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.trivia.activities.TriviaActivity;
import com.lglab.ivan.lgxeducontroller.games.trivia.fragments.TriviaQuestionEditFragment;
import com.lglab.ivan.lgxeducontroller.games.utils.MultiplayerManagerGame;
import com.lglab.ivan.lgxeducontroller.games.utils.Player;
import com.lglab.ivan.lgxeducontroller.games.trivia.interfaces.IAnswerListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TriviaManager extends MultiplayerManagerGame {

    private static final Class<?> GAME_ACTIVITY = TriviaActivity.class;
    private static final Class<?> TRIVIA_EDIT_FRAGMENT = TriviaQuestionEditFragment.class;

    private IAnswerListener listener;

    public TriviaManager(Game game) {
        super(game);
    }

    public void setListener(IAnswerListener listener) {
        this.listener = listener;
    }

    @Override
    protected com.lglab.ivan.lgxeducontroller.games.utils.Player createEmptyPlayer(Game game) {
        return new TriviaPlayer(getGame());
    }

    @Override
    public Class<?> getGameActivity() {
        if(getPlayers() == null)
            return super.getGameActivity();

        return GAME_ACTIVITY;
    }

    @Override
    public Class<?> getGameEditFragment() {
        return TRIVIA_EDIT_FRAGMENT;
    }

    public void answerQuestion(int playerId, int question, int selectedAnswer) {
        ((TriviaPlayer)getPlayers()[playerId]).selectedAnswers.set(question, selectedAnswer);
        if(listener != null)
            listener.updateAnswer(playerId, question, selectedAnswer);
    }

    public int getAnswerFromPlayer(int playerId, int questionId) {
        return ((TriviaPlayer)getPlayers()[playerId]).selectedAnswers.get(questionId);
    }

    public boolean allPlayersHasAnswerQuestion(int i) {
        for (Player player : getPlayers()) {
            if (!((TriviaPlayer)player).hasAnswer(i))
                return false;
        }
        return true;
    }

    public int[] correctAnsweredQuestionsCount() {
        int[] total = new int[getPlayers().length];
        for(int i = 0; i < getPlayers().length; i++) {
            total[i] = ((TriviaPlayer)getPlayers()[i]).getCorrectAnswersCount();
        }
        return total;
    }

    public boolean isAnswerCorrect(int playerId, Integer index) {
        return ((TriviaPlayer)getPlayers()[playerId]).isAnswerCorrect(index);
    }

    public Set<Integer> getWrongAnswers(int questionId) {
        HashSet<Integer> set = new HashSet<>();
        for(Player player : getPlayers())
            if(!((TriviaPlayer)player).isAnswerCorrect(questionId))
                set.add(((TriviaPlayer)player).selectedAnswers.get(questionId) - 1);
        return set;
    }

    public String getPlayerName(int playerId) {
        return getPlayers()[playerId].getName();
    }

    public static class TriviaPlayer extends Player {
        private List<Integer> selectedAnswers;

        public TriviaPlayer(Game game) {
            super("", game);

            selectedAnswers = new ArrayList<>();
            for (int i = 0; i < game.getQuestions().size(); i++)
                selectedAnswers.add(0);
        }

        private int getCorrectAnswersCount() {
            int total = 0;
            for (int i = 0; i < getGame().getQuestions().size(); i++) {
                total += isAnswerCorrect(i) ? 1 : 0;
            }
            return total;
        }

        private boolean hasAnswer(int index) {
            return selectedAnswers.get(index) != 0;
        }

        private boolean isAnswerCorrect(int index) {
            return selectedAnswers.get(index) == ((TriviaQuestion)getGame().getQuestions().get(index)).correctAnswer;
        }
    }
}
