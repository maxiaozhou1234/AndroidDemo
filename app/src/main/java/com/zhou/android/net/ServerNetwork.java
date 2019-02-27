package com.zhou.android.net;

import android.util.Log;

import com.zhou.android.common.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerNetwork extends BaseNetwork {

    private Thread tcpThread = null;
    private ServerSocket serverSocket = null;

    private byte[] reply = "{\"statusCode\":200,\"role\":\"server\"}".getBytes();
    private volatile boolean keepAlive = true;
    private List<Socket> socketList = new ArrayList<>(16);

    public ServerNetwork() {
        super();
        tcpThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(DEFAULT_TCP_PORT);
                while (keepAlive) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    Log.d("net", "wait connect...");
                    Socket socket = serverSocket.accept();
                    socketList.add(socket);
                    getExecutor().execute(new ServerSocketThread(socket));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Log.d("net", "alive = " + keepAlive);
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        //
                    }

                }
            }
        });
        getExecutor().execute(tcpThread);
    }

    @Override
    void send(JSONObject json) {

    }

    @Override
    void createTcpSocket(String ip, int port) {
        //服务端不需要处理
        Log.d("net", "server create Tcp ??");
    }

    @Override
    int receiverUdpPort() {
        return DEFAULT_SERVER_UDP_PORT;
    }

    @Override
    int sendUdpPort() {
        return DEFAULT_CLIENT_UDP_PORT;
    }

    @Override
    protected void destroy() {
        super.destroy();
        keepAlive = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (tcpThread != null && tcpThread.isAlive()) {
            tcpThread.interrupt();
        }
        for (Socket socket : socketList) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ServerSocketThread extends Thread {
        private Socket socket;

        ServerSocketThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            OutputStream outputStream = null;
            InputStream inputStream = null;
            try {
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                byte[] buf = new byte[4096];
                while (socket.isConnected() && !socket.isInputShutdown()) {
                    int length = inputStream.read(buf);
                    if (length > 0) {
                        String data = new String(buf, 0, length);
                        Log.d("net", "来自客户端接收 = " + data);

                        outputStream.write(reply);
                        outputStream.flush();

                        try {
                            JSONObject json = new JSONObject(data);
                            if ("close".equals(json.optString("cmd"))) {
                                socket.shutdownInput();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Tools.closeIO(outputStream, inputStream);
            }
        }
    }
}
