package com.zhou.android.main;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 禁止屏幕截屏
 * <p>
 * Created by ZhOu on 2017/3/23.
 */

public class ForbidScreenshotActivity extends BaseActivity {

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_forbid_screenshot);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void init() {

    }

    @Override
    protected void addListener() {

    }

    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    public void onClick(View v) {

        int id = v.getId();
        if (R.id.btn_screen == id) {

            v.setVisibility(View.GONE);

            View view = getWindow().getDecorView();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();

            Bitmap bitmap = view.getDrawingCache();
            Bitmap _b = Bitmap.createBitmap(bitmap, 0, 0, Tools.getScreenWidth(this), Tools.getScreenHeight(this));
            view.destroyDrawingCache();
            saveBitmapToFile(_b);
            v.setVisibility(View.VISIBLE);
        } else if (R.id.btn_snap == id) {
            //开启禁止截屏的话，这里将是黑屏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                createCapture();
            else
                Toast.makeText(this, "Your Android version is lower than LOLLIPOP,count not use this.", Toast.LENGTH_SHORT).show();
        }
    }

    private int CaptureRequestCode = 10011;
    private MediaProjectionManager mediaManager;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createCapture() {
        if (mediaManager == null)
            mediaManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mediaManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, CaptureRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CaptureRequestCode && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mediaManager == null)
                    mediaManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                MediaProjection mediaProjection = mediaManager.getMediaProjection(resultCode, data);
                int screenWidth = Tools.getScreenWidth(this);
                int screenHeight = Tools.getScreenHeight(this);
                ImageReader imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 1);
                mediaProjection.createVirtualDisplay("SnapShot", screenWidth, screenHeight, (int) Tools.getDpi(this),
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, imageReader.getSurface(), null, null);
                SystemClock.sleep(1000);
                Image image = imageReader.acquireNextImage();
                int w = image.getWidth();
                int h = image.getHeight();
                Image.Plane[] plane = image.getPlanes();
                ByteBuffer buffer = plane[0].getBuffer();
                int pixelStride = plane[0].getPixelStride();
                int rowStride = plane[0].getRowStride();
                int rowPadding = rowStride - pixelStride * w;
                Bitmap bitmap = Bitmap.createBitmap(w + rowPadding / pixelStride, h, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                saveBitmapToFile(bitmap);
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveBitmapToFile(Bitmap b) {
        if (b == null)
            return;
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/zhou/screenshot/" + format.format(new Date()) + ".png");
            if (!file.getParentFile().exists())
                file.getParentFile().mkdir();
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
            b.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
