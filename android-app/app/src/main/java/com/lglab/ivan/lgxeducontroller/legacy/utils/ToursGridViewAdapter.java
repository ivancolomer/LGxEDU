package com.lglab.ivan.lgxeducontroller.legacy.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lglab.ivan.lgxeducontroller.R;
import com.lglab.ivan.lgxeducontroller.activities.navigate.POIController;
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
    private FragmentActivity activity;


    public ToursGridViewAdapter(List<Tour> tourList, Context context, FragmentActivity activity) {
        this.tourList = tourList;
        this.context = context;
        this.activity = activity;
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

        Button button = new Button(context);
        String displayName = currentTour.getName();
        button.setText(displayName);

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            button.setTextSize(15);
        }

        Drawable left = ResourcesCompat.getDrawable(context.getResources(), R.drawable.politour48, null);
        button.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);

        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        button.setMaxLines(1);

        button.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.button_rounded_grey, null));
        button.setLayoutParams(params);
        button.setOnClickListener(view1 -> {
            LaunchTourTask tourTask = new LaunchTourTask(currentTour);
            tourTask.execute();
        });

        return button;
    }


    private class LaunchTourTask extends AsyncTask<Void, Void, Boolean> {

        Tour currentTour;
        private ProgressDialog dialog;


        LaunchTourTask(Tour currentTour) {
            this.currentTour = currentTour;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = new ProgressDialog(context);
                String message = context.getResources().getString(R.string.viewing) + " " + this.currentTour.getName() + " " + context.getResources().getString(R.string.inLG);
                dialog.setMessage(message);
                dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(true);
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.stop_tour), (dialog, which) -> {
                    dialog.dismiss();
                    cancel(true);
                });
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnCancelListener(dialog -> cancel(true));
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
            if (!sendFirstTourPOI(pois.get(0)))
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

        private boolean sendFirstTourPOI(POI firstPoi) {
            return POIController.getInstance().moveToPOI(firstPoi, false);
        }

        private boolean sendTourPOI(Integer duration, POI poi) {
            try {
                Thread.sleep((long) ((duration * 2) * 1000));
                return POIController.getInstance().moveToPOI(poi, false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
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
                MaterialAlertDialogBuilder alertbox = new MaterialAlertDialogBuilder(activity);

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
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
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
