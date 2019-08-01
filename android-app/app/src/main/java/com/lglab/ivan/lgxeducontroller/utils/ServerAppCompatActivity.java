package com.lglab.ivan.lgxeducontroller.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class ServerAppCompatActivity extends AppCompatActivity {

    private IAssistantHandler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new AssistantHandler(this);
        new CreateWebServer().execute(getApplicationContext(), handler);
    }

    private static class CreateWebServer extends AsyncTask<Object, Integer, Void> {
        protected Void doInBackground(Object... handlers) {
            if(WebServer.getInstance() == null) {
                WebServer.createServer((Context)handlers[0], (IAssistantHandler)handlers[1]);
            }
            return null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        WebServer.setHandler(handler);
    }

    @Override
    protected void onStop() {
        super.onStop();
        WebServer.removeHandler(handler);
    }
}
