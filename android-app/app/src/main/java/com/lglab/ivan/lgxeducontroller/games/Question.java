package com.lglab.ivan.lgxeducontroller.games;

import android.os.Parcel;
import android.os.Parcelable;

import com.lglab.ivan.lgxeducontroller.interfaces.IJsonPacker;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Question implements IJsonPacker, Parcelable {

    private String question;

    public Question(String question) {
        this.question = question;
    }

    public Question(Parcel in) {
        this(in.readString());
    }

    @Override
    public JSONObject pack() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("question", question);
        return obj;
    }

    @Override
    public Question unpack(JSONObject obj) throws JSONException {
        question = obj.getString("question");
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(question);
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
