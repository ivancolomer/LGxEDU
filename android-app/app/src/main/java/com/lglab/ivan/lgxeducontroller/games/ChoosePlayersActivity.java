package com.lglab.ivan.lgxeducontroller.games;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.lglab.ivan.lgxeducontroller.R;

public class ChoosePlayersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.choose_players_activity);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(GameManager.getInstance().getGame().getName());

        findViewById(R.id.playersCount1).setOnClickListener(view -> enterGame(1));
        findViewById(R.id.playersCount2).setOnClickListener(view -> enterGame(2));
        findViewById(R.id.playersCount3).setOnClickListener(view -> enterGame(3));
        findViewById(R.id.playersCount4).setOnClickListener(view -> enterGame(4));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        GameManager.getInstance().endGame();
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
            return onSupportNavigateUp();

        return super.onKeyDown(keyCode, event);
    }

    public void enterGame(int playersCount) {
        GameManager.getInstance().setPlayers(playersCount);

        Intent i = new Intent(this, GameManager.getInstance().getGameActivity());
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
    }
}



