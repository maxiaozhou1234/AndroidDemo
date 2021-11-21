package com.zhou.android.skin

import android.view.View
import android.widget.Button
import android.widget.TextView
import java.util.*

/**
 * Created by mxz on 2021/3/17.
 */
class SkinItem(private val data: List<SkinAttr>) : Observer {

    override fun update(o: Observable?, arg: Any?) {
        apply()
    }

    fun apply() {
        for (attr in data) {
            attr.apply()
        }
    }

}

class BackgroundSkinAttr(val view: View, val attrName: String, val id: Int, val entryName: String, val typeName: String) : SkinAttr() {
    override fun apply() {
        when (typeName) {
            "color" -> view.setBackgroundColor(SkinManager.getColor(entryName))
            "drawable" -> view.setBackgroundResource(SkinManager.getDrawableRes(entryName))
        }
    }
}

class TextColorSkinAttr(val view: View, val attrName: String, val id: Int, val entryName: String, val typeName: String) : SkinAttr() {
    override fun apply() {
        if (view is Button || view is TextView) {
            (view as TextView).setTextColor(SkinManager.getColor(entryName))
        }
    }

}