package com.zhou.android.gl;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;

/**
 * open GL
 * Created by Administrator on 2018/11/1.
 */

public class OpenGlActivity extends BaseActivity implements SurfaceTexture.OnFrameAvailableListener {

    private GLSurfaceView glSurface;
    private GLRender glRender;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_open_gl);
    }

    @Override
    protected void init() {

        glSurface = findViewById(R.id.glSurface);

        glRender = new GLRender(this);

        glSurface.setEGLContextClientVersion(2);
        glSurface.setRenderer(glRender);
        //RENDERMODE_WHEN_DIRTY 有通知才重绘
        //RENDERMODE_CONTINUOUSLY 连续绘制
        glSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void addListener() {
        glRender.setOnFrameAvailableListener(this);

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glSurface.requestRender();
    }
}
