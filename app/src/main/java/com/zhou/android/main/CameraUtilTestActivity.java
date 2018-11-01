package com.zhou.android.main;

import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.DoubleCameraUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * DoubleCameraUtil Test
 * Created by Administrator on 2018/10/31.
 */

public class CameraUtilTestActivity extends BaseActivity {

    private DoubleCameraUtil cameraUtil;

    private ByteBuffer doubleBuffer, tmpBuffer;
    private byte[] tmpCopy;

//    private GLSurfaceView glSurface;
//    private GLRenderer renderer;

    private Semaphore semaphore = new Semaphore(1);

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_camera_util);
    }

    @Override
    protected void init() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            String msg = "系统版本需高于安卓 5.0，当前版本（" + Build.VERSION.RELEASE + "），暂无法使用";
            Log.e("CameraTest", msg);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            return;
        }
        TextureView textureView = findViewById(R.id.textureView);
        cameraUtil = new DoubleCameraUtil(this, textureView, null);

//        glSurface = findViewById(R.id.glSurface);
//        renderer = new GLRenderer(0);
//        glSurface.setEGLContextClientVersion(2);
//        glSurface.setRenderer(renderer);
//        glSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void addListener() {
        if (cameraUtil != null) {

            final Size size = cameraUtil.getSize();
            final int capacity = size.getWidth() * size.getHeight();
            doubleBuffer = ByteBuffer.allocate(size.getWidth() * size.getHeight() * 3);
            tmpCopy = new byte[(int) (size.getWidth() * size.getHeight() * 1.5)];

            cameraUtil.setPreviewFrameCallback(new DoubleCameraUtil.OnPreviewFrameCallback() {
                @Override
                public void onCameraFront(byte[][] bytes, int orientation) {
                }

                @Override
                public void onCameraBack(byte[][] bytes, int orientation) {

                }

                @Override
                public void onCameraFront(byte[] bytes, int orientation) {

                    if (semaphore.tryAcquire()) {
                        try {
                            doubleBuffer.put(bytes, 0, capacity);
                            doubleBuffer.put(tmpCopy, 0, capacity);
                            doubleBuffer.put(bytes, capacity, capacity / 4);
                            doubleBuffer.put(tmpCopy, capacity, capacity / 4);
                            doubleBuffer.put(bytes, capacity * 5 / 4, capacity / 4);
                            doubleBuffer.put(tmpCopy, capacity * 5 / 4, capacity / 4);

                            doubleBuffer.flip();
                            byte[] data = doubleBuffer.array();
                            doubleBuffer.flip();
                            doubleBuffer.clear();
                        } finally {
                            semaphore.release();
                        }
                    }

                    //发送
                }

                @Override
                public void onCameraBack(byte[] bytes, int orientation) {
                    try {
                        if (semaphore.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                            tmpCopy = bytes;
                            semaphore.release();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraUtil != null) {
            cameraUtil.startPreview(this);
        }
    }

    @Override
    protected void onPause() {
        if (cameraUtil != null) {
            cameraUtil.release();
        }
        super.onPause();
    }

}
