package com.zhou.android.bluetooth;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.CommonAdapter;
import com.zhou.android.common.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙聊天
 * <p>
 * Created by ZhOu on 2017/5/24.
 */

public class BluetoothChatActivity extends BaseActivity {

    private ListView listView;
    private CommonAdapter<ArrayMap<String, Object>> adapter;
    private List<ArrayMap<String, Object>> data = new ArrayList<>();
    private EditText editText;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_bluetooth);
    }

    @Override
    protected void init() {
        listView = (ListView) findViewById(R.id.listView);
        editText = (EditText) findViewById(R.id.editText);
        adapter = new CommonAdapter<ArrayMap<String, Object>>(this, data, R.layout.listformat_bluetooth_chat) {
            @Override
            protected void fillData(ViewHolder holder, int position) {
                TextView tv = holder.getView(R.id.tv_chat);
                ArrayMap<String, Object> map = data.get(position);
                int type = (int) map.get("type");
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tv.getLayoutParams();
                lp.gravity = type == 0 ? Gravity.START : Gravity.END;
                tv.setLayoutParams(lp);
                tv.setBackgroundResource(type == 0 ? R.drawable.ic_chat_left : R.drawable.ic_chat_right);
                tv.setText((String) map.get("text"));
            }
        };
        listView.setAdapter(adapter);
        test();
    }

    @Override
    protected void addListener() {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    if (TextUtils.isEmpty(v.getText().toString()))
                        return false;
                    ArrayMap<String, Object> map = new ArrayMap<String, Object>();
                    map.put("type", 1);
                    map.put("text", v.getText().toString());
                    data.add(map);
                    editText.setText("");
                    adapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            }
        });
    }

    private void test() {
        ArrayMap<String, Object> am;
        for (int i = 0; i < 50; i++) {
            am = new ArrayMap<>();
            am.put("type", i % 2);
            am.put("text", "test abc RTABSKP,MM;L[{}]" + i);
            data.add(am);
        }
        adapter.notifyDataSetChanged();
    }

}
