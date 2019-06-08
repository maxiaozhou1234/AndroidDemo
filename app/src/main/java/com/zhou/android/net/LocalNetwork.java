package com.zhou.android.net;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.text.format.Formatter;

import org.json.JSONObject;

public class LocalNetwork {

    private static LocalNetwork instance = null;
    private BaseNetwork baseNetwork;

    private LocalNetwork(BaseNetwork network) {
        instance = this;
        this.baseNetwork = network;
    }

    public static LocalNetwork getInstance() {
        if (instance == null) {
            throw new NullPointerException("LocalNetwork is null,you must call createServer or createClient before you use this.");
        }
        return instance;
    }

    /**
     * 服务端：发给所有建立连接的客户端
     * <br/>
     * 客户端：发给唯一服务端
     */
    public void send(JSONObject json, @Nullable Callback callback) {
        baseNetwork.send(json, callback);
    }

    /**
     * 销毁
     */
    public void destroy() {
        baseNetwork.destroy();
    }

    /**
     * 创建服务端
     *
     * @param context 上下文
     * @param handler 处理来自客户端的请求
     * @return LocalNetwork
     */
    public static LocalNetwork createServer(Context context, ServerHandler handler) {
        ServerNetwork network = new ServerNetwork(handler);
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String address = Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress());
        network.setSelfIp(address);
        return new LocalNetwork(network);
    }

    /**
     * 创建客户端
     *
     * @param context  上下文
     * @param callback 用于处理服务端的回复
     * @return LocalNetwork
     */
    public static LocalNetwork createClient(Context context, Callback callback) {
        ClientNetwork network = new ClientNetwork(callback);
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String address = Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress());
        network.setSelfIp(address);
        return new LocalNetwork(network);
    }
}
