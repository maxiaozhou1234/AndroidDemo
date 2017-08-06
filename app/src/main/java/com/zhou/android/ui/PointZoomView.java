package com.zhou.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 计算相对位置点
 * Created by ZhOu on 2017/7/9.
 */

public class PointZoomView extends ImageView {

    private float imageWidth = 0f, imageHeight = 0f;
    private float viewWidth = 0f, viewHeight = 0f;
    private float startY = 0f, endY = 0f;
    private float scale = 1f;//真实文件大小和控件的大小的比例

    private Paint pointPaint;

    private List<P> points = new ArrayList<>();

    public PointZoomView(Context context) {
        this(context, null);
    }

    public PointZoomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PointZoomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setStrokeWidth(5f);
        pointPaint.setColor(Color.RED);

        setOnTouchListener(new L());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(500, startY, 500, endY, pointPaint);

        if (points.size() > 0) {
            for (P p : points) {
                canvas.drawCircle(p.x, p.y, 12f, pointPaint);
            }
        }
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
//        matrix.set(getMatrix());
//        float[] value = new float[9];
//        matrix.getValues(value);
//        imageWidth = getWidth() / value[Matrix.MSCALE_X];
//        imageHeight = (getHeight() - value[Matrix.MTRANS_Y] * 2) / value[Matrix.MSCALE_Y];
        if (drawable != null) {
            imageWidth = drawable.getIntrinsicWidth();
            imageHeight = drawable.getIntrinsicHeight();
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
//        matrix.set(getMatrix());
//        float[] value = new float[9];
//        matrix.getValues(value);
//        imageWidth = getWidth() / value[Matrix.MSCALE_X];
//        imageHeight = (getHeight() - value[Matrix.MTRANS_Y] * 2) / value[Matrix.MSCALE_Y];
        if (bm == null || bm.isRecycled())
            return;
        imageWidth = bm.getWidth();
        imageHeight = bm.getHeight();

        float _w = viewWidth / imageWidth;
        float _h = viewHeight / imageHeight;
        scale = (_w < _h ? _w : _h);
//        scale = scale > 0 ? scale : 1f;

        startY = (viewHeight - imageHeight * scale) / 2;
        endY = startY + imageHeight * scale;
        Log.d("zhou", "image:w-h: " + imageWidth + "," + imageHeight);
        Log.d("zhou", "view:w-h: " + viewWidth + "," + viewHeight);
        Log.d("zhou", "start-: " + startY + "," + endY + " --" + scale);
    }


    class L implements View.OnTouchListener {
        private long downTime = 0l;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    downTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_UP:
                    if (500 > (System.currentTimeMillis() - downTime)) {
                        float x = event.getX();
                        float y = event.getY();
                        points.add(new P(x, y));
                        if (listener != null) {
                            if (y < startY || y > endY)
                                listener.outRoom();
                            else
                                listener.getPercent(x / viewWidth, (y - startY) / (viewHeight - 2 * startY));
                        }
                        invalidate();
                    }
                    break;
            }
            return true;
        }
    }


    class P {
        float x = 0, y = 0;

        P(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private PercentListener listener;

    public void setListener(PercentListener listener) {
        this.listener = listener;
    }

    public interface PercentListener {
        void getPercent(float x, float y);

        void outRoom();
    }
}


