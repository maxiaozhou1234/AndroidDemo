package com.zhou.android.net;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.annotation.IntDef;
import android.text.format.Formatter;

import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.InetAddress;

public class LocalNetwork {

    //一种为主机，一种为客户端，区别是主机会发送广播
    public final static int SERVER = 0;//主机
    public final static int CLIENT = 1;//接收

    private BaseNetwork baseNetwork;

    @IntDef({SERVER, CLIENT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LocalNetworkType {
    }

    private LocalNetwork(Context context, int type) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String address = Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress());
        if (SERVER == type) {
            baseNetwork = new ServerNetwork();
        } else {
            baseNetwork = new ClientNetwork();
        }
        baseNetwork.setSelfIp(address);
    }

    public void send(JSONObject json) {
        baseNetwork.send(json);
    }

    public void destroy() {
        baseNetwork.destroy();
    }

    public static class LocalNetworkFactory {
        public static LocalNetwork create(Context context, @LocalNetworkType int type) {
            return new LocalNetwork(context, type);
        }
    }
}
