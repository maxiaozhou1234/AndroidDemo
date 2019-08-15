package com.zhou.android.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Created by mxz on 2019/8/15.
 */
public class Utils {

    public static Disposable checkPermission(Activity activity, String permission, String permissionZn) {
        RxPermissions rxPermissions = new RxPermissions(activity);
        Disposable disposable = null;
        if (!rxPermissions.isGranted(permission)) {
            disposable = rxPermissions.shouldShowRequestPermissionRationale(activity, permission)
                    .flatMap(b -> {
                        if (b) {
                            return rxPermissions.request(permission);
                        } else {
                            return Observable.just(false);
                        }
                    }).subscribe(b -> {
                        if (!b) {
                            new AlertDialog.Builder(activity)
                                    .setTitle("权限申请")
                                    .setMessage("缺少" + permissionZn + "权限，请在设置中开启")
                                    .setPositiveButton("设置", (DialogInterface dialog, int which) -> {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                                        activity.startActivity(intent);
                                        dialog.dismiss();
                                    })
                                    .setNegativeButton("拒绝", (DialogInterface dialog, int which) -> {
                                        Toast.makeText(activity, permissionZn + "权限已拒绝，请在应用管理中开启该权限", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    })
                                    .create().show();
                        }
                    });
        }
        return disposable;
    }
}
