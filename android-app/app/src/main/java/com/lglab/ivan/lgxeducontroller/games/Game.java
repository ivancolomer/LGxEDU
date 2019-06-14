package com.lglab.ivan.lgxeducontroller.games;

import android.os.Parcel;
import android.os.Parcelable;

import com.lglab.ivan.lgxeducontroller.interfaces.IJsonPacker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class Game implements IJsonPacker, Parcelable {

    private long id;
    private String name;
    private String category;
    private GameEnum type;
    private List<Question> questions;

    public Game() {
        id = 0;
        name = "";
        category = "";
        type = null;
        questions = new ArrayList<>();
    }

    public Game(Parcel in) {
        this();
        id = in.readLong();
        name = in.readString();
        category = in.readString();
        type = GameEnum.findByName(in.readString());
        in.readArrayList(createQuestion().getClass().getClassLoader());
    }

    @Override
    public Game unpack(JSONObject obj) throws JSONException {
        name = obj.getString("name");
        category = obj.getString("category");
        type = GameEnum.findByName(obj.getString("type"));
        if(type == null)
            throw new JSONException("No game type found!");

        unpackQuestions(obj.getJSONArray("questions"));
        return this;
    }

    @Override
    public JSONObject pack() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("name", name);
        obj.put("category", category);
        obj.put("type", type.name());

        JSONArray array = new JSONArray();
        for (int i = 0; i < questions.size(); i++) {
            array.put(questions.get(i).pack());
        }
        obj.put("questions", array);

        return obj;
    }

    private void unpackQuestions(JSONArray array) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            Question q = createQuestion();
            if(q != null)
                questions.add(q.unpack(array.getJSONObject(i)));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeString(category);
        parcel.writeString(type.name());
        parcel.writeList(questions);
    }

    public String getNameForExporting() {
        return name.replaceAll("[:\\/*\"?|<> ]", "_") + ".json";
    }

    public abstract Question createQuestion();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public GameEnum getType() {
        return type;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
