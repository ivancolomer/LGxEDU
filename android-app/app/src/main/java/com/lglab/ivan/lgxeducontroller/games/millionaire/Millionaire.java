package com.lglab.ivan.lgxeducontroller.games.millionaire;

import android.os.Parcel;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.Question;

import org.json.JSONException;
import org.json.JSONObject;

public class Millionaire extends Game {
    public static final Creator<Millionaire> CREATOR = new Creator<Millionaire>() {
        @Override
        public Millionaire createFromParcel(Parcel in) {
            return new Millionaire(in);
        }

        @Override
        public Millionaire[] newArray(int size) {
            return new Millionaire[size];
        }
    };

    public Millionaire() {
        super();
    }

    protected Millionaire(Parcel in) {
        super(in);
    }

    @Override
    public JSONObject pack() throws JSONException {
        JSONObject obj = super.pack();
        //add wahtever you want more...
        return obj;
    }

    @Override
    public Millionaire unpack(JSONObject obj) throws JSONException {
        super.unpack(obj);
        //add whatever you want more here...
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
    }

    @Override
    public Question createQuestion() {
        return new MillionaireQuestion();
    }

    @Override
    public GameManager createManager() {
        return new MillionaireManager(this);
    }
}
