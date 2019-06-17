package com.lglab.ivan.lgxeducontroller.activities_new.manager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lglab.ivan.lgxeducontroller.R;


public class CreatePOIActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_item);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, CreatePOIFragment.newInstance())
                    .commit();
        }
    }
}