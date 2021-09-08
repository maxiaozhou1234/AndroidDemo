package com.zhou.android.skin

import android.content.Context
import android.support.v4.view.LayoutInflaterFactory
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View

/**
 * Created by mxz on 2021/3/17.
 */
class SkinFactory : LayoutInflaterFactory {

    private var skinItem: SkinItem? = null

    override fun onCreateView(parent: View?, name: String?, context: Context, attrs: AttributeSet): View? {
        var view: View? = null
        Log.d("zhou", "create name $name")
//        if (name?.contains("Layout") == false) {
        try {
            if (-1 == name?.indexOf('.')) {
                if ("View" == name) {//"Button" == name || "TextView" == name
                    view = LayoutInflater.from(context).createView(name, "android.view.", attrs)
                }
                if (view == null) {
                    view = LayoutInflater.from(context).createView(name, "android.widget.", attrs)
                }
                if (view == null) {
                    view = LayoutInflater.from(context).createView(name, "android.webkit.", attrs)
                }
            } else {
                view = LayoutInflater.from(context).createView(name, null, attrs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.e("zhou", "view: $name, ${view?.javaClass?.name}")
        if (view != null) {
            parseSkinAttr(context, attrs, view)
        }
//        }
        return view
    }

    private fun parseSkinAttr(context: Context, attrs: AttributeSet, view: View) {
        val list = ArrayList<SkinAttr>()
        for (i in 0.until(attrs.attributeCount)) {
            val name = attrs.getAttributeName(i)//属性名
            val value = attrs.getAttributeValue(i)//属性值
            Log.d("zhou", "parse >> $name, $value")
            if (isSupport(name) && value.startsWith("@")) {
                try {
                    val id = value.substring(1).toInt()
                    val entryName = context.resources.getResourceEntryName(id)//资源文件名称
                    val typeName = context.resources.getResourceTypeName(id)//color drawable
                    Log.d("zhou", "  id=$id, entryName = $entryName, typeName = $typeName")
                    val skinAttr = getSkinAttr(view, name, id, entryName, typeName)
                    if (skinAttr != null) {
                        list.add(skinAttr)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        Log.i("zhou", "[${view.javaClass.simpleName}] skin attr size = ${list.size}")
        if (list.isNotEmpty()) {
            skinItem = SkinItem(list).also {
                it.apply()
            }
            SkinManager.addObserver(skinItem)
        }
    }

    fun destroy() {
        if (skinItem != null) {
            SkinManager.deleteObserver(skinItem)
            skinItem = null
        }
    }

}