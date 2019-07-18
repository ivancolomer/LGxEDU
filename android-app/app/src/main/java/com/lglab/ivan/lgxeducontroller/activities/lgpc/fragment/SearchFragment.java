package com.lglab.ivan.lgxeducontroller.activities.lgpc.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.connection.LGCommand;
import com.lglab.ivan.lgxeducontroller.connection.LGConnectionManager;
import com.lglab.ivan.lgxeducontroller.legacy.CategoriesAdapter;
import com.lglab.ivan.lgxeducontroller.legacy.beans.Category;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract;
import com.lglab.ivan.lgxeducontroller.legacy.utils.PoisGridViewAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SearchFragment extends Fragment {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private View rootView;
    private GridView poisGridView;

    private EditText editSearch;
    private FloatingActionButton buttonSearch;
    private AppCompatImageView earth, moon, mars;
    private String currentPlanet = "EARTH";
    private ListView categoriesListView;
    private TextView categorySelectorTitle;
    private ArrayList<String> backIDs = new ArrayList<>();
    private Handler handler = new Handler();

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        editSearch = rootView.findViewById(R.id.search_edittext);
        buttonSearch = rootView.findViewById(R.id.searchButton);
        earth = rootView.findViewById(R.id.earth);
        moon = rootView.findViewById(R.id.moon);
        mars = rootView.findViewById(R.id.mars);

        FloatingActionButton btnSpeak = rootView.findViewById(R.id.btnSpeak);

        categoriesListView = rootView.findViewById(R.id.categories_listview);
        AppCompatImageView backIcon = rootView.findViewById(R.id.back_icon);
        AppCompatImageView backStartIcon = rootView.findViewById(R.id.back_start_icon);//comes back to the initial category
        categorySelectorTitle = rootView.findViewById(R.id.current_category);

        btnSpeak.setOnClickListener(v -> promptSpeechInput());

        setSearchInLGButton();
        setPlanetsButtonsBehaviour();

        poisGridView = rootView.findViewById(R.id.POISgridview);

        backStartIcon.setOnClickListener(v -> {
            backIDs.clear();
            Category category = getCategoryByName(currentPlanet);
            backIDs.add(String.valueOf(category.getId()));

            showPoisByCategory();
        });

        backIcon.setOnClickListener(v -> {
            if (backIDs.size() > 1) {
                backIDs.remove(0);
            }
            showPoisByCategory();
        });

        return rootView;
    }

    private void showPoisByCategory() {

        Cursor queryCursor = getCategoriesCursor();
        showCategoriesOnScreen(queryCursor);

        String currentCategoryName = POIsContract.CategoryEntry.getNameById(getActivity(), Integer.parseInt(backIDs.get(0)));

        categorySelectorTitle.setText(currentCategoryName);

        final List<POI> poisList = getPoisList(Integer.parseInt(backIDs.get(0)));
        if (poisList != null) {
            poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity()));
        }
    }

    private Cursor getCategoriesCursor() {
        //we get only the categories that the admin user wants to be shown on the app screen and have father category ID the once of the parameters.
        return POIsContract.CategoryEntry.getNotHiddenCategoriesByFatherID(getActivity(), backIDs.get(0));
    }

    private void showCategoriesOnScreen(Cursor queryCursor) {
        CategoriesAdapter adapter = new CategoriesAdapter(getActivity(), queryCursor, 0);

        if (queryCursor.getCount() > 0) {
            categoriesListView.setAdapter(adapter);

            categoriesListView.setOnItemClickListener((parent, view, position, id) -> {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);//gets the category selected
                if (cursor != null) {
                    int itemSelectedID = cursor.getInt(0);
                    backIDs.add(0, String.valueOf(itemSelectedID));
                    //this method is call to see AGAIN the categories list. However, the view will
                    //correspond to the categories inside the current category just clicked.
                    showPoisByCategory();
                }
            });
        } else {
            categoriesListView.setAdapter(null);
        }
    }

    private void promptSpeechInput() {

        Locale spanish = new Locale("es", "ES");
        Locale catalan = new Locale("ca", "ES");

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, catalan);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, catalan);
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, catalan);
        intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, spanish);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, spanish);
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, spanish);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == Activity.RESULT_OK && null != data) {

                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                String placeToSearch = result.get(0);
                if (placeToSearch != null && !placeToSearch.equals("")) {
                    editSearch.setText(placeToSearch);
                    String command = buildSearchCommand(placeToSearch);
                    doSearch(command, false);

                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.please_enter_search), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            backIDs = savedInstanceState.getStringArrayList("backIds");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("backIds", backIDs);
    }

    @Override
    public void onResume() {
        super.onResume();
        showPoisByCategory();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (currentPlanet.equals("EARTH")) {
            Category category = getCategoryByName(currentPlanet);

            final List<POI> poisList = getPoisList(category.getId());
            if (poisList != null) {
                poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity()));
            }
        }

        Category category = getCategoryByName(currentPlanet);
        categorySelectorTitle.setText(category.getName());

        backIDs.add(String.valueOf(category.getId()));
        Cursor queryCursor = POIsContract.CategoryEntry.getNotHiddenCategoriesByFatherID(getActivity(), String.valueOf(category.getId()));
        showCategoriesOnScreen(queryCursor);
    }

    private void setPlanetsButtonsBehaviour() {
        Earth();
        Moon();
        Mars();
    }

    private List<POI> getPoisList(int categoryId) {

        List<POI> lPois = new ArrayList<>();

        try (Cursor allPoisByCategoryCursor = POIsContract.POIEntry.getPOIsByCategory(getActivity(), String.valueOf(categoryId))) {

            while (allPoisByCategoryCursor.moveToNext()) {

                int poiId = allPoisByCategoryCursor.getInt(0);

                POI poiEntry = getPoiData(poiId);
                lPois.add(poiEntry);
            }
        }
        return lPois;
    }

    private POI getPoiData(int poiId) {
        POI poiEntry = POI.getPOIByIDFromDB(poiId);
        if (poiEntry == null)
            return new POI();
        return poiEntry;
    }

    private Category getCategoryByName(String categoryName) {
        Category category = new Category();
        try (Cursor categoryCursor = POIsContract.CategoryEntry.getCategoriesByName(getActivity(), categoryName)) {

            if (categoryCursor.moveToNext()) {
                category.setId(categoryCursor.getInt(categoryCursor.getColumnIndex(POIsContract.CategoryEntry.COLUMN_ID)));
                category.setFatherID(categoryCursor.getInt(categoryCursor.getColumnIndex(POIsContract.CategoryEntry.COLUMN_FATHER_ID)));
                category.setName(categoryCursor.getString(categoryCursor.getColumnIndex(POIsContract.CategoryEntry.COLUMN_NAME)));
                category.setShownName(categoryCursor.getString(categoryCursor.getColumnIndex(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME)));
            }
        }
        return category;
    }

    private void Earth() {
        earth.setOnClickListener(v -> {
            String command = "echo 'planet=earth' > /tmp/query.txt";

            if (!currentPlanet.equals("EARTH")) {
                doSearch(command, true);
                currentPlanet = "EARTH";
            }

            Category category = getCategoryByName(currentPlanet);
            categorySelectorTitle.setText(category.getName());

            backIDs = new ArrayList<>();
            backIDs.add(String.valueOf(category.getId()));

            Cursor queryCursor = POIsContract.CategoryEntry.getNotHiddenCategoriesByFatherID(getActivity(), String.valueOf(category.getId()));
            showCategoriesOnScreen(queryCursor);

            final List<POI> poisList = getPoisList(category.getId());
            if (poisList != null) {
                poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity()));
            }
        });
    }

    private void Moon() {

        moon.setOnClickListener(v -> {
            String command = "echo 'planet=moon' > /tmp/query.txt";
            if (!currentPlanet.equals("MOON")) {
                //setConnectionWithLiquidGalaxy(command);
                doSearch(command, true);
                currentPlanet = "MOON";
                Category category = getCategoryByName(currentPlanet);
                categorySelectorTitle.setText(category.getName());

                backIDs = new ArrayList<>();
                backIDs.add(String.valueOf(category.getId()));

                Cursor queryCursor = POIsContract.CategoryEntry.getNotHiddenCategoriesByFatherID(getActivity(), String.valueOf(category.getId()));
                showCategoriesOnScreen(queryCursor);

                final List<POI> poisList = getPoisList(category.getId());
                poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity()));
            }
        });
    }

    private void Mars() {

        mars.setOnClickListener(v -> {
            String command = "echo 'planet=mars' > /tmp/query.txt";
            if (!currentPlanet.equals("MARS")) {
                doSearch(command, true);
                currentPlanet = "MARS";
                Category category = getCategoryByName(currentPlanet);
                categorySelectorTitle.setText(category.getName());

                backIDs = new ArrayList<>();
                backIDs.add(String.valueOf(category.getId()));

                Cursor queryCursor = POIsContract.CategoryEntry.getNotHiddenCategoriesByFatherID(getActivity(), String.valueOf(category.getId()));
                showCategoriesOnScreen(queryCursor);

                final List<POI> poisList = getPoisList(category.getId());
                poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity()));
            }
        });
    }

    private void setSearchInLGButton() {
        buttonSearch.setOnClickListener(v -> {
            String placeToSearch = editSearch.getText().toString();
            if (!placeToSearch.equals("")) {
                doSearch("echo 'search=" + placeToSearch + "' > /tmp/query.txt", false);
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.please_enter_search), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String buildSearchCommand(String search) {
        return "echo 'search=" + search + "' > /tmp/query.txt";
    }

    private void doSearch(String command, boolean isChangingPlanet) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setMessage(getContext().getResources().getString(isChangingPlanet ? R.string.changingPlanet : R.string.searching));
        builder.setView(R.layout.progress);
        builder.setNegativeButton(getContext().getResources().getString(R.string.cancel), (dialog, id) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.show();

        LGCommand lgCommand = new LGCommand(command, LGCommand.CRITICAL_MESSAGE, (String response) -> {
            if(response == null) {
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.connection_failure), Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        });

        LGConnectionManager.getInstance().addCommandToLG(lgCommand);
        //TIMEOUT 5 seconds BEFORE REMOVING COMMAND FORM LG AND DISPLAYING CONNECTION FAILURE MESSAGE
        handler.postDelayed(() -> {
            if(dialog.isShowing()) {
                LGConnectionManager.getInstance().removeCommandFromLG(lgCommand);
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.connection_failure), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        }, 5000);
    }
}