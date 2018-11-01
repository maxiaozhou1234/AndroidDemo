package com.zhou.android.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.ToastUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * 双摄（Camera2 API）
 * Created by Administrator on 2018/10/10.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DoubleNewCameraActivity extends BaseActivity {

    private final static String FRONT = "camera_front";
    private final static String BACK = "camera_back";

    private CameraManager cameraManager;
    private TextureView textureBack, textureFront;
//    private Handler backgroundHandler, frontHandler;

    private HashMap<String, CameraConfig> map = new HashMap<>();
    private boolean isBackCamera = true;

    private ImageReader imageReader;
    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            Log.d("image", "size [ w: " + image.getWidth() + " h: " + image.getHeight() + " ]");
        }
    };

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_double_new_camera);
    }

    @Override
    protected void init() {
        textureBack = findViewById(R.id.texture_back);
        textureFront = findViewById(R.id.texture_front);

        Handler backgroundHandler = new Handler();
        Handler frontHandler = new Handler();

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer cameraOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraOrientation == null)
                    continue;
                if (cameraOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    map.put(FRONT, new CameraConfig(id, characteristics, deviceFrontStateCallback, frontHandler));
                } else if (cameraOrientation == CameraCharacteristics.LENS_FACING_BACK) {
//                    if (map.containsKey(BACK)) {
//                        map.put(FRONT, new CameraConfig(id, characteristics, deviceFrontStateCallback, frontHandler));
//                    } else {
                    map.put(BACK, new CameraConfig(id, characteristics, deviceBackStateCallback, backgroundHandler));
//                }
                    StreamConfigurationMap size = characteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (size != null) {
                        Size largest = Collections.max(Arrays.asList(size.getOutputSizes(ImageFormat.JPEG)),
                                new Comparator<Size>() {
                                    @Override
                                    public int compare(Size lhs, Size rhs) {
                                        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                                                (long) rhs.getWidth() * rhs.getHeight());
                                    }
                                });
                        imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
                        imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler);
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void addListener() {
        findViewById(R.id.ib_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCamera(map.get(isBackCamera ? BACK : FRONT));
                isBackCamera = !isBackCamera;
                if (isBackCamera) {
                    if (textureBack.isAvailable()) {
                        openCamera(map.get(isBackCamera ? BACK : FRONT));
                    } else {
                        textureBack.setSurfaceTextureListener(textureListenerBack);
                    }
                } else {
                    if (textureFront.isAvailable()) {
                        openCamera(map.get(FRONT));
                    } else {
                        textureFront.setSurfaceTextureListener(textureListenerFront);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isBackCamera) {
            if (textureBack.isAvailable()) {
//            openCamera(map.get(isBackCamera ? BACK : FRONT));
                openCamera(map.get(BACK));
            } else {
                textureBack.setSurfaceTextureListener(textureListenerBack);
            }
        } else {
            if (textureFront.isAvailable()) {
                openCamera(map.get(FRONT));
            } else {
                textureFront.setSurfaceTextureListener(textureListenerFront);
            }
        }
    }

    private TextureView.SurfaceTextureListener textureListenerFront = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//            openCamera(map.get(isBackCamera ? BACK : FRONT));
            openCamera(map.get(FRONT));
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
    };

    private TextureView.SurfaceTextureListener textureListenerBack = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(map.get(BACK));
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
    };

    private CameraDevice.StateCallback deviceBackStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            createCameraSession(textureBack, map.get(BACK), camera);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            Log.d("camera", "disconnect");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            Log.d("camera", "error " + error);
        }
    };

    private CameraDevice.StateCallback deviceFrontStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            createCameraSession(textureFront, map.get(FRONT), camera);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d("camera", "disconnect");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d("camera", "error " + error);
        }
    };

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    private void openCamera(CameraConfig config) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ToastUtils.show(this, "This app does not have camera permission");
            return;
        }
        if (config == null) {
            Log.d("camera", "CameraConfig is null.");
            return;
        }
        String cameraId = config.cameraId;
        if (cameraManager == null) {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        }
        try {
            cameraManager.openCamera(cameraId, config.deviceStateCallback, config.handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraSession(TextureView textureView, final CameraConfig config, CameraDevice cameraDevice) {

        Surface surface = new Surface(textureView.getSurfaceTexture());
        try {
            if (config != null) {
                config.cameraDevice = cameraDevice;
            }
            final CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            requestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {

                    if (null != config) {
                        config.cameraCaptureSession = session;
                        requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                        try {
                            session.setRepeatingRequest(requestBuilder.build(), captureCallback, config.handler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.d("camera", "config failed.");
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera(CameraConfig config) {
        if (config != null) {
            if (config.cameraCaptureSession != null) {
                config.cameraCaptureSession.close();
                config.cameraCaptureSession = null;
            }
            if (config.cameraDevice != null) {
                config.cameraDevice.close();
                config.cameraDevice = null;
            }
        }
    }

    private class CameraConfig {

        String cameraId;
        int orientation;
        boolean fullSupport = false;
        CameraDevice cameraDevice;
        CameraDevice.StateCallback deviceStateCallback;
        CameraCaptureSession cameraCaptureSession;

        Handler handler;

        CameraConfig(String cameraId, CameraCharacteristics characteristics, @NonNull CameraDevice.StateCallback deviceStateCallback, @NonNull Handler handler) {
            this.cameraId = cameraId;
            Integer orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            this.orientation = orientation == null ? 0 : orientation;
            Integer level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            fullSupport = level != null && level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL;
            this.deviceStateCallback = deviceStateCallback;
            this.handler = handler;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCamera(map.get(isBackCamera ? BACK : FRONT));
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }
}
