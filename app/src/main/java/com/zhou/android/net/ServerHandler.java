package com.zhou.android.net;

import org.json.JSONException;
import org.json.JSONObject;

public interface ServerHandler {
    JSONObject handleRequest(JSONObject json) throws JSONException;
}
