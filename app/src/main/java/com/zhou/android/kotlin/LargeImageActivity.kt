package com.zhou.android.kotlin

import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import kotlinx.android.synthetic.main.activity_large_image.*

/**
 * Created by zhou on 2020/5/30.
 */
class LargeImageActivity : BaseActivity() {

    override fun setContentView() {
        setContentView(R.layout.activity_large_image)

    }

    override fun init() {
        val input = assets.open("world.jpg")
        largeImage.setImageStream(input)
    }

    override fun addListener() {
    }

}