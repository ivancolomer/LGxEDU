package com.lglab.ivan.lgxeducontroller.activities_new.navigate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;

public class NavigateActivity extends AppCompatActivity implements ILGConnection {

    private static final String TAG = NavigateActivity.class.getSimpleName();

    private final HashMap<Integer, PointerDetector> pointers = new HashMap<>();
    private long canMoveTime = 0;

    private GifImageView wifiGif;
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
        currentStatus = 0;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LGConnectionManager.getInstance().setData(prefs.getString("User", "lg"), prefs.getString("Password", "lqgalaxy"), prefs.getString("HostName", "10.160.67.80"), Integer.parseInt(prefs.getString("Port", "22")));
        isOnChromeBook = prefs.getBoolean("isOnChromeBook", false);

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
        final int action = event.getActionMasked();
        int index;
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            if (pointers.size() < 2) {
                index = event.getActionIndex();
                pointers.put(event.getPointerId(index), new PointerDetector(event.getX(index), event.getY(index)));

            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            pointers.remove(event.getPointerId(event.getActionIndex()));
        } else if (action == MotionEvent.ACTION_MOVE) {
            int pointerCount = event.getPointerCount();
            for (int i = 0; i < pointerCount; i++) {
                int pointerId = event.getPointerId(i);
                if (pointers.containsKey(pointerId)) {
                    pointers.get(pointerId).update(event.getX(i), event.getY(i));
                }
            }
        }

        if (pointers.size() != 2) {
            if (PointerDetector.isZoomingIn) {
                PointerDetector.isZoomingIn = false;
                updateKeyToLG(false, PointerDetector.KEY_ZOOM_IN);
            }
            if (PointerDetector.isZoomingOut) {
                PointerDetector.isZoomingOut = false;
                updateKeyToLG(false, PointerDetector.KEY_ZOOM_OUT);
            }
        }

        if (pointers.size() == 0)
            return true;

        if (pointers.size() == 1) {
            PointerDetector pointer = pointers.entrySet().iterator().next().getValue();
            if (pointer.isMoving() && canMove()) {
                POIController.getInstance().moveXY(pointer.getTraveledAngle(), Math.min(pointer.getTraveledDistance(), 250) / 250.0d);
            }
        } else if (pointers.size() == 2) {
            Iterator<Map.Entry<Integer, PointerDetector>> iterator = pointers.entrySet().iterator();
            PointerDetector pointer1 = iterator.next().getValue();
            PointerDetector pointer2 = iterator.next().getValue();

            setNotMovable();

            short zoomInteractionType = pointer1.getZoomInteractionType(pointer2);
            if (zoomInteractionType == PointerDetector.ZOOM_IN && !PointerDetector.isZoomingIn) {
                if (PointerDetector.isZoomingOut) {
                    PointerDetector.isZoomingOut = false;
                    updateKeyToLG(false, PointerDetector.KEY_ZOOM_OUT);
                }
                PointerDetector.isZoomingIn = true;
                updateKeyToLG(true, PointerDetector.KEY_ZOOM_IN);
            } else if (zoomInteractionType == PointerDetector.ZOOM_OUT && !PointerDetector.isZoomingOut) {
                if (PointerDetector.isZoomingIn) {
                    PointerDetector.isZoomingIn = false;
                    updateKeyToLG(false, PointerDetector.KEY_ZOOM_IN);
                }
                PointerDetector.isZoomingOut = true;
                updateKeyToLG(true, PointerDetector.KEY_ZOOM_OUT);
            }

            double angleDiff = getAngleDiff(pointer1.getTraveledAngle(), pointer2.getTraveledAngle());
            if (angleDiff <= 30 && pointer1.isMoving() && pointer2.isMoving() && zoomInteractionType == PointerDetector.ZOOM_NONE) {
                if (PointerDetector.isZoomingIn) {
                    PointerDetector.isZoomingIn = false;
                    updateKeyToLG(false, PointerDetector.KEY_ZOOM_IN);
                }
                if (PointerDetector.isZoomingOut) {
                    PointerDetector.isZoomingOut = false;
                    updateKeyToLG(false, PointerDetector.KEY_ZOOM_OUT);
                }

                LGConnectionManager.getInstance().addCommandToLG(new LGCommand("export DISPLAY=:" + (isOnChromeBook ? "1" : "0") + "; " +
                        "xdotool mouseup 1 " +
                        "mousemove --polar --sync 0 0 " +
                        "mousedown 2 " +
                        "mousemove --polar --sync " + (int) getAverageAngle(pointer1.getTraveledAngle(), pointer2.getTraveledAngle(), angleDiff) + " " + (isOnChromeBook ? 3 : 1) * (int) Math.min((pointer1.getTraveledDistance() + pointer2.getTraveledDistance()) / 2, 250) + " " +
                        "mouseup 2;", LGCommand.NON_CRITICAL_MESSAGE)
                );
            }
        }
        return true;
    }

    private void updateKeyToLG(boolean isActive, String key) {
        LGConnectionManager.getInstance().addCommandToLG(new LGCommand("export DISPLAY=:" + (isOnChromeBook ? "1" : "0") + "; " +
                "xdotool key" + (isActive ? "down" : "up") + " " + key + ";", LGCommand.CRITICAL_MESSAGE)
        );
    }

    private double getAngleDiff(double alpha, double beta) {
        double phi = Math.abs(beta - alpha) % 360; // This is either the distance or 360 - distance
        return phi > 180 ? 360 - phi : phi;
    }

    private double getAverageAngle(double alpha, double beta, double diff) {
        return alpha > beta ? alpha - (diff / 2) : beta - (diff / 2);
    }


    private void setNotMovable() {
        canMoveTime = System.currentTimeMillis() + 200;
    }

    private boolean canMove() {
        return canMoveTime <= System.currentTimeMillis();
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

