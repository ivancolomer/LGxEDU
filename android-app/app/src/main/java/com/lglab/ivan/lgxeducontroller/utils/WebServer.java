package com.lglab.ivan.lgxeducontroller.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.lglab.ivan.lgxeducontroller.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    private static WebServer server;

    static boolean createServer(Context context, IAssistantHandler handler) {
        try {
            server = new WebServer(context);
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

    static void removeHandler(IAssistantHandler oldHandler) {
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
    private Context context;

    private WebServer(Context context) throws IOException {
        super(8080);
        this.context = context;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();

        Log.d("WEBSERVER",method + " '" + uri + "' ");

        String[] uriSplitted = uri.split("/");
        AssistantHandler.Result result = AssistantHandler.Result.PATH_NOT_FOUND;

        if(method == Method.GET && uri.equals("/logo.jpg")) {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.logos_vertical)).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
            InputStream inputStream = new ByteArrayInputStream(stream.toByteArray());

            //res.addHeader("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
            return newChunkedResponse(Response.Status.OK, "image/jpeg", inputStream);
        }

        if(method == Method.GET && uri.equals("/logo.kml")) {
            String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
                    "  <Document>\n" +
                    "        <Folder>\n" +
                    "<ScreenOverlay>\n" +
                    "  <name>LOGOS</name>\n" +
                    "  <Icon>\n" +
                    "    <href>\n" +
                    "      http://" + Utils.getIPAddress(true) + ":" + server.getListeningPort() + "/logo.png\n" +
                    "    </href>\n" +
                    "  </Icon>\n" +
                    "  <overlayXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
                    "  <screenXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
                    "  <rotationXY x=\"0.5\" y=\"0.5\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
                    "  <size x=\"250\" y=\"450\" xunits=\"pixels\" yunits=\"pixels\"/>\n" +
                    "</ScreenOverlay>\n" +
                    "        </Folder>\n" +
                    "  </Document>\n" +
                    "</kml>";
            return newChunkedResponse(Response.Status.OK, "application/vnd.google-earth.kml+xml", new ByteArrayInputStream(kml.getBytes()));
        }

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
