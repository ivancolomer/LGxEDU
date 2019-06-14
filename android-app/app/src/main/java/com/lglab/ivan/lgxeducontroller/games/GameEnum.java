package com.lglab.ivan.lgxeducontroller.games;

public enum GameEnum {
    TRIVIA, VERSUS_TRIVIA, MILLIONAIRE, HANGMAN, FIND_LOCATION;

    public static GameEnum findByName(String name) {
        try {
            return GameEnum.valueOf(name);
        }
        catch(IllegalArgumentException ignored) {

        }
        return null;
    }
}
