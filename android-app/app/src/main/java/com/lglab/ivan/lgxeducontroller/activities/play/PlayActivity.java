package com.lglab.ivan.lgxeducontroller.activities.play;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.play.adapters.PlayAdapter;
import com.lglab.ivan.lgxeducontroller.games.Category;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;
import com.lglab.ivan.lgxeducontroller.utils.ServerAppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

public class PlayActivity extends ServerAppCompatActivity implements AIListener, TextToSpeech.OnInitListener {

    private static final String TAG = PlayActivity.class.getSimpleName();
    private static final int REQUEST_AUDIO_PERMISSION_RESULT = 13;
    private static final int SPEECH_REQUEST_CODE = 14;
    private static final int MY_DATA_CHECK_CODE = 15;

    private String searchInput = "";

    private List<Category> dataList;

    private List<Category> allGames;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private AIDataService aiService;
    private TextToSpeech myTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_play_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.play);

        dataList = new ArrayList<>();

        adapter = new PlayAdapter(dataList, this);
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        recyclerView = findViewById(R.id.play_rv);
        progressBar = findViewById(R.id.play_pb);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        reloadAdapter();

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

        //findViewById(R.id.play_import).setOnClickListener(view -> importQuiz());
    }

    @Override
    protected void onStart() {
        super.onStart();
        reloadAdapter();
    }

    private void searchCategories() {
        if (allGames == null) {
            makeCategories();
        }

        dataList.clear();

        for (Category c : allGames) {
            Category new_category = new Category(c);
            for (int i = new_category.getItemCount() - 1; i >= 0; i--) {
                if (!searchInput.isEmpty() && !new_category.getItems().get(i).getName().toLowerCase().startsWith(searchInput))
                    new_category.getItems().remove(i);
            }
            if (new_category.getItemCount() > 0) {
                dataList.add(new_category);
            }
        }
    }

    private void makeCategories() {

        HashMap<String, Category> categories = new HashMap<>();

        Cursor category_cursor = POIsProvider.getAllGameCategories();
        while (category_cursor.moveToNext()) {
            long categoryId = category_cursor.getLong(category_cursor.getColumnIndexOrThrow("_id"));
            String categoryName = category_cursor.getString(category_cursor.getColumnIndexOrThrow("Name"));
            categories.put(categoryName.toLowerCase(), new Category(categoryId, categoryName, new ArrayList<>()));
        }
        category_cursor.close();

        Cursor game_cursor = POIsProvider.getAllGames();
        while (game_cursor.moveToNext()) {
            long gameId = game_cursor.getLong(game_cursor.getColumnIndexOrThrow("_id"));
            String questData = game_cursor.getString(game_cursor.getColumnIndexOrThrow("Data"));
            try {
                Game newGame = GameManager.unpackGame(new JSONObject(questData));
                newGame.setId(gameId);

                Category category = categories.get(newGame.getCategory().toLowerCase());
                if (category == null) {
                    long id = POIsProvider.insertCategoryGame(newGame.getCategory());
                    categories.put(newGame.getCategory().toLowerCase(), new Category(id, newGame.getCategory(), new ArrayList<>(Collections.singletonList(newGame))));
                } else {
                    category.getItems().add(newGame);
                }
            } catch (JSONException e) {
                Log.e("TAG", e.toString());
            }
        }
        game_cursor.close();

        //REMOVE EMPTY CATEGORIES
        Iterator<Map.Entry<String, Category>> iter = categories.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Category> entry = iter.next();
            if (entry.getValue().getItems().size() == 0) {
                iter.remove();
            } else {
                Collections.sort(entry.getValue().getItems(), (p1, p2) -> p1.getName().compareTo(p2.getName()));
            }
        }

        allGames = new ArrayList<>(categories.values());

        //ORDER CATEGORIES BY NAME
        Collections.sort(allGames, (f1, f2) -> f1.getTitle().compareTo(f2.getTitle()));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //adapter.getFilter().filter(newText);
                searchInput = newText.toLowerCase();
                reloadAdapter();
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    private void reloadAdapter() {
        searchCategories();
        progressBar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
            return onSupportNavigateUp();

        return super.onKeyDown(keyCode, event);
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
