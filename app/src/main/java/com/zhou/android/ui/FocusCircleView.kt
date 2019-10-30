package com.zhou.android.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Created by mxz on 2019/8/26.
 */
class FocusCircleView : View {

    companion object {
        const val TAG = "zhou"
    }

    private var innerRadius = 360F
    private var outRadius = 400F
    private var bigRadius = 500F

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var centerX: Float = 0F
    private var centerY: Float = 0F
    private val rectF: RectF = RectF()
    private val bigRectF: RectF = RectF()
    private val oval: RectF = RectF()
    private var defaultAngle = 0F
    private var bigDefaultAngle = 280F
    private val circlePath = Path()

    private var animEnable = true
    private val valueAnimator = ValueAnimator.ofFloat(0F, 720F)
            .apply {
                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
                duration = 2500//1440
                interpolator = AccelerateDecelerateInterpolator()

                addUpdateListener { animator ->
                    defaultAngle = animator.animatedValue as Float
                    bigDefaultAngle = 280 - animator.animatedValue as Float
                    postInvalidate()
                }
            }

    init {
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#FF66A1CC")

        shadowPaint.style = Paint.Style.FILL
        shadowPaint.alpha = (255 * 0.4).toInt()

//        valueAnimator.start()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        centerX = width / 2F
        centerY = height / 2F
        Log.d(TAG, "size = [$width, $height],center = [$centerX, $centerY]")
        if (centerX > 0 && centerY > 0) {

            val base = if (centerX < centerY) centerX else centerY

            bigRadius = base / 4 * 3
            outRadius = bigRadius - 100
            innerRadius = outRadius - 40

            rectF.left = centerX - outRadius
            rectF.right = centerX + outRadius
            rectF.top = centerY - outRadius
            rectF.bottom = centerY + outRadius

            bigRectF.left = centerX - bigRadius
            bigRectF.right = centerX + bigRadius
            bigRectF.top = centerY - bigRadius
            bigRectF.bottom = centerY + bigRadius

            oval.left = centerX - innerRadius
            oval.top = centerY - innerRadius
            oval.right = centerX + innerRadius
            oval.bottom = centerY + innerRadius

            circlePath.addCircle(centerX, centerY, innerRadius, Path.Direction.CCW)
        }

        setMeasuredDimension(width, height)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.clipRect(0F, 0F, width.toFloat(), height.toFloat())
        canvas.clipPath(circlePath, Region.Op.XOR)
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), shadowPaint)

        paint.strokeWidth = 13F
        canvas.drawCircle(centerX, centerY, innerRadius, paint)

        paint.strokeWidth = 20F

        var startAngle = defaultAngle
        var sweepAngle = 60F
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)

        startAngle += sweepAngle + 30
        sweepAngle = 90F
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)

        startAngle += sweepAngle + 30
        sweepAngle = 120F
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)

        paint.strokeWidth = 15F
        startAngle = bigDefaultAngle
        sweepAngle = 160F
        canvas.drawArc(bigRectF, startAngle, sweepAngle, false, paint)
        startAngle += sweepAngle + 20
        canvas.drawArc(bigRectF, startAngle, sweepAngle, false, paint)

        defaultAngle += 5
        if (defaultAngle >= 360) {
            defaultAngle -= 360
        }

        bigDefaultAngle -= 6
        if (bigDefaultAngle <= 0) {
            bigDefaultAngle += 360
        }

        if (animEnable) {
            postInvalidateDelayed(20)
        }
    }

    fun startAnim() {
        if (!animEnable) {
            animEnable = true
            postInvalidateDelayed(20)
        }

        if (!valueAnimator.isRunning) {
            valueAnimator.cancel()
            valueAnimator.start()
        }
    }

    fun stopAnim() {
        animEnable = false

        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }
    }

    override fun onDetachedFromWindow() {
        animEnable = false
        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }
        super.onDetachedFromWindow()
    }
}