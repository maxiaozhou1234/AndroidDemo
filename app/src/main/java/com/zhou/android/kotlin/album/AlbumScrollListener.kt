package com.zhou.android.kotlin.album

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SnapHelper

open class AlbumScrollListener : RecyclerView.OnScrollListener {

    private var helper: SnapHelper
    private var currentPosition = RecyclerView.NO_POSITION

    constructor(helper: SnapHelper) {
        this@AlbumScrollListener.helper = helper
    }

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val layoutManager = recyclerView?.layoutManager
        var position = 0
        layoutManager?.also {
            val view = helper.findSnapView(it)
            view?.apply {
                position = it.getPosition(this)
            }
        }
        if (position == RecyclerView.NO_POSITION) {
            return
        }
        if (position != currentPosition) {
            currentPosition = position
            onPageSelected(position)
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (currentPosition == RecyclerView.NO_POSITION)
            return
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            onPageScrolled(currentPosition, 0f, 0)
        }
    }

    public fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    open fun onPageSelected(position: Int) {

    }

}