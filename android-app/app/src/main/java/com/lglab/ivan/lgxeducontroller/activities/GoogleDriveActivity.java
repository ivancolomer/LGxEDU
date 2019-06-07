package com.lglab.ivan.lgxeducontroller.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import com.lglab.ivan.lgxeducontroller.drive.DriveServiceHelper;
import com.lglab.ivan.lgxeducontroller.drive.GoogleDriveManager;
import com.lglab.ivan.lgxeducontroller.games.quiz.Quiz;

import java.util.Collections;

public abstract class GoogleDriveActivity extends AppCompatActivity {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case GoogleDriveManager.RC_SIGN_IN:
                if (resultCode == RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                    return;
                }

                Log.e(GoogleDriveManager.TAG, "Sign-in failed with resultCode = " + resultCode);
                showMessage("Unable to sign in. Make sure you have internet connection and try again.");
                //finish();
                break;
            case GoogleDriveManager.RC_OPEN_FILE:
                if (resultCode == RESULT_OK && resultData != null) {
                    Uri uri = resultData.getData();
                    if (uri != null) {
                        openFileFromFilePicker(uri);
                    }
                }
                break;
            case GoogleDriveManager.RC_OPEN_FOLDER:
                if (resultCode == RESULT_OK && resultData != null) {
                    Uri uri = resultData.getData();
                    if (uri != null) {
                        openFolderFromFolderPicker(uri);
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        signIn();
    }

    private void signIn() {
        if (GoogleDriveManager.GoogleSignInClient != null && GoogleDriveManager.DriveServiceHelper != null)
            return;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                //The scope should be changed in order to see other files... https://developers.google.com/drive/api/v3/about-auth https://www.googleapis.com/auth/drive
                .build();

        GoogleDriveManager.GoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null)
            GoogleDriveManager.GoogleSignInClient.signOut();

        Intent signInIntent = GoogleDriveManager.GoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GoogleDriveManager.RC_SIGN_IN);
    }

    public void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(GoogleDriveManager.TAG, "Signed in as " + googleAccount.getEmail());
                    showMessage("Signed in as " + googleAccount.getEmail());

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    this, Collections.singleton(DriveScopes.DRIVE_FILE)); //SCOPE_FILE
                    credential.setSelectedAccount(googleAccount.getAccount());
                    Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                            .setApplicationName("LGxEDU")
                            .build();

                    GoogleDriveManager.DriveServiceHelper = new DriveServiceHelper(googleDriveService);

                    openFilePicker();
                })
                .addOnFailureListener(exception -> Log.e(GoogleDriveManager.TAG, "Unable to sign in.", exception));
    }



    /**
     * Opens the Storage Access Framework file picker using GoogleDriveManager.RC_OPEN_FILE.
     */
    private void openFilePicker() {
        if (GoogleDriveManager.DriveServiceHelper != null) {
            Log.d(GoogleDriveManager.TAG, "Opening file picker.");

            Intent pickerIntent = GoogleDriveManager.DriveServiceHelper.createFilePickerIntent();

            // The result of the SAF Intent is handled in onActivityResult.
            startActivityForResult(pickerIntent, GoogleDriveManager.RC_OPEN_FILE);
        }
    }

    /**
     * Opens the Storage Access Framework file picker using GoogleDriveManager.RC_OPEN_FOLDER.
     */
    private void openFolderPicker() {
        if (GoogleDriveManager.DriveServiceHelper != null) {
            Log.d(GoogleDriveManager.TAG, "Opening folder picker.");

            Intent pickerIntent = GoogleDriveManager.DriveServiceHelper.createFolderPickerIntent();

            // The result of the SAF Intent is handled in onActivityResult.
            startActivityForResult(pickerIntent, GoogleDriveManager.RC_OPEN_FOLDER);
        }
    }


    /**
     * Opens a file from its {@code uri} returned from the Storage Access Framework file picker
     * initiated by {@link #openFilePicker()}.
     */
    private void openFileFromFilePicker(Uri uri) {
        if (GoogleDriveManager.DriveServiceHelper != null) {
            Log.d(GoogleDriveManager.TAG, "Opening " + uri.getPath());

            GoogleDriveManager.DriveServiceHelper.openFileUsingStorageAccessFramework(getContentResolver(), uri)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;

                        // Files opened through SAF cannot be modified.
                        GoogleDriveManager.setReadOnlyMode(name, content);
                        Log.d(GoogleDriveManager.TAG, name + ": " + content);
                    })
                    .addOnFailureListener(exception ->
                            Log.e(GoogleDriveManager.TAG, "Unable to open file from picker.", exception));
        }
    }

    /**
     * Opens a file from its {@code uri} returned from the Storage Access Framework file picker
     * initiated by {@link #openFilePicker()}.
     */
    private void openFolderFromFolderPicker(Uri uri) {
        if (GoogleDriveManager.DriveServiceHelper != null) {
            Log.d(GoogleDriveManager.TAG, "Opening " + uri.getPath());

            GoogleDriveManager.DriveServiceHelper.openFileUsingStorageAccessFramework(getContentResolver(), uri)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;

                        // Files opened through SAF cannot be modified.
                        GoogleDriveManager.setReadOnlyMode(name, content);
                        Log.d(GoogleDriveManager.TAG, name + ": " + content);
                    })
                    .addOnFailureListener(exception ->
                            Log.e(GoogleDriveManager.TAG, "Unable to open file from picker.", exception));
        }
    }
















    public void importQuiz() {
        Toast.makeText(this, "importQuiz", Toast.LENGTH_SHORT).show();
        /*if (GoogleDriveManager.DriveClient != null && GoogleDriveManager.DriveResourceClient != null) {
            pickTextFile()
                    .addOnSuccessListener(this,
                            driveId -> handleReadItem(driveId.asDriveFile()))
                    .addOnFailureListener(this, e -> {
                        Log.e(GoogleDriveManager.TAG, "No file selected", e);
                        finish();
                    });
        }*/
    }

    public void exportQuiz(Quiz quiz) {
        /*if (GoogleDriveManager.DriveClient != null && GoogleDriveManager.DriveResourceClient != null) {
            pickFolder()
                    .addOnSuccessListener(this,
                            driveId -> handleSaveItem(driveId.asDriveFolder(), quiz))
                    .addOnFailureListener(this, e -> {
                        Log.e(GoogleDriveManager.TAG, "No folder selected", e);
                        finish();
                    });
        }*/
    }

    /*private Task<DriveId> pickTextFile() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        //.setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                        .setMimeType(new ArrayList(Arrays.asList("text/plain", "application/json")))
                        .setActivityTitle("Select a file")
                        .build();
        return pickItem(openOptions);
    }

    private Task<DriveId> pickFolder() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        .setSelectionFilter(
                                Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
                        .setActivityTitle("Select a folder")
                        .build();
        return pickItem(openOptions);
    }

    private Task<DriveId> pickItem(OpenFileActivityOptions openOptions) {
        mOpenItemTaskSource = new TaskCompletionSource<>();
        GoogleDriveManager.DriveClient
                .newOpenFileActivityIntentSender(openOptions)
                .continueWith((Continuation<IntentSender, Void>) task -> {
                    startIntentSenderForResult(
                            task.getResult(), GoogleDriveManager.RC_OPEN_ITEM, null, 0, 0, 0);
                    return null;
                });
        return mOpenItemTaskSource.getTask();
    }*/



    /*public void handleReadItem(final DriveFile file) {
        try {
            GoogleDriveManager.DriveResourceClient.openFile(file, DriveFile.MODE_READ_ONLY)
                    .continueWithTask(task -> {
                        DriveContents contents = task.getResult();
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(contents.getInputStream()))) {

                            StringBuilder builder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                builder.append(line).append("\n");
                            }

                            handleStringFromDrive(builder.toString());
                            //quiz = new Quiz().unpack(new JSONObject(builder.toString()));
                        }

                        Task<Void> discardTask = GoogleDriveManager.DriveResourceClient.discardContents(contents);
                        return discardTask;
                    })
                    .addOnFailureListener(e -> {
                        Log.e(GoogleDriveManager.TAG, e.toString());
                    });
        } catch (Exception e) {
            Log.e(GoogleDriveManager.TAG, e.toString());
        }
    }*/

    /*private void handleSaveItem(final DriveFolder parent, Quiz quiz) {
        GoogleDriveManager.DriveResourceClient
                .createContents()
                .continueWithTask(task -> {
                    DriveContents contents = task.getResult();
                    OutputStream outputStream = contents.getOutputStream();
                    try (Writer writer = new OutputStreamWriter(outputStream)) {
                        writer.write(quiz.pack().toString());
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(quiz.getNameForExporting())
                            .setMimeType("application/json")
                            .setStarred(true)
                            .build();

                    return GoogleDriveManager.DriveResourceClient.createFile(parent, changeSet, contents);
                })
                .addOnSuccessListener(this,
                        driveFile -> showMessage("File saved successfully with name " + quiz.getNameForExporting()))
                .addOnFailureListener(this, e -> {
                    Log.e(GoogleDriveManager.TAG, "Unable to create file", e);
                    showMessage("Unable to create file");
                });
    }*/

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public abstract void handleStringFromDrive(String input);
}
