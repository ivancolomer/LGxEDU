package com.lglab.ivan.lgxeducontroller.games.utils;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.utils.multiplayer.ChoosePlayersActivity;

public abstract class MultiplayerManagerGame extends GameManager {

    private static final Class<?> GAME_ACTIVITY = ChoosePlayersActivity.class;

    private Player[] players;

    public MultiplayerManagerGame(Game game) {
        super(game);
    }

    public void setPlayers(String[] names) {
        players = new Player[names.length];
        for(int i = 0; i < names.length; i++) {
            players[i] = createEmptyPlayer(getGame());
            players[i].setName(names[i]);
        }
    }

    public int getPlayersCount() {
        return players.length;
    }

    public String[] getPlayerNames() {
        String[] names = new String[players.length];
        for(int i = 0; i < players.length; i++) {
            names[i] = ChoosePlayersActivity.getPlayerSubName(i, players[i].getName());
        }
        return names;
    }

    protected abstract Player createEmptyPlayer(Game game);

    public Player[] getPlayers() {
        return players;
    }

    public Class<?> getGameActivity() {
        return GAME_ACTIVITY;
    }
}

