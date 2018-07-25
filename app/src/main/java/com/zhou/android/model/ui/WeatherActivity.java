package com.zhou.android.model.ui;

import android.app.ProgressDialog;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.model.presenter.WeatherPresenter;
import com.zhou.android.model.view.IWeatherView;

/**
 * 天气
 * Created by ZhOu on 2018/7/23.
 */

public class WeatherActivity extends BaseActivity implements IWeatherView {

    private TextView location, weather;
    private ProgressDialog progressDialog;

    private WeatherPresenter presenter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_weather);
    }

    @Override
    protected void init() {
        location = (TextView) findViewById(R.id.location);
        weather = (TextView) findViewById(R.id.text);

        presenter = new WeatherPresenter(this, this);
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
    public void setLocation(final String address) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                location.setText(address);
            }
        });
    }

    @Override
    public String getLocation() {
        return location.getText().toString();
    }

    @Override
    public void setWeather(final String msg) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                weather.setText(msg);
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
        if (Looper.myLooper() != Looper.getMainLooper()
                && progressDialog != null
                && progressDialog.isShowing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });

        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public void toast(String msg) {
        Toast.makeText(WeatherActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
