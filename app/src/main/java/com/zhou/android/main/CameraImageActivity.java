package com.zhou.android.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.Utils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import io.reactivex.disposables.Disposable;

/**
 * 获取预览数据
 * Created by Administrator on 2018/10/29.
 */
@RequiresApi(api = 21)
public class CameraImageActivity extends BaseActivity {

    private final static String TAG = "camera-aty";

    private CameraManager cameraManager;

    private HandlerThread handlerThread;
    private Handler handler;
    private ImageReader imageReader;
    private TextureView textureView;
    private SurfaceView surfaceView;

    private String cameraId;
    private Size largest;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;

    private Disposable disposable;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_camera_image);
    }

    @Override
    protected void init() {

        textureView = findViewById(R.id.textureView);
        surfaceView = findViewById(R.id.surfaceView);

        handlerThread = new HandlerThread("back");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer cameraOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraOrientation == null)
                    continue;
                if (cameraOrientation == CameraCharacteristics.LENS_FACING_BACK) {

                    cameraId = id;

                    StreamConfigurationMap size = characteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (size != null) {
                        largest = Collections.max(Arrays.asList(size.getOutputSizes(ImageFormat.JPEG)),
                                new Comparator<Size>() {
                                    @Override
                                    public int compare(Size lhs, Size rhs) {
                                        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                                                (long) rhs.getWidth() * rhs.getHeight());
                                    }
                                });
                        imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.YUV_420_888, 1);
                        imageReader.setOnImageAvailableListener(onImageAvailableListener, handler);
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        disposable = Utils.checkPermission(this, Manifest.permission.CAMERA, "相机");

    }

    @Override
    protected void addListener() {

    }

    private ByteBuffer buffer;
    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            Image image = reader.acquireLatestImage();
            //我们可以将这帧数据转成字节数组，类似于Camera1的PreviewCallback回调的预览帧数据
//            int len = image.getPlanes().length;
//            if (buffer == null) {
//                buffer = ByteBuffer.allocate(reader.getWidth() * reader.getHeight());
//            }
//            for (int i = 0; i < len; i++) {
//                ByteBuffer _buffer = image.getPlanes()[i].getBuffer();
//                byte[] data = new byte[_buffer.remaining()];
//                _buffer.get(data);
//                buffer.put(data);
//            }
//            buffer.flip();
//            byte[] data = buffer.array();
//            buffer.flip();
//            buffer.clear();
//
//            byte[] nv21Data = new byte[data.length];
//            YuvUtil.yuvI420ToNV21(data, nv21Data, reader.getWidth(), reader.getHeight());
//
//            //这里采用yuvImage将yuvi420转化为图片，当然用libyuv也是可以做到的，这里主要介绍libyuv的裁剪，旋转，缩放，镜像的操作
//            YuvImage yuvImage = new YuvImage(nv21Data, ImageFormat.NV21, reader.getWidth(), reader.getHeight(), null);
//            ByteArrayOutputStream fOut = new ByteArrayOutputStream();
//            yuvImage.compressToJpeg(new Rect(0, 0, reader.getWidth(), reader.getHeight()), 100, fOut);
//            byte[] bytes = fOut.toByteArray();

            try {
//                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                byte[] bytes = new byte[buffer.remaining()];
//                buffer.get(bytes);
//                show(bytes);
//                Log.d(TAG, "format = " + reader.getImageFormat() + ", planes length =  " + image.getPlanes().length);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                image.close();
            }
        }
    };

    private CameraDevice.StateCallback deviceBackStateCallback = new CameraDevice.StateCallback() {
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
            Log.e(TAG, "ERROR = " + error);
        }
    };

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
//            session.getDevice().
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    private void openCamera() {
        if (cameraManager == null) {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        }
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraManager.openCamera(cameraId, deviceBackStateCallback, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            try {
                cameraCaptureSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
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
    }

    private void createCameraSession() {

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface surface1 = surfaceView.getHolder().getSurface();
            captureRequestBuilder.addTarget(surface1);

            Surface surface2 = imageReader.getSurface();
            captureRequestBuilder.addTarget(surface2);

            cameraDevice.createCaptureSession(Arrays.asList(surface1, surface2), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSession = session;

                    try {
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, handler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
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

    @Override
    protected void onResume() {
        super.onResume();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
        closeCamera();
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Handler pic;
    private boolean flag = true;

    private void show(byte[] bytes) {
        if (pic == null) {
            pic = new Handler(handlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (flag && msg.what == 100) {
                        flag = false;
                        byte[] bytes = (byte[]) msg.obj;
                        Canvas canvas = textureView.lockCanvas();
                        if (canvas != null) {
                            canvas.drawBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), 0, 0, null);
                        }
                        textureView.unlockCanvasAndPost(canvas);
                        flag = true;
                    }
                }
            };
        }
        byte[] _bytes = new byte[bytes.length];
        System.arraycopy(bytes, 0, _bytes, 0, bytes.length);

        Message msg = pic.obtainMessage(100);
        msg.obj = _bytes;
        msg.sendToTarget();
    }

}
