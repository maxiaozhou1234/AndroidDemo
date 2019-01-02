package com.zhou.android.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class CardStackLayout extends ViewGroup {

    private int scrollY = 0;

    public CardStackLayout(@NonNull Context context) {
        this(context, null);
    }

    public CardStackLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardStackLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                        MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;

        int maxWidth = 0;
        int maxHeight = 0;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
            maxHeight += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        }
        if (measureMatchParentChildren) {
            setMeasuredDimension(maxWidth, maxHeight);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int count = getChildCount();
//        final int parentLeft = getPaddingLeft();
//        final int parentRight = getPaddingRight();

        int layoutTop = top;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            left += lp.leftMargin;
            layoutTop += lp.topMargin;
            if (i == 0) {
                int _l = scrollY != 0 ? 30 : 0;
                child.layout(left + _l, layoutTop, left + width + _l, layoutTop + height);
            } else {
                child.layout(left, layoutTop - scrollY, left + width, layoutTop + height);
            }
            layoutTop += height;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
        requestLayout();
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
}
