package com.zhou.android.main;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.CommonAdapter;
import com.zhou.android.common.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 通讯录
 * <p>
 * Created by ZhOu on 2017/5/23.
 */

public class ContactActivity extends BaseActivity {

    private int PermissionRequestCode = 0x1002;

    private ListView listView;
    private List<String> data = new ArrayList<>();
    private CommonAdapter<String> adapter;

    //    private ContentResolver contentResolver;
    private AsyncQueryHandler asyncHandler;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_contact);
    }

    @Override
    protected void init() {
        listView = (ListView) findViewById(R.id.listView);
        adapter = new CommonAdapter<String>(this, data, android.R.layout.simple_list_item_1) {
            @Override
            protected void fillData(ViewHolder viewHolder, int position) {
                ((TextView) viewHolder.getView(android.R.id.text1)).setText(data.get(position));
            }
        };
        listView.setAdapter(adapter);
        asyncHandler = new AsyncQueryHandler(getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                if (isFinishing())
                    return;
                if (0 == token) {
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
//                            String contractId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            data.add(name);
                            adapter.notifyDataSetChanged();
                        }
                        cursor.close();
                    }
                } else if (1 == token) {
                    if (cursor != null) {
                        HashMap<Long, String> _d = new HashMap<>();
                        while (cursor.moveToNext()) {
                            long id = cursor.getLong(0);
                            String name = cursor.getString(1);
                            String number = cursor.getString(2);
                            String value = _d.get(id);
                            if (value == null) {
                                _d.put(id, name + "\n" + number);
                            } else {
                                _d.put(id, value + "\n" + number);
                            }
                        }
                        cursor.close();
                        data.addAll(_d.values());
                        adapter.notifyDataSetChanged();
                    }
                } else
                    super.onQueryComplete(token, cookie, cursor);
            }
        };
        checkPermission();
    }

    @Override
    protected void addListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Snackbar.make(view, "Snack bar " + position, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, new String[]{}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String item;
                String contractId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contractId, null, null);
                item = name;
                if (phoneCursor != null) {
                    while (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        item += "\n" + phoneNumber;
                    }
                    phoneCursor.close();
                }
                data.add(item);
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void asyncLoadData() {
        asyncHandler.startQuery(0, null, ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Contacts.DISPLAY_NAME}, null, null, null);
    }

    private void asyncLoadData2() {
        asyncHandler.startQuery(1, null, ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null, null, null);
    }

    private void checkPermission() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) &&
                PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS))
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {
                new AlertDialog.Builder(this)
                        .setTitle("通知")
                        .setMessage("本功能需要读取通讯录权限，请允许")
                        .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(ContactActivity.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, PermissionRequestCode);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(ContactActivity.this, "申请通讯录权限已拒绝，请在应用管理中开启该权限", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, PermissionRequestCode);
        else
//            loadData();
            asyncLoadData2();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionRequestCode == requestCode) {
            boolean result = false;
            for (int i : grantResults) {
                if (i == PackageManager.PERMISSION_DENIED) {
                    result = true;
                    Toast.makeText(ContactActivity.this, "获取通讯录权限失败，请在应用管理中开启该权限", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            if (!result) asyncLoadData2();
//                loadData();
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
