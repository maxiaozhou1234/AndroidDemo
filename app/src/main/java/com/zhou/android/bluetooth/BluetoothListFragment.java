package com.zhou.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.Tools;

import java.util.HashMap;
import java.util.Set;

/**
 * Bluetooth 蓝牙列表
 * Created by ZhOu on 2017/6/15.
 */

public class BluetoothListFragment extends Fragment {

    /**
     * 开启蓝牙请求码
     */
    private final static int RequestBluetoothCode = 10001;
    /**
     * 开放搜索请求码
     */
    private final static int RequestBluetoothDiscoverable = 10002;

    private Switch sw_bluetooth, sw_discoverable;
    private LinearLayout ll_match, ll_usable;
    private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private int padding = 6;

    private BluetoothAdapter bluetoothAdapter;
    private Handler handler;

    private HashMap<String, BluetoothDevice> matchData = new HashMap<>();
    private HashMap<String, BluetoothDevice> usableData = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth_list, container, false);
        initView(view);
        init();
        addListener();
        return view;
    }

    private void initView(View view) {
        sw_bluetooth = (Switch) view.findViewById(R.id.sw_bluetooth);
        sw_discoverable = (Switch) view.findViewById(R.id.sw_discoverable);
        ll_match = (LinearLayout) view.findViewById(R.id.ll_match);
        ll_usable = (LinearLayout) view.findViewById(R.id.ll_use);

        padding = Tools.dip2px(getActivity(), padding);
    }

    private void init() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Your device do not support Bluetooth.", Toast.LENGTH_SHORT).show();
            return;
        }
        sw_bluetooth.setChecked(bluetoothAdapter.isEnabled());
        sw_discoverable.setChecked(bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
        if (!bluetoothAdapter.isEnabled())
            requestBluetoothEnable();
        else {
            loadMatch();
            bluetoothAdapter.startDiscovery();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        getActivity().registerReceiver(bluetoothReceiver, intentFilter);
    }

    private void addListener() {
        sw_bluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    requestBluetoothEnable();
                } else {
                    bluetoothAdapter.disable();
                }
            }
        });

        sw_discoverable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) requestBluetoothDiscoverable();
            }
        });
    }

    private void loadMatch() {
        Set<BluetoothDevice> set = bluetoothAdapter.getBondedDevices();
        if (set.size() == 0)
            return;
        if (ll_match.getVisibility() == View.GONE)
            ll_match.setVisibility(View.VISIBLE);
        if (matchData.size() > set.size()) {
            View title = ll_match.getChildAt(0);
            ll_match.removeAllViews();
            ll_match.addView(title);
        }
        for (BluetoothDevice device : set) {
            String key = device.getAddress();
            if (!matchData.containsKey(key)) {
                matchData.put(key, device);
            }
            View v = ll_match.findViewWithTag(key);
            if (v == null) {
                ll_match.addView(createItem(device));
            }
        }
    }

    private void refreshUsableList() {
        View title = ll_usable.getChildAt(0);
        ll_usable.removeAllViews();
        ll_usable.addView(title);
    }

    private TextView createItem(BluetoothDevice device) {
        String key = device.getAddress();
        TextView tv_item = new TextView(getActivity());
        tv_item.setLayoutParams(params);
        tv_item.setBackgroundResource(R.drawable.bg_white);
        tv_item.setPadding(padding, padding, padding, padding);
        tv_item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tv_item.setTextColor(getActivity().getResources().getColor(R.color.black));
        String value = device.getName();
        if (TextUtils.isEmpty(value))
            value = key;
        tv_item.setText(value);
        tv_item.setTag(key);
        tv_item.setOnClickListener(onClickListener);
        return tv_item;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String tag = (String) v.getTag();

            if (onBluetoothClickListener != null) {
                BluetoothDevice device = null;
                if (matchData.containsKey(tag))
                    device = matchData.get(tag);
                else if (usableData.containsKey(tag))
                    device = usableData.get(tag);
                onBluetoothClickListener.onBluetoothClick(device);
            }
        }
    };

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                if (state == BluetoothAdapter.STATE_OFF) {
                    sw_bluetooth.setChecked(false);
                    matchData.clear();
                    usableData.clear();
                    ll_match.setVisibility(View.GONE);
                    ll_usable.setVisibility(View.GONE);

                } else if (state == BluetoothAdapter.STATE_ON) {
                    sw_bluetooth.setChecked(true);
                    loadMatch();
                    if (ll_usable.getVisibility() == View.GONE)
                        ll_usable.setVisibility(View.VISIBLE);
                    refreshUsableList();
                    bluetoothAdapter.startDiscovery();
                }
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                usableData.put(device.getAddress(), device);
                ll_usable.addView(createItem(device));
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                usableData.clear();
                if (handler != null) {
                    handler.obtainMessage(MessageType.MESSAGE_REFRESH_STARTED).sendToTarget();
                }
            } else if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
                if (mode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    sw_discoverable.setChecked(true);
                } else {
                    sw_discoverable.setChecked(false);
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                if (handler != null) {
                    handler.obtainMessage(MessageType.MESSAGE_REFRESH_FINISHED).sendToTarget();
                }
            }
        }
    };

    /**
     * 蓝牙是否可用
     */
    private void requestBluetoothEnable() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, RequestBluetoothCode);
    }

    /**
     * 蓝牙开放搜索
     */
    private void requestBluetoothDiscoverable() {
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivityForResult(intent, RequestBluetoothDiscoverable);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RequestBluetoothCode == requestCode) {
            sw_bluetooth.setChecked(-1 == resultCode);
            if (-1 != resultCode) {
                Toast.makeText(getActivity(), "Open Bluetooth is failed,please open in Setting.",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (RequestBluetoothDiscoverable == requestCode) {
            sw_discoverable.setChecked(300 == resultCode);
        }
    }

    public void refresh(Handler handler) {
        this.handler = handler;
        bluetoothAdapter.startDiscovery();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(bluetoothReceiver);
    }

    private OnBluetoothClickListener onBluetoothClickListener = null;

    public void setOnBluetoothClickListener(OnBluetoothClickListener onBluetoothClickListener) {
        this.onBluetoothClickListener = onBluetoothClickListener;
    }

    public interface OnBluetoothClickListener {
        void onBluetoothClick(BluetoothDevice device);
    }
}
