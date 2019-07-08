package com.lglab.ivan.lgxeducontroller.connection;

public class LGCommand {

    public static final short NON_CRITICAL_MESSAGE = 0;
    public static final short CRITICAL_MESSAGE = 1;

    private final String command;
    private final short priorityType;
    private final TaskListener taskListener;

    public interface TaskListener {
        public void onFinished(String result);
    }

    public LGCommand(String command, short priorityType, TaskListener listener) {
        this.command = command;
        this.priorityType = priorityType;
        this.taskListener = listener;
    }

    String getCommand() {
        return command;
    }

    short getPriorityType() {
        return priorityType;
    }

    void doAction(String result) {
        if (this.taskListener != null) {
            this.taskListener.onFinished(result);
        }
    }
}
