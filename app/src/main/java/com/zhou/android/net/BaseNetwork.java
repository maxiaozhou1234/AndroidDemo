package com.zhou.android.net;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseNetwork {

    //默认接收端口
    public final static int DEFAULT_SERVER_UDP_PORT = 9102;
    public final static int DEFAULT_CLIENT_UDP_PORT = 9103;
    public final static int DEFAULT_TCP_PORT = 9500;

    final static String SERVER = "server";
    final static String CLIENT = "client";

    protected String defaultBroadcast = "255.255.255.255";

    public String role;
    private DatagramSocket receiveSocket;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private String ipAddress;

    BaseNetwork() {
//        String broadcast = Tools.getBroadcast();
//        if (broadcast != null) {
//            defaultBroadcast = broadcast;
//        }
        try {
            receiveSocket = new DatagramSocket(receiverUdpPort());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            byte[] buffer = new byte[2048];
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                if (receiveSocket == null) {
                    break;
                }
                DatagramPacket receiver = new DatagramPacket(buffer, buffer.length);
                try {
                    receiveSocket.receive(receiver);
                    String result = new String(buffer, 0, receiver.getLength());
                    Log.d("net", result + " ip = " + receiver.getAddress().getHostAddress() + ", port = " + receiver.getPort());
                    if (receiver.getAddress().getHostAddress().equals(ipAddress)) {
                        return;
                    }

                    JSONObject json = new JSONObject(result);
                    if (!json.has("statusCode")) {
                        Log.i("net", "receiver = " + json.toString());
                        json.put("from_role", "server");
                        json.put("statusCode", 200);
                        byte[] msg = json.toString().getBytes();
                        DatagramPacket reply = new DatagramPacket(msg, msg.length,
                                InetAddress.getByName(defaultBroadcast), sendUdpPort());
                        DatagramSocket send = new DatagramSocket();
                        send.setBroadcast(true);
                        send.send(reply);
                        send.close();
                    }
                    if (Constant.SEARCH.equals(json.optString("cmd"))) {
                        createTcpSocket(receiver.getAddress().getHostAddress(), DEFAULT_TCP_PORT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    abstract void send(JSONObject json, @Nullable Callback callback);

    abstract void createTcpSocket(String ip, int port);

    protected void setSelfIp(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    abstract int receiverUdpPort();

    abstract int sendUdpPort();

    protected void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    protected void destroy() {
        if (receiveSocket != null) {
            receiveSocket.disconnect();
            receiveSocket.close();
            receiveSocket = null;
        }
    }
}
