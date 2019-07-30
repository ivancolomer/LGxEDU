package com.lglab.ivan.lgxeducontroller.activities.navigate;

import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.ActionBar;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.utils.ServerAppCompatActivity;

public class NavigateActivity extends ServerAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigate);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Navigate");
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.navigate_container, new NavigateFragment())
                .commit();
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

