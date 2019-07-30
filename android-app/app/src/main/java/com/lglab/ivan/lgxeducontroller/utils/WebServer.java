package com.lglab.ivan.lgxeducontroller.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    private static WebServer server;

    public static boolean createServer(IAssistantHandler handler) {
        try {
            server = new WebServer(handler);
            handler.onServerCreated(Utils.getIPAddress(true), String.valueOf(server.getListeningPort()));
            return true;
        } catch (IOException e) {
            Log.e("WEBSERVER", e.toString());
            return false;
        }
    }

    public synchronized static WebServer getInstance() {
        return server;
    }

    public static void setHandler(IAssistantHandler handler) {
        if(server != null) {
            server.handler = handler;
        }
    }

    public static void removeHandler(IAssistantHandler oldHandler) {
        if(server != null && (oldHandler == null || server.handler == oldHandler)) {
            server.handler = new IAssistantHandler() {
                @Override
                public AssistantHandler.Result handleNewResponse(Method method, String[] uri, Map<String, List<String>> parms) {
                    Log.d("WEBSERVER", method + " '" + uri[1] + "' ");
                    return AssistantHandler.Result.NOT_IN_ACTIVITY;
                }

                @Override
                public void onServerCreated(String ip, String port) {
                    Log.d("WEBSERVER", ip + " '" + port + "' ");
                }
            };
        }
    }

    private IAssistantHandler handler;

    private WebServer(IAssistantHandler handler) throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();

        Log.d("WEBSERVER",method + " '" + uri + "' ");

        String[] uriSplitted = uri.split("/");
        AssistantHandler.Result result = AssistantHandler.Result.PATH_NOT_FOUND;
        if(method == Method.GET && uriSplitted.length > 1) {
            if(handler != null) {
                result = handler.handleNewResponse(method, uriSplitted, session.getParameters());
            }
            else {
                Log.e("WEBSERVER", "No handler found!");
            }
        }

        Response response;
        try {
            response = newFixedLengthResponse(new JSONObject()
                    .put("status", result.toString())
                    .toString());
        } catch (JSONException e) {
            response = newFixedLengthResponse(e.toString());
        }

        response.setMimeType("application/vnd.api+json");
        return response;
    }
}
