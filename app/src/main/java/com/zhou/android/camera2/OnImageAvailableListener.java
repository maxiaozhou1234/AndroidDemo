package com.zhou.android.camera2;

import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * ImageAvailableListener
 * Created by Administrator on 2018/10/31.
 */

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
