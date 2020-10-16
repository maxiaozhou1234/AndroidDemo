package com.zhou.android.main

import android.animation.ObjectAnimator
import android.view.animation.AnticipateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.common.toast
import kotlinx.android.synthetic.main.activity_slide_menu.*

/**
 * 滑动显示更多菜单
 * Created by mxz on 2020/10/15.
 */
class SlideMenuActivity : BaseActivity() {

    private var width = 0f

    override fun setContentView() {
        setContentView(R.layout.activity_slide_menu)
        view.post {
            width = resources.displayMetrics.widthPixels.toFloat() - view.width - 50f
        }
    }

    override fun addListener() {
        btnA.setOnClickListener {
            ObjectAnimator.ofFloat(view, "translationX", 50f, width).apply {
                duration = 1200
                interpolator = AnticipateOvershootInterpolator()
            }.start()
        }

        btnB.setOnClickListener {
            ObjectAnimator.ofFloat(view, "translationX", 50f, width).apply {
                duration = 1200
                interpolator = AnticipateInterpolator()
            }.start()
        }

        btnC.setOnClickListener {
            ObjectAnimator.ofFloat(view, "translationX", 50f, width).apply {
                duration = 1200
                interpolator = OvershootInterpolator()
            }.start()
        }

        btnReset.setOnClickListener {
            view.translationX = 0f
            view.translationY = 0f
        }

        tv1.setOnClickListener {
            toast("选项 A")
        }
        tv2.setOnClickListener {
            toast("选项 B")
        }
    }

    override fun init() {
    }
}