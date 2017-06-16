package com.zhou.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Bluetooth 蓝牙主页列表测试用
 * Created by ZhOu on 2017/6/14.
 */

public class BluetoothMainActivity extends BaseActivity {

    private final static String TAG = "zhou";
    private final static int RequestBluetoothCode = 10001;

    private BluetoothChatService bluetoothChatService;

    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> bondedDevices = new ArrayList<>();
    private Set<BluetoothDevice> foundDevices = Collections.synchronizedSet(new LinkedHashSet<BluetoothDevice>());

    private TextView tv_detail;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_bluetooth_main);
    }

    @Override
    protected void init() {
        tv_detail = (TextView) findViewById(R.id.tv_detail);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Your device do not support Bluetooth.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RequestBluetoothCode);
        } else {
            Set<BluetoothDevice> set = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : set) {
                bondedDevices.add(device);
                String text = device.getName() + " address: " + device.getAddress() + " type: " + device.getType() + "\n";
                tv_detail.append(text);
            }
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, intentFilter);

        bluetoothChatService = new BluetoothChatService(this, handler);
        bluetoothChatService.start();
    }

    @Override
    protected void addListener() {
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bondedDevices.size() == 0)
                    return;
                bluetoothChatService.connect(bondedDevices.get(0));
            }
        });
        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothChatService != null) {
                    bluetoothChatService.write("123".getBytes());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
        if (bluetoothChatService != null) {
            bluetoothChatService.initiativeStop();
            bluetoothChatService = null;
        }
    }

    private void deviceDiscoverable() {
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(intent);
        }
    }

    private void discoverDevice() {
        tv_detail.append("扫描设备：" + bluetoothAdapter.isEnabled() + "\n");
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            foundDevices.clear();
            bluetoothAdapter.startDiscovery();
        }
    }

    private void loadDiscoverDevices() {
        tv_detail.append("发现设备：\n");
        if (foundDevices.size() > 0) {
            for (BluetoothDevice device : foundDevices) {
                String text = device.getName() + " address: " + device.getAddress() + " type: " + device.getType() + "\n";
                tv_detail.append(text);
            }
        }
    }

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (BluetoothAdapter.STATE_OFF == state) {
                    Log.d(TAG, "state: " + state);
                } else if (BluetoothAdapter.STATE_ON == state) {
                    Log.d(TAG, "state: " + state);
                    discoverDevice();
                }
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                Log.d(TAG, "found");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                foundDevices.add(device);
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                loadDiscoverDevices();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestBluetoothCode) {
            if (resultCode == RESULT_OK) {
                Set<BluetoothDevice> set = bluetoothAdapter.getBondedDevices();
                bondedDevices.clear();
                for (BluetoothDevice device : set) {
                    bondedDevices.add(device);
                    String text = device.getName() + " address: " + device.getAddress() + " type: " + device.getType() + "\n";
                    tv_detail.append(text);
                }
            } else
                Toast.makeText(this, "Open Bluetooth is failed,please open in Setting.", Toast.LENGTH_SHORT).show();
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Object o = msg.obj;
            String v;
            if (o instanceof Integer)
                v = String.valueOf(o);
            else if (o instanceof String)
                v = (String) o;
            else
                v = new String((byte[]) o);
            if (msg.what == MessageType.MESSAGE_READ) {
                Toast.makeText(BluetoothMainActivity.this, v, Toast.LENGTH_SHORT).show();
                if (v.equals(MessageType.MESSAGE_DISCONNECT_DEVICE)) {
                    Log.d(TAG, "断开连接");
                }
            } else if (msg.what == MessageType.MESSAGE_WRITE) {
                Toast.makeText(BluetoothMainActivity.this, v, Toast.LENGTH_SHORT).show();
                if (v.equals(MessageType.MESSAGE_DISCONNECT_DEVICE)) {
                    Log.d(TAG, "断开连接");
                }
            }
            Log.d(TAG, msg.what + " : " + v);
        }
    };
}
