package com.lglab.ivan.lgxeducontroller.legacy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.lglab.ivan.lgxeducontroller.R;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        getSupportFragmentManager().beginTransaction().add(new InfoActivityFragment(), "").commit();
    }

}
