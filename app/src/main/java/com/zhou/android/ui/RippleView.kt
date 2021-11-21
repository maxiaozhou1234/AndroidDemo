package com.zhou.android.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.ceil

/**
 * 模拟波纹涟漪
 * 正常涟漪扩散应越来越慢，这里简化为匀速扩散
 * Created by mxz on 2020/9/21.
 */
class RippleView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, def: Int = 0)
    : View(context, attributeSet, def) {

    private var center = 0f
    private var startRadius = 5f
    private var viewRadius = 10f

    private val rippleCount = 8
    private var rippleDiff = 10f//间距

    private var maxLimit = 1f

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#FF83B0FF")
        strokeWidth = resources.displayMetrics.density * 2f + 0.5f
    }

    //渐变遮罩，使外层透明
    private val shapeGradient: RadialGradient by lazy {
        RadialGradient(center, center, viewRadius, intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.BLACK), floatArrayOf(0f, 0.68f, 1f), Shader.TileMode.CLAMP)
    }
    private val shapePaint: Paint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            shader = shapeGradient
        }
    }

    //扩散刷新
    private val spreadAnimator: ValueAnimator by lazy {
        ValueAnimator.ofFloat(0f, rippleDiff).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = 1500
            interpolator = LinearInterpolator()
            addUpdateListener {
                curSpread = it.animatedValue as Float
                postInvalidate()
            }
        }
    }

    private var curSpread = 5f //当前偏移量
    private var maxSpread = 1f //最大偏移量

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val ws = MeasureSpec.getSize(widthMeasureSpec)
        val wm = MeasureSpec.getMode(widthMeasureSpec)
        val hs = MeasureSpec.getSize(heightMeasureSpec)
        val hm = MeasureSpec.getMode(heightMeasureSpec)

        var w: Int
        var h: Int
        if (wm == MeasureSpec.EXACTLY && hm == MeasureSpec.EXACTLY) {
            w = ws.coerceAtMost(hs).also {
                h = it
            }
        } else if (wm == MeasureSpec.EXACTLY) {
            w = ws.also {
                h = it
            }
        } else if (hm == MeasureSpec.EXACTLY) {
            h = hs.also {
                w = it
            }
        } else {
            w = 300
            h = 300
        }
        //获取中心位置
        center = (w / 2f).also {
            startRadius = it * 0.58f
        }
        //最大扩散距离 = 视图宽度一半 - 内圆半径
        maxSpread = center - startRadius
        //圆间距
        rippleDiff = ceil((center - startRadius - 4.0) / rippleCount).toFloat()
        viewRadius = center + rippleDiff + 6f//设置大一点的空间，避免最外面的圈闪烁
        //最大圆的半径
        maxLimit = center + rippleDiff + 4f
        Log.d("ripple", "center = $center, startRadius = $startRadius, viewRadius = $viewRadius, maxSpread = $maxSpread, rippleDif = $rippleDiff")
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {

        for (i in rippleCount.downTo(0)) {
            var r = startRadius + i * rippleDiff + curSpread
            if (r > maxLimit) {//将超出的距离的圆圈重置为靠近中心的圈
                r -= maxSpread
            }
            canvas.drawCircle(center, center, r, circlePaint)
        }
        canvas.drawCircle(center, center, viewRadius, shapePaint)

        canvas.drawCircle(center, center, startRadius, circlePaint)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun start() {
        postDelayed({
            spreadAnimator.start()
        }, 800)
    }

    private fun stop() {
        if (spreadAnimator.isRunning) {
            spreadAnimator.cancel()
        }
    }
}