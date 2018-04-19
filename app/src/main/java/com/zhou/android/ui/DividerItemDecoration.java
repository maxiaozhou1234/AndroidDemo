package com.zhou.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zhou.android.common.Tools;

/**
 * 分割线
 * Created by ZhOu on 2017/10/31.
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Paint paint;

    public DividerItemDecoration(Context context) {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(0xffffffff);
        paint.setStrokeWidth(Tools.dip2px(context, 4));
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void setStrokeWidth(int width) {
        paint.setStrokeWidth(width);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        drawVertical(c, parent);
        drawHorizontal(c, parent);
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            int top = view.getTop() - lp.topMargin;
            int bottom = view.getBottom() + lp.topMargin + lp.bottomMargin;
            int x = view.getRight() + lp.rightMargin;
            c.drawLine(x, top, x, bottom, paint);
        }
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            int left = view.getLeft() - lp.leftMargin;
            int right = view.getRight() + lp.rightMargin;
            int y = view.getBottom() + lp.topMargin + lp.bottomMargin / 2;
            c.drawLine(left, y, right, y, paint);
        }
    }

}
