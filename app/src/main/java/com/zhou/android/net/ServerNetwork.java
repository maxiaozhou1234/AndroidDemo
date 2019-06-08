package com.zhou.android.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    private ServerHandler handler;

    private byte[] defaultReply = "{\"code\":200,\"role\":\"server\"}".getBytes();
    private volatile boolean keepAlive = true;
    private List<Socket> socketList = new ArrayList<>(16);

    public ServerNetwork(@NonNull ServerHandler handler) {
        super();
        this.handler = handler;
        role = SERVER;
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
                    execute(new ServerSocketThread(socket));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        //
                    }
                }
            }
        });
        execute(tcpThread);
    }

    @Override
    void send(final JSONObject json, @Nullable Callback callback) {
        // 客户端没有一直监听，所以发送也收不到
//        for (Socket socket : socketList) {
//            if (socket.isConnected() && !socket.isOutputShutdown()) {
//                try {
//                    OutputStream out = socket.getOutputStream();
//                    out.write(json.toString().getBytes());
//                    out.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    @Override
    void createTcpSocket(String ip, int port) {
        //服务端不需要处理
        Log.d("net", "server TCP has started.");
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
        socketList.clear();
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

                        JSONObject json;
                        JSONObject reply = null;
                        try {
                            json = new JSONObject(data);
                            reply = handler.handleRequest(json);
                            if (reply != null) {
                                reply.put("fromRole", role);
                                reply.put("code", ResponseCode.SUCCESS);
                            }
                        } catch (JSONException e) {
                            json = new JSONObject();
                        }
                        if (Constant.CLOSE.equals(json.optString("cmd"))) {
                            socket.shutdownInput();
                            break;
                        }
                        if (reply != null) {
                            outputStream.write(reply.toString().getBytes());
                            outputStream.flush();
                        } else {
                            outputStream.write(defaultReply);
                            outputStream.flush();
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
