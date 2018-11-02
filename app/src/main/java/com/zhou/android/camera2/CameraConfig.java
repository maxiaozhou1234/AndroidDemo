package com.zhou.android.camera2;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 相机基本配置
 * Created by Administrator on 2018/10/31.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraConfig {

    private final static String TAG = "CameraConfig";
    private String[] _error = {"ERROR_CAMERA_IN_USE", "ERROR_MAX_CAMERAS_IN_USE", "ERROR_CAMERA_DISABLED", "ERROR_CAMERA_DEVICE", "ERROR_CAMERA_SERVICE"};

    String cameraId;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    CameraDevice.StateCallback cameraStateCallback;

    private Handler handler;
    TextureView textureView;
    private ImageReader imageReader;
    private OnImageAvailableListener imageAvailableListener;
    private Size largest;

    public CameraConfig(String cameraId, CameraCharacteristics characteristics, @Nullable TextureView textureView, OnImageAvailableListener listener, Handler handler) {

        this.cameraId = cameraId;
        this.textureView = textureView;
        this.imageAvailableListener = listener;
        this.handler = handler;

        StreamConfigurationMap size = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (size != null) {
            //暂定使用最大的尺寸 最小尺寸
            largest = Collections.max(Arrays.asList(size.getOutputSizes(ImageFormat.JPEG)),
                    new Comparator<Size>() {
                        @Override
                        public int compare(Size lhs, Size rhs) {
                            return Long.signum((long) rhs.getWidth() * rhs.getHeight() -
                                    (long) lhs.getWidth() * lhs.getHeight());
//                            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
//                                    (long) rhs.getWidth() * rhs.getHeight());
                        }
                    });
            Log.d(TAG, "width = " + largest.getWidth() + " height = " + largest.getHeight());
            //三通道 YUV  YV12,YUV_420_888,NV21 但 NV21 不支持
            imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.YV12, 1);
            imageReader.setOnImageAvailableListener(imageAvailableListener, handler);
        }

        this.cameraStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                createCameraSession();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                camera.close();
                cameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                camera.close();
                cameraDevice = null;
                Log.e(TAG, _error[error]);
            }
        };

        //noinspection ConstantConditions
        int mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//        boolean swappedDimensions = false;
//        switch (displayRotation) {
//            case Surface.ROTATION_0:
//            case Surface.ROTATION_180:
//                if (mSensorOrientation == 90 || mSensorOrientation == 270) {
//                    swappedDimensions = true;
//                }
//                break;
//            case Surface.ROTATION_90:
//            case Surface.ROTATION_270:
//                if (mSensorOrientation == 0 || mSensorOrientation == 180) {
//                    swappedDimensions = true;
//                }
//                break;
//            default:
//                Log.e(TAG, "Display rotation is invalid: " + displayRotation);
//        }
        listener.setOrientation(mSensorOrientation);
    }

    private void createCameraSession() {

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            List<Surface> output = new ArrayList<>(textureView == null ? 1 : 2);
            output.add(imageReader.getSurface());
            captureRequestBuilder.addTarget(imageReader.getSurface());
            if (textureView != null) {
                SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
                assert surfaceTexture != null;
                Surface surface = new Surface(surfaceTexture);
                output.add(surface);
                captureRequestBuilder.addTarget(surface);
            }

            cameraDevice.createCaptureSession(output, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (null == cameraDevice)//camera is closed.
                        return;
                    try {
                        cameraCaptureSession = session;
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        session.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    cameraCaptureSession = null;
                    Log.d(TAG, "capture session failed.");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void setImageFormat(int imageFormat) {
        imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), imageFormat, 1);
        imageReader.setOnImageAvailableListener(imageAvailableListener, handler);
    }

    //最好在 onPause 中调用，如果在 onDestroy 中调用，CameraDevice 会优先被系统关闭
    //此时 session.close()　会抛状态异常
    public void release() {

        if (cameraCaptureSession != null) {
            if (cameraDevice != null) {
                cameraCaptureSession.close();
            }
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }

        handler = null;
    }

    public long getFrameSize() {
        return largest.getWidth() * largest.getHeight();
    }

    public Size getSize() {
        return largest;
    }

}
