package com.lglab.ivan.lgxeducontroller.drive;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.core.util.Pair;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;
    private String drive_app_folder;
    public List<File> files;

    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    private static final String JSON_MIME_TYPE = "application/json";

    DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList(drive_app_folder))
                    .setMimeType(JSON_MIME_TYPE)
                    .setName("Untitled file");

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("application/json", content);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    private Task<String> createAppFolderID() {
        return Tasks.call(mExecutor, () -> {

            List<String> root = Collections.singletonList("root");

            File metadata = new File()
                    .setParents(root)
                    .setMimeType(FOLDER_MIME_TYPE)
                    .setName("LGxEDU");

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }


    public void searchForAppFolderID() {
        Tasks.call(mExecutor, () ->
                mDriveService.files().list().setQ("mimeType = '" + FOLDER_MIME_TYPE + "' and name = 'LGxEDU' and parents in 'root' ").setSpaces("drive").execute())
                .addOnSuccessListener(fileList -> {
                    List<File> files = fileList.getFiles();
                    if (files.size() > 0) {
                        drive_app_folder = files.get(0).getId();
                        Log.d(GoogleDriveManager.TAG, "App folder was already created: " + drive_app_folder);
                        searchForFilesInsideAppFolderID();
                    } else {
                        createAppFolderID()
                                .addOnSuccessListener(file -> {
                                    drive_app_folder = file;
                                    Log.d(GoogleDriveManager.TAG, "App folder created: " + drive_app_folder);
                                    searchForFilesInsideAppFolderID();
                                })
                                .addOnFailureListener(exception -> Log.e(GoogleDriveManager.TAG, "Unable to search for appfolder", exception));
                    }
                }).addOnFailureListener(exception -> Log.e(GoogleDriveManager.TAG, "Unable to search for appfolder", exception));
    }

    private void searchForFilesInsideAppFolderID() {
        queryFiles()
                .addOnSuccessListener(fileList -> files = fileList.getFiles())
                .addOnFailureListener(exception -> Log.e(GoogleDriveManager.TAG, "Unable to search for for files inside appfolder", exception));
    }

    private Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files()
                        .list()
                        .setQ("mimeType = 'application/json' and parents in '" + drive_app_folder + "' ")
                        .setSpaces("drive").execute());
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType(JSON_MIME_TYPE);

        /*String[] mimetypes = {"application/json"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);*/

        return intent;
    }

    /**
     * Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
     * created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
     */
    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
            ContentResolver contentResolver, Uri uri) {
        return Tasks.call(mExecutor, () -> {

            // Retrieve the document's display name from its metadata.
            String name;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
            }

            // Read the document's contents as a String.
            String content;
            try (InputStream is = contentResolver.openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                content = stringBuilder.toString();
            }

            return Pair.create(name, content);
        });
    }
}
