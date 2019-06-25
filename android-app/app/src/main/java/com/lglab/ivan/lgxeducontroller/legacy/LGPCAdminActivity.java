package com.lglab.ivan.lgxeducontroller.legacy;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBar.TabListener;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsDbHelper;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;

public class LGPCAdminActivity extends AppCompatActivity {

    AdminCollectionPagerAdapter adapter;
    ViewPager viewPager;
    TabLayout tabLayout;

    PendingIntent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lgpcadmin);

        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.lg_logo);
            actionBar.setDisplayUseLogoEnabled(true);
        }


        tabLayout = findViewById(R.id.admin_tabLayout);
        viewPager = findViewById(R.id.admin_pager);
        adapter = new AdminCollectionPagerAdapter(getSupportFragmentManager());

        //Adding adapter to pager
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(getIntent()), PendingIntent.FLAG_ONE_SHOT);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("comeFrom");
            if (value != null && value.equalsIgnoreCase("tours")) {
                viewPager.setCurrentItem(AdminCollectionPagerAdapter.PAGE_TOURS);
            } else if (value != null && value.equalsIgnoreCase("treeView")) {
                viewPager.setCurrentItem(AdminCollectionPagerAdapter.PAGE_TREEEVIEW);
            }
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lgpcadmin, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.reset_db) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getResources().getString(R.string.are_you_sure_delete_database));

            alert.setPositiveButton(getResources().getString(R.string.yes), (dialog, whichButton) -> resetDatabase());

            alert.setNegativeButton(getResources().getString(R.string.no),
                    (dialog, whichButton) -> {
                    });
            alert.show();


            return true;
        } else if (id == R.id.export_db) {
            exportDatabase();
            return true;
        } else if (id == R.id.action_information_help) {
            startActivity(new Intent(this, Help.class));
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.log_out) {
            onSupportNavigateUp();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void showAboutDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.about_dialog);
        dialog.setTitle(getResources().getString(R.string.about_Controller_message));

        Button dialogButton = dialog.findViewById(R.id.aboutDialogButtonOK);
        dialogButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void exportDatabase() {
        Log.i("INFO", "EXPORTING DATABASE");
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                Calendar c = Calendar.getInstance();
                String dayAndMonth = c.get(Calendar.DAY_OF_MONTH) + "_" + (c.get(Calendar.MONTH) + 1) + "_" + c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE);

                String currentDBPath = "/data/" + this.getPackageName() + "/databases/" + POIsDbHelper.DATABASE_NAME;
                String backupDBPath = "DB_" + dayAndMonth + ".sqlite";


                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    Log.i("INFO", backupDB.getAbsolutePath());
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
            Log.i("INFO", "DATABASE EXPORTED");
        } catch (Exception e) {
            Log.e("ERROR", "EXPORTING DATABASE ERROR" + e.getCause());

        }
    }

    public void resetDatabase() {
        ContentResolver resolver = getApplicationContext().getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(this.getPackageName());
        POIsProvider provider = (POIsProvider) client.getLocalContentProvider();
        provider.resetDatabase();
        client.release();
        resetApp();
    }

    public void resetApp() {

        AlarmManager alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alm.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
        System.exit(0);
    }
}
