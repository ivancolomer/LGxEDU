package com.lglab.ivan.lgxeducontroller.utils.Exceptions;

public class MissingInformationException extends Exception {

    private String exception;

    public MissingInformationException(String msg) {
        super(msg);
        this.exception = msg;
    }

    @Override
    public String toString() {
        return "Missing the following input: " + this.exception;
    }
}
