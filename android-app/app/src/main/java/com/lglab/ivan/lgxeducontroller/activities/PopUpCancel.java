package com.lglab.ivan.lgxeducontroller.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.lglab.ivan.lgxeducontroller.R;

public class PopUpCancel extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_cancel_question);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        getWindow().setLayout(dm.widthPixels, dm.heightPixels);
    }
}
