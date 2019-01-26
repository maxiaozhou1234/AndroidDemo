package com.zhou.android.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * 卡片堆叠效果
 */
public class CardStackBehavior extends CoordinatorLayout.Behavior {

    private String TAG = "behavior";

    private int limitOffset = 500, currentOffset = 500, targetOffset = 0;

    public CardStackBehavior() {
    }

    public CardStackBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        //处理位置问题
        int childCount = parent.getChildCount();
        if (childCount > 1) {

            int left = parent.getPaddingLeft();
            int top = parent.getTop() + parent.getPaddingTop();
            for (int i = 0; i < childCount - 1; i++) {
                View _child = parent.getChildAt(i);
                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) _child.getLayoutParams();
                top += _child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                if (i == 0) {
                    limitOffset = currentOffset = top / 2;
                }
            }

            final CoordinatorLayout.LayoutParams lp =
                    (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            limitOffset = currentOffset = currentOffset + lp.topMargin;

            child.layout(left + lp.leftMargin, top + lp.topMargin,
                    parent.getWidth() - parent.getPaddingRight() - lp.rightMargin,
                    parent.getHeight() - parent.getPaddingBottom() - lp.bottomMargin + limitOffset);
            return true;
        } else {
            return super.onLayoutChild(parent, child, layoutDirection);
        }
    }

    @Override
    public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        //处理测量问题，暂跳过
        int height = View.MeasureSpec.getSize(parentHeightMeasureSpec);

        int childCount = parent.getChildCount();
        if (childCount > 1) {
            int maxHeight = parent.getTop() + parent.getPaddingTop();
            for (int i = 0; i < childCount; i++) {
                View _child = parent.getChildAt(i);
                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) _child.getLayoutParams();
                maxHeight += _child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            }

            if (maxHeight > height) {
                CoordinatorLayout.LayoutParams lp =
                        (CoordinatorLayout.LayoutParams) child.getLayoutParams();

                int nWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(),
                        lp.width == ViewGroup.LayoutParams.MATCH_PARENT ? View.MeasureSpec.EXACTLY : lp.width);

                int nHeight = child.getMeasuredHeight() - height + maxHeight;
                int nHeightSpec = View.MeasureSpec.makeMeasureSpec(nHeight, lp.height == ViewGroup.LayoutParams.MATCH_PARENT ?
                        View.MeasureSpec.EXACTLY : lp.height);
                child.measure(nWidthMeasureSpec, nHeightSpec);
                return true;
            }
        }
        return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }


//    @Override
//    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
//        return dependency instanceof RecyclerView;//dependency 监听对象，其实是 CoordinatorLayout，因为要使用 behavior
//    }

//    @Override
//    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
//        //被监听的控件发生变化就会调用这个方法
//        if (deltaY == 0) {
//            deltaY = dependency.getY() - child.getHeight();
//        }
//
//        float dy = dependency.getY() - child.getHeight();
//        dy = dy < 0 ? 0 : dy;
//        float y = -(dy / deltaY) * child.getHeight();
//        Log.d("behavior", "translationY = " + y);
//        child.setTranslationY(y);
//
//        return true;
//    }


    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
                                  @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        Log.i(TAG, "onNestedPreScroll == " + dy);
        if (canScroll(target)) {
            return;
        }
        if (dy > 0) {//手指往上滑动
            //消耗量
            int parentCanConsume = currentOffset - targetOffset;
            if (parentCanConsume > 0) {
                if (dy > parentCanConsume) {
                    consumed[1] = parentCanConsume;
                    moveView(child, -parentCanConsume);
                } else {
                    consumed[1] = dy;
                    moveView(child, -dy);
                }
            }
        }
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
                               @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        Log.i(TAG, "onNestedScroll == " + dyUnconsumed);
        if (dyUnconsumed < 0 && !canScroll(target)) {//手指往下滑动
            if (currentOffset < limitOffset) {
                int dy = currentOffset - dyUnconsumed <= limitOffset ? -dyUnconsumed : limitOffset - currentOffset;
                moveView(child, dy);
            }
        }
    }

    private boolean canScroll(View target) {
        return ViewCompat.canScrollVertically(target, -1);
    }

    private void moveView(View child, int dy) {
        int dis = currentOffset + dy;
        dis = Math.max(dis, targetOffset);
        ViewCompat.offsetTopAndBottom(child, dis - currentOffset);
        currentOffset = dis;
    }

}
