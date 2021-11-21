package com.zhou.android.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.os.Build
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import com.zhou.android.R
import kotlin.math.abs

/**
 * 侧滑显示更多菜单：
 * 仿 com.github.mcxtzhang:SwipeDelMenuLayout:V1.3.0 ，实现菜单隐藏于 item 之下
 * 默认为扩展（Expand）模式，左滑打开
 * 更细致注释，移步 SwipeDelMenuLayout
 * @see <a href = "https://github.com/mcxtzhang/SwipeDelMenuLayout/blob/master/swipemenulib/src/main/java/com/mcxtzhang/swipemenulib/SwipeMenuLayout.java">SwipeMenuLayout</a>
 * <br/>
 * Created by mxz on 2020/10/15.
 */
class SlideMenuLayout @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyle: Int = 0)
    : ViewGroup(context, attributeSet, defStyle) {

    companion object {
        var isTouching = false
        var viewCache: SlideMenuLayout? = null

        const val LeftSwipe = 0
        const val RightSwipe = 1

        const val Expand = 0
        const val Cover = 1
    }

    private var slideWidth = 0 //菜单总宽度
    private var viewHeight = 0 //view 高度
    private var contentWidth = 0
    private var contentView: View? = null

    private var isLeftSwipe = true //左滑显示菜单
    private var isExpandStyle = true

    private var slideDisLimit = 0 //滑动触发最小距离

    private val lastPoint = PointF() //上一次触摸记录
    private val firstPoint = PointF()
    private var firstPointId = 0

    private val scaleTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val maxVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity.toFloat()

    private var velocityTracker: VelocityTracker? = null

    private var isItemExpendButNoMoved = true //菜单打开，但没有滑动操作，有点击，关闭菜单

    init {
        val ta = context.obtainStyledAttributes(attributeSet, R.styleable.SlideMenuLayout)
        isExpandStyle = ta.getInt(R.styleable.SlideMenuLayout_view_style, Expand) == Expand
        isLeftSwipe = ta.getInt(R.styleable.SlideMenuLayout_swipe_direction, LeftSwipe) == LeftSwipe
        ta.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        isClickable = true

        slideWidth = 0
        viewHeight = 0
//        var contentWidth = 0
        val measureMatchParentChildren = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY
        var isNeedMeasureHeight = false

        val contentIndex = if (isExpandStyle) 0 else childCount - 1

        for (i in 0.until(childCount)) {
            val child = getChildAt(i)
            child.isClickable = true
            if (child.visibility != View.GONE) {

                if (!isExpandStyle) {//覆盖模式下，把 elevation z高度属性抹除，否则会出现无法遮盖现象
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        child.stateListAnimator = null
                        child.elevation = 0f
                    }
                }

                measureChild(child, widthMeasureSpec, heightMeasureSpec)
                val lp = child.layoutParams as MarginLayoutParams
                viewHeight = viewHeight.coerceAtLeast(child.measuredHeight + lp.topMargin + lp.bottomMargin)
                if (measureMatchParentChildren && lp.height == LayoutParams.MATCH_PARENT) {
                    isNeedMeasureHeight = true
                }

                if (i == contentIndex) {//计算 item 最终宽度为第一个/最后一个 view 的宽度
                    contentWidth = child.measuredWidth
                } else {
                    slideWidth += child.measuredWidth
                }
            }
        }

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

        if (isExpandStyle) {//扩展模式
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
        } else {//覆盖模式
            left = contentWidth //拿到 item 宽度，用于左滑放置菜单
            for (i in 0.until(childCount)) {
                val child = getChildAt(i)
                if (child.visibility != View.GONE) {
                    if (i == childCount - 1) {
                        contentView = child
                        child.layout(paddingLeft, paddingTop, paddingLeft + child.measuredWidth, paddingTop + child.measuredHeight)
                    } else {

                        if (isLeftSwipe) {
                            child.layout(left - child.measuredWidth, paddingTop, left, paddingTop + child.measuredHeight)
                            left -= child.measuredWidth
                        } else {
                            child.layout(right, paddingTop, right + child.measuredWidth, paddingTop + child.measuredHeight)
                            right += child.measuredWidth
                        }
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
                if (abs(gap) > 10 || abs(getScrollX2()) > 10) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }

                if (abs(gap) > scaleTouchSlop) {// 滑动了
                    isItemExpendButNoMoved = false
                }
                scrollBy(gap.toInt(), 0)
                if (isLeftSwipe) {
                    if (getScrollX2() < 0) {
                        scrollTo(0, 0)
                    }

                    if (getScrollX2() > slideWidth) {
                        scrollTo(slideWidth, 0)
                    }
                } else {
                    if (getScrollX2() > 0) {
                        scrollTo(0, 0)
                    }
                    if (getScrollX2() < -slideWidth) {
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
                    if (abs(getScrollX2()) > slideDisLimit) {
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
                    if (getScrollX2() > scaleTouchSlop) {
                        if (ev.rawX < width - getScrollX2()) {
                            if (isItemExpendButNoMoved) {
                                smoothClose()
                            }
                            return true
                        }
                    }
                } else {
                    if (-getScrollX2() > scaleTouchSlop) {
                        if (ev.rawX > -getScrollX2()) {
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

        expandAnim = ValueAnimator.ofInt(getScrollX2(), if (isLeftSwipe) slideWidth else -slideWidth).apply {
            addUpdateListener {
                scrollTo(it.animatedValue as Int, 0)
            }
            interpolator = LinearInterpolator()
            duration = 300
        }
        expandAnim?.start()
    }

    private fun smoothClose() {

        viewCache = null

        cancelAnim()

        closeAnim = ValueAnimator.ofInt(getScrollX2(), 0).apply {
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

    override fun scrollBy(x: Int, y: Int) {
        if (isExpandStyle)
            super.scrollBy(x, y)
        else if (contentView != null)
            ViewCompat.offsetLeftAndRight(contentView!!, -x)
    }

    //扩展模式：把整个 ViewGroup 左移右移
    //覆盖模式：只能把最上面的 view 进行左右移动
    override fun scrollTo(x: Int, y: Int) {
        if (isExpandStyle)
            super.scrollTo(x, y)
        else if (contentView != null) {
            contentView!!.x = -x.toFloat()
        }
    }

    // 获取已移动的距离，为了兼容 scroller 和 getX 取值不同（相反），
    // 这里把 getX 值取反，因为调用判断时已经做了正数处理
    private fun getScrollX2(): Int {
        return if (isExpandStyle) {
            scrollX
        } else {
            (contentView?.x?.toInt() ?: 0) * -1
        }
    }
}