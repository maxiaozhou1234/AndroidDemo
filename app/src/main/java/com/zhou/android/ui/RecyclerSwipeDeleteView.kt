package com.zhou.android.ui

import android.content.Context
import android.graphics.Point
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

/**
 * Created by mxz on 2020/8/13.
 */
class RecyclerSwipeDeleteView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyle: Int = 0)
    : FrameLayout(context, attributeSet, defStyle) {

    private lateinit var itemView: View
    private lateinit var deleteView: View

    private val point = Point()

    private val viewDragHelper = ViewDragHelper.create(this, 1.0f, Callback())

    override fun onFinishInflate() {
        super.onFinishInflate()
        deleteView = getChildAt(0)
        itemView = getChildAt(1)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        point.x = itemView.left
        point.y = itemView.top
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val shouldIntercepted = viewDragHelper.shouldInterceptTouchEvent(event)
        var flag = false
        if (!shouldIntercepted) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                flag = itemView.x >= 0
                if (flag) {
                    viewDragHelper.captureChildView(itemView, event.getPointerId(0))
                }
            }
        }
        return shouldIntercepted || flag
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        viewDragHelper.processTouchEvent(event)
        return true
    }

    override fun computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            invalidate()
        }
    }

    fun reset() {

        viewDragHelper.smoothSlideViewTo(itemView, paddingLeft, point.y)
        postInvalidate()
    }

    inner class Callback : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child == itemView
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            point.x = left
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return point.y
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {

            val leftBound = -deleteView.width
            val rightBound = paddingRight
            return (leftBound).coerceAtLeast(left).coerceAtMost(rightBound)//Math.min(Math.max(left, leftBound), rightBound)
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            if (-itemView.x > deleteView.width / 2) {//展开
                viewDragHelper.settleCapturedViewAt(-deleteView.width, point.y)
            } else {
                viewDragHelper.settleCapturedViewAt(paddingLeft, point.y)
            }
            invalidate()
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return width - child.measuredWidth
        }

    }
}