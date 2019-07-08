package com.lglab.ivan.lgxeducontroller.games.trivia.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.adapters.ResultsAdapter;

public class TriviaResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_results);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        RecyclerView rv = findViewById(R.id.my_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        ResultsAdapter adapter = new ResultsAdapter();
        rv.setAdapter(adapter);

        ((TextView) findViewById(R.id.textViewScore)).setText("You have scored " + ((TriviaManager) GameManager.getInstance()).correctAnsweredQuestionsCount() + " out of " + GameManager.getInstance().getGame().getQuestions().size() + "!");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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

}


