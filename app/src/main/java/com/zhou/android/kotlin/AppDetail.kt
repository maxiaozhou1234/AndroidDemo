package com.zhou.android.kotlin

import android.graphics.drawable.Drawable

data class AppDetail(val packageName: String, var code: Long, var data: Long, var cache: Long, val icon: Drawable) {

    fun getAppString(): String {
        return "Code:${StorageUtil.getUnit(code)} Data:${StorageUtil.getUnit(data)} Cache:${StorageUtil.getUnit(cache)}"
    }

    fun getApp(): Long {
        return code + data + cache
    }
}