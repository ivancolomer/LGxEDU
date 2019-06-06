package com.lglab.ivan.lgxeducontroller.games.quiz;

import android.os.Parcel;
import android.os.Parcelable;

import com.lglab.ivan.lgxeducontroller.interfaces.IJsonPacker;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class Question implements IJsonPacker, Parcelable {

    public static final String TAG = Question.class.getSimpleName();
    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };
    public static final int MAX_ANSWERS = 4;
    public String question;
    public int correctAnswer;
    public String[] answers;
    public String information;
    public POI[] pois;
    public POI initialPOI;
    //Additional for game-use only
    public int selectedAnswer = 0;
    public int id;

    public Question() {
        answers = new String[MAX_ANSWERS];
        pois = new POI[MAX_ANSWERS];
    }

    public Question(Parcel in) {
        this();
        question = in.readString();
        correctAnswer = in.readInt();
        in.readStringArray(answers);
        information = in.readString();
        in.readTypedArray(pois, POI.CREATOR);
        initialPOI = in.readParcelable(POI.class.getClassLoader());
        selectedAnswer = in.readInt();
        id = in.readInt();
    }

    @Override
    public JSONObject pack() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("ID_question", id);
        obj.put("Question", question);
        obj.put("Correct_answer", correctAnswer);
        obj.put("Text_bubble", information);

        obj.put("Poi_0", initialPOI.pack());

        for (int i = 0; i < MAX_ANSWERS; i++) {
            obj.put("Answer_" + (i + 1), answers[i]);
        }

        for (int i = 0; i < MAX_ANSWERS; i++) {
            obj.put("Poi_" + (i + 1), pois[i].pack());
        }

        return obj;
    }

    @Override
    public Question unpack(JSONObject obj) throws JSONException {
        id = obj.getInt("ID_question");
        question = obj.getString("Question");
        correctAnswer = obj.getInt("Correct_answer");
        information = obj.getString("Text_bubble");
        initialPOI = new POI().unpack(obj.getJSONObject("Poi_0"));

        for (int i = 0; i < MAX_ANSWERS; i++) {
            answers[i] = obj.getString("Answer_" + (i + 1));
        }

        for (int i = 0; i < MAX_ANSWERS; i++) {
            pois[i] = new POI().unpack(obj.getJSONObject("Poi_" + (i + 1)));
        }

        return this;
    }

    @Override
    public String toString() {
        return "Question{" +
                "question='" + question + '\'' +
                ", correctAnswer=" + correctAnswer +
                ", answers=" + Arrays.toString(answers) +
                ", information='" + information + '\'' +
                ", pois=" + Arrays.toString(pois) +
                ", initialPOI=" + initialPOI +
                ", selectedAnswer=" + selectedAnswer +
                ", id=" + id +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(question);
        parcel.writeInt(correctAnswer);
        parcel.writeStringArray(answers);
        parcel.writeString(information);
        parcel.writeTypedArray(pois, flags);
        parcel.writeParcelable(initialPOI, flags);
        parcel.writeInt(selectedAnswer);
        parcel.writeInt(id);
    }
}
