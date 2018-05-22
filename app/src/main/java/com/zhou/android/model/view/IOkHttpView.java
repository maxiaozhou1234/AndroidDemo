package com.zhou.android.model.view;

import okhttp3.Response;

/**
 * Created by ZhOu on 2018/4/19.
 */

public interface IOkHttpView {
    void showResponse(Response response);

    void showResponse(String response);

    void updateProgress(long progress);

    void showToast(String message);
}
