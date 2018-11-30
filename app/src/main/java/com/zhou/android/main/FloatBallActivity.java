package com.zhou.android.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.Tools;
import com.zhou.android.service.FloatBallService;

/**
 * 悬浮球开关
 * Created by ZhOu on 2017/3/23.
 */

public class FloatBallActivity extends BaseActivity {

    private ImageView wifi;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_floatball);
    }

    @Override
    protected void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                showAlertDialog();
            }
        }

        wifi = findViewById(R.id.wifi);
    }

    @Override
    protected void addListener() {

    }

    public void onClick(View v) {
        int id = v.getId();
        if (R.id.open == id) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "请在设置中开启弹窗权限", Toast.LENGTH_LONG).show();
                return;
            }
            if (!Tools.isServiceRunning(this, FloatBallService.class.getName())) {
                startService(new Intent(this, FloatBallService.class));
            }
        } else if (R.id.close == id) {
            if (Tools.isServiceRunning(this, FloatBallService.class.getName())) {
                stopService(new Intent(this, FloatBallService.class));
            }
        } else if (R.id.startAnim == id) {

            wifi.setBackgroundResource(R.drawable.wifi_anim);
            AnimationDrawable animation = (AnimationDrawable) wifi.getBackground();
            animation.start();

        } else if (R.id.stopAnim == id) {
            stopAnimation();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (1001 == requestCode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                showAlertDialog();
            }
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("提示").setMessage("该功能需要悬浮窗权限，请开启")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 1001);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void stopAnimation(){
        AnimationDrawable animation = null;
        try {
            animation = (AnimationDrawable) wifi.getBackground();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        if (animation != null && animation.isRunning()) {
            animation.stop();
        }
    }

    @Override
    protected void onDestroy() {
        stopAnimation();
        super.onDestroy();
    }
}
