package com.lglab.ivan.lgxeducontroller.games.geofinder;

import android.os.Parcel;

import com.lglab.ivan.lgxeducontroller.games.Question;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;

import org.json.JSONException;
import org.json.JSONObject;

public class GeoFinderQuestion extends Question {

    public static final String TAG = GeoFinderQuestion.class.getSimpleName();
    public static final Creator<GeoFinderQuestion> CREATOR = new Creator<GeoFinderQuestion>() {
        @Override
        public GeoFinderQuestion createFromParcel(Parcel in) {
            return new GeoFinderQuestion(in);
        }

        @Override
        public GeoFinderQuestion[] newArray(int size) {
            return new GeoFinderQuestion[size];
        }
    };

    public String information;
    public POI poi;
    public POI initialPOI;
    public double area;

    public GeoFinderQuestion() {
        super("");
    }

    public GeoFinderQuestion(Parcel in) {
        super(in);
        information = in.readString();
        poi = in.readParcelable(POI.class.getClassLoader());
        initialPOI = in.readParcelable(POI.class.getClassLoader());
        area = in.readDouble();
    }

    @Override
    public JSONObject pack() throws JSONException {
        JSONObject obj = super.pack();

        if (information == null)
            throw new NullPointerException();
        obj.put("information", information);

        if (poi == null)
            throw new NullPointerException();
        obj.put("poi", poi.pack());

        if (initialPOI == null)
            throw new NullPointerException();
        obj.put("initial_poi", initialPOI.pack());

        if(area == 0.0d)
            throw new NullPointerException();
        obj.put("area", area);

        return obj;
    }

    @Override
    public GeoFinderQuestion unpack(JSONObject obj) throws JSONException {
        super.unpack(obj);
        information = obj.getString("information");
        poi = new POI().unpack(obj.getJSONObject("poi"));
        initialPOI = new POI().unpack(obj.getJSONObject("initial_poi"));
        area = obj.getDouble("area");

        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeString(information);
        parcel.writeParcelable(poi, flags);
        parcel.writeParcelable(initialPOI, flags);
        parcel.writeDouble(area);
    }
}
