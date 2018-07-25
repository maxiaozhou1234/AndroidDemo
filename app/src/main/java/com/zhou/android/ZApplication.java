package com.zhou.android;

import android.app.Application;
import android.content.SharedPreferences;

import com.zhou.android.common.GV;

/**
 *
 * Created by ZhOu on 2017/2/9.
 */

public class ZApplication extends Application {

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
}
