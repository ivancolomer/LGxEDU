package com.lglab.ivan.lgxeducontroller.connection;

public class LGCommand {

    public static final short NON_CRITICAL_MESSAGE = 0;
    public static final short CRITICAL_MESSAGE = 1;

    private final String command;
    private final short priorityType;

    public LGCommand(String command, short priorityType) {
        this.command = command;
        this.priorityType = priorityType;
    }

    public String getCommand() {
        return command;
    }

    public short getPriorityType() {
        return priorityType;
    }
}
