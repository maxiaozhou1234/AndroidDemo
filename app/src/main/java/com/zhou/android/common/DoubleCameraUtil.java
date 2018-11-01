package com.zhou.android.common;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 3399 双摄同时开启，都被识别为后置解决方案
 * Created by Administrator on 2018/10/30.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DoubleCameraUtil {

    private final static String TAG = "camera_util";

    private CameraManager cameraManager;
    private Handler handler;
    private HandlerThread handlerThread;

    private List<CameraConfig> cameraConfig = null;

    private TextureView frontTextureView = null, backTextureView = null;

    public DoubleCameraUtil(Context context) {
        this(context, null, null);
    }

    /**
     * 传入需预览的 TextureView 控件，如不需要预览可传 null
     */
    public DoubleCameraUtil(Context context, @Nullable TextureView frontTextureView, @Nullable TextureView backTextureView) {
        this.frontTextureView = frontTextureView;
        this.backTextureView = backTextureView;
        init(context);
    }

    private void init(Context context) {

        //检查权限
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
            throw new SecurityException("without camera permission!");
        }

        initHandlerThread();

        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        try {
            String[] ids = cameraManager.getCameraIdList();
            Log.i(TAG, "camera count = " + ids.length);
            cameraConfig = new ArrayList<>(ids.length);
            for (String id : ids) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer cameraOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraOrientation == null)
                    continue;
                if (cameraOrientation == CameraCharacteristics.LENS_FACING_BACK) {
//                    int displayRotation = wm.getDefaultDisplay().getRotation();

                    if (cameraConfig.size() == 0) {
                        CameraConfig config = new CameraConfig(id, characteristics, frontTextureView, frontAvailableListener, handler);
                        cameraConfig.add(config);
                    } else {
                        CameraConfig config = new CameraConfig(id, characteristics, backTextureView, backAvailableListener, handler);
                        cameraConfig.add(config);
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initHandlerThread() {
        handlerThread = new HandlerThread("CameraThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    private void stopHandlerThread() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置预览编码
     */
    public void setPreviewImageFormat(int imageFormat) {
        if (imageFormat == ImageFormat.JPEG) {
            Log.e(TAG, "JPEG 格式占用缓存过多，会导致预览卡顿，不建议使用该编码进行预览，最好使用其他编码格式");
        }
        for (CameraConfig config : cameraConfig) {
            config.setImageFormat(imageFormat);
        }
    }

    /**
     * 释放<br/>
     * 最好在 onPause 中调用而不是在 onDestroy 中
     */
    public void release() {
        Log.d(TAG, "camera release.");
        previewFrameCallback = null;
        for (CameraConfig config : cameraConfig) {
            config.release();
        }
        stopHandlerThread();
    }

    public void startPreview(Context context) {
        Log.d(TAG, "camera start preview.");
        if (cameraManager == null) {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
        for (final CameraConfig config : cameraConfig) {
            if (config.textureView != null) {
                if (config.textureView.isAvailable()) {
                    openCamera(cameraManager, config);
                } else {
                    config.textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                        @Override
                        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                            openCamera(cameraManager, config);
                        }

                        @Override
                        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                        }

                        @Override
                        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                            return true;
                        }

                        @Override
                        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                        }
                    });
                }
            } else {
                openCamera(cameraManager, config);
            }
        }
    }

    private void openCamera(CameraManager cameraManager, CameraConfig config) {
        try {
            cameraManager.openCamera(config.cameraId, config.cameraStateCallback, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public Size getSize() {
        return Collections.max(cameraConfig, new Comparator<CameraConfig>() {
            @Override
            public int compare(CameraConfig o1, CameraConfig o2) {
                return Long.signum(o1.getFrameSize() - o2.getFrameSize());
            }
        }).getSize();
    }

    private byte[][] getBytes(Image image) {
        int len = image.getPlanes().length;
        byte[][] bytes = new byte[len][];
        int count = 0;
        for (int i = 0; i < len; i++) {
            ByteBuffer buffer = image.getPlanes()[i].getBuffer();
            int remaining = buffer.remaining();
            byte[] data = new byte[remaining];
            byte[] _data = new byte[remaining];
            buffer.get(data);
            System.arraycopy(data, 0, _data, 0, remaining);
            bytes[i] = _data;
            count += remaining;
        }
        Log.d(TAG, "bytes = " + count);
        return bytes;
    }

    private int capacity = -1;

    private byte[] getBytes2(Image image) {
        if (capacity == -1) {
            Size size = getSize();
            capacity = (int) (size.getWidth() * size.getHeight() * 1.5);
        }
        int len = image.getPlanes().length;
        byte[] bytes = new byte[capacity];
        int count = 0;
        for (int i = 0; i < len; i++) {
            ByteBuffer buffer = image.getPlanes()[i].getBuffer();
            int remaining = buffer.remaining();
            byte[] data = new byte[remaining];
            byte[] _data = new byte[remaining];
            buffer.get(data);

            System.arraycopy(data, 0, bytes, count, remaining);
            count += remaining;
        }
        Log.d(TAG, "bytes = " + count);
        return bytes;
    }

    //预览处理
    private OnImageAvailableListener frontAvailableListener = new OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            Log.d(TAG, " size = " + reader.getWidth() * reader.getHeight() + " ,width = " + reader.getWidth() + " height = " + reader.getHeight() + " = " + " bytes = " + getBytes(image).length);
            if (previewFrameCallback != null) {
//                previewFrameCallback.onCameraFront(getBytes(image), getOrientation());
                previewFrameCallback.onCameraFront(getBytes2(image), getOrientation());
            }
            image.close();
        }
    };

    private OnImageAvailableListener backAvailableListener = new OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (previewFrameCallback != null) {
//                previewFrameCallback.onCameraBack(getBytes(image), getOrientation());
                previewFrameCallback.onCameraBack(getBytes2(image), getOrientation());
            }
            image.close();
        }
    };

    private OnPreviewFrameCallback previewFrameCallback = null;

    /**
     * 设置预览回调，均在子线程
     */
    public void setPreviewFrameCallback(OnPreviewFrameCallback previewFrameCallback) {
        this.previewFrameCallback = previewFrameCallback;
    }

    public interface OnPreviewFrameCallback {
        void onCameraFront(byte[][] bytes, int orientation);

        void onCameraBack(byte[][] bytes, int orientation);

        void onCameraFront(byte[] bytes, int orientation);

        void onCameraBack(byte[] bytes, int orientation);
    }

}
