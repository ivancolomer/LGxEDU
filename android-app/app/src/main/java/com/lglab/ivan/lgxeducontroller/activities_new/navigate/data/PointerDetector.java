package com.lglab.ivan.lgxeducontroller.activities_new.navigate.data;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class PointerDetector {

    private final String TAG = "new_pointerdetector";

    public final static double MINIMUM_DISTANCE_MOVE = 12.5d;
    public final double MINIMUM_DISTANCE_ZOOM = 35.0d;
    public final String KEY_ZOOM_IN = "Page_Up";
    public final String KEY_ZOOM_OUT = "Page_Down";

    private static PointerDetector INSTANCE = null;

    public static PointerDetector getInstance() {
        if(INSTANCE == null)
            INSTANCE = new PointerDetector();
        return INSTANCE;
    }

    private Pointer[] pointers = new Pointer[2];
    private Action previousAction = Action.NONE;
    private Action currentAction = Action.NONE;

    private List<Pair<String, Boolean>> commands = new ArrayList<>();

    private double distanceBetweenFingers = 0.0d;

    public void preAction() {
        previousAction = currentAction;
        commands.clear();
    }

    public List<Pair<String, Boolean>> postAction() {
        if(currentAction != previousAction) {
            //launch commands for the end of the previous action and the initial of the next
            if(previousAction.finalCommand != "")
                commands.add(new Pair<>(previousAction.finalCommand, true));

            commands.add(new Pair<>(mouseMoveCommand(0, 0), true));

            if(currentAction.initialCommand != "")
                commands.add(new Pair<>(currentAction.initialCommand, true));
        }

        if(currentAction == Action.MOVE_POSITION) {
            commands.add(new Pair<>(mouseMoveCommand((int)pointers[0].getAngleInDegrees(), (int)pointers[0].getDistance()), false));
        } else if(currentAction != Action.NONE) {

            double distanceDiff = Math.sqrt(Math.pow(pointers[0].currentX - pointers[1].currentX, 2) + Math.pow(pointers[0].currentY - pointers[1].currentY, 2));


            double angleDiff = Pointer.getAngleDiff(pointers[0].getAngleInDegrees(), pointers[1].getAngleInDegrees());

            if (angleDiff <= 30 && pointers[0].hasMoved() && pointers[1].hasMoved() && zoomInteractionType == PointerDetector.ZOOM_NONE) {
                if (PointerDetector.isZoomingIn) {
                    PointerDetector.isZoomingIn = false;
                    updateKeyToLG(false, PointerDetector.KEY_ZOOM_IN);
                }
                if (PointerDetector.isZoomingOut) {
                    PointerDetector.isZoomingOut = false;
                    updateKeyToLG(false, PointerDetector.KEY_ZOOM_OUT);
                }

                LGConnectionManager.getInstance().addCommandToLG(new LGCommand("export DISPLAY=:" + (isOnChromeBook ? "1" : "0") + "; " +
                        "xdotool mouseup 2 " +
                        "mousemove --polar --sync 0 0 " +
                        "mousedown 2 " +
                        "mousemove --polar --sync " + (int) getAverageAngle(pointer1.getTraveledAngle(), pointer2.getTraveledAngle(), angleDiff) + " " + (isOnChromeBook ? 3 : 1) * (int) Math.min((pointer1.getTraveledDistance() + pointer2.getTraveledDistance()) / 2, 100) + " " +
                        "mouseup 2;", LGCommand.NON_CRITICAL_MESSAGE)
                );
            }
            commands.add(new Pair<>(mouseMoveCommand((int)pointers[0].getAngleInDegrees(), (int)pointers[0].getDistance()), false));
        }

        return commands;
    }

    public void addPointer(int id, float x, float y) {
        if(pointers[0] == null) {
            pointers[0] = new Pointer(id, x, y);
            currentAction = Action.MOVE_POSITION;
            return;
        }

        if(pointers[0].pointerId == id)
            return;

        if(pointers[1] == null) {
            pointers[1] = new Pointer(id, x, y);
            currentAction = Action.MOVE_ANGLE;
            return;
        }

        if(pointers[1].pointerId == id)
            return;

        Log.d(TAG, "can't add a third pointer");
    }

    public void updatePointer(int id, float x, float y) {
        for(Pointer p : pointers) {
            if(p != null && p.pointerId == id)
                p.update(x, y);
        }
    }

    public void removePointer(int id) {
        if(pointers[0] != null && pointers[0].pointerId == id) {
            pointers[0] = pointers[1];
            pointers[1] = null;
            currentAction = pointers[0] != null ? Action.MOVE_POSITION : Action.NONE;
        }
        else if(pointers[1] != null && pointers[1].pointerId == id) {
            pointers[1] = null;
            currentAction = Action.MOVE_POSITION;
        }
    }



    private double getDistanceFromPointerAfter(PointerDetector pointer) {
        return Math.sqrt(Math.pow(xAfter - pointer.xAfter, 2) + Math.pow(yAfter - pointer.yAfter, 2));
    }

    private double getDistanceFromPointerBefore(PointerDetector pointer) {
        return Math.sqrt(Math.pow(xBefore - pointer.xBefore, 2) + Math.pow(yBefore - pointer.yBefore, 2));
    }

    public short getZoomInteractionType(PointerDetector pointer) {
        double distance = getDistanceFromPointerAfter(pointer) - getDistanceFromPointerBefore(pointer);
        return xBefore == -1 ? ZOOM_NONE : distance >= MINIMUM_DISTANCE_ZOOM ? ZOOM_IN : distance <= -MINIMUM_DISTANCE_ZOOM ? ZOOM_OUT : ZOOM_NONE;
    }

    private static String mouseMoveCommand(int angle, int distance) {
        return "mousemove --polar " + angle + " " + distance;
    }

    public static class Pointer {

        private int pointerId;

        private float initialX, initialY;
        private float currentX, currentY;

        private float currentMovedX, currentMovedY;
        private float previousMovedX, previousMovedY;

        private boolean hasMoved;

        Pointer(int id, float x, float y) {
            pointerId = id;
            initialX = currentX = currentMovedX = previousMovedX = x;
            initialY = currentY = currentMovedY = previousMovedY = y;
            hasMoved = false;
        }

        public void update(float x, float y) {
            currentX = x;
            currentY = y;

            hasMoved = Math.sqrt(Math.pow(currentMovedX - x, 2) + Math.pow(currentMovedY - y, 2)) >= MINIMUM_DISTANCE_MOVE;
            if (hasMoved) {
                previousMovedX = currentMovedX;
                previousMovedY = currentMovedY;
                currentMovedX = x;
                currentMovedY = y;
            }
        }

        private double getMoved() {
            return Math.sqrt(Math.pow(currentX - initialX, 2) + Math.pow(currentY - initialY, 2));
        }

        private double getDistanceFromPrevious() {
            return Math.sqrt(Math.pow(currentX - previousX, 2) + Math.pow(currentY - previousY, 2));
        }

        public boolean hasMoved() {
            return hasMoved;
        }

        private double getAngleFromInitialInDegrees() {
            double angle = Math.toDegrees(Math.atan2(currentY - initialY, currentX - initialX));
            angle -= 270;
            while (angle < 0) {
                angle += 360;
            }
            return angle % 360;
        }

        private double getAngleFromPreviousInDegrees() {
            double angle = Math.toDegrees(Math.atan2(currentY - previousY, currentX - previousX));
            angle -= 270;
            while (angle < 0) {
                angle += 360;
            }
            return angle % 360;
        }

        private static double getAngleDiff(double alpha, double beta) {
            double phi = Math.abs(beta - alpha) % 360; // This is either the distance or 360 - distance
            return phi > 180 ? 360 - phi : phi;
        }

        private static double getAverageAngle(double alpha, double beta, double diff) {
            return alpha > beta ? alpha - (diff / 2) : beta - (diff / 2);
        }
    }

    public enum Action {
        NONE("", ""),
        MOVE_POSITION("mousedown 1", "mouseup 1"),
        MOVE_ANGLE("mousedown 2", "mouseup 2"),
        ZOOM_IN("keydown Page_Up", "keyup Page_Up"),
        ZOOM_OUT("keydown Page_Down", "keyup Page_Down");

        final String initialCommand, finalCommand;

        Action(String initialCommand, String finalCommand) {
            this.initialCommand = initialCommand;
            this.finalCommand = finalCommand;
        }
    }
}



