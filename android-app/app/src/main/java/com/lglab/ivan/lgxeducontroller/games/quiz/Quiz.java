package com.lglab.ivan.lgxeducontroller.games.quiz;

import android.os.Parcel;
import android.os.Parcelable;

import com.lglab.ivan.lgxeducontroller.interfaces.IJsonPacker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Quiz implements IJsonPacker, Parcelable {
    public static final Creator<Quiz> CREATOR = new Creator<Quiz>() {
        @Override
        public Quiz createFromParcel(Parcel in) {
            return new Quiz(in);
        }

        @Override
        public Quiz[] newArray(int size) {
            return new Quiz[size];
        }
    };
    public long id;
    public String name;
    public String category;
    public List<Question> questions;

    public Quiz() {
        id = 0;
        name = "";
        category = "Geography";
        questions = new ArrayList<>();
    }

    protected Quiz(Parcel in) {
        this();
        id = in.readLong();
        name = in.readString();
        category = in.readString();
        in.readList(questions, getClass().getClassLoader());
    }

    @Override
    public JSONObject pack() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("name", name);
        obj.put("category", category);

        JSONArray array = new JSONArray();
        for (int i = 0; i < questions.size(); i++) {
            array.put(questions.get(i).pack());
        }
        obj.put("questions", array);

        return obj;
    }

    @Override
    public Quiz unpack(JSONObject obj) throws JSONException {
        name = obj.getString("name");
        category = obj.getString("category");

        JSONArray array = obj.getJSONArray("questions");
        for (int i = 0; i < array.length(); i++) {
            questions.add(new Question().unpack(array.getJSONObject(i)));
        }
        return this;
    }

    public String getNameForExporting() {
        return name.replaceAll("[:\\/*\"?|<> ]", "_") + ".json";
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "questions=" + questions +
                ", id=" + id +
                ", category='" + category + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeString(category);
        parcel.writeList(questions);
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }
}
