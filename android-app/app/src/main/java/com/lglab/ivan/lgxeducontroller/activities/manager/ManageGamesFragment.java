package com.lglab.ivan.lgxeducontroller.activities.manager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.manager.adapters.CategoryManagerAdapter;
import com.lglab.ivan.lgxeducontroller.activities.manager.fragments.AddGameFragment;
import com.lglab.ivan.lgxeducontroller.drive.GoogleDriveApp;
import com.lglab.ivan.lgxeducontroller.drive.GoogleDriveManager;
import com.lglab.ivan.lgxeducontroller.games.Category;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class ManageGamesFragment extends GoogleDriveApp implements IGamesAdapterActivity {

    private static final int READ_REQUEST_CODE = 10;

    public static ManageGamesFragment newInstance() {
        return new ManageGamesFragment();
    }

    private CategoryManagerAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textView;
    private Dialog loadingDialog;

    private Game uploadingGame;
    private String jsonToUpload;

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == READ_REQUEST_CODE) {
            if (resultCode == RESULT_OK && resultData != null) {
                Uri uri = resultData.getData();
                if(uri != null) {
                    Log.i("saf", "Uri: " + uri.toString());
                    try {
                        String json = readTextFromUri(uri);
                        try {
                            Game game = GameManager.unpackExternalGame(new JSONObject(json), getContext());
                            POIsProvider.insertGame(game.pack().toString(), "");
                            onGamesChanged(true);
                        } catch (Exception ignored) {
                            Toast.makeText(getContext(), "Unable to read from " + uri.toString() + ".", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ignored) {
                        Toast.makeText(getContext(), "Unable to read from " + uri.toString() + ".", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        reader.close();
        return stringBuilder.toString();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_games_manager, null, false);
        recyclerView = rootView.findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(rootView.getContext());

        textView = rootView.findViewById(R.id.no_games_found);
        textView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(layoutManager);

        rootView.findViewById(R.id.add_game).setOnClickListener(view -> {
            MaterialAlertDialogBuilder builderSingle = new MaterialAlertDialogBuilder(getContext());
            builderSingle.setTitle("Choose an item");

            String[] strings = new String[] {
                    "Create a new game",
                    "Import a game from your storage",
                    "Download a game from LiquidGalaxyLAB repository"
            };

            builderSingle.setSingleChoiceItems(strings, -1, (dialog, i) -> {
                if(i == 0) {
                    AddGameFragment.newInstance(null, null, 0).show(getFragmentManager(), "fragment_add_game");
                } else if(i == 1) {
                    Intent intent = new Intent();
                    if (isMediaProviderSupported())
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    else
                        intent.setAction(Intent.ACTION_GET_CONTENT);

                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/json");
                    startActivityForResult(intent, READ_REQUEST_CODE);

                } else if(i == 2) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setStartAnimations(getContext(), R.anim.slide_in_right, R.anim.slide_out_left);
                    builder.setExitAnimations(getContext(), R.anim.slide_in_left, R.anim.slide_out_right);
                    builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.primary_dark));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(getContext(), Uri.parse("https://drive.google.com/drive/folders/1fTWHHWllJpARaIotzx9ha3C-5L7yCqOI?usp=sharing"));

                }
                dialog.dismiss();
            });

            builderSingle.create().show();
        });

        return rootView;
    }


    private boolean isMediaProviderSupported()
    {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            final PackageManager pm = getActivity().getPackageManager();
            // Pick up provider with action string
            final Intent i = new Intent(DocumentsContract.PROVIDER_INTERFACE);
            final List<ResolveInfo> providers = pm.queryIntentContentProviders(i, 0);
            for (ResolveInfo info : providers)
            {
                if(info != null && info.providerInfo != null)
                {
                    final String authority = info.providerInfo.authority;
                    if(isMediaDocumentProvider(Uri.parse("content://"+authority)))
                        return true;
                }
            }
        }
        return false;
    }

    private static boolean isMediaDocumentProvider(final Uri uri)
    {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private void setLoadingDialog(String title, String message) {
        loadingDialog = new MaterialAlertDialogBuilder(getContext())
            .setView(R.layout.progress)
            .setTitle(title)
            .setMessage(message)
            .create();

        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        reloadAdapter();
    }

    private List<Category> makeCategories() {
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
            String fileId = game_cursor.getString(game_cursor.getColumnIndexOrThrow("google_drive_file_id"));

            try {
                Game newGame = GameManager.unpackGame(new JSONObject(questData));
                newGame.setId(gameId);
                newGame.setFileId(fileId);
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

        //ORDER CATEGORIES BY ID
        ArrayList<Category> orderedCategories = new ArrayList<>(categories.values());
        Collections.sort(orderedCategories, (f1, f2) -> f1.getTitle().compareTo(f2.getTitle()));

        return orderedCategories;
    }

    private void reloadAdapter() {

        List<Category> categories = makeCategories();
        adapter = new CategoryManagerAdapter(categories, this);
        recyclerView.setAdapter(adapter);

        for (int i = adapter.getGroups().size() - 1; i >= 0; i--) {
            if (adapter.isGroupExpanded(i)) {
                continue;
            }
            adapter.toggleGroup(i);
        }

        onGamesChanged(false);
    }

    @Override
    public void onGamesChanged(boolean reloadAdapter) {
        if (reloadAdapter) {
            reloadAdapter();
            return;
        }

        if (adapter.getGroups().size() == 0) {
            recyclerView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() { super.onDestroy();

        try {
            trimCache(getContext());
            // Toast.makeText(this,"onDestroy " ,Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void trimCache(Context context) {
        try {
            File[] dirs = ContextCompat.getExternalCacheDirs(context);
            for (File dir : dirs) {
                if (dir != null && dir.isDirectory()) {
                    deleteDir(dir);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    @Override
    public void onFailedLogIn() {
        Toast.makeText(getContext(), "Unable to sign in. Make sure you have internet connection and try again.", Toast.LENGTH_LONG).show();

        this.jsonToUpload = null;
        this.uploadingGame = null;
    }

    @Override
    public void onSuccessLogIn() {
        if(GoogleDriveManager.DriveServiceHelper.files == null) {
            setLoadingDialog("Google Drive", "Searching files from LGxEDU");
            GoogleDriveManager.DriveServiceHelper.searchForAppFolderID(() -> {
                for (String filename : GoogleDriveManager.DriveServiceHelper.files.values()) {
                    Log.d("drive", filename);
                }
                updateGame();

            }, null);
        } else {
            setLoadingDialog("Google Drive", "Searching files from LGxEDU");
            updateGame();
        }
    }

    private void updateGame() {
        if(loadingDialog != null)
            ((TextView)loadingDialog.findViewById(android.R.id.message)).setText("Uploading game...");

        //UPLOAD TO DRIVE
        if(uploadingGame.getFileId() != null && (GoogleDriveManager.DriveServiceHelper.files.containsKey(uploadingGame.getFileId()) || GoogleDriveManager.DriveServiceHelper.files.containsValue(uploadingGame.getNameForExporting()))) {
            //Already on Drive, so let's only update it...
            if(uploadingGame.getFileId() == null || uploadingGame.getFileId().equals("")) {
                for(String key : GoogleDriveManager.DriveServiceHelper.files.keySet()) {
                    if(GoogleDriveManager.DriveServiceHelper.files.get(key).equals(uploadingGame.getNameForExporting())) {
                        uploadingGame.setFileId(key);
                        POIsProvider.updateGameFileIdById(uploadingGame.getId(), key);
                        break;
                    }
                }
            }

            GoogleDriveManager.DriveServiceHelper.saveFile(uploadingGame.getFileId(), uploadingGame.getNameForExporting(), jsonToUpload)
                    .addOnSuccessListener((result) -> {
                        Toast.makeText(getContext(), "Uploaded to Google Drive", Toast.LENGTH_LONG).show();
                        if(loadingDialog != null) {
                            loadingDialog.dismiss();
                            loadingDialog = null;
                        }
                        this.jsonToUpload = null;
                        this.uploadingGame = null;
                    })
                    .addOnFailureListener((result) -> {
                        Toast.makeText(getContext(), "Failed to upload to Google Drive", Toast.LENGTH_LONG).show();
                        if(loadingDialog != null) {
                            loadingDialog.dismiss();
                            loadingDialog = null;
                        }
                        this.jsonToUpload = null;
                        this.uploadingGame = null;
                    });
        } else {
            GoogleDriveManager.DriveServiceHelper.createFile(uploadingGame.getNameForExporting())
                    .addOnSuccessListener((result) -> GoogleDriveManager.DriveServiceHelper.saveFile(result, uploadingGame.getNameForExporting(), jsonToUpload)
                            .addOnFailureListener(exception ->  {
                                Toast.makeText(getContext(), "Failed to upload to Google Drive", Toast.LENGTH_LONG).show();
                                if(loadingDialog != null) {
                                    loadingDialog.dismiss();
                                    loadingDialog = null;
                                }
                                this.jsonToUpload = null;
                                this.uploadingGame = null;
                            })
                            .addOnSuccessListener(result2 -> {
                                uploadingGame.setFileId(result);
                                POIsProvider.updateGameFileIdById(uploadingGame.getId(), result);
                                GoogleDriveManager.DriveServiceHelper.files.put(result, uploadingGame.getNameForExporting());
                                Toast.makeText(getContext(), "Uploaded to Google Drive", Toast.LENGTH_LONG).show();
                                if(loadingDialog != null) {
                                    loadingDialog.dismiss();
                                    loadingDialog = null;
                                }
                                this.jsonToUpload = null;
                                this.uploadingGame = null;
                            }))
                    .addOnFailureListener(exception ->  {
                        Toast.makeText(getContext(), "Failed to upload to Google Drive", Toast.LENGTH_LONG).show();
                        if(loadingDialog != null) {
                            loadingDialog.dismiss();
                            loadingDialog = null;
                        }
                        this.jsonToUpload = null;
                        this.uploadingGame = null;
                    });
        }
    }

    public void updateGameToDrive(Game uploadingGame, String jsonToUpload) {
        if(this.uploadingGame == null && this.jsonToUpload == null) {

            Dialog dialog = new MaterialAlertDialogBuilder(getContext())
                    .setTitle("Do you really want to share the game " + uploadingGame.getName() + "?")
                    .setMessage("The game will be shared with liquidgalaxylab@gmail.com")
                    .setPositiveButton("Yes", ((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        this.uploadingGame = uploadingGame;
                        this.jsonToUpload = jsonToUpload;
                        signIn();
                    }))
                    .setNegativeButton("Cancel", ((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }))
                    .create();

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }
}
