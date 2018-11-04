package com.zhou.android.gl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 渲染器
 * Created by Administrator on 2018/11/1.
 */

public class GLRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private Context context;
    private SurfaceTexture surface;
    private int textureID = -1;
    private DirectDrawer directDrawer;

    public GLRender(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        textureID = GlUtil.createTextureID();
        surface = new SurfaceTexture(textureID);
        surface.setOnFrameAvailableListener(this);

        directDrawer = new DirectDrawer(context, textureID);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 设置白色为清屏
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        // 清除屏幕和深度缓存
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // 更新纹理
        surface.updateTexImage();

        directDrawer.draw();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (listener != null) {
            listener.onFrameAvailable(surfaceTexture);
        }
    }

    private SurfaceTexture.OnFrameAvailableListener listener = null;

    public void setOnFrameAvailableListener(SurfaceTexture.OnFrameAvailableListener listener) {
        this.listener = listener;
    }
}
