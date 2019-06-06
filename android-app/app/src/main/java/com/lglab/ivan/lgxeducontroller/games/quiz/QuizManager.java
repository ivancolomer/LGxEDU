package com.lglab.ivan.lgxeducontroller.games.quiz;

public class QuizManager {

    private static QuizManager instance = null;
    private Quiz quiz;

    private QuizManager() {
    }

    public static QuizManager getInstance() {
        if (instance == null)
            instance = new QuizManager();
        return instance;
    }

    public void startQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public boolean hasAnsweredAllQuestions() {
        for (Question question : quiz.questions) {
            if (question.selectedAnswer == 0) return false;
        }
        return true;
    }

    public int correctAnsweredQuestionsCount() {
        int total = 0;
        for (Question question : quiz.questions) {
            total += isCorrectAnswer(question) ? 1 : 0;
        }
        return total;
    }

    private boolean isCorrectAnswer(Question question) {
        return question.selectedAnswer == question.correctAnswer;
    }
}
