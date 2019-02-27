package com.zhou.android.net;

import android.util.Log;

import com.zhou.android.common.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ClientNetwork extends BaseNetwork {

    private String defaultRole = "app";
    private String defaultAccount = "12345";
    private String defaultAppId = "1000000003";
    private volatile boolean success = false;
    private DatagramSocket searchSocket = null;
    private Socket tcpSocket = null;

    private Thread tcpThread = null;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;

    private BlockingQueue<String> queue = new ArrayBlockingQueue<>(12);
    private Thread consumer = null;

    private byte[] buf = new byte[4096];
    private StringBuilder builder = new StringBuilder();

    public ClientNetwork() {
        super();
        success = false;
        if (searchSocket == null) {
            try {
                searchSocket = new DatagramSocket();
                searchSocket.setBroadcast(true);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        getExecutor().execute(sendRunnable);
    }

    @Override
    public void send(JSONObject json) {
        if (json == null)
            return;
        if (tcpSocket != null) {
            if (consumer == null || consumer.isInterrupted()) {
                consumer = new Thread(() -> {
                    for (; ; ) {
                        try {
                            String packet = queue.take();
                            if (outputStream == null) {
                                outputStream = tcpSocket.getOutputStream();
                            }
                            Log.d("net", "write  = " + packet);
                            outputStream.write(packet.getBytes());
                            outputStream.flush();

                            readInput();

                        } catch (InterruptedException e) {
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                consumer.start();
            }
            queue.offer(json.toString());
        } else {
            Log.e("net", "TCP 连接已经断开");
        }
    }

    @Override
    void createTcpSocket(String ip, int port) {
        Log.i("net", "client create Tcp::ip = " + ip + ", port = " + port + ", default = " + DEFAULT_TCP_PORT);
        success = true;
        if (searchSocket != null) {
            searchSocket.close();
        }
        if (tcpSocket != null) {
            return;
        }
        try {
            tcpSocket = new Socket(ip, DEFAULT_TCP_PORT, null, 0);
            tcpSocket.setSoTimeout(120000);
            tcpSocket.setKeepAlive(true);
            tcpSocket.setTcpNoDelay(true);
            outputStream = tcpSocket.getOutputStream();
            inputStream = tcpSocket.getInputStream();
            JSONObject json = new JSONObject();
            try {
                json.put("message", "connect");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            send(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    int receiverUdpPort() {
        return DEFAULT_CLIENT_UDP_PORT;
    }

    @Override
    int sendUdpPort() {
        return DEFAULT_SERVER_UDP_PORT;
    }

    private Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            JSONObject json = new JSONObject();
            try {
                json.put("from_role", defaultRole);
                json.put("from_account", defaultAccount);
                json.put("command", "query");
                json.put("app_id", defaultAppId);
                json.put("msg_type", "search");
            } catch (Exception e) {
                e.printStackTrace();
            }
            String data = json.toString();
            int count = 0;

            if (searchSocket != null) {
                DatagramPacket packet;
                while (!success && count < 3) {
                    try {
                        packet = new DatagramPacket(data.getBytes(), data.length(),
                                InetAddress.getByName(defaultBroadcast), sendUdpPort());
                        searchSocket.send(packet);
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    count++;
                }
            }
        }
    };

    @Override
    protected void destroy() {
        super.destroy();
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "close");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (queue.size() > 0) {
            queue.clear();
        }
        send(json);
        if (tcpSocket != null) {
            try {
                tcpSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (tcpThread != null) {
            tcpThread.interrupt();
        }
        if (consumer != null) {
            consumer.interrupt();
        }
        Tools.closeIO(outputStream, inputStream);
    }

    private void readInput() {
        if (inputStream == null && tcpSocket != null && tcpSocket.isConnected()) {
            try {
                inputStream = tcpSocket.getInputStream();
            } catch (IOException e) {
                return;
            }
        }
        if (inputStream == null) {
            return;
        }
        try {
            int length = inputStream.read(buf);
            builder.append(new String(buf, 0, length));
            if (builder.length() > 0) {
                Log.i("net", "来自服务端接收 = " + builder.toString());
                builder.setLength(0);
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
}
