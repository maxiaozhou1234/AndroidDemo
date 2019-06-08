package com.zhou.android.net;

import org.json.JSONObject;

public interface Callback {
    boolean handleMessage(JSONObject json);
}
