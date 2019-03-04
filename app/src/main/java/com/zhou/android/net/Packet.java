package com.zhou.android.net;

import org.json.JSONException;
import org.json.JSONObject;

public class Packet {
    public JSONObject json;
    public Callback callback;

    public Packet(int packageId, JSONObject json, final Callback callback) {
        this.json = json;
        this.callback = callback;
        try {
            this.json.put("packageId", packageId);
        } catch (JSONException e) {
            //
        }
    }

    public byte[] getBytes() {
        return json.toString().getBytes();
    }

    public String getContent() {
        return json.toString();
    }

    public JSONObject getRequest() {

        try {
            return new JSONObject(json.toString());
        } catch (JSONException e) {
            //
        }
        return new JSONObject();
    }
}
