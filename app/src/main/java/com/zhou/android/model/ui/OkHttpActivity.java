package com.zhou.android.model.ui;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.ToastUtils;
import com.zhou.android.model.presenter.OkHttpPresenter;
import com.zhou.android.model.view.IOkHttpView;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by ZhOu on 2018/4/19.
 */

public class OkHttpActivity extends BaseActivity implements IOkHttpView {

    private OkHttpPresenter okHttpPresenter;

    private ProgressBar progress;
    private TextView text;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_okhttp);
    }

    @Override
    protected void init() {
        okHttpPresenter = new OkHttpPresenter(this);
        progress = (ProgressBar) findViewById(R.id.progress);
        text = (TextView) findViewById(R.id.text);
    }

    @Override
    protected void addListener() {
        findViewById(R.id.btn_get).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okHttpPresenter.getMethod();
            }
        });
        findViewById(R.id.btn_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okHttpPresenter.postMethod();
            }
        });
        findViewById(R.id.btn_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okHttpPresenter.downloadFile();
            }
        });
    }

    @Override
    public void showResponse(Response response) {
        try {
            text.setText(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showResponse(String response) {
        if (response != null)
            text.setText(response);
    }

    @Override
    public void updateProgress(long progress) {
        this.progress.setProgress((int) progress);
    }

    @Override
    public void showToast(String message) {
        ToastUtils.show(this, message);
    }
}
