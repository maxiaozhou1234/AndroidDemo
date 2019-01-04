package com.zhou.android.ui;

import android.content.Context;
import android.nfc.TagLostException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class CardStackLayout extends ViewGroup {

    private final static String TAG = "scroll";


    private float downY, lastMotionY;
    private int pointerId = -1;
    private boolean isDragging = false;

    private int dexY = 0;
    private boolean canScroll = true;
    private int touchSlop;

    private int limitOffset = 0, targetCurrentOffset = 0, targetEndOffset = 0;
    private View target = null;

    public CardStackLayout(@NonNull Context context) {
        this(context, null);
    }

    public CardStackLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardStackLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ViewConfiguration vc = ViewConfiguration.get(getContext());
        touchSlop = vc.getScaledTouchSlop();

        Log.d(TAG, "touchSlop = " + touchSlop);
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
//            if (i == 0) {
//                int _l = dexY != 0 ? 30 : 0;
//                child.layout(left + _l, layoutTop, left + width - _l, layoutTop + height);
//            } else {
//                child.layout(left, layoutTop - dexY, left + width, layoutTop + height);
//            }
            child.layout(left + lp.leftMargin, layoutTop, left + width - lp.rightMargin, layoutTop + height);
            layoutTop += height;
        }
        if (count > 1) {
            target = getChildAt(count - 1);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled() || childCanScroll()) {
            return false;
        }
        int action = event.getAction();
        int pointIndex;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "action down");
                pointerId = event.getPointerId(0);
                pointIndex = event.findPointerIndex(pointerId);
                if (pointIndex < 0) {
                    return false;
                }
                isDragging = false;
                downY = event.getY(pointIndex);
//                return true;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "action move");
                pointIndex = event.findPointerIndex(pointerId);
                if (pointIndex < 0) {
                    return false;
                }
                float y = event.getY(pointIndex);
                Log.d(TAG, "y = " + y);
                checkScrollBound(y);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pointerId = -1;
                isDragging = false;
                break;
        }

        Log.d(TAG, "status = " + isDragging);
        return isDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled() || childCanScroll()) {
            return false;
        }

        int action = event.getAction();
        int pointIndex;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "touch action down");
                pointerId = event.getPointerId(0);
                pointIndex = event.findPointerIndex(pointerId);
                if (pointIndex < 0) {
                    return false;
                }
                isDragging = false;
                downY = event.getY(pointIndex);
                return true;
//                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "touch action move");
                pointIndex = event.findPointerIndex(pointerId);
                if (pointIndex < 0) {
                    return false;
                }
                float y = event.getY(pointIndex);
                checkScrollBound(y);
                if (isDragging) {
                    //处理
                    float dy = y - lastMotionY;
                    moveAllView(dy);
                    if (dy < 0 && targetCurrentOffset + dy <= targetEndOffset) {//父布局到顶了，事件重写下发
                        //重新下发，必须先触发down才能使move被子view拦截到
                        Log.d(TAG, "dispatch action down");
                        int tmp = event.getAction();
                        event.setAction(MotionEvent.ACTION_DOWN);
                        dispatchTouchEvent(event);
                        event.setAction(tmp);
                    }
                    lastMotionY = y;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pointerId = -1;
                isDragging = false;
                break;
        }
        Log.d(TAG, "touch status = " + isDragging);
        return isDragging;
    }

    public void setTargetOffset(int offset) {
        this.limitOffset = this.targetCurrentOffset = offset;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    //处理双指触摸，更新id
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pId = ev.getPointerId(pointerIndex);
        if (pId == pointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            pointerId = ev.getPointerId(newPointerIndex);
        }
    }

    //判断是否符合滑动条件
    private void checkScrollBound(float y) {
        if (y > downY || targetCurrentOffset > targetEndOffset) {
            float dy = Math.abs(y - downY);
            if (dy > touchSlop && !isDragging) {//滑动判定
                lastMotionY = downY + touchSlop;
                isDragging = true;
            }
        }
    }

    private boolean childCanScroll() {
        return false;
    }

    private void moveAllView(float dy) {//偏移量
        int _target = (int) (targetCurrentOffset + dy);
        _target = Math.max(_target, targetEndOffset);
        ViewCompat.offsetTopAndBottom(target, _target - targetCurrentOffset);
        targetCurrentOffset = _target;
        Log.d(TAG, "update view,offset = " + dy + " current = " + targetCurrentOffset);
    }

    public void testCompat(int dis) {
        ViewCompat.offsetTopAndBottom(target, dis);
    }

}
