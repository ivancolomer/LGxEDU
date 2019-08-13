package com.lglab.ivan.lgxeducontroller.games.utils.multiplayer;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.adapters.DynamicSquareLayout;
import com.lglab.ivan.lgxeducontroller.games.utils.MultiplayerManagerGame;
import com.lglab.ivan.lgxeducontroller.utils.ServerAppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import static com.lglab.ivan.lgxeducontroller.utils.StringHelper.convertToUTF8;

public class ChoosePlayersActivity extends ServerAppCompatActivity implements AIListener, TextToSpeech.OnInitListener {

    private static final int REQUEST_AUDIO_PERMISSION_RESULT = 13;
    private static final int SPEECH_REQUEST_CODE = 14;
    private static final int MY_DATA_CHECK_CODE = 15;

    private AIDataService aiService;
    private TextToSpeech myTTS;

    private final int MAX_PLAYERS = 4;

    private List<String> playernames = new ArrayList<>();
    private AppCompatImageButton[] remove_player_buttons = new AppCompatImageButton[MAX_PLAYERS];
    private EditText[] player_names_text = new EditText[MAX_PLAYERS];
    private DynamicSquareLayout[] player_circles = new DynamicSquareLayout[MAX_PLAYERS];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

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

        remove_player_buttons[0].setOnClickListener(view -> removeOrAddPlayer(0));
        remove_player_buttons[1].setOnClickListener(view -> removeOrAddPlayer(1));
        remove_player_buttons[2].setOnClickListener(view -> removeOrAddPlayer(2));
        remove_player_buttons[3].setOnClickListener(view -> removeOrAddPlayer(3));

        addOnTextChanged(player_names_text[0], 0);
        addOnTextChanged(player_names_text[1], 1);
        addOnTextChanged(player_names_text[2], 2);
        addOnTextChanged(player_names_text[3], 3);

        playernames.clear();
        playernames.add("");
        reloadPlayers();

        final AIConfiguration config = new AIConfiguration(getResources().getString(R.string.ai_api_key),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = new AIDataService(config);

        findViewById(R.id.assistant_button).setOnClickListener((view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED) {
                    startListening();
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                        Toast.makeText(this, "App required access to audio", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO
                    }, REQUEST_AUDIO_PERMISSION_RESULT);
                }

            } else {
                startListening();
            }
        }));

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Log.d("AII", spokenText);
            // Do something with spokenText
            final AIRequest aiRequest = new AIRequest();
            aiRequest.setQuery(spokenText);

            new AsyncTask<AIRequest, Void, AIResponse>() {
                @Override
                protected AIResponse doInBackground(AIRequest... requests) {
                    final AIRequest request = requests[0];
                    try {
                        final AIResponse response = aiService.request(aiRequest);
                        return response;
                    } catch (AIServiceException e) {
                        Log.e("AII", e.toString());
                    }
                    return null;
                }
                @Override
                protected void onPostExecute(AIResponse aiResponse) {
                    if (aiResponse != null) {
                        onResult(aiResponse);
                    }
                }
            }.execute(aiRequest);
        } else if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //the user has the necessary data - create the TTS
                myTTS = new TextToSpeech(this, this);
            }
            else {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startListening() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        }
        catch(ActivityNotFoundException e) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://market.android.com/details?id=com.prometheusinteractive.voice_launcher"));
            startActivity(browserIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_AUDIO_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application needs permission", Toast.LENGTH_SHORT).show();
            }
        }
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
                String subName = getPlayerSubName(i, s.toString());
                ((TextView)player_circles[i].getChildAt(0)).setText(subName);
                text.setError(subName.length() < 2 ? "This field needs atleast 2 characters" : null);
            }
        });
    }

    public static String getPlayerSubName(int playerId, String name) {
        List<String> names = Lists.newArrayList(name.split(" "));
        String textToWrite = String.valueOf(playerId + 1);

        for(int i2 = names.size() - 1; i2 >= 0; i2--) {
            if(names.get(i2).trim().equals("")) {
                names.remove(i2);
            }
        }

        if(names.size() >= 2 && names.get(0).length() > 0 && names.get(1).length() > 0) {
            textToWrite = names.get(0).substring(0, 1) + names.get(1).substring(0, 1);
        }
        else if(names.size()  >= 1 && names.get(0).length() > 1) {
            textToWrite = names.get(0).substring(0, 2);
        }
        return textToWrite;
    }

    private void removeOrAddPlayer(int id) {
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

    public boolean addNewPlayerName(String name) {
        updateArray();
        name = convertToUTF8(name);
        int i;
        for(i = 0; i < playernames.size(); i++) {
            if(playernames.get(i) == null || playernames.get(i).equals("")) {
                break;
            }
        }

        if(i == MAX_PLAYERS)
            return false;

        if(i >= playernames.size())
            removeOrAddPlayer(i);

        playernames.set(i, name);
        player_names_text[i].setText(name);
        reloadPlayers();

        return i < MAX_PLAYERS - 1;
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
        ((MultiplayerManagerGame)GameManager.getInstance()).setPlayers(itemsArray);

        Intent i = new Intent(this, GameManager.getInstance().getGameActivity());
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
    }

    @Override
    public void onResult(AIResponse response) {
        Log.d("AII", "onResult");
        Result result = response.getResult();
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            String parameterString = "";
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }

            Log.d("AII", "Query:" + result.getResolvedQuery() +
                    "\nAction: " + result.getAction() +
                    "\nParameters: " + parameterString);
        }

        if(myTTS != null)
            myTTS.speak(result.getFulfillment().getSpeech(), TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onError(AIError error) {
        Log.d("AII", error.toString());
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    public void onInit(int initStatus) {

        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.ENGLISH)==TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.ENGLISH);
            else if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }
}




