package com.zhou.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.zhou.android.R;
import com.zhou.android.common.Tools;

/**
 * 悬浮球
 * Created by ZhOu on 2017/3/23.
 */

public class FloatView extends View {

    private Context context;
    private int size = 50;
    private Paint paint, whitePaint;

    public FloatView(Context context) {
        super(context);
        this.context = context;
        size = Tools.dip2px(context, size);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.colorPrimary));
        whitePaint = new Paint();
        whitePaint.setStyle(Paint.Style.STROKE);
        whitePaint.setStrokeWidth(5);
        whitePaint.setAntiAlias(true);
        whitePaint.setColor(getResources().getColor(R.color.white));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(size / 2, size / 2, size / 2, paint);
        canvas.drawCircle(size / 2, size / 2, size / 2 - 10, whitePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(size, size);
    }
}
