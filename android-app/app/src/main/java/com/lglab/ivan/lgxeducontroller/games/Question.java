package com.lglab.ivan.lgxeducontroller.games;

import com.lglab.ivan.lgxeducontroller.interfaces.IJsonPacker;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Question implements IJsonPacker {

    private String name;

    public JSONObject pack() throws JSONException {
        throw new UnsupportedOperationException();
    }

    public Question unpack(JSONObject obj) throws JSONException {
        throw new UnsupportedOperationException();
    }

}
