package com.zhou.android.camera2;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

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
    private final static int TYPE_TEXTURE_VIEW = 0;
    private final static int TYPE_SURFACE_VIEW = 1;
    private String[] _error = {"ERROR_CAMERA_IN_USE", "ERROR_MAX_CAMERAS_IN_USE", "ERROR_CAMERA_DISABLED", "ERROR_CAMERA_DEVICE", "ERROR_CAMERA_SERVICE"};

    String cameraId;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    CameraDevice.StateCallback cameraStateCallback;

    private int type = -1;

    private Handler handler;
    //     TextureView textureView;
    private ImageReader imageReader;
    private View view;
    private OnImageAvailableListener imageAvailableListener;
    private Size largest;

    private StreamConfigurationMap streamConfigurationMap;

    public CameraConfig(String cameraId, StreamConfigurationMap map, @Nullable View view, OnImageAvailableListener listener, Handler handler) {
        if (view != null) {
            this.view = view;
            if (view instanceof TextureView) {
                type = TYPE_TEXTURE_VIEW;
            } else if (view instanceof SurfaceView) {
                type = TYPE_SURFACE_VIEW;
            } else {
                throw new IllegalArgumentException("不支持类型");
            }
        }

        this.streamConfigurationMap = map;
        this.cameraId = cameraId;
        this.imageAvailableListener = listener;
        this.handler = handler;

        int format = ImageFormat.JPEG;
        if (map.isOutputSupportedFor(ImageFormat.YUV_420_888)) {
            format = ImageFormat.YUV_420_888;
            Log.i(TAG, "support YUV_420_888");
        } else if (map.isOutputSupportedFor(ImageFormat.YV12)) {
            format = ImageFormat.YV12;
        }
        Log.e(TAG, "current ImageFormat = " + format);
        largest = calculationSize(map);
        Log.d(TAG, "width = " + largest.getWidth() + " height = " + largest.getHeight());
        //三通道 YUV  YV12,YUV_420_888,不支持 NV21
        imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), format, 1);
        imageReader.setOnImageAvailableListener(imageAvailableListener, handler);

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
    }

    private void createCameraSession() {

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            List<Surface> output = new ArrayList<>(view == null ? 1 : 2);
            output.add(imageReader.getSurface());
            captureRequestBuilder.addTarget(imageReader.getSurface());

            Surface surface = getSurface();
            if (surface != null && surface.isValid()) {
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
        } catch (CameraAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void setImageFormat(int imageFormat) {
        if (streamConfigurationMap.isOutputSupportedFor(imageFormat)) {

            imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), imageFormat, 1);
            imageReader.setOnImageAvailableListener(imageAvailableListener, handler);
        } else {
            Log.w(TAG, "ImageFormat <" + imageFormat + "> is not support.");
        }
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

    public void onPause() {
        if (cameraCaptureSession != null) {
            try {
                cameraCaptureSession.stopRepeating();
                cameraCaptureSession.close();
                cameraCaptureSession = null;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    public Size getSize() {
        return largest;
    }

    private Surface getSurface() {
        if (type == TYPE_TEXTURE_VIEW) {
            SurfaceTexture surfaceTexture = ((TextureView) view).getSurfaceTexture();
            assert surfaceTexture != null;
            return new Surface(surfaceTexture);
        } else if (type == TYPE_SURFACE_VIEW) {
            return ((SurfaceView) view).getHolder().getSurface();
        }
        return null;
    }

    public void startPreview() {
        if (callback == null)
            return;
        if (view != null) {
            if (type == TYPE_TEXTURE_VIEW) {
                final TextureView textureView = (TextureView) view;
                if (textureView.isAvailable()) {
                    callback.onSurfaceTextureAvailable(this);
                } else {
                    textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                        @Override
                        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                            callback.onSurfaceTextureAvailable(CameraConfig.this);
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
            } else if (type == TYPE_SURFACE_VIEW) {
                if (((SurfaceView) view).getHolder().getSurface().isValid()) {
                    callback.onSurfaceTextureAvailable(CameraConfig.this);
                } else {
                    ((SurfaceView) view).getHolder().addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(SurfaceHolder holder) {
                            callback.onSurfaceTextureAvailable(CameraConfig.this);
                        }

                        @Override
                        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                        }

                        @Override
                        public void surfaceDestroyed(SurfaceHolder holder) {

                        }
                    });
                }
            }
        } else {
            callback.onSurfaceTextureAvailable(this);
        }
    }

    private SurfaceCallback callback;

    public void setSurfaceCallback(SurfaceCallback callback) {
        this.callback = callback;
    }

    public interface SurfaceCallback {

        void onSurfaceTextureAvailable(CameraConfig config);
    }

    private Size calculationSize(StreamConfigurationMap map) {
        if (map != null) {
            return Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new Comparator<Size>() {
                        @Override
                        public int compare(Size lhs, Size rhs) {
//                            return Long.signum((long) rhs.getWidth() * rhs.getHeight() -
//                                    (long) lhs.getWidth() * lhs.getHeight());
                            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                                    (long) rhs.getWidth() * rhs.getHeight());
                        }
                    });
        }
        return null;
    }

    public View getPreviewView() {
        return view;
    }

}
