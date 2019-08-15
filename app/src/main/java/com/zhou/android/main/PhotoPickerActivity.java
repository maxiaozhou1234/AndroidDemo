package com.zhou.android.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.ui.PhotoPicker;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * 图片选择
 * Created by ZhOu on 2017/8/4.
 */

public class PhotoPickerActivity extends BaseActivity {

    private PhotoPicker photoPicker;
    private TextView tv;

    private RxPermissions rxPermissions;
    private Disposable disposable;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_photo_picker);
    }

    @Override
    protected void init() {
        photoPicker = (PhotoPicker) findViewById(R.id.photoPicker);
        tv = (TextView) findViewById(R.id.tv);
        rxPermissions = new RxPermissions(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (!rxPermissions.isGranted(Manifest.permission_group.STORAGE)) {
                disposable = rxPermissions.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .flatMap(b -> {
                            if (b) {
                                return rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            } else {
                                return Observable.just(false);
                            }
                        }).subscribe(b -> {
                            if (!b) {//显示弹窗
                                new AlertDialog.Builder(PhotoPickerActivity.this)
                                        .setTitle("权限申请")
                                        .setMessage("缺少读取存储卡权限，请在设置中开启")
                                        .setPositiveButton("设置", (DialogInterface dialog, int which) -> {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                                            startActivity(intent);
                                            dialog.dismiss();
                                        })
                                        .setNegativeButton("拒绝", (DialogInterface dialog, int which) -> {
                                            Toast.makeText(PhotoPickerActivity.this, "读取存储权限已拒绝，请在应用管理中开启该权限", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        })
                                        .create().show();
                            }
                        });
            }
        }
    }

    @Override
    protected void addListener() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (PhotoPicker.REQUEST_CODE_CHOOSE == requestCode)
            photoPicker.onActivityResult(requestCode, resultCode, data);
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_ok) {
            if (tv.getText().length() > 0)
                tv.setText("");
            else
                tv.setText(photoPicker.getPhotos().toString().replaceAll("[\\[|\\]]", ""));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }
}
