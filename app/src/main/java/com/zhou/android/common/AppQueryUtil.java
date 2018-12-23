package com.zhou.android.common;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 应用大小查询
 */
public class AppQueryUtil {

    private static String TAG = "app_util";

    public final static int WITHOUT_PERMISSION = 0;
    public final static int OTHER_ERROR = 1;

    private static int count = 0;

    public static void queryAppSize(Context context, Callback callback) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            queryAfterTarget26(context, callback);
        } else {
            queryBeforeTarget26(context, callback);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void queryAfterTarget26(final Context context, final Callback callback) {
        Log.i(TAG, "use queryAfterTarget26");
        new Thread(new Runnable() {
            @Override
            public void run() {

                final List<Item> data = new ArrayList<>();
                Item item;
                int error = -1;
                StorageStatsManager statsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
                UserHandle handler = UserHandle.getUserHandleForUid(-2);

                PackageManager pm = context.getPackageManager();
                List<ApplicationInfo> apps = pm.getInstalledApplications(0);

                boolean hasPermission = true;
                for (ApplicationInfo info : apps) {
                    item = new Item();
                    item.packageName = info.packageName;
                    item.type = (info.flags & ApplicationInfo.FLAG_SYSTEM) <= 0 ? 0 : 1;
                    try {
                        item.icon = pm.getApplicationIcon(info.packageName);
                    } catch (PackageManager.NameNotFoundException e) {
                        //
                    }

                    if (statsManager != null) {
                        try {
                            //permission.PACKAGE_USAGE_STATS
                            //info.storageUuid
                            if (!hasPermission && !context.getPackageName().equals(info.packageName)) {
                                //没有权限，不是当前应用，不申请
                                data.add(item);
                                continue;
                            }
                            StorageStats stats = statsManager.queryStatsForPackage(StorageManager.UUID_DEFAULT, info.packageName, handler);

                            item.codeBytes = stats.getAppBytes();
                            item.dataBytes = stats.getDataBytes();
                            item.cacheBytes = stats.getCacheBytes();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (SecurityException e) {
                            //这里说明没有权限，没有权限只能查询自身应用大小
                            if (hasPermission) {
                                hasPermission = false;
                                error = WITHOUT_PERMISSION;
                                e.printStackTrace();
                            }
                        }
                    } else {
                        error = OTHER_ERROR;
                    }
                    data.add(item);
                }
                if (callback != null) {
                    final int _e = error;
                    Handler ui = new Handler(Looper.getMainLooper());
                    ui.post(new Runnable() {
                        @Override
                        public void run() {
                            if (_e != -1) {
                                callback.onError(data, _e);
                            } else {
                                callback.onSuccess(data);
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private static void queryBeforeTarget26(final Context context, final Callback callback) {
        Log.i(TAG, "use queryBeforeTarget26");

        new Thread(new Runnable() {
            @Override
            public void run() {

                final HashMap<String, Item> map = new HashMap<>();
                Item item;
                int error = -1;

                PackageManager pm = context.getPackageManager();
                List<ApplicationInfo> apps = pm.getInstalledApplications(0);

                Method method = null;
                try {
                    method = PackageManager.class.getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                } catch (NoSuchMethodException e) {
                    error = OTHER_ERROR;
                }

                count = 0;
                IPackageStatsObserver packageStatsObserver = new IPackageStatsObserver.Stub() {
                    @Override
                    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                        count++;
                        Item item = map.get(pStats.packageName);
                        if (item != null) {
                            item.codeBytes = pStats.codeSize;
                            item.dataBytes = pStats.dataSize;
                            item.cacheBytes = pStats.cacheSize;
                        }
                        if (count == map.size()) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    List<Item> data = new ArrayList<>(map.values());
                                    callback.onSuccess(data);
                                }
                            });
                        }
                    }
                };

                for (ApplicationInfo info : apps) {
                    item = new Item();
                    item.packageName = info.packageName;
                    item.type = (info.flags & ApplicationInfo.FLAG_SYSTEM) <= 0 ? 0 : 1;
                    try {
                        item.icon = pm.getApplicationIcon(info.packageName);
                    } catch (PackageManager.NameNotFoundException e) {
                        //
                    }
                    if (method != null && error == -1) {
                        try {
                            method.invoke(pm, info.packageName, packageStatsObserver);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            error = OTHER_ERROR;
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                            error = OTHER_ERROR;
                        }
                    }

                    map.put(info.packageName, item);
                }

                if (error != -1) {
                    final int _e = error;
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            List<Item> data = new ArrayList<>(map.values());
                            callback.onError(data, _e);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 请求 PACKAGE_USAGE_STATS 权限，使用自带的权限查询，有很大可能是显示无权限
     */
    public static void requestPermission(final Context context) {
        AlertDialog ad = new AlertDialog.Builder(context)
                .setTitle("警告")
                .setMessage("缺少权限：permission.PACKAGE_USAGE_STATS\n需要在\"设置>安全\"中给应用提供权限")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ToastUtils.show(context, "已拒绝，请手动开启");
                    }
                })
                .create();
        ad.show();
    }

    public static class Item {
        public String packageName;
        public long codeBytes, dataBytes, cacheBytes;
        public Drawable icon;
        public int type;

        public String getAppSizeString() {
            return "code: " + Tools.getUnit(codeBytes)
                    + " data: " + Tools.getUnit(dataBytes)
                    + " cache: " + Tools.getUnit(cacheBytes);
        }

        public long getAppSize() {
            return codeBytes + dataBytes + cacheBytes;
        }
    }

    public interface Callback {
        void onSuccess(List<Item> data);

        void onError(List<Item> data, int error);
    }

}
