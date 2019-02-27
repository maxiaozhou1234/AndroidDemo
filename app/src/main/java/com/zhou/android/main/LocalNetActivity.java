package com.zhou.android.main;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.Tools;
import com.zhou.android.net.LocalNetwork;

import org.json.JSONException;
import org.json.JSONObject;

public class LocalNetActivity extends BaseActivity {

    private RadioGroup radioGroup;
    private Button start;
    private EditText edit;
    private LinearLayout clientPad;
    private LocalNetwork localNetwork;

    private JSONObject json;
    private int count = 0;

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
                localNetwork = LocalNetwork.LocalNetworkFactory.create(LocalNetActivity.this, LocalNetwork.SERVER);
            } else {
                localNetwork = LocalNetwork.LocalNetworkFactory.create(LocalNetActivity.this, LocalNetwork.CLIENT);
            }
        });
        findViewById(R.id.send).setOnClickListener(v -> {
            try {
                json.put("clientCount", count++);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            localNetwork.send(json);
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
                    localNetwork.send(json);
                }
                edit.clearFocus();
            }
        });

        json = new JSONObject();
        try {
            json.put("from", "client");
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
}
