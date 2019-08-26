package com.zhou.android.main

import android.view.ViewGroup
import android.widget.LinearLayout
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.ui.FocusCircleView

/**
 * Created by mxz on 2019/8/26.
 */
class FocusAnimActivity : BaseActivity() {

    override fun setContentView() {

        val layout = LinearLayout(this)
        layout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layout.setBackgroundColor(resources.getColor(R.color.MediumTurquoise))

        val focus = FocusCircleView(this)
        focus.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        layout.addView(focus)

        setContentView(layout)
    }

    override fun init() {
    }

    override fun addListener() {
    }
}