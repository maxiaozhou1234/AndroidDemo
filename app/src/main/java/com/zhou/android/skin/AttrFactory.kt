package com.zhou.android.skin

import android.view.View

/**
 * Created by mxz on 2021/3/18.
 */
fun getSkinAttr(view: View, attrName: String, id: Int, entryName: String, typeName: String): SkinAttr? {
    return when (attrName) {
        "background" -> BackgroundSkinAttr(view, attrName, id, entryName, typeName)
        "textColor" -> TextColorSkinAttr(view, attrName, id, entryName, typeName)
        else -> null
    }
}

private val supportAttrArray = arrayOf("background", "textColor")
fun isSupport(attrName: String) = supportAttrArray.contains(attrName)