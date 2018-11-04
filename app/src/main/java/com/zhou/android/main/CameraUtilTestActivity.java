package com.zhou.android.main;

import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.camera2.CameraUtil;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.opengl.GLFrameRenderer;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

/**
 * CameraUtil Test
 * Created by Administrator on 2018/10/31.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraUtilTestActivity extends BaseActivity {

    private CameraUtil cameraUtil;

    private ByteBuffer doubleBuffer, tmpBuffer;
    private byte[] tmpCopy;

    private GLSurfaceView glSurface;
    private GLFrameRenderer renderer;

//    private Semaphore semaphore = new Semaphore(1);

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
        cameraUtil = new CameraUtil(this, null, textureView);

        glSurface = findViewById(R.id.glSurface);
        glSurface.setEGLContextClientVersion(2);
        renderer = new GLFrameRenderer(glSurface);
        glSurface.setRenderer(renderer);
        glSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void addListener() {
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraUtil != null) {
                    cameraUtil.switchCamera();
                }
            }
        });

        if (cameraUtil != null) {

            final Size size = cameraUtil.getSize();
            final int capacity = size.getWidth() * size.getHeight();
            doubleBuffer = ByteBuffer.allocate(size.getWidth() * size.getHeight() * 3);
            tmpCopy = new byte[(int) (size.getWidth() * size.getHeight() * 1.5)];

            cameraUtil.setPreviewFrameCallback(new CameraUtil.OnPreviewFrameCallback() {
                @Override
                public void onCameraFront(byte[][] bytes, int orientation) {
//                    if (renderer != null && bytes.length == 3) {
//                        renderer.update(bytes[0], bytes[1], bytes[2]);
//                    }
                }

                @Override
                public void onCameraBack(byte[][] bytes, int orientation) {
                    if (renderer != null && bytes.length == 3) {
                        renderer.update(bytes[0], bytes[1], bytes[2]);
                    }
                }

                @Override
                public void onCameraFront(byte[] bytes, int orientation) {

//                    if (semaphore.tryAcquire()) {
//                        try {
//                            doubleBuffer.put(bytes, 0, capacity);
//                            doubleBuffer.put(tmpCopy, 0, capacity);
//                            doubleBuffer.put(bytes, capacity, capacity / 4);
//                            doubleBuffer.put(tmpCopy, capacity, capacity / 4);
//                            doubleBuffer.put(bytes, capacity * 5 / 4, capacity / 4);
//                            doubleBuffer.put(tmpCopy, capacity * 5 / 4, capacity / 4);
//
//                            doubleBuffer.flip();
//                            byte[] data = doubleBuffer.array();
//                            doubleBuffer.flip();
//                            doubleBuffer.clear();
//                        } finally {
//                            semaphore.release();
//                        }
//                    }

                    //发送
                }

                @Override
                public void onCameraBack(byte[] bytes, int orientation) {
//                    try {
//                        if (semaphore.tryAcquire(100, TimeUnit.MILLISECONDS)) {
//                            tmpCopy = bytes;
//                            semaphore.release();
//                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!renderer.isInit()) {

            glSurface.post(new Runnable() {
                @Override
                public void run() {
                    int w = glSurface.getWidth();
                    int h = glSurface.getHeight();
                    Log.d("camera_util", "width = " + w + " , height = " + h);

                    Size size = cameraUtil.getSize();
                    renderer.update(w, h, size.getWidth(), size.getHeight());
                }
            });
        }

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
