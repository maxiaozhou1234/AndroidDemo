package com.zhou.android.main

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.ui.FocusDrawView
import kotlinx.android.synthetic.main.layout_app.view.*

/**
 * Created by mxz on 2019/9/3.
 */
class FocusDrawActivity : BaseActivity() {

    private lateinit var focus: FocusDrawView
    private lateinit var b: Bitmap

    override fun setContentView() {

        b = BitmapFactory.decodeResource(resources, R.drawable.pic_head)

        val layout = RelativeLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        focus = FocusDrawView(this)
        layout.addView(focus)

        val btn = Button(this).apply {
            text = "加载位图"
            layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                addRule(RelativeLayout.CENTER_HORIZONTAL)
                bottomMargin = 20
            }
            setOnClickListener {
                focus.setBitmap(b)
            }
        }
        layout.addView(btn)

        setContentView(layout)
    }

    override fun init() {
    }

    override fun addListener() {
    }
}