package com.zhou.android.bluetooth;

import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;

/**
 * Bluetooth 启动页
 * Created by ZhOu on 2017/6/15.
 */

public class BluetoothActivity extends BaseActivity implements BluetoothListFragment.OnBluetoothClickListener, BluetoothChatFragment.OnSendMessageListener {

    private BluetoothChatService mService;
    private BluetoothListFragment listFragment;
    private BluetoothChatFragment chatFragment;
    private Fragment currentFragment;

    private final static String LIST_FRAGMENT = "BluetoothListFragment";
    private final static String CHAT_FRAGMENT = "BluetoothChatFragment";

    private Bundle savedInstanceState;
    private View view;
    private MenuItem bluetoothStatus;
    private InputMethodManager imm;

    private NotificationManager notificationManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_bluetooth);
    }

    @Override
    protected void init() {
        view = findViewById(R.id.fragment);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mService = new BluetoothChatService(this, handler);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (savedInstanceState != null) {
            FragmentManager manager = getSupportFragmentManager();
            listFragment = (BluetoothListFragment) manager.findFragmentByTag(LIST_FRAGMENT);
            chatFragment = (BluetoothChatFragment) manager.findFragmentByTag(CHAT_FRAGMENT);
        } else {
            listFragment = new BluetoothListFragment();
            chatFragment = new BluetoothChatFragment();
            transaction.add(R.id.fragment, listFragment, LIST_FRAGMENT);
            transaction.add(R.id.fragment, chatFragment, CHAT_FRAGMENT);
        }
//        transaction.setCustomAnimations(R.anim.slide_in_right, android.R.anim.slide_out_right);
        transaction.hide(chatFragment).show(listFragment).commit();
        currentFragment = listFragment;
    }

    @Override
    protected void addListener() {
        listFragment.setOnBluetoothClickListener(this);
        chatFragment.setOnSendMessageListener(this);
    }

    private void setSubtitle(@NonNull String subtitle) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subtitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        bluetoothStatus = menu.findItem(R.id.menu_status);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mService != null && mService.getState() == BluetoothChatService.StateNone)
            mService.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.initiativeStop();
        }
    }

    private String lastDeviceAddress = "";

    @Override
    public void onBluetoothClick(BluetoothDevice device) {
        if (device != null) {
            if (!lastDeviceAddress.equals(device.getAddress()))
                mService.initiativeStop();
            mService.connect(device);
            lastDeviceAddress = device.getAddress();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            if (currentFragment.getTag().equals(LIST_FRAGMENT))
                transaction.hide(currentFragment);
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(CHAT_FRAGMENT);
            ((BluetoothChatFragment) fragment).setDeviceAddress(lastDeviceAddress);
            if (!fragment.isAdded())
                transaction.add(fragment, CHAT_FRAGMENT);
            transaction.show(fragment);
            currentFragment = fragment;
            transaction.commit();
            String subtitle = device.getName();
            if (TextUtils.isEmpty(subtitle))
                subtitle = lastDeviceAddress;
            setSubtitle(subtitle);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MessageType.MESSAGE_STATE_CHANGE) {
                int state = (int) msg.obj;
                switch (state) {
                    case BluetoothChatService.StateConnected:
                        bluetoothStatus.setTitle("连接成功");
                        break;
                    case BluetoothChatService.StateConnecting:
                        bluetoothStatus.setTitle("连接中");
                        break;
                    case BluetoothChatService.StateListen:
                        break;
                    case BluetoothChatService.StateNone:
                        bluetoothStatus.setTitle("连接断开");
                        break;
                    default:
                        back();
                }

            } else if (msg.what == MessageType.MESSAGE_WRITE) {
//                String message = new String((byte[]) msg.obj);
//                if (message.equals(MessageType.MESSAGE_DISCONNECT_DEVICE)) {
//                    mService.stop();
//                } else {
//                }
            } else if (msg.what == MessageType.MESSAGE_READ) {
                String message = (String) msg.obj;
                if (message.equals(MessageType.MESSAGE_DISCONNECT_DEVICE)) {
                    mService.stop();
                } else {
                    if (currentFragment.getTag().equals(CHAT_FRAGMENT))
                        ((BluetoothChatFragment) currentFragment).pushMessage(message);
                    else {
                        notification(mService.deviceName, message);
                        Log.d("zhou", message);
                    }
                }
            }
        }
    };

    @Override
    public void sendMessage(String message) {
        mService.write(message.getBytes());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentFragment.getTag().equals(CHAT_FRAGMENT)) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                transaction.hide(chatFragment).show(listFragment).commit();
                currentFragment = getSupportFragmentManager().findFragmentByTag(LIST_FRAGMENT);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                setSubtitle("");
                return true;
            } else
                return super.onKeyDown(keyCode, event);
        } else
            return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish() {
        if (currentFragment.getTag().equals(CHAT_FRAGMENT)) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            transaction.hide(chatFragment).show(listFragment).commit();
            currentFragment = getSupportFragmentManager().findFragmentByTag(LIST_FRAGMENT);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            setSubtitle("");
        } else
            super.finish();
    }

    int messageId = 0;

    private void notification(String name, String message) {
        if (notificationManager == null)
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (name == null)
            name = "收到一条信息";
        else
            name += "发来信息";
        Notification notification = new Notification.Builder(this)
                .setContentTitle(name)
                .setContentText(message)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setTicker(message)
                .build();
        notificationManager.notify(messageId, notification);
        messageId++;
    }
}
