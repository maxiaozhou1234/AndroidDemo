package com.zhou.android.main;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.Tools;
import com.zhou.android.net.Callback;
import com.zhou.android.net.Constant;
import com.zhou.android.net.LocalNetwork;
import com.zhou.android.net.ServerHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 局域网通信测试，两台机子一个用做服务端，一个用作客户端
 */
public class LocalNetActivity extends BaseActivity implements Callback, ServerHandler {

    private RadioGroup radioGroup;
    private Button start;
    private EditText edit;
    private LinearLayout clientPad;
    private ScrollView scrollView;
    private TextView text;
    private LocalNetwork localNetwork;

    private JSONObject json;
    private int count = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                String data = (String) msg.obj;
                text.append(data);
                text.append("\n");
                sendEmptyMessageDelayed(1, 400);
            } else if (msg.what == 1) {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }
    };

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_local_net);
    }

    @Override
    protected void init() {

        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((gp, id) ->
                clientPad.setVisibility(id == R.id.server ? View.GONE : View.VISIBLE));
        start = findViewById(R.id.start);
        clientPad = findViewById(R.id.clientPad);
        start.setOnClickListener(v -> {
            start.setEnabled(false);
            if (radioGroup.getCheckedRadioButtonId() == R.id.server) {
                localNetwork = LocalNetwork.createServer(LocalNetActivity.this, this);
            } else {
                localNetwork = LocalNetwork.createClient(LocalNetActivity.this, this);
            }
        });
        findViewById(R.id.send).setOnClickListener(v -> {
            if (localNetwork != null) {

                try {
                    json.put("clientCount", count++);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                localNetwork.send(json, this);
            }
        });
        findViewById(R.id.destroy).setOnClickListener(v -> {
            if (localNetwork != null) {
                localNetwork.destroy();
                localNetwork = null;
            }
            start.setEnabled(true);
        });
        edit = findViewById(R.id.edit);
        findViewById(R.id.tcp).setOnClickListener(v -> {
            String data = edit.getText().toString();
            if (!TextUtils.isEmpty(data)) {
                if (localNetwork != null) {
                    try {
                        json.put("content", data);
                    } catch (JSONException e) {
                        //
                    }
                    localNetwork.send(json, this);
                }
                edit.clearFocus();
            }
        });

        findViewById(R.id.close).setOnClickListener(v -> findViewById(R.id.rl_tip).setVisibility(View.GONE));

        scrollView = findViewById(R.id.scrollView);
        text = findViewById(R.id.text);

        json = new JSONObject();
        try {
            json.put("fromRole", "client");
            json.put("message", "nothing");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("net", "broadcast = " + Tools.getBroadcast());
    }

    @Override
    protected void addListener() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localNetwork != null) {
            localNetwork.destroy();
        }
    }

    //用于处理服务端的回复
    @Override
    public boolean handleMessage(JSONObject json) {
        Message msg = handler.obtainMessage();
        msg.obj = json.toString();
        msg.what = 0;
        msg.sendToTarget();

        String cmd = json.optString("cmd");
        switch (cmd) {
            case Constant.CONNECT: {//连接后，服务端回复
                String content = json.optString("content");
                Log.d("net", "content = " + content);
                break;
            }
            default:
                break;
        }
        return true;
    }

    //用于处理客户端的请求
    @Override
    public JSONObject handleRequest(JSONObject json) throws JSONException {
        Message msg = handler.obtainMessage();
        msg.obj = json.toString();
        msg.what = 0;
        msg.sendToTarget();

        String cmd = json.optString("cmd");
        switch (cmd) {
            case Constant.CONNECT: {
                JSONArray content = new JSONArray();
                JSONObject jo = new JSONObject();
                jo.put("server_ip", "192.168.100.100");
                jo.put("server_port", "5001");
                jo.put("device_type", "gateway");
                content.put(jo);
                json.put("content", content);
                break;
            }
            default:
                break;
        }
        return json;
    }
}
