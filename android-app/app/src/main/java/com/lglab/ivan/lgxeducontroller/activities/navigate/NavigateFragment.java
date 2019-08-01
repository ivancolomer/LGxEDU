package com.lglab.ivan.lgxeducontroller.activities.navigate;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.navigate.data.PointerDetector;
import com.lglab.ivan.lgxeducontroller.connection.ILGConnection;
import com.lglab.ivan.lgxeducontroller.connection.LGCommand;
import com.lglab.ivan.lgxeducontroller.connection.LGConnectionManager;

import java.util.List;

public class NavigateFragment extends Fragment implements ILGConnection {

    public AppCompatImageView wifiGif;
    private short currentStatus;

    private boolean isOnChromeBook = false;
    private boolean setInstanceToLGConnectionManager = true;

    public NavigateFragment(boolean setInstanceToLGConnectionManager) {
        this.setInstanceToLGConnectionManager = setInstanceToLGConnectionManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigate, container,false);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(setInstanceToLGConnectionManager)
            LGConnectionManager.getInstance().setActivity(this);

        currentStatus = 0;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        isOnChromeBook = prefs.getBoolean("isOnChromeBook", false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        wifiGif = view.findViewById(R.id.wifi_gif);
        view.setOnTouchListener((v, event) -> {
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
            if (commands.size() > 0) {
                StringBuilder command = new StringBuilder()
                        .append("export DISPLAY=:")
                        .append(isOnChromeBook ? "1" : "0")
                        .append("; xdotool ");

                boolean critical = false;
                for (Pair<String, Boolean> pair : commands) {
                    command.append(pair.first).append(" ");
                    critical = critical || pair.second;
                }

                LGConnectionManager.getInstance().addCommandToLG(new LGCommand(command.toString(), critical ? LGCommand.CRITICAL_MESSAGE : LGCommand.NON_CRITICAL_MESSAGE, null));
            }
            return true;
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        LGConnectionManager.getInstance().removeActivity(this);
    }

    @Override
    public void setStatus(short status) {
        FragmentActivity activity = getActivity();
        if(activity != null)
            activity.runOnUiThread(() -> {
            if (status != currentStatus && wifiGif != null) {
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
        });
    }
}
