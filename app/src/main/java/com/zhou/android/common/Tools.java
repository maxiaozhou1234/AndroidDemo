package com.zhou.android.common;

import android.app.ActivityManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import sun.misc.BASE64Encoder;

/**
 * 工具类
 * Created by ZhOu on 2017/3/23.
 */

public class Tools {

    public static int dip2px(Context context, int d) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (density * d + 0.5F);
    }

    public static float dip2pxf(Context context, int d) {
        float density = context.getResources().getDisplayMetrics().density;
        return density * d + 0.5F;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static boolean isServiceRunning(Context context, String className) {
        boolean result = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> list = am.getRunningServices(Integer.MAX_VALUE);
        if (list != null && list.size() > 0) {
            for (ActivityManager.RunningServiceInfo info : list) {
                String name = info.service.getClassName();
                if (name.equals(className)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    public static String[] getTopActivity(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.RunningTaskInfo runningTaskInfo = am.getRunningTasks(1).get(0);
        String packageName = runningTaskInfo.topActivity.getPackageName();
        String activityName = runningTaskInfo.topActivity.getClassName();
        return new String[]{packageName, activityName};
    }

    public static float getDpi(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int getStateHeight(Context context) {
        int statusHeight;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            Field field = clazz.getField("status_bar_height");
            int id = Integer.valueOf(field.get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(id);
        } catch (Exception e) {
            e.printStackTrace();
            statusHeight = (int) (getDpi(context) * 25 + 0.5f);
        }
        return statusHeight;
    }

    public static Uri parseImageAbsolutePath(ContentResolver contentResolver, String path) {
        Uri uri = null;
        if (!TextUtils.isEmpty(path)) {
            if (path.startsWith("file://")) {
                path = path.substring("file://".length(), path.length());
            }
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.DATA + "='" + path + "'", null, null);
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                Uri tmp = Uri.parse("content://media/external/images/media/" + id);
                if (tmp != null)
                    uri = tmp;
            }
            if (cursor != null) {
                cursor.close();
            }
        }

        return uri;
    }

    private static String[] units = {"B", "KB", "MB", "GB", "TB"};

    public static String getUnit(float size) {
        int index = 0;
        while (size > 1024 && index < 4) {
            size = size / 1024;
            index++;
        }
        return String.format(Locale.getDefault(), " %.2f %s", size, units[index]);
    }

    public static void closeIO(Closeable... closeable) {
        for (Closeable c : closeable) {
            if (c == null)
                continue;
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getBroadcast() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        try {
            for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements(); ) {
                NetworkInterface ni = niEnum.nextElement();
                if (ni.getName().toLowerCase().equals("wlan0")) {
                    if (!ni.isLoopback()) {
                        for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                            if (interfaceAddress.getBroadcast() != null) {
                                return interfaceAddress.getBroadcast().toString().substring(1);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            //
        }
        return null;
    }
}
