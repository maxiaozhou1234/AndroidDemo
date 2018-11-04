package com.zhou.android.camera2;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.util.ArrayMap;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * Camera2 工具类
 * Created by Administrator on 2018/10/30.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraUtil {

    private final static String TAG = "camera_util";
    public final static String FRONT = "CAMERA_FRONT";
    public final static String BACK = "CAMERA_BACK";
    private boolean isCameraBack = true;

    private CameraManager cameraManager;
    private Handler handler;
    private HandlerThread handlerThread;

    private ArrayMap<String, CameraConfig> cameraConfig = new ArrayMap<>();
    private int capacity = -1;

    private TextureView frontTextureView = null, backTextureView = null;

    public CameraUtil(Context context) {
        this(context, null, null);
    }

    /**
     * 传入需预览的 TextureView 控件，如不需要预览可传 null
     */
    public CameraUtil(Context context, @Nullable TextureView frontTextureView, @Nullable TextureView backTextureView) {
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
            for (String id : ids) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer cameraOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraOrientation == null)
                    continue;
//                    int displayRotation = wm.getDefaultDisplay().getRotation();
                if (cameraOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    CameraConfig config = new CameraConfig(id, characteristics, frontTextureView, frontAvailableListener, handler);
                    cameraConfig.put(FRONT, config);
                } else if (cameraOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                    CameraConfig config = new CameraConfig(id, characteristics, backTextureView, backAvailableListener, handler);
                    cameraConfig.put(BACK, config);
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
        previewFrameCallback = null;
        CameraConfig config = cameraConfig.get(isCameraBack ? BACK : FRONT);
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
        final CameraConfig config = cameraConfig.get(isCameraBack ? BACK : FRONT);
        if (config != null) {
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

    @SuppressLint("MissingPermission")
    private void openCamera(CameraManager cameraManager, CameraConfig config) {
        try {
            cameraManager.openCamera(config.cameraId, config.cameraStateCallback, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void switchCamera() {
        CameraConfig config = cameraConfig.get(isCameraBack ? BACK : FRONT);
        if (cameraConfig != null) {
            config.onPause();
        }
        isCameraBack = !isCameraBack;
        startPreview(null);
    }

    public void setDefauleCamera(boolean useBackCamera) {
        isCameraBack = useBackCamera;
    }

    public Size getSize() {
        Size size = null;
        CameraConfig config = cameraConfig.get(isCameraBack ? BACK : FRONT);
        if (config != null) {
            size = config.getSize();
        }
        return size;
    }

    /**
     * 获取流
     *
     * @param image
     * @param orientation 旋转方向
     */
    private byte[][] getBytes(Image image, int orientation) {
        if (capacity == -1) {
            Size size = getSize();
            capacity = (int) (size.getWidth() * size.getHeight() * 1.5);
        }
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

    //预览处理
    private OnImageAvailableListener frontAvailableListener = new OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (previewFrameCallback != null) {
                byte[][] bytes = getBytes(image, getOrientation());
                byte[] _bytes = new byte[capacity];
                int count = 0;
                for (byte[] b : bytes) {
                    System.arraycopy(b, 0, _bytes, count, b.length);
                    count += b.length;
                }
//                int orientation = getOrientation();
//                if (orientation == 90 || orientation == 270) {
//                    byte[][] tmp = new byte[3][];
//                    tmp[0] = new byte[bytes[0].length];
//                    tmp[1] = new byte[bytes[1].length];
//                    tmp[2] = new byte[bytes[2].length];
//
//                    YuvUtil.i420RotatePlane(bytes[0], bytes[1], bytes[2], tmp[0], tmp[1], tmp[2],
//                            getSize().getHeight(), getSize().getWidth(), orientation);
//                    previewFrameCallback.onCameraFront(tmp, getOrientation());
//                } else {
//
//                }
                previewFrameCallback.onCameraFront(bytes, getOrientation());
                previewFrameCallback.onCameraFront(_bytes, getOrientation());
            }
            image.close();
        }
    };

    private OnImageAvailableListener backAvailableListener = new OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (previewFrameCallback != null) {
                byte[][] bytes = getBytes(image, getOrientation());
                byte[] _bytes = new byte[capacity];
                int count = 0;
                for (byte[] b : bytes) {
                    System.arraycopy(b, 0, _bytes, count, b.length);
                    count += b.length;
                }
                previewFrameCallback.onCameraBack(bytes, getOrientation());
                previewFrameCallback.onCameraBack(_bytes, getOrientation());
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
