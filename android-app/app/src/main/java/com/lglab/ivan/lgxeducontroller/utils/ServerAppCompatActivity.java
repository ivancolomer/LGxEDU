package com.lglab.ivan.lgxeducontroller.utils;

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
        new CreateWebServer().execute(handler);
    }

    private static class CreateWebServer extends AsyncTask<IAssistantHandler, Integer, Void> {
        protected Void doInBackground(IAssistantHandler... handlers) {
            if(WebServer.getInstance() == null) {
                WebServer.createServer(handlers[0]);
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
