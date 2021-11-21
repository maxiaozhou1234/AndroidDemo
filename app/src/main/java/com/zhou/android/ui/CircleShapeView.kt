package com.zhou.android.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Created by mxz on 2019/9/3.
 */
class CircleShapeView : View {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        alpha = (255 * 0.4).toInt()
    }
    private val circlePath = Path()

    var innerRadius = 0f
        set(value) {
            if (value <= 0)
                throw IllegalArgumentException("innerRadius should positive.")
            field = value
            circlePath.reset()
            circlePath.addCircle(width / 2F, height / 2F, value, Path.Direction.CCW)
            postInvalidate()
        }
    private var faceBitmap: Bitmap? = null

    override fun onDraw(canvas: Canvas) {
        if (faceBitmap != null) {
            canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
            //截取中间显示
            canvas.clipPath(circlePath, Region.Op.INTERSECT)
            //将位图放到中间
            m.postTranslate(width / 2 - innerRadius, height / 2 - innerRadius)
            canvas.drawBitmap(faceBitmap!!, m, headPaint)
        } else if (innerRadius > 0) {
            canvas.clipRect(0F, 0F, width.toFloat(), height.toFloat())
            canvas.clipPath(circlePath, Region.Op.XOR)
            canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
        } else {
            canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
        }
    }

    private val headPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val m = Matrix()

    fun drawBitmap(bitmap: Bitmap) {

        if (faceBitmap != null) {
            return
        }

        val radius = if (innerRadius == 0f) 250f else innerRadius

        val size = (radius * 2).toInt()

        val w = size * 1f / bitmap.width
        val h = size * 1f / bitmap.height
        val scale = if (w < h) h else w
        m.reset()
        m.postTranslate((size - bitmap.width) / 2f, (size - bitmap.height) / 2f)
        m.postScale(-scale, scale, radius, radius)

        faceBitmap = bitmap

        postInvalidate()
        postDelayed({
            faceBitmap = null
            postInvalidate()
        }, 2000)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (faceBitmap != null) {
            faceBitmap?.recycle()
            faceBitmap = null
        }
    }

}