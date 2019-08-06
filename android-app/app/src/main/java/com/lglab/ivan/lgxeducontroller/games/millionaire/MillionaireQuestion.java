package com.lglab.ivan.lgxeducontroller.games.millionaire;

import android.os.Parcel;

import com.lglab.ivan.lgxeducontroller.games.Question;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;

import org.json.JSONException;
import org.json.JSONObject;

public class MillionaireQuestion extends Question {

    public static final String TAG = MillionaireQuestion.class.getSimpleName();
    public static final Creator<MillionaireQuestion> CREATOR = new Creator<MillionaireQuestion>() {
        @Override
        public MillionaireQuestion createFromParcel(Parcel in) {
            return new MillionaireQuestion(in);
        }

        @Override
        public MillionaireQuestion[] newArray(int size) {
            return new MillionaireQuestion[size];
        }
    };
    public static final int MAX_ANSWERS = 4;

    public int correctAnswer;
    public String[] answers;
    public String information;
    public POI poi;
    public POI initialPOI;

    public MillionaireQuestion() {
        super("");
        answers = new String[MAX_ANSWERS];
    }

    public MillionaireQuestion(Parcel in) {
        super(in);
        answers = new String[MAX_ANSWERS];

        correctAnswer = in.readInt();
        in.readStringArray(answers);
        information = in.readString();
        poi = in.readParcelable(POI.class.getClassLoader());
        initialPOI = in.readParcelable(POI.class.getClassLoader());
    }

    @Override
    public JSONObject pack() throws JSONException {
        JSONObject obj = super.pack();

        if (correctAnswer == 0)
            throw new NullPointerException();

        obj.put("correct_answer", correctAnswer);

        if (answers == null)
            throw new NullPointerException();

        for (int i = 0; i < MAX_ANSWERS; i++) {
            if (answers[i] == null)
                throw new NullPointerException();

            obj.put("answer" + (i + 1), answers[i]);
        }

        if (information == null)
            throw new NullPointerException();
        obj.put("information", information);

        if (poi == null)
            throw new NullPointerException();
        obj.put("poi", poi.pack());

        if (initialPOI == null)
            throw new NullPointerException();
        obj.put("initial_poi", initialPOI.pack());

        return obj;
    }

    @Override
    public MillionaireQuestion unpack(JSONObject obj) throws JSONException {
        super.unpack(obj);
        correctAnswer = obj.getInt("correct_answer");
        for (int i = 0; i < MAX_ANSWERS; i++) {
            answers[i] = obj.getString("answer" + (i + 1));
        }
        information = obj.getString("information");
        poi = new POI().unpack(obj.getJSONObject("poi"));
        initialPOI = new POI().unpack(obj.getJSONObject("initial_poi"));

        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeInt(correctAnswer);
        parcel.writeStringArray(answers);
        parcel.writeString(information);
        parcel.writeParcelable(poi, flags);
        parcel.writeParcelable(initialPOI, flags);
    }
}
