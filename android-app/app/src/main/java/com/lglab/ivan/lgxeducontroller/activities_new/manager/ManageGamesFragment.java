package com.lglab.ivan.lgxeducontroller.activities_new.manager;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities_new.manager.adapters.CategoryManagerAdapter;
import com.lglab.ivan.lgxeducontroller.games.Category;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ManageGamesFragment extends Fragment {

    public static ManageGamesFragment newInstance() {
        return new ManageGamesFragment();
    }

    private CategoryManagerAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_games_manager, null, false);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(rootView.getContext());

        textView = rootView.findViewById(R.id.no_games_found);
        textView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(layoutManager);
        reloadAdapter();
        //rootView.findViewById(R.id.import_from_drive).setOnClickListener(view -> importQuiz());

        rootView.findViewById(R.id.add_category).setOnClickListener(view -> {
            //Check if already exists on sql
            //if no
            //long id = POIsProvider.insertCategoryGame(newGame.getCategory());
            //categories.put(newGame.getCategory().toLowerCase(), new Category(id, newGame.getCategory(), Collections.singletonList(newGame)));
        });

        rootView.findViewById(R.id.manage_drive).setOnClickListener(view -> {

        });

        return rootView;
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
                    categories.put(newGame.getCategory().toLowerCase(), new Category(id, newGame.getCategory(), Collections.singletonList(newGame)));
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
            }
        }

        //ORDER CATEGORIES BY ID
        ArrayList<Category> orderedCategories = new ArrayList<>(categories.values());
        Collections.sort(orderedCategories, (p1, p2) -> Long.compare(p1.id, p2.id));

        return orderedCategories;
    }

    private void reloadAdapter() {

        List<Category> categories = makeCategories();
        adapter = new CategoryManagerAdapter(categories);
        recyclerView.setAdapter(adapter);

        for (int i = adapter.getGroups().size() - 1; i >= 0; i--) {
            if (adapter.isGroupExpanded(i)) {
                continue;
            }
            adapter.toggleGroup(i);
        }

        if(adapter.getGroups().size() == 0) {
            recyclerView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }
        else {
            textView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

    }
}
