package com.zhou.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 圆形 ImageView
 * Created by ZhOu on 2018/8/1.
 */

public class CircleImageView extends ImageView {

    private Paint paint;
//    private int size = 0;

    private Path path;
    private RectF rectF = null;
    private PaintFlagsDrawFilter paintFlagsDrawFilter;

    private float r = 0;
    private int padding = 10;

    public CircleImageView(Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        Xfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
//        paint.setXfermode(xfermode);
//        paint.setStrokeWidth(50);
//        paint.setColor(0x000000);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF303F9F);
        paint.setStrokeWidth(50);

        path = new Path();
        paintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
//        setLayerType(View.LAYER_TYPE_HARDWARE, null);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        path.reset();
        path.addRoundRect(rectF, r, r, Path.Direction.CW);
        canvas.setDrawFilter(paintFlagsDrawFilter);
        canvas.save();
        canvas.clipPath(path);
        canvas.drawColor(0xffc3c3c3);
        super.onDraw(canvas);
        canvas.drawCircle(r + 5, r + 5, r + 5, paint);
        canvas.restore();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width < height ? width : height;
        setMeasuredDimension(size, size);
        if (rectF == null) {
            r = (size - padding) / 2;
            int p = padding / 2;
            rectF = new RectF(p, p, size - p, size - p);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int p = padding / 2;
        int s = w < h ? w : h;
        if (p > s) {
            s = 2 * p;
        }
        r = (s - padding) / 2;
        rectF = new RectF(p, p, s - p, s - p);
    }
}
