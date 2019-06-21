package com.lglab.ivan.lgxeducontroller.activities_new.manager.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.Category;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameEnum;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.activities.EditGameActivity;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import java.util.ArrayList;
import java.util.List;

public class AddGameFragment extends DialogFragment {
    private EditText gameTitleText;
    private Spinner gameTypeSpinner;
    private AutoCompleteTextView categoryAutoComplete;
    private ArrayAdapter<Category> categoryStringList;
    private ArrayAdapter<String> gameTypeStringList;

    private Category selectedCategory = null;
    private GameEnum selectedGameType = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_add_game, null);
        builder.setView(dialogView);

        builder.setTitle("Create a game");

        gameTitleText = dialogView.findViewById(R.id.game_title_text);
        gameTypeSpinner = dialogView.findViewById(R.id.game_type_spinner);
        categoryAutoComplete = dialogView.findViewById(R.id.game_category_autocomplete);

        List<String> list = new ArrayList<>();
        for(GameEnum gameType : GameEnum.values()) {
            list.add(gameType.name());
        }

        gameTypeStringList = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, list);
        gameTypeStringList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gameTypeSpinner.setAdapter(gameTypeStringList);
        gameTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long selectedItemId) {
                selectedGameType = GameEnum.findByName(gameTypeStringList.getItem(pos));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        selectedGameType = GameEnum.findByName(list.get(0));

        List<Category> categories = new ArrayList<>();
        Cursor category_cursor = POIsProvider.getAllGameCategories();
        while (category_cursor.moveToNext()) {
            long categoryId = category_cursor.getLong(category_cursor.getColumnIndexOrThrow("_id"));
            String categoryName = category_cursor.getString(category_cursor.getColumnIndexOrThrow("Name"));
            categories.add(new Category(categoryId, categoryName, new ArrayList<>()));
        }
        category_cursor.close();
        selectedCategory = categories.get(0);
        categoryStringList = new ArrayAdapter<>(this.getContext(), android.R.layout.select_dialog_item);
        categoryStringList.addAll(categories);
        categoryAutoComplete.setAdapter(categoryStringList);
        categoryAutoComplete.setOnItemClickListener((parent, view1, position, id) -> selectedCategory = categoryStringList.getItem(position));


        // Show soft keyboard automatically and request focus to field
        gameTitleText.requestFocus();

        builder.setPositiveButton("Create", (dialog, id) -> {
            dialog.cancel();

            Game newGame = GameManager.createGame(gameTitleText.getText().toString(), selectedGameType, selectedCategory.getTitle());
            newGame.getQuestions().add(newGame.createQuestion());
            GameManager.editGame(newGame);

            Intent intent = new Intent(getContext(), EditGameActivity.class);
            intent.putExtra("is_new", true);
            getContext().startActivity(intent);
        })
        .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());

        return builder.create();
    }

    public static AddGameFragment newInstance() {
        AddGameFragment frag = new AddGameFragment();
        return frag;
    }
}
