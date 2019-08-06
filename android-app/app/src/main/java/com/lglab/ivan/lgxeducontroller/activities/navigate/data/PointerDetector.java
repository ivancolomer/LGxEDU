package com.lglab.ivan.lgxeducontroller.activities.navigate.data;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class PointerDetector {

    private final static double MINIMUM_DISTANCE_MOVE = 12.5d;

    private static PointerDetector INSTANCE = null;

    public static PointerDetector getInstance() {
        if (INSTANCE == null)
            INSTANCE = new PointerDetector();
        return INSTANCE;
    }

    private Pointer[] pointers = new Pointer[2];
    private Action previousAction = Action.NONE;
    private Action currentAction = Action.NONE;

    private long lastTimeSentRepetitiveMove = 0;

    private void restartLastTimeSentRepetitiveMove() {
        lastTimeSentRepetitiveMove = 0;
    }

    private boolean canSendRepetitiveMove() {
        if(lastTimeSentRepetitiveMove == 0 || System.currentTimeMillis() - lastTimeSentRepetitiveMove >= 250) {
            lastTimeSentRepetitiveMove = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private List<Pair<String, Boolean>> commands = new ArrayList<>();

    public void preAction() {
        previousAction = currentAction;
        commands.clear();
    }

    public List<Pair<String, Boolean>> postAction() {
        if (currentAction != previousAction) {
            if (!previousAction.finalCommand.equals(""))
                commands.add(new Pair<>(previousAction.finalCommand, true));
            if (currentAction == Action.NONE || previousAction == Action.NONE)
                commands.add(new Pair<>(mouseMoveCommand(0, 0), true));
            if (!currentAction.initialCommand.equals(""))
                commands.add(new Pair<>(currentAction.initialCommand, true));

            restartLastTimeSentRepetitiveMove();
        }

        if (currentAction == Action.MOVE_POSITION) {
            if(pointers[0].hasMoved || canSendRepetitiveMove()) {
                commands.add(new Pair<>(mouseMoveCommand((int) pointers[0].getAngleFromInitial(), (int) pointers[0].getDistanceFromInitial()), false));
            }
        } else if (currentAction != Action.NONE) {

            double angleDiff = Pointer.getAngleDiff(pointers[0].getAngleFromPrevious(), pointers[1].getAngleFromPrevious());
            if (angleDiff >= 150) {
                double distanceDiff = Math.sqrt(Math.pow(pointers[0].currentX - pointers[1].currentX, 2) + Math.pow(pointers[0].currentY - pointers[1].currentY, 2));
                distanceDiff -= Math.sqrt(Math.pow(pointers[0].previousMovedX - pointers[1].previousMovedX, 2) + Math.pow(pointers[0].previousMovedY - pointers[1].previousMovedY, 2));

                if (distanceDiff >= 0) {
                    if (currentAction != Action.ZOOM_IN) {
                        if (currentAction == Action.MOVE_ANGLE) {
                            pointers[0].changedToZoomAction();
                            pointers[1].changedToZoomAction();
                        }

                        commands.add(new Pair<>(previousAction.finalCommand, true));
                        currentAction = Action.ZOOM_IN;
                        commands.add(new Pair<>(currentAction.initialCommand, true));
                    }
                    return commands;
                } else if (distanceDiff <= 0) {
                    if (currentAction != Action.ZOOM_OUT) {
                        if (currentAction == Action.MOVE_ANGLE) {
                            pointers[0].changedToZoomAction();
                            pointers[1].changedToZoomAction();
                        }

                        commands.add(new Pair<>(previousAction.finalCommand, true));
                        currentAction = Action.ZOOM_OUT;
                        commands.add(new Pair<>(currentAction.initialCommand, true));
                    }
                    return commands;
                }
            } else if (angleDiff <= 30) {
                if (currentAction != Action.MOVE_ANGLE) {
                    commands.add(new Pair<>(previousAction.finalCommand, true));
                    currentAction = Action.MOVE_ANGLE;
                    commands.add(new Pair<>(currentAction.initialCommand, true));

                    pointers[0].changedFromZoomAction();
                    pointers[1].changedFromZoomAction();
                }

                if(pointers[0].hasMoved || canSendRepetitiveMove()) {
                    commands.add(new Pair<>(mouseMoveCommand((int) pointers[0].getAngleFromInitial(), (int) pointers[0].getDistanceFromInitial()), false));
                }
            }
        }

        return commands;
    }

    public void addPointer(int id, float x, float y) {
        if (pointers[0] == null) {
            pointers[0] = new Pointer(id, x, y);
            currentAction = Action.MOVE_POSITION;
            return;
        }

        if (pointers[0].pointerId == id)
            return;

        if (pointers[1] == null) {
            pointers[1] = new Pointer(id, x, y);
            currentAction = Action.MOVE_ANGLE;
            return;
        }

        if (pointers[1].pointerId == id)
            return;

        Log.e("PointerDetector", "Can't add a third pointer!!!");
    }

    public void updatePointer(int id, float x, float y) {
        for (Pointer p : pointers) {
            if (p != null && p.pointerId == id)
                p.update(x, y);
        }
    }

    public void removePointer(int id) {
        if (pointers[0] != null && pointers[0].pointerId == id) {
            Pointer removedPointer = pointers[0];
            pointers[0] = pointers[1];
            pointers[1] = null;
            currentAction = pointers[0] != null ? Action.MOVE_POSITION : Action.NONE;
            if (currentAction == Action.MOVE_POSITION) {
                if (previousAction == Action.ZOOM_IN || previousAction == Action.ZOOM_OUT) {
                    pointers[0].changedFromZoomAction();
                    removedPointer.changedFromZoomAction();
                }
                pointers[0].initialX = pointers[0].currentX - (removedPointer.currentX - removedPointer.initialX);
                pointers[0].initialY = pointers[0].currentY - (removedPointer.currentY - removedPointer.initialY);
            }

        } else if (pointers[1] != null && pointers[1].pointerId == id) {
            pointers[1] = null;
            currentAction = Action.MOVE_POSITION;
            if (previousAction == Action.ZOOM_IN || previousAction == Action.ZOOM_OUT) {
                pointers[0].changedFromZoomAction();
            }
        }
    }

    public static String mouseMoveCommand(int angle, int distance) {
        return "mousemove --polar " + angle + " " + distance;
    }

    public static class Pointer {

        private int pointerId;

        private float initialX, initialY;
        private float currentX, currentY;

        private float currentMovedX, currentMovedY;
        private float previousMovedX, previousMovedY;

        private float savedX, savedY;

        private boolean hasMoved;

        private Pointer(int id, float x, float y) {
            pointerId = id;
            initialX = currentX = currentMovedX = previousMovedX = x;
            initialY = currentY = currentMovedY = previousMovedY = y;
            savedX = savedY = 0;
            hasMoved = false;
        }

        private void update(float x, float y) {
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

        private void changedToZoomAction() {
            savedX = currentX;
            savedY = currentY;
        }

        private void changedFromZoomAction() {
            initialX += currentX - savedX;
            initialY += currentY - savedY;
        }

        private double getDistanceFromInitial() {
            return Math.sqrt(Math.pow(currentX - initialX, 2) + Math.pow(currentY - initialY, 2));
        }

        private double getAngleFromInitial() {
            double angle = Math.toDegrees(Math.atan2(currentY - initialY, currentX - initialX));
            angle -= 270;
            while (angle < 0) {
                angle += 360;
            }
            return angle % 360;
        }

        private double getAngleFromPrevious() {
            double angle = Math.toDegrees(Math.atan2(currentY - previousMovedY, currentX - previousMovedX));
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

        /*private static double getAverageAngle(double alpha, double beta, double diff) {
            return alpha > beta ? alpha - (diff / 2) : beta - (diff / 2);
        }*/
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



