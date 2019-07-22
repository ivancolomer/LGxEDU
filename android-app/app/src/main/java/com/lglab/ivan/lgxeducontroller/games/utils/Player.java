package com.lglab.ivan.lgxeducontroller.games.utils;

import com.lglab.ivan.lgxeducontroller.games.Game;

public class Player {
    private String name;
    private Game game;

    public Player(String name, Game game) {
        this.name = name;
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
