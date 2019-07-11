package com.lglab.ivan.lgxeducontroller.games.trivia;

import android.os.Parcel;

import com.lglab.ivan.lgxeducontroller.games.Game;
import com.lglab.ivan.lgxeducontroller.games.GameManager;
import com.lglab.ivan.lgxeducontroller.games.Question;

import org.json.JSONException;
import org.json.JSONObject;

public class Trivia extends Game {
    public static final Creator<Trivia> CREATOR = new Creator<Trivia>() {
        @Override
        public Trivia createFromParcel(Parcel in) {
            return new Trivia(in);
        }

        @Override
        public Trivia[] newArray(int size) {
            return new Trivia[size];
        }
    };

    public Trivia() {
        super();
    }

    protected Trivia(Parcel in) {
        super(in);
    }

    @Override
    public JSONObject pack() throws JSONException {
        JSONObject obj = super.pack();
        //add wahtever you want more...
        return obj;
    }

    @Override
    public Trivia unpack(JSONObject obj) throws JSONException {
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
        return new TriviaQuestion();
    }

    @Override
    public GameManager createManager() {
        return new TriviaManager(this);
    }
}
