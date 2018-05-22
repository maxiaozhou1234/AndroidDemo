package com.zhou.android.main;

import android.view.KeyEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Udp 监听
 * Created by ZhOu on 2018/5/22.
 */

public class UdpReceiverActivity extends BaseActivity {

    private ScrollView scrollView;
    private TextView text;

    private DatagramSocket receiverSocket;
    private Receiver thread;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_udp);
    }

    @Override
    protected void init() {
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        text = (TextView) findViewById(R.id.text);

        //check network permission ...
        try {
            receiverSocket = new DatagramSocket(2000);
        } catch (SocketException e) {
            e.printStackTrace();
            text.setText("Socket Error...");
        }
        thread = new Receiver();
        thread.start();
    }

    @Override
    protected void addListener() {

    }

    @Override
    public void back() {
        if (thread != null) {
            if (thread.isAlive()) thread.interrupt();
            thread = null;
        }
        if (receiverSocket != null && !receiverSocket.isClosed()) {
            receiverSocket.close();
            receiverSocket.disconnect();
        }
        super.back();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    private byte[] buf = new byte[1024];

    private class Receiver extends Thread {
        @Override
        public void run() {
            if (receiverSocket != null) {
                while (true) {
                    if (interrupted()) break;
                    try {
                        DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);
                        receiverSocket.receive(packet);
                        final String data = new String(packet.getData(), 0, packet.getLength());
                        if (data.length() > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    text.append("\r\n");
                                    text.append(data);
                                    scrollView.fullScroll(View.FOCUS_DOWN);

                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
    }
}
