package com.zhou.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.zhou.android.common.Tools;

import java.util.ArrayList;
import java.util.List;

/**
 * 计算相对位置点
 * Created by ZhOu on 2017/7/9.
 */

public class PointZoomView extends ImageView {

    private Matrix matrix = new Matrix();

    private float imageWidth, imageHeight;
    private float startY, endY;

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
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
//        Drawable drawable = getDrawable();
//        if (drawable == null)
//            return;
//        if (drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
//            return;
//
//        drawable.draw(canvas);

        canvas.drawCircle(100, startY, 12f, pointPaint);
        canvas.drawCircle(300, endY, 12f, pointPaint);

        if (points.size() > 0) {
            for (P p : points) {
                canvas.drawCircle(p.x, p.y, 12f, pointPaint);
//                canvas.drawPoint(p.x, p.y, pointPaint);
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
            startY = (Tools.getScreenHeight(getContext()) - imageHeight) / 2
                    + Tools.getStateHeight(getContext())
                    + Tools.getDpi(getContext()) * 48;
            endY = startY + imageHeight;
            Log.d("zhou", "w-h: " + imageWidth + "," + imageHeight);
            Log.d("zhou", "start-: " + startY + "," + endY);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        matrix.set(getMatrix());
        float[] value = new float[9];
        matrix.getValues(value);
        imageWidth = getWidth() / value[Matrix.MSCALE_X];
        imageHeight = (getHeight() - value[Matrix.MTRANS_Y] * 2) / value[Matrix.MSCALE_Y];

        startY = (getHeight() - imageHeight) / 2;
        endY = startY + imageHeight;
        Log.d("zhou", "w-h: " + imageWidth + "," + imageHeight);
        Log.d("zhou", "start-: " + startY + "," + endY);
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
                        Log.d("zhou", "point: " + x + "," + y);
                        Log.d("zhou", "point2: " + event.getRawX() + "," + event.getRawY());
                        invalidate();
                    }
                    break;
            }
            return true;
        }
    }

}

class P {
    float x = 0, y = 0;

    P(float x, float y) {
        this.x = x;
        this.y = y;
    }
}


