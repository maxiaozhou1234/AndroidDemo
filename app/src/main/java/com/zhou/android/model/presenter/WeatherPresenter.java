package com.zhou.android.model.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.zhou.android.common.LocationUtil;
import com.zhou.android.common.LogInterceptor;
import com.zhou.android.model.view.IWeatherView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ZhOu on 2018/7/23.
 */

public class WeatherPresenter {

    private IWeatherView weatherView;
    private OkHttpClient client;
    private LocationUtil locationUtil;

    public WeatherPresenter(Context context, IWeatherView weatherView) {
        this.weatherView = weatherView;
        locationUtil = LocationUtil.getInstance(context);
        client = new OkHttpClient.Builder()
                .addInterceptor(new LogInterceptor())
                .build();
    }

    public void requestLocation() {

        weatherView.showRequestWindow();
        locationUtil.requestLocation(bdListener);

    }

    public void requestWeather() {
        String address = weatherView.getLocation();
        if (TextUtils.isEmpty(address)) {
            address = "深圳";
        }

        String sign = "aff66c2c67a34fdab322e47cce44a6b4";

        FormBody body = new FormBody.Builder(Charset.forName("UTF-8"))
                .add("location", address)
                .add("key", sign)
                .add("unit", "m")
                .build();

        Request request = new Request.Builder()
                .url("https://key_free-api.heweather.com/s6/weather/now?parameters")
                .post(body)
                .build();

        weatherView.showRequestWindow();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("weather", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                weatherView.hideRequestWindow();

                String data = response.body().string();
                Log.d("weather", data);

                final StringBuilder builder = new StringBuilder();
                try {
                    JSONObject json = new JSONObject(data);
                    JSONObject jo = json.optJSONArray("HeWeather6").optJSONObject(0);

                    JSONObject _j = jo.optJSONObject("basic");
                    Iterator<String> it;
                    if (_j != null) {
                        builder.append("basic\n---------------------\n");
                        it = _j.keys();
                        while (it.hasNext()) {
                            String k = it.next();
                            String _data = k + ":" + _j.optString(k) + "\n";
                            builder.append(_data);
                        }
                    }
                    if (_j != null) {
                        builder.append("\nnow\n---------------------\n");
                        _j = jo.optJSONObject("now");
                        it = _j.keys();
                        while (it.hasNext()) {
                            String k = it.next();
                            String _data = k + ":" + _j.optString(k) + "\n";
                            builder.append(_data);
                        }
                    }
                    if (_j != null) {
                        builder.append("\nupdate\n---------------------\n");
                        _j = jo.optJSONObject("update");
                        it = _j.keys();
                        while (it.hasNext()) {
                            String k = it.next();
                            String _data = k + ":" + _j.optString(k) + "\n";
                            builder.append(_data);
                        }
                        builder.append("\n---------------------\n");
                    }
                    builder.append("status:").append(jo.optString("status"))
                            .append("\n============================");

                } catch (JSONException e) {
                    e.printStackTrace();
                    builder.append(data);
                }

                weatherView.setWeather(builder.toString());
            }
        });
    }

    private LocationUtil.BDListener bdListener = new LocationUtil.BDListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            weatherView.hideRequestWindow();
            if (bdLocation != null) {
                String district = bdLocation.getDistrict();
                String city = bdLocation.getCity();
                String address = district.substring(0, district.length() - 1) + "," + city.substring(0, city.length() - 1);
                weatherView.setLocation(address);
            }
        }

        @Override
        public void onFail(int code, String msg) {
            String data = msg + " code: " + code;
            Log.d("weather", data);
            weatherView.toast(data);
        }
    };

    public void onDestroy() {
        locationUtil.onDestroy();
    }

}
