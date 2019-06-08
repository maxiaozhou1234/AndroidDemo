package com.zhou.android.net;

import android.support.annotation.Nullable;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ClientNetwork extends BaseNetwork {

    //    private String defaultAccount = "12345";
//    private String defaultAppId = "1000000003";
    private volatile boolean success;
    private DatagramSocket searchSocket = null;
    private Socket tcpSocket = null;
    private int packageId = 0;

    private Thread tcpThread = null;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    private Callback callback = null;

    private BlockingQueue<Packet> queue = new ArrayBlockingQueue<>(12);
    private Thread consumer = null;

    private byte[] buf = new byte[4096];
    private StringBuilder builder = new StringBuilder();

    public ClientNetwork(Callback callback) {
        super();
        this.callback = callback;
        role = CLIENT;
        success = false;
        if (searchSocket == null) {
            try {
                searchSocket = new DatagramSocket();
                searchSocket.setBroadcast(true);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        execute(sendRunnable);
    }

    @Override
    public void send(JSONObject json, @Nullable final Callback callback) {
        if (json == null)
            return;
        if (tcpSocket != null) {
            if (consumer == null || consumer.isInterrupted()) {
                consumer = new Thread(() -> {
                    for (; ; ) {
                        try {
                            Packet packet = queue.take();
                            if (outputStream == null) {
                                outputStream = tcpSocket.getOutputStream();
                            }
                            Log.d("net", "write  = " + packet.getContent());
                            outputStream.write(packet.getBytes());
                            outputStream.flush();

                            readInput(packet);

                        } catch (InterruptedException e) {
                            break;
                        } catch (NullPointerException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                consumer.start();
            }
            queue.offer(new Packet(packageId++, json, callback));
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
            tcpSocket = new Socket(ip, port, null, 0);
            tcpSocket.setSoTimeout(120000);
            tcpSocket.setKeepAlive(true);
            tcpSocket.setTcpNoDelay(true);
            outputStream = tcpSocket.getOutputStream();
            inputStream = tcpSocket.getInputStream();
            JSONObject json = new JSONObject();
            try {
                json.put("cmd", Constant.CONNECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            send(json, null);
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
                json.put("from_role", role);
                json.put("cmd", Constant.SEARCH);
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
        send(json, callback -> {
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
            return true;
        });
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

    private void readInput(Packet packet) {
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
            if (length > 0) {
                builder.append(new String(buf, 0, length));
                if (builder.length() > 0) {
                    String data = builder.toString();
                    Log.i("net", "来自服务端接收 = " + data);
                    JSONObject json = new JSONObject(data);
                    if (packet.callback == null || !packet.callback.handleMessage(json)) {
                        if (callback != null) {
                            callback.handleMessage(json);
                        }
                    }
                    builder.setLength(0);
                }
            }
        } catch (Exception e) {
            try {
                packet.json.put("code", ResponseCode.EXCEPTION);
                packet.json.put("reason", e.getMessage());
                JSONObject request = packet.getRequest();
                if (packet.callback == null || !packet.callback.handleMessage(request)) {
                    if (callback != null) {
                        callback.handleMessage(request);
                    }
                }
            } catch (JSONException e1) {
                //
            }
        }
    }
}
