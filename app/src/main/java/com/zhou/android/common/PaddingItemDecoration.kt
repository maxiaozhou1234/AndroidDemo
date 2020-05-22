package com.zhou.android.common

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * 边距设置
 * Created by mxz on 2020/3/23.
 */
class PaddingItemDecoration(context: Context, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0)
    : RecyclerView.ItemDecoration() {
    private var leftPadding = 0
    private var topPadding = 0
    private var rightPadding = 0
    private var bottomPadding = 0

    init {
        val density = context.resources.displayMetrics.density
        leftPadding = (density * left).toInt()
        topPadding = (density * top).toInt()
        rightPadding = (density * right).toInt()
        bottomPadding = (density * bottom).toInt()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        outRect.set(leftPadding, topPadding, rightPadding, bottomPadding)
    }
}