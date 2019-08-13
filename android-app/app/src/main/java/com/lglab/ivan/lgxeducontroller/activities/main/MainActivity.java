package com.lglab.ivan.lgxeducontroller.activities.main;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonElement;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.lgpc.LGPC;
import com.lglab.ivan.lgxeducontroller.activities.navigate.NavigateActivity;
import com.lglab.ivan.lgxeducontroller.activities.play.PlayActivity;
import com.lglab.ivan.lgxeducontroller.connection.LGApi;
import com.lglab.ivan.lgxeducontroller.connection.LGConnectionManager;
import com.lglab.ivan.lgxeducontroller.legacy.Help;
import com.lglab.ivan.lgxeducontroller.legacy.LGPCAdminActivity;
import com.lglab.ivan.lgxeducontroller.utils.ServerAppCompatActivity;

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


public class MainActivity extends ServerAppCompatActivity implements AIListener, TextToSpeech.OnInitListener {
    private static final int REQUEST_AUDIO_PERMISSION_RESULT = 13;
    private static final int SPEECH_REQUEST_CODE = 14;
    private static final int MY_DATA_CHECK_CODE = 15;

    /*static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }*/



    private AIDataService aiService;
    private TextToSpeech myTTS;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);

        findViewById(R.id.navigate_button).setOnClickListener(view -> startActivity(new Intent(this, NavigateActivity.class)));
        findViewById(R.id.play_button).setOnClickListener(view -> startActivity(new Intent(this, PlayActivity.class)));
        findViewById(R.id.pois_and_tours_button).setOnClickListener(view -> startActivity(new Intent(this, LGPC.class)));

        /*Log.d("MAIN", getResources().getDisplayMetrics().toString());
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.d("MAIN", String.valueOf(metrics.densityDpi));*/

        final AIConfiguration config = new AIConfiguration(getResources().getString(R.string.ai_api_key),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = new AIDataService(config);
        //aiService.setListener(this);

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
            // Start the activity, the intent will be populated with the speech text
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        }
        catch(ActivityNotFoundException e) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://market.android.com/details?id=com.prometheusinteractive.voice_launcher"));
            startActivity(browserIntent);
        }

            //aiService.startListening();
        /*Dialog loadingDialog = new MaterialAlertDialogBuilder(this)
                .setView(R.layout.progress)
                .setTitle("Google Assistant")
                .setMessage("Listening...")
                .setOnCancelListener((dialog) -> {
                    //aiService.stopListening()
                })
                .setNegativeButton("STOP", (dialog, id) -> dialog.cancel())
                .create();

        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();*/
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
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LGConnectionManager.getInstance().setData(prefs.getString("SSH-USER", "lg"), prefs.getString("SSH-PASSWORD", "lqgalaxy"), prefs.getString("SSH-IP", "192.168.86.39"), Integer.parseInt(prefs.getString("SSH-PORT", "22")));

        LGApi.SERVER_IP = prefs.getString("KML-API-IP", "192.168.86.145");
        LGApi.PORT = Integer.parseInt(prefs.getString("KML-API-PORT", "8112"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lgpc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_admin:
                showPasswordAlert();
                return true;
            case R.id.action_information_help:
                startActivity(new Intent(this, Help.class));
                return true;
            case R.id.action_about:
                showAboutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPasswordAlert() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final EditText input = new EditText(this);
        input.setSingleLine();
        input.setHint("Password");
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        Dialog dialog = new MaterialAlertDialogBuilder(this)
                .setMessage("Please, enter the password:")
                .setView(input)
                .setPositiveButton("Confirm", (arg0, arg1) -> {
                    String pass = input.getText().toString();
                    String correct_pass = prefs.getString("AdminPassword", "lg");
                    if (pass.equals(correct_pass)) {
                        Intent intent = new Intent(this, LGPCAdminActivity.class);
                        startActivity(intent);
                    } else {
                        incorrectPasswordAlertMessage();
                    }
                })
                .setNegativeButton("Cancel", (arg0, arg1) -> {
                }).create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void incorrectPasswordAlertMessage() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage("Incorrect password. Please, try it again or cancel the operation.")
                .setPositiveButton("Retry", (arg0, arg1) -> showPasswordAlert())
                .setNegativeButton("Cancel", (arg0, arg1) -> {
                })
                .show();
    }

    private void showAboutDialog() {

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.about_dialog, null);

        androidx.appcompat.app.AlertDialog alert = new MaterialAlertDialogBuilder(this)
                .setTitle(getResources().getString(R.string.about_Controller_message))
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.close), (dialog, id) -> dialog.dismiss())
                .create();

        alert.show();
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
