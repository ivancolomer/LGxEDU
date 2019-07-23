package com.lglab.ivan.lgxeducontroller.games.geofinder;

import android.os.Parcel;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.Question;

import org.json.JSONException;
import org.json.JSONObject;

public class GeoFinder extends Game {
    public static final Creator<GeoFinder> CREATOR = new Creator<GeoFinder>() {
        @Override
        public GeoFinder createFromParcel(Parcel in) {
            return new GeoFinder(in);
        }

        @Override
        public GeoFinder[] newArray(int size) {
            return new GeoFinder[size];
        }
    };

    public GeoFinder() {
        super();
    }

    protected GeoFinder(Parcel in) {
        super(in);
    }

    @Override
    public JSONObject pack() throws JSONException {
        JSONObject obj = super.pack();
        //add wahtever you want more...
        return obj;
    }

    @Override
    public GeoFinder unpack(JSONObject obj) throws JSONException {
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
        return new GeoFinderQuestion();
    }

    @Override
    public GameManager createManager() {
        return new GeoFinderManager(this);
    }
}
