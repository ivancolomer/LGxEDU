package com.lglab.ivan.lgxeducontroller.activities_new.navigate;

import com.lglab.ivan.lgxeducontroller.connection.LGCommand;
import com.lglab.ivan.lgxeducontroller.connection.LGConnectionManager;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;

public class POIController {

    private static POIController INSTANCE = null;

    public static final POI EARTH_POI = new POI()
            .setLongitude(10.52668d)
            .setLatitude(40.085941d)
            .setAltitude(0.0d)
            .setHeading(0.0d)
            .setTilt(0.0d)
            .setRange(10000000.0d)
            .setAltitudeMode("relativeToSeaFloor");

    public synchronized static POIController getInstance() {
        if (INSTANCE == null)
            INSTANCE = new POIController();
        return INSTANCE;
    }

    private static double STEP_XY = 0.000001; //0.0001% of RANGE

    private POI currentPOI;
    private POI previousPOI;

    private POIController() {
        currentPOI = EARTH_POI;
        moveToPOI(EARTH_POI, true);
    }

    public synchronized boolean moveToPOI(POI poi, boolean inBackground) {
        previousPOI = new POI(currentPOI);
        currentPOI = new POI(poi);
        return sendPoiToLG(inBackground);
    }

    public synchronized void moveXY(double angle, double percentDistance) {
        //.setLongitude() [-180 to +180]: X (cos)
        //.setLatitude() [-90 to +90]: Y (sin)

        POI newPoi = new POI(currentPOI);
        newPoi.setLongitude(newPoi.getLongitude() + Math.cos(angle) * percentDistance * STEP_XY * newPoi.getRange());
        while (newPoi.getLongitude() > 180) {
            newPoi.setLongitude(newPoi.getLongitude() - 360);
        }
        while (newPoi.getLongitude() < -180) {
            newPoi.setLongitude(newPoi.getLongitude() + 360);
        }

        newPoi.setLatitude(newPoi.getLatitude() - Math.sin(angle) * percentDistance * STEP_XY * newPoi.getRange());
        while (newPoi.getLatitude() > 90) {
            newPoi.setLatitude(newPoi.getLatitude() - 180);
        }
        while (newPoi.getLatitude() < -90) {
            newPoi.setLatitude(newPoi.getLatitude() + 180);
        }

        moveToPOI(newPoi, true);
    }

    public synchronized void moveCameraAngle(double angle, double percentDistance) {
        //.setTilt() [0 to 90]: the angle between what you see and the earth (90 means you see horizon) (the sin of the angle)
        //.setHeading() [-180 to 180]: compass degrees (the cos of the angle)
    }

    public synchronized void zoomIn(double percent) {
        //.setRange() [0 to 999999]
    }

    public synchronized void zoomOut(double percent) {
        //.setRange() [0 to 999999]
    }

    private synchronized boolean sendPoiToLG(boolean inBackground) {
        if (inBackground) {
            LGConnectionManager.getInstance().addCommandToLG(new LGCommand(buildCommand(currentPOI), LGCommand.CRITICAL_MESSAGE, null));
            return true;
        }

        LGConnectionManager.getInstance().addCommandToLG(new LGCommand(buildCommand(currentPOI), LGCommand.CRITICAL_MESSAGE, (String result) -> currentPOI = new POI(previousPOI)));
        return true;
    }

    private static String buildCommand(POI poi) {
        return "echo 'flytoview=<gx:duration>1.5</gx:duration><gx:flyToMode>smooth</gx:flyToMode><LookAt><longitude>" + poi.getLongitude() + "</longitude><latitude>" + poi.getLatitude() + "</latitude><altitude>" + poi.getAltitude() + "</altitude><heading>" + poi.getHeading() + "</heading><tilt>" + poi.getTilt() + "</tilt><range>" + poi.getRange() + "</range><gx:altitudeMode>" + poi.getAltitudeMode() + "</gx:altitudeMode></LookAt>' > /tmp/query.txt";
    }
}
