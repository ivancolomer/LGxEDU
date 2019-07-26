package com.lglab.ivan.lgxeducontroller.legacy.utils;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.connection.LGCommand;
import com.lglab.ivan.lgxeducontroller.connection.LGConnectionManager;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;
import com.lglab.ivan.lgxeducontroller.legacy.beans.Tour;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan Josa on 7/07/16.
 */
public class ToursGridViewAdapter extends BaseAdapter {

    private List<Tour> tourList;
    private Context context;

    public ToursGridViewAdapter(List<Tour> tourList, Context context) {
        this.tourList = tourList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return this.tourList.size();
    }

    @Override
    public Object getItem(int i) {
        return this.tourList.get(i);

    }

    @Override
    public long getItemId(int i) {
        return this.tourList.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Tour currentTour = this.tourList.get(i);

        MaterialButton button = new MaterialButton(context);
        String displayName = currentTour.getName();
        button.setText(displayName);
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) context.getResources().getDimension(R.dimen._8ssp));

        Drawable left = AppCompatResources.getDrawable(context, R.drawable.politour48);
        Bitmap bitmap = ((BitmapDrawable) left).getBitmap();
        left = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, (int) context.getResources().getDimension(R.dimen._6sdp), (int) context.getResources().getDimension(R.dimen._6sdp), true));
        DrawableCompat.setTint(left, ContextCompat.getColor(context, R.color.whiteGrey));

        button.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);

        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        button.setMaxLines(1);

        button.setBackground(AppCompatResources.getDrawable(context, R.drawable.button_rounded_grey));
        button.setLayoutParams(params);
        button.setOnClickListener(view1 -> {
            LaunchTourTask tourTask = new LaunchTourTask(currentTour, context);
            tourTask.execute();
        });

        return button;
    }


    private static class LaunchTourTask extends AsyncTask<Void, Void, Boolean> {

        Tour currentTour;
        private AlertDialog dialog;
        private Context context;


        private LaunchTourTask(Tour currentTour, Context context) {
            this.currentTour = currentTour;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);

                String message = context.getResources().getString(R.string.viewing) + " " + this.currentTour.getName() + " " + context.getResources().getString(R.string.inLG);
                builder.setMessage(message);
                builder.setView(R.layout.progress);
                builder.setNegativeButton(context.getResources().getString(R.string.stop_tour), (dialog, id) -> dialog.cancel());

                dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(true);

                dialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            List<POI> pois = new ArrayList<>();
            List<Integer> poisDuration = new ArrayList<>();

            try {
                Cursor tourPoiCursor = POIsContract.TourPOIsEntry.getPOIsByTourID(String.valueOf(this.currentTour.getId()));
                while (tourPoiCursor.moveToNext()) {
                    int poiID = tourPoiCursor.getInt(tourPoiCursor.getColumnIndex(POIsContract.TourPOIsEntry.COLUMN_POI_ID));
                    poisDuration.add(tourPoiCursor.getInt(tourPoiCursor.getColumnIndex(POIsContract.TourPOIsEntry.COLUMN_POI_DURATION)));
                    pois.add(getPOIData(poiID));
                }
                try {
                    if (!sendTourPOIs(pois, poisDuration)) {
                        return false;
                    }
                    return true;
                } catch (IndexOutOfBoundsException e) {
                    return false;
                }
            } catch (Exception e2) {
                return false;
            }
        }

        private boolean sendTourPOIs(List<POI> pois, List<Integer> poisDuration) {
            if (!sendTourPOI(0, pois.get(0)))
                return false;
            return sendOtherTourPOIs(pois, poisDuration);
        }

        private boolean sendOtherTourPOIs(List<POI> pois, List<Integer> poisDuration) {
            int i = 1;
            while (!isCancelled()) {
                if (!sendTourPOI(poisDuration.get(i), pois.get(i % pois.size())))
                    return false;
                i++;
            }
            return true;
        }

        private boolean sendTourPOI(int duration, POI firstPoi) {
            LGCommand lgCommand = POIController.getInstance().moveToPOI(firstPoi, response ->  { });

            try {
                if(duration != 0)
                    Thread.sleep((long) ((duration * 2) * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //TIMEOUT 5 seconds BEFORE REMOVING COMMAND FORM LG AND DISPLAYING CONNECTION FAILURE MESSAGE
            long currentTime = System.currentTimeMillis();
            try {
                while(System.currentTimeMillis() - currentTime <= 5000) {
                    if(!LGConnectionManager.getInstance().containsCommandFromLG(lgCommand))
                        break;
                    Thread.sleep(100);
                }
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }

            return !LGConnectionManager.getInstance().removeCommandFromLG(lgCommand);
        }


        private POI getPOIData(int id) throws Exception {
            POI poi = POI.getPOIByIDFromDB(id);
            if (poi != null) {
                return poi;
            }
            throw new Exception("There is no POI with this features inside the data base. Try creating once correct.");
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            resetTourSettings();
            if (!success) {
                MaterialAlertDialogBuilder alertbox = new MaterialAlertDialogBuilder(context);

                // set the message to display
                alertbox.setTitle("Error");
                alertbox.setMessage("There's probably no POI inside this Tour");

                // set a positive/yes button and create a listener
                alertbox.setPositiveButton("OK", (arg0, arg1) -> {
                });
                alertbox.show();
            }
            if (dialog != null) {
                dialog.hide();
                dialog.dismiss();
            }
        }

        void resetTourSettings() {
            this.cancel(true);
            showStopAlert();
        }


        private void showStopAlert() {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            builder.setMessage("The tour running on LG has been stopped.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {
                        //do things
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }
}
