package com.zhou.android.model.ui;

import android.app.ProgressDialog;
import android.view.View;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.model.presenter.WeatherPresenter;
import com.zhou.android.model.view.IWeatherView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * 天气
 * Created by ZhOu on 2018/7/23.
 */

public class WeatherActivity extends BaseActivity implements IWeatherView {

    private TextView location, text;
    private ProgressDialog progressDialog;

    private WeatherPresenter presenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_weather);
    }

    @Override
    protected void init() {
        location = (TextView) findViewById(R.id.location);
        text = (TextView) findViewById(R.id.text);

        presenter = new WeatherPresenter(this);
    }

    @Override
    protected void addListener() {

        findViewById(R.id.request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.requestLocation();
            }
        });

        findViewById(R.id.query).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.requestWeather();
            }
        });
    }

    @Override
    public TextView getLocationView() {
        return location;
    }

    @Override
    public void setWeather(final String msg) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(msg);
            }
        });
    }

    @Override
    public void showRequestWindow() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("数据加载中……");
        }
        progressDialog.show();

    }

    @Override
    public void hideRequestWindow() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
