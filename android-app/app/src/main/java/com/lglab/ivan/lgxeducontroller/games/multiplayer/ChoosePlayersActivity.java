package com.lglab.ivan.lgxeducontroller.games.multiplayer;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.widget.AppCompatImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.ImageViewCompat;

import com.google.common.collect.Lists;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.adapters.DynamicSquareLayout;

import java.util.ArrayList;
import java.util.List;

public class ChoosePlayersActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final int MAX_PLAYERS = 4;

    private List<String> playernames = new ArrayList<>();
    private AppCompatImageButton[] remove_player_buttons = new AppCompatImageButton[MAX_PLAYERS];
    private EditText[] player_names_text = new EditText[MAX_PLAYERS];
    private DynamicSquareLayout[] player_circles = new DynamicSquareLayout[MAX_PLAYERS];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.choose_players_activity);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(GameManager.getInstance().getGame().getName());

        remove_player_buttons[0] = findViewById(R.id.remove_player_1);
        remove_player_buttons[1] = findViewById(R.id.remove_player_2);
        remove_player_buttons[2] = findViewById(R.id.remove_player_3);
        remove_player_buttons[3] = findViewById(R.id.remove_player_4);

        player_names_text[0] = findViewById(R.id.player1_editname);
        player_names_text[1] = findViewById(R.id.player2_editname);
        player_names_text[2] = findViewById(R.id.player3_editname);
        player_names_text[3] = findViewById(R.id.player4_editname);

        player_circles[0] = findViewById(R.id.player1_circle);
        player_circles[1] = findViewById(R.id.player2_circle);
        player_circles[2] = findViewById(R.id.player3_circle);
        player_circles[3] = findViewById(R.id.player4_circle);

        findViewById(R.id.play_button_game).setOnClickListener(view -> enterGame());

        remove_player_buttons[0].setOnClickListener(view -> removePlayer(0));
        remove_player_buttons[1].setOnClickListener(view -> removePlayer(1));
        remove_player_buttons[2].setOnClickListener(view -> removePlayer(2));
        remove_player_buttons[3].setOnClickListener(view -> removePlayer(3));

        addOnTextChanged(player_names_text[0], 0);
        addOnTextChanged(player_names_text[1], 1);
        addOnTextChanged(player_names_text[2], 2);
        addOnTextChanged(player_names_text[3], 3);

        playernames.clear();
        playernames.add("");
        reloadPlayers();
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

    private void addOnTextChanged(EditText text, int i) {
        text.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                List<String> names = Lists.newArrayList(s.toString().split(" "));
                String textToWrite = String.valueOf(i + 1);

                for(int i2 = names.size() - 1; i2 >= 0; i2--) {
                    if(names.get(i2).trim().equals("")) {
                        names.remove(i2);
                    }
                }

                if(names.size() >= 2 && names.get(0).length() > 0 && names.get(1).length() > 0) {
                    textToWrite = names.get(0).substring(0, 1) + names.get(1).substring(0, 1);
                    text.setError(null);
                }
                else if(names.size()  >= 1 && names.get(0).length() > 1) {
                    textToWrite = names.get(0).substring(0, 2);
                    text.setError(null);
                }
                else {
                    text.setError("This field needs atleast 2 characters");
                }
                ((TextView)player_circles[i].getChildAt(0)).setText(textToWrite);
            }
        });
    }

    private void removePlayer(int id) {
        updateArray();
        if(playernames.size() > 1 && id < playernames.size()) {
            playernames.remove(id);
            reloadPlayers();
        } else if(playernames.size() < 4 && id == playernames.size()) {
            playernames.add("");
            reloadPlayers();
        }
    }

    private void updateArray() {
        playernames.clear();
        for(EditText text : player_names_text) {
            if(text.getVisibility() == View.VISIBLE) {
                playernames.add(text.getText().toString());
            }
        }
    }

    private void reloadPlayers() {
        for(int i = 0; i < player_names_text.length; i++) {
            player_names_text[i].setText(playernames.size() > i ? playernames.get(i) : "");
            player_names_text[i].setVisibility(playernames.size() > i ? View.VISIBLE : View.GONE);

            player_circles[i].setVisibility(playernames.size() >= i ? View.VISIBLE : View.GONE);
            remove_player_buttons[i].setImageResource(i == playernames.size() ? R.drawable.ic_add_circle_black_24dp : R.drawable.ic_remove_circle_black_24dp);
            ImageViewCompat.setImageTintList(remove_player_buttons[i], ColorStateList.valueOf(getResources().getColor(i == playernames.size() ? R.color.green : R.color.red)));
            remove_player_buttons[i].setVisibility(i == playernames.size() || i < playernames.size() && playernames.size() > 1 ? View.VISIBLE : i == 0 ? View.INVISIBLE : View.GONE);
        }
    }

    public void enterGame() {
        updateArray();

        for(int i = 0; i < playernames.size(); i++) {
            if(player_names_text[i].getError() != null && player_names_text[i].getError().toString().length() > 0) {
                Toast.makeText(this, "Fill all the missing fields in order to continue to the next screen", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String[] itemsArray = new String[playernames.size()];
        itemsArray = playernames.toArray(itemsArray);
        GameManager.getInstance().setPlayers(itemsArray);

        Intent i = new Intent(this, GameManager.getInstance().getGameActivity());
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
    }
}




