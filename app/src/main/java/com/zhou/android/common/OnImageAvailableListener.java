package com.zhou.android.common;

import android.media.ImageReader;

/**
 * ImageAvailableListener
 * Created by Administrator on 2018/10/31.
 */

public class OnImageAvailableListener implements ImageReader.OnImageAvailableListener {

    private int orientation = 0;

    @Override
    public void onImageAvailable(ImageReader reader) {

    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getOrientation() {
        return orientation;
    }
}
