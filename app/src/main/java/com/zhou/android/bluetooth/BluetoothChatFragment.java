package com.zhou.android.bluetooth;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.common.CommonAdapter;
import com.zhou.android.common.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Bluetooth 蓝牙对话界面
 * Created by ZhOu on 2017/6/15.
 */

public class BluetoothChatFragment extends Fragment {

    private ListView listView;
    private EditText editText;

    private ArrayMap<String, List<ArrayMap<String, Object>>> allChatData = new ArrayMap<>();
    private List<ArrayMap<String, Object>> chatData = new ArrayList<>();
    private CommonAdapter<ArrayMap<String, Object>> chatAdapter;
    private InputMethodManager imm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        listView = (ListView) view.findViewById(R.id.listView);
        editText = (EditText) view.findViewById(R.id.editText);
        chatAdapter = new CommonAdapter<ArrayMap<String, Object>>(getActivity(), chatData, R.layout.listformat_bluetooth_chat) {
            @Override
            protected void fillData(ViewHolder holder, int position) {
                TextView tv = holder.getView(R.id.tv_chat);
                ArrayMap<String, Object> map = chatData.get(position);
                int type = (int) map.get("type");
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tv.getLayoutParams();
                lp.gravity = type == 0 ? Gravity.START : Gravity.END;
                tv.setLayoutParams(lp);
                tv.setBackgroundResource(type == 0 ? R.drawable.ic_chat_left : R.drawable.ic_chat_right);
                tv.setText((String) map.get("text"));
            }
        };
        listView.setAdapter(chatAdapter);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String msg = v.getText().toString();
                    if (TextUtils.isEmpty(msg))
                        return true;
                    writeMessage(msg);
                    if (onSendMessageListener != null) {
                        onSendMessageListener.sendMessage(msg);
                    }
                    v.setText("");
                    return true;
                } else
                    return false;
            }
        });
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && imm != null && imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });
    }

    private String currentAddress;

    public void setDeviceAddress(String address) {
        currentAddress = address;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            List<ArrayMap<String, Object>> tmp = allChatData.get(currentAddress);
            if (tmp == null)
                tmp = new ArrayList<>();
            else
                tmp.clear();
            tmp.addAll(chatData);
            allChatData.put(currentAddress, tmp);
        } else {
            List<ArrayMap<String, Object>> tmp = allChatData.get(currentAddress);
            chatData.clear();
            if (tmp != null) {
                chatData.addAll(tmp);
            }
            chatAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 接收对方消息
     *
     * @param message
     */
    public void pushMessage(String message) {
        if (TextUtils.isEmpty(message))
            return;
        ArrayMap<String, Object> am = new ArrayMap<>();
        am.put("type", 0);
        am.put("text", message);
        chatData.add(am);
        chatAdapter.notifyDataSetChanged();
    }

    /**
     * 写发送给对方消息
     *
     * @param message
     */
    private void writeMessage(String message) {
        ArrayMap<String, Object> am = new ArrayMap<>();
        am.put("type", 1);
        am.put("text", message);
        chatData.add(am);
        chatAdapter.notifyDataSetChanged();
    }

    private OnSendMessageListener onSendMessageListener;

    public void setOnSendMessageListener(OnSendMessageListener onSendMessageListener) {
        this.onSendMessageListener = onSendMessageListener;
    }

    public interface OnSendMessageListener {
        void sendMessage(String message);
    }
}
