package com.zhou.android.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by ZhOu on 2017/2/8.
 */

public class RotateView extends SurfaceView implements SurfaceHolder.Callback {

    private Canvas mCanvas;

    private SurfaceHolder holder;

    private Thread thread;

    private boolean isRunning = false;

    private Paint mPaint;

    private float angle = 30f;

    public RotateView(Context context) {
        this(context, null);
    }

    public RotateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        holder = getHolder();
        holder.addCallback(this);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        thread = new Thread(mRunnable);
        thread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                draw();
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void draw() {
        try {
            mCanvas = holder.lockCanvas();
            if (mCanvas != null) {
                mCanvas.drawArc(200, 200, 1000, 800, 0, angle, true, mPaint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                holder.unlockCanvasAndPost(mCanvas);
            }
        }
    }
}
