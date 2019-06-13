package com.lglab.ivan.lgxeducontroller.games;

public class GameManager {
    private static GameManager INSTANCE = null;

    public synchronized static final GameManager getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new GameManager();
        }
        return INSTANCE;
    }

}