package com.zhou.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Bluetooth 蓝牙通信服务
 * Created by ZhOu on 2017/6/14.
 */

public class BluetoothChatService {

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public final static int StateNone = 0;
    public final static int StateListen = 1;
    public final static int StateConnecting = 2;
    public final static int StateConnected = 3;

    private final BluetoothAdapter bluetoothAdapter;
    private Handler handler;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private int state = StateNone;

    public BluetoothChatService(Context context, Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.handler = handler;
    }

    class AcceptThread extends Thread {

        private BluetoothServerSocket bluetoothServerSocket;

        public AcceptThread() {

            try {
                bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothChat", uuid);
            } catch (IOException e) {
//                e.printStackTrace();
                bluetoothServerSocket = null;
            }
        }

        @Override
        public void run() {
            BluetoothSocket bluetoothSocket;
            while (state != StateConnected) {
                try {
                    bluetoothSocket = bluetoothServerSocket.accept();
                } catch (IOException e) {
//                    e.printStackTrace();
                    break;
                }
                if (bluetoothSocket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (state) {
                            case StateNone:
                            case StateListen:
                            case StateConnecting:
                                connected(bluetoothSocket, bluetoothSocket.getRemoteDevice());
                                break;
                            case StateConnected:
                                try {
                                    bluetoothSocket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ConnectThread extends Thread {

        private BluetoothSocket bluetoothSocket;
        private BluetoothDevice bluetoothDevice;

        public ConnectThread(BluetoothDevice device) {
            bluetoothDevice = device;
            BluetoothSocket tmp;
            try {
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
//                e.printStackTrace();
                tmp = null;
            }
            bluetoothSocket = tmp;
        }

        @Override
        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                bluetoothSocket.connect();
            } catch (IOException e) {
//                e.printStackTrace();
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    //连接失败
                    connectionFailed();
                    return;
                }
            }
            synchronized (BluetoothChatService.this) {
                connectThread = null;
            }
            //连接
            connected(bluetoothSocket, bluetoothDevice);
        }


        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
    }

    class ConnectedThread extends Thread {
        private BluetoothSocket bluetoothSocket;
        //        private BluetoothDevice bluetoothDevice;
        private InputStream inputStream;
        private OutputStream outputStream;

        public ConnectedThread(BluetoothSocket bluetoothSocket) {
            this.bluetoothSocket = bluetoothSocket;
            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int len;
            while (true) {
                if (inputStream == null)
                    break;
                try {
                    len = inputStream.read(buffer);
                    if (len != -1) {
                        String v = new String(buffer, 0, len);
                        handler.obtainMessage(MessageType.MESSAGE_READ, v).sendToTarget();
                    }
                } catch (IOException e) {
//                    e.printStackTrace();
                    connectionLost();
                    BluetoothChatService.this.start();
                    break;
                } catch (NullPointerException e) {
//                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            if (buffer == null) {
                return;
            }
            try {
                outputStream.write(buffer);
                handler.obtainMessage(MessageType.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }

        public void disconnect() {
            try {
                outputStream.write(MessageType.MESSAGE_DISCONNECT_DEVICE.getBytes());
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    inputStream.close();
                }
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(StateListen);
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public synchronized void connect(BluetoothDevice bluetoothDevice) {

        if (state == StateConnecting) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        connectThread = new ConnectThread(bluetoothDevice);
        connectThread.start();

        setState(StateConnecting);
    }

    public synchronized void connected(BluetoothSocket bSocket, BluetoothDevice bDevice) {
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        connectedThread = new ConnectedThread(bSocket);
        connectedThread.start();

        handler.obtainMessage(MessageType.MESSAGE_DEVICE, bDevice.getName()).sendToTarget();

        setState(StateConnected);
    }

    private void connectionFailed() {
        Message msg = handler.obtainMessage(MessageType.MESSAGE_FAILED);
        msg.obj = "Unable to connect device.";
        handler.sendMessage(msg);
        BluetoothChatService.this.start();
    }

    private void connectionLost() {
        Message msg = handler.obtainMessage(MessageType.MESSAGE_LOST);
        msg.obj = "Device connection was lost.";
        handler.sendMessage(msg);
        BluetoothChatService.this.start();
    }

    public synchronized void stop() {

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        setState(StateNone);
    }

    /**
     * 主动关闭，发送关闭消息通知对方
     */
    public void initiativeStop() {
        disconnect();
        stop();
    }

    public void write(byte[] buffer) {
        ConnectedThread t;
        synchronized (BluetoothChatService.this) {
            if (state != StateConnected) return;
            t = connectedThread;
        }
        if (t != null)
            t.write(buffer);
    }

    private void disconnect() {
        ConnectedThread t;
        synchronized (BluetoothChatService.this) {
            if (state != StateConnected) return;
            t = connectedThread;
        }
        if (t != null)
            t.disconnect();
    }

    private void setState(int state) {
        this.state = state;
        handler.obtainMessage(MessageType.MESSAGE_STATE_CHANGE, state).sendToTarget();
    }

    public synchronized int getState() {
        return state;
    }
}
