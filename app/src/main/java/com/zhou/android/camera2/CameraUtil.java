package com.zhou.android.camera2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;

import com.zhou.android.common.ToastUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * Camera2 工具类
 * Created by Administrator on 2018/10/30.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraUtil {

    private final static String TAG = "camera_util";

    private final static String FRONT = "CAMERA_FRONT";
    private final static String BACK = "CAMERA_BACK";

    private String cameraName = BACK;
    private boolean autoFixSurface = true;

    private CameraManager cameraManager;
    private Handler workHandler, mainHandler;
    private HandlerThread handlerThread;

    private ArrayMap<String, CameraConfig> cameraConfig = new ArrayMap<>();
    private int capacity = -1;

    private View frontTextureView = null, backTextureView = null;

    public CameraUtil(Context context) {
        this(context, null, null);
    }

    /**
     * 传入需预览的 TextureView 控件，如不需要预览可传 null
     */
    public CameraUtil(Context context, @Nullable View frontTextureView, @Nullable View backTextureView) {
        this.frontTextureView = frontTextureView;
        this.backTextureView = backTextureView;
        init(context);
    }

    private void init(Context context) {

        //检查权限
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
            ToastUtils.show(context, "without camera permission!");
        }

        initHandlerThread();

        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        int displayRotation = wm.getDefaultDisplay().getRotation();
//        int degree = 0;
//        switch (displayRotation) {
//            case Surface.ROTATION_0:
//                degree = 0;
//                break;
//            case Surface.ROTATION_180:
//                degree = 180;
//                break;
//            case Surface.ROTATION_90:
//                degree = 90;
//                break;
//            case Surface.ROTATION_270:
//                degree = 270;
//                break;
//        }
//        Log.e(TAG, "displayRotation:" + displayRotation + " ,degree:" + degree);
        try {
            String[] ids = cameraManager.getCameraIdList();
            Log.i(TAG, "camera count = " + ids.length);
            for (String id : ids) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer cameraOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                Integer sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                if (sensorOrientation == null) {
                    sensorOrientation = 0;
                }
                Log.e(TAG, "SensorOrientation = " + sensorOrientation);
                if (cameraOrientation == null)
                    continue;
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (cameraOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    CameraConfig config = new CameraConfig(id, map, frontTextureView, frontAvailableListener, workHandler);
                    config.setSurfaceCallback(surfaceCallback);
                    frontAvailableListener.setOrientation(sensorOrientation);
                    cameraConfig.put(FRONT, config);
                } else if (cameraOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                    CameraConfig config = new CameraConfig(id, map, backTextureView, backAvailableListener, workHandler);
                    config.setSurfaceCallback(surfaceCallback);
                    backAvailableListener.setOrientation(sensorOrientation);
                    cameraConfig.put(BACK, config);

                    //日志输出
                    {
                        Log.d("zhou","========================================");
                        Size[] list = map.getOutputSizes(ImageFormat.JPEG);
                        StringBuilder sb = new StringBuilder();
                        for (Size size : list) {
                            sb.append("\nsize: ").append(size.getWidth())
                                    .append(" x ").append(size.getHeight());
                        }
                        Log.d("zhou", sb.toString());

                        for (Range<Integer> fpsRange : map.getHighSpeedVideoFpsRanges()) {
                            Log.d("zhou", "openCamera: [width, height] = " + fpsRange.toString());
                        }

                        Log.d("zhou", "LENS_FACING_BACK");
                        Log.d("zhou", "720 x 480 QUALITY_HIGH_SPEED_480P " + CamcorderProfile.hasProfile(1, CamcorderProfile.QUALITY_HIGH_SPEED_480P));
                        Log.d("zhou", "1280 x 720 QUALITY_HIGH_SPEED_720P " + CamcorderProfile.hasProfile(1, CamcorderProfile.QUALITY_HIGH_SPEED_720P));
                        Log.d("zhou", "1920 x 1080 QUALITY_HIGH_SPEED_1080P " + CamcorderProfile.hasProfile(1, CamcorderProfile.QUALITY_HIGH_SPEED_1080P));
                        Log.d("zhou", "3840 x 2160 QUALITY_HIGH_SPEED_2160P " + CamcorderProfile.hasProfile(1, CamcorderProfile.QUALITY_HIGH_SPEED_2160P));

                        Log.d("zhou", "720 x 480 QUALITY_480P " + CamcorderProfile.hasProfile(1, CamcorderProfile.QUALITY_480P));
                        Log.d("zhou", "1280 x 720 QUALITY_720P " + CamcorderProfile.hasProfile(1, CamcorderProfile.QUALITY_720P));
                        Log.d("zhou", "1920 x 1080 QUALITY_1080P " + CamcorderProfile.hasProfile(1, CamcorderProfile.QUALITY_1080P));
                        Log.d("zhou", "3840 x 2160 QUALITY_2160P " + CamcorderProfile.hasProfile(1, CamcorderProfile.QUALITY_2160P));

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
        workHandler = new Handler(handlerThread.getLooper());

        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void stopHandlerThread() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            workHandler.removeCallbacksAndMessages(null);
            workHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mainHandler.removeCallbacksAndMessages(null);
        mainHandler = null;
    }

    /**
     * 设置预览编码
     * <p>
     * NV21 format is not supported
     */
    public void setPreviewImageFormat(int imageFormat) {
        if (imageFormat == ImageFormat.JPEG) {
            Log.e(TAG, "JPEG 格式占用缓存过多，会导致预览卡顿，不建议使用该编码进行预览，最好使用其他编码格式");
        }
        Collection<CameraConfig> configs = cameraConfig.values();
        for (CameraConfig config : configs) {
            config.setImageFormat(imageFormat);
        }
    }

    /**
     * 释放<br/>
     * 最好在 onPause 中调用而不是在 onDestroy 中
     */
    public void release() {
        Log.d(TAG, "camera release.");
//        previewFrameCallback = null;
        onImageAvailableListener = null;
        CameraConfig config = cameraConfig.get(cameraName);
        if (cameraConfig != null)
            config.release();
        stopHandlerThread();
    }

    public void startPreview(Context context) {
        Log.d(TAG, "camera start preview.");
        if (cameraManager == null) {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
        capacity = -1;
        CameraConfig config = cameraConfig.get(cameraName);
        if (config != null) {
            config.startPreview();
        }
    }

    public void stopPreview() {
        CameraConfig config = cameraConfig.get(cameraName);
        if (cameraConfig != null) {
            config.onPause();
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(android.Manifest.permission.CAMERA)
    private void openCamera(CameraManager cameraManager, CameraConfig config) {
        try {
            cameraManager.openCamera(config.cameraId, config.cameraStateCallback, workHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void switchCamera() {
        CameraConfig config = cameraConfig.get(cameraName);
        if (cameraConfig != null) {
            config.onPause();
        }
        if (BACK.equals(cameraName)) {
            cameraName = FRONT;
        } else {
            cameraName = BACK;
        }
        startPreview(null);
    }

    public void setDefaultCamera(boolean useBackCamera) {
        cameraName = useBackCamera ? BACK : FRONT;
    }

    public Size getSize() {
        Size size = null;
        CameraConfig config = cameraConfig.get(cameraName);
        if (config != null) {
            size = config.getSize();
        }
        return size;
    }

    private Size size = null;

    /**
     * 获取流
     *
     * @param image       预览帧
     * @param orientation 旋转方向
     */
    private byte[][] getBytes(Image image, int orientation) {
//        if (capacity == -1) {
//            size = getSize();//Size
//            capacity = (int) (size.getWidth() * size.getHeight() * 1.5);
//        }
        int len = image.getPlanes().length;
        byte[][] bytes = new byte[len][];
        int count = 0;
        for (int i = 0; i < len; i++) {
            ByteBuffer buffer = image.getPlanes()[i].getBuffer();
            int remaining = buffer.remaining();
            byte[] data = new byte[remaining];
            buffer.get(data);
            bytes[i] = data;
//            bytes[i] = fixOrientation(data, size, orientation);
            count += remaining;
        }
        Log.d(TAG, "bytes = " + count);
        if (capacity == -1) {
            capacity = count;
        }
        return bytes;
    }

    //预览处理
    private OnImageAvailableListener frontAvailableListener = new OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            if (onImageAvailableListener != null) {
                onImageAvailableListener.onImageAvailable(reader);
            }
        }
    };

    private OnImageAvailableListener backAvailableListener = new OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            if (onImageAvailableListener != null) {
                onImageAvailableListener.onImageAvailable(reader);
            }
        }
    };

    private CameraConfig.SurfaceCallback surfaceCallback = new CameraConfig.SurfaceCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onSurfaceTextureAvailable(CameraConfig config) {
            if (autoFixSurface && config.getPreviewView() != null) {
                //宽高要反过来算
                int w, h;
                Size size = config.getSize();
                if (getSensorOrientation() == 0 || getSensorOrientation() == 180) {
                    w = size.getHeight();
                    h = size.getWidth();
                } else {
                    w = size.getWidth();
                    h = size.getHeight();
                }
                View view = config.getPreviewView();
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                lp.height = (int) (view.getWidth() * 1.0f * w / h);
                view.setLayoutParams(lp);
                view.requestLayout();
            }
            if (callback != null) {
                callback.onSurfaceTextureAvailable(config);
            }
            openCamera(cameraManager, config);
        }
    };

    private OnImageAvailableListener onImageAvailableListener = new OnImageAvailableListener();

    public void setOnImageAvailableListener(OnImageAvailableListener listener) {
        this.onImageAvailableListener = listener;
    }

    private CameraConfig.SurfaceCallback callback = null;

    public void setSurfaceCallback(CameraConfig.SurfaceCallback callback) {
        this.callback = callback;
    }

    public void setAutoFixSurface(boolean enable) {
        autoFixSurface = enable;
    }

    //旋转角度
    public int getSensorOrientation() {
        return BACK.equals(cameraName) ? backAvailableListener.getOrientation() : frontAvailableListener.getOrientation();
    }

    public void capturePicture() {
        cameraConfig.get(cameraName).capturePicture();
    }

    public void startRecordVideo(String outputPath) throws IOException {
        cameraConfig.get(cameraName).startRecordVideo(outputPath);
    }

    public void stopRecordVideo() {
        cameraConfig.get(cameraName).stopRecordVideo();
    }

    public void capturePictureWhileRecord() {
        cameraConfig.get(cameraName).capturePictureWhileRecord();
    }
}
