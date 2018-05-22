package com.zhou.android.model.view;

import android.widget.ImageView;

import java.util.List;

/**
 * Created by ZhOu on 2018/4/14.
 */

public interface IPictureView {

    void requestPermission();

    void onShow(List<String> pictures);

    void showToast(String message);

    boolean getCache();

    boolean getDisk();

    boolean getTransformation();

    ImageView getTarget();

    void clearImageView();
}
