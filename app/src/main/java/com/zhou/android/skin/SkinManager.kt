package com.zhou.android.skin

import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import com.zhou.android.R
import com.zhou.android.ZApplication
import java.util.*

/**
 * Created by mxz on 2021/3/17.
 */
object SkinManager : Observable() {

    private val resource = ZApplication.context.resources

    private val daySkin = mapOf(
            "shape_button" to R.drawable.shape_button_day,
            "text_color" to R.color.text_color_day,
            "button_color" to R.color.button_color_day,
            "page_color" to R.color.page_color_day,
            "shape_linear" to R.drawable.shape_linear_day,
            "shape_skin_item" to R.drawable.shape_skin_item_day)

    private val nightSkin = mapOf(
            "shape_button" to R.drawable.shape_button_night,
            "text_color" to R.color.text_color_night,
            "button_color" to R.color.button_color_night,
            "page_color" to R.color.page_color_night,
            "shape_linear" to R.drawable.shape_linear_night,
            "shape_skin_item" to R.drawable.shape_skin_item_night)

    private var map = daySkin

    fun isDayMode() = map === daySkin

    fun switchMode() = switchMode(!isDayMode())

    fun switchMode(dayMode: Boolean = true) {
        map = if (dayMode) daySkin else nightSkin

        setChanged()
        notifyObservers()
    }

    //资源名称
    fun getDrawable(entryName: String): Drawable {
        val id = map[entryName] ?: R.drawable.shape_button_day
        return resource.getDrawable(id)
    }

    //资源名称
    fun getDrawableRes(entryName: String): Int {
        val name = entryName.substring(0, entryName.lastIndexOf("_"))
        return map[name] ?: R.drawable.shape_button_day
    }

    //属性名称
    @ColorInt
    fun getColor(entryName: String): Int {
        val name = entryName.substring(0, entryName.lastIndexOf("_"))
        val id = map[name] ?: R.color.text_color_day
        return resource.getColor(id)
    }
}