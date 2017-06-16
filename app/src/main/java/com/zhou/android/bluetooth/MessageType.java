package com.zhou.android.bluetooth;

/**
 * Bluetooth 通信数据类型
 * Created by ZhOu on 2017/6/14.
 */

public interface MessageType {
    int MESSAGE_STATE_CHANGE = 0;
    int MESSAGE_READ = 1;
    int MESSAGE_WRITE = 2;
    int MESSAGE_DEVICE = 3;
    int MESSAGE_FAILED = 4;
    int MESSAGE_LOST = 5;
    int MESSAGE_REFRESH_STARTED = 6;
    int MESSAGE_REFRESH_FINISHED = 7;
    String MESSAGE_DEVICE_NAME = "deviceName";
    String MESSAGE_DISCONNECT_DEVICE = "disconnectDevice";

}
