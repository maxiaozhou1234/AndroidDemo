package com.zhou.android.main;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.CommonAdapter;
import com.zhou.android.common.ViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 通话记录
 * <p>
 * Created by ZhOu on 2017/5/23.
 */

public class CallLogActivity extends BaseActivity {

    private int PermissionRequestCode = 0x1003;

    private ListView listView;
    private CommonAdapter<String> adapter;
    private List<String> data = new ArrayList<>();
    private AsyncQueryHandler asyncHandler;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_contact);
    }

    @Override
    protected void init() {
        listView = (ListView) findViewById(R.id.listView);
        adapter = new CommonAdapter<String>(this, data, android.R.layout.simple_list_item_1) {
            @Override
            protected void fillData(ViewHolder holder, int position) {
                ((TextView) holder.getView(android.R.id.text1)).setText(data.get(position));
            }
        };
        listView.setAdapter(adapter);

        asyncHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String number = cursor.getString(1);
                        if (TextUtils.isEmpty(number))
                            continue;
                        String name = cursor.getString(0);
                        long date = cursor.getLong(2);
                        int duration = cursor.getInt(3);
                        duration = duration < 0 ? 0 : duration;
                        int type = cursor.getInt(4);
                        String v;
                        if (name == null || name.trim().length() == 0 || name.equalsIgnoreCase("null"))
                            v = String.format(Locale.getDefault(), "%s\n%s   %d%s   %s",
                                    number, format.format(new Date(date)),
                                    duration < 60 ? duration : duration / 60,
                                    duration < 60 ? "分钟" : "小时",
                                    type == CallLog.Calls.INCOMING_TYPE ? "打入" : "拨出");
                        else
                            v = String.format(Locale.getDefault(), "%s   %s\n%s   %d%s   %s",
                                    name, format.format(new Date(date)), number,
                                    duration < 60 ? duration : duration / 60,
                                    duration < 60 ? "分钟" : "小时",
                                    type == CallLog.Calls.INCOMING_TYPE ? "打入" : "拨出");
                        data.add(v);
                    }
                    cursor.close();
                    adapter.notifyDataSetChanged();
                }
            }
        };
        if (Build.VERSION_CODES.JELLY_BEAN <= Build.VERSION.SDK_INT)
            checkPermission();
        else
            asyncLoadData();
    }

    @Override
    protected void addListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Snackbar.make(view, "snack " + position, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG)) {
                new AlertDialog.Builder(CallLogActivity.this)
                        .setTitle("通知")
                        .setMessage("本功能需要读取通话记录权限，请允许")
                        .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(CallLogActivity.this, new String[]{Manifest.permission.READ_CALL_LOG}, PermissionRequestCode);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(CallLogActivity.this, "申请通话记录权限已拒绝，请在应用管理中开启该权限", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, PermissionRequestCode);
        } else
            asyncLoadData();
    }

    private void asyncLoadData() {
        asyncHandler.startQuery(0, null, CallLog.Calls.CONTENT_URI,
                new String[]{
                        CallLog.Calls.CACHED_NAME,
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.DATE,
                        CallLog.Calls.DURATION,
                        CallLog.Calls.TYPE
                },
                null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionRequestCode == requestCode) {
            if (PackageManager.PERMISSION_DENIED == grantResults[0])
                Toast.makeText(CallLogActivity.this, "请求读取通话记录失败，请在应用管理器中开启该权限", Toast.LENGTH_SHORT).show();
            else
                asyncLoadData();
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

