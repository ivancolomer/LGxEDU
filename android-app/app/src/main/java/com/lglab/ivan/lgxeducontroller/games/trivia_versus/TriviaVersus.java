package com.lglab.ivan.lgxeducontroller.games.trivia_versus;

import android.os.Parcel;

import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.Question;
import com.lglab.ivan.lgxeducontroller.games.trivia.Trivia;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaManager;
import com.lglab.ivan.lgxeducontroller.games.trivia.TriviaQuestion;

import org.json.JSONException;
import org.json.JSONObject;

public class TriviaVersus extends Trivia {

    public TriviaVersus() {
        super();
    }

    protected TriviaVersus(Parcel in) {
        super(in);
    }

    @Override
    public JSONObject pack() throws JSONException {
        JSONObject obj = super.pack();
        return obj;
    }

    @Override
    public Trivia unpack(JSONObject obj) throws JSONException {
        super.unpack(obj);
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
        return new TriviaQuestion();
    }

    @Override
    public GameManager createManager() {
        return new TriviaManager(this);
    }
}
