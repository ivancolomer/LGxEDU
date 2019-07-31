package com.lglab.ivan.lgxeducontroller.connection;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LGApi {

    private static RequestQueue requestQueue = null;

    private static RequestQueue getRequestQueue(Context context) {
        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(context);
        return requestQueue;
    }

    public static void sendJsonRequest(Context context, int requestMethod, String url, Response.Listener<JSONObject> callback, Map<String, String> params) {
        RequestQueue requestQueue = getRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(requestMethod, url, null, callback, error -> {
            Log.d("LGAPI", error.toString());
            callback.onResponse(new JSONObject());
        }) {

            @Override
            protected Map<String, String> getParams()
            {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<>();
                //params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("Content-Type","application/form-data");
                return params;
            }
        };
        requestQueue.add(request);
    }
}
