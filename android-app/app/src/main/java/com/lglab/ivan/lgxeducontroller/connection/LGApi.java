package com.lglab.ivan.lgxeducontroller.connection;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class LGApi {

    public static void sendJsonRequest(Context context, int requestMethod, String url, Response.Listener<JSONArray> callback, Map<String, String> params) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonArrayRequest request = new JsonArrayRequest(requestMethod, url, null, callback, error -> Log.d("LGAPI", error.toString())) {
            /*@Override
            protected Map<String, String> getParams()
            {
                return params;
            }*/

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<>();
                //params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("Content-Type","application/json");
                return params;
            }
        };
        requestQueue.add(request);
    }
}
