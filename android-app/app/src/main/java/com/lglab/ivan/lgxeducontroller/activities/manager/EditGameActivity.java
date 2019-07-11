package com.lglab.ivan.lgxeducontroller.activities.manager;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.ISaveData;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import github.chenupt.multiplemodel.ItemEntity;
import github.chenupt.multiplemodel.ItemEntityUtil;
import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerManager;
import github.chenupt.springindicator.SpringIndicator;
import github.chenupt.springindicator.viewpager.ScrollerViewPager;

public class EditGameActivity extends AppCompatActivity {

    private ScrollerViewPager viewPager;
    private ModelPagerAdapter adapter;
    private SpringIndicator springIndicator;
    private Game game;
    private boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quiz);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        game = GameManager.getInstance().getGame();
        isNew = getIntent().getBooleanExtra("is_new", false);
        actionBar.setTitle(game.getName());

        viewPager = findViewById(R.id.view_pager);
        springIndicator = findViewById(R.id.indicator);

        loadPages();
        viewPager.setCurrentItem(getIntent().getIntExtra("page", 0), false);

        LinearLayout myLayout = findViewById(R.id.main_layout_quiz);
        myLayout.requestFocus();
    }

    private void loadPages() {

        List<ItemEntity> list = new ArrayList<>();

        for (int i = 0; i < game.getQuestions().size(); i++) {
            ItemEntityUtil.create(i).setModelView(GameManager.getInstance().getGameEditFragment()).attach(list);
        }

        PagerManager manager = PagerManager.begin().addFragments(list).setTitles(getTitles());

        adapter = new ModelPagerAdapter(getSupportFragmentManager(), manager);

        viewPager.setAdapter(adapter);
        viewPager.fixScrollSpeed();

        springIndicator.removeAllViews();
        springIndicator.setViewPager(viewPager);
    }

    private List<String> getTitles() {
        int size = game.getQuestions().size();

        ArrayList<String> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(String.valueOf(i + 1));
        }

        return list;
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

    @Override
    public boolean onSupportNavigateUp() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Do you really want to exit from this page?")
                .setMessage("If you continue, you will lose all your progress.")
                .setPositiveButton("Yes", (dialog, id) -> onBackPressed())
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel()).create()
                .show();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_game, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save_game) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Do you want to save the current game?")

                    .setPositiveButton("Yes", (dialog, id1) -> {
                        //Save the current instance of GAME
                        for (int i = 0; i < game.getQuestions().size(); i++) {
                            try {
                                ISaveData page = (ISaveData) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + i);
                                if (page != null)
                                    page.saveData();
                                game.getQuestions().get(i).pack();
                            } catch (JSONException | NullPointerException e) {
                                //THERE'S A QUESTION i THAT HASN'T BEEN FILLED SUCCESSFULLY!!!
                                viewPager.setCurrentItem(i, true);
                                Toast.makeText(EditGameActivity.this, "Please fill all the information from this page in order to save the game", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        try {
                            if (!isNew)
                                POIsProvider.updateGameById((int) game.getId(), game.pack().toString());
                            else
                                POIsProvider.insertGame(game.pack().toString(), "");
                            //Log.d("save", quiz.pack().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }
                        onBackPressed();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id1) -> dialog.cancel()).create()
                    .show();
        } else if (id == R.id.add_question) {
            //Add page (question)
            int currentItem = viewPager.getCurrentItem();

            for (int i = 0; i < game.getQuestions().size(); i++) {
                ISaveData page = (ISaveData) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + i);
                if (page != null) {
                    page.saveData();
                    //getSupportFragmentManager().getFragments().remove(page);
                }
            }

            if (game.getQuestions().size() == currentItem + 1) {
                game.getQuestions().add(game.createQuestion());
            } else {
                game.getQuestions().add(currentItem + 1, game.createQuestion());
            }

            finish();
            getIntent().putExtra("page", currentItem + 1);
            startActivity(getIntent());
        } else if (id == R.id.remove_question) {

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Do you want to remove the current question from the game?")

                    .setPositiveButton("Yes", (dialog, id1) -> {
                        if (game.getQuestions().size() <= 1) {
                            Toast.makeText(EditGameActivity.this, "You can't delete the last page", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int currentItem = viewPager.getCurrentItem();
                        for (int i = 0; i < game.getQuestions().size(); i++) {
                            ISaveData page = (ISaveData) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + i);
                            if (page != null) {
                                page.saveData();
                                //getSupportFragmentManager().getFragments().remove(page);
                            }
                        }

                        game.getQuestions().remove(currentItem);

                        finish();
                        getIntent().putExtra("page", currentItem == 0 ? currentItem : currentItem - 1);
                        startActivity(getIntent());
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id1) -> dialog.cancel()).create()
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }
}
