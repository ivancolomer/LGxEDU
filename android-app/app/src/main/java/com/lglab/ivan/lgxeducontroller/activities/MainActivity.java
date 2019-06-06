package com.lglab.ivan.lgxeducontroller.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.legacy.Help;
import com.lglab.ivan.lgxeducontroller.legacy.LGPCAdminActivity;


public class MainActivity extends GoogleDriveActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;

        button_navigate();
        button_play();
    }

    private void button_navigate() {
        findViewById(R.id.navigate).setOnClickListener(view -> {
            //Toast.makeText(context, "Navigate", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(context, NavigateActivity.class));
        });
    }

    private void button_play() {
        findViewById(R.id.play).setOnClickListener(view -> {
            //Toast.makeText(context, "Play", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(context, PlayActivity.class));
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lgpc, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_admin) {
            showPasswordAlert();
            return true;
        } else if (id == R.id.action_information_help) {
            startActivity(new Intent(this, Help.class));
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showAboutDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.about_dialog);
        dialog.setTitle(getResources().getString(R.string.about_Controller_message));

        Button dialogButton = dialog.findViewById(R.id.aboutDialogButtonOK);
        dialogButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void handleStringFromDrive(String input) {
        //Nothing here...
    }


    private void incorrectPasswordAlertMessage() {
        // prepare the alert box
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(context);

        // set the message to display
        alertbox.setTitle("Error");
        alertbox.setMessage("Incorrect password. Please, try it again or cancel the operation.");

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Retry", new DialogInterface.OnClickListener() {

            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
                showPasswordAlert();
            }
        });

        // set a negative/no button and create a listener
        alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        // display box
        alertbox.show();
    }

    private void showPasswordAlert() {
        // prepare the alert box
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // set the message to display
        alertbox.setMessage("Please, enter the password:");
        final EditText input = new EditText(context);
        input.setSingleLine();
        input.setHint("Password");
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertbox.setView(input);

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {

                String pass = input.getText().toString();
                String correct_pass = prefs.getString("AdminPassword", "lg");
                if (pass.equals(correct_pass)) {
                    Intent intent = new Intent(context, LGPCAdminActivity.class);
                    startActivity(intent);
                } else {
                    incorrectPasswordAlertMessage();
                }
            }
        });

        // set a negative/no button and create a listener
        alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        // display box
        alertbox.show();
    }
}
