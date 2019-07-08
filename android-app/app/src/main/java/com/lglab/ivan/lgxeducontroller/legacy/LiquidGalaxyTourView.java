package com.lglab.ivan.lgxeducontroller.legacy;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.lglab.ivan.lgxeducontroller.BuildConfig;
import com.lglab.ivan.lgxeducontroller.activities_new.navigate.POIController;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;

import java.util.ArrayList;
import java.util.List;

import static com.lglab.ivan.lgxeducontroller.legacy.data.POIsContract.TourPOIsEntry;

public class LiquidGalaxyTourView extends AsyncTask<String, Void, String> {


    private static final String TAG;

    static {
        TAG = LiquidGalaxyTourView.class.getSimpleName();
    }

    private FragmentActivity poisFragmentAct;

    public LiquidGalaxyTourView(FragmentActivity activity) {
        this.poisFragmentAct = activity;
    }

    protected String doInBackground(String... params) {

        ArrayList<POI> pois = new ArrayList<>();
        ArrayList<Integer> poisDuration = new ArrayList<>();
        if (params == null || params.length == 0) {
            return "Error. There's no item selected.";
        }
        try {
            Cursor c = TourPOIsEntry.getPOIsByTourID(params[0]);
            while (c.moveToNext()) {
                int poiID = c.getInt(0);
                poisDuration.add(c.getInt(2));
                pois.add(getPOIData(poiID));
            }
            try {
                sendTourPOIs(pois, poisDuration);
                return BuildConfig.FLAVOR;
            } catch (IndexOutOfBoundsException e) {
                return "Error. There's probably no POI inside the Tour.";
            }
        } catch (Exception e2) {
            return "Error. Tour POIs cannot be read.";
        }
    }

    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        cancel(true);
        POISFragment.resetTourSettings();
        if (s.startsWith("Error")) {
            new Builder(this.poisFragmentAct)
                    .setTitle("Error")
                    .setMessage("There's probably no POI inside this Tour")
                    .setPositiveButton("OK", new TourDialog())
                    .show();
        }
    }

    public void setActivity(FragmentActivity activity) {
        this.poisFragmentAct = activity;
    }

    private POI getPOIData(int id) throws Exception {
        POI poi = POI.getPOIByIDFromDB(id);
        if (poi != null) {
            return poi;
        }
        throw new Exception("There is no POI with this features inside the data base. Try creating once correct.");
    }

    private void sendTourPOIs(List<POI> pois, List<Integer> poisDuration) {
        sendFirstTourPOI(pois.get(0));
        sendOtherTourPOIs(pois, poisDuration);
    }

    private void sendOtherTourPOIs(List<POI> pois, List<Integer> poisDuration) {
        int i = 1;
        while (!isCancelled()) {
            sendTourPOI(poisDuration.get(i), pois.get(i));
            i++;
            if (i == pois.size()) {
                i = 0;
            }
        }
    }

    private void sendFirstTourPOI(POI firstPoi) {
        if (POIController.getInstance().moveToPOI(firstPoi, false)) {
            Log.d(TAG, "First send");
        } else {
            Log.d(TAG, "Error in connection with Liquid Galaxy.");
        }
    }

    private void sendTourPOI(Integer duration, POI poi) {
        try {
            Thread.sleep((long) (duration * 1000));

            if (!POIController.getInstance().moveToPOI(poi, false)) {
                Log.d(TAG, "Error in connection with Liquid Galaxy.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "Error in duration of POIs.");
        }
    }

    protected void onCancelled() {
        super.onCancelled();
    }

    class TourDialog implements OnClickListener {
        TourDialog() {
        }

        public void onClick(DialogInterface arg0, int arg1) {
        }
    }
}
