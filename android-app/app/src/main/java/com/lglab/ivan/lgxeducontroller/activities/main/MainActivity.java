package com.lglab.ivan.lgxeducontroller.activities.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.navigate.NavigateActivity;
import com.lglab.ivan.lgxeducontroller.activities.play.PlayActivity;
import com.lglab.ivan.lgxeducontroller.connection.LGConnectionManager;
import com.lglab.ivan.lgxeducontroller.drive.GoogleDriveActivity;
import com.lglab.ivan.lgxeducontroller.legacy.Help;
import com.lglab.ivan.lgxeducontroller.legacy.LGPCAdminActivity;


public class MainActivity extends GoogleDriveActivity {

    /*static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_main);

        findViewById(R.id.navigate).setOnClickListener(view -> startActivity(new Intent(this, NavigateActivity.class)));
        findViewById(R.id.play).setOnClickListener(view -> startActivity(new Intent(this, PlayActivity.class)));
        Log.d("MAIN", getResources().getDisplayMetrics().toString());
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LGConnectionManager.getInstance().setData(prefs.getString("User", "lg"), prefs.getString("Password", "lqgalaxy"), prefs.getString("HostName", "192.168.86.39"), Integer.parseInt(prefs.getString("Port", "22")));
    }

    @Override
    public void handleStringFromDrive(String input) {
        //Nothing here...
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lgpc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_admin:
                showPasswordAlert();
                return true;
            case R.id.action_information_help:
                startActivity(new Intent(this, Help.class));
                return true;
            case R.id.action_about:
                showAboutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPasswordAlert() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final EditText input = new EditText(this);
        input.setSingleLine();
        input.setHint("Password");
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        new MaterialAlertDialogBuilder(this)
                .setMessage("Please, enter the password:")
                .setView(input)
                .setPositiveButton("Confirm", (arg0, arg1) -> {
                    String pass = input.getText().toString();
                    String correct_pass = prefs.getString("AdminPassword", "lg");
                    if (pass.equals(correct_pass)) {
                        Intent intent = new Intent(this, LGPCAdminActivity.class);
                        startActivity(intent);
                    } else {
                        incorrectPasswordAlertMessage();
                    }
                })
                .setNegativeButton("Cancel", (arg0, arg1) -> {
                })
                .show();
    }

    private void incorrectPasswordAlertMessage() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage("Incorrect password. Please, try it again or cancel the operation.")
                .setPositiveButton("Retry", (arg0, arg1) -> showPasswordAlert())
                .setNegativeButton("Cancel", (arg0, arg1) -> {
                })
                .show();
    }

    private void showAboutDialog() {

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.about_dialog, null);

        androidx.appcompat.app.AlertDialog alert = new MaterialAlertDialogBuilder(this)
                .setTitle(getResources().getString(R.string.about_Controller_message))
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.close), (dialog, id) -> {
                    dialog.dismiss();
                })
                .create();

        alert.show();
    }
}
