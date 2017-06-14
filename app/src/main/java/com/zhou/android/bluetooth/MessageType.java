package com.zhou.android.bluetooth;

/**
 * Created by ZhOu on 2017/6/14.
 */

public interface MessageType {
    int MESSAGE_STATE_CHANGE = 20000;
    int MESSAGE_READ = 20001;
    int MESSAGE_WRITE = 20002;
    int MESSAGE_DEVICE = 20003;
    int MESSAGE_FAILED = 20004;
    int MESSAGE_LOST = 20005;

    String MESSAGE_DEVICE_NAME = "deviceName";

}
