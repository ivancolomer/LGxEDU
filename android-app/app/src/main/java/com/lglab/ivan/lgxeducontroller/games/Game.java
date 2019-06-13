package com.lglab.ivan.lgxeducontroller.games;

import com.lglab.ivan.lgxeducontroller.interfaces.IJsonPacker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public abstract class Game implements IJsonPacker {

    private String name;
    private String category;
    private GameEnum type;
    private List<Question> questions;

    public Game unpack(JSONObject obj) throws JSONException {
        name = obj.getString("name");
        category = obj.getString("category");
        type = GameEnum.findByName(obj.getString("type"));
        if(type == null)
            throw new JSONException("No game type found");

        unpackQuestions(obj.getJSONArray("questions"));

        /*for (int i = 0; i < array.length(); i++) {
            questions.add(new Question().unpack(array.getJSONObject(i)));
        }*/
        return this;
    }

    public abstract void unpackQuestions(JSONArray array) throws JSONException;

    public JSONObject pack() throws JSONException {
        throw new UnsupportedOperationException();
    }

    public abstract void start();
    public abstract void stop();
    public abstract void end();
    public abstract void selectAnswer();
    public abstract void changeScreen();
}
