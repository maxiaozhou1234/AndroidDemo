package com.zhou.android.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.zhou.android.common.Tools
import java.io.IOException
import java.io.InputStream
import kotlin.math.abs

/**
 * 鸿洋 LargeImage
 * Created by zhou on 2020/5/30.
 */
class LargeImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private var decoder: BitmapRegionDecoder? = null
    private var options = BitmapFactory.Options().apply {
        inPreferredConfig = Bitmap.Config.RGB_565
    }

    private val rect = Rect()
    private var imageWidth = 0
    private var imageHeight = 0

    fun setImageStream(inputStream: InputStream) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            imageWidth = options.outWidth
            imageHeight = options.outHeight
            Log.d("zhou", "setImageStream image[$imageWidth, $imageHeight]")
            decoder = BitmapRegionDecoder.newInstance(inputStream, false)
            requestLayout()
            invalidate()

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            Tools.closeIO(inputStream)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        Log.d("zhou", "measure image[$imageWidth, $imageHeight] view[$measuredWidth, $measuredHeight]")
        if (imageWidth < 0 || imageHeight < 0)
            return

        //设定初始显示图片位置
        if (measuredWidth > imageWidth) {
            rect.left = 0
            rect.right = imageWidth
        } else {
            rect.left = (imageWidth - measuredWidth) / 2
            rect.right = rect.left + measuredWidth
        }
        if (measuredHeight > imageHeight) {
            rect.top = 0
            rect.bottom = imageHeight
        } else {
            rect.top = (imageHeight - measuredHeight) / 2
            rect.bottom = rect.top + measuredHeight
        }
    }

    override fun onDraw(canvas: Canvas) {

        if (decoder == null) {
            return
        }

        try {
            val bitmap = decoder!!.decodeRegion(rect, options)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var lastX = 0f
    private var lastY = 0f
    private var downX = 0f
    private var downY = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY

                downX = event.rawX
                downY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - lastX
                val dy = event.rawY - lastY

                updateRect(dx, dy)

                lastX = event.rawX
                lastY = event.rawY

            }
            MotionEvent.ACTION_UP -> {
                if (abs(event.rawX - downX) < 50 && abs(event.rawY - downY) < 50) {
                    performClick()
                }
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun updateRect(dx: Float, dy: Float) {

//        Log.d("zhou", "update x:$dx, y:$dy")

        val w = rect.width()
        val h = rect.height()

        rect.left = (rect.left - dx).toInt()
        rect.right = (rect.right - dx).toInt()
        rect.top = (rect.top - dy).toInt()
        rect.bottom = (rect.bottom - dy).toInt()

        if (rect.left < 0) {
            rect.left = 0
            rect.right = w
        }
        if (rect.top < 0) {
            rect.top = 0
            rect.bottom = h
        }
        if (rect.right > imageWidth) {
            rect.right = imageWidth
            rect.left = rect.right - w
        }
        if (rect.bottom > imageHeight) {
            rect.bottom = imageHeight
            rect.top = imageHeight - h
        }
        invalidate()
    }

}