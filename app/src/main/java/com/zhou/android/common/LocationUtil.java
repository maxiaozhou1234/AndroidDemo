package com.zhou.android.common;

import android.content.Context;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by ZhOu on 2018/7/25.
 */

public class LocationUtil {

    private static LocationUtil instance;
    private LocationClient locationClient;
    private LocationListener locationListener;

    public static LocationUtil getInstance(Context context) {
        if (instance == null)
            instance = new LocationUtil(context);
        return instance;
    }

    public LocationUtil(Context context) {
        locationClient = new LocationClient(context);
        locationClient.setLocOption(getOption());
        locationClient.registerLocationListener(locationListener = new LocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdListener != null) {
                    locationClient.stop();
                    bdListener.onReceiveLocation(bdLocation);
                }
            }

            @Override
            public void onLocDiagnosticMessage(int i, int i1, String s) {
                if (bdListener != null) {
                    bdListener.onFail(i, s);
                }
            }
        });
    }

    private LocationClientOption getOption() {
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setCoorType("bd09ll");
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
//        option.setScanSpan(1000);
        option.setLocationNotify(false);
        return option;
    }

    public void requestLocation(BDListener bdListener) {
        this.bdListener = bdListener;
        if (!locationClient.isStarted()) {
            locationClient.start();
        } else {
            locationClient.requestLocation();
        }
    }

    public void onDestroy() {
        bdListener = null;
        locationClient.stop();
        locationClient.unRegisterLocationListener(locationListener);
        locationClient = null;
        instance = null;
    }


    private static class LocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

        }

        @Override
        public void onLocDiagnosticMessage(int i, int i1, String s) {

        }
    }

    private BDListener bdListener = null;

    public interface BDListener {
        void onReceiveLocation(BDLocation bdLocation);

        void onFail(int code, String msg);
    }

}
