package com.lglab.ivan.lgxeducontroller.games.trivia;

import android.os.Parcel;

import com.lglab.ivan.lgxeducontroller.games.Question;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;

import org.json.JSONException;
import org.json.JSONObject;

public class TriviaQuestion extends Question {

    public static final String TAG = TriviaQuestion.class.getSimpleName();
    public static final Creator<TriviaQuestion> CREATOR = new Creator<TriviaQuestion>() {
        @Override
        public TriviaQuestion createFromParcel(Parcel in) {
            return new TriviaQuestion(in);
        }

        @Override
        public TriviaQuestion[] newArray(int size) {
            return new TriviaQuestion[size];
        }
    };
    public static final int MAX_ANSWERS = 4;

    public int correctAnswer;
    public String[] answers;
    public String information;
    public POI[] pois;
    public POI initialPOI;

    public TriviaQuestion() {
        super("");
        answers = new String[MAX_ANSWERS];
        pois = new POI[MAX_ANSWERS];
    }

    public TriviaQuestion(Parcel in) {
        super(in);
        answers = new String[MAX_ANSWERS];
        pois = new POI[MAX_ANSWERS];

        correctAnswer = in.readInt();
        in.readStringArray(answers);
        information = in.readString();
        in.readTypedArray(pois, POI.CREATOR);
        initialPOI = in.readParcelable(POI.class.getClassLoader());
    }

    @Override
    public JSONObject pack() throws JSONException {
        JSONObject obj = super.pack();

        if(correctAnswer == 0)
            throw new NullPointerException();

        obj.put("correct_answer", correctAnswer);

        if(information == null)
            throw new NullPointerException();

        obj.put("information", information);

        if(initialPOI == null)
            throw new NullPointerException();

        obj.put("initial_poi", initialPOI.pack());

        if(answers == null)
            throw new NullPointerException();

        for (int i = 0; i < MAX_ANSWERS; i++) {
            if(answers[i] == null)
                throw new NullPointerException();

            obj.put("answer" + (i + 1), answers[i]);
        }

        for (int i = 0; i < MAX_ANSWERS; i++) {
            if(pois[i] == null)
                throw new NullPointerException();

            obj.put("poi" + (i + 1), pois[i].pack());
        }

        return obj;
    }

    @Override
    public TriviaQuestion unpack(JSONObject obj) throws JSONException {
        super.unpack(obj);
        correctAnswer = obj.getInt("correct_answer");
        information = obj.getString("information");
        initialPOI = new POI().unpack(obj.getJSONObject("initial_poi"));

        for (int i = 0; i < MAX_ANSWERS; i++) {
            answers[i] = obj.getString("answer" + (i + 1));
        }

        for (int i = 0; i < MAX_ANSWERS; i++) {
            pois[i] = new POI().unpack(obj.getJSONObject("poi" + (i + 1)));
        }

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
        parcel.writeTypedArray(pois, flags);
        parcel.writeParcelable(initialPOI, flags);
    }
}
