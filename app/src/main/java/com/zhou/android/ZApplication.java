package com.zhou.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;

import com.zhou.android.common.GV;
import com.zhou.android.kotlin.ActivityMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhOu on 2017/2/9.
 */

public class ZApplication extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sp = getSharedPreferences(GV.Config, 0);
        if (sp.contains(GV.HasFingerPrintApi))
            return;
        try {
            //判断是否有指纹Api
            Class.forName("android.hardware.fingerprint.FingerprintManager");
            sp.edit().putBoolean(GV.HasFingerPrintApi, true).apply();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            sp.edit().putBoolean(GV.HasFingerPrintApi, false).apply();
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        context = base;
        MultiDex.install(base);
        ArrayList uncheck = new ArrayList<Class>();
        uncheck.add(MainActivity.class);
        ActivityMonitor.get().attach(base,uncheck);
    }
}
