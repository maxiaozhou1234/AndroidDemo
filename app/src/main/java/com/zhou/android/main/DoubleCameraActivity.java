package com.zhou.android.main;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.ToastUtils;
import com.zhou.android.common.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * 双摄同时显示（使用 Camera API）
 * 经验证开启摄像头数目被系统限制，默认只开启一个摄像头
 * 需修改系统来支持
 * <p>
 * 源码位置：hardware\你的系统\camera\CameraHal\CameraHal_Module.h
 * <p>
 * #define CAMERAS_SUPPORT_MAX             2 支持摄像头个数
 * #define CAMERAS_SUPPORTED_SIMUL_MAX     1 支持使用最大数目
 * <p>
 * 在 CameraHal_Module.cpp 文件中使用地方
 * <p>
 * if(gCamerasOpen >= CAMERAS_SUPPORTED_SIMUL_MAX) {
 * LOGE("maximum number(%d) of cameras already open",gCamerasOpen);
 * rv = -EUSERS;
 * goto fail;
 * }
 * <p>
 * Created by Administrator on 2018/10/10.
 */

public class DoubleCameraActivity extends BaseActivity {

    private final static String FRONT = "camera_front";
    private final static String BACK = "camera_back";

    private SurfaceHolder holderBack, holderFront;

    private HashMap<String, CameraConfig> map = new HashMap<>();
    private List<Camera> cameras = new ArrayList<>();

    private boolean isBack = true;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_double_camera);
    }

    @Override
    protected void init() {
        SurfaceView surfaceBack = findViewById(R.id.surface_back);
        SurfaceView surfaceFront = findViewById(R.id.surface_front);

        holderBack = surfaceBack.getHolder();
        holderFront = surfaceFront.getHolder();

        int count = Camera.getNumberOfCameras();
        for (int i = 0; i < count; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (Camera.CameraInfo.CAMERA_FACING_FRONT == info.facing) {
                int displayRotation = (info.orientation) % 360;
                displayRotation = (360 - displayRotation) % 360;
                map.put(FRONT, new CameraConfig(info.facing, displayRotation, Tools.dip2px(this, 150), Tools.dip2px(this, 200)));
            } else if (Camera.CameraInfo.CAMERA_FACING_BACK == info.facing) {
                int displayRotation = (info.orientation + 360) % 360;
                map.put(BACK, new CameraConfig(info.facing, displayRotation, 0, 0));
            }
        }
    }

    @Override
    protected void addListener() {
        //启动后摄
        holderBack.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                openCamera(map.get(isBack ? BACK : FRONT), holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        //启动前摄
        holderFront.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (isBack) {
                    openCamera(map.get(FRONT), holder);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        //前后摄像头切换
        findViewById(R.id.ib_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBack = !isBack;
                if (cameras.size() > 0) {
                    Camera camera = cameras.remove(0);
                    camera.release();
                    openCamera(map.get(isBack ? BACK : FRONT), holderBack);
                }
            }
        });
    }

    private void openCamera(CameraConfig config, SurfaceHolder holder) {
        if (config == null) {
            Log.e("camera", "camera config is null");
            return;
        }
        try {
            Camera camera = Camera.open(config.cameraId);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewFormat(ImageFormat.YV12);
            Camera.Size optimal = getSupportSize(parameters.getSupportedPreviewSizes(), config.width, config.height);
            if (optimal != null) {
                if (config.width != 0 && config.height != 0 && optimal.width > config.width) {
                    int w = optimal.width / config.width;
                    int h = optimal.height / config.height;
                    int zoom = w > h ? w : h;
                    if (parameters.isZoomSupported()) {
                        parameters.setZoom(zoom);
                    } else {
                        parameters.setPreviewSize(config.width, config.height);
                    }
                } else {
                    parameters.setPreviewSize(optimal.width, optimal.height);
                }
            }
            camera.setParameters(parameters);
            camera.setDisplayOrientation(config.degree);
            cameras.add(camera);
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            ToastUtils.show(this, "open camera failed.");
            e.printStackTrace();
        }
    }

    //获取最大分辨率（如果没有指定大小）
    private Camera.Size getSupportSize(List<Camera.Size> supportSize, int viewWidth, int viewHeight) {
        if (supportSize == null)
            return null;

        Camera.Size optimalSize = null;
        if (viewWidth == 0 || viewHeight == 0) {
            optimalSize = Collections.max(supportSize, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size o1, Camera.Size o2) {
                    if (o1.width > o2.width)
                        return 1;
                    else if (o1.width < o2.width)
                        return -1;
                    else {
                        if (o1.height > o2.height)
                            return 1;
                        else if (o1.height < o2.height)
                            return -1;
                        else
                            return 0;
                    }
                }
            });
        } else {

            double diff = Double.MAX_VALUE;
            for (Camera.Size size : supportSize) {
                if (size.width > viewWidth) {
                    continue;
                }
                if (Math.abs(size.width - viewWidth) < diff) {
                    optimalSize = size;
                    diff = Math.abs(size.width - viewWidth);
                }
            }

            if (optimalSize == null) {
                diff = Double.MAX_VALUE;
                for (Camera.Size size : supportSize) {
                    if (Math.abs(size.width - viewWidth) < diff) {
                        optimalSize = size;
                        diff = Math.abs(size.width - viewWidth);
                    }
                }
            }
        }
        if (optimalSize != null) {
            Log.d("camera", "camera size = [ " + optimalSize.width + " , " + optimalSize.height + " ]");
        } else {
            Log.d("camera", "camera size is null.");
        }
        return optimalSize;
    }

    private class CameraConfig {

        int cameraId;
        int degree;
        int width, height;

        CameraConfig(int cameraId, int degree, int width, int height) {
            this.cameraId = cameraId;
            this.degree = degree;
            this.width = width;
            this.height = height;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameras.size() > 0) {
            for (Camera camera : cameras) {
                camera.release();
            }
            cameras.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameras.size() > 0) {
            for (Camera camera : cameras) {
                camera.release();
            }
        }
    }
}
