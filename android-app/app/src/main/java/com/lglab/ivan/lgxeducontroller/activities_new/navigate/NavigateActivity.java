package com.lglab.ivan.lgxeducontroller.activities_new.navigate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities_new.lgpc.LGPC;
import com.lglab.ivan.lgxeducontroller.activities_new.navigate.data.PointerDetector;
import com.lglab.ivan.lgxeducontroller.connection.ILGConnection;
import com.lglab.ivan.lgxeducontroller.connection.LGCommand;
import com.lglab.ivan.lgxeducontroller.connection.LGConnectionManager;

import java.util.List;

public class NavigateActivity extends AppCompatActivity implements ILGConnection {

    private static final String TAG = NavigateActivity.class.getSimpleName();

    private AppCompatImageView wifiGif;
    private short currentStatus;

    private boolean isOnChromeBook = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigate);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Navigate");

        wifiGif = findViewById(R.id.wifi_gif);
    }

    @Override
    protected void onStart() {
        super.onStart();

        LGConnectionManager.getInstance().setActivity(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        isOnChromeBook = prefs.getBoolean("isOnChromeBook", false);
        currentStatus = 0;

        if(LGConnectionManager.getInstance().isShouldRestartMapNavigation())
            POIController.getInstance().moveToPOI(POIController.EARTH_POI, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.go_to_pois_and_tours:
                Intent intent = new Intent(this, LGPC.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointerDetector.getInstance().preAction();

        final int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            PointerDetector.getInstance().addPointer(event.getPointerId(event.getActionIndex()), event.getX(event.getActionIndex()), event.getY(event.getActionIndex()));
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            PointerDetector.getInstance().removePointer(event.getPointerId(event.getActionIndex()));
        } else if (action == MotionEvent.ACTION_MOVE) {
            int pointerCount = event.getPointerCount();
            for (int i = 0; i < pointerCount; i++) {
                PointerDetector.getInstance().updatePointer(event.getPointerId(i), event.getX(i), event.getY(i));
            }
        }

        List<Pair<String, Boolean>> commands = PointerDetector.getInstance().postAction();
        if(commands.size() > 0) {
            String command = "export DISPLAY=:" + (isOnChromeBook ? "1" : "0") + "; xdotool ";
            boolean critical = false;
            for(Pair<String, Boolean> pair : commands) {
                command += pair.first + " ";
                critical = critical || pair.second;
            }

            LGConnectionManager.getInstance().addCommandToLG(new LGCommand(command, critical ? LGCommand.CRITICAL_MESSAGE : LGCommand.NON_CRITICAL_MESSAGE));
        }


        return super.onTouchEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LGConnectionManager.getInstance().setActivity(null);
    }

    @Override
    public void setStatus(short status) {

        runOnUiThread(() -> {
            try {
                if (status != currentStatus) {
                    currentStatus = status;

                    switch (status) {
                        case LGConnectionManager.CONNECTED:
                            wifiGif.setColorFilter(Color.parseColor("#4CAF50"));
                            break;
                        case LGConnectionManager.NOT_CONNECTED:
                            wifiGif.setColorFilter(Color.parseColor("#F44336"));
                            break;
                        case LGConnectionManager.QUEUE_BUSY:
                            wifiGif.setColorFilter(Color.parseColor("#FF9800"));
                            break;
                        default:
                            wifiGif.setColorFilter(Color.argb(255, 255, 255, 255));
                            break;
                    }

                }
            } catch (Exception e) {

            }
        });
    }
}

