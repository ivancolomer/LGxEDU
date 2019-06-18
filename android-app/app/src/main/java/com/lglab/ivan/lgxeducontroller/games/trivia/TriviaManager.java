package com.lglab.ivan.lgxeducontroller.games.trivia;

import com.lglab.ivan.lgxeducontroller.games.trivia.activities.CreateTriviaQuestionActivity;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.activities.TriviaActivity;

import java.util.ArrayList;
import java.util.List;

public class TriviaManager extends GameManager {

    private static final Class<?> GAME_ACTIVITY = TriviaActivity.class;
    private static final Class<?> GAME_MANAGER_ACTIVITY = CreateTriviaQuestionActivity.class;

    private List<Integer> selectedAnswers;

    public TriviaManager(Game game) {
        super(game);
        selectedAnswers = new ArrayList<>();
        for(int i = 0; i < getGame().getQuestions().size(); i++)
            selectedAnswers.add(0);
    }

    @Override
    public Class<?> getGameActivity() {
        return GAME_ACTIVITY;
    }

    @Override
    public Class<?> getManagerGameActivity() {
        return GAME_MANAGER_ACTIVITY;
    }

    public boolean hasAnsweredAllQuestions() {
        for (int answer : selectedAnswers) {
            if (answer == 0) return false;
        }
        return true;
    }

    public boolean hasAnsweredQuestion(int i) {
        return selectedAnswers.get(i) != 0;
    }

    public void answerQuestion(int i, int selectedAnswer) {
        selectedAnswers.set(i, selectedAnswer);
    }

    public int getAnswerIdOfQuestion(int i) {
        return selectedAnswers.get(i);
    }

    public int correctAnsweredQuestionsCount() {
        int total = 0;
        for (int i = 0; i < getGame().getQuestions().size(); i++) {
            total += isCorrectAnswer(i) ? 1 : 0;
        }
        return total;
    }

    public boolean isCorrectAnswer(Integer index) {
        return selectedAnswers.get(index) == ((TriviaQuestion)getGame().getQuestions().get(index)).correctAnswer;
    }
}
