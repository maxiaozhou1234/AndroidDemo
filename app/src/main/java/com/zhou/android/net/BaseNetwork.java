package com.zhou.android.net;

import android.util.Log;

import com.zhou.android.common.Tools;

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
    public final static int DEFAULT_TCP_PORT = 9300;

    protected String defaultBroadcast = "255.255.255.255";

    private DatagramSocket receiveSocket;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private String ipAddress;

    public BaseNetwork() {
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
                    if ("search".equals(json.optString("msg_type"))) {
                        createTcpSocket(receiver.getAddress().getHostAddress(), receiver.getPort());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected ExecutorService getExecutor() {
        return executor;
    }

    abstract void send(JSONObject json);

    abstract void createTcpSocket(String ip, int port);

    protected void setSelfIp(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    abstract int receiverUdpPort();

    abstract int sendUdpPort();

    protected void destroy() {
        if (receiveSocket != null) {
            receiveSocket.disconnect();
            receiveSocket.close();
            receiveSocket = null;
        }
    }
}
