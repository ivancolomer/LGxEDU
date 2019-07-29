package com.lglab.ivan.lgxeducontroller.utils;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public interface IAssistantHandler {
    AssistantHandler.Result handleNewResponse(NanoHTTPD.Method method, String[] uri, Map<String, List<String>> params);
    void onServerCreated(String ip, String port);
}
