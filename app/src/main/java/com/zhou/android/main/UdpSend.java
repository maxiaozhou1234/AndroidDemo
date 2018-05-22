package com.zhou.android.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * udp 发送端
 * Created by ZhOu on 2018/5/22.
 */

public class UdpSend {

//    private static DatagramSocket sendSocket;

    private static int count = 0;

    public static void main(String[] args) {
        new Send().start();
    }

    /**
     * 广播域 {
     *
     * @link https://blog.csdn.net/qq_26075861/article/details/78361363
     * <p>
     * 255.255.255.255是受限广播地址
     */
    private static class Send extends Thread {
        @Override
        public void run() {
            do {
                try {
                    DatagramSocket sendSocket = new DatagramSocket();
                    String msg = "Message from udp:" + String.valueOf((char) ('A' + count));
                    byte[] buf = msg.getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, 0, buf.length, InetAddress.getByName("192.168.0.255"), 2000);
                    sendSocket.setBroadcast(true);
                    sendSocket.send(packet);
                    sendSocket.close();
                    System.out.println(msg);
                    count++;
                    if (count > 30) {
                        count = -1;
                    }
                    Thread.sleep(2000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("Error exit");
                    break;
                }
            } while (true);

            System.exit(0);

        }
    }
}
