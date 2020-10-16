package com.zhou.android.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import kotlin.math.abs

/**
 * Created by mxz on 2020/10/15.
 */
class SlideMenuLayout @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyle: Int = 0)
    : ViewGroup(context, attributeSet, defStyle) {

    companion object {
        var isTouching = false
        var viewCache: SlideMenuLayout? = null
    }

    private var slideWidth = 0 //菜单总宽度
    private var viewHeight = 0 //view 高度

    private var isLeftSwipe = true //左滑显示菜单

    private var slideDisLimit = 0 //滑动触发最小距离

    private val lastPoint = PointF() //上一次触摸记录
    private val firstPoint = PointF()
    private var firstPointId = 0

    private val scaleTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val maxVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity.toFloat()

    private var velocityTracker: VelocityTracker? = null

    private var isItemExpendButNoMoved = true //菜单打开，但没有滑动操作，有点击，关闭菜单

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        isClickable = true

        slideWidth = 0
        viewHeight = 0
        var contentWidth = 0
        val measureMatchParentChildren = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY
        var isNeedMeasureHeight = false
        for (i in 0.until(childCount)) {
            val child = getChildAt(i)
            child.isClickable = true
            if (child.visibility != View.GONE) {

                measureChild(child, widthMeasureSpec, heightMeasureSpec)
                val lp = child.layoutParams as MarginLayoutParams
                viewHeight = viewHeight.coerceAtLeast(child.measuredHeight + lp.topMargin + lp.bottomMargin)
                if (measureMatchParentChildren && lp.height == LayoutParams.MATCH_PARENT) {
                    isNeedMeasureHeight = true
                }
                if (i > 0) {
                    slideWidth += child.measuredWidth
                } else {
                    contentWidth = child.measuredWidth
                }
            }
        }

        Log.d("zhou", "width = ${paddingLeft + paddingRight + contentWidth},height = ${paddingTop + paddingBottom + viewHeight}")
        setMeasuredDimension(paddingLeft + paddingRight + contentWidth,
                paddingTop + paddingBottom + viewHeight)

        slideDisLimit = slideWidth * 4 / 10
        if (isNeedMeasureHeight) {
            forceUniformHeight(childCount, widthMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var left = paddingLeft
        var right = paddingLeft

        for (i in 0.until(childCount)) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                if (i == 0) {
                    child.layout(left, paddingTop, left + child.measuredWidth, paddingTop + child.measuredHeight)
                    left += child.measuredWidth

                } else {
                    if (isLeftSwipe) {
                        child.layout(left, paddingTop, left + child.measuredWidth, paddingTop + child.measuredHeight)
                        left += child.measuredWidth
                    } else {
                        child.layout(right - child.measuredWidth, paddingTop, right, paddingTop + child.measuredHeight)
                        right -= child.measuredWidth
                    }
                }
            }
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    private fun forceUniformHeight(count: Int, widthMeasureSpec: Int) {
        val uniformMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
        for (i in 0.until(count)) {
            val child = getChildAt(i)
            if (child != null && child.visibility != View.GONE) {
                val lp = child.layoutParams as MarginLayoutParams
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    val oldWidth = lp.width
                    lp.width = child.measuredWidth
                    measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0)
                    lp.width = oldWidth
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        acquireVelocityTracker(ev)
        val tracker = velocityTracker!!

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {

                isItemExpendButNoMoved = true

                if (isTouching) {
                    return false
                } else {
                    isTouching = true
                }
                lastPoint.set(ev.rawX, ev.rawY)
                firstPoint.set(ev.rawX, ev.rawY)

                if (viewCache != null) {
                    if (viewCache != this) {
                        viewCache?.smoothClose()
                    }
                    parent.requestDisallowInterceptTouchEvent(true)
                }

                firstPointId = ev.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {

                val gap = lastPoint.x - ev.rawX
                if (abs(gap) > 10 || abs(scrollX) > 10) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }

                if (abs(gap) > scaleTouchSlop) {// 滑动了
                    isItemExpendButNoMoved = false
                }
                scrollBy(gap.toInt(), 0)
                if (isLeftSwipe) {
                    if (scrollX < 0) {
                        scrollTo(0, 0)
                    }

                    if (scrollX > slideWidth) {
                        scrollTo(slideWidth, 0)
                    }
                } else {
                    if (scrollX > 0) {
                        scrollTo(0, 0)
                    }
                    if (scaleX < -slideWidth) {
                        scrollTo(-slideWidth, 0)
                    }
                }
                lastPoint.set(ev.rawX, ev.rawY)

            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                tracker.computeCurrentVelocity(1000, maxVelocity)
                val xVelocity = tracker.getXVelocity(firstPointId)
                if (abs(xVelocity) > 1000) {
                    if (xVelocity < -1000) {
                        if (isLeftSwipe) {
                            smoothExpand()
                        } else {
                            smoothClose()
                        }
                    } else {
                        if (isLeftSwipe) {
                            smoothClose()
                        } else {
                            smoothExpand()
                        }
                    }
                } else {
                    if (abs(scrollX) > slideDisLimit) {
                        smoothExpand()
                    } else {
                        smoothClose()
                    }
                }

                releaseVelocityTracker()
                isTouching = false
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {

            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_UP -> {
                if (isLeftSwipe) {
                    if (scrollX > scaleTouchSlop) {
                        if (ev.rawX < width - scrollX) {
                            if (isItemExpendButNoMoved) {
                                smoothClose()
                            }
                            return true
                        }
                    }
                } else {
                    if (-scrollX > scaleTouchSlop) {
                        if (ev.rawX > -scrollX) {
                            if (isItemExpendButNoMoved) {
                                smoothClose()
                            }
                            return true
                        }
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private var expandAnim: ValueAnimator? = null
    private var closeAnim: ValueAnimator? = null

    private fun smoothExpand() {

        viewCache = this

        cancelAnim()

        expandAnim = ValueAnimator.ofInt(scrollX, if (isLeftSwipe) slideWidth else -slideWidth).apply {
            addUpdateListener {
                scrollTo(it.animatedValue as Int, 0)
            }
            interpolator = OvershootInterpolator()
            duration = 300
        }
        expandAnim?.start()
    }

    private fun smoothClose() {

        viewCache = null

        cancelAnim()

        closeAnim = ValueAnimator.ofInt(scrollX, 0).apply {
            addUpdateListener {
                scrollTo(it.animatedValue as Int, 0)
            }
            interpolator = AccelerateInterpolator()
            duration = 200
        }
        closeAnim?.start()
    }

    private fun cancelAnim() {
        if (expandAnim?.isRunning == true) {
            expandAnim?.cancel()
        }

        if (closeAnim?.isRunning == true) {
            closeAnim?.cancel()
        }
    }

    private fun acquireVelocityTracker(event: MotionEvent) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)
    }

    private fun releaseVelocityTracker() {
        velocityTracker = velocityTracker?.run {
            clear()
            recycle()
            null
        }
    }

    override fun onDetachedFromWindow() {
        if (viewCache == this) {
            viewCache?.smoothClose()
            viewCache = null
        }
        super.onDetachedFromWindow()
    }

    fun quickClose() {
        if (viewCache == this) {
            cancelAnim()
            viewCache?.scrollTo(0, 0)
            viewCache = null
        }
    }

}