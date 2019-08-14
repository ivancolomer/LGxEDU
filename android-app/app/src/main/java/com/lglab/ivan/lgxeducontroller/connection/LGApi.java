package com.lglab.ivan.lgxeducontroller.connection;

import android.content.Context;
import android.util.Log;

import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.lglab.ivan.lgxeducontroller.legacy.beans.POI;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.CharsetUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.lglab.ivan.lgxeducontroller.utils.StringHelper.convertToUTF8;

public class LGApi {

    private static final String DEFAULT_CACHE_DIR = "volley";
    private static RequestQueue requestQueue = null;

    private static RequestQueue getRequestQueue(Context context) {
        if(requestQueue == null)
            requestQueue = newRequestQueue(context);
        return requestQueue;
    }

    public static void sendJsonRequest(Context context, int requestMethod, String url, Response.Listener<String> callback, Map<String, String> params) {
        MultipartRequest request = new MultipartRequest(requestMethod, url, callback, error -> {
            Log.d("LGAPI", error.toString());
            if(error.networkResponse != null)
                Log.d("LGAPI", "ERROR: " + new String(error.networkResponse.data));
            callback.onResponse("");

        }, params);
        getRequestQueue(context).add(request);
    }


    public static String SERVER_IP = "192.168.86.145";
    public static int PORT = 8112;

    public static void sendBalloonToPoi(Context context, POI poi, String information) {
        //LGApi.sendJsonRequest(getApplicationContext(), Request.Method.DELETE, "http://" + SERVER_IP + ":" + PORT + "/kml/builder/deleteTag/Placemark/12345", (response) -> Log.d("LGAPI", response), null);

        //clean before sending
        cleanBalloon(context);

        LGApi.sendJsonRequest(context, Request.Method.POST, "http://" + SERVER_IP + ":" + PORT + "/kml/builder/addplacemark", (response) -> Log.d("LGAPI", "ADDED PLACEMARK SUCCESS: " + response), new HashMap<String, String>() {{
            put("id", "12345");
            put("name", "");
            put("longitude", String.valueOf(poi.getLongitude()));
            put("latitude", String.valueOf(poi.getLatitude()));
            put("range", "0");
            put("description", "<![CDATA[\n" +
                    "  <head>\n" +
                    "    <!-- Required meta tags -->\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n" +
                    "\n" +
                    "    <!-- Bootstrap CSS -->\n" +
                    "    <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">\n" +
                    "\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "    <div class=\"p-lg-5\" align=\"center\">\n" +
                    "\n" +
                    "        <h1>" + convertToUTF8(poi.getName()) + "</h1>\n" +
                    "        <hr>\n" +
                    "        <h3>" + information + "</h3>\n" +
                    "        <br>\n" +
                    "\n" +
                    "    </div>\n" +
                    "\n" +
                    "    <script src=\"https://code.jquery.com/jquery-3.2.1.slim.min.js\" integrity=\"sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN\" crossorigin=\"anonymous\"></script>\n" +
                    "    <script src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js\" integrity=\"sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q\" crossorigin=\"anonymous\"></script>\n" +
                    "    <script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js\" integrity=\"sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl\" crossorigin=\"anonymous\"></script>\n" +
                    "  </body>\n" +
                    "]]>");
            Log.d("DEBUG", get("description"));
            put("icon", "afasfas");
        }});
        LGApi.sendJsonRequest(context, Request.Method.GET, "http://" + SERVER_IP + ":" + PORT + "/kml/manage/balloon/12345/1", (response) -> Log.d("LGAPI", response), null);
    }

    public static void cleanBalloon(Context context) {
        LGApi.sendJsonRequest(context, Request.Method.GET, "http://" + SERVER_IP + ":" + PORT + "/kml/manage/clean", (response) -> Log.d("LGAPI", "CLEAN SUCCESS: " + response), null);
        LGApi.sendJsonRequest(context, Request.Method.POST, "http://" + SERVER_IP + ":" + PORT + "/kml/manage/new?name=IvanKML", (response) -> Log.d("LGAPI", response), null);
    }




    private static RequestQueue newRequestQueue(Context context) {
        return newRequestQueue(context, (BaseHttpStack) null);
    }

    private static RequestQueue newRequestQueue(Context context, BaseHttpStack stack) {
        BasicNetwork network;
        if (stack == null) {
            network = new BasicNetwork(new HurlStack());
        } else {
            network = new BasicNetwork(stack);
        }

        return newRequestQueue(context, network);
    }

    private static RequestQueue newRequestQueue(Context context, Network network) {
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network, 1);
        queue.start();
        return queue;
    }

    private static class MultipartRequest extends Request<String> {

        MultipartEntityBuilder entity = MultipartEntityBuilder.create();
        HttpEntity httpentity;

        private final Response.Listener<String> mListener;
        private final Map<String, String> mStringPart;

        public MultipartRequest(int method, String url, Response.Listener<String> listener,
                                Response.ErrorListener errorListener, Map<String, String> mStringPart) {
            super(method, url, errorListener);
            this.mListener = listener;
            this.mStringPart = mStringPart;
            entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            try {
                entity.setCharset(CharsetUtils.get("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            buildMultipartEntity();
            httpentity = entity.build();
        }

        private void buildMultipartEntity() {
            if (mStringPart != null) {
                for (Map.Entry<String, String> entry : mStringPart.entrySet()) {
                    entity.addTextBody(entry.getKey(), entry.getValue());
                }
            }
        }

        @Override
        public String getBodyContentType() {
            return httpentity.getContentType().getValue();
        }

        @Override
        public byte[] getBody() {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                httpentity.writeTo(new CountingOutputStream(bos, httpentity.getContentLength(), null));
            } catch (IOException e) {
                VolleyLog.e("IOException writing to ByteArrayOutputStream");
            }
            return bos.toByteArray();
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {

            try {
//          System.out.println("Network Response "+ new String(response.data, "UTF-8"));
                return Response.success(new String(response.data, "UTF-8"),
                        getCacheEntry());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                // fuck it, it should never happen though
                return Response.success(new String(response.data), getCacheEntry());
            }
        }

        @Override
        protected void deliverResponse(String response) {
            mListener.onResponse(response);
        }

        public interface MultipartProgressListener {
            void transferred(long transfered, int progress);
        }

        public static class CountingOutputStream extends FilterOutputStream {
            private final MultipartProgressListener progListener;
            private long transferred;
            private long fileLength;

            public CountingOutputStream(final OutputStream out, long fileLength,
                                        final MultipartProgressListener listener) {
                super(out);
                this.fileLength = fileLength;
                this.progListener = listener;
                this.transferred = 0;
            }

            public void write(byte[] b, int off, int len) throws IOException {
                out.write(b, off, len);
                if (progListener != null) {
                    this.transferred += len;
                    int prog = (int) (transferred * 100 / fileLength);
                    this.progListener.transferred(this.transferred, prog);
                }
            }

            public void write(int b) throws IOException {
                out.write(b);
                if (progListener != null) {
                    this.transferred++;
                    int prog = (int) (transferred * 100 / fileLength);
                    this.progListener.transferred(this.transferred, prog);
                }
            }

        }
    }
}
