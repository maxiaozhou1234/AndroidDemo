package com.zhou.android.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.zhou.android.R

/**
 * Created by mxz on 2019/9/19.
 */
class XfermodeView : View {

    private var center = 100f
    private val rectf = Rect()

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attr, defStyleAttr)

    private val rect: Bitmap by lazy { createRect() }
    private val circle: Bitmap by lazy { createCircle() }
    private val icon: Bitmap by lazy { BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher) }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val size = Math.min(w, h)
        setMeasuredDimension(size, size)

        center = size / 2f
        rectf.apply {
            top = 0
            left = 0
            bottom = size
            right = size
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFEF5350")//FF26A69A
    }
    var xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        set(value) {
            field = value
            postInvalidate()
        }

    override fun onDraw(canvas: Canvas) {

        paint.xfermode = null
//        canvas.drawBitmap(rect, rectf, rectf, paint)//像官方示例图显示
        canvas.drawBitmap(icon, null, rectf, paint)//用图像显示，效果更易理解
        paint.xfermode = xfermode
        canvas.drawBitmap(circle, rectf, rectf, paint)

        //直接输出到屏幕的效果与实际不符，该模式只对 bitmap 有效
//        canvas.drawARGB(255, 255, 255, 255)
//        paint.xfermode = null
//        paint.color = Color.parseColor("#FFEF5350")
//        canvas.drawRect(0f, 0f, center, center, paint)
//        paint.xfermode = xfermode
//        paint.color = Color.parseColor("#FF26A69A")
//        canvas.drawCircle(center * 1.2f, center * 1.2f, center * 0.8f, paint)

    }

    private fun createRect(): Bitmap {
        val dst = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(dst)
        val paint = Paint()
        paint.color = Color.parseColor("#FFEF5350")
        canvas.drawRect(0f, 0f, center, center, paint)
        return dst
    }

    private fun createCircle(): Bitmap {
        val dst = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(dst)
        val paint = Paint()
        paint.color = Color.parseColor("#FF26A69A")
        canvas.drawCircle(center * 1.2f, center * 1.2f, center * 0.8f, paint)
        return dst
    }

    fun output(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
}