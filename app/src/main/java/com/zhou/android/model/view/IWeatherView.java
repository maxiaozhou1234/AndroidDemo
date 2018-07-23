package com.zhou.android.model.view;

import android.widget.TextView;

import org.json.JSONObject;

/**
 * Created by ZhOu on 2018/7/23.
 */

public interface IWeatherView {

    TextView getLocationView();

    void setWeather(String data);

    void showRequestWindow();

    void hideRequestWindow();
}
