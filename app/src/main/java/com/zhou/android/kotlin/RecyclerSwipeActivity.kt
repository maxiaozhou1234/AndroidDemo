package com.zhou.android.kotlin

import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import kotlinx.android.synthetic.main.activity_recycler_swipe.*

/**
 * Created by zhou on 2020/8/13.
 */
class RecyclerSwipeActivity : BaseActivity() {

    override fun setContentView() {
        setContentView(R.layout.activity_recycler_swipe)
    }

    override fun init() {
    }

    override fun addListener() {
        btnReset.setOnClickListener {
            recyclerSwipe.reset()
        }
    }
}