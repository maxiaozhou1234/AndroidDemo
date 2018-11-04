package com.zhou.android.opengl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLFrameRenderer implements Renderer {

    private String TAG = "open_gl";

    private GLSurfaceView mTargetSurface;
    private GLProgram prog = new GLProgram(0);
    //    private int width, height;//控件尺寸
    private int outputWidth, outputHeight;
    private ByteBuffer y;
    private ByteBuffer u;
    private ByteBuffer v;

    private boolean isInit = false;

    public GLFrameRenderer(GLSurfaceView surface) {
        mTargetSurface = surface;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "GLFrameRenderer :: onSurfaceCreated");
        if (!prog.isProgramBuilt()) {
            prog.buildProgram();
            Log.d(TAG, "GLFrameRenderer :: buildProgram done");
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "GLFrameRenderer :: onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (this) {
            if (isInit) {
                // reset position, have to be done
                y.position(0);
                u.position(0);
                v.position(0);
                prog.buildTextures(y, u, v, outputWidth, outputHeight);
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                prog.drawFrame();
            }
        }
    }

    /**
     * this method will be called from native code, it happens when the video is about to play or
     * the video size changes.
     */
    public void update(int viewWidth, int viewHeight, int outputWidth, int outputHeight) {
        Log.d(TAG, "INIT E");
        isInit = true;
        if (outputWidth > 0 && outputHeight > 0) {
            // 调整比例
            if (viewWidth > 0 && viewHeight > 0) {
                float f1 = 1f * viewHeight / viewWidth;
                float f2 = 1f * outputHeight / outputWidth;
                if (f1 == f2) {
                    prog.createBuffers(GLProgram.squareVertices);
                } else if (f1 < f2) {
                    float widScale = f1 / f2;
                    prog.createBuffers(new float[]{-widScale, -1.0f, widScale, -1.0f, -widScale, 1.0f, widScale,
                            1.0f,});
                } else {
                    float heightScale = f2 / f1;
                    prog.createBuffers(new float[]{-1.0f, -heightScale, 1.0f, -heightScale, -1.0f, heightScale, 1.0f,
                            heightScale,});
                }
            }

            // 初始化容器
            if (outputWidth != this.outputWidth && outputHeight != this.outputHeight) {
                this.outputWidth = outputWidth;
                this.outputHeight = outputHeight;
                int yarraySize = outputWidth * outputHeight;
                int uvarraySize = yarraySize / 4;
                synchronized (this) {
                    y = ByteBuffer.allocate(yarraySize);
                    u = ByteBuffer.allocate(uvarraySize);
                    v = ByteBuffer.allocate(uvarraySize);
                }
            }
        }
        Log.d(TAG, "INIT X");
    }

    /**
     * this method will be called from native code, it's used for passing yuv data to me.
     */
    public void update(byte[] ydata, byte[] udata, byte[] vdata) {
        if (y != null) {
            synchronized (this) {
                y.clear();
                u.clear();
                v.clear();
                y.put(ydata, 0, ydata.length);
                u.put(udata, 0, udata.length);
                v.put(vdata, 0, vdata.length);
            }

            // request to render
            mTargetSurface.requestRender();
        }
    }

    /**
     * this method will be called from native code, it's used for passing play state to activity.
     */
    public void updateState(int state) {
        Log.d(TAG, "updateState E = " + state);
        Log.d(TAG, "updateState X");
    }

    public boolean isInit() {
        return isInit;
    }
}
