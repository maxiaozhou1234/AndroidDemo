package com.zhou.android.model.view;

/**
 * Created by ZhOu on 2018/7/23.
 */

public interface IWeatherView {

    void setLocation(String address);

    String getLocation();

    void setWeather(String data);

    void showRequestWindow();

    void hideRequestWindow();

    void toast(String msg);
}
