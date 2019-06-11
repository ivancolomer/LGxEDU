package com.lglab.ivan.lgxeducontroller.activities_new.play;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.GoogleDriveActivity;
import com.lglab.ivan.lgxeducontroller.activities_new.play.adapter.PlayAdapter;
import com.lglab.ivan.lgxeducontroller.games.quiz.Quiz;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;
import com.lglab.ivan.lgxeducontroller.utils.Category;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlayActivity extends GoogleDriveActivity {

    private static final String TAG = PlayActivity.class.getSimpleName();

    private String searchInput = "";

    private List<Category> dataList;

    private List<Category> allQuizes;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.play);

        dataList = new ArrayList<>();

        adapter = new PlayAdapter(dataList, this);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView = findViewById(R.id.play_rv);
        progressBar = findViewById(R.id.play_pb);

        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        reloadAdapter();

        findViewById(R.id.play_import).setOnClickListener(view -> importQuiz());
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*reloadAdapter();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LGConnectionManager.getInstance().setData(prefs.getString("User", "lg"), prefs.getString("Password", "lqgalaxy"), prefs.getString("HostName", "10.160.67.80"), Integer.parseInt(prefs.getString("Port", "22")));
        */
    }

    private void searchCategories() {
        if(allQuizes == null) {
            makeCategories();
        }

        dataList.clear();

        for(Category c : allQuizes) {
            Category new_category = new Category(c);
            for(int i = new_category.getItemCount() - 1; i >= 0; i--){
                if (!searchInput.isEmpty() && !new_category.getItems().get(i).name.toLowerCase().startsWith(searchInput))
                    new_category.getItems().remove(i);
            }
            if(new_category.getItemCount() > 0) {
                dataList.add(new_category);
            }
        }
    }

    private void makeCategories() {

        HashMap<String, Category> categories = new HashMap<>();

        Cursor category_cursor = POIsProvider.getAllCategories();
        while (category_cursor.moveToNext()) {
            long categoryId = category_cursor.getLong(category_cursor.getColumnIndexOrThrow("_id"));
            String categoryName = category_cursor.getString(category_cursor.getColumnIndexOrThrow("Name"));
            categories.put(categoryName.toLowerCase(), new Category(categoryId, categoryName, new ArrayList<>()));
        }


        Cursor quiz_cursor = POIsProvider.getAllQuizes();
        while (quiz_cursor.moveToNext()) {
            long quizId = quiz_cursor.getLong(quiz_cursor.getColumnIndexOrThrow("_id"));
            String questData = quiz_cursor.getString(quiz_cursor.getColumnIndexOrThrow("Data"));
            try {
                Quiz newQuiz = new Quiz().unpack(new JSONObject(questData));
                newQuiz.id = quizId;

                Category category = categories.get(newQuiz.category.toLowerCase());
                if (category == null) {
                    long id = POIsProvider.insertCategory(newQuiz.category);
                    categories.put(newQuiz.category.toLowerCase(), new Category(id, newQuiz.category, Collections.singletonList(newQuiz)));
                } else {
                    category.getItems().add(newQuiz);
                }
            } catch (Exception e) {
                Log.e("TAG", e.toString());
            }
        }

        //REMOVE EMPTY CATEGORIES
        Iterator<Map.Entry<String, Category>> iter = categories.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Category> entry = iter.next();
            if (entry.getValue().getItems().size() == 0) {
                iter.remove();
            }
        }

        allQuizes = new ArrayList<>(categories.values());

        //ORDER CATEGORIES BY ID
        Collections.sort(allQuizes, (p1, p2) -> Long.compare(p1.id, p2.id));
    }


    @Override
    public void handleStringFromDrive(String input) {
        try {
            new Quiz().unpack(new JSONObject(input)); //Checking if the json is fine ;)

            POIsProvider.insertQuiz(input);
            reloadAdapter();

            showMessage("Quiz imported successfully");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            showMessage("Couldn't import the file");
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

}
