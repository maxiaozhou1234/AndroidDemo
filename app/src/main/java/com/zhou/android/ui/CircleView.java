package com.zhou.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by ZhOu on 2017/2/8.
 */

public class CircleView extends SurfaceView implements SurfaceHolder.Callback {

    private static String TAG = "zhou";

    private Canvas mCanvas;

    private SurfaceHolder holder;

    private Thread thread;

    private boolean isRunning = false;

    private Paint mPaint;

    private RectF rectF;

    private float startAngle = 0f, sweepAngle = 10f;

    private int sleepTime = 600;

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Log.d(TAG, "RotateView Created");
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(20);

        rectF = new RectF();
        rectF.top = 200;
        rectF.left = 200;
        rectF.right = 1000;
        rectF.bottom = 1000;

        holder = getHolder();
        holder.addCallback(this);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Surface Created");

        isRunning = true;
        thread = new Thread(mRunnable);
        thread.start();
        Log.d(TAG, "thread is running");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
        Log.d(TAG, "Surface destroy");
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                if (startAngle == 0 && sweepAngle < 360)
                    drawArc();
                else
                    clearArc();
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void drawArc() {
        try {
            mCanvas = holder.lockCanvas();
            if (mCanvas != null) {
                sleepTime = 600;
                mCanvas.drawColor(Color.WHITE);
                mCanvas.drawArc(rectF, 0, sweepAngle, true, mPaint);
                sweepAngle += 10;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                holder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    private void clearArc() {
        try {
            mCanvas = holder.lockCanvas();
            if (mCanvas != null) {
                startAngle += 6;
                sleepTime = 100;
                mCanvas.drawColor(Color.WHITE);
                mCanvas.drawArc(rectF, startAngle, 360 - startAngle, true, mPaint);
                if (startAngle > 355) {
                    startAngle = 0;
                    sweepAngle = 10f;
                }
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
