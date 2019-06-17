package com.lglab.ivan.lgxeducontroller.activities_new.navigate.data;

public class PointerDetector {

    public final static int MINIUM_DISTANCE_MOVE = 8;
    public final static int MINIUM_DISTANCE_ZOOM = 30;


    public static final String KEY_ZOOM_IN = "Page_Up";
    public static final String KEY_ZOOM_OUT = "Page_Down";

    public static final short ZOOM_NONE = 0;
    public static final short ZOOM_IN = 1;
    public static final short ZOOM_OUT = 2;

    public static boolean isZoomingIn = false;
    public static boolean isZoomingOut = false;

    private float xAfter;
    private float yAfter;

    private float xBefore;
    private float yBefore;

    public PointerDetector(float x, float y) {
        xAfter = -1;
        yAfter = -1;
        update(x, y);
    }

    public double getTraveledDistance() {
        return Math.sqrt(Math.pow(xAfter - xBefore, 2) + Math.pow(yAfter - yBefore, 2));
    }

    public double getTraveledAngle() {
        return Math.atan2(yAfter - yBefore, xAfter - xBefore) - 1.5d * Math.PI;
    }

    public void update(float x, float y) {
        if (Math.sqrt(Math.pow(xAfter - x, 2) + Math.pow(yAfter - y, 2)) >= MINIUM_DISTANCE_MOVE) {
            xBefore = xAfter;
            yBefore = yAfter;

            xAfter = x;
            yAfter = y;
        }
    }

    public boolean isMoving() {
        return (xBefore != xAfter || yBefore != yAfter) && xBefore != -1;
    }

    private double getDistanceFromPointerAfter(PointerDetector pointer) {
        return Math.sqrt(Math.pow(xAfter - pointer.xAfter, 2) + Math.pow(yAfter - pointer.yAfter, 2));
    }

    private double getDistanceFromPointerBefore(PointerDetector pointer) {
        return Math.sqrt(Math.pow(xBefore - pointer.xBefore, 2) + Math.pow(yBefore - pointer.yBefore, 2));
    }

    public short getZoomInteractionType(PointerDetector pointer) {
        double distance = getDistanceFromPointerAfter(pointer) - getDistanceFromPointerBefore(pointer);
        return xBefore == -1 ? ZOOM_NONE : distance >= MINIUM_DISTANCE_ZOOM ? ZOOM_IN : distance <= -MINIUM_DISTANCE_ZOOM ? ZOOM_OUT : ZOOM_NONE;
    }
}



