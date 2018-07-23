package com.zhou.android.model.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.zhou.android.common.LogInterceptor;
import com.zhou.android.model.view.IWeatherView;

import org.json.JSONArray;
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

    public WeatherPresenter(IWeatherView weatherView) {
        this.weatherView = weatherView;
        client = new OkHttpClient.Builder()
                .addInterceptor(new LogInterceptor())
                .build();
    }

    public void requestLocation() {


    }

    public void requestWeather() {
        String address = weatherView.getLocationView().getText().toString();
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
                .url("https://free-api.heweather.com/s6/weather/now?parameters")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("weather", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                Log.d("weather", data);

                final StringBuilder builder = new StringBuilder();
                try {
                    JSONObject json = new JSONObject(data);
                    JSONObject jo = json.optJSONArray("HeWeather6").optJSONObject(0);
                    JSONObject _j = jo.optJSONObject("basic");
                    builder.append("basic\n---------------------\n");
                    Iterator<String> it = _j.keys();
                    while (it.hasNext()) {
                        String k = it.next();
                        String _data = k + ":" + _j.optString(k) + "\n";
                        builder.append(_data);
                    }

                    builder.append("\nnow\n---------------------\n");
                    _j = jo.optJSONObject("now");
                    it = _j.keys();
                    while (it.hasNext()) {
                        String k = it.next();
                        String _data = k + ":" + _j.optString(k) + "\n";
                        builder.append(_data);
                    }

                    builder.append("\nupdate\n---------------------\n");
                    _j = jo.optJSONObject("update");
                    it = _j.keys();
                    while (it.hasNext()) {
                        String k = it.next();
                        String _data = k + ":" + _j.optString(k) + "\n";
                        builder.append(_data);
                    }
                    builder.append("\n---------------------\n");
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

}
