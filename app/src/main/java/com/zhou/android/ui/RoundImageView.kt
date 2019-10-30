package com.zhou.android.ui

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.View

/**
 * Created by mxz on 2019/9/18.
 */
class RoundImageView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attr, defStyleAttr)

    var bitmap: Bitmap? = null
        set(value) {
            field = value
            postInvalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#ffffff")
        style = Paint.Style.FILL
    }
    private val m = Matrix()
    private val path = Path()

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas) {
        if (bitmap == null)
            return
        bitmap?.also {

            m.reset()
            val w = width * 1f / it.width
            val h = width * 1f / it.height
            val scale = Math.max(w, h)
            val p = width / 2f

            m.postTranslate((width * 1f - it.width) / 2, (height * 1f - it.height) / 2)
            m.postScale(scale, scale, p, p)
            canvas.clipPath(path)

            canvas.drawBitmap(it, m, paint)

        }
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = Math.min(width, height)
        setMeasuredDimension(size, size)
        val p = size / 2f
//        path.reset()
//        path.addCircle(p, p, p, Path.Direction.CCW)
//
//        rectF.apply {
//            top = 20f
//            left = 20f
//            bottom = size * 1f - 40
//            right = size * 1f - 40
//        }

        val r = radiusPercent * p
        path.reset()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            path.addRoundRect(0f, 0f, width * 1f, width * 1f, r, r, Path.Direction.CCW)
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bitmap = bitmap?.run {
            if (!isRecycled) {
                recycle()
            }
            null
        }
    }

    var radiusPercent = 0.2f
        set(value) {
            if (radiusPercent in 0f..1f) {
                field = value
                val r = radiusPercent * width / 2
                path.reset()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    path.addRoundRect(0f, 0f, width * 1f, width * 1f, r, r, Path.Direction.CCW)
                }
                postInvalidate()
            }
        }

}