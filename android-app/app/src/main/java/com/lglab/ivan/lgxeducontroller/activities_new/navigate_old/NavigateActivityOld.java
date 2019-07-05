package com.lglab.ivan.lgxeducontroller.activities_new.navigate_old;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities_new.lgpc.LGPC;
import com.lglab.ivan.lgxeducontroller.activities_new.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.activities_new.navigate_old.data.PointerDetectorOld;
import com.lglab.ivan.lgxeducontroller.connection.ILGConnection;
import com.lglab.ivan.lgxeducontroller.connection.LGCommand;
import com.lglab.ivan.lgxeducontroller.connection.LGConnectionManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NavigateActivityOld extends AppCompatActivity implements ILGConnection {

    private static final String TAG = NavigateActivityOld.class.getSimpleName();

    private final HashMap<Integer, PointerDetectorOld> pointers = new HashMap<>();
    private long canMoveTime = 0;

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
        final int action = event.getActionMasked();
        int index;
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            if (pointers.size() < 2) {
                index = event.getActionIndex();
                pointers.put(event.getPointerId(index), new PointerDetectorOld(event.getX(index), event.getY(index)));

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
            if (PointerDetectorOld.isZoomingIn) {
                PointerDetectorOld.isZoomingIn = false;
                updateKeyToLG(false, PointerDetectorOld.KEY_ZOOM_IN);
            }
            if (PointerDetectorOld.isZoomingOut) {
                PointerDetectorOld.isZoomingOut = false;
                updateKeyToLG(false, PointerDetectorOld.KEY_ZOOM_OUT);
            }
        }

        if (pointers.size() == 0)
            return true;

        if (pointers.size() == 1) {
            PointerDetectorOld pointer = pointers.entrySet().iterator().next().getValue();
            if (pointer.isMoving() && canMove()) {
                //POIController.getInstance().moveXY(pointer.getTraveledAngle(), Math.min(pointer.getTraveledDistance(), 100) / 100.0d);
                LGConnectionManager.getInstance().addCommandToLG(new LGCommand("export DISPLAY=:" + (isOnChromeBook ? "1" : "0") + "; " +
                        "xdotool mouseup 1 " +
                        "mousemove --polar --sync 0 0 " +
                        "mousedown 1 " +
                        "mousemove --polar --sync " + (int) pointer.getTraveledAngle() + " " + (isOnChromeBook ? 3 : 0.75) * (int) Math.min(pointer.getTraveledDistance(), 100) + " " +
                        "mouseup 1;", LGCommand.NON_CRITICAL_MESSAGE)
                );
            }
        } else if (pointers.size() == 2) {
            Iterator<Map.Entry<Integer, PointerDetectorOld>> iterator = pointers.entrySet().iterator();
            PointerDetectorOld pointer1 = iterator.next().getValue();
            PointerDetectorOld pointer2 = iterator.next().getValue();

            setNotMovable();

            short zoomInteractionType = pointer1.getZoomInteractionType(pointer2);
            if (zoomInteractionType == PointerDetectorOld.ZOOM_IN && !PointerDetectorOld.isZoomingIn) {
                if (PointerDetectorOld.isZoomingOut) {
                    PointerDetectorOld.isZoomingOut = false;
                    updateKeyToLG(false, PointerDetectorOld.KEY_ZOOM_OUT);
                }
                PointerDetectorOld.isZoomingIn = true;
                updateKeyToLG(true, PointerDetectorOld.KEY_ZOOM_IN);
            } else if (zoomInteractionType == PointerDetectorOld.ZOOM_OUT && !PointerDetectorOld.isZoomingOut) {
                if (PointerDetectorOld.isZoomingIn) {
                    PointerDetectorOld.isZoomingIn = false;
                    updateKeyToLG(false, PointerDetectorOld.KEY_ZOOM_IN);
                }
                PointerDetectorOld.isZoomingOut = true;
                updateKeyToLG(true, PointerDetectorOld.KEY_ZOOM_OUT);
            }

            double angleDiff = getAngleDiff(pointer1.getTraveledAngle(), pointer2.getTraveledAngle());
            //Log.d("ConnectionManager", String.valueOf(angleDiff));
            if (angleDiff <= 30 && pointer1.isMoving() && pointer2.isMoving() && zoomInteractionType == PointerDetectorOld.ZOOM_NONE) {
                if (PointerDetectorOld.isZoomingIn) {
                    PointerDetectorOld.isZoomingIn = false;
                    updateKeyToLG(false, PointerDetectorOld.KEY_ZOOM_IN);
                }
                if (PointerDetectorOld.isZoomingOut) {
                    PointerDetectorOld.isZoomingOut = false;
                    updateKeyToLG(false, PointerDetectorOld.KEY_ZOOM_OUT);
                }

                LGConnectionManager.getInstance().addCommandToLG(new LGCommand("export DISPLAY=:" + (isOnChromeBook ? "1" : "0") + "; " +
                        "xdotool mouseup 2 " +
                        "mousemove --polar --sync 0 0 " +
                        "mousedown 2 " +
                        "mousemove --polar --sync " + (int) getAverageAngle(pointer1.getTraveledAngle(), pointer2.getTraveledAngle(), angleDiff) + " " + (isOnChromeBook ? 3 : 1) * (int) Math.min((pointer1.getTraveledDistance() + pointer2.getTraveledDistance()) / 2, 100) + " " +
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

